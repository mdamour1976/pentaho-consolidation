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

package org.pentaho.di.trans.steps.combinationlookup;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;



/**
 * Manages or looks up information in a Type 1 or junk dimension.<p>
 * <p>
 	 1) Lookup combination field1..n in a dimension<p>
 	 2) If this combination exists, return technical key<p>
     3) If this combination doesn't exist, insert & return technical key<p>
 	 4) if replace is Y, remove all key fields from output.<p>
	 <p>
 * @author Matt
 * @since 22-jul-2003
 */
public class CombinationLookup extends BaseStep implements StepInterface
{
	private final static int CREATION_METHOD_AUTOINC  = 1;
    private final static int CREATION_METHOD_SEQUENCE = 2;
	private final static int CREATION_METHOD_TABLEMAX = 3;

	private int techKeyCreation;

	private CombinationLookupMeta meta;
	private CombinationLookupData data;

	public CombinationLookup(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);

		meta=(CombinationLookupMeta)getStepMeta().getStepMetaInterface();
		data=(CombinationLookupData)stepDataInterface;
	}

	private void setTechKeyCreation(int method)
	{
		techKeyCreation = method;
	}

	private int getTechKeyCreation()
	{
		return techKeyCreation;
	}

	private void determineTechKeyCreation()
	{
		String keyCreation = meta.getTechKeyCreation();
		if (meta.getDatabaseMeta().supportsAutoinc() &&
			CombinationLookupMeta.CREATION_METHOD_AUTOINC.equals(keyCreation) )
		{
		    setTechKeyCreation(CREATION_METHOD_AUTOINC);
		}
		else if (meta.getDatabaseMeta().supportsSequences() &&
		  	     CombinationLookupMeta.CREATION_METHOD_SEQUENCE.equals(keyCreation) )
		{
		    setTechKeyCreation(CREATION_METHOD_SEQUENCE);
		}
		else
		{
			setTechKeyCreation(CREATION_METHOD_TABLEMAX);
		}
	}

	private Long lookupInCache(RowMetaInterface rowMeta, Object[] row)
	{
		// try to find the row in the cache...
		// 
		Long tk = (Long) data.cache.get(new RowMetaAndData(rowMeta, row));
		return tk;
	}
    
    /**
     * Adds a row to the cache
     * In case we are doing updates, we need to store the complete rows from the database.
     * These are the values we need to store
     * 
     * Key:
     *   - natural key fields
     * Value:
     *   - Technical key
     *   - lookup fields / extra fields (allows us to compare or retrieve)
     *   - Date_from
     *   - Date_to
     * 
     * @param row
     * @param returnValues
     * @throws KettleValueException 
     */
    private void addToCache(RowMetaInterface rowMeta, Object[] row, Long tk) throws KettleValueException
    {
        // store it in the cache if needed.
        data.cache.put(new RowMetaAndData(rowMeta, row), tk);
        
        // check if the size is not too big...
        // Allow for a buffer overrun of 20% and then remove those 20% in one go.
        // Just to keep performance in track.
        //
        int tenPercent = meta.getCacheSize()/10;
        if (meta.getCacheSize()>0 && data.cache.size()>meta.getCacheSize()+tenPercent)
        {
            // Which cache entries do we delete here?
            // We delete those with the lowest technical key...
            // Those would arguably be the "oldest" dimension entries.
            // Oh well... Nothing is going to be perfect here...
            // 
            // Getting the lowest 20% requires some kind of sorting algorithm and I'm not sure we want to do that.
            // Sorting is slow and even in the best case situation we need to do 2 passes over the cache entries...
            //
            // Perhaps we should get 20% random values and delete everything below the lowest but one TK.
            //
            List<RowMetaAndData> keys = new ArrayList<RowMetaAndData>(data.cache.keySet());
            int sizeBefore = keys.size();
            List<Long> samples = new ArrayList<Long>();
            
            // Take 10 sample technical keys....
            int stepsize=keys.size()/5;
            if (stepsize<1) stepsize=1; //make sure we have no endless loop
            for (int i=0;i<keys.size();i+=stepsize)
            {
                RowMetaAndData key = (RowMetaAndData) keys.get(i);
                Long value = (Long) data.cache.get(key);
                if (value!=null)
                {
                    samples.add(value);
                }
            }
            // Sort these 5 elements...
            Collections.sort(samples);
            
            // What is the smallest?
            // Take the second, not the fist in the list, otherwise we would be removing a single entry = not good.
            if (samples.size()>1) {
                data.smallestCacheKey = ((Long) samples.get(1)).longValue();
            } else { // except when there is only one sample
                data.smallestCacheKey = ((Long) samples.get(0)).longValue();
            }
            
            // Remove anything in the cache <= smallest.
            // This makes it almost single pass...
            // This algorithm is not 100% correct, but I guess it beats sorting the whole cache all the time.
            //
            for (int i=0;i<keys.size();i++)
            {
                RowMetaAndData key = (RowMetaAndData) keys.get(i);
                Long value = (Long) data.cache.get(key);
                if (value!=null)
                {
                    if (value.longValue()<=data.smallestCacheKey)
                    {
                        data.cache.remove(key); // this one has to go.
                    }
                }
            }
            
            int sizeAfter = data.cache.size();
            logDetailed("Reduced the lookup cache from "+sizeBefore+" to "+sizeAfter+" rows.");
        }
        
        if (log.isRowLevel()) logRowlevel("Cache store: key="+rowMeta.getString(row)+"    key="+tk);
    }

    
    private boolean isAutoIncrement()
    {
        return techKeyCreation == CREATION_METHOD_AUTOINC;
    }

	private Object[] lookupValues(RowMetaInterface rowMeta, Object[] row) throws KettleException
	{
		Long val_key  = null;
        Long val_hash = null;
        Object[] hashRow = null;
        
        Object[] lookupRow = new Object[data.lookupRowMeta.size()];
        int lookupIndex = 0;
        
		if (meta.useHash() || meta.getCacheSize()>=0)
		{
            hashRow = new Object[data.hashRowMeta.size()];
            for (int i=0;i<meta.getKeyField().length;i++)
            {
                hashRow[i] = row[data.keynrs[i]];
            }

            val_hash = new Long( data.hashRowMeta.hashCode(hashRow) );
			
            if (meta.useHash())
            {
                lookupRow[lookupIndex] = val_hash;
                lookupIndex++;
            }
		}

		for (int i=0;i<meta.getKeyField().length;i++)
		{
			lookupRow[lookupIndex] = row[data.keynrs[i]]; // KEYi = ?
            lookupIndex++;

            lookupRow[lookupIndex] = row[data.keynrs[i]]; // KEYi IS NULL or ? IS NULL
            lookupIndex++;
		}

		// Before doing the actual lookup in the database, see if it's not in the cache...
		val_key = lookupInCache(data.hashRowMeta, hashRow);
		if (val_key==null)
		{
			data.db.setValues(data.lookupRowMeta, lookupRow, data.prepStatementLookup);
			Object[] add = data.db.getLookup(data.prepStatementLookup);
            linesInput++;

			if (add==null) // The dimension entry was not found, we need to add it!
			{
				// First try to use an AUTOINCREMENT field
				switch ( getTechKeyCreation() )
				{
				    case CREATION_METHOD_TABLEMAX:
				    	//  Use our own counter: what's the next value for the technical key?
				        val_key = data.db.getNextValue(getTransMeta().getCounters(), meta.getSchemaName(), meta.getTablename(), meta.getTechnicalKeyField());
                        break;
				    case CREATION_METHOD_AUTOINC:
						val_key=new Long(0); // value to accept new key...
						break;
				    case CREATION_METHOD_SEQUENCE:
						val_key=data.db.getNextSequenceValue(meta.getSchemaName(), meta.getSequenceFrom(), meta.getTechnicalKeyField());
						if (val_key!=null && log.isRowLevel()) logRowlevel(Messages.getString("CombinationLookup.Log.FoundNextSequenceValue")+val_key.toString()); //$NON-NLS-1$
						break;
				}

				val_key = combiInsert( rowMeta, row, val_key, val_hash );
				linesOutput++;

                if (log.isRowLevel()) log.logRowlevel(toString(), Messages.getString("CombinationLookup.Log.AddedDimensionEntry")+val_key); //$NON-NLS-1$

				// Also store it in our Hashtable...
				addToCache(data.hashRowMeta, hashRow, val_key);
			}
			else
			{
                // Entry already exists...
                //
				val_key = (Long) add[0]; // Only one value possible here...
                addToCache(data.hashRowMeta, hashRow, val_key);
			}
		}

        Object[] outputRow = new Object[data.outputRowMeta.size()];
        int outputIndex = 0; 
        
		// See if we need to replace the fields with the technical key
		if (meta.replaceFields())
		{
			for (int i=0;i<rowMeta.size();i++)
			{
				if (!data.removeField[i])
                {
				    outputRow[outputIndex] = row[i];
                    outputIndex++;
                }
			}
		}
        else
        {
            // Just copy the input row and add the technical key
            for (outputIndex=0;outputIndex<rowMeta.size();outputIndex++)
            {
                outputRow[outputIndex] = row[outputIndex];
            }
        }

		// Add the technical key...
		outputRow[outputIndex] = val_key;
        
        return outputRow;
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		Object[] r=getRow();       // Get row from input rowset & set row busy!
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
        
        if (first)
        {
            first=false;
            
            data.outputRowMeta = getInputRowMeta().clone();
            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
            
            data.schemaTable = meta.getDatabaseMeta().getQuotedSchemaTableCombination(meta.getSchemaName(), meta.getTablename());
            
            determineTechKeyCreation();

            // The indexes of the key values...
            //
            data.keynrs      = new int[meta.getKeyField().length];
            
            for (int i=0;i<meta.getKeyField().length;i++)
            {
                data.keynrs[i]=getInputRowMeta().indexOfValue(meta.getKeyField()[i]);
                if (data.keynrs[i]<0) // couldn't find field!
                {
                    throw new KettleStepException(Messages.getString("CombinationLookup.Exception.FieldNotFound",meta.getKeyField()[i])); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }

            // Determine for each input field if we want it removed or not.
            //
            data.removeField = new boolean[getInputRowMeta().size()];
            
            // Sort lookup values keys so that we
            //
            for (int i=0;i<getInputRowMeta().size();i++)
            {
                ValueMetaInterface valueMeta = getInputRowMeta().getValueMeta(i);
                // Is this one of the keys?
                int idx = Const.indexOfString(valueMeta.getName(), meta.getKeyField());
                data.removeField[i] = idx>=0;
            }

            // Determine the metadata row to calculate hashcodes.
            //
            data.hashRowMeta = new RowMeta();
            for (int i=0;i<meta.getKeyField().length;i++)
            {
                data.hashRowMeta.addValueMeta( getInputRowMeta().getValueMeta(data.keynrs[i]) ); // KEYi = ?
            }
            
            setCombiLookup(getInputRowMeta());
        }


		try
		{
			Object[] outputRow = lookupValues(getInputRowMeta(), r); // add new values to the row in rowset[0].
			putRow(data.outputRowMeta, outputRow);       // copy row to output rowset(s);

            if (checkFeedback(linesRead)) logBasic(Messages.getString("CombinationLookup.Log.LineNumber")+linesRead); //$NON-NLS-1$
		}
		catch(KettleException e)
		{
			logError(Messages.getString("CombinationLookup.Log.ErrorInStepRunning")+e.getMessage()); //$NON-NLS-1$
			setErrors(1);
			stopAll();
			setOutputDone();  // signal end to receiver(s)
			return false;
		}

		return true;
	}
    
    /**
     * CombinationLookup
     * table: dimension table
     * keys[]: which dim-fields do we use to look up key?
     * retval: name of the key to return
     */
    public void setCombiLookup(RowMetaInterface inputRowMeta) throws KettleDatabaseException
    {
        DatabaseMeta databaseMeta = meta.getDatabaseMeta();
        
        String sql = "";
        boolean comma;
        data.lookupRowMeta = new RowMeta();
        
        /* 
         * SELECT <retval> 
         * FROM   <table> 
         * WHERE  ( ( <key1> = ? ) OR ( <key1> IS NULL AND ? IS NULL ) )  
         * AND    ( ( <key2> = ? ) OR ( <key1> IS NULL AND ? IS NULL ) )  
         * ...
         * ;
         * 
         * OR
         * 
         * SELECT <retval> 
         * FROM   <table> 
         * WHERE  <crcfield> = ?  
         * AND    ( ( <key1> = ? ) OR ( <key1> IS NULL AND ? IS NULL ) )
         * AND    ( ( <key2> = ? ) OR ( <key1> IS NULL AND ? IS NULL ) )
         * ...
         * ;
         * 
         */
        
        sql += "SELECT "+databaseMeta.quoteField(meta.getTechnicalKeyField())+Const.CR;
        sql += "FROM "+data.schemaTable+Const.CR;
        sql += "WHERE ";
        comma=false;
        
        if (meta.useHash())
        {
            sql += databaseMeta.quoteField(meta.getHashField()) + " = ? " + Const.CR;
            comma=true;
            data.lookupRowMeta.addValueMeta(new ValueMeta(meta.getHashField(), ValueMetaInterface.TYPE_INTEGER));
        }
        else
        {
            sql += "( ( ";
        }
        
        for (int i=0;i<meta.getKeyField().length;i++)
        {
            if (comma)
            {
                sql += " AND ( ( ";
            }
            else
            { 
                comma=true; 
            }
            sql += databaseMeta.quoteField(meta.getKeyField()[i]) + " = ? ) OR ( " + databaseMeta.quoteField(meta.getKeyField()[i]);
            data.lookupRowMeta.addValueMeta(inputRowMeta.getValueMeta(data.keynrs[i]));
            
            sql += " IS NULL AND ";
            if (databaseMeta.getDatabaseType()==DatabaseMeta.TYPE_DATABASE_DB2)
            {
                sql += "CAST(? AS VARCHAR(256)) IS NULL";
            }
            else
            {
                sql += "? IS NULL";
            }
            data.lookupRowMeta.addValueMeta(inputRowMeta.getValueMeta(data.keynrs[i]));
            
            sql += " ) )";
            sql += Const.CR;
        }
        
        try
        {
            String sqlStatement = sql.toString();
            if (log.isDebug()) log.logDebug(toString(), "preparing combi-lookup statement:"+Const.CR+sqlStatement);
            data.prepStatementLookup=data.db.getConnection().prepareStatement(databaseMeta.stripCR(sqlStatement));
            if (databaseMeta.supportsSetMaxRows())
            {
                data.prepStatementLookup.setMaxRows(1); // alywas get only 1 line back!
            }
        }
        catch(SQLException ex) 
        {
            throw new KettleDatabaseException("Unable to prepare combi-lookup statement", ex);
        }
    }

    /**
     * This inserts new record into a junk dimension
     */
    public Long combiInsert( RowMetaInterface rowMeta, Object[] row, Long val_key, Long val_crc ) throws KettleDatabaseException
    {
        String debug="Combination insert";
        DatabaseMeta databaseMeta = meta.getDatabaseMeta();
        try
        {
            if (data.prepStatementInsert==null) // first time: construct prepared statement
            {
                debug="First: construct prepared statement";
                
                data.insertRowMeta = new RowMeta();
                
                /* Construct the SQL statement...
                 *
                 * INSERT INTO 
                 * d_test(keyfield, [crcfield,] keylookup[])
                 * VALUES(val_key, [val_crc], row values with keynrs[])
                 * ;
                 */
                 
                String sql = "";
                sql += "INSERT INTO " + data.schemaTable + ("( ");
                boolean comma=false;
    
                if (!isAutoIncrement()) // NO AUTOINCREMENT 
                {
                    sql += databaseMeta.quoteField(meta.getTechnicalKeyField());
                    data.insertRowMeta.addValueMeta( new ValueMeta(meta.getTechnicalKeyField(), ValueMetaInterface.TYPE_INTEGER));
                    comma=true;
                }
                else
                if (databaseMeta.needsPlaceHolder()) 
                {
                    sql += "0";   // placeholder on informix!  Will be replaced in table by real autoinc value.
                    data.insertRowMeta.addValueMeta( new ValueMeta(meta.getTechnicalKeyField(), ValueMetaInterface.TYPE_INTEGER));
                    comma=true;
                } 
                
                if (meta.useHash())
                {
                    if (comma) sql += ", ";
                    sql += databaseMeta.quoteField(meta.getHashField());
                    data.insertRowMeta.addValueMeta( new ValueMeta(meta.getHashField(), ValueMetaInterface.TYPE_INTEGER));
                    comma=true;
                }
                
                for (int i=0;i<meta.getKeyLookup().length;i++)
                {
                    if (comma) sql += ", "; 
                    sql += databaseMeta.quoteField(meta.getKeyLookup()[i]);
                    data.insertRowMeta.addValueMeta( rowMeta.getValueMeta(data.keynrs[i]) );
                    comma=true;
                }
                
                sql += ") VALUES (";
                
                comma=false;
                
                if (!isAutoIncrement())
                {
                    sql += '?';
                    comma=true;
                }
                if (meta.useHash())
                {
                    if (comma) sql+=',';
                    sql += '?';
                    comma=true;
                }
    
                for (int i=0;i<meta.getKeyLookup().length;i++)
                {
                    if (comma) sql += ','; else comma=true;
                    sql += '?';
                }
                
                sql += " )";
    
                String sqlStatement = sql.toString();
                try
                {
                    debug="First: prepare statement";
                    if (isAutoIncrement())
                    {
                        log.logDetailed(toString(), "SQL with return keys: "+sqlStatement);
                        data.prepStatementInsert=data.db.getConnection().prepareStatement(databaseMeta.stripCR(sqlStatement), Statement.RETURN_GENERATED_KEYS);
                    }
                    else
                    {
                        log.logDetailed(toString(), "SQL without return keys: "+sqlStatement);
                        data.prepStatementInsert=data.db.getConnection().prepareStatement(databaseMeta.stripCR(sqlStatement));
                    }
                }
                catch(SQLException ex) 
                {
                    throw new KettleDatabaseException("Unable to prepare combi insert statement : "+Const.CR+sqlStatement, ex);
                }
                catch(Exception ex)
                {
                    throw new KettleDatabaseException("Unable to prepare combi insert statement : "+Const.CR+sqlStatement, ex);
                }
            }
            
            debug="Create new insert row rins";
            Object[] insertRow=new Object[data.insertRowMeta.size()];
            int insertIndex = 0;
            
            if (!isAutoIncrement()) 
            {
                insertRow[insertIndex] = val_key;
                insertIndex++;
            }
            if (meta.useHash())
            {
                insertRow[insertIndex] = val_crc;
                insertIndex++;
            }
            for (int i=0;i<data.keynrs.length;i++)
            {
                insertRow[insertIndex] = row[data.keynrs[i]];
                insertIndex++;
            }
            
            if (log.isRowLevel()) log.logRowlevel(toString(), "rins="+data.insertRowMeta.getString(insertRow));
            
            debug="Set values on insert";
            // INSERT NEW VALUE!
            data.db.setValues(data.insertRowMeta, insertRow, data.prepStatementInsert);
            
            debug="Insert row";
            data.db.insertRow(data.prepStatementInsert);
            
            debug="Retrieve key";
            if (isAutoIncrement())
            {
                ResultSet keys = null;
                try
                {
                    keys=data.prepStatementInsert.getGeneratedKeys(); // 1 key
                    if (keys.next()) val_key = new Long( keys.getLong(1) );
                    else 
                    {
                        throw new KettleDatabaseException("Unable to retrieve auto-increment of combi insert key : "+meta.getTechnicalKeyField()+", no fields in resultset");
                    }                   
                }
                catch(SQLException ex) 
                {
                    throw new KettleDatabaseException("Unable to retrieve auto-increment of combi insert key : "+meta.getTechnicalKeyField(), ex);
                }
                finally 
                {
                    try 
                    {
                        if ( keys != null ) keys.close();
                    }
                    catch(SQLException ex) 
                    {
                        throw new KettleDatabaseException("Unable to retrieve auto-increment of combi insert key : "+meta.getTechnicalKeyField(), ex);
                    }                       
                }
            }
        }
        catch(Exception e)
        {
            log.logError(toString(), Const.getStackTracker(e));
            throw new KettleDatabaseException("Unexpected error in combination insert in part ["+debug+"] : "+e.toString(), e);
        }
        
        return val_key;
    }

    
	public boolean init(StepMetaInterface sii, StepDataInterface sdi)
	{
		if (super.init(sii, sdi))
		{
			if (meta.getCacheSize()>0)
			{
				data.cache=new HashMap<RowMetaAndData, Long>((int)(meta.getCacheSize()*1.5));
			}
			else
			{
				data.cache=new HashMap<RowMetaAndData, Long>();
			}

			data.db=new Database(meta.getDatabaseMeta());
			data.db.shareVariablesWith(this);
			try
			{
				data.db.connect(getPartitionID());
				logBasic(Messages.getString("CombinationLookup.Log.ConnectedToDB")); //$NON-NLS-1$
				data.db.setCommit(meta.getCommitSize());

				return true;
			}
			catch(KettleDatabaseException dbe)
			{
				logError(Messages.getString("CombinationLookup.Log.UnableToConnectDB")+dbe.getMessage()); //$NON-NLS-1$
			}
		}
		return false;
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
	    meta = (CombinationLookupMeta)smi;
	    data = (CombinationLookupData)sdi;

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
        }
        catch(KettleDatabaseException e)
        {
            logError(Messages.getString("CombinationLookup.Log.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        finally 
        {
  	        data.db.disconnect();
        }

	    super.dispose(smi, sdi);
	}

	public String toString()
	{
		return this.getClass().getName();
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