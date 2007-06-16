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
 

package org.pentaho.di.spoon.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.Database;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.dialog.ErrorDialog;
import org.pentaho.di.core.exception.KettleDatabaseException;




/**
 * This wizard page let's you select the table that need to be ripped.
 * 
 * @author Matt
 * @since 29-mar-05
 */
public class CopyTableWizardPage2 extends WizardPage
{
	private Props props;
	
	private Shell     shell;
	 
	private String    input[];
	
	private List      wListSource;
	private Label     wlListSource;
	
    /** @deprecated */
    public CopyTableWizardPage2(String arg, LogWriter log, Props props)
    {
        super(arg);
        this.props=props;

        setTitle(Messages.getString("CopyTableWizardPage2.Dialog.Title")); //$NON-NLS-1$
        setDescription(Messages.getString("CopyTableWizardPage2.Dialog.Description")); //$NON-NLS-1$
    }
    
	public CopyTableWizardPage2(String arg)
	{
		super(arg);
		this.props=Props.getInstance();

		setTitle(Messages.getString("CopyTableWizardPage2.Dialog.Title")); //$NON-NLS-1$
		setDescription(Messages.getString("CopyTableWizardPage2.Dialog.Description")); //$NON-NLS-1$
	}
	
	public void createControl(Composite parent)
	{
		shell   = parent.getShell();
		
		// create the composite to hold the widgets
		Composite composite = new Composite(parent, SWT.NONE);
        props.setLook(composite);
	    
	    FormLayout compLayout = new FormLayout();
	    compLayout.marginHeight = Const.FORM_MARGIN;
	    compLayout.marginWidth  = Const.FORM_MARGIN;
		composite.setLayout(compLayout);

 		// Source list to the left...
		wlListSource  = new Label(composite, SWT.NONE);
		wlListSource.setText(Messages.getString("CopyTableWizardPage2.Dialog.TableList.Label")); //$NON-NLS-1$
        props.setLook(wlListSource);
 		FormData fdlListSource = new FormData();
		fdlListSource.left   = new FormAttachment(0, 0); 
		fdlListSource.top    = new FormAttachment(0, 0);
		wlListSource.setLayoutData(fdlListSource);
		
 		wListSource = new List(composite, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
        props.setLook(wListSource);
 		wListSource.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				setPageComplete(canFlipToNextPage());
			}
		});
        
 		FormData fdListSource = new FormData();
		fdListSource.left   = new FormAttachment(0, 0); 
		fdListSource.top    = new FormAttachment(wlListSource, 0);
		fdListSource.right  = new FormAttachment(100, 0);
		fdListSource.bottom = new FormAttachment(100, 0);
		wListSource.setLayoutData(fdListSource);

		// Double click adds to destination.
		wListSource.addSelectionListener(new SelectionAdapter()
			{
				public void widgetDefaultSelected(SelectionEvent e)
				{
					if (canFinish())
                    {
					    getWizard().performFinish();
                        shell.dispose();
                    }
				}
			}
		);
		
		// set the composite as the control for this page
		setControl(composite);
	}	
	
	public boolean getInputData()
	{
		// Get some data...
		CopyTableWizardPage1 page1 = (CopyTableWizardPage1)getPreviousPage();
		
		Database sourceDb = new Database(page1.getSourceDatabase());
		try
		{
			sourceDb.connect();
			input = sourceDb.getTablenames();
		}
		catch(KettleDatabaseException dbe)
		{
			new ErrorDialog(shell, Messages.getString("CopyTableWizardPage2.ErrorGettingTables.DialogTitle"), Messages.getString("CopyTableWizardPage2.ErrorGettingTables.DialogMessage"), dbe); //$NON-NLS-1$ //$NON-NLS-2$
			input = null;
			return false;
		}
		finally
		{
			sourceDb.disconnect();
		}
		return true;
	}
	
	public void getData()
	{
		wListSource.removeAll();
		
		if (input!=null)
		{
			for (int i=0;i<input.length;i++)
			{
				wListSource.add(input[i]);
			}
		}
		setPageComplete(canFlipToNextPage());
	}
	
	public boolean canFinish()
	{
		String sel[] = wListSource.getSelection();
		boolean canFlip = sel.length>0;
		return canFlip;
	}	
	
	public String getSelection()
	{
		return wListSource.getSelection()[0];
	}
}
