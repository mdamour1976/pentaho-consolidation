 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 

package org.pentaho.di.ui.trans.steps.gettablenames;

import org.eclipse.swt.widgets.Group;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.gettablenames.GetTableNamesMeta;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


public class GetTableNamesDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = GetTableNamesMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	
	private CCombo       wConnection;

	private Label        wlTablenameField;
	private Text         wTablenameField;
	private FormData     fdlTablenameField, fdTablenameField;
	private Button      wincludeTable;
	private FormData	fdincludeTable;
	private Label	  wlincludeTable;
	private FormData	fdlincludeTable;
	
	private Button      wincludeProcedure;
	private FormData	fdincludeProcedure;
	private Label	  wlincludeProcedure;
	private FormData	fdlincludeProcedure;
	
	private Button      wincludeSynonym;
	private FormData	fdincludeSynonym;
	private Label	  wlincludeSynonym;
	private FormData	fdlincludeSynonym;
	
	private Button      wincludeView;
	private FormData	fdincludeView;
	private Label	  wlincludeView;
	private FormData	fdlincludeView;
	
	private Label        wlObjectTypeField;
	private Text         wObjectTypeField;
	private FormData     fdlObjectTypeField, fdObjectTypeField;
	
	private Label        wlisSystemObjectField;
	private Text         wisSystemObjectField;
	private FormData     fdlisSystemObjectField, fdisSystemObjectField;
	
	private GetTableNamesMeta input;
	
	private Group wSettings;
	private Group wOutputFields;

	public GetTableNamesDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(GetTableNamesMeta)in;
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
		shell.setText(BaseMessages.getString(PKG, "GetTableNamesDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin=Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "GetTableNamesDialog.Stepname.Label")); //$NON-NLS-1$
 		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right= new FormAttachment(middle, -margin);
		fdlStepname.top  = new FormAttachment(0, margin);
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

		// Connection line
		wConnection = addConnectionLine(shell, wStepname, middle, margin);
		if (input.getDatabase()==null && transMeta.nrDatabases()==1) wConnection.select(0);
		wConnection.addModifyListener(lsMod);
		
		
		// ///////////////////////////////
		// START OF SETTINGS GROUP  //
		///////////////////////////////// 

		wSettings = new Group(shell, SWT.SHADOW_NONE);
		props.setLook(wSettings);
		wSettings.setText(BaseMessages.getString(PKG, "GetTableNamesDialog.wSettings.Label"));
		
		FormLayout SettingsgroupLayout = new FormLayout();
		SettingsgroupLayout.marginWidth = 10;
		SettingsgroupLayout.marginHeight = 10;
		wSettings.setLayout(SettingsgroupLayout);

		//Include tables	
		wlincludeTable = new Label(wSettings, SWT.RIGHT);
		wlincludeTable.setText(BaseMessages.getString(PKG, "GetTableNamesDialog.includeTable.Label"));
		props.setLook(wlincludeTable);
		fdlincludeTable = new FormData();
		fdlincludeTable.left = new FormAttachment(0, -margin);
		fdlincludeTable.top = new FormAttachment(wSettings, margin);
		fdlincludeTable.right = new FormAttachment(middle, -2*margin);
		wlincludeTable.setLayoutData(fdlincludeTable);
		
		wincludeTable = new Button(wSettings, SWT.CHECK);
		props.setLook(wincludeTable);
		wincludeTable.setToolTipText(BaseMessages.getString(PKG, "GetTableNamesDialog.includeTable.Tooltip"));
		fdincludeTable = new FormData();
		fdincludeTable.left = new FormAttachment(middle, -margin);
		fdincludeTable.top = new FormAttachment(wConnection, margin);
		wincludeTable.setLayoutData(fdincludeTable);		
		SelectionAdapter lincludeTable = new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
            	input.setChanged();
            }
        };
        wincludeTable.addSelectionListener(lincludeTable);

		//Include views	
		wlincludeView = new Label(wSettings, SWT.RIGHT);
		wlincludeView.setText(BaseMessages.getString(PKG, "GetTableNamesDialog.includeView.Label"));
		props.setLook(wlincludeView);
		fdlincludeView = new FormData();
		fdlincludeView.left = new FormAttachment(0, -margin);
		fdlincludeView.top = new FormAttachment(wincludeTable, margin);
		fdlincludeView.right = new FormAttachment(middle, -2*margin);
		wlincludeView.setLayoutData(fdlincludeView);
		
		wincludeView = new Button(wSettings, SWT.CHECK);
		props.setLook(wincludeView);
		wincludeView.setToolTipText(BaseMessages.getString(PKG, "GetTableNamesDialog.includeView.Tooltip"));
		fdincludeView = new FormData();
		fdincludeView.left = new FormAttachment(middle, -margin);
		fdincludeView.top = new FormAttachment(wincludeTable, margin);
		wincludeView.setLayoutData(fdincludeView);		
		SelectionAdapter lincludeView = new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
            	input.setChanged();
            }
        };
        wincludeView.addSelectionListener(lincludeView);

		//Include procedures	
		wlincludeProcedure = new Label(wSettings, SWT.RIGHT);
		wlincludeProcedure.setText(BaseMessages.getString(PKG, "GetTableNamesDialog.includeProcedure.Label"));
		props.setLook(wlincludeProcedure);
		fdlincludeProcedure = new FormData();
		fdlincludeProcedure.left = new FormAttachment(0, -margin);
		fdlincludeProcedure.top = new FormAttachment(wincludeView, margin);
		fdlincludeProcedure.right = new FormAttachment(middle, -2*margin);
		wlincludeProcedure.setLayoutData(fdlincludeProcedure);
		
		wincludeProcedure = new Button(wSettings, SWT.CHECK);
		props.setLook(wincludeProcedure);
		wincludeProcedure.setToolTipText(BaseMessages.getString(PKG, "GetTableNamesDialog.includeProcedure.Tooltip"));
		fdincludeProcedure = new FormData();
		fdincludeProcedure.left = new FormAttachment(middle, -margin);
		fdincludeProcedure.top = new FormAttachment(wincludeView, margin);
		wincludeProcedure.setLayoutData(fdincludeProcedure);		
		SelectionAdapter lincludeProcedure = new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
            	input.setChanged();
            }
        };
        wincludeProcedure.addSelectionListener(lincludeProcedure);

        
		//Include Synonyms	
		wlincludeSynonym = new Label(wSettings, SWT.RIGHT);
		wlincludeSynonym.setText(BaseMessages.getString(PKG, "GetTableNamesDialog.includeSynonym.Label"));
		props.setLook(wlincludeSynonym);
		fdlincludeSynonym = new FormData();
		fdlincludeSynonym.left = new FormAttachment(0, -margin);
		fdlincludeSynonym.top = new FormAttachment(wincludeProcedure, margin);
		fdlincludeSynonym.right = new FormAttachment(middle, -2*margin);
		wlincludeSynonym.setLayoutData(fdlincludeSynonym);
		
		wincludeSynonym = new Button(wSettings, SWT.CHECK);
		props.setLook(wincludeSynonym);
		wincludeSynonym.setToolTipText(BaseMessages.getString(PKG, "GetTableNamesDialog.includeSynonym.Tooltip"));
		fdincludeSynonym = new FormData();
		fdincludeSynonym.left = new FormAttachment(middle, -margin);
		fdincludeSynonym.top = new FormAttachment(wincludeProcedure, margin);
		wincludeSynonym.setLayoutData(fdincludeSynonym);		
		SelectionAdapter lincludeSynonym = new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
            	input.setChanged();
            }
        };
        wincludeSynonym.addSelectionListener(lincludeSynonym);

        
		FormData fdSettings = new FormData();
		fdSettings.left = new FormAttachment(0, margin);
		fdSettings.top = new FormAttachment(wConnection, 2*margin);
		fdSettings.right = new FormAttachment(100, -margin);
		wSettings.setLayoutData(fdSettings);
		
		// ///////////////////////////////////////////////////////////
		// / END OF SETTINGS GROUP
		// ///////////////////////////////////////////////////////////		

		
		// ///////////////////////////////
		// START OF OutputFields GROUP  //
		///////////////////////////////// 

		wOutputFields = new Group(shell, SWT.SHADOW_NONE);
		props.setLook(wOutputFields);
		wOutputFields.setText(BaseMessages.getString(PKG, "GetTableNamesDialog.wOutputFields.Label"));
		
		FormLayout OutputFieldsgroupLayout = new FormLayout();
		OutputFieldsgroupLayout.marginWidth = 10;
		OutputFieldsgroupLayout.marginHeight = 10;
		wOutputFields.setLayout(OutputFieldsgroupLayout);
		
		// TablenameField fieldname ...
		wlTablenameField=new Label(wOutputFields, SWT.RIGHT);
		wlTablenameField.setText(BaseMessages.getString(PKG, "GetTableNamesDialog.TablenameFieldName.Label")); //$NON-NLS-1$
 		props.setLook(wlTablenameField);
		fdlTablenameField=new FormData();
		fdlTablenameField.left = new FormAttachment(0, 0);
		fdlTablenameField.right= new FormAttachment(middle, -margin);
		fdlTablenameField.top  = new FormAttachment(wSettings, margin*2);
		wlTablenameField.setLayoutData(fdlTablenameField);
		wTablenameField=new Text(wOutputFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wTablenameField.setToolTipText(BaseMessages.getString(PKG, "GetTableNamesDialog.TablenameFieldName.Tooltip"));
 		props.setLook(wTablenameField);
		wTablenameField.addModifyListener(lsMod);
		fdTablenameField=new FormData();
		fdTablenameField.left = new FormAttachment(middle, 0);
		fdTablenameField.top  = new FormAttachment(wSettings, margin*2);
		fdTablenameField.right= new FormAttachment(100, 0);
		wTablenameField.setLayoutData(fdTablenameField);
		
		// ObjectTypeField fieldname ...
		wlObjectTypeField=new Label(wOutputFields, SWT.RIGHT);
		wlObjectTypeField.setText(BaseMessages.getString(PKG, "GetTableNamesDialog.ObjectTypeFieldName.Label")); //$NON-NLS-1$
 		props.setLook(wlObjectTypeField);
		fdlObjectTypeField=new FormData();
		fdlObjectTypeField.left = new FormAttachment(0, 0);
		fdlObjectTypeField.right= new FormAttachment(middle, -margin);
		fdlObjectTypeField.top  = new FormAttachment(wTablenameField, margin);
		wlObjectTypeField.setLayoutData(fdlObjectTypeField);
		wObjectTypeField=new Text(wOutputFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wObjectTypeField.setToolTipText(BaseMessages.getString(PKG, "GetTableNamesDialog.ObjectTypeFieldName.Tooltip"));
 		props.setLook(wObjectTypeField);
		wObjectTypeField.addModifyListener(lsMod);
		fdObjectTypeField=new FormData();
		fdObjectTypeField.left = new FormAttachment(middle, 0);
		fdObjectTypeField.top  = new FormAttachment(wTablenameField, margin);
		fdObjectTypeField.right= new FormAttachment(100, 0);
		wObjectTypeField.setLayoutData(fdObjectTypeField);
		
		// isSystemObjectField fieldname ...
		wlisSystemObjectField=new Label(wOutputFields, SWT.RIGHT);
		wlisSystemObjectField.setText(BaseMessages.getString(PKG, "GetTableNamesDialog.isSystemObjectFieldName.Label")); //$NON-NLS-1$
 		props.setLook(wlisSystemObjectField);
		fdlisSystemObjectField=new FormData();
		fdlisSystemObjectField.left = new FormAttachment(0, 0);
		fdlisSystemObjectField.right= new FormAttachment(middle, -margin);
		fdlisSystemObjectField.top  = new FormAttachment(wObjectTypeField, margin);
		wlisSystemObjectField.setLayoutData(fdlisSystemObjectField);
		wisSystemObjectField=new Text(wOutputFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wisSystemObjectField.setToolTipText(BaseMessages.getString(PKG, "GetTableNamesDialog.isSystemObjectFieldName.Tooltip"));
 		props.setLook(wisSystemObjectField);
		wisSystemObjectField.addModifyListener(lsMod);
		fdisSystemObjectField=new FormData();
		fdisSystemObjectField.left = new FormAttachment(middle, 0);
		fdisSystemObjectField.top  = new FormAttachment(wObjectTypeField, margin);
		fdisSystemObjectField.right= new FormAttachment(100, 0);
		wisSystemObjectField.setLayoutData(fdisSystemObjectField);


		FormData fdOutputFields = new FormData();
		fdOutputFields.left = new FormAttachment(0, margin);
		fdOutputFields.top = new FormAttachment(wSettings, 2*margin);
		fdOutputFields.right = new FormAttachment(100, -margin);
		wOutputFields.setLayoutData(fdOutputFields);
		
		// ///////////////////////////////////////////////////////////
		// / END OF OutputFields GROUP
		// ///////////////////////////////////////////////////////////		

		

		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$

		wPreview = new Button(shell, SWT.PUSH);
		wPreview.setText(BaseMessages.getString(PKG, "GetTableNamesDialog.Preview.Button"));

		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wPreview, wCancel }, margin, wOutputFields);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();        } };

		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();    } };
		lsPreview = new Listener() { public void handleEvent(Event e) {preview();}};
		wOK.addListener    (SWT.Selection, lsOK    );
		wPreview.addListener(SWT.Selection, lsPreview);
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
		input.setChanged(changed);

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		if(log.isDebug()) log.logDebug(toString(), BaseMessages.getString(PKG, "GetTableNamesDialog.Log.GettingKeyInfo")); //$NON-NLS-1$
		
		if (input.getDatabase()!=null)   wConnection.setText(input.getDatabase().getName());
		else if (transMeta.nrDatabases()==1)
		{
			wConnection.setText( transMeta.getDatabase(0).getName() );
		}
		if (input.getTablenameFieldName()!=null)   wTablenameField.setText(input.getTablenameFieldName());
		if (input.getObjectTypeFieldName()!=null)   wObjectTypeField.setText(input.getObjectTypeFieldName());
		if (input.isSystemObjectFieldName()!=null)   wisSystemObjectField.setText(input.isSystemObjectFieldName());
		
		wincludeTable.setSelection(input.isIncludeTable());
		wincludeView.setSelection(input.isIncludeView());
		wincludeProcedure.setSelection(input.isIncludeProcedure());
		wincludeSynonym.setSelection(input.isIncludeSyonym());
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
		stepname = wStepname.getText(); // return value	
		getInfo(input);
		if (input.getDatabase()==null)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(BaseMessages.getString(PKG, "GetTableNamesDialog.InvalidConnection.DialogMessage")); //$NON-NLS-1$
			mb.setText(BaseMessages.getString(PKG, "GetTableNamesDialog.InvalidConnection.DialogTitle")); //$NON-NLS-1$
			mb.open();
			return;
		}
		dispose();
	}
	private void getInfo(GetTableNamesMeta info)
	{
		info.setDatabase( transMeta.findDatabase(wConnection.getText()) );
		info.setTablenameFieldName(wTablenameField.getText() );
		info.setObjectTypeFieldName(wObjectTypeField.getText() );
		info.setIsSystemObjectFieldName(wisSystemObjectField.getText() );
		info.setIncludeTable(wincludeTable.getSelection());
		info.setIncludeView(wincludeView.getSelection());
		info.setIncludeProcedure(wincludeProcedure.getSelection());
		info.setIncludeSyonym(wincludeSynonym.getSelection());
	}
	private boolean checkUserInput(GetTableNamesMeta meta)
	{

		if(Const.isEmpty(meta.getTablenameFieldName()))
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(BaseMessages.getString(PKG, "GetTableNamesDialog.Error.TablenameFieldNameMissingMessage")); //$NON-NLS-1$
			mb.setText(BaseMessages.getString(PKG, "GetTableNamesDialog.Error.TablenameFieldNameMissingTitle")); //$NON-NLS-1$
			mb.open();
			
			return false;
		}
		return true;
	}
	// Preview the data
	private void preview()
	{
		GetTableNamesMeta oneMeta = new GetTableNamesMeta();
		
		getInfo(oneMeta);
		if (oneMeta.getDatabase()==null)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(BaseMessages.getString(PKG, "GetTableNamesDialog.InvalidConnection.DialogMessage")); //$NON-NLS-1$
			mb.setText(BaseMessages.getString(PKG, "GetTableNamesDialog.InvalidConnection.DialogTitle")); //$NON-NLS-1$
			mb.open();
			return;
		}
		if(!checkUserInput(oneMeta)) return;
		
		TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, oneMeta, wStepname.getText());
	    
		EnterNumberDialog numberDialog = new EnterNumberDialog(shell, props.getDefaultPreviewSize(), BaseMessages.getString(PKG, "GetTableNamesDialog.PreviewSize.DialogTitle"), BaseMessages.getString(PKG, "GetTableNamesDialog.PreviewSize.DialogMessage"));
		int previewSize = numberDialog.open();
		if (previewSize > 0)
		{
			TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(shell, previewMeta,
					new String[] { wStepname.getText() }, new int[] { previewSize });
			progressDialog.open();

			if (!progressDialog.isCancelled())
			{
				Trans trans = progressDialog.getTrans();
				String loggingText = progressDialog.getLoggingText();

				if (trans.getResult() != null && trans.getResult().getNrErrors() > 0)
				{
					EnterTextDialog etd = new EnterTextDialog(shell, BaseMessages.getString(PKG, "System.Dialog.Error.Title"), BaseMessages.getString(PKG, "GetTableNamesDialog.ErrorInPreview.DialogMessage"), loggingText, true);
					etd.setReadOnly();
					etd.open();
				}

				PreviewRowsDialog prd = new PreviewRowsDialog(shell, transMeta, SWT.NONE, wStepname.getText(),progressDialog.getPreviewRowsMeta(wStepname.getText()),
						progressDialog.getPreviewRows(wStepname.getText()), loggingText);
				prd.open();
			}
		}
	}

	public String toString()
	{
		return this.getClass().getName();
	}
}
