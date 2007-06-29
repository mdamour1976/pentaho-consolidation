
package org.pentaho.di.core.database.dialog;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.dialog.ErrorDialog;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.LocalVariables;


/**
 * Takes care of displaying a dialog that will handle the wait while 
 * we're getting the number of rows for a certain table in a database.
 * 
 * @author Matt
 * @since  12-may-2005
 */
public class GetTableSizeProgressDialog
{
	private Shell shell;
	private DatabaseMeta dbMeta;
	private String tableName;
	private Long size;
	
	private Database db;
    private Thread parentThread;
    
	/**
	 * Creates a new dialog that will handle the wait while we're doing the hard work.
	 */
	public GetTableSizeProgressDialog(Shell shell, DatabaseMeta dbInfo, String tableName)
	{
		this.shell = shell;
		this.dbMeta = dbInfo;
		this.tableName = tableName;
        
        this.parentThread = Thread.currentThread();
	}
	
	public Long open()
	{
		IRunnableWithProgress op = new IRunnableWithProgress()
		{
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
			{
                // This is running in a new process: copy some KettleVariables info
                LocalVariables.getInstance().createKettleVariables(Thread.currentThread().getName(), parentThread.getName(), true);

				db = new Database(dbMeta);
				try 
				{
					db.connect();
					
					String sql = "SELECT count(*) FROM "+tableName;
					RowMetaAndData row =  db.getOneRow(sql);
                    size = row.getRowMeta().getInteger(row.getData(), 0);
					
					if (monitor.isCanceled()) 
						throw new InvocationTargetException(new Exception("This operation was cancelled!"));

				}
				catch(KettleException e)
				{
					throw new InvocationTargetException(e, "Couldn't get a result because of an error :"+e.toString());
				}
				finally
				{
					db.disconnect();
				}
			}
		};
		
		try
		{
			final ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell);
			// Run something in the background to cancel active database queries, forecably if needed!
			Runnable run = new Runnable()
            {
                public void run()
                {
                    IProgressMonitor monitor = pmd.getProgressMonitor();
                    while (pmd.getShell()==null || ( !pmd.getShell().isDisposed() && !monitor.isCanceled() ))
                    {
                        try { Thread.sleep(100); } catch(InterruptedException e) { };
                    }
                    
                    if (monitor.isCanceled()) // Disconnect and see what happens!
                    {
                        try { db.cancelQuery(); } catch(Exception e) {};
                    }
                }
            };
            // Start the cancel tracker in the background!
            new Thread(run).start();
            
			pmd.run(true, true, op);
		}
		catch (InvocationTargetException e)
		{
			showErrorDialog(e);
			return null;
		}
		catch (InterruptedException e)
		{
            showErrorDialog(e);
			return null;
		}
		
		return size;
	}

    /**
     * Showing an error dialog
     * 
     * @param e
    */
    private void showErrorDialog(Exception e)
    {
        new ErrorDialog(shell, Messages.getString("GetTableSizeProgressDialog.Error.Title"),
            Messages.getString("GetTableSizeProgressDialog.Error.Message"), e);
    }
}
