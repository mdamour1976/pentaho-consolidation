/*
 *
 *
 */

package be.ibridge.kettle.spoon.dialog;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;

import be.ibridge.kettle.core.LocalVariables;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.trans.TransMeta;


/**
 * Takes care of displaying a dialog that will handle the wait while 
 * where determining the impact of a transformation on the used databases.
 * 
 * @author Matt
 * @since  04-apr-2005
 */
public class AnalyseImpactProgressDialog
{
	private Props props;
	private Shell shell;
	private TransMeta transMeta;
	private ArrayList impact;
	private boolean impactHasRun;

	/**
	 * Creates a new dialog that will handle the wait while determining the impact of the transformation on the databases used...
	 */
	public AnalyseImpactProgressDialog(LogWriter log, Props props, Shell shell, TransMeta transMeta, ArrayList impact)
	{
		this.props = props;
		this.shell = shell;
		this.transMeta = transMeta;
		this.impact = impact;
	}
	
	public boolean open()
	{
		IRunnableWithProgress op = new IRunnableWithProgress()
		{
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
			{
                // This is running in a new process: copy some KettleVariables info
                LocalVariables.getInstance().createKettleVariables(Thread.currentThread(), shell.getDisplay().getSyncThread(), true);

				try
				{
					impact.clear(); // Start with a clean slate!!
					transMeta.analyseImpact(impact, monitor);
					impactHasRun = true;
				}
				catch(Exception e)
				{
					impact.clear();
					impactHasRun=false;
					throw new InvocationTargetException(e, Messages.getString("AnalyseImpactProgressDialog.RuntimeError.UnableToAnalyzeImpact.Exception", e.toString())); //Problem encountered generating impact list: {0}
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
			new ErrorDialog(shell, props, Messages.getString("AnalyseImpactProgressDialog.Dialog.UnableToAnalyzeImpact.Title"), Messages.getString("AnalyseImpactProgressDialog.Dialog.UnableToAnalyzeImpact.Messages"), e); //"Error checking transformation","An error occured checking this transformation\!"
		}
		catch (InterruptedException e)
		{
			new ErrorDialog(shell, props, Messages.getString("AnalyseImpactProgressDialog.Dialog.UnableToAnalyzeImpact.Title"), Messages.getString("AnalyseImpactProgressDialog.Dialog.UnableToAnalyzeImpact.Messages"), e); //"Error checking transformation","An error occured checking this transformation\!"
		}
		
		return impactHasRun;
	}
}
