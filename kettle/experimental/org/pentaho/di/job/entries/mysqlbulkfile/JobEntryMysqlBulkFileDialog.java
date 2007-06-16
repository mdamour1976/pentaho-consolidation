/**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

/*
 * Created on 19-jun-2003
 *
 */

package org.pentaho.di.job.entries.mysqlbulkfile;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.dialog.DatabaseDialog;
import org.pentaho.di.core.database.dialog.DatabaseExplorerDialog;
import org.pentaho.di.core.dialog.EnterSelectionDialog;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.widget.TextVar;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.dialog.JobDialog;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.trans.step.BaseStepDialog;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.gui.WindowProperty;
import org.pentaho.di.core.dialog.ErrorDialog;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.util.StringUtil;


/**
 * This dialog allows you to edit the MYSQL Bulk Load To a file entry settings. 
 * (select the connection and the table to be checked) 
 * This entry type evaluates!
 * 
 * @author Samatar
 * @since 06-03-2006
 */
public class JobEntryMysqlBulkFileDialog extends Dialog implements JobEntryDialogInterface
{

	private static final String[] FILETYPES = new String[] 
		{
			Messages
			.getString("JobMysqlBulkFile.Filetype.Text"),
			Messages
			.getString("JobMysqlBulkFile.Filetype.All") };

	private Label wlName;

	private Text wName;

	private FormData fdlName, fdName;

	private Label wlConnection;

	private CCombo wConnection;

	private Button wbConnection;

	private FormData fdlConnection, fdbConnection, fdConnection;

	private Label wlTablename;

	private TextVar wTablename;

	private FormData fdlTablename, fdTablename;

	// Schema name
	private Label wlSchemaname;
	private TextVar wSchemaname;
	private FormData fdlSchemaname, fdSchemaname;

	private Button wOK, wCancel;

	private Listener lsOK, lsCancel;

	private JobEntryMysqlBulkFile jobEntry;

	private JobMeta jobMeta;

	private Shell shell;

	private Props props;

	private SelectionAdapter lsDef;

	private boolean changed;

	//Fichier
	private Label wlFilename;

	private Button wbFilename;

	private TextVar wFilename;

	private FormData fdlFilename, fdbFilename, fdFilename;

	//  HighPriority
	private Label        wlHighPriority;
	private Button       wHighPriority;
	private FormData     fdlHighPriority, fdHighPriority;

	// Separator
	private Label        wlSeparator;
	private TextVar         wSeparator;
	private FormData     fdlSeparator, fdSeparator;

	//Enclosed
	private Label wlEnclosed;
	private TextVar wEnclosed;
	private FormData fdlEnclosed, fdEnclosed;

	//  OptionEnclosed
	private Label        wlOptionEnclosed;
	private Button       wOptionEnclosed;
	private FormData     fdlOptionEnclosed, fdOptionEnclosed;


	//Line terminated
	private Label wlLineterminated;
	private TextVar wLineterminated;
	private FormData fdlLineterminated, fdLineterminated;

	//List Columns

	private Label wlListColumn;

	private TextVar wListColumn;

	private FormData fdlListColumn, fdListColumn;
	

	//Limit First lines
	private Label wlLimitlines;
	private TextVar wLimitlines;
	private FormData fdlLimitlines, fdLimitlines;

	// If Output File exists
	private Label wlIfFileExists;
	private  CCombo wIfFileExists;
	private FormData fdlIfFileExists, fdIfFileExists;


	// Out/ DUMP
	private Label wlOutDumpValue;
	private  CCombo wOutDumpValue;
	private FormData fdlOutDumpValue, fdOutDumpValue;

    private Button wbTable;
    private Button wbListColumns;

	public JobEntryMysqlBulkFileDialog(Shell parent, JobEntryMysqlBulkFile jobEntry, JobMeta jobMeta)
	{
		super(parent, SWT.NONE);
		props = Props.getInstance();
		this.jobEntry = jobEntry;
		this.jobMeta = jobMeta;

		if (this.jobEntry.getName() == null)
			this.jobEntry.setName(Messages.getString("JobMysqlBulkFile.Name.Default"));
	}

	public JobEntryInterface open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, props.getJobsDialogStyle());
		props.setLook(shell);
		JobDialog.setShellImage(shell, jobEntry);

		ModifyListener lsMod = new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				jobEntry.setChanged();
			}
		};
		changed = jobEntry.hasChanged();

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("JobMysqlBulkFile.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Filename line
		wlName = new Label(shell, SWT.RIGHT);
		wlName.setText(Messages.getString("JobMysqlBulkFile.Name.Label"));
		props.setLook(wlName);
		fdlName = new FormData();
		fdlName.left = new FormAttachment(0, 0);
		fdlName.right = new FormAttachment(middle, 0);
		fdlName.top = new FormAttachment(0, margin);
		wlName.setLayoutData(fdlName);
		wName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wName);
		wName.addModifyListener(lsMod);
		fdName = new FormData();
		fdName.left = new FormAttachment(middle, 0);
		fdName.top = new FormAttachment(0, margin);
		fdName.right = new FormAttachment(100, 0);
		wName.setLayoutData(fdName);

		// Connection line
		wlConnection = new Label(shell, SWT.RIGHT);
		wlConnection.setText(Messages.getString("JobMysqlBulkFile.Connection.Label"));
		props.setLook(wlConnection);
		fdlConnection = new FormData();
		fdlConnection.left = new FormAttachment(0, 0);
		fdlConnection.top = new FormAttachment(wName, margin);
		fdlConnection.right = new FormAttachment(middle, -margin);
		wlConnection.setLayoutData(fdlConnection);

		wbConnection = new Button(shell, SWT.PUSH);
		wbConnection.setText(Messages.getString("System.Button.New") + "...");
		wbConnection.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				DatabaseMeta databaseMeta = new DatabaseMeta();
				DatabaseDialog cid = new DatabaseDialog(shell, databaseMeta);
				if (cid.open() != null)
				{
					jobMeta.addDatabase(databaseMeta);

					// SB: Maybe do the same her as in BaseStepDialog: remove
					// all db connections and add them again.
					wConnection.add(databaseMeta.getName());
					wConnection.select(wConnection.getItemCount() - 1);
				}
			}
		});
		fdbConnection = new FormData();
		fdbConnection.right = new FormAttachment(100, 0);
		fdbConnection.top = new FormAttachment(wName, margin);
		fdbConnection.height = 20;
		wbConnection.setLayoutData(fdbConnection);

		wConnection = new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
		props.setLook(wConnection);
		for (int i = 0; i < jobMeta.nrDatabases(); i++)
		{
			DatabaseMeta ci = jobMeta.getDatabase(i);
			wConnection.add(ci.getName());
		}
		wConnection.select(0);
		wConnection.addModifyListener(lsMod);
		fdConnection = new FormData();
		fdConnection.left = new FormAttachment(middle, 0);
		fdConnection.top = new FormAttachment(wName, margin);
		fdConnection.right = new FormAttachment(wbConnection, -margin);
		wConnection.setLayoutData(fdConnection);

		
		// Schema name line
		wlSchemaname = new Label(shell, SWT.RIGHT);
		wlSchemaname.setText(Messages.getString("JobMysqlBulkFile.Schemaname.Label"));
		props.setLook(wlSchemaname);
		fdlSchemaname = new FormData();
		fdlSchemaname.left = new FormAttachment(0, 0);
		fdlSchemaname.right = new FormAttachment(middle, 0);
		fdlSchemaname.top = new FormAttachment(wConnection, margin);
		wlSchemaname.setLayoutData(fdlSchemaname);

		wSchemaname = new TextVar(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wSchemaname);
		wSchemaname.setToolTipText(Messages.getString("JobMysqlBulkFile.Schemaname.Tooltip"));
		wSchemaname.addModifyListener(lsMod);
		fdSchemaname = new FormData();
		fdSchemaname.left = new FormAttachment(middle, 0);
		fdSchemaname.top = new FormAttachment(wConnection, margin);
		fdSchemaname.right = new FormAttachment(100, 0);
		wSchemaname.setLayoutData(fdSchemaname);
		
		// Table name line
		wlTablename = new Label(shell, SWT.RIGHT);
		wlTablename.setText(Messages.getString("JobMysqlBulkFile.Tablename.Label"));
		props.setLook(wlTablename);
		fdlTablename = new FormData();
		fdlTablename.left = new FormAttachment(0, 0);
		fdlTablename.right = new FormAttachment(middle, 0);
		fdlTablename.top = new FormAttachment(wSchemaname, margin);
		wlTablename.setLayoutData(fdlTablename);

        wbTable=new Button(shell, SWT.PUSH| SWT.CENTER);
        props.setLook(wbTable);
        wbTable.setText(Messages.getString("System.Button.Browse"));
        FormData fdbTable = new FormData();
        fdbTable.right= new FormAttachment(100, 0);
        fdbTable.top  = new FormAttachment(wSchemaname, margin/2);
        wbTable.setLayoutData(fdbTable);
        wbTable.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { getTableName(); } } );

		wTablename = new TextVar(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wTablename);
		wTablename.setToolTipText(Messages.getString("JobMysqlBulkFile.Tablename.Tooltip"));
		wTablename.addModifyListener(lsMod);
		fdTablename = new FormData();
		fdTablename.left = new FormAttachment(middle, 0);
		fdTablename.top = new FormAttachment(wSchemaname, margin);
		fdTablename.right = new FormAttachment(wbTable, -margin);
		wTablename.setLayoutData(fdTablename);


		// Filename line
		wlFilename = new Label(shell, SWT.RIGHT);
		wlFilename.setText(Messages.getString("JobMysqlBulkFile.Filename.Label"));
		props.setLook(wlFilename);
		fdlFilename = new FormData();
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.top = new FormAttachment(wTablename, margin);
		fdlFilename.right = new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlFilename);

		wbFilename = new Button(shell, SWT.PUSH | SWT.CENTER);
		props.setLook(wbFilename);
		wbFilename.setText(Messages.getString("System.Button.Browse"));
		fdbFilename = new FormData();
		fdbFilename.right = new FormAttachment(100, 0);
		fdbFilename.top = new FormAttachment(wTablename, 0);
		// fdbFilename.height = 22;
		wbFilename.setLayoutData(fdbFilename);

		wFilename = new TextVar(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wFilename);
		wFilename.addModifyListener(lsMod);
		fdFilename = new FormData();
		fdFilename.left = new FormAttachment(middle, 0);
		fdFilename.top = new FormAttachment(wTablename, margin);
		fdFilename.right = new FormAttachment(wbFilename, -margin);
		wFilename.setLayoutData(fdFilename);


		// Whenever something changes, set the tooltip to the expanded version:
		wFilename.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				wFilename.setToolTipText(StringUtil.environmentSubstitute(wFilename.getText()));
			}
		});

		wbFilename.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
				dialog.setFilterExtensions(new String[] { "*.txt", "*.csv", "*" });
				if (wFilename.getText() != null)
				{
					dialog.setFileName(StringUtil.environmentSubstitute(wFilename.getText()));
				}
				dialog.setFilterNames(FILETYPES);
				if (dialog.open() != null)
				{
					wFilename.setText(dialog.getFilterPath() + Const.FILE_SEPARATOR
						+ dialog.getFileName());
				}
			}
		});


		//High Priority ?
		wlHighPriority = new Label(shell, SWT.RIGHT);
		wlHighPriority.setText(Messages.getString("JobMysqlBulkFile.HighPriority.Label"));
		props.setLook(wlHighPriority);
		fdlHighPriority = new FormData();
		fdlHighPriority.left = new FormAttachment(0, 0);
		fdlHighPriority.top = new FormAttachment(wFilename, margin);
		fdlHighPriority.right = new FormAttachment(middle, -margin);
		wlHighPriority.setLayoutData(fdlHighPriority);
		wHighPriority = new Button(shell, SWT.CHECK);
		props.setLook(wHighPriority);
		wHighPriority.setToolTipText(Messages.getString("JobMysqlBulkFile.HighPriority.Tooltip"));
		fdHighPriority = new FormData();
		fdHighPriority.left = new FormAttachment(middle, 0);
		fdHighPriority.top = new FormAttachment(wFilename, margin);
		fdHighPriority.right = new FormAttachment(100, 0);
		wHighPriority.setLayoutData(fdHighPriority);
		wHighPriority.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
			}
		});



		// Out Dump
		wlOutDumpValue = new Label(shell, SWT.RIGHT);
		wlOutDumpValue.setText(Messages.getString("JobMysqlBulkFile.OutDumpValue.Label"));
		props.setLook(wlOutDumpValue);
		fdlOutDumpValue = new FormData();
		fdlOutDumpValue.left = new FormAttachment(0, 0);
		fdlOutDumpValue.right = new FormAttachment(middle, 0);
		fdlOutDumpValue.top = new FormAttachment(wHighPriority, margin);
		wlOutDumpValue.setLayoutData(fdlOutDumpValue);
		wOutDumpValue = new CCombo(shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
					wOutDumpValue.add(Messages.getString("JobMysqlBulkFile.OutFileValue.Label"));
					wOutDumpValue.add(Messages.getString("JobMysqlBulkFile.DumpFileValue.Label"));
					wOutDumpValue.select(0); // +1: starts at -1

		props.setLook(wOutDumpValue);
		fdOutDumpValue= new FormData();
		fdOutDumpValue.left = new FormAttachment(middle, 0);
		fdOutDumpValue.top = new FormAttachment(wHighPriority, margin);
		fdOutDumpValue.right = new FormAttachment(100, 0);
		wOutDumpValue.setLayoutData(fdOutDumpValue);

		fdOutDumpValue = new FormData();
		fdOutDumpValue.left = new FormAttachment(middle, 0);
		fdOutDumpValue.top = new FormAttachment(wHighPriority, margin);
		fdOutDumpValue.right = new FormAttachment(100, 0);
		wOutDumpValue.setLayoutData(fdOutDumpValue);


		wOutDumpValue.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				DumpFile();
			}
		});



		// Separator
		wlSeparator = new Label(shell, SWT.RIGHT);
		wlSeparator.setText(Messages.getString("JobMysqlBulkFile.Separator.Label"));
		props.setLook(wlSeparator);
		fdlSeparator = new FormData();
		fdlSeparator.left = new FormAttachment(0, 0);
		fdlSeparator.right = new FormAttachment(middle, 0);
		fdlSeparator.top = new FormAttachment(wOutDumpValue, margin);
		wlSeparator.setLayoutData(fdlSeparator);

		wSeparator = new TextVar(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wSeparator);
		wSeparator.addModifyListener(lsMod);
		fdSeparator = new FormData();
		fdSeparator.left = new FormAttachment(middle, 0);
		fdSeparator.top = new FormAttachment(wOutDumpValue, margin);
		fdSeparator.right = new FormAttachment(100, 0);
		wSeparator.setLayoutData(fdSeparator);

		// enclosed
		wlEnclosed = new Label(shell, SWT.RIGHT);
		wlEnclosed.setText(Messages.getString("JobMysqlBulkFile.Enclosed.Label"));
		props.setLook(wlEnclosed);
		fdlEnclosed = new FormData();
		fdlEnclosed.left = new FormAttachment(0, 0);
		fdlEnclosed.right = new FormAttachment(middle, 0);
		fdlEnclosed.top = new FormAttachment(wSeparator, margin);
		wlEnclosed.setLayoutData(fdlEnclosed);

		wEnclosed = new TextVar(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wEnclosed);
		wEnclosed.addModifyListener(lsMod);
		fdEnclosed = new FormData();
		fdEnclosed.left = new FormAttachment(middle, 0);
		fdEnclosed.top = new FormAttachment(wSeparator, margin);
		fdEnclosed.right = new FormAttachment(100, 0);
		wEnclosed.setLayoutData(fdEnclosed);

		//Optionnally enclosed ?
		wlOptionEnclosed = new Label(shell, SWT.RIGHT);
		wlOptionEnclosed.setText(Messages.getString("JobMysqlBulkFile.OptionEnclosed.Label"));
		props.setLook(wlOptionEnclosed);
		fdlOptionEnclosed = new FormData();
		fdlOptionEnclosed.left = new FormAttachment(0, 0);
		fdlOptionEnclosed.top = new FormAttachment(wEnclosed, margin);
		fdlOptionEnclosed.right = new FormAttachment(middle, -margin);
		wlOptionEnclosed.setLayoutData(fdlOptionEnclosed);
		wOptionEnclosed = new Button(shell, SWT.CHECK);
		props.setLook(wOptionEnclosed);
		wOptionEnclosed.setToolTipText(Messages.getString("JobMysqlBulkFile.OptionEnclosed.Tooltip"));
		fdOptionEnclosed = new FormData();
		fdOptionEnclosed.left = new FormAttachment(middle, 0);
		fdOptionEnclosed.top = new FormAttachment(wEnclosed, margin);
		fdOptionEnclosed.right = new FormAttachment(100, 0);
		wOptionEnclosed.setLayoutData(fdOptionEnclosed);
		wOptionEnclosed.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
			}
		});


		// Line terminated
		wlLineterminated = new Label(shell, SWT.RIGHT);
		wlLineterminated.setText(Messages.getString("JobMysqlBulkFile.Lineterminated.Label"));
		props.setLook(wlLineterminated);
		fdlLineterminated = new FormData();
		fdlLineterminated.left = new FormAttachment(0, 0);
		fdlLineterminated.right = new FormAttachment(middle, 0);
		fdlLineterminated.top = new FormAttachment(wOptionEnclosed, margin);
		wlLineterminated.setLayoutData(fdlLineterminated);

		wLineterminated = new TextVar(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wLineterminated);
		wLineterminated.addModifyListener(lsMod);
		fdLineterminated = new FormData();
		fdLineterminated.left = new FormAttachment(middle, 0);
		fdLineterminated.top = new FormAttachment(wOptionEnclosed, margin);
		fdLineterminated.right = new FormAttachment(100, 0);
		wLineterminated.setLayoutData(fdLineterminated);




		// List of columns to set for
		wlListColumn = new Label(shell, SWT.RIGHT);
		wlListColumn.setText(Messages.getString("JobMysqlBulkFile.ListColumn.Label"));
		props.setLook(wlListColumn);
		fdlListColumn = new FormData();
		fdlListColumn.left = new FormAttachment(0, 0);
		fdlListColumn.right = new FormAttachment(middle, 0);
		fdlListColumn.top = new FormAttachment(wLineterminated, margin);
		wlListColumn.setLayoutData(fdlListColumn);

        wbListColumns=new Button(shell, SWT.PUSH| SWT.CENTER);
        props.setLook(wbListColumns);
        wbListColumns.setText(Messages.getString("System.Button.Edit"));
        FormData fdbListColumns = new FormData();
        fdbListColumns.right= new FormAttachment(100, 0);
        fdbListColumns.top  = new FormAttachment(wLineterminated, margin);
        wbListColumns.setLayoutData(fdbListColumns);
        wbListColumns.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { getListColumns(); } } );

		wListColumn = new TextVar(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wListColumn);
		wListColumn.setToolTipText(Messages.getString("JobMysqlBulkFile.ListColumn.Tooltip"));
		wListColumn.addModifyListener(lsMod);
		fdListColumn = new FormData();
		fdListColumn.left = new FormAttachment(middle, 0);
		fdListColumn.top = new FormAttachment(wLineterminated, margin);
		fdListColumn.right = new FormAttachment(wbListColumns, -margin);
		wListColumn.setLayoutData(fdListColumn);




		// Nbr of lines to Limit
		wlLimitlines = new Label(shell, SWT.RIGHT);
		wlLimitlines.setText(Messages.getString("JobMysqlBulkFile.Limitlines.Label"));
		props.setLook(wlLimitlines);
		fdlLimitlines = new FormData();
		fdlLimitlines.left = new FormAttachment(0, 0);
		fdlLimitlines.right = new FormAttachment(middle, 0);
		fdlLimitlines.top = new FormAttachment(wListColumn, margin);
		wlLimitlines.setLayoutData(fdlLimitlines);

		wLimitlines = new TextVar(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wLimitlines);
		wLimitlines.setToolTipText(Messages.getString("JobMysqlBulkFile.Limitlines.Tooltip"));
		wLimitlines.addModifyListener(lsMod);
		fdLimitlines = new FormData();
		fdLimitlines.left = new FormAttachment(middle, 0);
		fdLimitlines.top = new FormAttachment(wListColumn, margin);
		fdLimitlines.right = new FormAttachment(100, 0);
		wLimitlines.setLayoutData(fdLimitlines);


		//IF File Exists
		wlIfFileExists = new Label(shell, SWT.RIGHT);
		wlIfFileExists.setText(Messages.getString("JobMysqlBulkFile.IfFileExists.Label"));
		props.setLook(wlIfFileExists);
		fdlIfFileExists = new FormData();
		fdlIfFileExists.left = new FormAttachment(0, 0);
		fdlIfFileExists.right = new FormAttachment(middle, 0);
		fdlIfFileExists.top = new FormAttachment(wLimitlines, margin);
		wlIfFileExists.setLayoutData(fdlIfFileExists);
		wIfFileExists = new CCombo(shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		wIfFileExists.add(Messages.getString("JobMysqlBulkFile.Create_NewFile_IfFileExists.Label"));
		wIfFileExists.add(Messages.getString("JobMysqlBulkFile.Do_Nothing_IfFileExists.Label"));
		wIfFileExists.add(Messages.getString("JobMysqlBulkFile.Fail_IfFileExists.Label"));
		wIfFileExists.select(2); // +1: starts at -1

		props.setLook(wIfFileExists);
		fdIfFileExists= new FormData();
		fdIfFileExists.left = new FormAttachment(middle, 0);
		fdIfFileExists.top = new FormAttachment(wLimitlines, margin);
		fdIfFileExists.right = new FormAttachment(100, 0);
		wIfFileExists.setLayoutData(fdIfFileExists);

		fdIfFileExists = new FormData();
		fdIfFileExists.left = new FormAttachment(middle, 0);
		fdIfFileExists.top = new FormAttachment(wLimitlines, margin);
		fdIfFileExists.right = new FormAttachment(100, 0);
		wIfFileExists.setLayoutData(fdIfFileExists);



		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK"));
		FormData fd = new FormData();
		fd.right = new FormAttachment(50, -10);
		fd.bottom = new FormAttachment(100, 0);
		fd.width = 100;
		wOK.setLayoutData(fd);

		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel"));
		fd = new FormData();
		fd.left = new FormAttachment(50, 10);
		fd.bottom = new FormAttachment(100, 0);
		fd.width = 100;
		wCancel.setLayoutData(fd);

		// Add listeners
		lsCancel = new Listener()
		{
			public void handleEvent(Event e)
			{
				cancel();
			}
		};
		lsOK = new Listener()
		{
			public void handleEvent(Event e)
			{
				ok();
			}
		};

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener(SWT.Selection, lsOK);

		lsDef = new SelectionAdapter()
		{
			public void widgetDefaultSelected(SelectionEvent e)
			{
				ok();
			}
		};

		wName.addSelectionListener(lsDef);
		wTablename.addSelectionListener(lsDef);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(new ShellAdapter()
		{
			public void shellClosed(ShellEvent e)
			{
				cancel();
			}
		});


	
		getData();

		BaseStepDialog.setSize(shell);

		shell.open();
		props.setDialogSize(shell, "JobMysqlBulkFileDialogSize");
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
				display.sleep();
		}
		return jobEntry;
	}
	public void DumpFile()
	{

		jobEntry.setChanged();
		if (wOutDumpValue.getSelectionIndex()==0)
		{
			wSeparator.setEnabled(true);
			wEnclosed.setEnabled(true);
			wLineterminated.setEnabled(true);

		}	
		else
		{
			wSeparator.setEnabled(false);
			wEnclosed.setEnabled(false);
			wLineterminated.setEnabled(false);

		}

				

	}
	public void dispose()
	{
		WindowProperty winprop = new WindowProperty(shell);
		props.setScreen(winprop);
		shell.dispose();
	}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */
	public void getData()
	{
		// System.out.println("evaluates: "+jobentry.evaluates());

		if (jobEntry.getName() != null)
			wName.setText(jobEntry.getName());
		if (jobEntry.getSchemaname() != null)
			wTablename.setText(jobEntry.getSchemaname());
		if (jobEntry.getTablename() != null)
			wTablename.setText(jobEntry.getTablename());
		if (jobEntry.getFilename() != null)
			wFilename.setText(jobEntry.getFilename());
		if (jobEntry.getSeparator() != null)
			wSeparator.setText(jobEntry.getSeparator());

		if (jobEntry.getEnclosed() != null)
			wEnclosed.setText(jobEntry.getEnclosed());
		wOptionEnclosed.setSelection(jobEntry.isOptionEnclosed());
	
		if (jobEntry.getLineterminated() != null)
			wLineterminated.setText(jobEntry.getLineterminated());
		
			
		wHighPriority.setSelection(jobEntry.isHighPriority());
		wOptionEnclosed.setSelection(jobEntry.isOptionEnclosed());

		if (jobEntry.getLimitlines() != null)
			wLimitlines.setText(jobEntry.getLimitlines());
		else
			wLimitlines.setText("0");
		
		if (jobEntry.getListColumn() != null)
			wListColumn.setText(jobEntry.getListColumn());
		
     
		if (jobEntry.outdumpvalue>=0) 
        {
            wOutDumpValue.select(jobEntry.outdumpvalue );
        }
        else
        {
            wOutDumpValue.select(0); // NORMAL priority
        }

	
		if (jobEntry.iffileexists>=0) 
		{
			wIfFileExists.select(jobEntry.iffileexists );
		}
		else
		{
			wIfFileExists.select(2); // FAIL
		}
		
		if (jobEntry.getDatabase() != null)
		{
			wConnection.setText(jobEntry.getDatabase().getName());
		}
		wName.selectAll();
	}

	private void cancel()
	{
		jobEntry.setChanged(changed);
		jobEntry = null;
		dispose();
	}

	private void ok()
	{
		jobEntry.setName(wName.getText());
		jobEntry.setDatabase(jobMeta.findDatabase(wConnection.getText()));
		jobEntry.setSchemaname(wSchemaname.getText());
		jobEntry.setTablename(wTablename.getText());
		jobEntry.setFilename(wFilename.getText());
		jobEntry.setSeparator(wSeparator.getText());
		jobEntry.setEnclosed(wEnclosed.getText());
		jobEntry.setOptionEnclosed(wOptionEnclosed.getSelection());
		jobEntry.setLineterminated(wLineterminated.getText());
		
		jobEntry.setLimitlines(wLimitlines.getText());
		jobEntry.setListColumn(wListColumn.getText());

		jobEntry.outdumpvalue = wOutDumpValue.getSelectionIndex();

		jobEntry.setHighPriority(wHighPriority.getSelection());
		jobEntry.iffileexists = wIfFileExists.getSelectionIndex();

		dispose();
	}
    
    private void getTableName()
    {
        // New class: SelectTableDialog
        int connr = wConnection.getSelectionIndex();
        if (connr>=0)
        {
            DatabaseMeta inf = jobMeta.getDatabase(connr);
                        
            DatabaseExplorerDialog std = new DatabaseExplorerDialog(shell, SWT.NONE, inf, jobMeta.getDatabases());
            std.setSelectedSchema(wSchemaname.getText());
            std.setSelectedTable(wTablename.getText());
            std.setSplitSchemaAndTable(true);
            if (std.open() != null)
            {
               // wSchemaname.setText(Const.NVL(std.getSchemaName(), ""));
                wTablename.setText(Const.NVL(std.getTableName(), ""));
            }
        }
        else
        {
            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
            mb.setMessage(Messages.getString("JobMysqlBulkFile.ConnectionError2.DialogMessage"));
            mb.setText(Messages.getString("System.Dialog.Error.Title"));
            mb.open(); 
        }
                    
    }

	public String toString()
	{
		return this.getClass().getName();
	}
    
    /**
     * Get a list of columns, comma separated, allow the user to select from it.
     *
     */
    private void getListColumns()
    {
        if (!Const.isEmpty(wTablename.getText()))
        {
            DatabaseMeta databaseMeta = jobMeta.findDatabase(wConnection.getText());
            if (databaseMeta!=null)
            {
                Database database = new Database(databaseMeta);
                try
                {
                    database.connect();
                    String schemaTable = databaseMeta.getQuotedSchemaTableCombination(wSchemaname.getText(), wTablename.getText());
                    RowMetaInterface row = database.getTableFields(schemaTable);
                    String available[] = row.getFieldNames();
                    
                    String source[] = wListColumn.getText().split(",");
                    for (int i=0;i<source.length;i++) source[i] = Const.trim(source[i]);
                    int idxSource[] = Const.indexsOfStrings(source, available);
                    EnterSelectionDialog dialog = new EnterSelectionDialog(shell, available, Messages.getString("JobMysqlBulkFile.SelectColumns.Title"), Messages.getString("JobMysqlBulkFile.SelectColumns.Message"));
                    dialog.setMulti(true);
                    dialog.setSelectedNrs(idxSource);
                    if (dialog.open()!=null)
                    {
                        String columns="";
                        int idx[] = dialog.getSelectionIndeces();
                        for (int i=0;i<idx.length;i++)
                        {
                            if (i>0) columns+=", ";
                            columns+=available[idx[i]];
                        }
                        wListColumn.setText(columns);
                    }
                }
                catch(KettleDatabaseException e)
                {
                    new ErrorDialog(shell, Messages.getString("System.Dialog.Error.Title"), Messages.getString("JobMysqlBulkFile.ConnectionError2.DialogMessage"), e);
                }
                finally
                {
                    database.disconnect();
                }
            }
        }
    }
}
