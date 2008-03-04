/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 **                                                                   **
 **                                                                   **
 **********************************************************************/


package org.pentaho.di.ui.job.entries.movefiles;

import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;

import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.job.entries.movefiles.JobEntryMoveFiles;
import org.pentaho.di.job.entries.movefiles.Messages;

/**
 * This dialog allows you to edit the Move Files job entry settings.
 *
 * @author Samatar Hassan
 * @since  20-02-2008
 */

public class JobEntryMoveFilesDialog extends JobEntryDialog implements JobEntryDialogInterface
{

	private static final String[] FILETYPES = new String[] 
		{
			Messages.getString("JobMoveFiles.Filetype.All") };
	
	private Label        wlName;
	private Text         wName;
	private FormData     fdlName, fdName;

	private Label        wlSourceFileFolder;
	private Button       wbSourceFileFolder,wbDestinationFileFolder,
						 wbSourceDirectory,wbDestinationDirectory;
	
	private TextVar      wSourceFileFolder;
	private FormData     fdlSourceFileFolder, fdbSourceFileFolder, 
						 fdSourceFileFolder,fdbDestinationFileFolder,fdbSourceDirectory,fdbDestinationDirectory;
	
	private Label        wlMoveEmptyFolders;
	private Button       wMoveEmptyFolders;
	private FormData     fdlMoveEmptyFolders, fdMoveEmptyFolders;

	private Label        wlOverwriteFiles;
	private Button       wOverwriteFiles;
	private FormData     fdlOverwriteFiles, fdOverwriteFiles;

	private Label        wlIncludeSubfolders;
	private Button       wIncludeSubfolders;
	private FormData     fdlIncludeSubfolders, fdIncludeSubfolders;	

	private Button       wOK, wCancel;
	private Listener     lsOK, lsCancel;


	private JobEntryMoveFiles jobEntry;
	private Shell         	    shell;

	private SelectionAdapter lsDef;

	private boolean changed;

	private Label wlPrevious;

	private Button wPrevious;

	private FormData fdlPrevious, fdPrevious;

	private Label wlFields;

	private TableView wFields;

	private FormData fdlFields, fdFields;

	private Group wSettings;
	private FormData fdSettings;

	private Label wlDestinationFileFolder;
	private TextVar wDestinationFileFolder;
	private FormData fdlDestinationFileFolder, fdDestinationFileFolder;
	
	private Label wlWildcard;
	private TextVar wWildcard;
	private FormData fdlWildcard, fdWildcard;

	private Button       wbdSourceFileFolder; // Delete
	private Button       wbeSourceFileFolder; // Edit
	private Button       wbaSourceFileFolder; // Add or change
	
	
	private CTabFolder   wTabFolder;
	private Composite    wGeneralComp,wAdvancedComp;	
	private CTabItem     wGeneralTab,wAdvancedTab;
	private FormData	 fdGeneralComp,fdAdvancedComp;
	private FormData     fdTabFolder;
	
	//  Add File to result
    
	private Group wFileResult;
    private FormData fdFileResult;
    
    
	private Group wSuccessOn;
    private FormData fdSuccessOn;
    
	private Label        wlAddFileToResult;
	private Button       wAddFileToResult;
	private FormData     fdlAddFileToResult, fdAddFileToResult;
	
	private Label        wlCreateDestinationFolder;
	private Button       wCreateDestinationFolder;
	private FormData     fdlCreateDestinationFolder, fdCreateDestinationFolder;
	
	private Label        wlDestinationIsAFile;
	private Button       wDestinationIsAFile;
	private FormData     fdlDestinationIsAFile, fdDestinationIsAFile;
	private FormData fdbeSourceFileFolder, fdbaSourceFileFolder, fdbdSourceFileFolder;

	private Label wlSuccessCondition;
	private CCombo wSuccessCondition;
	private FormData fdlSuccessCondition, fdSuccessCondition;
	
	
	private Label wlNrErrorsLessThan;
	private TextVar wNrErrorsLessThan;
	private FormData fdlNrErrorsLessThan, fdNrErrorsLessThan;
	
	private Label        wlDoNotProcessRest;
	private Button       wDoNotProcessRest;
	private FormData     fdlDoNotProcessRest, fdDoNotProcessRest;
	
	private Group wDestinationFile;
	private FormData fdDestinationFile;
	
	private Label        wlAddDate;
	private Button       wAddDate;
	private FormData     fdlAddDate, fdAddDate;

	private Label        wlAddTime;
	private Button       wAddTime;
	private FormData     fdlAddTime, fdAddTime;
	
	private Label        wlSpecifyFormat;
	private Button       wSpecifyFormat;
	private FormData     fdlSpecifyFormat, fdSpecifyFormat;

  	private Label        wlDateTimeFormat;
	private CCombo       wDateTimeFormat;
	private FormData     fdlDateTimeFormat, fdDateTimeFormat; 
	
	private Label        wlAddDateBeforeExtension;
	private Button       wAddDateBeforeExtension;
	private FormData     fdlAddDateBeforeExtension, fdAddDateBeforeExtension;
	
	private Label        wlDoNotKeepFolderStructure;
	private Button       wDoNotKeepFolderStructure;
	private FormData     fdlDoNotKeepFolderStructure, fdDoNotKeepFolderStructure;
	
	
   public JobEntryMoveFilesDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryMoveFiles) jobEntryInt;

		if (this.jobEntry.getName() == null) 
			this.jobEntry.setName(Messages.getString("JobMoveFiles.Name.Default"));
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

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("JobMoveFiles.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Filename line
		wlName=new Label(shell, SWT.RIGHT);
		wlName.setText(Messages.getString("JobMoveFiles.Name.Label"));
		props.setLook(wlName);
		fdlName=new FormData();
		fdlName.left = new FormAttachment(0, 0);
		fdlName.right= new FormAttachment(middle, -margin);
		fdlName.top  = new FormAttachment(0, margin);
		wlName.setLayoutData(fdlName);
		wName=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wName);
		wName.addModifyListener(lsMod);
		fdName=new FormData();
		fdName.left = new FormAttachment(middle, 0);
		fdName.top  = new FormAttachment(0, margin);
		fdName.right= new FormAttachment(100, 0);
		wName.setLayoutData(fdName);
		
		
		
		  
        wTabFolder = new CTabFolder(shell, SWT.BORDER);
 		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);
 		
 		//////////////////////////
		// START OF GENERAL TAB   ///
		//////////////////////////
		
		
		
		wGeneralTab=new CTabItem(wTabFolder, SWT.NONE);
		wGeneralTab.setText(Messages.getString("JobMoveFiles.Tab.General.Label"));
		
		wGeneralComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wGeneralComp);

		FormLayout generalLayout = new FormLayout();
		generalLayout.marginWidth  = 3;
		generalLayout.marginHeight = 3;
		wGeneralComp.setLayout(generalLayout);
		

		// SETTINGS grouping?
		// ////////////////////////
		// START OF SETTINGS GROUP
		// 

		wSettings = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(wSettings);
		wSettings.setText(Messages.getString("JobMoveFiles.Settings.Label"));

		FormLayout groupLayout = new FormLayout();
		groupLayout.marginWidth = 10;
		groupLayout.marginHeight = 10;
		wSettings.setLayout(groupLayout);
		
		wlIncludeSubfolders = new Label(wSettings, SWT.RIGHT);
		wlIncludeSubfolders.setText(Messages.getString("JobMoveFiles.IncludeSubfolders.Label"));
		props.setLook(wlIncludeSubfolders);
		fdlIncludeSubfolders = new FormData();
		fdlIncludeSubfolders.left = new FormAttachment(0, 0);
		fdlIncludeSubfolders.top = new FormAttachment(wName, margin);
		fdlIncludeSubfolders.right = new FormAttachment(middle, -margin);
		wlIncludeSubfolders.setLayoutData(fdlIncludeSubfolders);
		wIncludeSubfolders = new Button(wSettings, SWT.CHECK);
		props.setLook(wIncludeSubfolders);
		wIncludeSubfolders.setToolTipText(Messages.getString("JobMoveFiles.IncludeSubfolders.Tooltip"));
		fdIncludeSubfolders = new FormData();
		fdIncludeSubfolders.left = new FormAttachment(middle, 0);
		fdIncludeSubfolders.top = new FormAttachment(wName, margin);
		fdIncludeSubfolders.right = new FormAttachment(100, 0);
		wIncludeSubfolders.setLayoutData(fdIncludeSubfolders);
		wIncludeSubfolders.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
				CheckIncludeSubFolders();
			}
		});
		
		// Destination is a file?
		wlDestinationIsAFile = new Label(wSettings, SWT.RIGHT);
		wlDestinationIsAFile.setText(Messages.getString("JobMoveFiles.DestinationIsAFile.Label"));
		props.setLook(wlDestinationIsAFile);
		fdlDestinationIsAFile = new FormData();
		fdlDestinationIsAFile.left = new FormAttachment(0, 0);
		fdlDestinationIsAFile.top = new FormAttachment(wIncludeSubfolders, margin);
		fdlDestinationIsAFile.right = new FormAttachment(middle, -margin);
		wlDestinationIsAFile.setLayoutData(fdlDestinationIsAFile);
		wDestinationIsAFile = new Button(wSettings, SWT.CHECK);
		props.setLook(wDestinationIsAFile);
		wDestinationIsAFile.setToolTipText(Messages.getString("JobMoveFiles.DestinationIsAFile.Tooltip"));
		fdDestinationIsAFile = new FormData();
		fdDestinationIsAFile.left = new FormAttachment(middle, 0);
		fdDestinationIsAFile.top = new FormAttachment(wIncludeSubfolders, margin);
		fdDestinationIsAFile.right = new FormAttachment(100, 0);
		wDestinationIsAFile.setLayoutData(fdDestinationIsAFile);
		wDestinationIsAFile.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
			}
		});
		
		// Copy empty folders
		wlMoveEmptyFolders = new Label(wSettings, SWT.RIGHT);
		wlMoveEmptyFolders.setText(Messages.getString("JobMoveFiles.MoveEmptyFolders.Label"));
		props.setLook(wlMoveEmptyFolders);
		fdlMoveEmptyFolders = new FormData();
		fdlMoveEmptyFolders.left = new FormAttachment(0, 0);
		fdlMoveEmptyFolders.top = new FormAttachment(wDestinationIsAFile, margin);
		fdlMoveEmptyFolders.right = new FormAttachment(middle, -margin);
		wlMoveEmptyFolders.setLayoutData(fdlMoveEmptyFolders);
		wMoveEmptyFolders = new Button(wSettings, SWT.CHECK);
		props.setLook(wMoveEmptyFolders);
		wMoveEmptyFolders.setToolTipText(Messages.getString("JobMoveFiles.MoveEmptyFolders.Tooltip"));
		fdMoveEmptyFolders = new FormData();
		fdMoveEmptyFolders.left = new FormAttachment(middle, 0);
		fdMoveEmptyFolders.top = new FormAttachment(wDestinationIsAFile, margin);
		fdMoveEmptyFolders.right = new FormAttachment(100, 0);
		wMoveEmptyFolders.setLayoutData(fdMoveEmptyFolders);
		wMoveEmptyFolders.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
			}
		});
		
		// Create destination folder/parent folder
		wlCreateDestinationFolder = new Label(wSettings, SWT.RIGHT);
		wlCreateDestinationFolder.setText(Messages.getString("JobMoveFiles.CreateDestinationFolder.Label"));
		props.setLook(wlCreateDestinationFolder);
		fdlCreateDestinationFolder = new FormData();
		fdlCreateDestinationFolder.left = new FormAttachment(0, 0);
		fdlCreateDestinationFolder.top = new FormAttachment(wMoveEmptyFolders, margin);
		fdlCreateDestinationFolder.right = new FormAttachment(middle, -margin);
		wlCreateDestinationFolder.setLayoutData(fdlCreateDestinationFolder);
		wCreateDestinationFolder = new Button(wSettings, SWT.CHECK);
		props.setLook(wCreateDestinationFolder);
		wCreateDestinationFolder.setToolTipText(Messages.getString("JobMoveFiles.CreateDestinationFolder.Tooltip"));
		fdCreateDestinationFolder = new FormData();
		fdCreateDestinationFolder.left = new FormAttachment(middle, 0);
		fdCreateDestinationFolder.top = new FormAttachment(wMoveEmptyFolders, margin);
		fdCreateDestinationFolder.right = new FormAttachment(100, 0);
		wCreateDestinationFolder.setLayoutData(fdCreateDestinationFolder);
		wCreateDestinationFolder.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
			}
		});
		
		// OverwriteFiles Option 
		wlOverwriteFiles = new Label(wSettings, SWT.RIGHT);
		wlOverwriteFiles.setText(Messages.getString("JobMoveFiles.OverwriteFiles.Label"));
		props.setLook(wlOverwriteFiles);
		fdlOverwriteFiles = new FormData();
		fdlOverwriteFiles.left = new FormAttachment(0, 0);
		fdlOverwriteFiles.top = new FormAttachment(wCreateDestinationFolder, margin);
		fdlOverwriteFiles.right = new FormAttachment(middle, -margin);
		wlOverwriteFiles.setLayoutData(fdlOverwriteFiles);
		wOverwriteFiles = new Button(wSettings, SWT.CHECK);
		props.setLook(wOverwriteFiles);
		wOverwriteFiles.setToolTipText(Messages.getString("JobMoveFiles.OverwriteFiles.Tooltip"));
		fdOverwriteFiles = new FormData();
		fdOverwriteFiles.left = new FormAttachment(middle, 0);
		fdOverwriteFiles.top = new FormAttachment(wCreateDestinationFolder, margin);
		fdOverwriteFiles.right = new FormAttachment(100, 0);
		wOverwriteFiles.setLayoutData(fdOverwriteFiles);
		wOverwriteFiles.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
			}
		});
		
		wlPrevious = new Label(wSettings, SWT.RIGHT);
		wlPrevious.setText(Messages.getString("JobMoveFiles.Previous.Label"));
		props.setLook(wlPrevious);
		fdlPrevious = new FormData();
		fdlPrevious.left = new FormAttachment(0, 0);
		fdlPrevious.top = new FormAttachment(wOverwriteFiles, margin );
		fdlPrevious.right = new FormAttachment(middle, -margin);
		wlPrevious.setLayoutData(fdlPrevious);
		wPrevious = new Button(wSettings, SWT.CHECK);
		props.setLook(wPrevious);
		wPrevious.setSelection(jobEntry.arg_from_previous);
		wPrevious.setToolTipText(Messages.getString("JobMoveFiles.Previous.Tooltip"));
		fdPrevious = new FormData();
		fdPrevious.left = new FormAttachment(middle, 0);
		fdPrevious.top = new FormAttachment(wOverwriteFiles, margin );
		fdPrevious.right = new FormAttachment(100, 0);
		wPrevious.setLayoutData(fdPrevious);
		wPrevious.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{

				RefreshArgFromPrevious();				
				
			}
		});
		fdSettings = new FormData();
		fdSettings.left = new FormAttachment(0, margin);
		fdSettings.top = new FormAttachment(wName, margin);
		fdSettings.right = new FormAttachment(100, -margin);
		wSettings.setLayoutData(fdSettings);
		
		// ///////////////////////////////////////////////////////////
		// / END OF SETTINGS GROUP
		// ///////////////////////////////////////////////////////////

		// SourceFileFolder line
		wlSourceFileFolder=new Label(wGeneralComp, SWT.RIGHT);
		wlSourceFileFolder.setText(Messages.getString("JobMoveFiles.SourceFileFolder.Label"));
		props.setLook(wlSourceFileFolder);
		fdlSourceFileFolder=new FormData();
		fdlSourceFileFolder.left = new FormAttachment(0, 0);
		fdlSourceFileFolder.top  = new FormAttachment(wSettings, 2*margin);
		fdlSourceFileFolder.right= new FormAttachment(middle, -margin);
		wlSourceFileFolder.setLayoutData(fdlSourceFileFolder);

		// Browse Source folders button ...
		wbSourceDirectory=new Button(wGeneralComp, SWT.PUSH| SWT.CENTER);
		props.setLook(wbSourceDirectory);
		wbSourceDirectory.setText(Messages.getString("JobMoveFiles.BrowseFolders.Label"));
		fdbSourceDirectory=new FormData();
		fdbSourceDirectory.right= new FormAttachment(100, 0);
		fdbSourceDirectory.top  = new FormAttachment(wSettings, margin);
		wbSourceDirectory.setLayoutData(fdbSourceDirectory);
		
		wbSourceDirectory.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					DirectoryDialog ddialog = new DirectoryDialog(shell, SWT.OPEN);
					if (wSourceFileFolder.getText()!=null)
					{
						ddialog.setFilterPath(jobMeta.environmentSubstitute(wSourceFileFolder.getText()) );
					}
					
					 // Calling open() will open and run the dialog.
			        // It will return the selected directory, or
			        // null if user cancels
			        String dir = ddialog.open();
			        if (dir != null) {
			          // Set the text box to the new selection
			        	wSourceFileFolder.setText(dir);
			        }
					
				}
			}
		);
		
		// Browse Source files button ...
		wbSourceFileFolder=new Button(wGeneralComp, SWT.PUSH| SWT.CENTER);
		props.setLook(wbSourceFileFolder);
		wbSourceFileFolder.setText(Messages.getString("JobMoveFiles.BrowseFiles.Label"));
		fdbSourceFileFolder=new FormData();
		fdbSourceFileFolder.right= new FormAttachment(wbSourceDirectory, -margin);
		fdbSourceFileFolder.top  = new FormAttachment(wSettings, margin);
		wbSourceFileFolder.setLayoutData(fdbSourceFileFolder);
		
		// Browse Destination file add button ...
		wbaSourceFileFolder=new Button(wGeneralComp, SWT.PUSH| SWT.CENTER);
		props.setLook(wbaSourceFileFolder);
		wbaSourceFileFolder.setText(Messages.getString("JobMoveFiles.FilenameAdd.Button"));
		fdbaSourceFileFolder=new FormData();
		fdbaSourceFileFolder.right= new FormAttachment(wbSourceFileFolder, -margin);
		fdbaSourceFileFolder.top  = new FormAttachment(wSettings, margin);
		wbaSourceFileFolder.setLayoutData(fdbaSourceFileFolder);

		wSourceFileFolder=new TextVar(jobMeta, wGeneralComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wSourceFileFolder.setToolTipText(Messages.getString("JobMoveFiles.SourceFileFolder.Tooltip"));
		
		props.setLook(wSourceFileFolder);
		wSourceFileFolder.addModifyListener(lsMod);
		fdSourceFileFolder=new FormData();
		fdSourceFileFolder.left = new FormAttachment(middle, 0);
		fdSourceFileFolder.top  = new FormAttachment(wSettings, 2*margin);
		fdSourceFileFolder.right= new FormAttachment(wbSourceFileFolder, -55);
		wSourceFileFolder.setLayoutData(fdSourceFileFolder);

		// Whenever something changes, set the tooltip to the expanded version:
		wSourceFileFolder.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				wSourceFileFolder.setToolTipText(jobMeta.environmentSubstitute(wSourceFileFolder.getText() ) );
			}
		}
			);

		wbSourceFileFolder.addSelectionListener
			(
			new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
				dialog.setFilterExtensions(new String[] {"*"});
				if (wSourceFileFolder.getText()!=null)
				{
					dialog.setFileName(jobMeta.environmentSubstitute(wSourceFileFolder.getText()) );
				}
				dialog.setFilterNames(FILETYPES);
				if (dialog.open()!=null)
				{
					wSourceFileFolder.setText(dialog.getFilterPath()+Const.FILE_SEPARATOR+dialog.getFileName());
				}
			}
		}
			);
		
		// Destination
		wlDestinationFileFolder = new Label(wGeneralComp, SWT.RIGHT);
		wlDestinationFileFolder.setText(Messages.getString("JobMoveFiles.DestinationFileFolder.Label"));
		props.setLook(wlDestinationFileFolder);
		fdlDestinationFileFolder = new FormData();
		fdlDestinationFileFolder.left = new FormAttachment(0, 0);
		fdlDestinationFileFolder.top = new FormAttachment(wSourceFileFolder, margin);
		fdlDestinationFileFolder.right = new FormAttachment(middle, -margin);
		wlDestinationFileFolder.setLayoutData(fdlDestinationFileFolder);
		
		
		
		// Browse Destination folders button ...
		wbDestinationDirectory=new Button(wGeneralComp, SWT.PUSH| SWT.CENTER);
		props.setLook(wbDestinationDirectory);
		wbDestinationDirectory.setText(Messages.getString("JobMoveFiles.BrowseFolders.Label"));
		fdbDestinationDirectory=new FormData();
		fdbDestinationDirectory.right= new FormAttachment(100, 0);
		fdbDestinationDirectory.top  = new FormAttachment(wSourceFileFolder, margin);
		wbDestinationDirectory.setLayoutData(fdbDestinationDirectory);
		
		
		wbDestinationDirectory.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					DirectoryDialog ddialog = new DirectoryDialog(shell, SWT.OPEN);
					if (wDestinationFileFolder.getText()!=null)
					{
						ddialog.setFilterPath(jobMeta.environmentSubstitute(wDestinationFileFolder.getText()) );
					}
					
					 // Calling open() will open and run the dialog.
			        // It will return the selected directory, or
			        // null if user cancels
			        String dir = ddialog.open();
			        if (dir != null) {
			          // Set the text box to the new selection
			        	wDestinationFileFolder.setText(dir);
			        }
					
				}
			}
		);

		
		
		
		
		// Browse Destination file browse button ...
		wbDestinationFileFolder=new Button(wGeneralComp, SWT.PUSH| SWT.CENTER);
		props.setLook(wbDestinationFileFolder);
		wbDestinationFileFolder.setText(Messages.getString("JobMoveFiles.BrowseFiles.Label"));
		fdbDestinationFileFolder=new FormData();
		fdbDestinationFileFolder.right= new FormAttachment(wbDestinationDirectory, -margin);
		fdbDestinationFileFolder.top  = new FormAttachment(wSourceFileFolder, margin);
		wbDestinationFileFolder.setLayoutData(fdbDestinationFileFolder);
		
				
		
		wDestinationFileFolder = new TextVar(jobMeta, wGeneralComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER); 
		wDestinationFileFolder.setToolTipText(Messages.getString("JobMoveFiles.DestinationFileFolder.Tooltip"));
		props.setLook(wDestinationFileFolder);
		wDestinationFileFolder.addModifyListener(lsMod);
		fdDestinationFileFolder = new FormData();
		fdDestinationFileFolder.left = new FormAttachment(middle, 0);
		fdDestinationFileFolder.top = new FormAttachment(wSourceFileFolder, margin);
		fdDestinationFileFolder.right= new FormAttachment(wbSourceFileFolder, -55);
		wDestinationFileFolder.setLayoutData(fdDestinationFileFolder);
		
		wbDestinationFileFolder.addSelectionListener
			(
			new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
				dialog.setFilterExtensions(new String[] {"*"});
				if (wDestinationFileFolder.getText()!=null)
				{
					dialog.setFileName(jobMeta.environmentSubstitute(wDestinationFileFolder.getText()) );
				}
				dialog.setFilterNames(FILETYPES);
				if (dialog.open()!=null)
				{
					wDestinationFileFolder.setText(dialog.getFilterPath()+Const.FILE_SEPARATOR+dialog.getFileName());
				}
			}
		}
			);

		// Buttons to the right of the screen...
		wbdSourceFileFolder=new Button(wGeneralComp, SWT.PUSH| SWT.CENTER);
		props.setLook(wbdSourceFileFolder);
		wbdSourceFileFolder.setText(Messages.getString("JobMoveFiles.FilenameDelete.Button"));
		wbdSourceFileFolder.setToolTipText(Messages.getString("JobMoveFiles.FilenameDelete.Tooltip"));
		fdbdSourceFileFolder=new FormData();
		fdbdSourceFileFolder.right = new FormAttachment(100, 0);
		fdbdSourceFileFolder.top  = new FormAttachment (wDestinationFileFolder, 40);
		wbdSourceFileFolder.setLayoutData(fdbdSourceFileFolder);

		wbeSourceFileFolder=new Button(wGeneralComp, SWT.PUSH| SWT.CENTER);
		props.setLook(wbeSourceFileFolder);
		wbeSourceFileFolder.setText(Messages.getString("JobMoveFiles.FilenameEdit.Button"));
		wbeSourceFileFolder.setToolTipText(Messages.getString("JobMoveFiles.FilenameEdit.Tooltip"));
		fdbeSourceFileFolder=new FormData();
		fdbeSourceFileFolder.right = new FormAttachment(100, 0);
		fdbeSourceFileFolder.left = new FormAttachment(wbdSourceFileFolder, 0, SWT.LEFT);
		fdbeSourceFileFolder.top  = new FormAttachment (wbdSourceFileFolder, margin);
		wbeSourceFileFolder.setLayoutData(fdbeSourceFileFolder);
		
		
		
		// Wildcard
		wlWildcard = new Label(wGeneralComp, SWT.RIGHT);
		wlWildcard.setText(Messages.getString("JobMoveFiles.Wildcard.Label"));
		props.setLook(wlWildcard);
		fdlWildcard = new FormData();
		fdlWildcard.left = new FormAttachment(0, 0);
		fdlWildcard.top = new FormAttachment(wDestinationFileFolder, margin);
		fdlWildcard.right = new FormAttachment(middle, -margin);
		wlWildcard.setLayoutData(fdlWildcard);
		
		wWildcard = new TextVar(jobMeta, wGeneralComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wWildcard.setToolTipText(Messages.getString("JobMoveFiles.Wildcard.Tooltip"));
		props.setLook(wWildcard);
		wWildcard.addModifyListener(lsMod);
		fdWildcard = new FormData();
		fdWildcard.left = new FormAttachment(middle, 0);
		fdWildcard.top = new FormAttachment(wDestinationFileFolder, margin);
		fdWildcard.right= new FormAttachment(wbSourceFileFolder, -55);
		wWildcard.setLayoutData(fdWildcard);

		wlFields = new Label(wGeneralComp, SWT.NONE);
		wlFields.setText(Messages.getString("JobMoveFiles.Fields.Label"));
		props.setLook(wlFields);
		fdlFields = new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.right= new FormAttachment(middle, -margin);
		fdlFields.top = new FormAttachment(wWildcard,margin);
		wlFields.setLayoutData(fdlFields);

		int rows = jobEntry.source_filefolder == null
			? 1
			: (jobEntry.source_filefolder.length == 0
			? 0
			: jobEntry.source_filefolder.length);
		final int FieldsRows = rows;

		ColumnInfo[] colinf=new ColumnInfo[]
			{
				new ColumnInfo(Messages.getString("JobMoveFiles.Fields.SourceFileFolder.Label"),  ColumnInfo.COLUMN_TYPE_TEXT,    false),
				new ColumnInfo(Messages.getString("JobMoveFiles.Fields.DestinationFileFolder.Label"),  ColumnInfo.COLUMN_TYPE_TEXT,    false),
				new ColumnInfo(Messages.getString("JobMoveFiles.Fields.Wildcard.Label"), ColumnInfo.COLUMN_TYPE_TEXT,    false ),
			};

		colinf[0].setUsingVariables(true);
		colinf[0].setToolTip(Messages.getString("JobMoveFiles.Fields.SourceFileFolder.Tooltip"));
		colinf[1].setUsingVariables(true);
		colinf[1].setToolTip(Messages.getString("JobMoveFiles.Fields.DestinationFileFolder.Tooltip"));
		colinf[2].setUsingVariables(true);
		colinf[2].setToolTip(Messages.getString("JobMoveFiles.Fields.Wildcard.Tooltip"));

		wFields = new TableView(jobMeta, wGeneralComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf,	FieldsRows, lsMod, props);

		fdFields = new FormData();
		fdFields.left = new FormAttachment(0, 0);
		fdFields.top = new FormAttachment(wlFields, margin);
		fdFields.right = new FormAttachment(100, -75);
		fdFields.bottom = new FormAttachment(100, -margin);
		wFields.setLayoutData(fdFields);

		RefreshArgFromPrevious();

		// Add the file to the list of files...
		SelectionAdapter selA = new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				wFields.add(new String[] { wSourceFileFolder.getText(), wDestinationFileFolder.getText(), wWildcard.getText() } );
				wSourceFileFolder.setText("");
				wDestinationFileFolder.setText("");
				wWildcard.setText("");
				wFields.removeEmptyRows();
				wFields.setRowNums();
				wFields.optWidth(true);
			}
		};
		wbaSourceFileFolder.addSelectionListener(selA);
		wSourceFileFolder.addSelectionListener(selA);

		// Delete files from the list of files...
		wbdSourceFileFolder.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				int idx[] = wFields.getSelectionIndices();
				wFields.remove(idx);
				wFields.removeEmptyRows();
				wFields.setRowNums();
			}
		});

		// Edit the selected file & remove from the list...
		wbeSourceFileFolder.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				int idx = wFields.getSelectionIndex();
				if (idx>=0)
				{
					String string[] = wFields.getItem(idx);
					wSourceFileFolder.setText(string[0]);
					wDestinationFileFolder.setText(string[1]);
					wWildcard.setText(string[2]);
					wFields.remove(idx);
				}
				wFields.removeEmptyRows();
				wFields.setRowNums();
			}
		});
		
		
		

		fdGeneralComp=new FormData();
		fdGeneralComp.left  = new FormAttachment(0, 0);
		fdGeneralComp.top   = new FormAttachment(0, 0);
		fdGeneralComp.right = new FormAttachment(100, 0);
		fdGeneralComp.bottom= new FormAttachment(100, 0);
		wGeneralComp.setLayoutData(fdGeneralComp);
		
		wGeneralComp.layout();
		wGeneralTab.setControl(wGeneralComp);
 		props.setLook(wGeneralComp);
 		
 		
 		
		/////////////////////////////////////////////////////////////
		/// END OF GENERAL TAB
		/////////////////////////////////////////////////////////////
		
 		
        
 		//////////////////////////////////////
		// START OF ADVANCED  TAB   ///
		/////////////////////////////////////
		
		
		
		wAdvancedTab=new CTabItem(wTabFolder, SWT.NONE);
		wAdvancedTab.setText(Messages.getString("JobMoveFiles.Tab.Advanced.Label"));

		FormLayout contentLayout = new FormLayout ();
		contentLayout.marginWidth  = 3;
		contentLayout.marginHeight = 3;
		
		wAdvancedComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wAdvancedComp);
 		wAdvancedComp.setLayout(contentLayout);
 		
 		
 	// DestinationFile grouping?
		// ////////////////////////
		// START OF DestinationFile GROUP
		// 

		wDestinationFile = new Group(wAdvancedComp, SWT.SHADOW_NONE);
		props.setLook(wDestinationFile);
		wDestinationFile.setText(Messages.getString("JobMoveFiles.GroupDestinationFile.Label"));

		FormLayout groupLayoutFile = new FormLayout();
		groupLayoutFile.marginWidth = 10;
		groupLayoutFile.marginHeight = 10;
		wDestinationFile.setLayout(groupLayoutFile);
		
		// Do not keep folder structure?
		wlDoNotKeepFolderStructure=new Label(wDestinationFile, SWT.RIGHT);
		wlDoNotKeepFolderStructure.setText(Messages.getString("JobMoveFiles.DoNotKeepFolderStructure.Label"));
 		props.setLook(wlDoNotKeepFolderStructure);
		fdlDoNotKeepFolderStructure=new FormData();
		fdlDoNotKeepFolderStructure.left = new FormAttachment(0, 0);
		fdlDoNotKeepFolderStructure.top  = new FormAttachment(0, margin);
		fdlDoNotKeepFolderStructure.right= new FormAttachment(middle, -margin);
		wlDoNotKeepFolderStructure.setLayoutData(fdlDoNotKeepFolderStructure);
		wDoNotKeepFolderStructure=new Button(wDestinationFile, SWT.CHECK);
 		props.setLook(wDoNotKeepFolderStructure);
 		wDoNotKeepFolderStructure.setToolTipText(Messages.getString("JobMoveFiles.DoNotKeepFolderStructure.Tooltip"));
		fdDoNotKeepFolderStructure=new FormData();
		fdDoNotKeepFolderStructure.left = new FormAttachment(middle, 0);
		fdDoNotKeepFolderStructure.top  = new FormAttachment(0, margin);
		fdDoNotKeepFolderStructure.right= new FormAttachment(100, 0);
		wDoNotKeepFolderStructure.setLayoutData(fdDoNotKeepFolderStructure);
		wDoNotKeepFolderStructure.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					jobEntry.setChanged();
				}
			}
		);
		
		// Create multi-part file?
		wlAddDate=new Label(wDestinationFile, SWT.RIGHT);
		wlAddDate.setText(Messages.getString("JobMoveFiles.AddDate.Label"));
 		props.setLook(wlAddDate);
		fdlAddDate=new FormData();
		fdlAddDate.left = new FormAttachment(0, 0);
		fdlAddDate.top  = new FormAttachment(wDoNotKeepFolderStructure, margin);
		fdlAddDate.right= new FormAttachment(middle, -margin);
		wlAddDate.setLayoutData(fdlAddDate);
		wAddDate=new Button(wDestinationFile, SWT.CHECK);
 		props.setLook(wAddDate);
 		wAddDate.setToolTipText(Messages.getString("JobMoveFiles.AddDate.Tooltip"));
		fdAddDate=new FormData();
		fdAddDate.left = new FormAttachment(middle, 0);
		fdAddDate.top  = new FormAttachment(wDoNotKeepFolderStructure, margin);
		fdAddDate.right= new FormAttachment(100, 0);
		wAddDate.setLayoutData(fdAddDate);
		wAddDate.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					jobEntry.setChanged();
					setAddDateBeforeExtension();
				}
			}
		);
		// Create multi-part file?
		wlAddTime=new Label(wDestinationFile, SWT.RIGHT);
		wlAddTime.setText(Messages.getString("JobMoveFiles.AddTime.Label"));
 		props.setLook(wlAddTime);
		fdlAddTime=new FormData();
		fdlAddTime.left = new FormAttachment(0, 0);
		fdlAddTime.top  = new FormAttachment(wAddDate, margin);
		fdlAddTime.right= new FormAttachment(middle, -margin);
		wlAddTime.setLayoutData(fdlAddTime);
		wAddTime=new Button(wDestinationFile, SWT.CHECK);
 		props.setLook(wAddTime);
 		wAddTime.setToolTipText(Messages.getString("JobMoveFiles.AddTime.Tooltip"));
		fdAddTime=new FormData();
		fdAddTime.left = new FormAttachment(middle, 0);
		fdAddTime.top  = new FormAttachment(wAddDate, margin);
		fdAddTime.right= new FormAttachment(100, 0);
		wAddTime.setLayoutData(fdAddTime);
		wAddTime.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					jobEntry.setChanged();
					setAddDateBeforeExtension();
				}
			}
		);

		// Specify date time format?
		wlSpecifyFormat=new Label(wDestinationFile, SWT.RIGHT);
		wlSpecifyFormat.setText(Messages.getString("JobMoveFiles.SpecifyFormat.Label"));
		props.setLook(wlSpecifyFormat);
		fdlSpecifyFormat=new FormData();
		fdlSpecifyFormat.left = new FormAttachment(0, 0);
		fdlSpecifyFormat.top  = new FormAttachment(wAddTime, margin);
		fdlSpecifyFormat.right= new FormAttachment(middle, -margin);
		wlSpecifyFormat.setLayoutData(fdlSpecifyFormat);
		wSpecifyFormat=new Button(wDestinationFile, SWT.CHECK);
		props.setLook(wSpecifyFormat);
		wSpecifyFormat.setToolTipText(Messages.getString("JobMoveFiles.SpecifyFormat.Tooltip"));
	    fdSpecifyFormat=new FormData();
		fdSpecifyFormat.left = new FormAttachment(middle, 0);
		fdSpecifyFormat.top  = new FormAttachment(wAddTime, margin);
		fdSpecifyFormat.right= new FormAttachment(100, 0);
		wSpecifyFormat.setLayoutData(fdSpecifyFormat);
		wSpecifyFormat.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					jobEntry.setChanged();
					setDateTimeFormat();
					setAddDateBeforeExtension();
				}
			}
		);

		
		//	Prepare a list of possible DateTimeFormats...
		String dats[] = Const.getDateFormats();
		
 		// DateTimeFormat
		wlDateTimeFormat=new Label(wDestinationFile, SWT.RIGHT);
        wlDateTimeFormat.setText(Messages.getString("JobMoveFiles.DateTimeFormat.Label"));
        props.setLook(wlDateTimeFormat);
        fdlDateTimeFormat=new FormData();
        fdlDateTimeFormat.left = new FormAttachment(0, 0);
        fdlDateTimeFormat.top  = new FormAttachment(wSpecifyFormat, margin);
        fdlDateTimeFormat.right= new FormAttachment(middle, -margin);
        wlDateTimeFormat.setLayoutData(fdlDateTimeFormat);
        wDateTimeFormat=new CCombo(wDestinationFile, SWT.BORDER | SWT.READ_ONLY);
        wDateTimeFormat.setEditable(true);
        props.setLook(wDateTimeFormat);
        wDateTimeFormat.addModifyListener(lsMod);
        fdDateTimeFormat=new FormData();
        fdDateTimeFormat.left = new FormAttachment(middle, 0);
        fdDateTimeFormat.top  = new FormAttachment(wSpecifyFormat, margin);
        fdDateTimeFormat.right= new FormAttachment(100, 0);
        wDateTimeFormat.setLayoutData(fdDateTimeFormat);
        for (int x=0;x<dats.length;x++) wDateTimeFormat.add(dats[x]);
        
        

        // Add Date before extension?
        wlAddDateBeforeExtension = new Label(wDestinationFile, SWT.RIGHT);
        wlAddDateBeforeExtension.setText(Messages.getString("JobMoveFiles.AddDateBeforeExtension.Label"));
        props.setLook(wlAddDateBeforeExtension);
        fdlAddDateBeforeExtension = new FormData();
        fdlAddDateBeforeExtension.left = new FormAttachment(0, 0);
        fdlAddDateBeforeExtension.top = new FormAttachment(wDateTimeFormat, margin);
        fdlAddDateBeforeExtension.right = new FormAttachment(middle, -margin);
        wlAddDateBeforeExtension.setLayoutData(fdlAddDateBeforeExtension);
        wAddDateBeforeExtension = new Button(wDestinationFile, SWT.CHECK);
        props.setLook(wAddDateBeforeExtension);
        wAddDateBeforeExtension.setToolTipText(Messages.getString("JobMoveFiles.AddDateBeforeExtension.Tooltip"));
        fdAddDateBeforeExtension = new FormData();
        fdAddDateBeforeExtension.left = new FormAttachment(middle, 0);
        fdAddDateBeforeExtension.top = new FormAttachment(wDateTimeFormat, margin);
        fdAddDateBeforeExtension.right = new FormAttachment(100, 0);
        wAddDateBeforeExtension.setLayoutData(fdAddDateBeforeExtension);
        wAddDateBeforeExtension.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
            }
        });
		
		
		
		fdDestinationFile = new FormData();
		fdDestinationFile.left = new FormAttachment(0, margin);
		fdDestinationFile.top = new FormAttachment(wName, margin);
		fdDestinationFile.right = new FormAttachment(100, -margin);
		wDestinationFile.setLayoutData(fdDestinationFile);
		
		// ///////////////////////////////////////////////////////////
		// / END OF DestinationFile GROUP
		// ///////////////////////////////////////////////////////////

 		
 		
		 // SuccessOngrouping?
	     // ////////////////////////
	     // START OF SUCCESS ON GROUP///
	     // /
	    wSuccessOn= new Group(wAdvancedComp, SWT.SHADOW_NONE);
	    props.setLook(wSuccessOn);
	    wSuccessOn.setText(Messages.getString("JobMoveFiles.SuccessOn.Group.Label"));

	    FormLayout successongroupLayout = new FormLayout();
	    successongroupLayout.marginWidth = 10;
	    successongroupLayout.marginHeight = 10;

	    wSuccessOn.setLayout(successongroupLayout);
	    

	    //Success Condition
	  	wlSuccessCondition = new Label(wSuccessOn, SWT.RIGHT);
	  	wlSuccessCondition.setText(Messages.getString("JobMoveFiles.SuccessCondition.Label"));
	  	props.setLook(wlSuccessCondition);
	  	fdlSuccessCondition = new FormData();
	  	fdlSuccessCondition.left = new FormAttachment(0, 0);
	  	fdlSuccessCondition.right = new FormAttachment(middle, 0);
	  	fdlSuccessCondition.top = new FormAttachment(wDestinationFile, 2*margin);
	  	wlSuccessCondition.setLayoutData(fdlSuccessCondition);
	  	wSuccessCondition = new CCombo(wSuccessOn, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
	  	wSuccessCondition.add(Messages.getString("JobMoveFiles.SuccessWhenAllWorksFine.Label"));
	  	wSuccessCondition.add(Messages.getString("JobMoveFiles.SuccessWhenErrorsLessThan.Label"));
	  	wSuccessCondition.select(0); // +1: starts at -1
	  	
		props.setLook(wSuccessCondition);
		fdSuccessCondition= new FormData();
		fdSuccessCondition.left = new FormAttachment(middle, 0);
		fdSuccessCondition.top = new FormAttachment(wDestinationFile, 2*margin);
		fdSuccessCondition.right = new FormAttachment(100, 0);
		wSuccessCondition.setLayoutData(fdSuccessCondition);
		wSuccessCondition.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				activeSuccessCondition();
				
			}
		});

		// Success when number of errors less than
		wlNrErrorsLessThan= new Label(wSuccessOn, SWT.RIGHT);
		wlNrErrorsLessThan.setText(Messages.getString("JobMoveFiles.NrErrorsLessThan.Label"));
		props.setLook(wlNrErrorsLessThan);
		fdlNrErrorsLessThan= new FormData();
		fdlNrErrorsLessThan.left = new FormAttachment(0, 0);
		fdlNrErrorsLessThan.top = new FormAttachment(wSuccessCondition, margin);
		fdlNrErrorsLessThan.right = new FormAttachment(middle, -margin);
		wlNrErrorsLessThan.setLayoutData(fdlNrErrorsLessThan);
		
		
		wNrErrorsLessThan= new TextVar(jobMeta,wSuccessOn, SWT.SINGLE | SWT.LEFT | SWT.BORDER, Messages
			.getString("JobMoveFiles.NrErrorsLessThan.Tooltip"));
		props.setLook(wNrErrorsLessThan);
		wNrErrorsLessThan.addModifyListener(lsMod);
		fdNrErrorsLessThan= new FormData();
		fdNrErrorsLessThan.left = new FormAttachment(middle, 0);
		fdNrErrorsLessThan.top = new FormAttachment(wSuccessCondition, margin);
		fdNrErrorsLessThan.right = new FormAttachment(100, -margin);
		wNrErrorsLessThan.setLayoutData(fdNrErrorsLessThan);
		
		

		// Do Not process rest of files
		wlDoNotProcessRest = new Label(wSuccessOn, SWT.RIGHT);
		wlDoNotProcessRest.setText(Messages.getString("JobMoveFiles.DoNotProcessRest.Label"));
		props.setLook(wlDoNotProcessRest);
		fdlDoNotProcessRest = new FormData();
		fdlDoNotProcessRest.left = new FormAttachment(0, 0);
		fdlDoNotProcessRest.top = new FormAttachment(wNrErrorsLessThan, margin);
		fdlDoNotProcessRest.right = new FormAttachment(middle, -margin);
		wlDoNotProcessRest.setLayoutData(fdlDoNotProcessRest);
		wDoNotProcessRest = new Button(wSuccessOn, SWT.CHECK);
		props.setLook(wDoNotProcessRest);
		wDoNotProcessRest.setToolTipText(Messages.getString("JobMoveFiles.DoNotProcessRest.Tooltip"));
		fdDoNotProcessRest = new FormData();
		fdDoNotProcessRest.left = new FormAttachment(middle, 0);
		fdDoNotProcessRest.top = new FormAttachment(wNrErrorsLessThan, margin);
		fdDoNotProcessRest.right = new FormAttachment(100, 0);
		wDoNotProcessRest.setLayoutData(fdDoNotProcessRest);
		wDoNotProcessRest.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
			}
		});
		
		
	    fdSuccessOn= new FormData();
	    fdSuccessOn.left = new FormAttachment(0, margin);
	    fdSuccessOn.top = new FormAttachment(wDestinationFile, margin);
	    fdSuccessOn.right = new FormAttachment(100, -margin);
	    wSuccessOn.setLayoutData(fdSuccessOn);
	     // ///////////////////////////////////////////////////////////
	     // / END OF Success ON GROUP
	     // ///////////////////////////////////////////////////////////

 		
 		
 		
 		
		 // fileresult grouping?
	     // ////////////////////////
	     // START OF LOGGING GROUP///
	     // /
	    wFileResult = new Group(wAdvancedComp, SWT.SHADOW_NONE);
	    props.setLook(wFileResult);
	    wFileResult.setText(Messages.getString("JobMoveFiles.FileResult.Group.Label"));

	    FormLayout fileresultgroupLayout = new FormLayout();
	    fileresultgroupLayout.marginWidth = 10;
	    fileresultgroupLayout.marginHeight = 10;

	    wFileResult.setLayout(fileresultgroupLayout);
	      
	      
	  	//Add file to result
		wlAddFileToResult = new Label(wFileResult, SWT.RIGHT);
		wlAddFileToResult.setText(Messages.getString("JobMoveFiles.AddFileToResult.Label"));
		props.setLook(wlAddFileToResult);
		fdlAddFileToResult = new FormData();
		fdlAddFileToResult.left = new FormAttachment(0, 0);
		fdlAddFileToResult.top = new FormAttachment(wSuccessOn, margin);
		fdlAddFileToResult.right = new FormAttachment(middle, -margin);
		wlAddFileToResult.setLayoutData(fdlAddFileToResult);
		wAddFileToResult = new Button(wFileResult, SWT.CHECK);
		props.setLook(wAddFileToResult);
		wAddFileToResult.setToolTipText(Messages.getString("JobMoveFiles.AddFileToResult.Tooltip"));
		fdAddFileToResult = new FormData();
		fdAddFileToResult.left = new FormAttachment(middle, 0);
		fdAddFileToResult.top = new FormAttachment(wSuccessOn, margin);
		fdAddFileToResult.right = new FormAttachment(100, 0);
		wAddFileToResult.setLayoutData(fdAddFileToResult);
		wAddFileToResult.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
			}
		});
	      
	      
	     fdFileResult = new FormData();
	     fdFileResult.left = new FormAttachment(0, margin);
	     fdFileResult.top = new FormAttachment(wSuccessOn, margin);
	     fdFileResult.right = new FormAttachment(100, -margin);
	     wFileResult.setLayoutData(fdFileResult);
	     // ///////////////////////////////////////////////////////////
	     // / END OF FilesResult GROUP
	     // ///////////////////////////////////////////////////////////

		
 		
 		
 		
	    fdAdvancedComp = new FormData();
		fdAdvancedComp.left  = new FormAttachment(0, 0);
 		fdAdvancedComp.top   = new FormAttachment(0, 0);
 		fdAdvancedComp.right = new FormAttachment(100, 0);
 		fdAdvancedComp.bottom= new FormAttachment(100, 0);
 		wAdvancedComp.setLayoutData(wAdvancedComp);

 		wAdvancedComp.layout();
		wAdvancedTab.setControl(wAdvancedComp);


		/////////////////////////////////////////////////////////////
		/// END OF ADVANCED TAB
		/////////////////////////////////////////////////////////////
 		
 		
 		
		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(wName, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);
		
		

		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK"));
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel"));

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wTabFolder);
		

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );

		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };

		wName.addSelectionListener( lsDef );
		wSourceFileFolder.addSelectionListener( lsDef );

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		getData();
		CheckIncludeSubFolders();
		activeSuccessCondition();
		setDateTimeFormat();
		activeSuccessCondition();
		setAddDateBeforeExtension();
		wTabFolder.setSelection(0);
		BaseStepDialog.setSize(shell);

		shell.open();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch()) display.sleep();
		}
		return jobEntry;
	}
	private void activeSuccessCondition()
	{
		wlNrErrorsLessThan.setEnabled(wSuccessCondition.getSelectionIndex()!=0);
		wNrErrorsLessThan.setEnabled(wSuccessCondition.getSelectionIndex()!=0);	
	}
	private void setAddDateBeforeExtension()
	{
		wlAddDateBeforeExtension.setEnabled(wAddDate.getSelection()||wAddTime.getSelection()||wSpecifyFormat.getSelection() );
		wAddDateBeforeExtension.setEnabled(wAddDate.getSelection()||wAddTime.getSelection()||wSpecifyFormat.getSelection() );
		if(!wAddDate.getSelection()&& !wAddTime.getSelection()&& !wSpecifyFormat.getSelection())
			wAddDateBeforeExtension.setSelection(false);
	}
	private void setDateTimeFormat()
	{
		if(wSpecifyFormat.getSelection())
		{
			wAddDate.setSelection(false);	
			wAddTime.setSelection(false);
		}

		wDateTimeFormat.setEnabled(wSpecifyFormat.getSelection());
		wlDateTimeFormat.setEnabled(wSpecifyFormat.getSelection());
		wAddDate.setEnabled(!wSpecifyFormat.getSelection());
		wlAddDate.setEnabled(!wSpecifyFormat.getSelection());
		wAddTime.setEnabled(!wSpecifyFormat.getSelection());
		wlAddTime.setEnabled(!wSpecifyFormat.getSelection());
		
	}
	private void RefreshArgFromPrevious()
	{

		wlFields.setEnabled(!wPrevious.getSelection());
		wFields.setEnabled(!wPrevious.getSelection());
		wbdSourceFileFolder.setEnabled(!wPrevious.getSelection());
		wbeSourceFileFolder.setEnabled(!wPrevious.getSelection());
		wbSourceFileFolder.setEnabled(!wPrevious.getSelection());
		wbaSourceFileFolder.setEnabled(!wPrevious.getSelection());		
		wbDestinationFileFolder.setEnabled(!wPrevious.getSelection());
		wlDestinationFileFolder.setEnabled(!wPrevious.getSelection());
		wDestinationFileFolder.setEnabled(!wPrevious.getSelection());
		wlSourceFileFolder.setEnabled(!wPrevious.getSelection());
		wSourceFileFolder.setEnabled(!wPrevious.getSelection());
		
		wlWildcard.setEnabled(!wPrevious.getSelection());
		wWildcard.setEnabled(!wPrevious.getSelection());	
		wbSourceDirectory.setEnabled(!wPrevious.getSelection());
		wbDestinationDirectory.setEnabled(!wPrevious.getSelection());	
	}

	public void dispose()
	{
		WindowProperty winprop = new WindowProperty(shell);
		props.setScreen(winprop);
		shell.dispose();
	}
	
	private void CheckIncludeSubFolders()
	{
		wlMoveEmptyFolders.setEnabled(wIncludeSubfolders.getSelection());
		wMoveEmptyFolders.setEnabled(wIncludeSubfolders.getSelection());
	}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */
	public void getData()
	{
		if (jobEntry.getName()    != null) wName.setText( jobEntry.getName() );
		wName.selectAll();
		wMoveEmptyFolders.setSelection(jobEntry.move_empty_folders);
		
		if (jobEntry.source_filefolder != null)
		{
			for (int i = 0; i < jobEntry.source_filefolder.length; i++)
			{
				TableItem ti = wFields.table.getItem(i);
				if (jobEntry.source_filefolder[i] != null)
					ti.setText(1, jobEntry.source_filefolder[i]);
				if (jobEntry.destination_filefolder[i] != null)
					ti.setText(2, jobEntry.destination_filefolder[i]);
				if (jobEntry.wildcard[i] != null)
					ti.setText(3, jobEntry.wildcard[i]);
			}
			wFields.setRowNums();
			wFields.optWidth(true);
		}
		wPrevious.setSelection(jobEntry.arg_from_previous);
		wOverwriteFiles.setSelection(jobEntry.overwrite_files);
		wIncludeSubfolders.setSelection(jobEntry.include_subfolders);
		wDestinationIsAFile.setSelection(jobEntry.destination_is_a_file);
		wCreateDestinationFolder.setSelection(jobEntry.create_destination_folder);
		wDoNotProcessRest.setSelection(jobEntry.IgnoreRestOfFiles);
		
		wAddFileToResult.setSelection(jobEntry.add_result_filesname);
		
		if (jobEntry.getNrErrorsLessThan()!= null) 
			wNrErrorsLessThan.setText( jobEntry.getNrErrorsLessThan() );
		else
			wNrErrorsLessThan.setText("10");
		
		
		if(jobEntry.getSuccessCondition()!=null)
		{
			if(jobEntry.getSuccessCondition().equals("success_when_errors_less_than"))
				wSuccessCondition.select(1);
			else
				wSuccessCondition.select(0);	
		}else wSuccessCondition.select(0);
		
		if (jobEntry.getDateTimeFormat()!= null) wDateTimeFormat.setText( jobEntry.getDateTimeFormat() );
		wSpecifyFormat.setSelection(jobEntry.isSpecifyFormat());
		
		wAddDate.setSelection(jobEntry.isAddDate());
		wAddTime.setSelection(jobEntry.isAddTime());
		
		wAddDateBeforeExtension.setSelection(jobEntry.isAddDateBeforeExtension());
		wDoNotKeepFolderStructure.setSelection(jobEntry.isDoNotKeepFolderStructure());
	
	}

	private void cancel()
	{
		jobEntry.setChanged(changed);
		jobEntry=null;
		dispose();
	}

	private void ok()
	{
		jobEntry.setName(wName.getText());
		jobEntry.setMoveEmptyFolders(wMoveEmptyFolders.getSelection());
		jobEntry.setoverwrite_files(wOverwriteFiles.getSelection());
		jobEntry.setIncludeSubfolders(wIncludeSubfolders.getSelection());
		jobEntry.setArgFromPrevious(wPrevious.getSelection());
		jobEntry.setAddresultfilesname(wAddFileToResult.getSelection());
		jobEntry.setDestinationIsAFile(wDestinationIsAFile.getSelection());
		jobEntry.setCreateDestinationFolder(wCreateDestinationFolder.getSelection());
		jobEntry.setDoNotProcessRest(wDoNotProcessRest.getSelection());
		jobEntry.setNrErrorsLessThan(wNrErrorsLessThan.getText());
		
		if(wSuccessCondition.getSelectionIndex()==1)
			jobEntry.setSuccessCondition("success_when_errors_less_than");
		else
			jobEntry.setSuccessCondition("success_when_all_works_fine");	
		
		
		jobEntry.setAddDate(wAddDate.getSelection());
		jobEntry.setAddTime(wAddTime.getSelection());
		jobEntry.setSpecifyFormat(wSpecifyFormat.getSelection());
		jobEntry.setDateTimeFormat(wDateTimeFormat.getText());
		jobEntry.setAddDateBeforeExtension(wAddDateBeforeExtension.getSelection());
		jobEntry.setDoNotKeepFolderStructure(wDoNotKeepFolderStructure.getSelection());
		
		
		
		int nritems = wFields.nrNonEmpty();
		int nr = 0;
		for (int i = 0; i < nritems; i++)
		{
			String arg = wFields.getNonEmpty(i).getText(1);
			if (arg != null && arg.length() != 0)
				nr++;
		}
		jobEntry.source_filefolder = new String[nr];
		jobEntry.destination_filefolder = new String[nr];
		jobEntry.wildcard = new String[nr];
		nr = 0;
		for (int i = 0; i < nritems; i++)
		{
			String source = wFields.getNonEmpty(i).getText(1);
			String dest = wFields.getNonEmpty(i).getText(2);
			String wild = wFields.getNonEmpty(i).getText(3);
			if (source != null && source.length() != 0)
			{
				jobEntry.source_filefolder[nr] = source;
				jobEntry.destination_filefolder[nr] = dest;
				jobEntry.wildcard[nr] = wild;
				nr++;
			}
		}
		dispose();
	}

	public String toString()
	{
		return this.getClass().getName();
	}

	public boolean evaluates()
	{
		return true;
	}

	public boolean isUnconditional()
	{
		return false;
	}
}