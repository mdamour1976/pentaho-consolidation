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
 * Created on 2-jul-2003
 *  
 */

package be.ibridge.kettle.trans.step.insertupdate;

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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.ColumnInfo;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.SQLStatement;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.dialog.DatabaseExplorerDialog;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.dialog.SQLEditor;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.widget.TableView;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepDialog;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.StepMeta;

public class InsertUpdateDialog extends BaseStepDialog implements StepDialogInterface
{
	private CCombo				wConnection;

	private Label				wlKey;
	private TableView			wKey;
	private FormData			fdlKey, fdKey;

	private Label				wlTable;
	private Button				wbTable;
	private Text				wTable;
	private FormData			fdlTable, fdbTable, fdTable;

	private Label				wlReturn;
	private TableView			wReturn;
	private FormData			fdlReturn, fdReturn;

	private Label				wlCommit;
	private Text				wCommit;
	private FormData			fdlCommit, fdCommit;

	private Label				wlUpdateBypassed;
	private Button				wUpdateBypassed;
	private FormData			fdlUpdateBypassed, fdUpdateBypassed;

	private Button				wGetLU;
	private FormData			fdGetLU;
	private Listener			lsGetLU;

	private InsertUpdateMeta	input;

	public InsertUpdateDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input = (InsertUpdateMeta) in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);

		ModifyListener lsMod = new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				input.setChanged();
			}
		};
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("InsertUpdateDialog.Shell.Title")); //$NON-NLS-1$

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("InsertUpdateDialog.Stepname.Label")); //$NON-NLS-1$
 		props.setLook(wlStepname);
		fdlStepname = new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right = new FormAttachment(middle, -margin);
		fdlStepname.top = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
 		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname = new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top = new FormAttachment(0, margin);
		fdStepname.right = new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

		// Connection line
		wConnection = addConnectionLine(shell, wStepname, middle, margin);
		if (input.getDatabase()==null && transMeta.nrDatabases()==1) wConnection.select(0);
		wConnection.addModifyListener(lsMod);

		// Table line...
		wlTable = new Label(shell, SWT.RIGHT);
		wlTable.setText(Messages.getString("InsertUpdateDialog.TargetTable.Label")); //$NON-NLS-1$
 		props.setLook(wlTable);
		fdlTable = new FormData();
		fdlTable.left = new FormAttachment(0, 0);
		fdlTable.right = new FormAttachment(middle, -margin);
		fdlTable.top = new FormAttachment(wConnection, margin * 2);
		wlTable.setLayoutData(fdlTable);

		wbTable = new Button(shell, SWT.PUSH | SWT.CENTER);
 		props.setLook(wbTable);
		wbTable.setText(Messages.getString("InsertUpdateDialog.Browse.Button")); //$NON-NLS-1$
		fdbTable = new FormData();
		fdbTable.right = new FormAttachment(100, 0);
		fdbTable.top = new FormAttachment(wConnection, margin);
		wbTable.setLayoutData(fdbTable);

		wTable = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wTable);
		wTable.addModifyListener(lsMod);
		fdTable = new FormData();
		fdTable.left = new FormAttachment(middle, 0);
		fdTable.top = new FormAttachment(wConnection, margin * 2);
		fdTable.right = new FormAttachment(wbTable, -margin);
		wTable.setLayoutData(fdTable);

		// Commit line
		wlCommit = new Label(shell, SWT.RIGHT);
		wlCommit.setText(Messages.getString("InsertUpdateDialog.CommitSize.Label")); //$NON-NLS-1$
 		props.setLook(wlCommit);
		fdlCommit = new FormData();
		fdlCommit.left = new FormAttachment(0, 0);
		fdlCommit.top = new FormAttachment(wTable, margin);
		fdlCommit.right = new FormAttachment(middle, -margin);
		wlCommit.setLayoutData(fdlCommit);
		wCommit = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wCommit);
		wCommit.addModifyListener(lsMod);
		fdCommit = new FormData();
		fdCommit.left = new FormAttachment(middle, 0);
		fdCommit.top = new FormAttachment(wTable, margin);
		fdCommit.right = new FormAttachment(100, 0);
		wCommit.setLayoutData(fdCommit);

		// UpdateBypassed line
		wlUpdateBypassed = new Label(shell, SWT.RIGHT);
		wlUpdateBypassed.setText(Messages.getString("InsertUpdateDialog.UpdateBypassed.Label")); //$NON-NLS-1$
 		props.setLook(wlUpdateBypassed);
		fdlUpdateBypassed = new FormData();
		fdlUpdateBypassed.left = new FormAttachment(0, 0);
		fdlUpdateBypassed.top = new FormAttachment(wCommit, margin);
		fdlUpdateBypassed.right = new FormAttachment(middle, -margin);
		wlUpdateBypassed.setLayoutData(fdlUpdateBypassed);
		wUpdateBypassed = new Button(shell, SWT.CHECK);
 		props.setLook(wUpdateBypassed);
		fdUpdateBypassed = new FormData();
		fdUpdateBypassed.left = new FormAttachment(middle, 0);
		fdUpdateBypassed.top = new FormAttachment(wCommit, margin);
		fdUpdateBypassed.right = new FormAttachment(100, 0);
		wUpdateBypassed.setLayoutData(fdUpdateBypassed);

		wlKey = new Label(shell, SWT.NONE);
		wlKey.setText(Messages.getString("InsertUpdateDialog.Keys.Label")); //$NON-NLS-1$
 		props.setLook(wlKey);
		fdlKey = new FormData();
		fdlKey.left = new FormAttachment(0, 0);
		fdlKey.top = new FormAttachment(wUpdateBypassed, margin);
		wlKey.setLayoutData(fdlKey);

		int nrKeyCols = 4;
		int nrKeyRows = (input.getKeyStream() != null ? input.getKeyStream().length : 1);

		ColumnInfo[] ciKey = new ColumnInfo[nrKeyCols];
		ciKey[0] = new ColumnInfo(Messages.getString("InsertUpdateDialog.ColumnInfo.TableField"), ColumnInfo.COLUMN_TYPE_TEXT, false); //$NON-NLS-1$
		ciKey[1] = new ColumnInfo(Messages.getString("InsertUpdateDialog.ColumnInfo.Comparator"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "=", "<>", "<", "<=", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				">", ">=", "LIKE", "BETWEEN", "IS NULL", "IS NOT NULL" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		ciKey[2] = new ColumnInfo(Messages.getString("InsertUpdateDialog.ColumnInfo.StreamField1"), ColumnInfo.COLUMN_TYPE_TEXT, false); //$NON-NLS-1$
		ciKey[3] = new ColumnInfo(Messages.getString("InsertUpdateDialog.ColumnInfo.StreamField2"), ColumnInfo.COLUMN_TYPE_TEXT, false); //$NON-NLS-1$

		wKey = new TableView(shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, ciKey,
				nrKeyRows, lsMod, props);

		wGet = new Button(shell, SWT.PUSH);
		wGet.setText(Messages.getString("InsertUpdateDialog.GetFields.Button")); //$NON-NLS-1$
		fdGet = new FormData();
		fdGet.right = new FormAttachment(100, 0);
		fdGet.top = new FormAttachment(wlKey, margin);
		wGet.setLayoutData(fdGet);

		fdKey = new FormData();
		fdKey.left = new FormAttachment(0, 0);
		fdKey.top = new FormAttachment(wlKey, margin);
		fdKey.right = new FormAttachment(wGet, -margin);
		fdKey.bottom = new FormAttachment(wlKey, 190);
		wKey.setLayoutData(fdKey);


		// THE BUTTONS
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("InsertUpdateDialog.OK.Button")); //$NON-NLS-1$
		wSQL = new Button(shell, SWT.PUSH);
		wSQL.setText(Messages.getString("InsertUpdateDialog.SQL.Button")); //$NON-NLS-1$
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wSQL, wCancel }, margin, null);

		
		// THE UPDATE/INSERT TABLE
		wlReturn = new Label(shell, SWT.NONE);
		wlReturn.setText(Messages.getString("InsertUpdateDialog.UpdateFields.Label")); //$NON-NLS-1$
 		props.setLook(wlReturn);
		fdlReturn = new FormData();
		fdlReturn.left = new FormAttachment(0, 0);
		fdlReturn.top = new FormAttachment(wKey, margin);
		wlReturn.setLayoutData(fdlReturn);

		int UpInsCols = 2;
		int UpInsRows = (input.getUpdateLookup() != null ? input.getUpdateLookup().length : 1);

		ColumnInfo[] ciReturn = new ColumnInfo[UpInsCols];
		ciReturn[0] = new ColumnInfo(Messages.getString("InsertUpdateDialog.ColumnInfo.TableField"), ColumnInfo.COLUMN_TYPE_TEXT, false); //$NON-NLS-1$
		ciReturn[1] = new ColumnInfo(Messages.getString("InsertUpdateDialog.ColumnInfo.StreamField"), ColumnInfo.COLUMN_TYPE_TEXT, false); //$NON-NLS-1$

		wReturn = new TableView(shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL,
				ciReturn, UpInsRows, lsMod, props);

		wGetLU = new Button(shell, SWT.PUSH);
		wGetLU.setText(Messages.getString("InsertUpdateDialog.GetAndUpdateFields.Label")); //$NON-NLS-1$
		fdGetLU = new FormData();
		fdGetLU.top   = new FormAttachment(wlReturn, margin);
		fdGetLU.right = new FormAttachment(100, 0);
		wGetLU.setLayoutData(fdGetLU);

		fdReturn = new FormData();
		fdReturn.left = new FormAttachment(0, 0);
		fdReturn.top = new FormAttachment(wlReturn, margin);
		fdReturn.right = new FormAttachment(wGetLU, -margin);
		fdReturn.bottom = new FormAttachment(wOK, -2*margin);
		wReturn.setLayoutData(fdReturn);
		
		// Add listeners
		lsOK = new Listener()
		{
			public void handleEvent(Event e)
			{
				ok();
			}
		};
		lsGet = new Listener()
		{
			public void handleEvent(Event e)
			{
				get();
			}
		};
		lsGetLU = new Listener()
		{
			public void handleEvent(Event e)
			{
				getUpdate();
			}
		};
		lsSQL = new Listener()
		{
			public void handleEvent(Event e)
			{
				create();
			}
		};
		lsCancel = new Listener()
		{
			public void handleEvent(Event e)
			{
				cancel();
			}
		};

		wOK.addListener(SWT.Selection, lsOK);
		wGet.addListener(SWT.Selection, lsGet);
		wGetLU.addListener(SWT.Selection, lsGetLU);
		wSQL.addListener(SWT.Selection, lsSQL);
		wCancel.addListener(SWT.Selection, lsCancel);

		lsDef = new SelectionAdapter()
		{
			public void widgetDefaultSelected(SelectionEvent e)
			{
				ok();
			}
		};

		wStepname.addSelectionListener(lsDef);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(new ShellAdapter()
		{
			public void shellClosed(ShellEvent e)
			{
				cancel();
			}
		});


		wbTable.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				getTableName();
			}
		});

		// Set the shell size, based upon previous time...
		setSize();

		getData();
		input.setChanged(changed);

		shell.open();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
				display.sleep();
		}
		return stepname;
	}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		int i;
		log.logDebug(toString(), Messages.getString("InsertUpdateDialog.Log.GettingKeyInfo")); //$NON-NLS-1$

		wCommit.setText("" + input.getCommitSize()); //$NON-NLS-1$
		wUpdateBypassed.setSelection(input.isUpdateBypassed());

		if (input.getKeyStream() != null)
			for (i = 0; i < input.getKeyStream().length; i++)
			{
				TableItem item = wKey.table.getItem(i);
				if (input.getKeyLookup()[i] != null)
					item.setText(1, input.getKeyLookup()[i]);
				if (input.getKeyCondition()[i] != null)
					item.setText(2, input.getKeyCondition()[i]);
				if (input.getKeyStream()[i] != null)
					item.setText(3, input.getKeyStream()[i]);
				if (input.getKeyStream2()[i] != null)
					item.setText(4, input.getKeyStream2()[i]);
			}

		if (input.getUpdateLookup() != null)
			for (i = 0; i < input.getUpdateLookup().length; i++)
			{
				TableItem item = wReturn.table.getItem(i);
				if (input.getUpdateLookup()[i] != null)
					item.setText(1, input.getUpdateLookup()[i]);
				if (input.getUpdateStream()[i] != null)
					item.setText(2, input.getUpdateStream()[i]);
			}

		if (input.getTableName() != null)
			wTable.setText(input.getTableName());
		if (input.getDatabase() != null)
			wConnection.setText(input.getDatabase().getName());
		else
			if (transMeta.nrDatabases() == 1)
			{
				wConnection.setText(transMeta.getDatabase(0).getName());
			}

		wStepname.selectAll();
		wKey.setRowNums();
		wKey.optWidth(true);
		wReturn.setRowNums();
		wReturn.optWidth(true);

	}

	private void cancel()
	{
		stepname = null;
		input.setChanged(changed);
		dispose();
	}

	private void getInfo(InsertUpdateMeta inf)
	{
		//Table ktable = wKey.table;
		int nrkeys = wKey.nrNonEmpty();
		int nrfields = wReturn.nrNonEmpty();

		inf.allocate(nrkeys, nrfields);

		inf.setCommitSize( Const.toInt(wCommit.getText(), 0) );
		inf.setUpdateBypassed( wUpdateBypassed.getSelection() );
		
		log.logDebug(toString(), Messages.getString("InsertUpdateDialog.Log.FoundKeys",nrkeys + "")); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i = 0; i < nrkeys; i++)
		{
			TableItem item = wKey.getNonEmpty(i);
			inf.getKeyLookup()[i] = item.getText(1);
			inf.getKeyCondition()[i] = item.getText(2);
			inf.getKeyStream()[i] = item.getText(3);
			inf.getKeyStream2()[i] = item.getText(4);
		}

		//Table ftable = wReturn.table;

		log.logDebug(toString(), Messages.getString("InsertUpdateDialog.Log.FoundFields", nrfields + "")); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i = 0; i < nrfields; i++)
		{
			TableItem item = wReturn.getNonEmpty(i);
			inf.getUpdateLookup()[i] = item.getText(1);
			inf.getUpdateStream()[i] = item.getText(2);
		}

		inf.setTableName( wTable.getText() );
		inf.setDatabase(  transMeta.findDatabase(wConnection.getText()) );

		stepname = wStepname.getText(); // return value
	}

	private void ok()
	{
		// Get the information for the dialog into the input structure.
		getInfo(input);

		if (input.getDatabase() == null)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
			mb.setMessage(Messages.getString("InsertUpdateDialog.InvalidConnection.DialogMessage")); //$NON-NLS-1$
			mb.setText(Messages.getString("InsertUpdateDialog.InvalidConnection.DialogTitle")); //$NON-NLS-1$
			mb.open();
		}

		dispose();
	}

	private void getTableName()
	{
		DatabaseMeta inf = null;
		// New class: SelectTableDialog
		int connr = wConnection.getSelectionIndex();
		if (connr >= 0)
			inf = transMeta.getDatabase(connr);

		if (inf != null)
		{
			log.logDebug(toString(), Messages.getString("InsertUpdateDialog.Log.LookingAtConnection") + inf.toString()); //$NON-NLS-1$

			DatabaseExplorerDialog std = new DatabaseExplorerDialog(shell, props, SWT.NONE, inf, transMeta.getDatabases());
			std.setSelectedTable(wTable.getText());
			String tableName = (String) std.open();
			if (tableName != null)
			{
				wTable.setText(tableName);
			}
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
			mb.setMessage(Messages.getString("InsertUpdateDialog.InvalidConnection.DialogMessage")); //$NON-NLS-1$
			mb.setText(Messages.getString("InsertUpdateDialog.InvalidConnection.DialogTitle")); //$NON-NLS-1$
			mb.open();
		}
	}

	private void get()
	{
		try
		{
			int i, count;
			Row r = transMeta.getPrevStepFields(stepname);
			if (r != null)
			{
				Table table = wKey.table;
				count = table.getItemCount();
				for (i = 0; i < r.size(); i++)
				{
					Value v = r.getValue(i);
					TableItem ti = new TableItem(table, SWT.NONE);
					ti.setText(0, "" + (count + i + 1)); //$NON-NLS-1$
					ti.setText(1, v.getName());
					ti.setText(2, "="); //$NON-NLS-1$
					ti.setText(3, v.getName());
					ti.setText(4, ""); //$NON-NLS-1$
				}
				wKey.removeEmptyRows();
				wKey.setRowNums();
				wKey.optWidth(true);
			}
		}
		catch (KettleException ke)
		{
			new ErrorDialog(shell, props, Messages.getString("InsertUpdateDialog.FailedToGetFields.DialogTitle"), //$NON-NLS-1$
					Messages.getString("InsertUpdateDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$
		}
	}

	private void getUpdate()
	{
		try
		{
			int i, count;
			Row r = transMeta.getPrevStepFields(stepname);
			if (r != null)
			{
				Table table = wReturn.table;
				count = table.getItemCount();
				for (i = 0; i < r.size(); i++)
				{
					Value v = r.getValue(i);
					TableItem ti = new TableItem(table, SWT.NONE);
					ti.setText(0, "" + (count + i + 1)); //$NON-NLS-1$
					ti.setText(1, v.getName());
					ti.setText(2, v.getName());

				}
				wReturn.removeEmptyRows();
				wReturn.setRowNums();
				wReturn.optWidth(true);
			}
		}
		catch (KettleException ke)
		{
			new ErrorDialog(shell, props, Messages.getString("InsertUpdateDialog.FailedToGetFields.DialogTitle"), //$NON-NLS-1$
					Messages.getString("InsertUpdateDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$
		}
	}

	// Generate code for create table...
	// Conversions done by Database
	private void create()
	{
		try
		{
			InsertUpdateMeta info = new InsertUpdateMeta();
			getInfo(info);

			String name = stepname; // new name might not yet be linked to other steps!
			StepMeta stepMeta = new StepMeta(log, Messages.getString("InsertUpdateDialog.StepMeta.Title"), name, info); //$NON-NLS-1$
			Row prev = transMeta.getPrevStepFields(stepname);

			SQLStatement sql = info.getSQLStatements(transMeta, stepMeta, prev);
			if (!sql.hasError())
			{
				if (sql.hasSQL())
				{
					SQLEditor sqledit = new SQLEditor(shell, SWT.NONE, info.getDatabase(), transMeta.getDbCache(),
							sql.getSQL());
					sqledit.open();
				}
				else
				{
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
					mb.setMessage(Messages.getString("InsertUpdateDialog.NoSQLNeeds.DialogMessage")); //$NON-NLS-1$
					mb.setText(Messages.getString("InsertUpdateDialog.NoSQLNeeds.DialogTitle")); //$NON-NLS-1$
					mb.open();
				}
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
				mb.setMessage(sql.getError());
				mb.setText(Messages.getString("InsertUpdateDialog.SQLError.DialogTitle")); //$NON-NLS-1$
				mb.open();
			}
		}
		catch (KettleException ke)
		{
			new ErrorDialog(shell, props, Messages.getString("InsertUpdateDialog.CouldNotBuildSQL.DialogTitle"), //$NON-NLS-1$
					Messages.getString("InsertUpdateDialog.CouldNotBuildSQL.DialogMessage"), ke); //$NON-NLS-1$
		}

	}

	public String toString()
	{
		return this.getClass().getName();
	}
}