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

 
package org.pentaho.di.core.dialog;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.dialog.Messages;
import org.pentaho.di.core.gui.GUIResource;
import org.pentaho.di.core.widget.TableView;
import org.pentaho.di.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.StepMeta;

import org.pentaho.di.core.widget.ColumnInfo;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.gui.WindowProperty;




/**
 * Dialog to display the results of a verify operation.
 * 
 * @author Matt
 * @since 19-06-2003
 *
 */

public class CheckResultDialog extends Dialog
{
	private static final String STRING_HIDE_SUCESSFUL = Messages.getString("CheckResultDialog.HideSuccessful.Label");
	private static final String STRING_SHOW_SUCESSFUL = Messages.getString("CheckResultDialog.ShowSuccessful.Label");

	private static final String STRING_HIDE_REMARKS = Messages.getString("CheckResultDialog.Remarks.Label");
	private static final String STRING_SHOW_REMARKS = Messages.getString("CheckResultDialog.WarningsErrors.Label");

	private ArrayList    remarks;
		
	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;

	private Button wClose, wView, wEdit, wNoOK;
	private Listener lsClose, lsView, lsEdit, lsNoOK;

	private Shell    shell;
	private Props    props;
	
	private Color    red, green, yellow;
	
	private boolean  show_successful_results = false;
	
	private String stepname;
	
    /**
     * @deprecated Use the CT without the <i>log</i> and <i>props</i> parameter (at 3rd and 4th position)
     */
    public CheckResultDialog(Shell parent, int style, LogWriter log, Props props, ArrayList rem)
    {
        this(parent, style, rem);
        this.props = props;
    }

	public CheckResultDialog(Shell parent, int style, ArrayList rem)
	{
			super(parent, style);
			remarks=rem;
			props=Props.getInstance();
			stepname=null;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();
		
		red    = display.getSystemColor(SWT.COLOR_RED);
		green  = display.getSystemColor(SWT.COLOR_GREEN);
		yellow = display.getSystemColor(SWT.COLOR_YELLOW);

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX);
 		props.setLook(shell);
		shell.setImage(GUIResource.getInstance().getImageSpoonGraph());

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("CheckResultDialog.Title"));
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		wlFields=new Label(shell, SWT.LEFT);
		wlFields.setText(Messages.getString("CheckResultDialog.Remarks.Label"));
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.right= new FormAttachment(middle, -margin);
		fdlFields.top  = new FormAttachment(0, margin);
		wlFields.setLayoutData(fdlFields);
		
		int FieldsCols=3;
		int FieldsRows=1;
		
		ColumnInfo[] colinf=new ColumnInfo[FieldsCols];
		colinf[0]=new ColumnInfo(Messages.getString("CheckResultDialog.Stepname.Label"), ColumnInfo.COLUMN_TYPE_TEXT,   false, true);
		colinf[1]=new ColumnInfo(Messages.getString("CheckResultDialog.Result.Label"),   ColumnInfo.COLUMN_TYPE_TEXT,   false, true);
		colinf[2]=new ColumnInfo(Messages.getString("CheckResultDialog.Remark.Label"),   ColumnInfo.COLUMN_TYPE_TEXT,   false, true);
		
		wFields=new TableView(shell, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
						      colinf, 
						      FieldsRows,  
							  true, // read-only
						      null,
							  props
						      );
		
		fdFields=new FormData();
		fdFields.left   = new FormAttachment(0, 0);
		fdFields.top    = new FormAttachment(wlFields, margin);
		fdFields.right  = new FormAttachment(100, 0);
		fdFields.bottom = new FormAttachment(100, -50);
		wFields.setLayoutData(fdFields);

		wNoOK=new Button(shell, SWT.CHECK);
		wNoOK.setText(STRING_SHOW_SUCESSFUL);
		FormData fd = new FormData();
		fd.left   = new FormAttachment(0, 0);
		fd.top    = new FormAttachment(wFields, margin);
		wNoOK.setLayoutData(fd);
        
		wClose=new Button(shell, SWT.PUSH);
		wClose.setText(Messages.getString("System.Button.Close"));

		wView=new Button(shell, SWT.PUSH);
		wView.setText(Messages.getString("CheckResultDialog.Button.ViewMessage"));

		wEdit=new Button(shell, SWT.PUSH);
		wEdit.setText(Messages.getString("CheckResultDialog.Button.EditOriginStep"));

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wClose, wView, wEdit }, margin, null);

		// Add listeners
		lsClose = new Listener() { public void handleEvent(Event e) { close(); } };
		lsView  = new Listener() { public void handleEvent(Event e) { view(); } };
		lsEdit  = new Listener() { public void handleEvent(Event e) { edit(); } };
		lsNoOK  = new Listener() { public void handleEvent(Event e) { noOK(); } };

		
		wClose.addListener(SWT.Selection, lsClose    );
		wView .addListener(SWT.Selection, lsView     );
		wEdit .addListener(SWT.Selection, lsEdit     );
		wNoOK .addListener(SWT.Selection, lsNoOK     );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { close(); } } );

		getData();
		
		BaseStepDialog.setSize(shell);
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	
	private void noOK() 
	{
		show_successful_results=!show_successful_results;
		
		getData();
	};


	public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		wFields.table.removeAll();
		
		for (int i=0;i<remarks.size();i++)
		{
			CheckResult cr = (CheckResult)remarks.get(i);
			if (show_successful_results || cr.getType()!=CheckResult.TYPE_RESULT_OK)
			{
				TableItem ti = new TableItem(wFields.table, SWT.NONE); 
	
				StepMeta stepMeta = cr.getStepInfo();
				if (stepMeta!=null) ti.setText(1, stepMeta.getName());
				else          ti.setText(1, "<global>");
				ti.setText(2, cr.getType()+" - "+cr.getTypeDesc());
				ti.setText(3, cr.getText());
	
				Color col = ti.getBackground();
				switch(cr.getType())
				{
					case CheckResult.TYPE_RESULT_OK:      col=green;  break;
					case CheckResult.TYPE_RESULT_ERROR:   col=red;    break;
					case CheckResult.TYPE_RESULT_WARNING: col=yellow; break;
					case CheckResult.TYPE_RESULT_COMMENT: 
					default:break;
				}
				ti.setBackground(col);
			}
		}
		
		if (wFields.table.getItemCount()==0) 
		{
			wFields.clearAll(false);
		}
		
		wFields.setRowNums();
		wFields.optWidth(true);
		
		if (show_successful_results) 
		{
			wlFields.setText(STRING_HIDE_REMARKS);
			wNoOK.setText(STRING_HIDE_SUCESSFUL);
		}
		else 
		{
			wlFields.setText(STRING_SHOW_REMARKS);
			wNoOK.setText(STRING_SHOW_SUCESSFUL);
		}

		shell.layout();
	}
	
	// View message:
	private void view()
	{
		StringBuffer message=new StringBuffer();
		TableItem item[] = wFields.table.getSelection();
		
		// None selected: don't waste users time: select them all!
		if (item.length==0) item=wFields.table.getItems();
		
		for (int i=0;i<item.length;i++)
		{
			if (i>0)
			    message.append("_______________________________________________________________________________").append(Const.CR).append(Const.CR);
			message.append("[").append(item[i].getText(2)).append("] ").append(item[i].getText(1)).append(Const.CR);
			message.append("  ").append(item[i].getText(3)).append(Const.CR).append(Const.CR);
		}
		
		String subtitle = (item.length != 1 ?
		    Messages.getString("CheckResultDialog.TextDialog.SubtitlePlural") :
		    Messages.getString("CheckResultDialog.TextDialog.Subtitle"));
		EnterTextDialog etd = new EnterTextDialog(shell, Messages.getString("CheckResultDialog.TextDialog.Title"),
		    subtitle, message.toString());
		etd.setReadOnly();
		etd.open();
	}
	
	private void edit()
	{
		int idx=wFields.table.getSelectionIndex();
		if (idx>=0)
		{
			stepname = wFields.table.getItem(idx).getText(1);
			dispose();
		}	
	}
	
	private void close()
	{
		dispose();
	}
}
