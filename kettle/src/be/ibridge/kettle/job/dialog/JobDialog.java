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


 
package be.ibridge.kettle.job.dialog;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.dialog.DatabaseDialog;
import be.ibridge.kettle.core.dialog.SQLEditor;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.repository.RepositoryDirectory;
import be.ibridge.kettle.repository.dialog.SelectDirectoryDialog;
import be.ibridge.kettle.trans.step.BaseStepDialog;


/**
 * Allows you to edit the Job settings.  Just pass a JobInfo object.
 * 
 * @author Matt
 * @since  02-jul-2003
 */
public class JobDialog extends Dialog
{
	private LogWriter    log;
	private Props        props;
		
	private Label        wlJobname;
	private Text         wJobname;
    private FormData     fdlJobname, fdJobname;

    private Label        wlDirectory;
	private Text         wDirectory;
	private Button       wbDirectory;
    private FormData     fdlDirectory, fdbDirectory, fdDirectory;    

	private Label        wlLogconnection;
	private Button       wbLogconnection;
	private CCombo       wLogconnection;
	private FormData     fdlLogconnection, fdbLogconnection, fdLogconnection;

	private Label        wlLogtable;
	private Text         wLogtable;
	private FormData     fdlLogtable, fdLogtable;

    private Label        wlBatch;
    private Button       wBatch;
    private FormData     fdlBatch, fdBatch;

    private Label        wlBatchTrans;
    private Button       wBatchTrans;
    private FormData     fdlBatchTrans, fdBatchTrans;

    private Label        wlLogfield;
    private Button       wLogfield;
    private FormData     fdlLogfield, fdLogfield;

	private Button wOK, wSQL, wCancel;
	private Listener lsOK, lsSQL, lsCancel;

	private JobMeta jobMeta;
	private Shell  shell;
	private Repository rep;
	
	private SelectionAdapter lsDef;
	
	private ModifyListener lsMod;
	private boolean changed;

    /** @deprecated */
    public JobDialog(Shell parent, int style, LogWriter l, Props props, JobMeta jobMeta, Repository rep)
    {
        this(parent, style, jobMeta, rep);
    }

	public JobDialog(Shell parent, int style, JobMeta jobMeta, Repository rep)
	{
		super(parent, style);
		this.log=LogWriter.getInstance();
		this.jobMeta=jobMeta;
		this.props=Props.getInstance();
		this.rep=rep;
	}
	

	public JobMeta open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(		shell);
		
		lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				jobMeta.setChanged();
			}
		};
		changed = jobMeta.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("Job properties");
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Transformation name:
		wlJobname=new Label(shell, SWT.RIGHT);
		wlJobname.setText("Job name :");
 		props.setLook(		wlJobname);
		fdlJobname=new FormData();
		fdlJobname.left = new FormAttachment(0, 0);
        fdlJobname.right= new FormAttachment(middle, 0);
		fdlJobname.top  = new FormAttachment(0, margin);
		wlJobname.setLayoutData(fdlJobname);
		wJobname=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(		wJobname);
		wJobname.addModifyListener(lsMod);
		fdJobname=new FormData();
		fdJobname.left = new FormAttachment(middle, 0);
		fdJobname.top  = new FormAttachment(0, margin);
		fdJobname.right= new FormAttachment(100, 0);
		wJobname.setLayoutData(fdJobname);

		// Directory:
		wlDirectory=new Label(shell, SWT.RIGHT);
		wlDirectory.setText("Directory :");
 		props.setLook(wlDirectory);
		fdlDirectory=new FormData();
		fdlDirectory.left = new FormAttachment(0, 0);
		fdlDirectory.right= new FormAttachment(middle, -margin);
		fdlDirectory.top  = new FormAttachment(wJobname, margin);
		wlDirectory.setLayoutData(fdlDirectory);

		wbDirectory=new Button(shell, SWT.PUSH);
		wbDirectory.setText("...");
 		props.setLook(wbDirectory);
		fdbDirectory=new FormData();
		fdbDirectory.top  = new FormAttachment(wJobname, margin);
		fdbDirectory.right= new FormAttachment(100, 0);
		wbDirectory.setLayoutData(fdbDirectory);
		wbDirectory.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				RepositoryDirectory directoryFrom = jobMeta.getDirectory();
				long idDirectoryFrom  = directoryFrom.getID();
				
				SelectDirectoryDialog sdd = new SelectDirectoryDialog(shell, SWT.NONE, rep);
				RepositoryDirectory rd = sdd.open();
				if (rd!=null)
				{
					if (idDirectoryFrom!=rd.getID())
					{
					 	try
						{
					 		rep.moveJob(jobMeta.getName(), idDirectoryFrom, rd.getID() );
					 		log.logDetailed(getClass().getName(), "Moved directory to ["+rd.getPath()+"]");
							jobMeta.setDirectory( rd );
							wDirectory.setText(jobMeta.getDirectory().getPath());
						}
					 	catch(KettleDatabaseException dbe)
					 	{
					 		jobMeta.setDirectory( directoryFrom );
					 		
							MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
							mb.setText("Error!");
							mb.setMessage("There was an error moving the jobMeta to another directory!");
							mb.open();
					 	}
					}
					else
					{
						// Same directory!
					}
				}
			}
		});

		wDirectory=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wDirectory);
		wDirectory.setEditable(false);
		fdDirectory=new FormData();
		fdDirectory.top  = new FormAttachment(wJobname, margin);
		fdDirectory.left = new FormAttachment(middle, 0);
		fdDirectory.right= new FormAttachment(wbDirectory, 0);
		wDirectory.setLayoutData(fdDirectory);

		// Log table connection...
		wlLogconnection=new Label(shell, SWT.RIGHT);
		wlLogconnection.setText("Log Connection: ");
 		props.setLook(wlLogconnection);
		fdlLogconnection=new FormData();
		fdlLogconnection.top  = new FormAttachment(wDirectory, margin*4);
		fdlLogconnection.left = new FormAttachment(0, 0);
        fdlLogconnection.right= new FormAttachment(middle, 0);
		wlLogconnection.setLayoutData(fdlLogconnection);

		wbLogconnection=new Button(shell, SWT.PUSH);
		wbLogconnection.setText("&Edit...");
		fdbLogconnection=new FormData();
		fdbLogconnection.top   = new FormAttachment(wDirectory, margin*4);
		fdbLogconnection.right = new FormAttachment(100, 0);
		wbLogconnection.setLayoutData(fdbLogconnection);
        wbLogconnection.addSelectionListener(new SelectionAdapter() 
            {
                public void widgetSelected(SelectionEvent e) 
                {
                    DatabaseMeta ci = jobMeta.findDatabase(wLogconnection.getText());
                    if (ci==null) ci=new DatabaseMeta();
                    DatabaseDialog cid = new DatabaseDialog(shell, SWT.NONE, log, ci, props);
                    if (cid.open()!=null)
                    {
                        wLogconnection.setText(ci.getName());
                    }
                }
            }
        );

		wLogconnection=new CCombo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wLogconnection);
		wLogconnection.addModifyListener(lsMod);
		fdLogconnection=new FormData();
		fdLogconnection.top  = new FormAttachment(wDirectory, margin*4);
		fdLogconnection.left = new FormAttachment(middle, 0);
		fdLogconnection.right= new FormAttachment(wbLogconnection, -margin);
		wLogconnection.setLayoutData(fdLogconnection);
        

        // populare the combo box...
        for (int i=0;i<jobMeta.nrDatabases();i++)
        {
            DatabaseMeta meta = jobMeta.getDatabase(i);
            wLogconnection.add(meta.getName());
        }

		// Log table...:
		wlLogtable=new Label(shell, SWT.RIGHT);
		wlLogtable.setText("Log table:");
 		props.setLook(wlLogtable);
		fdlLogtable=new FormData();
        fdlLogtable.left = new FormAttachment(0, 0);
		fdlLogtable.right= new FormAttachment(middle, 0);
		fdlLogtable.top  = new FormAttachment(wLogconnection, margin);
		wlLogtable.setLayoutData(fdlLogtable);
		wLogtable=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wLogtable);
		wLogtable.addModifyListener(lsMod);
		fdLogtable=new FormData();
		fdLogtable.left = new FormAttachment(middle, 0);
		fdLogtable.top  = new FormAttachment(wLogconnection, margin);
		fdLogtable.right= new FormAttachment(100, 0);
		wLogtable.setLayoutData(fdLogtable);

        wlBatch=new Label(shell, SWT.RIGHT);
        wlBatch.setText("Use Batch-ID? ");
        props.setLook(wlBatch);
        fdlBatch=new FormData();
        fdlBatch.left = new FormAttachment(0, 0);
        fdlBatch.top  = new FormAttachment(wLogtable, margin*3);
        fdlBatch.right= new FormAttachment(middle, -margin);
        wlBatch.setLayoutData(fdlBatch);
        wBatch=new Button(shell, SWT.CHECK);
        props.setLook(wBatch);
        fdBatch=new FormData();
        fdBatch.left = new FormAttachment(middle, 0);
        fdBatch.top  = new FormAttachment(wLogtable, margin*3);
        fdBatch.right= new FormAttachment(100, 0);
        wBatch.setLayoutData(fdBatch);

        wlBatchTrans=new Label(shell, SWT.RIGHT);
        wlBatchTrans.setText("Pass the batch ID to job entries");
        props.setLook(wlBatchTrans);
        fdlBatchTrans=new FormData();
        fdlBatchTrans.left = new FormAttachment(0, 0);
        fdlBatchTrans.top  = new FormAttachment(wBatch, margin);
        fdlBatchTrans.right= new FormAttachment(middle, -margin);
        wlBatchTrans.setLayoutData(fdlBatchTrans);
        wBatchTrans=new Button(shell, SWT.CHECK);
        props.setLook(wBatchTrans);
        fdBatchTrans=new FormData();
        fdBatchTrans.left = new FormAttachment(middle, 0);
        fdBatchTrans.top  = new FormAttachment(wBatch, margin);
        fdBatchTrans.right= new FormAttachment(100, 0);
        wBatchTrans.setLayoutData(fdBatchTrans);

        wlLogfield=new Label(shell, SWT.RIGHT);
        wlLogfield.setText("Use logfield to store logging in? ");
        props.setLook(wlLogfield);
        fdlLogfield=new FormData();
        fdlLogfield.left = new FormAttachment(0, 0);
        fdlLogfield.top  = new FormAttachment(wBatchTrans, margin);
        fdlLogfield.right= new FormAttachment(middle, -margin);
        wlLogfield.setLayoutData(fdlLogfield);
        wLogfield=new Button(shell, SWT.CHECK);
        props.setLook(wLogfield);
        fdLogfield=new FormData();
        fdLogfield.left = new FormAttachment(middle, 0);
        fdLogfield.top  = new FormAttachment(wBatchTrans, margin);
        fdLogfield.right= new FormAttachment(100, 0);
        wLogfield.setLayoutData(fdLogfield);

		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(" &OK ");
		wSQL=new Button(shell, SWT.PUSH);
		wSQL.setText(" &SQL ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(" &Cancel ");

        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wSQL, wCancel }, margin, wLogfield);
        
		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsSQL      = new Listener() { public void handleEvent(Event e) { sql();    } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wSQL.addListener   (SWT.Selection, lsSQL   );
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wJobname.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		WindowProperty winprop = props.getScreen(shell.getText());
		if (winprop!=null) winprop.setShell(shell); else shell.pack();

		getData();
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return jobMeta;
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
		log.logDebug(toString(), "getting transformation info...");
	
		if (jobMeta.getName()!=null)           wJobname.setText      ( jobMeta.getName());
		if (jobMeta.getDirectory()!=null)      wDirectory.setText    ( jobMeta.getDirectory().getPath() );
		if (jobMeta.getLogConnection()!=null)  wLogconnection.setText( jobMeta.getLogConnection().getName());
		if (jobMeta.getLogTable()!=null)       wLogtable.setText     ( jobMeta.getLogTable());
        
        wBatch.setSelection(jobMeta.isBatchIdUsed());
        wBatchTrans.setSelection(jobMeta.isBatchIdPassed());
        wLogfield.setSelection(jobMeta.isLogfieldUsed());
	}
	
	private void cancel()
	{
		jobMeta.setChanged(changed);
		jobMeta=null;
		dispose();
	}
	
	private void ok()
	{
		jobMeta.setName( wJobname.getText() );
		jobMeta.setLogConnection( jobMeta.findDatabase(wLogconnection.getText()) );
        jobMeta.setLogTable( wLogtable.getText() );
        
        jobMeta.setUseBatchId( wBatch.getSelection());
        jobMeta.setBatchIdPassed( wBatchTrans.getSelection());
        jobMeta.setLogfieldUsed( wLogfield.getSelection());

		dispose();
	}
	
	// Generate code for create table...
	// Conversions done by Database
	private void sql()
	{
		DatabaseMeta ci = jobMeta.findDatabase(wLogconnection.getText());
		if (ci!=null)
		{
			Row r = Database.getJobLogrecordFields(wBatch.getSelection(), wLogfield.getSelection());
			if (r!=null && r.size()>0)
			{
				String tablename = wLogtable.getText();
				if (tablename!=null && tablename.length()>0)
				{
					Database db = new Database(ci);
					try
					{
                        db.connect();
                        
                        // Get the DDL for the specified tablename and fields...
						String createTable = db.getDDL(tablename, r);
						log.logBasic(toString(), createTable);
	
						SQLEditor sqledit = new SQLEditor(shell, SWT.NONE, ci, null, createTable);
						sqledit.open();
					}
					catch(KettleDatabaseException dbe)
					{
						MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
						mb.setMessage("An error occured creating the SQL:"+Const.CR+dbe.getMessage());
						mb.setText("ERROR");
						mb.open();
					}
                    finally
                    {
                        db.disconnect();
                    }
				}
				else
				{
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
					mb.setMessage("Please enter a logtable-name!");
					mb.setText("ERROR");
					mb.open(); 
				}
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.setMessage("I couldn't find any fields to create the logtable!");
				mb.setText("ERROR");
				mb.open(); 
			}
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage("Please select/create a valid logtable connection!");
			mb.setText("ERROR");
			mb.open(); 
		}
	}



	public String toString()
	{
		return this.getClass().getName();
	}
}
