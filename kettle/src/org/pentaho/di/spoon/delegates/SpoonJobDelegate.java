package org.pentaho.di.spoon.delegates;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.dialog.ErrorDialog;
import org.pentaho.di.core.dialog.ShowMessageDialog;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.gui.GUIResource;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.gui.SpoonInterface;
import org.pentaho.di.core.undo.TransAction;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.JobEntryLoader;
import org.pentaho.di.job.JobEntryType;
import org.pentaho.di.job.JobHopMeta;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.JobPlugin;
import org.pentaho.di.job.entries.special.JobEntrySpecial;
import org.pentaho.di.job.entries.sql.JobEntrySQL;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.spoon.Messages;
import org.pentaho.di.spoon.Spoon;
import org.pentaho.di.spoon.TabMapEntry;
import org.pentaho.di.spoon.job.JobGraph;
import org.pentaho.di.spoon.job.JobHistory;
import org.pentaho.di.spoon.job.JobHistoryRefresher;
import org.pentaho.di.spoon.job.JobLog;
import org.pentaho.di.spoon.wizards.RipDatabaseWizardPage1;
import org.pentaho.di.spoon.wizards.RipDatabaseWizardPage2;
import org.pentaho.di.spoon.wizards.RipDatabaseWizardPage3;
import org.pentaho.di.trans.StepLoader;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.xul.swt.tab.TabItem;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class SpoonJobDelegate extends SpoonDelegate
{
	/**
	 * This contains a map between the name of a transformation and the
	 * TransMeta object. If the transformation has no name it will be mapped
	 * under a number [1], [2] etc.
	 */
	private Map<String, JobMeta> jobMap;

	public SpoonJobDelegate(Spoon spoon)
	{
		super(spoon);
		jobMap = new Hashtable<String, JobMeta>();
	}

	public JobEntryCopy newJobEntry(JobMeta jobMeta, String type_desc, boolean openit)
	{
		JobEntryLoader jobLoader = JobEntryLoader.getInstance();
		JobPlugin jobPlugin = null;

		try
		{
			jobPlugin = jobLoader.findJobEntriesWithDescription(type_desc);
			if (jobPlugin == null)
			{
				// Check if it's not START or DUMMY
				if (JobMeta.STRING_SPECIAL_START.equals(type_desc)
						|| JobMeta.STRING_SPECIAL_DUMMY.equals(type_desc))
				{
					jobPlugin = jobLoader.findJobEntriesWithID(JobMeta.STRING_SPECIAL);
				}
			}

			if (jobPlugin != null)
			{
				// Determine name & number for this entry.
				String basename = type_desc;
				int nr = jobMeta.generateJobEntryNameNr(basename);
				String entry_name = basename + " " + nr; //$NON-NLS-1$

				// Generate the appropriate class...
				JobEntryInterface jei = jobLoader.getJobEntryClass(jobPlugin);
				jei.setName(entry_name);

				if (jei.isSpecial())
				{
					if (JobMeta.STRING_SPECIAL_START.equals(type_desc))
					{
						// Check if start is already on the canvas...
						if (jobMeta.findStart() != null)
						{
							JobGraph.showOnlyStartOnceMessage(spoon.getShell());
							return null;
						}
						((JobEntrySpecial) jei).setStart(true);
						jei.setName(JobMeta.STRING_SPECIAL_START);
					}
					if (JobMeta.STRING_SPECIAL_DUMMY.equals(type_desc))
					{
						((JobEntrySpecial) jei).setDummy(true);
						jei.setName(JobMeta.STRING_SPECIAL_DUMMY);
					}
				}

				if (openit)
				{
					JobEntryDialogInterface d = getJobEntryDialog(jei, jobMeta);
					if (d != null && d.open() != null)
					{
						JobEntryCopy jge = new JobEntryCopy();
						jge.setEntry(jei);
						jge.setLocation(50, 50);
						jge.setNr(0);
						jobMeta.addJobEntry(jge);
						spoon.addUndoNew(jobMeta, new JobEntryCopy[] { jge }, new int[] { jobMeta
								.indexOfJobEntry(jge) });
						spoon.refreshGraph();
						spoon.refreshTree();
						return jge;
					} else
					{
						return null;
					}
				} else
				{
					JobEntryCopy jge = new JobEntryCopy();
					jge.setEntry(jei);
					jge.setLocation(50, 50);
					jge.setNr(0);
					jobMeta.addJobEntry(jge);
					spoon.addUndoNew(jobMeta, new JobEntryCopy[] { jge }, new int[] { jobMeta
							.indexOfJobEntry(jge) });
					spoon.refreshGraph();
					spoon.refreshTree();
					return jge;
				}
			} else
			{
				return null;
			}
		} catch (Throwable e)
		{
			new ErrorDialog(
					spoon.getShell(),
					Messages
							.getString("Spoon.ErrorDiaspoon.getLog().UnexpectedErrorCreatingNewJobGraphEntry.Title"), Messages.getString("Spoon.ErrorDiaspoon.getLog().UnexpectedErrorCreatingNewJobGraphEntry.Message"), new Exception(e)); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
	}

	public JobEntryDialogInterface getJobEntryDialog(JobEntryInterface jei, JobMeta jobMeta)
	{

		String dialogClassName = jei.getDialogClassName();
		try
		{
			Class<?> dialogClass;
			Class<?>[] paramClasses = new Class[] { spoon.getShell().getClass(), JobEntryInterface.class,
					Repository.class, JobMeta.class };
			Object[] paramArgs = new Object[] { spoon.getShell(), jei, spoon.getRepository(), jobMeta };
			Constructor<?> dialogConstructor;
			dialogClass = JobEntryLoader.getInstance().loadClass(jei.getDescription(),dialogClassName);
			dialogConstructor = dialogClass.getConstructor(paramClasses);
			return (JobEntryDialogInterface) dialogConstructor.newInstance(paramArgs);
		} catch (Throwable t)
		{
			spoon.getLog().logError("Could not create dialog for " + dialogClassName, t.getMessage());
		}
		return null;
	}

	public StepDialogInterface getStepEntryDialog(StepMetaInterface stepMeta, TransMeta transMeta,
			String stepName)
	{

		String dialogClassName = stepMeta.getDialogClassName();
		try
		{
			Class<?> dialogClass;
			Class<?>[] paramClasses = new Class[] { spoon.getShell().getClass(), Object.class,
					TransMeta.class, String.class };
			Object[] paramArgs = new Object[] { spoon.getShell(), stepMeta, transMeta, stepName };
			Constructor<?> dialogConstructor;
			dialogClass = stepMeta.getClass().getClassLoader().loadClass(dialogClassName);
			dialogConstructor = dialogClass.getConstructor(paramClasses);
			return (StepDialogInterface) dialogConstructor.newInstance(paramArgs);
		} catch (Throwable t)
		{
			spoon.getLog().logError("Could not create dialog for " + dialogClassName, t.getMessage());
		}
		return null;
	}

	public void editJobEntry(JobMeta jobMeta, JobEntryCopy je)
	{
		try
		{
			spoon.getLog().logBasic(toString(), "edit job graph entry: " + je.getName()); //$NON-NLS-1$

			JobEntryCopy before = (JobEntryCopy) je.clone_deep();
			boolean entry_changed = false;

			JobEntryInterface jei = je.getEntry();

			if (jei.isSpecial())
			{
				JobEntrySpecial special = (JobEntrySpecial) jei;
				if (special.isDummy())
					return;
			}

			JobEntryDialogInterface d = spoon.getJobEntryDialog(jei, jobMeta);
			if (d != null)
			{
				if (d.open() != null)
				{
					entry_changed = true;
				}

				if (entry_changed)
				{
					JobEntryCopy after = (JobEntryCopy) je.clone();
					spoon.addUndoChange(jobMeta, new JobEntryCopy[] { before }, new JobEntryCopy[] { after },
							new int[] { jobMeta.indexOfJobEntry(je) });
					spoon.refreshGraph();
					spoon.refreshTree();
				}
			} else
			{
				MessageBox mb = new MessageBox(spoon.getShell(), SWT.OK | SWT.ICON_INFORMATION);
				mb.setMessage(Messages.getString("Spoon.Diaspoon.getLog().JobEntryCanNotBeChanged.Message")); //$NON-NLS-1$
				mb.setText(Messages.getString("Spoon.Diaspoon.getLog().JobEntryCanNotBeChanged.Title")); //$NON-NLS-1$
				mb.open();
			}

		} catch (Exception e)
		{
			if (!spoon.getShell().isDisposed())
				new ErrorDialog(
						spoon.getShell(),
						Messages.getString("Spoon.ErrorDiaspoon.getLog().ErrorEditingJobEntry.Title"), Messages.getString("Spoon.ErrorDiaspoon.getLog().ErrorEditingJobEntry.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public JobEntryTrans newJobEntry(JobMeta jobMeta, JobEntryType type)
	{
		JobEntryTrans je = new JobEntryTrans();
		je.setJobEntryType(type);
		String basename = type.getDescription();
		int nr = jobMeta.generateJobEntryNameNr(basename);
		je.setName(basename + " " + nr); //$NON-NLS-1$

		spoon.setShellText();

		return je;
	}

	public void deleteJobEntryCopies(JobMeta jobMeta, JobEntryCopy jobEntry)
	{
		String name = jobEntry.getName();
		// TODO Show warning "Are you sure? This operation can't be undone." +
		// clear undo buffer.

		// First delete all the hops using entry with name:
		JobHopMeta hi[] = jobMeta.getAllJobHopsUsing(name);
		if (hi.length > 0)
		{
			int hix[] = new int[hi.length];
			for (int i = 0; i < hi.length; i++)
				hix[i] = jobMeta.indexOfJobHop(hi[i]);

			spoon.addUndoDelete(jobMeta, hi, hix);
			for (int i = hix.length - 1; i >= 0; i--)
				jobMeta.removeJobHop(hix[i]);
		}

		// Then delete all the entries with name:
		JobEntryCopy je[] = jobMeta.getAllJobGraphEntries(name);
		int jex[] = new int[je.length];
		for (int i = 0; i < je.length; i++)
			jex[i] = jobMeta.indexOfJobEntry(je[i]);

		if (je.length > 0)
			spoon.addUndoDelete(jobMeta, je, jex);
		for (int i = jex.length - 1; i >= 0; i--)
			jobMeta.removeJobEntry(jex[i]);

		jobMeta.clearUndo();
		spoon.setUndoMenu(jobMeta);
		spoon.refreshGraph();
		spoon.refreshTree();
	}

	public void dupeJobEntry(JobMeta jobMeta, JobEntryCopy jobEntry)
	{
		if (jobEntry != null && !jobEntry.isStart())
		{
			JobEntryCopy dupejge = (JobEntryCopy) jobEntry.clone();
			dupejge.setNr(jobMeta.findUnusedNr(dupejge.getName()));
			if (dupejge.isDrawn())
			{
				Point p = jobEntry.getLocation();
				dupejge.setLocation(p.x + 10, p.y + 10);
			}
			jobMeta.addJobEntry(dupejge);
			spoon.refreshGraph();
			spoon.refreshTree();
			spoon.setShellText();
		}
	}

	public void copyJobEntries(JobMeta jobMeta, JobEntryCopy jec[])
	{
		if (jec == null || jec.length == 0)
			return;

		String xml = XMLHandler.getXMLHeader();
		xml += "<jobentries>" + Const.CR; //$NON-NLS-1$

		for (int i = 0; i < jec.length; i++)
		{
			xml += jec[i].getXML();
		}

		xml += "    </jobentries>" + Const.CR; //$NON-NLS-1$

		spoon.toClipboard(xml);
	}

	public void pasteXML(JobMeta jobMeta, String clipcontent, Point loc)
	{
		try
		{
			Document doc = XMLHandler.loadXMLString(clipcontent);

			// De-select all, re-select pasted steps...
			jobMeta.unselectAll();

			Node entriesnode = XMLHandler.getSubNode(doc, "jobentries"); //$NON-NLS-1$
			int nr = XMLHandler.countNodes(entriesnode, "entry"); //$NON-NLS-1$
			spoon.getLog()
					.logDebug(toString(), "I found " + nr + " job entries to paste on location: " + loc); //$NON-NLS-1$ //$NON-NLS-2$
			JobEntryCopy entries[] = new JobEntryCopy[nr];

			// Point min = new Point(loc.x, loc.y);
			Point min = new Point(99999999, 99999999);

			for (int i = 0; i < nr; i++)
			{
				Node entrynode = XMLHandler.getSubNodeByNr(entriesnode, "entry", i); //$NON-NLS-1$
				entries[i] = new JobEntryCopy(entrynode, jobMeta.getDatabases(), spoon.getRepository());

				String name = jobMeta.getAlternativeJobentryName(entries[i].getName());
				entries[i].setName(name);

				if (loc != null)
				{
					Point p = entries[i].getLocation();

					if (min.x > p.x)
						min.x = p.x;
					if (min.y > p.y)
						min.y = p.y;
				}
			}

			// What's the difference between loc and min?
			// This is the offset:
			Point offset = new Point(loc.x - min.x, loc.y - min.y);

			// Undo/redo object positions...
			int position[] = new int[entries.length];

			for (int i = 0; i < entries.length; i++)
			{
				Point p = entries[i].getLocation();
				String name = entries[i].getName();

				entries[i].setLocation(p.x + offset.x, p.y + offset.y);

				// Check the name, find alternative...
				entries[i].setName(jobMeta.getAlternativeJobentryName(name));
				jobMeta.addJobEntry(entries[i]);
				position[i] = jobMeta.indexOfJobEntry(entries[i]);
			}

			// Save undo information too...
			spoon.addUndoNew(jobMeta, entries, position);

			if (jobMeta.hasChanged())
			{
				spoon.refreshTree();
				spoon.refreshGraph();
			}
		} catch (KettleException e)
		{
			new ErrorDialog(
					spoon.getShell(),
					Messages.getString("Spoon.ErrorDiaspoon.getLog().ErrorPasingJobEntries.Title"), Messages.getString("Spoon.ErrorDiaspoon.getLog().ErrorPasingJobEntries.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public void newJobHop(JobMeta jobMeta, JobEntryCopy fr, JobEntryCopy to)
	{
		JobHopMeta hi = new JobHopMeta(fr, to);
		jobMeta.addJobHop(hi);
		spoon.addUndoNew(jobMeta, new JobHopMeta[] { hi }, new int[] { jobMeta.indexOfJobHop(hi) });
		spoon.refreshGraph();
		spoon.refreshTree();
	}

	/**
	 * Create a job that extracts tables & data from a database.
	 * <p>
	 * <p>
	 * 
	 * 0) Select the database to rip
	 * <p>
	 * 1) Select the tables in the database to rip
	 * <p>
	 * 2) Select the database to dump to
	 * <p>
	 * 3) Select the repository directory in which it will end up
	 * <p>
	 * 4) Select a name for the new job
	 * <p>
	 * 5) Create an empty job with the selected name.
	 * <p>
	 * 6) Create 1 transformation for every selected table
	 * <p>
	 * 7) add every created transformation to the job & evaluate
	 * <p>
	 * 
	 */
	public void ripDBWizard()
	{
		final List<DatabaseMeta> databases = spoon.getActiveDatabases();
		if (databases.size() == 0)
			return; // Nothing to do here

		final RipDatabaseWizardPage1 page1 = new RipDatabaseWizardPage1("1", databases); //$NON-NLS-1$
		page1.createControl(spoon.getShell());
		final RipDatabaseWizardPage2 page2 = new RipDatabaseWizardPage2("2"); //$NON-NLS-1$
		page2.createControl(spoon.getShell());
		final RipDatabaseWizardPage3 page3 = new RipDatabaseWizardPage3("3", spoon.getRepository()); //$NON-NLS-1$
		page3.createControl(spoon.getShell());

		Wizard wizard = new Wizard()
		{
			public boolean performFinish()
			{
				JobMeta jobMeta = ripDB(databases, page3.getJobname(), page3.getRepositoryDirectory(), page3
						.getDirectory(), page1.getSourceDatabase(), page1.getTargetDatabase(), page2
						.getSelection());
				if (jobMeta == null)
					return false;

				if (page3.getRepositoryDirectory() != null)
				{
					spoon.saveToRepository(jobMeta);
				} else
				{
					spoon.saveToFile(jobMeta);
				}

				addJobGraph(jobMeta);
				return true;
			}

			/**
			 * @see org.eclipse.jface.wizard.Wizard#canFinish()
			 */
			public boolean canFinish()
			{
				return page3.canFinish();
			}
		};

		wizard.addPage(page1);
		wizard.addPage(page2);
		wizard.addPage(page3);

		WizardDialog wd = new WizardDialog(spoon.getShell(), wizard);
		wd.setMinimumPageSize(700, 400);
		wd.open();
	}

	public JobMeta ripDB(final List<DatabaseMeta> databases, final String jobname,
			final RepositoryDirectory repdir, final String directory, final DatabaseMeta sourceDbInfo,
			final DatabaseMeta targetDbInfo, final String[] tables)
	{
		//
		// Create a new job...
		//
		final JobMeta jobMeta = new JobMeta(spoon.getLog());
		jobMeta.setDatabases(databases);
		jobMeta.setFilename(null);
		jobMeta.setName(jobname);

		if (spoon.getRepository() != null)
		{
			jobMeta.setDirectory(repdir);
		} else
		{
			jobMeta.setFilename(Const.createFilename(directory, jobname, Const.STRING_JOB_DEFAULT_EXT));
		}

		spoon.refreshTree();
		spoon.refreshGraph();

		final Point location = new Point(50, 50);

		// The start entry...
		final JobEntryCopy start = JobMeta.createStartEntry();
		start.setLocation(new Point(location.x, location.y));
		start.setDrawn();
		jobMeta.addJobEntry(start);

		// final Thread parentThread = Thread.currentThread();

		// Create a dialog with a progress indicator!
		IRunnableWithProgress op = new IRunnableWithProgress()
		{
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
			{
				// This is running in a new process: copy some KettleVariables
				// info
				// LocalVariables.getInstance().createKettleVariables(Thread.currentThread().getName(),
				// parentThread.getName(), true);

				monitor.beginTask(Messages.getString("Spoon.RipDB.Monitor.BuildingNewJob"), tables.length); //$NON-NLS-1$
				monitor.worked(0);
				JobEntryCopy previous = start;

				// Loop over the table-names...
				for (int i = 0; i < tables.length && !monitor.isCanceled(); i++)
				{
					monitor
							.setTaskName(Messages.getString("Spoon.RipDB.Monitor.ProcessingTable") + tables[i] + "]..."); //$NON-NLS-1$ //$NON-NLS-2$
					//
					// Create the new transformation...
					//
					String transname = Messages.getString("Spoon.RipDB.Monitor.Transname1") + sourceDbInfo + "].[" + tables[i] + Messages.getString("Spoon.RipDB.Monitor.Transname2") + targetDbInfo + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

					TransMeta transMeta = new TransMeta((String) null, transname, null);

					if (repdir != null)
					{
						transMeta.setDirectory(repdir);
					} else
					{
						transMeta.setFilename(Const.createFilename(directory, transname,
								Const.STRING_TRANS_DEFAULT_EXT));
					}

					// Add the source & target db
					transMeta.addDatabase(sourceDbInfo);
					transMeta.addDatabase(targetDbInfo);

					//
					// Add a note
					//
					String note = Messages.getString("Spoon.RipDB.Monitor.Note1") + tables[i] + Messages.getString("Spoon.RipDB.Monitor.Note2") + sourceDbInfo + "]" + Const.CR; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					note += Messages.getString("Spoon.RipDB.Monitor.Note3") + tables[i] + Messages.getString("Spoon.RipDB.Monitor.Note4") + targetDbInfo + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					NotePadMeta ni = new NotePadMeta(note, 150, 10, -1, -1);
					transMeta.addNote(ni);

					//
					// Add the TableInputMeta step...
					// 
					String fromstepname = Messages.getString("Spoon.RipDB.Monitor.FromStep.Name") + tables[i] + "]"; //$NON-NLS-1$ //$NON-NLS-2$
					TableInputMeta tii = new TableInputMeta();
					tii.setDatabaseMeta(sourceDbInfo);
					tii.setSQL("SELECT * FROM " + sourceDbInfo.quoteField(tables[i])); //$NON-NLS-1$

					String fromstepid = StepLoader.getInstance().getStepPluginID(tii);
					StepMeta fromstep = new StepMeta(fromstepid, fromstepname, tii);
					fromstep.setLocation(150, 100);
					fromstep.setDraw(true);
					fromstep
							.setDescription(Messages.getString("Spoon.RipDB.Monitor.FromStep.Description") + tables[i] + Messages.getString("Spoon.RipDB.Monitor.FromStep.Description2") + sourceDbInfo + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					transMeta.addStep(fromstep);

					//
					// Add the TableOutputMeta step...
					//
					String tostepname = Messages.getString("Spoon.RipDB.Monitor.ToStep.Name") + tables[i] + "]"; //$NON-NLS-1$ //$NON-NLS-2$
					TableOutputMeta toi = new TableOutputMeta();
					toi.setDatabaseMeta(targetDbInfo);
					toi.setTablename(tables[i]);
					toi.setCommitSize(100);
					toi.setTruncateTable(true);

					String tostepid = StepLoader.getInstance().getStepPluginID(toi);
					StepMeta tostep = new StepMeta(tostepid, tostepname, toi);
					tostep.setLocation(500, 100);
					tostep.setDraw(true);
					tostep
							.setDescription(Messages.getString("Spoon.RipDB.Monitor.ToStep.Description1") + tables[i] + Messages.getString("Spoon.RipDB.Monitor.ToStep.Description2") + targetDbInfo + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					transMeta.addStep(tostep);

					//
					// Add a hop between the two steps...
					//
					TransHopMeta hi = new TransHopMeta(fromstep, tostep);
					transMeta.addTransHop(hi);

					//
					// Now we generate the SQL needed to run for this
					// transformation.
					//
					// First set the limit to 1 to speed things up!
					String tmpSql = tii.getSQL();
					tii.setSQL(tii.getSQL() + sourceDbInfo.getLimitClause(1));
					String sql = ""; //$NON-NLS-1$
					try
					{
						sql = transMeta.getSQLStatementsString();
					} catch (KettleStepException kse)
					{
						throw new InvocationTargetException(
								kse,
								Messages.getString("Spoon.RipDB.Exception.ErrorGettingSQLFromTransformation") + transMeta + "] : " + kse.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
					}
					// remove the limit
					tii.setSQL(tmpSql);

					//
					// Now, save the transformation...
					//
					boolean ok;
					if (spoon.getRepository() != null)
					{
						ok = spoon.saveToRepository(transMeta);
					} else
					{
						ok = spoon.saveToFile(transMeta);
					}
					if (!ok)
					{
						throw new InvocationTargetException(
								new Exception(
										Messages
												.getString("Spoon.RipDB.Exception.UnableToSaveTransformationToRepository")), Messages.getString("Spoon.RipDB.Exception.UnableToSaveTransformationToRepository")); //$NON-NLS-1$
					}

					// We can now continue with the population of the job...
					// //////////////////////////////////////////////////////////////////////

					location.x = 250;
					if (i > 0)
						location.y += 100;

					//
					// We can continue defining the job.
					//
					// First the SQL, but only if needed!
					// If the table exists & has the correct format, nothing is
					// done
					//
					if (!Const.isEmpty(sql))
					{
						String jesqlname = Messages.getString("Spoon.RipDB.JobEntrySQL.Name") + tables[i] + "]"; //$NON-NLS-1$ //$NON-NLS-2$
						JobEntrySQL jesql = new JobEntrySQL(jesqlname);
						jesql.setDatabase(targetDbInfo);
						jesql.setSQL(sql);
						jesql
								.setDescription(Messages.getString("Spoon.RipDB.JobEntrySQL.Description") + targetDbInfo + "].[" + tables[i] + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

						JobEntryCopy jecsql = new JobEntryCopy();
						jecsql.setEntry(jesql);
						jecsql.setLocation(new Point(location.x, location.y));
						jecsql.setDrawn();
						jobMeta.addJobEntry(jecsql);

						// Add the hop too...
						JobHopMeta jhi = new JobHopMeta(previous, jecsql);
						jobMeta.addJobHop(jhi);
						previous = jecsql;
					}

					//
					// Add the jobentry for the transformation too...
					//
					String jetransname = Messages.getString("Spoon.RipDB.JobEntryTrans.Name") + tables[i] + "]"; //$NON-NLS-1$ //$NON-NLS-2$
					JobEntryTrans jetrans = new JobEntryTrans(jetransname);
					jetrans.setTransname(transMeta.getName());
					if (spoon.getRepository() != null)
					{
						jetrans.setDirectory(transMeta.getDirectory());
					} else
					{
						jetrans.setFileName(Const.createFilename("${"
								+ Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY + "}", transMeta.getName(),
								Const.STRING_TRANS_DEFAULT_EXT));
					}

					JobEntryCopy jectrans = new JobEntryCopy(spoon.getLog(), jetrans);
					jectrans
							.setDescription(Messages.getString("Spoon.RipDB.JobEntryTrans.Description1") + Const.CR + Messages.getString("Spoon.RipDB.JobEntryTrans.Description2") + sourceDbInfo + "].[" + tables[i] + "]" + Const.CR + Messages.getString("Spoon.RipDB.JobEntryTrans.Description3") + targetDbInfo + "].[" + tables[i] + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
					jectrans.setDrawn();
					location.x += 400;
					jectrans.setLocation(new Point(location.x, location.y));
					jobMeta.addJobEntry(jectrans);

					// Add a hop between the last 2 job entries.
					JobHopMeta jhi2 = new JobHopMeta(previous, jectrans);
					jobMeta.addJobHop(jhi2);
					previous = jectrans;

					monitor.worked(1);
				}

				monitor.worked(100);
				monitor.done();
			}
		};

		try
		{
			ProgressMonitorDialog pmd = new ProgressMonitorDialog(spoon.getShell());
			pmd.run(false, true, op);
		} catch (InvocationTargetException e)
		{
			new ErrorDialog(
					spoon.getShell(),
					Messages.getString("Spoon.ErrorDiaspoon.getLog().RipDB.ErrorRippingTheDatabase.Title"), Messages.getString("Spoon.ErrorDiaspoon.getLog().RipDB.ErrorRippingTheDatabase.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		} catch (InterruptedException e)
		{
			new ErrorDialog(
					spoon.getShell(),
					Messages.getString("Spoon.ErrorDiaspoon.getLog().RipDB.ErrorRippingTheDatabase.Title"), Messages.getString("Spoon.ErrorDiaspoon.getLog().RipDB.ErrorRippingTheDatabase.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		} finally
		{
			spoon.refreshGraph();
			spoon.refreshTree();
		}

		return jobMeta;
	}

	public void addJobHistory(JobMeta jobMeta, boolean select)
	{
		// See if there already is a tab for this history view
		// If no, add it
		// If yes, select that tab
		//
		String tabName = spoon.delegates.tabs.makeJobHistoryTabName(jobMeta);
		TabItem tabItem = spoon.delegates.tabs.findTabItem(tabName, TabMapEntry.OBJECT_TYPE_JOB_HISTORY);
		if (tabItem == null)
		{
			JobHistory jobHistory = new JobHistory(spoon.tabfolder.getSwtTabset(), spoon, jobMeta);
			tabItem = new TabItem(spoon.tabfolder, tabName, tabName);
			tabItem.setToolTipText(Messages.getString("Spoon.Title.ExecHistoryJobView.Tooltip", spoon.delegates.tabs
					.makeJobGraphTabName(jobMeta)));
			tabItem.setControl(jobHistory);

			// If there is an associated log window that's open, find it and add
			// a refresher
			JobLog jobLog = findJobLogOfJob(jobMeta);
			if (jobLog != null)
			{
				JobHistoryRefresher jobHistoryRefresher = new JobHistoryRefresher(tabItem, jobHistory);
				spoon.tabfolder.addListener(jobHistoryRefresher);
				jobLog.setJobHistoryRefresher(jobHistoryRefresher);
			}
			jobHistory.markRefreshNeeded(); // will refresh when first selected

			spoon.delegates.tabs.addTab(tabName, new TabMapEntry(tabItem, tabName, jobHistory,
					TabMapEntry.OBJECT_TYPE_JOB_HISTORY));
		}
		if (select)
		{
			int idx = spoon.tabfolder.indexOf(tabItem);
			spoon.tabfolder.setSelected(idx);
		}
	}

	public boolean isDefaultJobName(String name)
	{
		if (!name.startsWith(SpoonInterface.STRING_JOB))
			return false;

		// see if there are only digits behind the job...
		// This will detect:
		// "Job"
		// "Job "
		// "Job 1"
		// "Job 2"
		// ...
		for (int i = SpoonInterface.STRING_JOB.length() + 1; i < name.length(); i++)
		{
			if (!Character.isDigit(name.charAt(i)))
				return false;
		}
		return true;
	}

	public JobGraph findJobGraphOfJob(JobMeta jobMeta)
	{
		// Now loop over the entries in the tab-map
		for (TabMapEntry mapEntry : spoon.delegates.tabs.getTabs())
		{
			if (mapEntry.getObject() instanceof JobGraph)
			{
				JobGraph jobGraph = (JobGraph) mapEntry.getObject();
				if (jobGraph.getMeta().equals(jobMeta))
					return jobGraph;
			}
		}
		return null;
	}

	public JobLog findJobLogOfJob(JobMeta jobMeta)
	{
		// Now loop over the entries in the tab-map
		for (TabMapEntry mapEntry : spoon.delegates.tabs.getTabs())
		{
			if (mapEntry.getObject() instanceof JobLog)
			{
				JobLog jobLog = (JobLog) mapEntry.getObject();
				if (jobLog.getMeta().equals(jobMeta))
					return jobLog;
			}
		}
		return null;
	}



	/**
	 * Add a job to the job map
	 * 
	 * @param jobMeta
	 *            the job to add to the map
	 * @return the key used to store the transformation in the map
	 */
	public String addJob(JobMeta jobMeta)
	{
		String key = spoon.delegates.tabs.makeJobGraphTabName(jobMeta);
		if (jobMap.get(key) == null)
		{
			jobMap.put(key, jobMeta);
		} else
		{
			ShowMessageDialog dialog = new ShowMessageDialog(spoon.getShell(), SWT.OK | SWT.ICON_INFORMATION,
					Messages.getString("Spoon.Dialog.JobAlreadyLoaded.Title"), "'" + key + "'" + Const.CR
							+ Const.CR + Messages.getString("Spoon.Dialog.JobAlreadyLoaded.Message"));
			dialog.setTimeOut(6);
			dialog.open();
		}

		return key;
	}

	/**
	 * @param transMeta
	 *            the transformation to close, make sure it's ok to dispose of
	 *            it BEFORE you call this.
	 */
	public void closeJob(JobMeta jobMeta)
	{
		String tabName = spoon.delegates.tabs.makeJobGraphTabName(jobMeta);
		jobMap.remove(tabName);

		// Close the associated tabs...
		TabItem graphTab = spoon.delegates.tabs.findTabItem(tabName, TabMapEntry.OBJECT_TYPE_JOB_GRAPH);
		if (graphTab != null)
		{
			spoon.delegates.tabs.removeTab(tabName);
			graphTab.dispose();
		}

		// Logging
		String logTabName = spoon.delegates.tabs.makeJobLogTabName(jobMeta);
		TabItem logTab = spoon.delegates.tabs.findTabItem(logTabName, TabMapEntry.OBJECT_TYPE_JOB_LOG);
		if (logTab != null)
		{
			logTab.dispose();
			spoon.delegates.tabs.removeTab(logTabName);
		}

		// History
		String historyTabName = spoon.delegates.tabs.makeJobHistoryTabName(jobMeta);
		TabItem historyTab = spoon.delegates.tabs.findTabItem(historyTabName, TabMapEntry.OBJECT_TYPE_JOB_HISTORY);
		if (historyTab != null)
		{
			historyTab.dispose();
			spoon.delegates.tabs.removeTab(historyTabName);
		}

		spoon.refreshTree();
	}

	public void addJobGraph(JobMeta jobMeta)
	{
		String key = addJob(jobMeta);
		if (key != null)
		{
			// See if there already is a tab for this graph
			// If no, add it
			// If yes, select that tab
			//
			String tabName = spoon.delegates.tabs.makeJobGraphTabName(jobMeta);
			TabItem tabItem = spoon.delegates.tabs.findTabItem(tabName, TabMapEntry.OBJECT_TYPE_JOB_GRAPH);
			if (tabItem == null)
			{
				JobGraph jobGraph = new JobGraph(spoon.tabfolder.getSwtTabset(), spoon, jobMeta);
				tabItem = new TabItem(spoon.tabfolder, tabName, tabName);
				tabItem.setToolTipText(Messages.getString("Spoon.TabJob.Tooltip", spoon.delegates.tabs
						.makeJobGraphTabName(jobMeta)));
				tabItem.setImage(GUIResource.getInstance().getImageJobGraph());
				tabItem.setControl(jobGraph);

				spoon.delegates.tabs.addTab(tabName, new TabMapEntry(tabItem, tabName, jobGraph,
						TabMapEntry.OBJECT_TYPE_JOB_GRAPH));
			}
			int idx = spoon.tabfolder.indexOf(tabItem);

			// OK, also see if we need to open a new history window.
			if (jobMeta.getLogConnection() != null && !Const.isEmpty(jobMeta.getLogTable()))
			{
				addJobHistory(jobMeta, false);
			}
			// keep the focus on the graph
			spoon.tabfolder.setSelected(idx);

			spoon.setUndoMenu(jobMeta);
			spoon.enableMenus();
		}
	}

	public void addJobLog(JobMeta jobMeta)
	{
		// See if there already is a tab for this log
		// If no, add it
		// If yes, select that tab
		//
		String tabName = spoon.delegates.tabs.makeJobLogTabName(jobMeta);
		TabItem tabItem = spoon.delegates.tabs.findTabItem(tabName, TabMapEntry.OBJECT_TYPE_JOB_LOG);
		if (tabItem == null)
		{
			JobLog jobLog = new JobLog(spoon.tabfolder.getSwtTabset(), spoon, jobMeta);
			tabItem = new TabItem(spoon.tabfolder, tabName, tabName);
			tabItem.setText(tabName);
			tabItem.setToolTipText(Messages.getString("Spoon.Title.ExecLogJobView.Tooltip", spoon.delegates.tabs
					.makeJobGraphTabName(jobMeta)));
			tabItem.setControl(jobLog);

			// If there is an associated history window, we want to keep that
			// one up-to-date as well.
			//
			JobHistory jobHistory = findJobHistoryOfJob(jobMeta);
			TabItem historyItem = spoon.delegates.tabs.findTabItem(spoon.delegates.tabs.makeJobHistoryTabName(jobMeta),
					TabMapEntry.OBJECT_TYPE_JOB_HISTORY);

			if (jobHistory != null && historyItem != null)
			{
				JobHistoryRefresher jobHistoryRefresher = new JobHistoryRefresher(historyItem, jobHistory);
				spoon.tabfolder.addListener(jobHistoryRefresher);
				jobLog.setJobHistoryRefresher(jobHistoryRefresher);
			}

			spoon.delegates.tabs.addTab(tabName, new TabMapEntry(tabItem, tabName, jobLog,
					TabMapEntry.OBJECT_TYPE_JOB_LOG));
		}
		int idx = spoon.tabfolder.indexOf(tabItem);
		spoon.tabfolder.setSelected(idx);
	}

	public List<JobMeta> getJobList()
	{
		return new ArrayList<JobMeta>(jobMap.values());
	}

	public JobMeta getJob(String tabItemText)
	{
		return jobMap.get(tabItemText);
	}

	public JobMeta[] getLoadedJobs()
	{
		List<JobMeta> list = new ArrayList<JobMeta>(jobMap.values());
		return list.toArray(new JobMeta[list.size()]);
	}

	public void addJob(String key, JobMeta entry)
	{
		jobMap.put(key, entry);
	}

	public void removeJob(String key)
	{
		jobMap.remove(key);
	}

	public JobHistory findJobHistoryOfJob(JobMeta jobMeta)
	{
		if (jobMeta == null)
			return null;

		// Now loop over the entries in the tab-map
		for (TabMapEntry mapEntry : spoon.delegates.tabs.getTabs())
		{
			if (mapEntry.getObject() instanceof JobHistory)
			{
				JobHistory jobHistory = (JobHistory) mapEntry.getObject();
				if (jobHistory.getMeta() != null && jobHistory.getMeta().equals(jobMeta))
					return jobHistory;
			}
		}
		return null;
	}

	public void redoJobAction(JobMeta jobMeta, TransAction transAction)
	{
		switch (transAction.getType())
		{
		//
		// NEW
		//
		case TransAction.TYPE_ACTION_NEW_JOB_ENTRY:
			// re-delete the entry at correct location:
		{
			JobEntryCopy si[] = (JobEntryCopy[]) transAction.getCurrent();
			int idx[] = transAction.getCurrentIndex();
			for (int i = 0; i < idx.length; i++)
				jobMeta.addJobEntry(idx[i], si[i]);
			spoon.refreshTree();
			spoon.refreshGraph();
		}
			break;

		case TransAction.TYPE_ACTION_NEW_NOTE:
			// re-insert the note at correct location:
		{
			NotePadMeta ni[] = (NotePadMeta[]) transAction.getCurrent();
			int idx[] = transAction.getCurrentIndex();
			for (int i = 0; i < idx.length; i++)
				jobMeta.addNote(idx[i], ni[i]);
			spoon.refreshTree();
			spoon.refreshGraph();
		}
			break;

		case TransAction.TYPE_ACTION_NEW_JOB_HOP:
			// re-insert the hop at correct location:
		{
			JobHopMeta hi[] = (JobHopMeta[]) transAction.getCurrent();
			int idx[] = transAction.getCurrentIndex();
			for (int i = 0; i < idx.length; i++)
				jobMeta.addJobHop(idx[i], hi[i]);
			spoon.refreshTree();
			spoon.refreshGraph();
		}
			break;

		//  
		// DELETE
		//
		case TransAction.TYPE_ACTION_DELETE_JOB_ENTRY:
			// re-remove the entry at correct location:
		{
			int idx[] = transAction.getCurrentIndex();
			for (int i = idx.length - 1; i >= 0; i--)
				jobMeta.removeJobEntry(idx[i]);
			spoon.refreshTree();
			spoon.refreshGraph();
		}
			break;

		case TransAction.TYPE_ACTION_DELETE_NOTE:
			// re-remove the note at correct location:
		{
			int idx[] = transAction.getCurrentIndex();
			for (int i = idx.length - 1; i >= 0; i--)
				jobMeta.removeNote(idx[i]);
			spoon.refreshTree();
			spoon.refreshGraph();
		}
			break;

		case TransAction.TYPE_ACTION_DELETE_JOB_HOP:
			// re-remove the hop at correct location:
		{
			int idx[] = transAction.getCurrentIndex();
			for (int i = idx.length - 1; i >= 0; i--)
				jobMeta.removeJobHop(idx[i]);
			spoon.refreshTree();
			spoon.refreshGraph();
		}
			break;

		//
		// CHANGE
		//

		// We changed a step : undo this...
		case TransAction.TYPE_ACTION_CHANGE_JOB_ENTRY:
			// replace with "current" version.
		{
			for (int i = 0; i < transAction.getCurrent().length; i++)
			{
				JobEntryCopy copy = (JobEntryCopy) ((JobEntryCopy) (transAction.getCurrent()[i]))
						.clone_deep();
				jobMeta.getJobEntry(transAction.getCurrentIndex()[i]).replaceMeta(copy);
			}
			spoon.refreshTree();
			spoon.refreshGraph();
		}
			break;

		// We changed a note : undo this...
		case TransAction.TYPE_ACTION_CHANGE_NOTE:
			// Delete & re-insert
		{
			NotePadMeta ni[] = (NotePadMeta[]) transAction.getCurrent();
			int idx[] = transAction.getCurrentIndex();

			for (int i = 0; i < idx.length; i++)
			{
				jobMeta.removeNote(idx[i]);
				jobMeta.addNote(idx[i], ni[i]);
			}
			spoon.refreshTree();
			spoon.refreshGraph();
		}
			break;

		// We changed a hop : undo this...
		case TransAction.TYPE_ACTION_CHANGE_JOB_HOP:
			// Delete & re-insert
		{
			JobHopMeta hi[] = (JobHopMeta[]) transAction.getCurrent();
			int idx[] = transAction.getCurrentIndex();

			for (int i = 0; i < idx.length; i++)
			{
				jobMeta.removeJobHop(idx[i]);
				jobMeta.addJobHop(idx[i], hi[i]);
			}
			spoon.refreshTree();
			spoon.refreshGraph();
		}
			break;

		//
		// CHANGE POSITION
		//
		case TransAction.TYPE_ACTION_POSITION_JOB_ENTRY:
		{
			// Find the location of the step:
			int idx[] = transAction.getCurrentIndex();
			Point p[] = transAction.getCurrentLocation();
			for (int i = 0; i < p.length; i++)
			{
				JobEntryCopy entry = jobMeta.getJobEntry(idx[i]);
				entry.setLocation(p[i]);
			}
			spoon.refreshGraph();
		}
			break;
		case TransAction.TYPE_ACTION_POSITION_NOTE:
		{
			int idx[] = transAction.getCurrentIndex();
			Point curr[] = transAction.getCurrentLocation();
			for (int i = 0; i < idx.length; i++)
			{
				NotePadMeta npi = jobMeta.getNote(idx[i]);
				npi.setLocation(curr[i]);
			}
			spoon.refreshGraph();
		}
			break;
		default:
			break;
		}
	}

	public void undoJobAction(JobMeta jobMeta, TransAction transAction)
	{
		switch (transAction.getType())
		{
		// We created a new entry : undo this...
		case TransAction.TYPE_ACTION_NEW_JOB_ENTRY:
			// Delete the entry at correct location:
		{
			int idx[] = transAction.getCurrentIndex();
			for (int i = idx.length - 1; i >= 0; i--)
				jobMeta.removeJobEntry(idx[i]);
			spoon.refreshTree();
			spoon.refreshGraph();
		}
			break;

		// We created a new note : undo this...
		case TransAction.TYPE_ACTION_NEW_NOTE:
			// Delete the note at correct location:
		{
			int idx[] = transAction.getCurrentIndex();
			for (int i = idx.length - 1; i >= 0; i--)
				jobMeta.removeNote(idx[i]);
			spoon.refreshTree();
			spoon.refreshGraph();
		}
			break;

		// We created a new hop : undo this...
		case TransAction.TYPE_ACTION_NEW_JOB_HOP:
			// Delete the hop at correct location:
		{
			int idx[] = transAction.getCurrentIndex();
			for (int i = idx.length - 1; i >= 0; i--)
				jobMeta.removeJobHop(idx[i]);
			spoon.refreshTree();
			spoon.refreshGraph();
		}
			break;

		//
		// DELETE
		//

		// We delete an entry : undo this...
		case TransAction.TYPE_ACTION_DELETE_STEP:
			// un-Delete the entry at correct location: re-insert
		{
			JobEntryCopy ce[] = (JobEntryCopy[]) transAction.getCurrent();
			int idx[] = transAction.getCurrentIndex();
			for (int i = 0; i < ce.length; i++)
				jobMeta.addJobEntry(idx[i], ce[i]);
			spoon.refreshTree();
			spoon.refreshGraph();
		}
			break;

		// We delete new note : undo this...
		case TransAction.TYPE_ACTION_DELETE_NOTE:
			// re-insert the note at correct location:
		{
			NotePadMeta ni[] = (NotePadMeta[]) transAction.getCurrent();
			int idx[] = transAction.getCurrentIndex();
			for (int i = 0; i < idx.length; i++)
				jobMeta.addNote(idx[i], ni[i]);
			spoon.refreshTree();
			spoon.refreshGraph();
		}
			break;

		// We deleted a new hop : undo this...
		case TransAction.TYPE_ACTION_DELETE_JOB_HOP:
			// re-insert the hop at correct location:
		{
			JobHopMeta hi[] = (JobHopMeta[]) transAction.getCurrent();
			int idx[] = transAction.getCurrentIndex();
			for (int i = 0; i < hi.length; i++)
			{
				jobMeta.addJobHop(idx[i], hi[i]);
			}
			spoon.refreshTree();
			spoon.refreshGraph();
		}
			break;

		//
		// CHANGE
		//

		// We changed a job entry: undo this...
		case TransAction.TYPE_ACTION_CHANGE_JOB_ENTRY:
			// Delete the current job entry, insert previous version.
		{
			for (int i = 0; i < transAction.getPrevious().length; i++)
			{
				JobEntryCopy copy = (JobEntryCopy) ((JobEntryCopy) transAction.getPrevious()[i]).clone();
				jobMeta.getJobEntry(transAction.getCurrentIndex()[i]).replaceMeta(copy);
			}
			spoon.refreshTree();
			spoon.refreshGraph();
		}
			break;

		// We changed a note : undo this...
		case TransAction.TYPE_ACTION_CHANGE_NOTE:
			// Delete & re-insert
		{
			NotePadMeta prev[] = (NotePadMeta[]) transAction.getPrevious();
			int idx[] = transAction.getCurrentIndex();
			for (int i = 0; i < idx.length; i++)
			{
				jobMeta.removeNote(idx[i]);
				jobMeta.addNote(idx[i], prev[i]);
			}
			spoon.refreshTree();
			spoon.refreshGraph();
		}
			break;

		// We changed a hop : undo this...
		case TransAction.TYPE_ACTION_CHANGE_JOB_HOP:
			// Delete & re-insert
		{
			JobHopMeta prev[] = (JobHopMeta[]) transAction.getPrevious();
			int idx[] = transAction.getCurrentIndex();
			for (int i = 0; i < idx.length; i++)
			{
				jobMeta.removeJobHop(idx[i]);
				jobMeta.addJobHop(idx[i], prev[i]);
			}
			spoon.refreshTree();
			spoon.refreshGraph();
		}
			break;

		//
		// POSITION
		//

		// The position of a step has changed: undo this...
		case TransAction.TYPE_ACTION_POSITION_JOB_ENTRY:
			// Find the location of the step:
		{
			int idx[] = transAction.getCurrentIndex();
			Point p[] = transAction.getPreviousLocation();
			for (int i = 0; i < p.length; i++)
			{
				JobEntryCopy entry = jobMeta.getJobEntry(idx[i]);
				entry.setLocation(p[i]);
			}
			spoon.refreshGraph();
		}
			break;

		// The position of a note has changed: undo this...
		case TransAction.TYPE_ACTION_POSITION_NOTE:
			int idx[] = transAction.getCurrentIndex();
			Point prev[] = transAction.getPreviousLocation();
			for (int i = 0; i < idx.length; i++)
			{
				NotePadMeta npi = jobMeta.getNote(idx[i]);
				npi.setLocation(prev[i]);
			}
			spoon.refreshGraph();
			break;
		default:
			break;
		}
	}

}
