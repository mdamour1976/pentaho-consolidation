/*
 *
 *
 */

package org.pentaho.di.repository.dialog;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.repository.Repository;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.variables.LocalVariables;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.dialog.ErrorDialog;
import org.pentaho.di.core.exception.KettleException;

/**
 * Takes care of displaying a dialog that will handle the wait while we are exporting the complete
 * repository to XML...
 * 
 * @author Matt
 * @since 02-jun-2005
 */
public class RepositoryExportProgressDialog
{
    private Shell shell;
    private Repository rep;
    private String filename;
    private Thread parentThread;

    public RepositoryExportProgressDialog(Shell shell, Repository rep, String filename)
    {
        this.shell = shell;
        this.rep = rep;
        this.filename = filename;

        this.parentThread = Thread.currentThread();
    }

    public boolean open()
    {
        boolean retval = true;

        IRunnableWithProgress op = new IRunnableWithProgress()
        {
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
            {
                // This is running in a new process: copy some KettleVariables info
                LocalVariables.getInstance().createKettleVariables(Thread.currentThread().getName(), parentThread.getName(), true);

                try
                {
                    rep.exportAllObjects(monitor, filename);
                }
                catch (KettleException e)
                {
                    LogWriter.getInstance().logError(toString(), Const.getStackTracker(e));
                    throw new InvocationTargetException(e, Messages.getString("RepositoryExportDialog.Error.CreateUpdate", e.getMessage()));
                }
            }
        };

        try
        {
            ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell);
            pmd.run(true, true, op);
        }
        catch (InvocationTargetException e)
        {
            LogWriter.getInstance().logError(RepositoryExportProgressDialog.class.toString(), "Error creating repository: " + e.toString());
            LogWriter.getInstance().logError(toString(), Const.getStackTracker(e));
            new ErrorDialog(shell, Messages.getString("RepositoryExportDialog.ErrorExport.Title"), Messages.getString("RepositoryExportDialog.ErrorExport.Message"), e);
            retval = false;
        }
        catch (InterruptedException e)
        {
            LogWriter.getInstance().logError(RepositoryExportProgressDialog.class.toString(), "Error creating repository: " + e.toString());
            LogWriter.getInstance().logError(toString(), Const.getStackTracker(e));
            new ErrorDialog(shell, Messages.getString("RepositoryExportDialog.ErrorExport.Title"), Messages.getString("RepositoryExportDialog.ErrorExport.Message"), e);
            retval = false;
        }

        return retval;
    }
}
