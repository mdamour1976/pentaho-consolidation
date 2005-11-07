 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** It belongs to, is maintained by and is copyright 1999-2005 by     **
 **                                                                   **
 **      i-Bridge bvba                                                **
 **      Fonteinstraat 70                                             **
 **      9400 OKEGEM                                                  **
 **      Belgium                                                      **
 **      http://www.kettle.be                                         **
 **      info@kettle.be                                               **
 **                                                                   **
 **********************************************************************/
 
/*
 * Created on 19-jun-2003
 *
 */

package be.ibridge.kettle.spoon.dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
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

import be.ibridge.kettle.core.ColumnInfo;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.widget.TableView;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.StepMeta;


public class PreviewSelectDialog extends Dialog
{
	private Label        wlFields;
	
	private TableView    wFields;
	private FormData     fdlFields, fdFields;

	private Button wPreview, wCancel;
	private FormData fdPreview, fdCancel;
	private Listener lsPreview, lsCancel;

	private Shell         shell;
	private TransMeta     trans;
	
	public String preview_steps[];
	public int    preview_sizes[];
	
	private Props props;
	
	public PreviewSelectDialog(Shell parent, int style, LogWriter l, Props props, TransMeta tr)
	{
		super(parent, style);
		trans=tr;
		this.props=props;
		preview_steps=null;
		preview_sizes=null;
	}

	public void open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("Preview selection screen");
		
		int margin = Const.MARGIN;

		wlFields=new Label(shell, SWT.NONE);
		wlFields.setText("Steps: ");
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(0, margin);
		wlFields.setLayoutData(fdlFields);
		
		final int FieldsCols=2;
		final int FieldsRows=trans.nrUsedSteps();
		
		ColumnInfo[] colinf=new ColumnInfo[FieldsCols];
		colinf[0]=new ColumnInfo("Stepname",     ColumnInfo.COLUMN_TYPE_TEXT, "", false, true );
		colinf[1]=new ColumnInfo("Preview size", ColumnInfo.COLUMN_TYPE_TEXT, "", false, false);
		
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

		wPreview=new Button(shell, SWT.PUSH);
		wPreview.setText(" &Show ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(" &Close ");
		fdPreview=new FormData();
		fdPreview.left=new FormAttachment(33, 0);
		fdPreview.bottom =new FormAttachment(100, 0);
		wPreview.setLayoutData(fdPreview);
		fdCancel=new FormData();
		fdCancel.left=new FormAttachment(66, 0);
		fdCancel.bottom =new FormAttachment(100, 0);
		wCancel.setLayoutData(fdCancel);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();  } };
		lsPreview  = new Listener() { public void handleEvent(Event e) { preview(); } };
		
		wCancel.addListener (SWT.Selection, lsCancel  );
		wPreview.addListener(SWT.Selection, lsPreview );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		WindowProperty winprop = props.getScreen(shell.getText());
		if (winprop!=null) winprop.setShell(shell); else shell.pack();

		getData();
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
	}

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
		int i;
		
		String pr_steps[] = props.getLastPreview();
		int    pr_sizes[] = props.getLastPreviewSize();
		String name;
		
		if (trans.nrSelectedSteps()==0)
		{
			for (i=0;i<trans.nrUsedSteps();i++)
			{
				StepMeta stepMeta = trans.getUsedStep(i);
				
				TableItem item = wFields.table.getItem(i);
				name = stepMeta.getName();
				item.setText(1, stepMeta.getName());
				item.setText(2, "0");
	
				// Remember the last time...?
				for (int x=0;x<pr_steps.length;x++)
				{
					if (pr_steps[x].equalsIgnoreCase(name)) 
					{
						item.setText(2, ""+pr_sizes[x]);
					} 
				}
			}
		}
		else
		{		
			// No previous selection: set the selected steps to 100
			for (i=0;i<trans.nrUsedSteps();i++)
			{
				StepMeta stepMeta = trans.getUsedStep(i);
				
				TableItem item = wFields.table.getItem(i);
				name = stepMeta.getName();
				item.setText(1, stepMeta.getName());
				item.setText(2, "0");
	
				// Is the step selected?
				if (stepMeta.isSelected())
				{
					item.setText(2, "100");
				}
			}
		}
		
		wFields.optWidth(true);
	}
	
	private void cancel()
	{
		dispose();
	}
	
	private void preview()
	{
		int sels=0;
		for (int i=0;i<wFields.table.getItemCount();i++)
		{
			TableItem ti = wFields.table.getItem(i);
			int size =  Const.toInt(ti.getText(2), 0);
			if (size > 0) 
			{
				sels++;
			} 
		}
		
		preview_steps=new String[sels];
		preview_sizes=new int   [sels];

		sels=0;		
		for (int i=0;i<wFields.table.getItemCount();i++)
		{
			TableItem ti = wFields.table.getItem(i);
			int size=Const.toInt(ti.getText(2), 0);

			if (size > 0) 
			{
				preview_steps[sels]=ti.getText(1);
				preview_sizes[sels]=size;

				sels++;
			} 
		}
		
		props.setLastPreview(preview_steps, preview_sizes);

		dispose();
	}
}
