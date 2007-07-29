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
 

package org.pentaho.di.trans.steps.tableinput;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Reads information from a database table by using freehand SQL
 * 
 * @author Matt
 * @since 8-apr-2003
 */
public class TableInput extends BaseStep implements StepInterface
{
	private TableInputMeta meta;
	private TableInputData data;
	
	public TableInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	private synchronized RowMetaAndData readStartDate() throws KettleException
    {
		if (log.isDetailed()) logDetailed("Reading from step [" + meta.getLookupStepname() + "]");

        RowMetaInterface parametersMeta = new RowMeta();
        Object[] parametersData = new Object[] {};
        RowMetaAndData parameters = new RowMetaAndData(parametersMeta, parametersData);

        RowSet rowSet = findInputRowSet(meta.getLookupStepname());
        Object[] rowData = getRowFrom(rowSet); // rows are originating from "lookup_from"
        while (rowData!=null)
        {
            parametersData = RowDataUtil.addRowData(parametersData, parametersMeta.size(), rowData);
            parametersMeta.addRowMeta(rowSet.getRowMeta());
            
            rowData = getRowFrom(rowSet); // take all input rows if needed!
        }
        
        if (parametersMeta.size()==0)
        {
            throw new KettleException("Expected to read parameters from step ["+meta.getLookupStepname()+"] but none were found.");
        }
        
        return parameters;
    }	
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		if (first) // we just got started
		{
            Object[] parameters;
            RowMetaInterface parametersMeta;
			first=false;
            
			// Make sure we read data from source steps...
            if (meta.getInfoSteps() != null)
            {
                if (meta.isExecuteEachInputRow())
                {
                	if (log.isDetailed()) logDetailed("Reading single row from stream [" + meta.getLookupStepname() + "]");
                	data.rowSet = findInputRowSet(meta.getLookupStepname());
                    parameters = getRowFrom(data.rowSet);
                    parametersMeta = data.rowSet.getRowMeta();
                }
                else
                {
                	if (log.isDetailed()) logDetailed("Reading query parameters from stream [" + meta.getLookupStepname() + "]");
                    RowMetaAndData rmad = readStartDate(); // Read values in lookup table (look)
                    parameters = rmad.getData();
                    parametersMeta = rmad.getRowMeta();
                }
                if (parameters!=null)
                {
                    if (log.isDetailed()) logDetailed("Query parameters found = " + parameters.toString());
                }
            }
            else
            {
                parameters = new Object[] {};
                parametersMeta = new RowMeta();
			}
            
            if (meta.isExecuteEachInputRow() && ( parameters==null || parametersMeta.size()==0) )
            {
                setOutputDone(); // signal end to receiver(s)
                return false; // stop immediately, nothing to do here.
            }
            
            boolean success = doQuery(parametersMeta, parameters);
            if (!success) 
            { 
                return false; 
            }
		}
        else
        {
            if (data.thisrow!=null) // We can expect more rows
            {
                data.nextrow=data.db.getRow(data.rs); 
                if (data.nextrow!=null) linesInput++;
            }
        }

    	if (data.thisrow == null) // Finished reading?
        {
            boolean done = false;
            if (meta.isExecuteEachInputRow()) // Try to get another row from the input stream
            {
                Object[] nextRow = getRowFrom(data.rowSet);
                if (nextRow == null) // Nothing more to get!
                {
                    done = true;
                }
                else
                {
                    // First close the previous query, otherwise we run out of cursors!
                    closePreviousQuery();
                    
                    boolean success = doQuery(data.rowSet.getRowMeta(), nextRow); // OK, perform a new query
                    if (!success) 
                    { 
                        return false; 
                    }
                }
            }
            else
            {
                done = true;
            }

            if (done)
            {
                setOutputDone(); // signal end to receiver(s)
                return false; // end of data or error.
            }
        }
        else
        {
            putRow(data.rowMeta, data.thisrow); // fill the rowset(s). (wait for empty)
            data.thisrow = data.nextrow;

            if (checkFeedback(linesInput)) logBasic("linenr " + linesInput);
        }
		
		return true;
	}
    
    private void closePreviousQuery() throws KettleDatabaseException
    {
        data.db.closeQuery(data.rs);
    }

    private boolean doQuery(RowMetaInterface parametersMeta, Object[] parameters) throws KettleDatabaseException
    {
        boolean success = true;

        // Open the query with the optional parameters received from the source steps.
        String sql = null;
        if (meta.isVariableReplacementActive()) sql = environmentSubstitute(meta.getSQL());
        else sql = meta.getSQL();
        
        data.rs = data.db.openQuery(sql, parametersMeta, parameters);
        if (data.rs == null)
        {
            logError("Couldn't open Query [" + sql + "]");
            setErrors(1);
            stopAll();
            success = false;
        }
        else
        {
            // Keep the metadata
            data.rowMeta = data.db.getReturnRowMeta();
            
            // Get the first row...
            data.thisrow = data.db.getRow(data.rs);
            if (data.thisrow != null)
            {
                linesInput++;
                data.nextrow = data.db.getRow(data.rs);
                if (data.nextrow != null) linesInput++;
            }
        }
        return success;
    }

	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		logBasic("Finished reading query, closing connection.");
		try
		{
		    closePreviousQuery();
		}
		catch(KettleException e)
		{
			logError("Unexpected error closing query : "+e.toString());
		    setErrors(1);
		    stopAll();
		}
		finally 
		{
		    data.db.disconnect();
		}

		super.dispose(smi, sdi);
	}
	
	/** Stop the running query */
	public void stopRunning(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
        meta=(TableInputMeta)smi;
        data=(TableInputData)sdi;

        setStopped(true);
        
        if (data.db!=null) data.db.cancelQuery();
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(TableInputMeta)smi;
		data=(TableInputData)sdi;

		if (super.init(smi, sdi))
		{
			data.db=new Database(meta.getDatabaseMeta());
			data.db.shareVariablesWith(this);
			
			data.db.setQueryLimit(meta.getRowLimit());

			try
			{
                if (getTransMeta().isUsingUniqueConnections())
                {
                    synchronized (getTrans()) { data.db.connect(getTrans().getThreadName(), getPartitionID()); }
                }
                else
                {
                    data.db.connect(getPartitionID());
                }

                if (meta.getDatabaseMeta().getDatabaseType()!=DatabaseMeta.TYPE_DATABASE_SYBASE)
                {
                    data.db.setCommit(100); // needed for PGSQL it seems...
                }
                if (log.isDetailed()) logDetailed("Connected to database...");

				return true;
			}
			catch(KettleException e)
			{
				logError("An error occurred, processing will be stopped: "+e.getMessage());
				setErrors(1);
				stopAll();
			}
		}
		
		return false;
	}
	
	//
	// Run is were the action happens!
	public void run()
	{
		try
		{
			logBasic(Messages.getString("System.Log.StartingToRun")); //$NON-NLS-1$
			
			while (processRow(meta, data) && !isStopped());
		}
		catch(Throwable t)
		{
			logError(Messages.getString("System.Log.UnexpectedError")+" : "+t.toString()); //$NON-NLS-1$ //$NON-NLS-2$
            logError(Const.getStackTracker(t));
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
}