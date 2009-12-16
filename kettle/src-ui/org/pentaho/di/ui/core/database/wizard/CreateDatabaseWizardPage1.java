 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/


package org.pentaho.di.ui.core.database.wizard;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;


/**
 * 
 * On page one we select the database connection name, the database type and the access type.
 * 
 * @author Matt
 * @since  04-apr-2005
 */
public class CreateDatabaseWizardPage1 extends WizardPage
{
	private static Class<?> PKG = CreateDatabaseWizard.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label    wlName;
	private Text     wName;
	private FormData fdlName, fdName;
	
	private Label    wlDBType;
	private List     wDBType;
	private FormData fdlDBType, fdDBType;
	
	private Label    wlAccType;
	private List     wAccType;
	private FormData fdlAccType, fdAccType;

	private PropsUI props;
	private DatabaseMeta info;
	private java.util.List<DatabaseMeta> databases;
	
	public CreateDatabaseWizardPage1(String arg, PropsUI props, DatabaseMeta info, java.util.List<DatabaseMeta> databases)
	{
		super(arg);
		this.props=props;
		this.info = info;
		this.databases = databases;
		
		setTitle(BaseMessages.getString(PKG, "CreateDatabaseWizardPage1.DialogTitle")); //$NON-NLS-1$
		setDescription(BaseMessages.getString(PKG, "CreateDatabaseWizardPage1.DialogMessage")); //$NON-NLS-1$
		
		setPageComplete(false);
	}
	
	public void createControl(Composite parent)
	{
		int margin = Const.MARGIN;
		int middle = props.getMiddlePct();
		
		// create the composite to hold the widgets
		Composite composite = new Composite(parent, SWT.NONE);
 		props.setLook(composite);
	    
	    FormLayout compLayout = new FormLayout();
	    compLayout.marginHeight = Const.FORM_MARGIN;
	    compLayout.marginWidth  = Const.FORM_MARGIN;
		composite.setLayout(compLayout);

		wlName = new Label(composite, SWT.RIGHT);
		wlName.setText(BaseMessages.getString(PKG, "CreateDatabaseWizardPage1.DBName.Label")); //$NON-NLS-1$
 		props.setLook(wlName);
		fdlName = new FormData();
		fdlName.left   = new FormAttachment(0,0);
		fdlName.top    = new FormAttachment(0,0);
		fdlName.right  = new FormAttachment(middle,0);
		wlName.setLayoutData(fdlName);
		wName = new Text(composite, SWT.SINGLE | SWT.BORDER);
 		props.setLook(wName);
		fdName = new FormData();
		fdName.left    = new FormAttachment(middle, margin);
		fdName.right   = new FormAttachment(100, 0);
		wName.setLayoutData(fdName);
		wName.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					setPageComplete(false);
				}
			}
		);
		
		wlDBType = new Label(composite, SWT.RIGHT);
		wlDBType.setText(BaseMessages.getString(PKG, "CreateDatabaseWizardPage1.DBType.Label")); //$NON-NLS-1$
 		props.setLook(wlDBType);
		fdlDBType = new FormData();
		fdlDBType.left   = new FormAttachment(0, 0);
		fdlDBType.top    = new FormAttachment(wName, margin);
		fdlDBType.right  = new FormAttachment(middle, 0);
		wlDBType.setLayoutData(fdlDBType);
		wDBType = new List(composite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
 		props.setLook(wDBType);
    
		for (int i=0;i<DatabaseMeta.getDBTypeDescLongList().length;i++)
		{
			wDBType.add(DatabaseMeta.getDBTypeDescLongList()[i]);
		}
    
		// Select a default: the first
		if (info.getDatabaseType() <= 0) 
		{
			wDBType.select(0);
		}
		else
		{
			int idx = wDBType.indexOf(info.getDatabaseTypeDesc());
			if (idx>=0)
			{
				wDBType.select(idx);
			}
			else
			{
				wDBType.select(0);
			}
		}
		fdDBType = new FormData();
		fdDBType.top    = new FormAttachment(wName, margin);
		fdDBType.left   = new FormAttachment(middle, margin);
		fdDBType.bottom = new FormAttachment(80, 0);
		fdDBType.right  = new FormAttachment(100,0);
		wDBType.setLayoutData(fdDBType);
		wDBType.addSelectionListener
			(
				new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						setAccessTypes();
						setPageComplete(false);
					}
				}
			);
		
		wlAccType = new Label(composite, SWT.RIGHT);
		wlAccType.setText(BaseMessages.getString(PKG, "CreateDatabaseWizardPage1.DBAccessType.Label")); //$NON-NLS-1$
 		props.setLook(wlAccType);
		fdlAccType = new FormData();
		fdlAccType.left   = new FormAttachment(0, 0);
		fdlAccType.top    = new FormAttachment(wDBType, margin);
		fdlAccType.right  = new FormAttachment(middle, 0);
		wlAccType.setLayoutData(fdlAccType);
		
		wAccType = new List(composite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
 		props.setLook(wAccType);
		fdAccType = new FormData();
		fdAccType.top    = new FormAttachment(wDBType, margin);
		fdAccType.left   = new FormAttachment(middle, margin);
		fdAccType.bottom = new FormAttachment(100, 0);
		fdAccType.right  = new FormAttachment(100, 0);
		wAccType.setLayoutData(fdAccType);
		wAccType.addSelectionListener
			(
				new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						setPageComplete(false);
					}
				}
			);
		
		setAccessTypes();
		
		// set the composite as the control for this page
		setControl(composite);
	}
	
	public void setAccessTypes()
	{
		if (wDBType.getSelectionCount()<1) return;
		
		int acc[] = DatabaseMeta.getAccessTypeList(wDBType.getSelection()[0]);
		wAccType.removeAll();
		for (int i=0;i<acc.length;i++)
		{
			wAccType.add( DatabaseMeta.getAccessTypeDescLong(acc[i]) );
		}
		// If nothing is selected: select the first item (mostly the native driver)
		if (wAccType.getSelectionIndex()<0) 
		{
			wAccType.select(0);
		}
	}
	
	public boolean canFlipToNextPage()
	{
		String name = wName.getText()!=null?wName.getText().length()>0?wName.getText():null:null;
		String dbType = wDBType.getSelection().length==1?wDBType.getSelection()[0]:null;
		String acType = wAccType.getSelection().length==1?wAccType.getSelection()[0]:null;
		
		if (name==null || dbType==null || acType==null)
		{
			setErrorMessage(BaseMessages.getString(PKG, "CreateDatabaseWizardPage1.ErrorMessage.InvalidInput")); //$NON-NLS-1$
			return false;
		}
		if (name!=null && DatabaseMeta.findDatabase(databases, name)!=null)
		{
			setErrorMessage(BaseMessages.getString(PKG, "CreateDatabaseWizardPage1.ErrorMessage.DBNameExists",name)); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
		else
		{
			getDatabaseInfo();
			setErrorMessage(null);
			setMessage(BaseMessages.getString(PKG, "CreateDatabaseWizardPage1.Message.Next")); //$NON-NLS-1$
			return true;
		}
	}	
	
	public DatabaseMeta getDatabaseInfo()
	{
		if (wName.getText()!=null && wName.getText().length()>0) 
		{
			info.setName(wName.getText());
		}
		
		String dbTypeSel[] = wDBType.getSelection();
		if (dbTypeSel!=null && dbTypeSel.length==1)
		{
			info.setDatabaseType(dbTypeSel[0]);
		}
		
		String accTypeSel[] = wAccType.getSelection();
		if (accTypeSel!=null && accTypeSel.length==1)
		{
			info.setAccessType(DatabaseMeta.getAccessType(accTypeSel[0]));
		}
		
		// Also, set the default port in case of JDBC:
		info.setDBPort(String.valueOf(info.getDefaultDatabasePort()));
		
		return info;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
	 */
	public IWizardPage getNextPage()
	{
		IWizard wiz = getWizard();
		
		IWizardPage nextPage;
		switch(info.getAccessType())
		{
		case DatabaseMeta.TYPE_ACCESS_OCI:
			nextPage = wiz.getPage("oci"); // OCI //$NON-NLS-1$
			break;
		case DatabaseMeta.TYPE_ACCESS_ODBC:
			nextPage = wiz.getPage("odbc");; // ODBC //$NON-NLS-1$
			break;
		case DatabaseMeta.TYPE_ACCESS_PLUGIN:
			nextPage = wiz.getPage(info.getDatabaseTypeDesc());; // e.g. SAPR3
			break;		
		default: // Generic or Native
			if(info.getDatabaseType() == DatabaseMeta.TYPE_DATABASE_GENERIC)
			{	// Generic
				nextPage = wiz.getPage("generic");; // generic //$NON-NLS-1$
			} else { // Native
				nextPage = wiz.getPage("jdbc"); //$NON-NLS-1$
				if (nextPage!=null) 
				{
					// Set the port number...
					((CreateDatabaseWizardPageJDBC)nextPage).setData();
				}
			}
			break;
		}
		
		return nextPage;
	}
	
	public boolean canPerformFinish()
	{
		return false;
	}
}