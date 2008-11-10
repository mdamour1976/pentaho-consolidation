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
 * Created on 18-mei-2003
 *
 */

package org.pentaho.di.ui.trans.steps.mail;

import java.nio.charset.Charset;
import java.util.ArrayList;

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
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.mail.Messages;
import org.pentaho.di.trans.steps.mail.MailMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.LabelText;
import org.pentaho.di.ui.core.widget.LabelTextVar;

/**
 * Send mail step.
 * based on Mail job entry
 * @author Samatar
 * @since 28-07-2008
 */

public class MailDialog extends BaseStepDialog implements StepDialogInterface
{
      
	private static final String[] FILETYPES = new String[] {
           Messages.getString("MailDialog.Filetype.All") };

	   
    private boolean  gotEncodings = false;
    
	private Group wOriginFiles,wZipGroup;
	
	private FormData fdOriginFiles,fdZipGroup,fdFilenameField,fdlFilenameField;
    private Button wisFileDynamic;
    
    private Label wlisFileDynamic,wlDynamicFilenameField;
    private CCombo wDynamicFilenameField;
    private FormData fdlisFileDynamic,fdisFileDynamic;
    
    private Label wlDynamicWildcardField;
    private CCombo wDynamicWildcardField;
    private FormData fdlDynamicWildcardField,fdDynamicWildcardField;
    
	private CTabFolder   wTabFolder;
	private Composite    wGeneralComp,wContentComp,wAttachedComp,wMessageComp;	
	private CTabItem     wGeneralTab,wContentTab,wAttachedTab,wMessageTab;
	private FormData	 fdGeneralComp,fdContentComp,fdAttachedComp,fdMessageComp;
	private FormData     fdTabFolder;
	
	private Label wlisZipFileDynamic ;
	
	
	private Group wDestinationGroup,wReplyGroup,wServerGroup,wAuthentificationGroup,wMessageSettingsGroup,
			wMessageGroup;
	private FormData fdDestinationGroup,fdReplyGroup,fdServerGroup,fdAuthentificationGroup,
		fdMessageSettingsGroup,fdMessageGroup;
	
    private LabelText wName;

    private FormData fdlDestination,fdlDestinationBCc;

    private CCombo wDestination;
    
    private Label wlDestination;

	private CCombo wDestinationCc,wDestinationBCc;

	private Label wlDestinationCc,wlDestinationBCc;
	
	private FormData fdlDestinationCc;

    private FormData fdDestination;

	private FormData fdDestinationCc;

	private FormData fdDestinationBCc;

    private CCombo wServer;

    private Label wlServer;
    
    private FormData fdlServer;
    
    private FormData fdServer;

    private CCombo wPort;
    
    private Label wlPort;
    
    private FormData fdlPort;

    private FormData fdPort;

    private Label wlUseAuth;

    private Button wUseAuth;

    private FormData fdlUseAuth, fdUseAuth;

    private Label wlUseSecAuth;

    private Button wUseSecAuth;

    private FormData fdlUseSecAuth, fdUseSecAuth;

    private CCombo wAuthUser;

    private Label wlAuthUser;
    
    private FormData fdAuthUser;

    private CCombo wAuthPass;
    
    private Label wlAuthPass;
    
    private FormData fdlAuthPass;

    private FormData fdAuthPass;

    private CCombo wReply,wReplyName;

    private FormData fdReply,fdReplyName;

    private CCombo wSubject;
    
    private Label wlSubject;
    
    private FormData fdlSubject;

    private FormData fdSubject;

    private Label wlAddDate;

    private Button wAddDate;

    private FormData fdlAddDate, fdAddDate;
    
    private Label wlReply,wlReplyName;
    
    private FormData fdlReply,fdlReplyName;
    
    private FormData fdlAuthUser;

    private CCombo wPerson;
    
	private Label        wlWildcard;
	
	private TextVar wWildcard;
	
	private FormData  fdlWildcard; 
	
	private FormData fdWildcard;
    
    private Label wlPerson,wlPhone;
    
    private FormData fdlPerson,fdlPhone;

    private FormData fdPerson;

    private CCombo wPhone;

    private FormData fdPhone;

    private CCombo wComment;
    
    private Label wlComment;
    
	private Label        wlSourceFileFoldername;
	private Button       wbFileFoldername,wbSourceFolder;
	private TextVar      wSourceFileFoldername;
	private FormData     fdlSourceFileFoldername, fdbSourceFileFoldername,fdSourceFileFoldername,fdbSourceFolder;

	private Label        wlincludeSubFolders;
	private Button       wincludeSubFolders;
	private FormData     fdlincludeSubFolders,fdincludeSubFolders;
	
    private FormData fdlComment, fdComment;

    private Label wlOnlyComment, wlUseHTML, wlUsePriority;

    private Button wOnlyComment, wUseHTML,wUsePriority;

    private FormData fdlOnlyComment, fdOnlyComment, fdlUseHTML, fdUseHTML, fdUsePriority;
    
    private Label        wlEncoding;
    private CCombo       wEncoding;
    private FormData     fdlEncoding, fdEncoding;
    
    private Label        wlSecureConnectionType;
    private CCombo       wSecureConnectionType;
    private FormData     fdlSecureConnectionType, fdSecureConnectionType;
    
    private Label        wlPriority;
    private CCombo       wPriority;
    private FormData     fdlPriority, fdPriority;
    
    
    private Label        wlImportance;
    private CCombo       wImportance;
    private FormData     fdlImportance, fdImportance;
    
    private Label wlZipFiles;
    
    private FormData fdlisZipFileDynamic;
    
    private Label wlDynamicZipFileField;
    
    private CCombo wDynamicZipFileField;
    
    private FormData fdlDynamicZipFileField;
    
    private FormData fdDynamicZipFileField;
    
    private FormData fdisZipFileDynamic;
    
    private Button wisZipFileDynamic;

    private Button wZipFiles;

    private FormData fdlZipFiles, fdZipFiles;

    private LabelTextVar wZipFilename;
    
    private LabelTextVar wZipSizeCondition;

    private FormData fdZipFilename;
    
    private FormData fdZipSizeCondition;
	
	private boolean 	getpreviousFields=false;
	
	private MailMeta input;

	public MailDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(MailMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
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
		shell.setText(Messages.getString("MailDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("MailDialog.Stepname.Label")); //$NON-NLS-1$
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
		
		 
        wTabFolder = new CTabFolder(shell, SWT.BORDER);
 		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);
 		
 		//////////////////////////
		// START OF GENERAL TAB   ///
		//////////////////////////
		
		wGeneralTab=new CTabItem(wTabFolder, SWT.NONE);
		wGeneralTab.setText(Messages.getString("Mail.Tab.General.Label"));
		
		wGeneralComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wGeneralComp);

		FormLayout generalLayout = new FormLayout();
		generalLayout.marginWidth  = 3;
		generalLayout.marginHeight = 3;
		wGeneralComp.setLayout(generalLayout);
		
		
		// ////////////////////////
		// START OF Destination Settings GROUP
		// ////////////////////////

		wDestinationGroup = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(wDestinationGroup);
		wDestinationGroup.setText(Messages.getString("Mail.Group.DestinationAddress.Label"));
		
		FormLayout destinationgroupLayout = new FormLayout();
		destinationgroupLayout.marginWidth = 10;
		destinationgroupLayout.marginHeight = 10;
		wDestinationGroup.setLayout(destinationgroupLayout);
        

		// Destination
		wlDestination=new Label(wDestinationGroup, SWT.RIGHT);
        wlDestination.setText(Messages.getString("Mail.DestinationAddress.Label"));
        props.setLook(wlDestination);
        fdlDestination=new FormData();
        fdlDestination.left = new FormAttachment(0, -margin);
        fdlDestination.top  = new FormAttachment(wStepname, margin);
        fdlDestination.right= new FormAttachment(middle, -2*margin);
        wlDestination.setLayoutData(fdlDestination);
        
        wDestination=new CCombo(wDestinationGroup, SWT.BORDER | SWT.READ_ONLY);
        wDestination.setEditable(true);
        props.setLook(wDestination);
        wDestination.addModifyListener(lsMod);
        fdDestination=new FormData();
        fdDestination.left = new FormAttachment(middle, -margin);
        fdDestination.top  = new FormAttachment(wStepname, margin);
        fdDestination.right= new FormAttachment(100, -margin);
        wDestination.setLayoutData(fdDestination);         
        wDestination.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                    shell.setCursor(busy);
                    getPreviousFields();
                    shell.setCursor(null);
                    busy.dispose();
                }
            }
        );  


		// DestinationCcCc
		wlDestinationCc=new Label(wDestinationGroup, SWT.RIGHT);
        wlDestinationCc.setText(Messages.getString("Mail.DestinationAddressCc.Label"));
        props.setLook(wlDestinationCc);
        fdlDestinationCc=new FormData();
        fdlDestinationCc.left = new FormAttachment(0, -margin);
        fdlDestinationCc.top  = new FormAttachment(wDestination, margin);
        fdlDestinationCc.right= new FormAttachment(middle, -2*margin);
        wlDestinationCc.setLayoutData(fdlDestinationCc);
        
        wDestinationCc=new CCombo(wDestinationGroup, SWT.BORDER | SWT.READ_ONLY);
        wDestinationCc.setEditable(true);
        props.setLook(wDestinationCc);
        wDestinationCc.addModifyListener(lsMod);
        fdDestinationCc=new FormData();
        fdDestinationCc.left = new FormAttachment(middle, -margin);
        fdDestinationCc.top  = new FormAttachment(wDestination, margin);
        fdDestinationCc.right= new FormAttachment(100, -margin);
        wDestinationCc.setLayoutData(fdDestinationCc);         
        wDestinationCc.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                    shell.setCursor(busy);
                    getPreviousFields();
                    shell.setCursor(null);
                    busy.dispose();
                }
            }
        );  
		// DestinationBCc
		wlDestinationBCc=new Label(wDestinationGroup, SWT.RIGHT);
        wlDestinationBCc.setText(Messages.getString("Mail.DestinationAddressBCc.Label"));
        props.setLook(wlDestinationBCc);
        fdlDestinationBCc=new FormData();
        fdlDestinationBCc.left = new FormAttachment(0, -margin);
        fdlDestinationBCc.top  = new FormAttachment(wDestinationCc, margin);
        fdlDestinationBCc.right= new FormAttachment(middle, -2*margin);
        wlDestinationBCc.setLayoutData(fdlDestinationBCc);
        
        wDestinationBCc=new CCombo(wDestinationGroup, SWT.BORDER | SWT.READ_ONLY);
        wDestinationBCc.setEditable(true);
        props.setLook(wDestinationBCc);
        wDestinationBCc.addModifyListener(lsMod);
        fdDestinationBCc=new FormData();
        fdDestinationBCc.left = new FormAttachment(middle, -margin);
        fdDestinationBCc.top  = new FormAttachment(wDestinationCc, margin);
        fdDestinationBCc.right= new FormAttachment(100, -margin);
        wDestinationBCc.setLayoutData(fdDestinationBCc);         
        wDestinationBCc.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                    shell.setCursor(busy);
                    getPreviousFields();
                    shell.setCursor(null);
                    busy.dispose();
                }
            }
        );  


    	fdDestinationGroup = new FormData();
    	fdDestinationGroup.left = new FormAttachment(0, margin);
    	fdDestinationGroup.top = new FormAttachment(wName, margin);
    	fdDestinationGroup.right = new FormAttachment(100, -margin);
		wDestinationGroup.setLayoutData(fdDestinationGroup);
		
		// ///////////////////////////////////////////////////////////
		// / END OF DESTINATION ADDRESS  GROUP
		// ///////////////////////////////////////////////////////////
		
		// ////////////////////////
		// START OF Reply Settings GROUP
		// ////////////////////////

		wReplyGroup = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(wReplyGroup);
		wReplyGroup.setText(Messages.getString("MailDialog.Group.Reply.Label"));
		
		FormLayout replygroupLayout = new FormLayout();
		replygroupLayout.marginWidth = 10;
		replygroupLayout.marginHeight = 10;
		wReplyGroup.setLayout(replygroupLayout);
		
		// ReplyName
		wlReplyName=new Label(wReplyGroup, SWT.RIGHT);
        wlReplyName.setText(Messages.getString("Mail.ReplyName.Label"));
        props.setLook(wlReplyName);
        fdlReplyName=new FormData();
        fdlReplyName.left = new FormAttachment(0, -margin);
        fdlReplyName.top  = new FormAttachment(wDestinationGroup, margin);
        fdlReplyName.right= new FormAttachment(middle, -2*margin);
        wlReplyName.setLayoutData(fdlReplyName);
        
        wReplyName=new CCombo(wReplyGroup, SWT.BORDER | SWT.READ_ONLY);
        wReplyName.setEditable(true);
        props.setLook(wReplyName);
        wReplyName.addModifyListener(lsMod);
        fdReplyName=new FormData();
        fdReplyName.left = new FormAttachment(middle, -margin);
        fdReplyName.top  = new FormAttachment(wDestinationGroup, margin);
        fdReplyName.right= new FormAttachment(100, -margin);
        wReplyName.setLayoutData(fdReplyName);         
        wReplyName.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                    shell.setCursor(busy);
                    getPreviousFields();
                    shell.setCursor(null);
                    busy.dispose();
                }
            }
        );  
		
		// Reply
		wlReply=new Label(wReplyGroup, SWT.RIGHT);
        wlReply.setText(Messages.getString("Mail.ReplyAddress.Label"));
        props.setLook(wlReply);
        fdlReply=new FormData();
        fdlReply.left = new FormAttachment(0, -margin);
        fdlReply.top  = new FormAttachment(wReplyName, margin);
        fdlReply.right= new FormAttachment(middle, -2*margin);
        wlReply.setLayoutData(fdlReply);
        
        wReply=new CCombo(wReplyGroup, SWT.BORDER | SWT.READ_ONLY);
        wReply.setEditable(true);
        props.setLook(wReply);
        wReply.addModifyListener(lsMod);
        fdReply=new FormData();
        fdReply.left = new FormAttachment(middle, -margin);
        fdReply.top  = new FormAttachment(wReplyName, margin);
        fdReply.right= new FormAttachment(100, -margin);
        wReply.setLayoutData(fdReply);         
        wReply.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                    shell.setCursor(busy);
                    getPreviousFields();
                    shell.setCursor(null);
                    busy.dispose();
                }
            }
        );  
    	fdReplyGroup = new FormData();
    	fdReplyGroup.left = new FormAttachment(0, margin);
    	fdReplyGroup.top = new FormAttachment(wDestinationGroup, margin);
    	fdReplyGroup.right = new FormAttachment(100, -margin);
		wReplyGroup.setLayoutData(fdReplyGroup);
		
		// ///////////////////////////////////////////////////////////
		// / END OF Reply  GROUP
		// ///////////////////////////////////////////////////////////


		// Person
		wlPerson=new Label(wGeneralComp, SWT.RIGHT);
        wlPerson.setText(Messages.getString("Mail.Contact.Label"));
        props.setLook(wlPerson);
        fdlPerson=new FormData();
        fdlPerson.left = new FormAttachment(0, -margin);
        fdlPerson.top  = new FormAttachment(wReplyGroup, 2*margin);
        fdlPerson.right= new FormAttachment(middle, -2*margin);
        wlPerson.setLayoutData(fdlPerson);
        
        wPerson=new CCombo(wGeneralComp, SWT.BORDER | SWT.READ_ONLY);
        wPerson.setEditable(true);
        props.setLook(wPerson);
        wPerson.addModifyListener(lsMod);
        fdPerson=new FormData();
        fdPerson.left = new FormAttachment(middle, -margin);
        fdPerson.top  = new FormAttachment(wReplyGroup, 2*margin);
        fdPerson.right= new FormAttachment(100, -margin);
        wPerson.setLayoutData(fdPerson);         
        wPerson.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                    shell.setCursor(busy);
                    getPreviousFields();
                    shell.setCursor(null);
                    busy.dispose();
                }
            }
        );  
        
        // Phone line
		wlPhone=new Label(wGeneralComp, SWT.RIGHT);
        wlPhone.setText(Messages.getString("Mail.ContactPhone.Label"));
        props.setLook(wlPhone);
        fdlPhone=new FormData();
        fdlPhone.left = new FormAttachment(0, -margin);
        fdlPhone.top  = new FormAttachment(wPerson, margin);
        fdlPhone.right= new FormAttachment(middle, -2*margin);
        wlPhone.setLayoutData(fdlPhone);
        
        wPhone=new CCombo(wGeneralComp, SWT.BORDER | SWT.READ_ONLY);
        wPhone.setEditable(true);
        props.setLook(wPhone);
        wPhone.addModifyListener(lsMod);
        fdPhone=new FormData();
        fdPhone.left = new FormAttachment(middle, -margin);
        fdPhone.top  = new FormAttachment(wPerson, margin);
        fdPhone.right= new FormAttachment(100, -margin);
        wPhone.setLayoutData(fdPhone);         
        wPhone.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                    shell.setCursor(busy);
                    getPreviousFields();
                    shell.setCursor(null);
                    busy.dispose();
                }
            }
        );  
        

		fdGeneralComp=new FormData();
		fdGeneralComp.left  = new FormAttachment(0, 0);
		fdGeneralComp.top   = new FormAttachment(0, 0);
		fdGeneralComp.right = new FormAttachment(100, 0);
		fdGeneralComp.bottom= new FormAttachment(500, -margin);
		wGeneralComp.setLayoutData(fdGeneralComp);
		
		wGeneralComp.layout();
		wGeneralTab.setControl(wGeneralComp);
 		props.setLook(wGeneralComp);
 		
 		
 		
		/////////////////////////////////////////////////////////////
		/// END OF GENERAL TAB
		/////////////////////////////////////////////////////////////
        
 		//////////////////////////////////////
		// START OF SERVER TAB   ///
		/////////////////////////////////////
		
		
		
		wContentTab=new CTabItem(wTabFolder, SWT.NONE);
		wContentTab.setText(Messages.getString("MailDialog.Server.Label"));

		FormLayout contentLayout = new FormLayout ();
		contentLayout.marginWidth  = 3;
		contentLayout.marginHeight = 3;
		
		wContentComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wContentComp);
 		wContentComp.setLayout(contentLayout);
 		
		// ////////////////////////
		// START OF SERVER  GROUP
		/////////////////////////// 

		wServerGroup = new Group(wContentComp, SWT.SHADOW_NONE);
		props.setLook(wServerGroup);
		wServerGroup.setText(Messages.getString("Mail.Group.SMTPServer.Label"));
		
		FormLayout servergroupLayout = new FormLayout();
		servergroupLayout.marginWidth = 10;
		servergroupLayout.marginHeight = 10;
		wServerGroup.setLayout(servergroupLayout);
        
 		
		// Server
		wlServer=new Label(wServerGroup, SWT.RIGHT);
        wlServer.setText(Messages.getString("Mail.SMTPServer.Label"));
        props.setLook(wlServer);
        fdlServer=new FormData();
        fdlServer.left = new FormAttachment(0, -margin);
        fdlServer.top  = new FormAttachment(0, margin);
        fdlServer.right= new FormAttachment(middle, -2*margin);
        wlServer.setLayoutData(fdlServer);
        
        wServer=new CCombo(wServerGroup, SWT.BORDER | SWT.READ_ONLY);
        wServer.setEditable(true);
        props.setLook(wServer);
        wServer.addModifyListener(lsMod);
        fdServer=new FormData();
        fdServer.left = new FormAttachment(middle, -margin);
        fdServer.top  = new FormAttachment(0, margin);
        fdServer.right= new FormAttachment(100, -margin);
        wServer.setLayoutData(fdServer);         
        wServer.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                    shell.setCursor(busy);
                    getPreviousFields();
                    shell.setCursor(null);
                    busy.dispose();
                }
            }
        );  


		// Port
		wlPort=new Label(wServerGroup, SWT.RIGHT);
        wlPort.setText(Messages.getString("Mail.Port.Label"));
        props.setLook(wlPort);
        fdlPort=new FormData();
        fdlPort.left = new FormAttachment(0, -margin);
        fdlPort.top  = new FormAttachment(wServer, margin);
        fdlPort.right= new FormAttachment(middle, -2*margin);
        wlPort.setLayoutData(fdlPort);
        
        wPort=new CCombo(wServerGroup, SWT.BORDER | SWT.READ_ONLY);
        wPort.setEditable(true);
        props.setLook(wPort);
        wPort.addModifyListener(lsMod);
        fdPort=new FormData();
        fdPort.left = new FormAttachment(middle, -margin);
        fdPort.top  = new FormAttachment(wServer, margin);
        fdPort.right= new FormAttachment(100, -margin);
        wPort.setLayoutData(fdPort);         
        wPort.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                    shell.setCursor(busy);
                    getPreviousFields();
                    shell.setCursor(null);
                    busy.dispose();
                }
            }
        );  
        
    	fdServerGroup = new FormData();
    	fdServerGroup.left = new FormAttachment(0, margin);
    	fdServerGroup.top = new FormAttachment(wName, margin);
    	fdServerGroup.right = new FormAttachment(100, -margin);
		wServerGroup.setLayoutData(fdServerGroup);
		
		// //////////////////////////////////////
		// / END OF SERVER ADDRESS  GROUP
		// ///////////////////////////////////////

		// ////////////////////////////////////
		// START OF AUTHENTIFICATION  GROUP
		////////////////////////////////////// 

		wAuthentificationGroup = new Group(wContentComp, SWT.SHADOW_NONE);
		props.setLook(wAuthentificationGroup);
		wAuthentificationGroup.setText(Messages.getString("Mail.Group.Authentification.Label"));
		
		FormLayout authentificationgroupLayout = new FormLayout();
		authentificationgroupLayout.marginWidth = 10;
		authentificationgroupLayout.marginHeight = 10;
		wAuthentificationGroup.setLayout(authentificationgroupLayout);
		
		
        // Authentication?
        wlUseAuth = new Label(wAuthentificationGroup, SWT.RIGHT);
        wlUseAuth.setText(Messages.getString("Mail.UseAuthentication.Label"));
        props.setLook(wlUseAuth);
        fdlUseAuth = new FormData();
        fdlUseAuth.left = new FormAttachment(0, 0);
        fdlUseAuth.top = new FormAttachment(wServerGroup, margin);
        fdlUseAuth.right = new FormAttachment(middle, -2*margin);
        wlUseAuth.setLayoutData(fdlUseAuth);
        wUseAuth = new Button(wAuthentificationGroup, SWT.CHECK);
        props.setLook(wUseAuth);
        fdUseAuth = new FormData();
        fdUseAuth.left = new FormAttachment(middle,-margin);
        fdUseAuth.top = new FormAttachment(wServerGroup, margin);
        fdUseAuth.right = new FormAttachment(100, 0);
        wUseAuth.setLayoutData(fdUseAuth);
        wUseAuth.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
            	setUseAuth();
                input.setChanged();
            }
        });

        // AuthUser line
		wlAuthUser=new Label(wAuthentificationGroup, SWT.RIGHT);
        wlAuthUser.setText(Messages.getString("Mail.AuthenticationUser.Label"));
        props.setLook(wlAuthUser);
        fdlAuthUser=new FormData();
        fdlAuthUser.left = new FormAttachment(0, -margin);
        fdlAuthUser.top  = new FormAttachment(wUseAuth, margin);
        fdlAuthUser.right= new FormAttachment(middle, -2*margin);
        wlAuthUser.setLayoutData(fdlAuthUser);
        
        wAuthUser=new CCombo(wAuthentificationGroup, SWT.BORDER | SWT.READ_ONLY);
        wAuthUser.setEditable(true);
        props.setLook(wAuthUser);
        wAuthUser.addModifyListener(lsMod);
        fdAuthUser=new FormData();
        fdAuthUser.left = new FormAttachment(middle, -margin);
        fdAuthUser.top  = new FormAttachment(wUseAuth, margin);
        fdAuthUser.right= new FormAttachment(100, -margin);
        wAuthUser.setLayoutData(fdAuthUser);         
        wAuthUser.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                    shell.setCursor(busy);
                    getPreviousFields();
                    shell.setCursor(null);
                    busy.dispose();
                }
            }
        );  

        // AuthPass line
		wlAuthPass=new Label(wAuthentificationGroup, SWT.RIGHT);
        wlAuthPass.setText(Messages.getString("Mail.AuthenticationPassword.Label"));
        props.setLook(wlAuthPass);
        fdlAuthPass=new FormData();
        fdlAuthPass.left = new FormAttachment(0, -margin);
        fdlAuthPass.top  = new FormAttachment(wAuthUser, margin);
        fdlAuthPass.right= new FormAttachment(middle, -2*margin);
        wlAuthPass.setLayoutData(fdlAuthPass);
        
        wAuthPass=new CCombo(wAuthentificationGroup, SWT.BORDER | SWT.READ_ONLY);
        wAuthPass.setEditable(true);
        props.setLook(wAuthPass);
        wAuthPass.addModifyListener(lsMod);
        fdAuthPass=new FormData();
        fdAuthPass.left = new FormAttachment(middle, -margin);
        fdAuthPass.top  = new FormAttachment(wAuthUser, margin);
        fdAuthPass.right= new FormAttachment(100, -margin);
        wAuthPass.setLayoutData(fdAuthPass);         
        wAuthPass.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                    shell.setCursor(busy);
                    getPreviousFields();
                    shell.setCursor(null);
                    busy.dispose();
                }
            }
        );  

        // Use secure authentication?
        wlUseSecAuth = new Label(wAuthentificationGroup, SWT.RIGHT);
        wlUseSecAuth.setText(Messages.getString("Mail.UseSecAuthentication.Label"));
        props.setLook(wlUseSecAuth);
        fdlUseSecAuth = new FormData();
        fdlUseSecAuth.left = new FormAttachment(0, 0);
        fdlUseSecAuth.top = new FormAttachment(wAuthPass, margin);
        fdlUseSecAuth.right = new FormAttachment(middle, -2*margin);
        wlUseSecAuth.setLayoutData(fdlUseSecAuth);
        wUseSecAuth = new Button(wAuthentificationGroup, SWT.CHECK);
        props.setLook(wUseSecAuth);
        fdUseSecAuth = new FormData();
        fdUseSecAuth.left = new FormAttachment(middle, -margin);
        fdUseSecAuth.top = new FormAttachment(wAuthPass, margin);
        fdUseSecAuth.right = new FormAttachment(100, 0);
        wUseSecAuth.setLayoutData(fdUseSecAuth);
        wUseSecAuth.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
            	setSecureConnectiontype();
               input.setChanged();
                
            }
        });
        
        // SecureConnectionType
        wlSecureConnectionType=new Label(wAuthentificationGroup, SWT.RIGHT);
        wlSecureConnectionType.setText(Messages.getString("Mail.SecureConnectionType.Label"));
        props.setLook(wlSecureConnectionType);
        fdlSecureConnectionType=new FormData();
        fdlSecureConnectionType.left = new FormAttachment(0, 0);
        fdlSecureConnectionType.top  = new FormAttachment(wUseSecAuth, margin);
        fdlSecureConnectionType.right= new FormAttachment(middle, -2*margin);
        wlSecureConnectionType.setLayoutData(fdlSecureConnectionType);
        wSecureConnectionType=new CCombo(wAuthentificationGroup, SWT.BORDER | SWT.READ_ONLY);
        wSecureConnectionType.setEditable(true);
        props.setLook(wSecureConnectionType);
        wSecureConnectionType.addModifyListener(lsMod);
        fdSecureConnectionType=new FormData();
        fdSecureConnectionType.left = new FormAttachment(middle, -margin);
        fdSecureConnectionType.top  = new FormAttachment(wUseSecAuth,margin);
        fdSecureConnectionType.right= new FormAttachment(100, 0);
        wSecureConnectionType.setLayoutData(fdSecureConnectionType);
        wSecureConnectionType.add("SSL");
        wSecureConnectionType.add("TLS");
        wSecureConnectionType.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
            	setSecureConnectiontype();
               input.setChanged();
                
            }
        });

        
 		
      	fdAuthentificationGroup = new FormData();
      	fdAuthentificationGroup.left = new FormAttachment(0, margin);
      	fdAuthentificationGroup.top = new FormAttachment(wServerGroup, margin);
      	fdAuthentificationGroup.right = new FormAttachment(100, -margin);
      	fdAuthentificationGroup.bottom = new FormAttachment(100, -margin);
      	wAuthentificationGroup.setLayoutData(fdAuthentificationGroup);
		
		// //////////////////////////////////////
		// / END OF AUTHENTIFICATION GROUP
		// ///////////////////////////////////////
 		
		fdContentComp = new FormData();
 		fdContentComp.left  = new FormAttachment(0, 0);
 		fdContentComp.top   = new FormAttachment(0, 0);
 		fdContentComp.right = new FormAttachment(100, 0);
 		fdContentComp.bottom= new FormAttachment(100, 0);
 		wContentComp.setLayoutData(wContentComp);

		wContentComp.layout();
		wContentTab.setControl(wContentComp);


		/////////////////////////////////////////////////////////////
		/// END OF SERVER TAB
		/////////////////////////////////////////////////////////////
 		
		//////////////////////////////////////
		// START OF MESSAGE          TAB   ///
		/////////////////////////////////////
		
		
		
		wMessageTab=new CTabItem(wTabFolder, SWT.NONE);
		wMessageTab.setText(Messages.getString("Mail.Tab.Message.Label"));

		FormLayout messageLayout = new FormLayout ();
		messageLayout.marginWidth  = 3;
		messageLayout.marginHeight = 3;
		
		wMessageComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wMessageComp);
 		wMessageComp.setLayout(contentLayout);
		
		// ////////////////////////////////////
		// START OF MESSAGE SETTINGS  GROUP
		////////////////////////////////////// 

		wMessageSettingsGroup = new Group(wMessageComp, SWT.SHADOW_NONE);
		props.setLook(wMessageSettingsGroup);
		wMessageSettingsGroup.setText(Messages.getString("Mail.Group.MessageSettings.Label"));
		
		FormLayout messagesettingsgroupLayout = new FormLayout();
		messagesettingsgroupLayout.marginWidth = 10;
		messagesettingsgroupLayout.marginHeight = 10;
		wMessageSettingsGroup.setLayout(messagesettingsgroupLayout);
        
        // Add date to logfile name?
        wlAddDate = new Label(wMessageSettingsGroup, SWT.RIGHT);
        wlAddDate.setText(Messages.getString("Mail.IncludeDate.Label"));
        props.setLook(wlAddDate);
        fdlAddDate = new FormData();
        fdlAddDate.left = new FormAttachment(0, 0);
        fdlAddDate.top = new FormAttachment(0,margin);
        fdlAddDate.right = new FormAttachment(middle, -2*margin);
        wlAddDate.setLayoutData(fdlAddDate);
        wAddDate = new Button(wMessageSettingsGroup, SWT.CHECK);
        props.setLook(wAddDate);
        fdAddDate = new FormData();
        fdAddDate.left = new FormAttachment(middle, -margin);
        fdAddDate.top = new FormAttachment(0, margin);
        fdAddDate.right = new FormAttachment(100, 0);
        wAddDate.setLayoutData(fdAddDate);
        wAddDate.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
               input.setChanged();
            }
        });

        // Only send the comment in the mail body
        wlOnlyComment = new Label(wMessageSettingsGroup, SWT.RIGHT);
        wlOnlyComment.setText(Messages.getString("Mail.OnlyCommentInBody.Label"));
        props.setLook(wlOnlyComment);
        fdlOnlyComment = new FormData();
        fdlOnlyComment.left = new FormAttachment(0, 0);
        fdlOnlyComment.top = new FormAttachment(wAddDate, margin);
        fdlOnlyComment.right = new FormAttachment(middle, -2*margin);
        wlOnlyComment.setLayoutData(fdlOnlyComment);
        wOnlyComment = new Button(wMessageSettingsGroup, SWT.CHECK);
        props.setLook(wOnlyComment);
        fdOnlyComment = new FormData();
        fdOnlyComment.left = new FormAttachment(middle, -margin);
        fdOnlyComment.top = new FormAttachment(wAddDate, margin );
        fdOnlyComment.right = new FormAttachment(100, 0);
        wOnlyComment.setLayoutData(fdOnlyComment);
        wOnlyComment.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {

            }
        });
        
        
        // HTML format ?
        wlUseHTML = new Label(wMessageSettingsGroup, SWT.RIGHT);
        wlUseHTML.setText(Messages.getString("Mail.UseHTMLInBody.Label"));
        props.setLook(wlUseHTML);
        fdlUseHTML = new FormData();
        fdlUseHTML.left = new FormAttachment(0, 0);
        fdlUseHTML.top = new FormAttachment(wOnlyComment, margin );
        fdlUseHTML.right = new FormAttachment(middle, -2*margin);
        wlUseHTML.setLayoutData(fdlUseHTML);
        wUseHTML = new Button(wMessageSettingsGroup, SWT.CHECK);
        props.setLook(wUseHTML);
        fdUseHTML = new FormData();
        fdUseHTML.left = new FormAttachment(middle, -margin);
        fdUseHTML.top = new FormAttachment(wOnlyComment, margin );
        fdUseHTML.right = new FormAttachment(100, 0);
        wUseHTML.setLayoutData(fdUseHTML);
        wUseHTML.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
            	SetEnabledEncoding();

            }
        });
           
        
        
        // Encoding
        wlEncoding=new Label(wMessageSettingsGroup, SWT.RIGHT);
        wlEncoding.setText(Messages.getString("Mail.Encoding.Label"));
        props.setLook(wlEncoding);
        fdlEncoding=new FormData();
        fdlEncoding.left = new FormAttachment(0, 0);
        fdlEncoding.top  = new FormAttachment(wUseHTML, margin);
        fdlEncoding.right= new FormAttachment(middle, -2*margin);
        wlEncoding.setLayoutData(fdlEncoding);
        wEncoding=new CCombo(wMessageSettingsGroup, SWT.BORDER | SWT.READ_ONLY);
        wEncoding.setEditable(true);
        props.setLook(wEncoding);
        wEncoding.addModifyListener(lsMod);
        fdEncoding=new FormData();
        fdEncoding.left = new FormAttachment(middle, -margin);
        fdEncoding.top  = new FormAttachment(wUseHTML,margin);
        fdEncoding.right= new FormAttachment(100, 0);
        wEncoding.setLayoutData(fdEncoding);
        wEncoding.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                    shell.setCursor(busy);
                    setEncodings();
                    shell.setCursor(null);
                    busy.dispose();
                }
            }
        );

        // Use Priority ?
        wlUsePriority = new Label(wMessageSettingsGroup, SWT.RIGHT);
        wlUsePriority.setText(Messages.getString("Mail.UsePriority.Label"));
        props.setLook(wlUsePriority);
        fdlPriority = new FormData();
        fdlPriority.left = new FormAttachment(0, 0);
        fdlPriority.top = new FormAttachment(wEncoding, margin );
        fdlPriority.right = new FormAttachment(middle, -2*margin);
        wlUsePriority.setLayoutData(fdlPriority);
        wUsePriority = new Button(wMessageSettingsGroup, SWT.CHECK);
        wUsePriority.setToolTipText(Messages.getString("Mail.UsePriority.Tooltip"));
        props.setLook(wUsePriority);
        fdUsePriority = new FormData();
        fdUsePriority.left = new FormAttachment(middle, -margin);
        fdUsePriority.top = new FormAttachment(wEncoding, margin );
        fdUsePriority.right = new FormAttachment(100, 0);
        wUsePriority.setLayoutData(fdUsePriority);
        wUsePriority.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
            	activeUsePriority();
            	input.setChanged();
            }
        });
        
        // Priority
        wlPriority = new Label(wMessageSettingsGroup, SWT.RIGHT);
        wlPriority.setText(Messages.getString("Mail.Priority.Label"));
        props.setLook(wlPriority);
        fdlPriority = new FormData();
        fdlPriority.left = new FormAttachment(0, 0);
        fdlPriority.right = new FormAttachment(middle, -2*margin);
        fdlPriority.top = new FormAttachment(wUsePriority, margin);
        wlPriority.setLayoutData(fdlPriority);
        wPriority = new CCombo(wMessageSettingsGroup, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        wPriority.add(Messages.getString("Mail.Priority.Low.Label"));
        wPriority.add(Messages.getString("Mail.Priority.Normal.Label"));
        wPriority.add(Messages.getString("Mail.Priority.High.Label"));
        wPriority.select(1); // +1: starts at -1
        props.setLook(wPriority);
        fdPriority = new FormData();
        fdPriority.left = new FormAttachment(middle, -margin);
        fdPriority.top = new FormAttachment(wUsePriority, margin);
        fdPriority.right = new FormAttachment(100, 0);
        wPriority.setLayoutData(fdPriority);

        // Importance
        wlImportance = new Label(wMessageSettingsGroup, SWT.RIGHT);
        wlImportance.setText(Messages.getString("Mail.Importance.Label"));
        props.setLook(wlImportance);
        fdlImportance = new FormData();
        fdlImportance.left = new FormAttachment(0, 0);
        fdlImportance.right = new FormAttachment(middle, -2*margin);
        fdlImportance.top = new FormAttachment(wPriority, margin);
        wlImportance.setLayoutData(fdlImportance);
        wImportance = new CCombo(wMessageSettingsGroup, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        wImportance.add(Messages.getString("Mail.Priority.Low.Label"));
        wImportance.add(Messages.getString("Mail.Priority.Normal.Label"));
        wImportance.add(Messages.getString("Mail.Priority.High.Label"));
   
        wImportance.select(1); // +1: starts at -1
        
        props.setLook(wImportance);
        fdImportance = new FormData();
        fdImportance.left = new FormAttachment(middle, -margin);
        fdImportance.top = new FormAttachment(wPriority, margin);
        fdImportance.right = new FormAttachment(100, 0);
        wImportance.setLayoutData(fdImportance);
        
        
        fdMessageSettingsGroup = new FormData();
    	fdMessageSettingsGroup.left = new FormAttachment(0, margin);
      	fdMessageSettingsGroup.top = new FormAttachment(wName, margin);
      	fdMessageSettingsGroup.right = new FormAttachment(100, -margin);
      	wMessageSettingsGroup.setLayoutData(fdMessageSettingsGroup);
		
		// //////////////////////////////////////
		// / END OF MESSAGE SETTINGS  GROUP
		// ///////////////////////////////////////
      	
      	
      	
    	// ////////////////////////////////////
		// START OF MESSAGE   GROUP
		////////////////////////////////////// 

		wMessageGroup = new Group(wMessageComp, SWT.SHADOW_NONE);
		props.setLook(wMessageGroup);
		wMessageGroup.setText(Messages.getString("Mail.Group.Message.Label"));
		
		FormLayout messagegroupLayout = new FormLayout();
		messagegroupLayout.marginWidth = 10;
		messagegroupLayout.marginHeight = 10;
		wMessageGroup.setLayout(messagegroupLayout);
        
        // Subject line
		wlSubject=new Label(wMessageGroup, SWT.RIGHT);
        wlSubject.setText(Messages.getString("Mail.Subject.Label"));
        props.setLook(wlSubject);
        fdlSubject=new FormData();
        fdlSubject.left = new FormAttachment(0, -margin);
        fdlSubject.top  = new FormAttachment(wMessageSettingsGroup, margin);
        fdlSubject.right= new FormAttachment(middle, -2*margin);
        wlSubject.setLayoutData(fdlSubject);
        
        wSubject=new CCombo(wMessageGroup, SWT.BORDER | SWT.READ_ONLY);
        wSubject.setEditable(true);
        props.setLook(wSubject);
        wSubject.addModifyListener(lsMod);
        fdSubject=new FormData();
        fdSubject.left = new FormAttachment(middle, -margin);
        fdSubject.top  = new FormAttachment(wMessageSettingsGroup, margin);
        fdSubject.right= new FormAttachment(100, -margin);
        wSubject.setLayoutData(fdSubject);         
        wSubject.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                    shell.setCursor(busy);
                    getPreviousFields();
                    shell.setCursor(null);
                    busy.dispose();
                }
            }
        );  
        // Comment line
		wlComment=new Label(wMessageGroup, SWT.RIGHT);
        wlComment.setText(Messages.getString("Mail.Comment.Label"));
        props.setLook(wlComment);
        fdlComment=new FormData();
        fdlComment.left = new FormAttachment(0, -margin);
        fdlComment.top  = new FormAttachment(wSubject, margin);
        fdlComment.right= new FormAttachment(middle, -2*margin);
        wlComment.setLayoutData(fdlComment);
        
        wComment=new CCombo(wMessageGroup, SWT.BORDER | SWT.READ_ONLY);
        wComment.setEditable(true);
        props.setLook(wComment);
        wComment.addModifyListener(lsMod);
        fdComment=new FormData();
        fdComment.left = new FormAttachment(middle, -margin);
        fdComment.top  = new FormAttachment(wSubject, margin);
        fdComment.right= new FormAttachment(100, -margin);
        wComment.setLayoutData(fdComment);         
        wComment.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                    shell.setCursor(busy);
                    getPreviousFields();
                    shell.setCursor(null);
                    busy.dispose();
                }
            }
        );  
        fdMessageGroup = new FormData();
        fdMessageGroup.left = new FormAttachment(0, margin);
    	fdMessageGroup.top = new FormAttachment(wMessageSettingsGroup, margin);
    	fdMessageGroup.bottom = new FormAttachment(100, -margin);
      	fdMessageGroup.right = new FormAttachment(100, -margin);
      	wMessageGroup.setLayoutData(fdMessageGroup);
		
		// //////////////////////////////////////
		// / END OF MESSAGE   GROUP
		// ///////////////////////////////////////
 		
		fdMessageComp = new FormData();
		fdMessageComp.left  = new FormAttachment(0, 0);
		fdMessageComp.top   = new FormAttachment(0, 0);
		fdMessageComp.right = new FormAttachment(100, 0);
		fdMessageComp.bottom= new FormAttachment(100, 0);
		wMessageComp.setLayoutData(wMessageComp);

		wMessageComp.layout();
		wMessageTab.setControl(wMessageComp);


		/////////////////////////////////////////////////////////////
		/// END OF MESSAGE TAB
		/////////////////////////////////////////////////////////////
 		
 		
		
		//////////////////////////////////////
		// START OF ATTACHED FILES   TAB   ///
		/////////////////////////////////////
		
		
		
		wAttachedTab=new CTabItem(wTabFolder, SWT.NONE);
		wAttachedTab.setText(Messages.getString("Mail.Tab.AttachedFiles.Label"));

		FormLayout attachedLayout = new FormLayout ();
		attachedLayout.marginWidth  = 3;
		attachedLayout.marginHeight = 3;
		
		wAttachedComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wAttachedComp);
 		wAttachedComp.setLayout(attachedLayout);
 		
 		
 		// ///////////////////////////////
		// START OF Origin files GROUP  //
		///////////////////////////////// 

		wOriginFiles = new Group(wAttachedComp, SWT.SHADOW_NONE);
		props.setLook(wOriginFiles);
		wOriginFiles.setText(Messages.getString("MailDialog.OriginAttachedFiles.Label"));
		
		FormLayout OriginFilesgroupLayout = new FormLayout();
		OriginFilesgroupLayout.marginWidth = 10;
		OriginFilesgroupLayout.marginHeight = 10;
		wOriginFiles.setLayout(OriginFilesgroupLayout);
		
		//Is Filename defined in a Field		
		wlisFileDynamic = new Label(wOriginFiles, SWT.RIGHT);
		wlisFileDynamic.setText(Messages.getString("MailDialog.isFileDynamic.Label"));
		props.setLook(wlisFileDynamic);
		fdlisFileDynamic = new FormData();
		fdlisFileDynamic.left = new FormAttachment(0, -margin);
		fdlisFileDynamic.top = new FormAttachment(0, margin);
		fdlisFileDynamic.right = new FormAttachment(middle, -2*margin);
		wlisFileDynamic.setLayoutData(fdlisFileDynamic);
		
		
		wisFileDynamic = new Button(wOriginFiles, SWT.CHECK);
		props.setLook(wisFileDynamic);
		wisFileDynamic.setToolTipText(Messages.getString("MailDialog.isFileDynamic.Tooltip"));
		fdisFileDynamic = new FormData();
		fdisFileDynamic.left = new FormAttachment(middle, -margin);
		fdisFileDynamic.top = new FormAttachment(0, margin);
		wisFileDynamic.setLayoutData(fdisFileDynamic);		
		SelectionAdapter lisFileDynamic = new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
            	ActiveisFileDynamic();
            	input.setChanged();
            }
        };
        wisFileDynamic.addSelectionListener(lisFileDynamic);
        
        
		// Filename field
		wlDynamicFilenameField=new Label(wOriginFiles, SWT.RIGHT);
        wlDynamicFilenameField.setText(Messages.getString("MailDialog.DynamicFilenameField.Label"));
        props.setLook(wlDynamicFilenameField);
        fdlFilenameField=new FormData();
        fdlFilenameField.left = new FormAttachment(0, -margin);
        fdlFilenameField.top  = new FormAttachment(wisFileDynamic, margin);
        fdlFilenameField.right= new FormAttachment(middle, -2*margin);
        wlDynamicFilenameField.setLayoutData(fdlFilenameField);
        
        
        wDynamicFilenameField=new CCombo(wOriginFiles, SWT.BORDER | SWT.READ_ONLY);
        wDynamicFilenameField.setEditable(true);
        props.setLook(wDynamicFilenameField);
        wDynamicFilenameField.addModifyListener(lsMod);
        fdFilenameField=new FormData();
        fdFilenameField.left = new FormAttachment(middle, -margin);
        fdFilenameField.top  = new FormAttachment(wisFileDynamic, margin);
        fdFilenameField.right= new FormAttachment(100, -margin);
        wDynamicFilenameField.setLayoutData(fdFilenameField);   	
        wDynamicFilenameField.addFocusListener(new FocusListener()
        {
            public void focusLost(org.eclipse.swt.events.FocusEvent e)
            {
            }
        
            public void focusGained(org.eclipse.swt.events.FocusEvent e)
            {
                Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                shell.setCursor(busy);
                getPreviousFields();
                shell.setCursor(null);
                busy.dispose();
            }
        }
    );  
    	

         // Wildcard field
 		 wlDynamicWildcardField=new Label(wOriginFiles, SWT.RIGHT);
         wlDynamicWildcardField.setText(Messages.getString("MailDialog.DynamicWildcardField.Label"));
         props.setLook(wlDynamicWildcardField);
         fdlDynamicWildcardField=new FormData();
         fdlDynamicWildcardField.left = new FormAttachment(0, -margin);
         fdlDynamicWildcardField.top  = new FormAttachment(wDynamicFilenameField, margin);
         fdlDynamicWildcardField.right= new FormAttachment(middle, -2*margin);
         wlDynamicWildcardField.setLayoutData(fdlDynamicWildcardField); 
         
         wDynamicWildcardField=new CCombo(wOriginFiles, SWT.BORDER | SWT.READ_ONLY);
         wDynamicWildcardField.setEditable(true);
         props.setLook(wDynamicWildcardField);
         wDynamicWildcardField.addModifyListener(lsMod);
         fdDynamicWildcardField=new FormData();
         fdDynamicWildcardField.left = new FormAttachment(middle, -margin);
         fdDynamicWildcardField.top  = new FormAttachment(wDynamicFilenameField, margin);
         fdDynamicWildcardField.right= new FormAttachment(100, -margin);
         wDynamicWildcardField.setLayoutData(fdDynamicWildcardField);
         wDynamicWildcardField.addFocusListener(new FocusListener()
         {
             public void focusLost(org.eclipse.swt.events.FocusEvent e)
             {
             }
         
             public void focusGained(org.eclipse.swt.events.FocusEvent e)
             {
                 Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                 shell.setCursor(busy);
                 getPreviousFields();
                 shell.setCursor(null);
                 busy.dispose();
             }
         }
     );  
     	

 		
 		// FileFoldername line
		wlSourceFileFoldername=new Label(wOriginFiles, SWT.RIGHT);
		wlSourceFileFoldername.setText(Messages.getString("MailDialog.FileFoldername.Label"));
 		props.setLook(wlSourceFileFoldername);
		fdlSourceFileFoldername=new FormData();
		fdlSourceFileFoldername.left = new FormAttachment(0, 0);
		fdlSourceFileFoldername.top  = new FormAttachment(wDynamicWildcardField, 2*margin);
		fdlSourceFileFoldername.right= new FormAttachment(middle, -margin);
		wlSourceFileFoldername.setLayoutData(fdlSourceFileFoldername);
		
		// Browse Destination folders button ...
		wbSourceFolder=new Button(wOriginFiles, SWT.PUSH| SWT.CENTER);
		props.setLook(wbSourceFolder);
		wbSourceFolder.setText(Messages.getString("MailDialog.BrowseFolders.Label"));
		fdbSourceFolder=new FormData();
		fdbSourceFolder.right= new FormAttachment(100, 0);
		fdbSourceFolder.top  = new FormAttachment(wDynamicWildcardField, 2*margin);
		wbSourceFolder.setLayoutData(fdbSourceFolder);
		wbSourceFolder.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					DirectoryDialog ddialog = new DirectoryDialog(shell, SWT.OPEN);
					if (wSourceFileFoldername.getText()!=null)
					{
						ddialog.setFilterPath(transMeta.environmentSubstitute(wSourceFileFoldername.getText()) );
					}
					
					 // Calling open() will open and run the dialog.
			        // It will return the selected directory, or
			        // null if user cancels
			        String dir = ddialog.open();
			        if (dir != null) {
			          // Set the text box to the new selection
			        	wSourceFileFoldername.setText(dir);
			        }
					
				}
			}
		);

		
		
		// Browse source file button ...
		wbFileFoldername=new Button(wOriginFiles, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbFileFoldername);
		wbFileFoldername.setText(Messages.getString("MailDialog.BrowseFiles.Label"));
		fdbSourceFileFoldername=new FormData();
		fdbSourceFileFoldername.right= new FormAttachment(wbSourceFolder, -margin);
		fdbSourceFileFoldername.top  = new FormAttachment(wDynamicWildcardField, 2*margin);
		wbFileFoldername.setLayoutData(fdbSourceFileFoldername);
		
		wSourceFileFoldername=new TextVar(transMeta,wOriginFiles, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wSourceFileFoldername);
		wSourceFileFoldername.addModifyListener(lsMod);
		fdSourceFileFoldername=new FormData();
		fdSourceFileFoldername.left = new FormAttachment(middle, 0);
		fdSourceFileFoldername.top  = new FormAttachment(wDynamicWildcardField, 2*margin);
		fdSourceFileFoldername.right= new FormAttachment(wbFileFoldername, -margin);
		wSourceFileFoldername.setLayoutData(fdSourceFileFoldername);

		// Whenever something changes, set the tooltip to the expanded version:
		wSourceFileFoldername.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					wSourceFileFoldername.setToolTipText(transMeta.environmentSubstitute( wSourceFileFoldername.getText() ) );
				}
			}
		);

		wbFileFoldername.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);
					dialog.setFilterExtensions(new String[] {"*"});
					if (wSourceFileFoldername.getText()!=null)
					{
						dialog.setFileName(transMeta.environmentSubstitute(wSourceFileFoldername.getText()) );
					}
					dialog.setFilterNames(FILETYPES);
					if (dialog.open()!=null)
					{
						wSourceFileFoldername.setText(dialog.getFilterPath()+Const.FILE_SEPARATOR+dialog.getFileName());
					}
				}
			}
		);
		

		//Include sub folders
		wlincludeSubFolders = new Label(wOriginFiles, SWT.RIGHT);
		wlincludeSubFolders.setText(Messages.getString("MailDialog.includeSubFolders.Label"));
		props.setLook(wlincludeSubFolders);
		fdlincludeSubFolders = new FormData();
		fdlincludeSubFolders.left = new FormAttachment(0, 0);
		fdlincludeSubFolders.top = new FormAttachment(wSourceFileFoldername, margin);
		fdlincludeSubFolders.right = new FormAttachment(middle, -margin);
		wlincludeSubFolders.setLayoutData(fdlincludeSubFolders);
		wincludeSubFolders = new Button(wOriginFiles, SWT.CHECK);
		props.setLook(wincludeSubFolders);
		wincludeSubFolders.setToolTipText(Messages.getString("MailDialog.includeSubFolders.Tooltip"));
		fdincludeSubFolders = new FormData();
		fdincludeSubFolders.left = new FormAttachment(middle, 0);
		fdincludeSubFolders.top = new FormAttachment(wSourceFileFoldername, margin);
		fdincludeSubFolders.right = new FormAttachment(100, 0);
		wincludeSubFolders.setLayoutData(fdincludeSubFolders);
		wincludeSubFolders.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				input.setChanged();
			}
		});

		// Wildcard
		wlWildcard=new Label(wOriginFiles, SWT.RIGHT);
		wlWildcard.setText(Messages.getString("MailDialog.Wildcard.Label"));
 		props.setLook(wlWildcard);
		fdlWildcard=new FormData();
		fdlWildcard.left = new FormAttachment(0, 0);
		fdlWildcard.top  = new FormAttachment(wincludeSubFolders, margin);
		fdlWildcard.right= new FormAttachment(middle, -margin);
		wlWildcard.setLayoutData(fdlWildcard);
		wWildcard=new TextVar(transMeta,wOriginFiles, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wWildcard);
 		wWildcard.setToolTipText(Messages.getString("MailDialog.Wildcard.Tooltip"));
		wWildcard.addModifyListener(lsMod);
		fdWildcard=new FormData();
		fdWildcard.left = new FormAttachment(middle, 0);
		fdWildcard.top  = new FormAttachment(wincludeSubFolders, margin);
		fdWildcard.right= new FormAttachment(wbFileFoldername, -margin);
		wWildcard.setLayoutData(fdWildcard);

		// Whenever something changes, set the tooltip to the expanded version:
		wWildcard.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					wWildcard.setToolTipText(transMeta.environmentSubstitute( wWildcard.getText() ) );
				}
			}
		);
		fdOriginFiles = new FormData();
		fdOriginFiles.left = new FormAttachment(0, margin);
		fdOriginFiles.top = new FormAttachment(0, 2*margin);
		fdOriginFiles.right = new FormAttachment(100, -margin);
		wOriginFiles.setLayoutData(fdOriginFiles);
		
		// ///////////////////////////////////////////////////////////
		// / END OF Origin files GROUP
		// ///////////////////////////////////////////////////////////		

		
 		// ///////////////////////////////
		// START OF Zip Group files GROUP  //
		///////////////////////////////// 

		wZipGroup = new Group(wAttachedComp, SWT.SHADOW_NONE);
		props.setLook(wZipGroup);
		wZipGroup.setText(Messages.getString("MailDialog.ZipGroup.Label"));
		
		FormLayout ZipGroupgroupLayout = new FormLayout();
		ZipGroupgroupLayout.marginWidth = 10;
		ZipGroupgroupLayout.marginHeight = 10;
		wZipGroup.setLayout(ZipGroupgroupLayout);

		
		 // Zip Files?
       wlZipFiles = new Label(wZipGroup, SWT.RIGHT);
       wlZipFiles.setText(Messages.getString("MailDialog.ZipFiles.Label"));
       props.setLook(wlZipFiles);
       fdlZipFiles = new FormData();
       fdlZipFiles.left = new FormAttachment(0, -margin);
       fdlZipFiles.top = new FormAttachment(wOriginFiles, margin);
       fdlZipFiles.right = new FormAttachment(middle, -2*margin);
       wlZipFiles.setLayoutData(fdlZipFiles);
       wZipFiles = new Button(wZipGroup, SWT.CHECK);
       props.setLook(wZipFiles);
       fdZipFiles = new FormData();
       fdZipFiles.left = new FormAttachment(middle,-margin);
       fdZipFiles.top = new FormAttachment(wOriginFiles, margin);
       fdZipFiles.right = new FormAttachment(100, -margin);
       wZipFiles.setLayoutData(fdZipFiles);
       wZipFiles.addSelectionListener(new SelectionAdapter()
       {
           public void widgetSelected(SelectionEvent e)
           {
               input.setChanged();
               setZip();
           }
       });
       
		 // is zipfilename is dynamic?
       wlisZipFileDynamic = new Label(wZipGroup, SWT.RIGHT);
       wlisZipFileDynamic.setText(Messages.getString("MailDialog.isZipFileDynamic.Label"));
       props.setLook(wlisZipFileDynamic);
       fdlisZipFileDynamic = new FormData();
       fdlisZipFileDynamic.left = new FormAttachment(0, -margin);
       fdlisZipFileDynamic.top = new FormAttachment(wZipFiles, margin);
       fdlisZipFileDynamic.right = new FormAttachment(middle, -2*margin);
       wlisZipFileDynamic.setLayoutData(fdlisZipFileDynamic);
       wisZipFileDynamic = new Button(wZipGroup, SWT.CHECK);
       props.setLook(wisZipFileDynamic);
       fdisZipFileDynamic = new FormData();
       fdisZipFileDynamic.left = new FormAttachment(middle,-margin);
       fdisZipFileDynamic.top = new FormAttachment(wZipFiles, margin);
       fdisZipFileDynamic.right = new FormAttachment(100, -margin);
       wisZipFileDynamic.setLayoutData(fdisZipFileDynamic);
       wisZipFileDynamic.addSelectionListener(new SelectionAdapter()
       {
           public void widgetSelected(SelectionEvent e)
           {
               input.setChanged();
               setDynamicZip();
           }
       });

       // ZipFile field
	   wlDynamicZipFileField=new Label(wZipGroup, SWT.RIGHT);
       wlDynamicZipFileField.setText(Messages.getString("MailDialog.DynamicZipFileField.Label"));
       props.setLook(wlDynamicZipFileField);
       fdlDynamicZipFileField=new FormData();
       fdlDynamicZipFileField.left = new FormAttachment(0, -margin);
       fdlDynamicZipFileField.top  = new FormAttachment(wisZipFileDynamic, margin);
       fdlDynamicZipFileField.right= new FormAttachment(middle, -2*margin);
       wlDynamicZipFileField.setLayoutData(fdlDynamicZipFileField); 
       
       wDynamicZipFileField=new CCombo(wZipGroup, SWT.BORDER | SWT.READ_ONLY);
       wDynamicZipFileField.setEditable(true);
       props.setLook(wDynamicZipFileField);
       wDynamicZipFileField.addModifyListener(lsMod);
       fdDynamicZipFileField=new FormData();
       fdDynamicZipFileField.left = new FormAttachment(middle, -margin);
       fdDynamicZipFileField.top  = new FormAttachment(wisZipFileDynamic, margin);
       fdDynamicZipFileField.right= new FormAttachment(100, -margin);
       wDynamicZipFileField.setLayoutData(fdDynamicZipFileField);
       wDynamicZipFileField.addFocusListener(new FocusListener()
       {
           public void focusLost(org.eclipse.swt.events.FocusEvent e)
           {
           }
       
           public void focusGained(org.eclipse.swt.events.FocusEvent e)
           {
               Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
               shell.setCursor(busy);
               getPreviousFields();
               shell.setCursor(null);
               busy.dispose();
           }
       }
   ); 
       
       // ZipFilename line
       wZipFilename = new LabelTextVar(transMeta,wZipGroup, Messages.getString("MailDialog.ZipFilename.Label"),
           Messages.getString("MailDialog.ZipFilename.Tooltip"));
       wZipFilename.addModifyListener(lsMod);
       fdZipFilename = new FormData();
       fdZipFilename.left = new FormAttachment(0, -margin);
       fdZipFilename.top = new FormAttachment(wDynamicZipFileField, margin);
       fdZipFilename.right = new FormAttachment(100, -4*margin);
       wZipFilename.setLayoutData(fdZipFilename);
       
       // Zip files on condition?
       wZipSizeCondition = new LabelTextVar(transMeta,wZipGroup, Messages.getString("MailDialog.ZipSizeCondition.Label"),
           Messages.getString("MailDialog.ZipSizeCondition.Tooltip"));
       wZipSizeCondition.addModifyListener(lsMod);
       fdZipSizeCondition = new FormData();
       fdZipSizeCondition.left = new FormAttachment(0, -margin);
       fdZipSizeCondition.top = new FormAttachment(wZipFilename, margin);
       fdZipSizeCondition.right = new FormAttachment(100, -4*margin);
       wZipSizeCondition.setLayoutData(fdZipSizeCondition);


	   fdZipGroup = new FormData();
	   fdZipGroup.left = new FormAttachment(0, margin);
	   fdZipGroup.top = new FormAttachment(wOriginFiles, margin);
	   fdZipGroup.right = new FormAttachment(100, -margin);
	   wZipGroup.setLayoutData(fdZipGroup);
		
		// ///////////////////////////////////////////////////////////
		// / END OF Zip Group GROUP
		// ///////////////////////////////////////////////////////////		

 		
 		fdAttachedComp = new FormData();
		fdAttachedComp.left  = new FormAttachment(0, 0);
		fdAttachedComp.top   = new FormAttachment(0, 0);
		fdAttachedComp.right = new FormAttachment(100, 0);
		fdAttachedComp.bottom= new FormAttachment(100, 0);
		wAttachedComp.setLayoutData(wAttachedComp);

		wAttachedComp.layout();
		wAttachedTab.setControl(wAttachedComp);


		/////////////////////////////////////////////////////////////
		/// END OF FILES TAB
		/////////////////////////////////////////////////////////////
 		
		
		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(wStepname, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);

        
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, wTabFolder);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );


		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
		ActiveisFileDynamic();
        SetEnabledEncoding();
        activeUsePriority();
        setDynamicZip();
        setZip();
        setUseAuth();
		
		input.setChanged(changed);
		wTabFolder.setSelection(0);
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	private void setDynamicZip()
	{
		wDynamicZipFileField.setEnabled(wZipFiles.getSelection() && wisZipFileDynamic.getSelection());
		wlDynamicZipFileField.setEnabled(wZipFiles.getSelection() && wisZipFileDynamic.getSelection());
	}
	private void setZip()
	{
		wZipFilename.setEnabled(wZipFiles.getSelection());
		wZipSizeCondition.setEnabled(wZipFiles.getSelection());
		wlisZipFileDynamic.setEnabled(wZipFiles.getSelection());
		wisZipFileDynamic.setEnabled(wZipFiles.getSelection());
		setDynamicZip();
	}
	private void ActiveisFileDynamic()
	{
		wlDynamicFilenameField.setEnabled(wisFileDynamic.getSelection());
		wDynamicFilenameField.setEnabled(wisFileDynamic.getSelection());
		wlDynamicWildcardField.setEnabled(wisFileDynamic.getSelection());
		wDynamicWildcardField.setEnabled(wisFileDynamic.getSelection());
		wWildcard.setEnabled(!wisFileDynamic.getSelection());
		wlWildcard.setEnabled(!wisFileDynamic.getSelection());
		wSourceFileFoldername.setEnabled(!wisFileDynamic.getSelection());
		wlSourceFileFoldername.setEnabled(!wisFileDynamic.getSelection());
		wbFileFoldername.setEnabled(!wisFileDynamic.getSelection());
		wbSourceFolder.setEnabled(!wisFileDynamic.getSelection());
	}
	 private void getPreviousFields()
	 {
		 try{
	         if(!getpreviousFields)
	         {
	        	 getpreviousFields=true;
	        	 String destination=null;
	        	 if(wDestination!=null) destination=wDestination.getText();
				 wDestination.removeAll();
				 
	        	 String destinationcc=null;
	        	 if(wDestinationCc!=null) destinationcc=wDestinationCc.getText();
				 wDestinationCc.removeAll();
				 
	        	 String destinationbcc=null;
	        	 if(wDestinationBCc!=null) destinationbcc=wDestinationBCc.getText();
				 wDestinationBCc.removeAll();
				 
	        	 String replyname=null;
	        	 if(wReplyName!=null) replyname=wReplyName.getText();
	        	 wReplyName.removeAll();
	        	 
	        	 String replyaddress=null;
	        	 if(wReply!=null) replyaddress=wReply.getText();
	        	 wReply.removeAll();
	        	 
	        	 String person=null;
	        	 if(wPerson!=null) person=wPerson.getText();
	        	 wPerson.removeAll();
	        	 
	        	 String phone=null;
	        	 if(wPhone!=null) phone=wPhone.getText();
	        	 wPhone.removeAll();
	        	 
	        	 String servername=null;
	        	 if(wServer!=null) servername=wServer.getText();
	        	 wServer.removeAll();
	        	 
	        	 String port=null;
	        	 if(wPort!=null) port=wPort.getText();
	        	 wPort.removeAll();
	        	 
	        	 String authuser=null;
	        	 String authpass=null;
	        	
	        	 if(wAuthUser!=null) authuser=wAuthUser.getText();
	        	 wAuthUser.removeAll();
	        	 if(wAuthPass!=null) authpass=wAuthPass.getText();
	        	 wAuthPass.removeAll();
	        	 
	        	 
	        	 String subject=null;
	        	 if(wSubject!=null) subject=wSubject.getText();
	        	 wSubject.removeAll();
	        	 
	        	 String comment=null;
	        	 if(wComment!=null) comment=wComment.getText();
	        	 wComment.removeAll();
					
	        	 
	        	 String dynamFile=null;
	        	 String dynamWildcard=null;
	        	
	        	 if(wDynamicFilenameField!=null) dynamFile=wDynamicFilenameField.getText();
	        	 wDynamicFilenameField.removeAll();
	        	 if(wDynamicWildcardField!=null) dynamWildcard=wDynamicWildcardField.getText();
	        	 wDynamicWildcardField.removeAll();
        	 
	        	 
	        	 String dynamZipFile=null;
	        
        		 if(wDynamicZipFileField!=null) dynamZipFile=wDynamicZipFileField.getText(); 
        		 wDynamicZipFileField.removeAll();
        	 
        		 RowMetaInterface r = transMeta.getPrevStepFields(stepname);
				 if (r!=null)
				 {
					 String[] fieldnames=r.getFieldNames();
		             wDestination.setItems(fieldnames);
		             wDestinationCc.setItems(fieldnames);
		             wDestinationBCc.setItems(fieldnames);
		             wReplyName.setItems(fieldnames);
		             wReply.setItems(fieldnames);
		             wPerson.setItems(fieldnames);
		             wPhone.setItems(fieldnames);
		             wServer.setItems(fieldnames);
		             wPort.setItems(fieldnames);
		             wAuthUser.setItems(fieldnames);
		             wAuthPass.setItems(fieldnames);
		             wSubject.setItems(fieldnames);
		             wComment.setItems(fieldnames);
		             wDynamicFilenameField.setItems(fieldnames);
		             wDynamicWildcardField.setItems(fieldnames);
		             wDynamicZipFileField.setItems(fieldnames);
		             
				 }
				 if(destination!=null) wDestination.setText(destination);
				 if(destinationcc!=null) wDestinationCc.setText(destinationcc);
				 if(destinationbcc!=null) wDestinationBCc.setText(destinationbcc);
				 if(replyname!=null) wReplyName.setText(replyname);
				 if(replyaddress!=null) wReply.setText(replyaddress);
				 if(person!=null) wPerson.setText(person);
				 if(phone!=null) wPhone.setText(phone);
				 if(servername!=null) wServer.setText(servername);
				 if(port!=null) wPort.setText(port);
				 if(authuser!=null) wAuthUser.setText(authuser);
				 if(authpass!=null) wAuthPass.setText(authpass);
				 if(subject!=null) wSubject.setText(subject);
				 if(comment!=null) wComment.setText(comment);
				 if(dynamFile!=null) wDynamicFilenameField.setText(dynamFile);
				 if(dynamWildcard!=null) wDynamicWildcardField.setText(dynamWildcard);
				 if(dynamZipFile!=null) wDynamicZipFileField.setText(dynamZipFile);
	         }
			 
			
		 }catch(KettleException ke){
				new ErrorDialog(shell, Messages.getString("MailDialog.FailedToGetFields.DialogTitle"), Messages.getString("MailDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
			}
	 }
	private void activeUsePriority()
	{
		wlPriority.setEnabled(wUsePriority.getSelection());
		wPriority.setEnabled(wUsePriority.getSelection());
		wlImportance.setEnabled(wUsePriority.getSelection());
		wImportance.setEnabled(wUsePriority.getSelection());
	}
    private void SetEnabledEncoding ()
    {
        wEncoding.setEnabled(wUseHTML.getSelection());
        wlEncoding.setEnabled(wUseHTML.getSelection());
        	
    }
    protected void setSecureConnectiontype()
    {
    	wSecureConnectionType.setEnabled(wUseSecAuth.getSelection());
    	wlSecureConnectionType.setEnabled(wUseSecAuth.getSelection());
    }
  
    private void setEncodings()
    {
        // Encoding of the text file:
        if (!gotEncodings)
        {
            gotEncodings = true;
            
            wEncoding.removeAll();
            ArrayList<Charset> values = new ArrayList<Charset>(Charset.availableCharsets().values());
            for (int i=0;i<values.size();i++)
            {
                Charset charSet = values.get(i);
                wEncoding.add( charSet.displayName() );
            }
            
            // Now select the default!
            String defEncoding = Const.getEnvironmentVariable("file.encoding", "UTF-8");
            int idx = Const.indexOfString(defEncoding, wEncoding.getItems() );
            if (idx>=0) wEncoding.select( idx );
        }
    }
	protected void setUseAuth()
	{
        wlAuthUser.setEnabled(wUseAuth.getSelection());
        wAuthUser.setEnabled(wUseAuth.getSelection());
        wlAuthPass.setEnabled(wUseAuth.getSelection());
        wAuthPass.setEnabled(wUseAuth.getSelection());
        wUseSecAuth.setEnabled(wUseAuth.getSelection());
        wlUseSecAuth.setEnabled(wUseAuth.getSelection());
        if (!wUseAuth.getSelection())
        {
        	wSecureConnectionType.setEnabled(false);
        	wlSecureConnectionType.setEnabled(false);
        }
        else
        {
        	setSecureConnectiontype();
        }
        
	}
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{

	        if (input.getDestination() != null)
	            wDestination.setText(input.getDestination());
			if (input.getDestinationCc() != null)
				wDestinationCc.setText(input.getDestinationCc());
			if (input.getDestinationBCc() != null)
				wDestinationBCc.setText(input.getDestinationBCc());
	        if (input.getServer() != null)
	            wServer.setText(input.getServer());
	        if (input.getPort() != null)
	            wPort.setText(input.getPort());
	        if (input.getReplyAddress() != null)
	            wReply.setText(input.getReplyAddress());
	        if (input.getReplyName() != null)
	            wReplyName.setText(input.getReplyName());
	        if (input.getSubject() != null)
	            wSubject.setText(input.getSubject());
	        if (input.getContactPerson() != null)
	            wPerson.setText(input.getContactPerson());
	        if (input.getContactPhone() != null)
	            wPhone.setText(input.getContactPhone());
	        if (input.getComment() != null)
	            wComment.setText(input.getComment());

	        wAddDate.setSelection(input.getIncludeDate());
	        wisFileDynamic.setSelection(input.isDynamicFilename());
	        if(input.getDynamicFieldname()!=null)
	        	wDynamicFilenameField.setText(input.getDynamicFieldname());
	        if(input.getDynamicWildcard()!=null)
	        	wDynamicWildcardField.setText(input.getDynamicWildcard());
	        
	        if(input.getSourceFileFoldername()!=null)
	        	wSourceFileFoldername.setText(input.getSourceFileFoldername());
	        
	        if(input.getSourceWildcard()!=null)
	        	wWildcard.setText(input.getSourceWildcard());
	        
	        wincludeSubFolders.setSelection(input.isIncludeSubFolders());
	        
	        wZipFiles.setSelection(input.isZipFiles());
	        if (input.getZipFilename() != null)
	            wZipFilename.setText(input.getZipFilename());
	        
	        if (input.getZipLimitSize() != null)
	        	wZipSizeCondition.setText(input.getZipLimitSize());
	        else 
	        	wZipSizeCondition.setText("0");
	        
    		wisZipFileDynamic.setSelection(input.isZipFilenameDynamic());
    		if (input.getDynamicZipFilenameField() != null)
    			wDynamicZipFileField.setText(input.getDynamicZipFilenameField());
    		
	        wUseAuth.setSelection(input.isUsingAuthentication());
	        wUseSecAuth.setSelection(input.isUsingSecureAuthentication());
	        if (input.getAuthenticationUser() != null)
	            wAuthUser.setText(input.getAuthenticationUser());
	        if (input.getAuthenticationPassword() != null)
	            wAuthPass.setText(input.getAuthenticationPassword());

	        wOnlyComment.setSelection(input.isOnlySendComment());
	        
	        wUseHTML.setSelection(input.isUseHTML());
	        
	        if (input.getEncoding()!=null) 
	        	wEncoding.setText(""+input.getEncoding());
	        else
	        	wEncoding.setText("UTF-8");
	        	
	        // Secure connection type	
	        if (input.getSecureConnectionType() !=null)
	        	wSecureConnectionType.setText(input.getSecureConnectionType());
	        else
	        	wSecureConnectionType.setText("SSL");

	        wUsePriority.setSelection(input.isUsePriority());
	        
	        // Priority
	        
	        if (input.getPriority()!=null)
	        {
		        if (input.getPriority().equals("low")) 
		        	wPriority.select(0); // Low
		        else if (input.getPriority().equals("normal")) 
		        	wPriority.select(1); // Normal
			    else 	
			    	wPriority.select(2);  // Default High
	        }
	        else
	        	wPriority.select(3);  // Default High

	        // Importance
	        if (input.getImportance()!=null)
	        {
		        if (input.getImportance().equals("low")) 
		        	wImportance.select(0); // Low
		        else if (input.getImportance().equals("normal")) 	
		        	wImportance.select(1); // Normal
			    else 	
			    	wImportance.select(2);  // Default High
	        }
	        else
	        	wImportance.select(3);  // Default High

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
		input.setDestination(wDestination.getText());
		input.setDestinationCc(wDestinationCc.getText());
		input.setDestinationBCc(wDestinationBCc.getText());
        input.setServer(wServer.getText());
        input.setPort(wPort.getText());
        input.setReplyAddress(wReply.getText());
        input.setReplyName(wReplyName.getText());
        input.setSubject(wSubject.getText());
        input.setContactPerson(wPerson.getText());
        input.setContactPhone(wPhone.getText());
        input.setComment(wComment.getText());
        
        input.setIncludeSubFolders(wincludeSubFolders.getSelection());
        input.setIncludeDate(wAddDate.getSelection());
        input.setisDynamicFilename(wisFileDynamic.getSelection());
        input.setDynamicFieldname(wDynamicFilenameField.getText());
        input.setDynamicWildcard(wDynamicWildcardField.getText());
        
		input.setDynamicZipFilenameField(wDynamicZipFileField.getText());
        
        input.setSourceFileFoldername(wSourceFileFoldername.getText());
        input.setSourceWildcard(wWildcard.getText());
        
        input.setZipLimitSize(wZipSizeCondition.getText());

        input.setZipFilenameDynamic(wisZipFileDynamic.getSelection());
        
        input.setZipFilename(wZipFilename.getText());
        input.setZipFiles(wZipFiles.getSelection());
        input.setAuthenticationUser(wAuthUser.getText());
        input.setAuthenticationPassword(wAuthPass.getText());
        input.setUsingAuthentication(wUseAuth.getSelection());
        input.setUsingSecureAuthentication(wUseSecAuth.getSelection());
        input.setOnlySendComment(wOnlyComment.getSelection());
        input.setUseHTML(wUseHTML.getSelection());
        input.setUsePriority(wUsePriority.getSelection());
        
        input.setEncoding(wEncoding.getText());
        input.setPriority(wPriority.getText());
        
        // Priority
        if (wPriority.getSelectionIndex()==0)
        {
        	input.setPriority("low");
        }
        else if (wPriority.getSelectionIndex()==1)
        {
        	input.setPriority("normal");
        }
        else
        {
        	input.setPriority("high");
        }
            
        // Importance
        if (wImportance.getSelectionIndex()==0)
        {
        	input.setImportance("low");
        }
        else if (wImportance.getSelectionIndex()==1)
        {
        	input.setImportance("normal");
        }
        else
        {
        	input.setImportance("high");
        }
                  
        // Secure Connection type
        input.setSecureConnectionType(wSecureConnectionType.getText());
      
		dispose();
	}
}
