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
 
package be.ibridge.kettle.trans.step.insertupdate;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleStepException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/**
 * Performs a lookup in a database table.  If the key doesn't exist it inserts values into the table, otherwise it performs an update of the changed values.
 * If nothing changed, do nothing.
 *  
 * @author Matt
 * @since 26-apr-2003
 */
public class InsertUpdate extends BaseStep implements StepInterface
{
	private InsertUpdateMeta meta;
	private InsertUpdateData data;
	
	public InsertUpdate(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	private synchronized void lookupValues(Row row)
		throws KettleException
	{
		Row lu;
		Row add;
	
		debug=Messages.getString("InsertUpdate.Debug.StartOfLookupValues"); //$NON-NLS-1$
			
		if (first)
		{
			debug=Messages.getString("InsertUpdate.Debug.FirstRunInitialize"); //$NON-NLS-1$
			first=false;
			
			data.dblup.setLookup(meta.getTableName(), meta.getKeyLookup(), meta.getKeyCondition(), meta.getUpdateLookup(), null, null);
            
            Row ins = new Row();
            // Insert the update fields: just names.  Type doesn't matter!
            for (int i=0;i<meta.getUpdateLookup().length;i++) 
            {
                if (ins.searchValueIndex(meta.getUpdateLookup()[i])<0) // Don't add twice!
                {
                    ins.addValue( new Value(meta.getUpdateLookup()[i]) );
                }
            }
            data.dbins.prepareInsert(ins, meta.getTableName());
            
            if (!meta.isUpdateBypassed())
            {
            	data.dbupd.prepareUpdate(meta.getTableName(), meta.getKeyLookup(), meta.getKeyCondition(), meta.getUpdateLookup());
            }
			
			debug=Messages.getString("InsertUpdate.Debug.FirstRunLookupValues"); //$NON-NLS-1$
			// lookup the values!
			if (log.isDetailed()) logDetailed(Messages.getString("InsertUpdate.Log.CheckingRow")+row.toString()); //$NON-NLS-1$
			data.keynrs  = new int[meta.getKeyStream().length];
			data.keynrs2 = new int[meta.getKeyStream().length];
			for (int i=0;i<meta.getKeyStream().length;i++)
			{
				data.keynrs[i]=row.searchValueIndex(meta.getKeyStream()[i]);
				if (data.keynrs[i]<0 &&  // couldn't find field!
                    !"IS NULL".equalsIgnoreCase(meta.getKeyCondition()[i]) &&   // No field needed! //$NON-NLS-1$
				    !"IS NOT NULL".equalsIgnoreCase(meta.getKeyCondition()[i])  // No field needed! //$NON-NLS-1$
                   )
				{
					throw new KettleStepException(Messages.getString("InsertUpdate.Exception.FieldRequired",meta.getKeyStream()[i])); //$NON-NLS-1$ //$NON-NLS-2$
				}
				data.keynrs2[i]=row.searchValueIndex(meta.getKeyStream2()[i]);
				if (data.keynrs2[i]<0 &&  // couldn't find field!
				    "BETWEEN".equalsIgnoreCase(meta.getKeyCondition()[i])   // 2 fields needed! //$NON-NLS-1$
				   )
				{
					throw new KettleStepException(Messages.getString("InsertUpdate.Exception.FieldRequired",meta.getKeyStream2()[i])); //$NON-NLS-1$ //$NON-NLS-2$
				}
				
				if (log.isDebug()) logDebug(Messages.getString("InsertUpdate.Log.FieldHasDataNumbers",meta.getKeyStream()[i])+data.keynrs[i]); //$NON-NLS-1$ //$NON-NLS-2$
			}
			// Cache the position of the compare fields in Row row
			//
			debug=Messages.getString("InsertUpdate.Debug.FirstRunLookupCompareFields"); //$NON-NLS-1$
			data.valuenrs = new int[meta.getUpdateLookup().length];
			for (int i=0;i<meta.getUpdateLookup().length;i++)
			{
				data.valuenrs[i]=row.searchValueIndex(meta.getUpdateStream()[i]);
				if (data.valuenrs[i]<0)  // couldn't find field!
				{
					throw new KettleStepException(Messages.getString("InsertUpdate.Exception.FieldRequired",meta.getUpdateStream()[i])); //$NON-NLS-1$ //$NON-NLS-2$
				}
				if (log.isDebug()) logDebug(Messages.getString("InsertUpdate.Log.FieldHasDataNumbers",meta.getUpdateStream()[i])+data.valuenrs[i]); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		
		lu = new Row();
		for (int i=0;i<meta.getKeyStream().length;i++)
		{
			if (data.keynrs[i]>=0)
			{
				lu.addValue( row.getValue(data.keynrs[i]) );
			}
			if (data.keynrs2[i]>=0)
			{
				lu.addValue( row.getValue(data.keynrs2[i]) );
			}
		}
		
		debug="setValues()"; //$NON-NLS-1$
		data.dblup.setValuesLookup(lu);
		
		if (log.isDebug()) logDebug(Messages.getString("InsertUpdate.Log.ValuesSetForLookup")+lu.toString()); //$NON-NLS-1$
		debug="getLookup()"; //$NON-NLS-1$
		add=data.dblup.getLookup();  // Got back the complete row!
		linesInput++;
		
		if (add==null) 
		{
			/* nothing was found:
			 *  
			 * INSERT ROW
			 *
			 */
			if (log.isRowLevel()) logRowlevel(Messages.getString("InsertUpdate.InsertRow")+row.toString()); //$NON-NLS-1$

			debug="setValuesInsert()"; //$NON-NLS-1$
            
            // The values to insert are those in the update section (all fields should be specified)
            // For the others, we have no definite mapping!
            //
            Row ins = new Row();
            for (int i=0;i<data.valuenrs.length;i++)
            {
                ins.addValue(row.getValue(data.valuenrs[i]));
            }
            
            // Set the values on the prepared statement...
			data.dbins.setValuesInsert(ins);
            
			// Insert the row
            debug="insertRow()"; //$NON-NLS-1$
            data.dbins.insertRow();
            
			linesOutput++;
		}
		else
		{
			if (!meta.isUpdateBypassed())
			{
				if (log.isRowLevel()) logRowlevel(Messages.getString("InsertUpdate.Log.FoundRowForUpdate")+row.toString()); //$NON-NLS-1$
				
				/* Row was found:
				 *  
				 * UPDATE row or do nothing?
				 *
				 */
				debug=Messages.getString("InsertUpdate.Debug.CompareForUpdate"); //$NON-NLS-1$
				boolean update = false;
				for (int i=0;i<data.valuenrs.length;i++)
				{
					Value rowvalue = row.getValue(data.valuenrs[i]);
					lu.addValue(i, rowvalue);
					Value retvalue = add.getValue(i);
					if (!retvalue.equals(rowvalue)) // Take table value as the driver.
					{
						update=true;
					}
				}
				if (update)
				{
					if (log.isRowLevel()) logRowlevel(Messages.getString("InsertUpdate.Log.UpdateRow")+lu.toString()); //$NON-NLS-1$
					debug="setValuesUpdate()"; //$NON-NLS-1$
					data.dbupd.setValuesUpdate(lu);
					debug="updateRow()"; //$NON-NLS-1$
					data.dbupd.updateRow();
					linesUpdated++;
				}
				else
				{
					linesSkipped++;
				}
			}
			else
			{
				if (log.isRowLevel()) logRowlevel(Messages.getString("InsertUpdate.Log.UpdateBypassed")+row.toString()); //$NON-NLS-1$
				linesSkipped++;
			}
		}
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(InsertUpdateMeta)smi;
		data=(InsertUpdateData)sdi;

		Row r=getRow();       // Get row from input rowset & set row busy!
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		    
		try
		{
			lookupValues(r); // add new values to the row in rowset[0].
			putRow(r);       // copy row to output rowset(s);
			
			if ((linesRead>0) && (linesRead%Const.ROWS_UPDATE)==0) logBasic(Messages.getString("InsertUpdate.Log.LineNumber")+linesRead); //$NON-NLS-1$
		}
		catch(KettleException e)
		{
			logError(Messages.getString("InsertUpdate.Log.ErrorInStep")+e.getMessage()); //$NON-NLS-1$
			setErrors(1);
			stopAll();
			setOutputDone();  // signal end to receiver(s)
			return false;
		}
			
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(InsertUpdateMeta)smi;
		data=(InsertUpdateData)sdi;
		
		if (super.init(smi, sdi))
		{
		    
		    try
		    {
				data.dblup=new Database(meta.getDatabase());
				data.dbins=new Database(meta.getDatabase());
				data.dbupd=new Database(meta.getDatabase());
				
				data.dblup.connect();
				data.dbins.connect();
				data.dbupd.connect();
				
				data.dbins.setCommit(meta.getCommitSize());
				data.dbupd.setCommit(meta.getCommitSize());

				return true;
			}
			catch(KettleException ke)
			{
				logError(Messages.getString("InsertUpdate.Log.ErrorOccurredDuringStepInitialize")+ke.getMessage()); //$NON-NLS-1$
			}
		}
		return false;
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
	    meta = (InsertUpdateMeta)smi;
	    data = (InsertUpdateData)sdi;
	
        try
        {
            if (!data.dbupd.isAutoCommit()) data.dbupd.commit();  
            data.dbupd.closeUpdate();
            if (!data.dbins.isAutoCommit()) data.dbins.commit();  
            data.dbins.closeInsert();
        }
        catch(KettleDatabaseException e)
        {
            log.logError(toString(), Messages.getString("InsertUpdate.Log.UnableToCommitConnection")+e.toString()); //$NON-NLS-1$
            setErrors(1);
        }

		data.dblup.disconnect();
		data.dbins.disconnect();
		data.dbupd.disconnect();

	    super.dispose(smi, sdi);
	}


	//
	// Run is were the action happens!
	public void run()
	{
		try
		{
			logBasic(Messages.getString("InsertUpdate.Log.StartingToRun")); //$NON-NLS-1$
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError(Messages.getString("InsertUpdate.Log.UnexpectedError")+debug+"' : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
			setErrors(1);
			stopAll();
		}
		finally
		{
			dispose(meta, data);
			logSummary();
			markStop();
		}
	}
	
	public String toString()
	{
		return this.getClass().getName();
	}
}
