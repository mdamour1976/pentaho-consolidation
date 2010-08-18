/*************************************************************************************** 
 * Copyright (C) 2007 Samatar.  All rights reserved. 
 * This software was developed by Samatar and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. A copy of the license, 
 * is included with the binaries and source code. The Original Code is Samatar.  
 * The Initial Developer is Samatar.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * Please refer to the license for the specific language governing your rights 
 * and limitations.
 ***************************************************************************************/

package org.pentaho.di.ui.trans.steps.ldapoutput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusListener;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.SourceToTargetMapping;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.ldapoutput.LDAPOutputMeta;
import org.pentaho.di.trans.steps.ldapinput.LDAPConnection;
import org.pentaho.di.ui.core.dialog.EnterMappingDialog;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;

public class LDAPOutputDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = LDAPOutputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private CTabFolder   wTabFolder;
	private FormData     fdTabFolder;
	
	private CTabItem     wGeneralTab;

	private Composite    wGeneralComp;
	private FormData     fdGeneralComp;
	
	
	private CTabItem     wFieldsTab;

	private Composite    wFieldsComp;
	private FormData     fdFieldsComp;
	
	private Label        wlusingAuthentication;
	private Button       wusingAuthentication;
	private FormData     fdlusingAuthentication;

	private LDAPOutputMeta input;
    
	private Group wConnectionGroup;
	private FormData fdConnectionGroup;
	
	private Group wSettings;
	private FormData fdSettings;
	private Group wFields;
	private FormData fdFields;
	
	
	private Label        wlHost;
	private TextVar      wHost;
	private FormData     fdlHost, fdHost;
	
	private Label        wlUserName;
	private TextVar      wUserName;
	private FormData     fdlUserName, fdUserName;
	
	private Label        wlPassword;
	private TextVar      wPassword;
	private FormData     fdlPassword, fdPassword;
	
	private Label        wlPort;
	private TextVar      wPort;
	private FormData     fdlPort, fdPort;
	
	private Button wTest;
	private FormData fdTest;
	
    private ComboVar wDeleteField;
    private FormData fdDeleteField;
    private Label wlDeleteField;
    private FormData fdlDeleteField;
    
	private Label        wlFailIfNotExist;
	private Button       wFailIfNotExist;
	private FormData     fdlFailIfNotExist, fdFailIfNotExist;

	
    private Map<String, Integer> inputFields;
    
    private ColumnInfo[] ciReturn;
    
	private Button     wDoMapping;
	private FormData   fdDoMapping;
	
	private Label		wlReturn;
	private TableView	wReturn;
	private FormData	fdlReturn, fdReturn;
	
	private Button		wGetLU;
	private FormData	fdGetLU;
	private Listener	lsGetLU;
	
	private Label 		wlOperation;
	private CCombo 		wOperation;
	private FormData    fdlOperation;
	private FormData    fdOperation;
	
	private Label 		wlReferral;
	private CCombo 		wReferral;
	private FormData    fdlReferral;
	private FormData    fdReferral;
	
	private Label 		wlDerefAliases;
	private CCombo 		wDerefAliases;
	private FormData    fdlDerefAliases;
	private FormData    fdDerefAliases;
	
	private Label        wlMultiValuedSeparator;
	private TextVar      wMultiValuedSeparator;
	private FormData     fdlMultiValuedSeparator, fdMultiValuedSeparator;
	
	private Label        wlBaseDN;
	private TextVar      wBaseDN;
	private FormData     fdlBaseDN, fdBaseDN;
    
	/**
	 * List of ColumnInfo that should have the field names of the selected base dn
	 */
	private List<ColumnInfo> tableFieldColumns = new ArrayList<ColumnInfo>();
    
    private Listener lsTest;
	private boolean gotPrevious=false;
	
	public static final int dateLengths[] = new int[]
	{
		23, 19, 14, 10, 10, 10, 10, 8, 8, 8, 8, 6, 6
	};

	public LDAPOutputDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(LDAPOutputMeta)in;
        inputFields =new HashMap<String, Integer>();
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);
        setShellImage(shell, input);

		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				input.setChanged();
			}
		};
		changed = input.hasChanged();
		
		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "LDAPOutputDialog.DialogTitle"));
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName"));
 		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.top  = new FormAttachment(0, margin);
		fdlStepname.right= new FormAttachment(middle, -margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
 		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname=new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top  = new FormAttachment(0, margin);
		fdStepname.right= new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

		wTabFolder = new CTabFolder(shell, SWT.BORDER);
 		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);

		//////////////////////////
		// START OF GENERAL TAB   ///
		//////////////////////////
		wGeneralTab=new CTabItem(wTabFolder, SWT.NONE);
		wGeneralTab.setText(BaseMessages.getString(PKG, "LDAPOutputDialog.General.Tab"));
		
		wGeneralComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wGeneralComp);

		FormLayout fileLayout = new FormLayout();
		fileLayout.marginWidth  = 3;
		fileLayout.marginHeight = 3;
		wGeneralComp.setLayout(fileLayout);
		
		// /////////////////////////////////
		// START OF Connection GROUP
		// /////////////////////////////////

		wConnectionGroup = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(wConnectionGroup);
		wConnectionGroup.setText(BaseMessages.getString(PKG, "LDAPOutputDialog.Group.ConnectionGroup.Label"));
		
		FormLayout connectiongroupLayout = new FormLayout();
		connectiongroupLayout.marginWidth = 10;
		connectiongroupLayout.marginHeight = 10;
		wConnectionGroup.setLayout(connectiongroupLayout);

		// Host line
		wlHost=new Label(wConnectionGroup, SWT.RIGHT);
		wlHost.setText(BaseMessages.getString(PKG, "LDAPOutputDialog.Host.Label"));
 		props.setLook(wlHost);
		fdlHost=new FormData();
		fdlHost.left = new FormAttachment(0, 0);
		fdlHost.top  = new FormAttachment(wStepname, margin);
		fdlHost.right= new FormAttachment(middle, -margin);
		wlHost.setLayoutData(fdlHost);
		wHost=new TextVar(transMeta, wConnectionGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wHost.setToolTipText(BaseMessages.getString(PKG, "LDAPOutputDialog.Host.Tooltip"));
		props.setLook(wHost);
		wHost.addModifyListener(lsMod);
		fdHost=new FormData();
		fdHost.left = new FormAttachment(middle, 0);
		fdHost.top  = new FormAttachment(wStepname, margin);
		fdHost.right= new FormAttachment(100, 0);
		wHost.setLayoutData(fdHost);
		
		// Port line
		wlPort=new Label(wConnectionGroup, SWT.RIGHT);
		wlPort.setText(BaseMessages.getString(PKG, "LDAPOutputDialog.Port.Label"));
 		props.setLook(wlPort);
		fdlPort=new FormData();
		fdlPort.left = new FormAttachment(0, 0);
		fdlPort.top  = new FormAttachment(wHost, margin);
		fdlPort.right= new FormAttachment(middle, -margin);
		wlPort.setLayoutData(fdlPort);
		wPort=new TextVar(transMeta, wConnectionGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wPort);
		wPort.setToolTipText(BaseMessages.getString(PKG, "LDAPOutputDialog.Port.Tooltip"));
		wPort.addModifyListener(lsMod);
		fdPort=new FormData();
		fdPort.left = new FormAttachment(middle, 0);
		fdPort.top  = new FormAttachment(wHost, margin);
		fdPort.right= new FormAttachment(100, 0);
		wPort.setLayoutData(fdPort);
		
		// using authentication ?
		wlusingAuthentication=new Label(wConnectionGroup, SWT.RIGHT);
		wlusingAuthentication.setText(BaseMessages.getString(PKG, "LDAPOutputDialog.usingAuthentication.Label"));
 		props.setLook(wlusingAuthentication);
		fdlusingAuthentication=new FormData();
		fdlusingAuthentication.left = new FormAttachment(0, 0);
		fdlusingAuthentication.top  = new FormAttachment(wPort, margin);
		fdlusingAuthentication.right= new FormAttachment(middle, -margin);
		wlusingAuthentication.setLayoutData(fdlusingAuthentication);
		wusingAuthentication=new Button(wConnectionGroup, SWT.CHECK );
 		props.setLook(wusingAuthentication);
		wusingAuthentication.setToolTipText(BaseMessages.getString(PKG, "LDAPOutputDialog.usingAuthentication.Tooltip"));
		FormData fdusingAuthentication=new FormData();
		fdusingAuthentication.left = new FormAttachment(middle, 0);
		fdusingAuthentication.top  = new FormAttachment(wPort, margin);
		wusingAuthentication.setLayoutData(fdusingAuthentication);
		
		wusingAuthentication.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				useAuthentication();
			}
		}
	);
	
		// UserName line
		wlUserName=new Label(wConnectionGroup, SWT.RIGHT);
		wlUserName.setText(BaseMessages.getString(PKG, "LDAPOutputDialog.Username.Label"));
 		props.setLook(wlUserName);
		fdlUserName=new FormData();
		fdlUserName.left = new FormAttachment(0, 0);
		fdlUserName.top  = new FormAttachment(wusingAuthentication, margin);
		fdlUserName.right= new FormAttachment(middle, -margin);
		wlUserName.setLayoutData(fdlUserName);
		wUserName=new TextVar(transMeta, wConnectionGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wUserName);
		wUserName.setToolTipText(BaseMessages.getString(PKG, "LDAPOutputDialog.Username.Tooltip"));
		wUserName.addModifyListener(lsMod);
		fdUserName=new FormData();
		fdUserName.left = new FormAttachment(middle, 0);
		fdUserName.top  = new FormAttachment(wusingAuthentication, margin);
		fdUserName.right= new FormAttachment(100, 0);
		wUserName.setLayoutData(fdUserName);

		// Password line
		wlPassword=new Label(wConnectionGroup, SWT.RIGHT);
		wlPassword.setText(BaseMessages.getString(PKG, "LDAPOutputDialog.Password.Label"));
 		props.setLook(wlPassword);
		fdlPassword=new FormData();
		fdlPassword.left = new FormAttachment(0, 0);
		fdlPassword.top  = new FormAttachment(wUserName, margin);
		fdlPassword.right= new FormAttachment(middle, -margin);
		wlPassword.setLayoutData(fdlPassword);
		wPassword=new TextVar(transMeta, wConnectionGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wPassword.setToolTipText(BaseMessages.getString(PKG, "LDAPOutputDialog.Password.Tooltip"));
 		props.setLook(wPassword);
        wPassword.setEchoChar('*');
        wPassword.addModifyListener(lsMod);
		fdPassword=new FormData();
		fdPassword.left = new FormAttachment(middle, 0);
		fdPassword.top  = new FormAttachment(wUserName, margin);
		fdPassword.right= new FormAttachment(100, 0);
		wPassword.setLayoutData(fdPassword);
		
		
		// Referral
		wlReferral=new Label(wConnectionGroup, SWT.RIGHT);
		wlReferral.setText(BaseMessages.getString(PKG, "LDAPOutputDialog.Referral.Label")); //$NON-NLS-1$
 		props.setLook(wlReferral);
		fdlReferral=new FormData();
		fdlReferral.left = new FormAttachment(0, 0);
		fdlReferral.right= new FormAttachment(middle, -margin);
		fdlReferral.top  = new FormAttachment(wPassword, margin);
		wlReferral.setLayoutData(fdlReferral);
		
		wReferral=new CCombo(wConnectionGroup, SWT.BORDER | SWT.READ_ONLY);
 		props.setLook(wReferral);
 		wReferral.addModifyListener(lsMod);
		fdReferral=new FormData();
		fdReferral.left = new FormAttachment(middle, 0);
		fdReferral.top  = new FormAttachment(wPassword, margin);
		fdReferral.right= new FormAttachment(100, -margin);
		wReferral.setLayoutData(fdReferral);
		wReferral.setItems(LDAPOutputMeta.referralTypeDesc);
		wReferral.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				input.setChanged();
			}
		});

		

		// DerefAliases
		wlDerefAliases=new Label(wConnectionGroup, SWT.RIGHT);
		wlDerefAliases.setText(BaseMessages.getString(PKG, "LDAPOutputDialog.DerefAliases.Label")); //$NON-NLS-1$
 		props.setLook(wlDerefAliases);
		fdlDerefAliases=new FormData();
		fdlDerefAliases.left = new FormAttachment(0, 0);
		fdlDerefAliases.right= new FormAttachment(middle, -margin);
		fdlDerefAliases.top  = new FormAttachment(wReferral, margin);
		wlDerefAliases.setLayoutData(fdlDerefAliases);
		
		wDerefAliases=new CCombo(wConnectionGroup, SWT.BORDER | SWT.READ_ONLY);
 		props.setLook(wDerefAliases);
 		wDerefAliases.addModifyListener(lsMod);
		fdDerefAliases=new FormData();
		fdDerefAliases.left = new FormAttachment(middle, 0);
		fdDerefAliases.top  = new FormAttachment(wReferral, margin);
		fdDerefAliases.right= new FormAttachment(100, -margin);
		wDerefAliases.setLayoutData(fdDerefAliases);
		wDerefAliases.setItems(LDAPOutputMeta.derefAliasesTypeDesc);
		wDerefAliases.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				input.setChanged();
			}
		});
		
		// Test LDAP connection button
		wTest=new Button(wConnectionGroup,SWT.PUSH);
		wTest.setText(BaseMessages.getString(PKG, "LDAPOutputDialog.TestConnection.Label"));
 		props.setLook(wTest);
		fdTest=new FormData();
		wTest.setToolTipText(BaseMessages.getString(PKG, "LDAPOutputDialog.TestConnection.Tooltip"));
		fdTest.top  = new FormAttachment(wDerefAliases, margin);
		fdTest.right= new FormAttachment(100, 0);
		wTest.setLayoutData(fdTest);
		
		
		fdConnectionGroup = new FormData();
		fdConnectionGroup.left = new FormAttachment(0, margin);
		fdConnectionGroup.top = new FormAttachment(0, margin);
		fdConnectionGroup.right = new FormAttachment(100, -margin);
		wConnectionGroup.setLayoutData(fdConnectionGroup);
		
		// ///////////////////////////////////////////////////////////
		// / END OF CONNECTION  GROUP
		// ///////////////////////////////////////////////////////////
		
		// /////////////////////////////////
		// START OF Search GROUP
		// /////////////////////////////////

		wSettings = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(wSettings);
		wSettings.setText(BaseMessages.getString(PKG, "LDAPOutputDialog.Group.Settings.Label"));
		
		FormLayout SettingsLayout = new FormLayout();
		SettingsLayout.marginWidth = 10;
		SettingsLayout.marginHeight = 10;
		wSettings.setLayout(SettingsLayout);
		
		// Operation
		wlOperation=new Label(wSettings, SWT.RIGHT);
		wlOperation.setText(BaseMessages.getString(PKG, "LDAPOutputDialog.Operation.Label")); //$NON-NLS-1$
 		props.setLook(wlOperation);
		fdlOperation=new FormData();
		fdlOperation.left = new FormAttachment(0, 0);
		fdlOperation.right= new FormAttachment(middle, -margin);
		fdlOperation.top  = new FormAttachment(wConnectionGroup, margin);
		wlOperation.setLayoutData(fdlOperation);
		
		wOperation=new CCombo(wSettings, SWT.BORDER | SWT.READ_ONLY);
 		props.setLook(wOperation);
 		wOperation.addModifyListener(lsMod);
		fdOperation=new FormData();
		fdOperation.left = new FormAttachment(middle, 0);
		fdOperation.top  = new FormAttachment(wConnectionGroup, margin);
		fdOperation.right= new FormAttachment(100, -margin);
		wOperation.setLayoutData(fdOperation);
		wOperation.setItems(LDAPOutputMeta.operationTypeDesc);
		wOperation.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				updateOperation();
				input.setChanged();
			}
		});
		
		// Multi valued field separator
		wlMultiValuedSeparator=new Label(wSettings, SWT.RIGHT);
		wlMultiValuedSeparator.setText(BaseMessages.getString(PKG, "LDAPOutputDialog.MultiValuedSeparator.Label"));
 		props.setLook(wlMultiValuedSeparator);
		fdlMultiValuedSeparator=new FormData();
		fdlMultiValuedSeparator.left = new FormAttachment(0, 0);
		fdlMultiValuedSeparator.top  = new FormAttachment(wOperation, margin);
		fdlMultiValuedSeparator.right= new FormAttachment(middle, -margin);
		wlMultiValuedSeparator.setLayoutData(fdlMultiValuedSeparator);
		wMultiValuedSeparator=new TextVar(transMeta,wSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wMultiValuedSeparator);
 		wMultiValuedSeparator.setToolTipText(BaseMessages.getString(PKG, "LDAPOutputDialog.MultiValuedSeparator.Tooltip"));
		wMultiValuedSeparator.addModifyListener(lsMod);
		fdMultiValuedSeparator=new FormData();
		fdMultiValuedSeparator.left = new FormAttachment(middle, 0);
		fdMultiValuedSeparator.top  = new FormAttachment(wOperation, margin);
		fdMultiValuedSeparator.right= new FormAttachment(100, 0);
		wMultiValuedSeparator.setLayoutData(fdMultiValuedSeparator);
		
		// Add File to the result files name
		wlFailIfNotExist=new Label(wSettings, SWT.RIGHT);
		wlFailIfNotExist.setText(BaseMessages.getString(PKG, "LDAPOutputDialog.FailIfNotExist.Label"));
		props.setLook(wlFailIfNotExist);
		fdlFailIfNotExist=new FormData();
		fdlFailIfNotExist.left  = new FormAttachment(0, 0);
		fdlFailIfNotExist.top   = new FormAttachment(wMultiValuedSeparator, margin);
		fdlFailIfNotExist.right = new FormAttachment(middle, -margin);
		wlFailIfNotExist.setLayoutData(fdlFailIfNotExist);
		wFailIfNotExist=new Button(wSettings, SWT.CHECK);
		wFailIfNotExist.setToolTipText(BaseMessages.getString(PKG, "LDAPOutputDialog.FailIfNotExist.Tooltip"));
 		props.setLook(wFailIfNotExist);
		fdFailIfNotExist=new FormData();
		fdFailIfNotExist.left  = new FormAttachment(middle, 0);
		fdFailIfNotExist.top   = new FormAttachment(wMultiValuedSeparator, margin);
		fdFailIfNotExist.right = new FormAttachment(100, 0);
		wFailIfNotExist.setLayoutData(fdFailIfNotExist);
		SelectionAdapter lsSelR = new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
                input.setChanged();
            }
        };
		wFailIfNotExist.addSelectionListener(lsSelR);


		
		
		// Dn fieldname
		wlDeleteField=new Label(wSettings, SWT.RIGHT);
        wlDeleteField.setText(BaseMessages.getString(PKG, "LDAPOutputDialog.DnField.Label"));
        props.setLook(wlDeleteField);
        fdlDeleteField=new FormData();
        fdlDeleteField.left = new FormAttachment(0, 0);
        fdlDeleteField.top  = new FormAttachment(wFailIfNotExist, margin);
        fdlDeleteField.right= new FormAttachment(middle, -margin);
        wlDeleteField.setLayoutData(fdlDeleteField);
        wDeleteField=new ComboVar(transMeta, wSettings, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        wDeleteField.setEditable(true);
        props.setLook(wDeleteField);
        wDeleteField.addModifyListener(lsMod);
        fdDeleteField=new FormData();
        fdDeleteField.left = new FormAttachment(middle, 0);
        fdDeleteField.top  = new FormAttachment(wFailIfNotExist, margin);
        fdDeleteField.right= new FormAttachment(100, -margin);
        wDeleteField.setLayoutData(fdDeleteField);
        wDeleteField.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                	getPreviousFields();
                }
            }
        );

		
		fdSettings = new FormData();
		fdSettings.left = new FormAttachment(0, margin);
		fdSettings.top = new FormAttachment(wConnectionGroup, margin);
		fdSettings.right = new FormAttachment(100, -margin);
		wSettings.setLayoutData(fdSettings);
		
		// ///////////////////////////////////////////////////////////
		// / END OF Search  GROUP
		// ///////////////////////////////////////////////////////////
		
	
		fdGeneralComp=new FormData();
		fdGeneralComp.left  = new FormAttachment(0, 0);
		fdGeneralComp.top   = new FormAttachment(0, 0);
		fdGeneralComp.right = new FormAttachment(100, 0);
		fdGeneralComp.bottom= new FormAttachment(100, 0);
		wGeneralComp.setLayoutData(fdGeneralComp);
	
		wGeneralComp.layout();
		wGeneralTab.setControl(wGeneralComp);
		
		/////////////////////////////////////////////////////////////
		/// END OF GENERAL TAB
		/////////////////////////////////////////////////////////////


		//////////////////////////
		// START OF Fields TAB   ///
		//////////////////////////
		wFieldsTab=new CTabItem(wTabFolder, SWT.NONE);
		wFieldsTab.setText(BaseMessages.getString(PKG, "LDAPOutputDialog.Fields.Tab"));
		
		wFieldsComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wFieldsComp);

		FormLayout fieldsLayout = new FormLayout();
		fieldsLayout.marginWidth  = 3;
		fieldsLayout.marginHeight = 3;
		wFieldsComp.setLayout(fieldsLayout);




		// /////////////////////////////////
		// START OF Fields GROUP
		// /////////////////////////////////

		wFields = new Group(wFieldsComp, SWT.SHADOW_NONE);
		props.setLook(wFields);
		wFields.setText(BaseMessages.getString(PKG, "LDAPOutputDialog.Group.Fields.Label"));
		
		FormLayout FieldsLayout = new FormLayout();
		FieldsLayout.marginWidth = 10;
		FieldsLayout.marginHeight = 10;
		wFields.setLayout(FieldsLayout);
		
		
		// Basedn line
		wlBaseDN=new Label(wFields, SWT.RIGHT);
		wlBaseDN.setText(BaseMessages.getString(PKG, "LDAPOutputDialog.BaseDN.Label"));
 		props.setLook(wlBaseDN);
		fdlBaseDN=new FormData();
		fdlBaseDN.left = new FormAttachment(0, 0);
		fdlBaseDN.top  = new FormAttachment(wSettings, margin);
		fdlBaseDN.right= new FormAttachment(middle, -margin);
		wlBaseDN.setLayoutData(fdlBaseDN);
		wBaseDN=new TextVar(transMeta, wFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wBaseDN.setToolTipText(BaseMessages.getString(PKG, "LDAPOutputDialog.BaseDN.Tooltip"));
		props.setLook(wBaseDN);
		wBaseDN.addModifyListener(lsMod);
		fdBaseDN=new FormData();
		fdBaseDN.left = new FormAttachment(middle, 0);
		fdBaseDN.top  = new FormAttachment(wSettings, margin);
		fdBaseDN.right= new FormAttachment(100, 0);
		wBaseDN.setLayoutData(fdBaseDN);
		wBaseDN.addModifyListener(new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				input.setChanged();
				if(Const.isEmpty(wBaseDN.getText())) {
					wDoMapping.setEnabled(false);
				}else {
					setFieldsCombo();
					wDoMapping.setEnabled(true);
				}
			}
		});
		// THE UPDATE/INSERT TABLE
		wlReturn = new Label(wFields, SWT.NONE);
		wlReturn.setText(BaseMessages.getString(PKG, "LDAPOutputUpdateDialog.UpdateFields.Label")); //$NON-NLS-1$
 		props.setLook(wlReturn);
		fdlReturn = new FormData();
		fdlReturn.left = new FormAttachment(0, 0);
		fdlReturn.top = new FormAttachment(wBaseDN, margin);
		wlReturn.setLayoutData(fdlReturn);
		

		int UpInsCols = 3;
		int UpInsRows = (input.getUpdateLookup() != null ? input.getUpdateLookup().length : 1);

		ciReturn = new ColumnInfo[UpInsCols];
		ciReturn[0] = new ColumnInfo(BaseMessages.getString(PKG, "LDAPOutputUpdateDialog.ColumnInfo.TableField"),  ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
		ciReturn[1] = new ColumnInfo(BaseMessages.getString(PKG, "LDAPOutputUpdateDialog.ColumnInfo.StreamField"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
		ciReturn[2] = new ColumnInfo(BaseMessages.getString(PKG, "LDAPOutputUpdateDialog.ColumnInfo.Update"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] {"Y","N"}); //$NON-NLS-1$
		
		tableFieldColumns.add(ciReturn[0]);
		wReturn = new TableView(transMeta, wFields, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL,
				ciReturn, UpInsRows, lsMod, props);
		
		wGetLU = new Button(wFields, SWT.PUSH);
		wGetLU.setText(BaseMessages.getString(PKG, "LDAPOutputUpdateDialog.GetAndUpdateFields.Label")); //$NON-NLS-1$
		fdGetLU = new FormData();
		fdGetLU.top   = new FormAttachment(wlReturn, margin);
		fdGetLU.right = new FormAttachment(100, 0);
		wGetLU.setLayoutData(fdGetLU);
		
		
		wDoMapping = new Button(wFields, SWT.PUSH);
		wDoMapping.setText(BaseMessages.getString(PKG, "LDAPOutputUpdateDialog.EditMapping.Label")); //$NON-NLS-1$
		fdDoMapping = new FormData();
		fdDoMapping.top   = new FormAttachment(wGetLU, margin);
		fdDoMapping.right = new FormAttachment(100, 0);
		wDoMapping.setLayoutData(fdDoMapping);

		wDoMapping.addListener(SWT.Selection, new Listener() { 	public void handleEvent(Event arg0) { generateMappings();}});


		fdReturn = new FormData();
		fdReturn.left = new FormAttachment(0, 0);
		fdReturn.top = new FormAttachment(wlReturn, margin);
		fdReturn.right = new FormAttachment(wGetLU, -5*margin);
		fdReturn.bottom = new FormAttachment(100, -2*margin);
		wReturn.setLayoutData(fdReturn);
		
		
	    // 
        // Search the fields in the background
        //
        
        final Runnable runnable = new Runnable()
        {
            public void run()
            {
                StepMeta stepMeta = transMeta.findStep(stepname);
                if (stepMeta!=null)
                {
                    try
                    {
                    	RowMetaInterface row = transMeta.getPrevStepFields(stepMeta);
                        
                        // Remember these fields...
                        for (int i=0;i<row.size();i++)
                        {
                        	inputFields.put(row.getValueMeta(i).getName(), Integer.valueOf(i));
                        }
                        
                        setComboBoxes(); 
                    }
                    catch(KettleException e)
                    {
                        logError(BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"));
                    }
                }
            }
        };
        new Thread(runnable).start();
        
		
        fdFields = new FormData();
		fdFields.left = new FormAttachment(0, margin);
		fdFields.top = new FormAttachment(wSettings, margin);
		fdFields.right = new FormAttachment(100, -margin);
		fdFields.bottom = new FormAttachment(100, -margin);
		wFields.setLayoutData(fdFields);
		
		// ///////////////////////////////////////////////////////////
		// / END OF Fields  GROUP
		// ///////////////////////////////////////////////////////////
		
		
		

		fdFieldsComp=new FormData();
		fdFieldsComp.left  = new FormAttachment(0, 0);
		fdFieldsComp.top   = new FormAttachment(0, 0);
		fdFieldsComp.right = new FormAttachment(100, 0);
		fdFieldsComp.bottom= new FormAttachment(100, 0);
		wFieldsComp.setLayoutData(fdFieldsComp);
	
		wFieldsComp.layout();
		wFieldsTab.setControl(wFieldsComp);
		
		/////////////////////////////////////////////////////////////
		/// END OF Fields TAB
		/////////////////////////////////////////////////////////////

	
		
		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(wStepname, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);
		
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));

		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
		
		setButtonPositions(new Button[] { wOK, wCancel }, margin, wTabFolder);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsTest     = new Listener() { public void handleEvent(Event e) { test(); } };
		lsPreview  = new Listener() { public void handleEvent(Event e) { preview();   } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();     } };

		lsGetLU = new Listener()
		{
			public void handleEvent(Event e)
			{
				getUpdate();
			}
		};
		wOK.addListener     (SWT.Selection, lsOK     );
		wTest.addListener    (SWT.Selection, lsTest    );		
		wCancel.addListener (SWT.Selection, lsCancel );
		wGetLU.addListener(SWT.Selection, lsGetLU);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );

		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		wTabFolder.setSelection(0);

		// Set the shell size, based upon previous time...
		setSize();
		getData(input);
		useAuthentication();
		updateOperation();
		input.setChanged(changed);
	
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	private void test()
	{
		LDAPConnection connection =null;
		try {
		
			LDAPOutputMeta meta = new LDAPOutputMeta();
			getInfo(meta);
			
			// Defined a LDAP connection
			connection= new LDAPConnection(log, transMeta.environmentSubstitute(meta.getHost()), 
					Const.toInt(transMeta.environmentSubstitute(meta.getPort()), LDAPConnection.DEFAULT_PORT));
			connection.setReferral(LDAPOutputMeta.getReferralTypeCode(meta.getReferralType()));
			connection.setDerefAliases(LDAPOutputMeta.getDerefAliasesCode(meta.getDerefAliasesType()));
			// connect...
			if(wusingAuthentication.getSelection()) {
				connection.connect(transMeta.environmentSubstitute(meta.getUserName()), 
						Encr.decryptPasswordOptionallyEncrypted(transMeta.environmentSubstitute(meta.getPassword())));
			}else {
				connection.connect();
			}
			// We are successfully connected
				
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
			mb.setMessage(BaseMessages.getString(PKG, "LDAPOutputDialog.Connected.OK") +Const.CR); //$NON-NLS-1$
			mb.setText(BaseMessages.getString(PKG, "LDAPOutputDialog.Connected.Title.Ok")); //$NON-NLS-1$
			mb.open();
			
		} catch(Exception e){
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(BaseMessages.getString(PKG, "LDAPOutputDialog.Connected.NOK",e.getMessage()));
			mb.setText(BaseMessages.getString(PKG, "LDAPOutputDialog.Connected.Title.Error")); //$NON-NLS-1$
			mb.open(); 
		} finally {
			if(connection!=null) {
				// Disconnect ...
				try {connection.close();}catch(Exception e){};
			}
		}
	}


	/**
	 * Read the data from the LDAPOutputMeta object and show it in this dialog.
	 * 
	 * @param in The LDAPOutputMeta object to obtain the data from.
	 */
	public void getData(LDAPOutputMeta in)
	{
		
		wusingAuthentication.setSelection(in.UseAuthentication());

		if (in.getHost() != null)  wHost.setText(in.getHost());
		if (in.getUserName() != null)  wUserName.setText(in.getUserName());
	    if (in.getPassword() != null)  wPassword.setText(in.getPassword());
		if (in.getPort() != null)  wPort.setText(in.getPort());
	    if (in.getDnField() != null)  wDeleteField.setText(in.getDnField());
	    wFailIfNotExist.setSelection(in.isFailIfNotExist());
		wOperation.setText(LDAPOutputMeta.getOperationTypeDesc(input.getOperationType()));
		if(in.getMultiValuedSeparator()!=null)	wMultiValuedSeparator.setText(in.getMultiValuedSeparator());
		if(in.getSearchBaseDN()!=null)	wBaseDN.setText(in.getSearchBaseDN());
		
		wReferral.setText(LDAPOutputMeta.getReferralTypeDesc(input.getReferralType()));
		wDerefAliases.setText(LDAPOutputMeta.getDerefAliasesTypeDesc(input.getDerefAliasesType()));
		
		if (input.getUpdateLookup() != null)
		{
			for (int i = 0; i < input.getUpdateLookup().length; i++)
			{
				TableItem item = wReturn.table.getItem(i);
				if (input.getUpdateLookup()[i] != null)
					item.setText(1, input.getUpdateLookup()[i]);
				if (input.getUpdateStream()[i] != null)
					item.setText(2, input.getUpdateStream()[i]);
				if (input.getUpdate()[i]==null||input.getUpdate()[i].booleanValue()) {
					item.setText(3,"Y");
				} else {
					item.setText(3,"N");
				}
			}
		}

		wReturn.removeEmptyRows();
		wReturn.setRowNums();
		wReturn.optWidth(true);
		wStepname.selectAll();
	}

	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		dispose();
	}
	
	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;

		stepname = wStepname.getText();
        try
        {
            getInfo(input);
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, BaseMessages.getString(PKG, "LDAPOutputDialog.ErrorParsingData.DialogTitle"), 
            		BaseMessages.getString(PKG, "LDAPOutputDialog.ErrorParsingData.DialogMessage"), e);
        }
		dispose();
	}
	
	private void getInfo(LDAPOutputMeta in) throws KettleException
	{
		stepname = wStepname.getText(); // return value

		in.setUseAuthentication( wusingAuthentication.getSelection() );
		in.setHost( wHost.getText() );
		in.setUserName( wUserName.getText() );
		in.setPassword(wPassword.getText());
		in.setPort( wPort.getText() );
		in.setDnField(wDeleteField.getText());
		in.setFailIfNotExist(wFailIfNotExist.getSelection());
		in.setOperationType(LDAPOutputMeta.getOperationTypeByDesc(wOperation.getText()));
		in.setMultiValuedSeparator(wMultiValuedSeparator.getText());
		in.setSearchBaseDN(wBaseDN.getText());
		in.setReferralType(LDAPOutputMeta.getReferralTypeByDesc(wReferral.getText()));
		in.setDerefAliasesType(LDAPOutputMeta.getDerefAliasesTypeByDesc(wDerefAliases.getText()));
		
		int nrfields = wReturn.nrNonEmpty();

		in.allocate(nrfields);

		for (int i = 0; i < nrfields; i++)
		{
			TableItem item = wReturn.getNonEmpty(i);
			in.getUpdateLookup()[i] = item.getText(1);
			in.getUpdateStream()[i] = item.getText(2);
			in.getUpdate()[i] = Boolean.valueOf("Y".equals(item.getText(3)));
		}
	}
	
	private void useAuthentication()
	{
		wUserName.setEnabled(wusingAuthentication.getSelection());
		wlUserName.setEnabled(wusingAuthentication.getSelection());
		wPassword.setEnabled(wusingAuthentication.getSelection());
		wlPassword.setEnabled(wusingAuthentication.getSelection());
	}
	
		
	// Preview the data
	private void preview()
	{
        try
        {
            // Create the XML input step
            LDAPOutputMeta oneMeta = new LDAPOutputMeta();
            getInfo(oneMeta);
            
            TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, oneMeta, wStepname.getText());
            
            EnterNumberDialog numberDialog = new EnterNumberDialog(shell, props.getDefaultPreviewSize(), BaseMessages.getString(PKG, "LDAPOutputDialog.NumberRows.DialogTitle"), 
            		BaseMessages.getString(PKG, "LDAPOutputDialog.NumberRows.DialogMessage"));
            int previewSize = numberDialog.open();
            if (previewSize>0)
            {
                TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(shell, previewMeta, new String[] { wStepname.getText() }, new int[] { previewSize } );
                progressDialog.open();
                
                if (!progressDialog.isCancelled())
                {
                    Trans trans = progressDialog.getTrans();
                    String loggingText = progressDialog.getLoggingText();
                    
                    if (trans.getResult()!=null && trans.getResult().getNrErrors()>0)
                    {
                    	EnterTextDialog etd = new EnterTextDialog(shell, BaseMessages.getString(PKG, "System.Dialog.PreviewError.Title"),  
                    			BaseMessages.getString(PKG, "System.Dialog.PreviewError.Message"), loggingText, true );
                    	etd.setReadOnly();
                    	etd.open();
                    }
                             
                    PreviewRowsDialog prd = new PreviewRowsDialog(shell, transMeta, SWT.NONE, wStepname.getText(),
							progressDialog.getPreviewRowsMeta(wStepname.getText()), progressDialog
									.getPreviewRows(wStepname.getText()), loggingText);
					prd.open();
                    
                }
            }
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, BaseMessages.getString(PKG, "LDAPOutputDialog.ErrorPreviewingData.DialogTitle"), BaseMessages.getString(PKG, "LDAPOutputDialog.ErrorPreviewingData.DialogMessage"), e);
       }
	}
	private void getPreviousFields()
	{
		if(!gotPrevious) {
			try
			{
				RowMetaInterface r = transMeta.getPrevStepFields(stepname);
				if (r != null)
				{
	                String value = wDeleteField.getText();
	                wDeleteField.removeAll();
	                wDeleteField.setItems(r.getFieldNames());
	                if(value!=null) wDeleteField.setText(value);
				}
			}
			catch (KettleException ke)
			{
				new ErrorDialog(shell, BaseMessages.getString(PKG, "LDAPOutputDialog.FailedToGetFields.DialogTitle"), //$NON-NLS-1$
						BaseMessages.getString(PKG, "LDAPOutputDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$
			}
			gotPrevious=true;
		}
	}
	protected void setComboBoxes()
    {
        // Something was changed in the row.
        //
		final Map<String, Integer> fields = new HashMap<String, Integer>();
        
        // Add the currentMeta fields...
        fields.putAll(inputFields);
        
        Set<String> keySet = fields.keySet();
        List<String> entries = new ArrayList<String>(keySet);
        
        String[] fieldNames= (String[]) entries.toArray(new String[entries.size()]);
        Const.sortStrings(fieldNames);
        // return fields
        ciReturn[1].setComboValues(fieldNames);
    }
	private void getUpdate()
	{
		try
		{
			RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			if (r != null)
			{
                TableItemInsertListener listener = new TableItemInsertListener()
                {
                    public boolean tableItemInserted(TableItem tableItem, ValueMetaInterface v)
                    {
                        tableItem.setText(3, "Y");
                        return true;
                    }
                };
                BaseStepDialog.getFieldsFromPrevious(r, wReturn, 1, new int[] { 1, 2}, new int[] {}, -1, -1, listener);
			}
		}
		catch (KettleException ke)
		{
			new ErrorDialog(shell, BaseMessages.getString(PKG, "LDAPOutputUpdateDialog.FailedToGetFields.DialogTitle"), //$NON-NLS-1$
					BaseMessages.getString(PKG, "LDAPOutputUpdateDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$
		}
	}
	private void updateOperation()
	{
		boolean activateFields= (LDAPOutputMeta.getOperationTypeByDesc(wOperation.getText()) != LDAPOutputMeta.OPERATION_TYPE_DELETE);
		
		wlReturn.setEnabled(activateFields);
		wReturn.setEnabled(activateFields);
		wGetLU.setEnabled(activateFields);
		wBaseDN.setEnabled(activateFields);
		wlBaseDN.setEnabled(activateFields);
		wDoMapping.setEnabled(activateFields && !Const.isEmpty(wBaseDN.getText()));
		
		boolean activateMulTiValueSeparator = (LDAPOutputMeta.getOperationTypeByDesc(wOperation.getText()) != LDAPOutputMeta.OPERATION_TYPE_DELETE)
					&& (LDAPOutputMeta.getOperationTypeByDesc(wOperation.getText()) != LDAPOutputMeta.OPERATION_TYPE_UPDATE);
		wlMultiValuedSeparator.setEnabled(activateMulTiValueSeparator);
		wMultiValuedSeparator.setEnabled(activateMulTiValueSeparator);
		
		boolean activateFailIfNotExist = (LDAPOutputMeta.getOperationTypeByDesc(wOperation.getText()) != LDAPOutputMeta.OPERATION_TYPE_UPSERT)
		&& (LDAPOutputMeta.getOperationTypeByDesc(wOperation.getText()) != LDAPOutputMeta.OPERATION_TYPE_INSERT);
		wlFailIfNotExist.setEnabled(activateFailIfNotExist);
		wFailIfNotExist.setEnabled(activateFailIfNotExist);
		
	}
	public String toString()
	{
		return this.getClass().getName();
	}
	
	public RowMetaInterface getLDAPFields() throws KettleException {
		LDAPConnection connection=null;
		try {
			 // Defined a LDAP connection
			connection = new LDAPConnection(log, transMeta.environmentSubstitute(wHost.getText()), 
					Const.toInt(transMeta.environmentSubstitute(wPort.getText()), LDAPConnection.DEFAULT_PORT));
			connection.setReferral(LDAPOutputMeta.getReferralTypeCode(LDAPOutputMeta.getReferralTypeByDesc(wReferral.getText())));
			connection.setDerefAliases(LDAPOutputMeta.getDerefAliasesCode(LDAPOutputMeta.getDerefAliasesTypeByDesc(wDerefAliases.getText())));
			// connect ...
			if(wusingAuthentication.getSelection()) {
	    		String username=transMeta.environmentSubstitute(wUserName.getText());
	    		String password=Encr.decryptPasswordOptionallyEncrypted(transMeta.environmentSubstitute(wPassword.getText()));
				connection.connect(username, password);
			}else {
				connection.connect();
			}
			return connection.getFields(transMeta.environmentSubstitute(wBaseDN.getText()), null);
		}finally {
			  if(connection!=null) {
				  try {
					  connection.close();
				  }catch(Exception e){};
			  }
		  }
	}
	
	/**
	 * Reads in the fields from the previous steps and from the ONE next step and opens an 
	 * EnterMappingDialog with this information. After the user did the mapping, those information 
	 * is put into the Select/Rename table.
	 */
	private void generateMappings() {
		
		// Determine the source and target fields...
		//
		RowMetaInterface sourceFields;
		RowMetaInterface targetFields = new RowMeta();

		try {
			sourceFields = transMeta.getPrevStepFields(stepMeta);
		} catch(KettleException e) {
			new ErrorDialog(shell, BaseMessages.getString(PKG, "LDAPOutputDialog.DoMapping.UnableToFindSourceFields.Title"), 
					BaseMessages.getString(PKG, "LDAPOutputDialog.DoMapping.UnableToFindSourceFields.Message"), e);
			return;
		}
		LDAPConnection connection=null;
		try {
			  
    		// return fields
    		targetFields=getLDAPFields();
			
		  }catch(Exception e) {
				new ErrorDialog(shell, BaseMessages.getString(PKG, "LDAPOutputDialog.DoMapping.UnableToFindTargetFields.Title"), 
						BaseMessages.getString(PKG, "LDAPOutputDialog.DoMapping.UnableToFindTargetFields.Message"), e);
				return;
		  }finally {
			  if(connection!=null) {
				  try {
					  connection.close();
				  }catch(Exception e){};
			  }
		  }
		  
		String[] inputNames = new String[sourceFields.size()];
		for (int i = 0; i < sourceFields.size(); i++) {
			ValueMetaInterface value = sourceFields.getValueMeta(i);
			inputNames[i] = value.getName()+
			     EnterMappingDialog.STRING_ORIGIN_SEPARATOR+value.getOrigin()+")";
		}
		
		// Create the existing mapping list...
		//
		List<SourceToTargetMapping> mappings = new ArrayList<SourceToTargetMapping>();
		StringBuffer missingSourceFields = new StringBuffer();
		StringBuffer missingTargetFields = new StringBuffer();

		int nrFields = wReturn.nrNonEmpty();
		for (int i = 0; i < nrFields ; i++) {
			TableItem item = wReturn.getNonEmpty(i);
			String source = item.getText(2);
			String target = item.getText(1);
			
			int sourceIndex = sourceFields.indexOfValue(source); 
			if (sourceIndex<0) {
				missingSourceFields.append(Const.CR + "   " + source+" --> " + target);
			}
			int targetIndex = targetFields.indexOfValue(target);
			if (targetIndex<0) {
				missingTargetFields.append(Const.CR + "   " + source+" --> " + target);
			}
			if (sourceIndex<0 || targetIndex<0) {
				continue;
			}

			SourceToTargetMapping mapping = new SourceToTargetMapping(sourceIndex, targetIndex);
			mappings.add(mapping);
		}

		// show a confirm dialog if some missing field was found
		//
		if (missingSourceFields.length()>0 || missingTargetFields.length()>0){
			
			String message="";
			if (missingSourceFields.length()>0) {
				message+=BaseMessages.getString(PKG, "LDAPOutputDialog.DoMapping.SomeSourceFieldsNotFound", missingSourceFields.toString())+Const.CR;
			}
			if (missingTargetFields.length()>0) {
				message+=BaseMessages.getString(PKG, "LDAPOutputDialog.DoMapping.SomeTargetFieldsNotFound", missingSourceFields.toString())+Const.CR;
			}
			message+=Const.CR;
			message+=BaseMessages.getString(PKG, "LDAPOutputDialog.DoMapping.SomeFieldsNotFoundContinue")+Const.CR;
			MessageDialog.setDefaultImage(GUIResource.getInstance().getImageSpoon());
			boolean goOn = MessageDialog.openConfirm(shell, BaseMessages.getString(PKG, "LDAPOutputDialog.DoMapping.SomeFieldsNotFoundTitle"), message);
			if (!goOn) {
				return;
			}
		}
		EnterMappingDialog d = new EnterMappingDialog(LDAPOutputDialog.this.shell, sourceFields.getFieldNames(), targetFields.getFieldNames(), mappings);
		mappings = d.open();

		// mappings == null if the user pressed cancel
		//
		if (mappings!=null) {
			// Clear and re-populate!
			//
			wReturn.table.removeAll();
			wReturn.table.setItemCount(mappings.size());
			for (int i = 0; i < mappings.size(); i++) {
				SourceToTargetMapping mapping = (SourceToTargetMapping) mappings.get(i);
				TableItem item = wReturn.table.getItem(i);
				item.setText(2, sourceFields.getValueMeta(mapping.getSourcePosition()).getName());
				item.setText(1, targetFields.getValueMeta(mapping.getTargetPosition()).getName());
			}
			wReturn.setRowNums();
			wReturn.optWidth(true);
		}
	}
	public void setFieldsCombo() {
		Display display = shell.getDisplay();
		if (!(display==null || display.isDisposed())) {
			display.asyncExec(new Runnable () {
				public void run() {
					//clear
					for (int i = 0; i < tableFieldColumns.size(); i++) {
						ColumnInfo colInfo = (ColumnInfo) tableFieldColumns.get(i);
						colInfo.setComboValues(new String[] {});
					}
					if(wBaseDN.isDisposed()) return;
					String baseDn= transMeta.environmentSubstitute(wBaseDN.getText());
					if (!Const.isEmpty(baseDn)) {
						try {
							RowMetaInterface fields = getLDAPFields();
							// loop through the objects and find build the list of fields
						    String[] fieldsName = new String[fields.size()];
						    for( int i=0; i<fields.size(); i++) {
						    	fieldsName[i]=fields.getValueMeta(i).getName();
						    }

						    if(fieldsName!=null) {
								for (int i = 0; i < tableFieldColumns.size(); i++) {
									ColumnInfo colInfo = (ColumnInfo) tableFieldColumns.get(i);
									colInfo.setComboValues(fieldsName);
								}
						    }
						}catch (Exception e) {
							for (int i = 0; i < tableFieldColumns.size(); i++) {
								ColumnInfo colInfo = (ColumnInfo) tableFieldColumns	.get(i);
								colInfo.setComboValues(new String[] {});
							}
							// ignore any errors here. drop downs will not be
							// filled, but no problem for the user
						}
					}
			
				}
			});
		}
	}
}