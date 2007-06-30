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
package org.pentaho.di.trans.steps.update;

import java.sql.SQLException;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;



/**
 * Update data in a database table, does NOT ever perform an insert.
 * 
 * @author Matt
 * @since 26-apr-2003
 */
public class Update extends BaseStep implements StepInterface
{
	private UpdateMeta meta;
	private UpdateData data;
	
	public Update(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	private synchronized Object[] lookupValues(RowMetaInterface rowMeta, Object[] row) throws KettleException
	{
        Object[] outputRow=row;
        
        // Create the output row and copy the input values
        if (!Const.isEmpty( meta.getIgnoreFlagField())) // add flag field!
        {
    		outputRow = new Object[data.outputRowMeta.size()];
            for (int i=0;i<rowMeta.size();i++)
            {
                outputRow[i] = row[i];
            }
        }
        
        // OK, now do the lookup.
        // We need the lookupvalues for that.
        Object[] lookupRow = new Object[data.lookupParameterRowMeta.size()];
        int lookupIndex = 0;
        
		for (int i=0;i<meta.getKeyStream().length;i++)
		{
			if (data.keynrs[i]>=0)
			{
				lookupRow[lookupIndex] = row[ data.keynrs[i] ];
                lookupIndex++;
			}
			if (data.keynrs2[i]>=0)
			{
                lookupRow[lookupIndex] = row[ data.keynrs2[i] ];
                lookupIndex++;
			}
		}
		
		data.db.setValues(data.lookupParameterRowMeta, lookupRow, data.prepStatementLookup);
		
		if (log.isDebug()) logDebug(Messages.getString("Update.Log.ValuesSetForLookup", data.lookupParameterRowMeta.getString(lookupRow), rowMeta.getString(row))); //$NON-NLS-1$ //$NON-NLS-2$
		Object[] add = data.db.getLookup(data.prepStatementLookup);  // Got back the complete row!
		linesInput++;
		
		if (add==null) 
		{
			/* nothing was found: throw error!
			 */
            if (!meta.isErrorIgnored())
            {
                if (getStepMeta().isDoingErrorHandling())
                {
                    outputRow=null;
                    if (data.stringErrorKeyNotFound==null)
                    {
                        data.stringErrorKeyNotFound=Messages.getString("Update.Exception.KeyCouldNotFound")+data.lookupParameterRowMeta.getString(lookupRow);
                        data.stringFieldnames="";
                        for (int i=0;i<data.lookupParameterRowMeta.size();i++) 
                        {
                            if (i>0) data.stringFieldnames+=", ";
                            data.stringFieldnames+=data.lookupParameterRowMeta.getValueMeta(i).getName();
                        }
                    }
                    putError(rowMeta, row, 1L, data.stringErrorKeyNotFound, data.stringFieldnames, "UPD001");
                }
                else
                {
                    throw new KettleDatabaseException(Messages.getString("Update.Exception.KeyCouldNotFound")+data.lookupParameterRowMeta.getString(lookupRow)); //$NON-NLS-1$
                }
            }
            else
            {
                if (log.isDetailed()) log.logDetailed(toString(), Messages.getString("Update.Log.KeyCouldNotFound")+data.lookupParameterRowMeta.getString(lookupRow)); //$NON-NLS-1$
                if (!Const.isEmpty( meta.getIgnoreFlagField())) // set flag field!
                {
                    outputRow[rowMeta.size()] = new Boolean(false);
                }
            }
		}
		else
		{
			if (log.isRowLevel()) logRowlevel(Messages.getString("Update.Log.FoundRow")+add.toString()); //$NON-NLS-1$
			/* Row was found:
			 *  
			 * UPDATE row or do nothing?
			 *
			 */
			boolean update = false;
			for (int i=0;i<data.valuenrs.length;i++)
			{
                ValueMetaInterface valueMeta = rowMeta.getValueMeta( data.valuenrs[i] );
				Object rowvalue = row[ data.valuenrs[i] ];
				Object retvalue = add[ i ];
                
                if ( valueMeta.compare(rowvalue, retvalue)!=0 )
				{
					update=true;
				}
			}
			if (update)
			{
                // Create the update row...
                Object[] updateRow = new Object[data.updateParameterRowMeta.size()];
                for (int i=0;i<data.valuenrs.length;i++)
                {
                    updateRow[i] = row[ data.valuenrs[i] ]; // the setters
                }
                // add the where clause parameters, they are exactly the same for lookup and update
                for (int i=0;i<lookupRow.length;i++)
                {
                    updateRow[data.valuenrs.length+i] = lookupRow[i];
                }
                
				if (log.isRowLevel()) logRowlevel(Messages.getString("Update.Log.UpdateRow")+data.lookupParameterRowMeta.getString(lookupRow)); //$NON-NLS-1$
				data.db.setValues(data.updateParameterRowMeta, updateRow, data.prepStatementUpdate);
				data.db.insertRow(data.prepStatementUpdate);
				linesUpdated++;
			}
			else
			{
				linesSkipped++;
			}
            
            if (!Const.isEmpty(meta.getIgnoreFlagField())) // add flag field!
            {
                row[rowMeta.size()] = new Boolean(true);
            }
		}
        
        return outputRow;
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(UpdateMeta)smi;
		data=(UpdateData)sdi;

		Object[] r=getRow();       // Get row from input rowset & set row busy!
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
        
        if (first)
        {
            first=false;
            
            // What's the output Row format?
            data.outputRowMeta = (RowMetaInterface)getInputRowMeta().clone();
            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
            
            data.schemaTable = meta.getDatabaseMeta().getQuotedSchemaTableCombination(meta.getSchemaName(), meta.getTableName());
                        
            // lookup the values!
            if (log.isDetailed()) logDetailed(Messages.getString("Update.Log.CheckingRow")+getInputRowMeta().getString(r)); //$NON-NLS-1$
            
            data.keynrs  = new int[meta.getKeyStream().length];
            data.keynrs2 = new int[meta.getKeyStream().length];
            for (int i=0;i<meta.getKeyStream().length;i++)
            {
                data.keynrs[i]=getInputRowMeta().indexOfValue(meta.getKeyStream()[i]);
                if (data.keynrs[i]<0 &&  // couldn't find field!
                    !"IS NULL".equalsIgnoreCase(meta.getKeyCondition()[i]) &&   // No field needed! //$NON-NLS-1$
                    !"IS NOT NULL".equalsIgnoreCase(meta.getKeyCondition()[i])  // No field needed! //$NON-NLS-1$
                   )
                {
                    throw new KettleStepException(Messages.getString("Update.Exception.FieldRequired",meta.getKeyStream()[i])); //$NON-NLS-1$ //$NON-NLS-2$
                }
                data.keynrs2[i]=getInputRowMeta().indexOfValue(meta.getKeyStream2()[i]);
                if (data.keynrs2[i]<0 &&  // couldn't find field!
                    "BETWEEN".equalsIgnoreCase(meta.getKeyCondition()[i])   // 2 fields needed! //$NON-NLS-1$
                   )
                {
                    throw new KettleStepException(Messages.getString("Update.Exception.FieldRequired",meta.getKeyStream2()[i])); //$NON-NLS-1$ //$NON-NLS-2$
                }
                
                if (log.isDebug()) logDebug(Messages.getString("Update.Log.FieldHasDataNumbers",meta.getKeyStream()[i])+""+data.keynrs[i]); //$NON-NLS-1$ //$NON-NLS-2$
            }
            // Cache the position of the compare fields in Row row
            //
            data.valuenrs = new int[meta.getUpdateLookup().length];
            for (int i=0;i<meta.getUpdateLookup().length;i++)
            {
                data.valuenrs[i]=getInputRowMeta().indexOfValue(meta.getUpdateStream()[i]);
                if (data.valuenrs[i]<0)  // couldn't find field!
                {
                    throw new KettleStepException(Messages.getString("Update.Exception.FieldRequired",meta.getUpdateStream()[i])); //$NON-NLS-1$ //$NON-NLS-2$
                }
                if (log.isDebug()) logDebug(Messages.getString("Update.Log.FieldHasDataNumbers",meta.getUpdateStream()[i])+""+data.valuenrs[i]); //$NON-NLS-1$ //$NON-NLS-2$
            }
            
            setLookup(getInputRowMeta());
            prepareUpdate(getInputRowMeta());
        }
            
		Object[] outputRow = lookupValues(getInputRowMeta(), r); // add new values to the row in rowset[0].
        if (outputRow!=null) putRow(data.outputRowMeta, outputRow); // copy non-ignored rows to output rowset(s);
        if (checkFeedback(linesRead)) logBasic(Messages.getString("Update.Log.LineNumber")+linesRead); //$NON-NLS-1$
			
		return true;
	}
    
    public void setLookup(RowMetaInterface rowMeta) throws KettleDatabaseException
    {
        data.lookupParameterRowMeta = new RowMeta();
        data.lookupReturnRowMeta = new RowMeta();
        
        DatabaseMeta databaseMeta = meta.getDatabaseMeta();
        
        String sql = "SELECT ";

        for (int i = 0; i < meta.getUpdateLookup().length; i++)
        {
            if (i != 0) sql += ", ";
            sql += databaseMeta.quoteField(meta.getUpdateLookup()[i]);
            data.lookupReturnRowMeta.addValueMeta( rowMeta.searchValueMeta(meta.getUpdateStream()[i]) );
        }

        sql += " FROM " + data.schemaTable + " WHERE ";

        for (int i = 0; i < meta.getKeyLookup().length; i++)
        {
            if (i != 0) sql += " AND ";
            sql += databaseMeta.quoteField(meta.getKeyLookup()[i]);
            if ("BETWEEN".equalsIgnoreCase(meta.getKeyCondition()[i]))
            {
                sql += " BETWEEN ? AND ? ";
                data.lookupParameterRowMeta.addValueMeta( rowMeta.searchValueMeta(meta.getKeyStream()[i]) );
                data.lookupParameterRowMeta.addValueMeta( rowMeta.searchValueMeta(meta.getKeyStream2()[i]) );
            }
            else
            {
                if ("IS NULL".equalsIgnoreCase(meta.getKeyCondition()[i]) || "IS NOT NULL".equalsIgnoreCase(meta.getKeyCondition()[i]))
                {
                    sql += " " + meta.getKeyCondition()[i] + " ";
                }
                else
                {
                    sql += " " + meta.getKeyCondition()[i] + " ? ";
                    data.lookupParameterRowMeta.addValueMeta( rowMeta.searchValueMeta(meta.getKeyStream()[i]) );
                }
            }
        }
        
        try
        {
            log.logDetailed(toString(), "Setting preparedStatement to [" + sql + "]");
            data.prepStatementLookup = data.db.getConnection().prepareStatement(databaseMeta.stripCR(sql));
        }
        catch (SQLException ex)
        {
            throw new KettleDatabaseException("Unable to prepare statement for SQL statement [" + sql + "]", ex);
        }
    }
    
    // Lookup certain fields in a table
    public void prepareUpdate(RowMetaInterface rowMeta) throws KettleDatabaseException
    {
        DatabaseMeta databaseMeta = meta.getDatabaseMeta();
        data.updateParameterRowMeta = new RowMeta();
        
        String sql = "UPDATE " + data.schemaTable + Const.CR;
        sql += "SET ";

        for (int i=0;i<meta.getUpdateLookup().length;i++)
        {
            if (i!=0) sql+= ",   ";
            sql += databaseMeta.quoteField(meta.getUpdateLookup()[i]);
            sql += " = ?" + Const.CR;
            data.updateParameterRowMeta.addValueMeta( rowMeta.searchValueMeta(meta.getUpdateStream()[i]) );
        }

        sql += "WHERE ";

        for (int i=0;i<meta.getKeyLookup().length;i++)
        {
            if (i!=0) sql += "AND   ";
            sql += databaseMeta.quoteField(meta.getKeyLookup()[i]);
            if ("BETWEEN".equalsIgnoreCase(meta.getKeyCondition()[i]))
            {
                sql += " BETWEEN ? AND ? ";
                data.updateParameterRowMeta.addValueMeta( rowMeta.searchValueMeta(meta.getKeyStream()[i]) );
                data.updateParameterRowMeta.addValueMeta( rowMeta.searchValueMeta(meta.getKeyStream2()[i]) );
            }
            else
            if ("IS NULL".equalsIgnoreCase(meta.getKeyCondition()[i]) || "IS NOT NULL".equalsIgnoreCase(meta.getKeyCondition()[i]))
            {
                sql += " "+meta.getKeyCondition()[i]+" ";
            }
            else
            {
                sql += " "+meta.getKeyCondition()[i]+" ? ";
                data.updateParameterRowMeta.addValueMeta( rowMeta.searchValueMeta(meta.getKeyStream()[i]) );
            }
        }

        try
        {
            String s = sql.toString();
            log.logDetailed(toString(), "Setting update preparedStatement to ["+s+"]");
            data.prepStatementUpdate=data.db.getConnection().prepareStatement(databaseMeta.stripCR(s));
        }
        catch(SQLException ex) 
        {
            throw new KettleDatabaseException("Unable to prepare statement for SQL statement [" + sql + "]", ex);
        }
    }
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(UpdateMeta)smi;
		data=(UpdateData)sdi;
		
		if (super.init(smi, sdi))
		{
			data.db=new Database(meta.getDatabaseMeta());
			data.db.shareVariablesWith(this);
			try 
			{
                if (getTransMeta().isUsingUniqueConnections())
                {
                    synchronized (getTrans()) 
                    { 
                        data.db.connect(getTrans().getThreadName(), getPartitionID());
                    }
                }
                else
                {
                    data.db.connect(getPartitionID());
                }
                
				logBasic(Messages.getString("Update.Log.ConnectedToDB")); //$NON-NLS-1$
				
				data.db.setCommit(meta.getCommitSize());

				return true;
			}
			catch(KettleException ke)
			{
				logError(Messages.getString("Update.Log.ErrorOccurred")+ke.getMessage()); //$NON-NLS-1$
				setErrors(1);
				stopAll();
			}
		}
		return false;
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(UpdateMeta)smi;
		data=(UpdateData)sdi;
		
        try
        {
            if (!data.db.isAutoCommit())
            {
                if (getErrors()==0)
                {
                    data.db.commit();
                }
                else
                {
                    data.db.rollback();                    
                }
            }
            data.db.closePreparedStatement(data.prepStatementUpdate);
            data.db.closePreparedStatement(data.prepStatementLookup);
        }
        catch(KettleDatabaseException e)
        {
            log.logError(toString(), Messages.getString("Update.Log.UnableToCommitUpdateConnection")+data.db+"] :"+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
            setErrors(1);
        }
        
        
        
		data.db.disconnect();

		super.dispose(smi, sdi);
	}
	
	//
	// Run is were the action happens!
	public void run()
	{
		try
		{
			logBasic(Messages.getString("Update.Log.StartingToRun")); //$NON-NLS-1$
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError(Messages.getString("Update.Log.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
            logError(Const.getStackTracker(e));
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
