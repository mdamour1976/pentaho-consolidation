package org.pentaho.di.repository.delegates;

import org.pentaho.di.core.Condition;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.repository.Repository;

public class RepositoryConditionDelegate extends BaseRepositoryDelegate {

//	private static Class<?> PKG = Condition.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public RepositoryConditionDelegate(Repository repository) {
		super(repository);
	}
	
	public RowMetaAndData getCondition(long id_condition) throws KettleException
	{
		return repository.connectionDelegate.getOneRow(quoteTable(Repository.TABLE_R_CONDITION), quote(Repository.FIELD_CONDITION_ID_CONDITION), id_condition);
	}

	/**
     *  
	 * Read a condition from the repository.
	 * @param id_condition The condition id
	 * @throws KettleException if something goes wrong.
	 */
	public Condition loadCondition(long id_condition) throws KettleException
	{
		Condition condition = new Condition();
		
		try
		{
			RowMetaAndData r = getCondition(id_condition);
            if (r!=null)
            {
    			condition.setNegated( r.getBoolean("NEGATED", false));
    			condition.setOperator( Condition.getOperator( r.getString("OPERATOR", null) ) );
    			
    			condition.setID( r.getInteger("ID_CONDITION", -1L) );
    			
    			long subids[] = repository.getSubConditionIDs(condition.getID());
    			if (subids.length==0)
    			{
    				condition.setLeftValuename( r.getString("LEFT_NAME", null) );
    				condition.setFunction( Condition.getFunction( r.getString("CONDITION_FUNCTION", null) ) );
    				condition.setRightValuename( r.getString("RIGHT_NAME", null) );
    				
    				long id_value = r.getInteger("ID_VALUE_RIGHT", -1L);
    				if (id_value>0)
    				{
    					ValueMetaAndData v = repository.loadValueMetaAndData(id_value);
    					condition.setRightExact( v );
    				}
    			}
    			else
    			{
    				for (int i=0;i<subids.length;i++)
    				{
    					condition.addCondition( loadCondition(subids[i]) );
    				}
    			}
    			
    			return condition;
            }
            else
            {
                throw new KettleException("Condition with id_condition="+id_condition+" could not be found in the repository");
            }
		}
		catch(KettleException dbe)
		{
			throw new KettleException("Error loading condition from the repository (id_condition="+id_condition+")", dbe);
		}
	}

	public long saveCondition(Condition condition) throws KettleException
	{
		return saveCondition(condition, 0L);
	}
	
	public long saveCondition(Condition condition, long id_condition_parent) throws KettleException
	{
		try
		{
			condition.setID( insertCondition( id_condition_parent, condition ) );
			for (int i=0;i<condition.nrConditions();i++)
			{
				Condition subc = condition.getCondition(i);
				repository.saveCondition(subc, condition.getID());
			}
			
			return condition.getID();
		}
		catch(KettleException dbe)
		{
			throw new KettleException("Error saving condition to the repository.", dbe);
		}
	}

	public synchronized long insertCondition(long id_condition_parent, Condition condition) throws KettleException
	{
		long id = repository.connectionDelegate.getNextConditionID();

		String tablename = Repository.TABLE_R_CONDITION;
		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta(Repository.FIELD_CONDITION_ID_CONDITION, ValueMetaInterface.TYPE_INTEGER), Long.valueOf(id));
		table.addValue(new ValueMeta(Repository.FIELD_CONDITION_ID_CONDITION_PARENT, ValueMetaInterface.TYPE_INTEGER), Long.valueOf(id_condition_parent));
		table.addValue(new ValueMeta(Repository.FIELD_CONDITION_NEGATED, ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(condition.isNegated()));
		table.addValue(new ValueMeta(Repository.FIELD_CONDITION_OPERATOR, ValueMetaInterface.TYPE_STRING), condition.getOperatorDesc());
		table.addValue(new ValueMeta(Repository.FIELD_CONDITION_LEFT_NAME, ValueMetaInterface.TYPE_STRING), condition.getLeftValuename());
		table.addValue(new ValueMeta(Repository.FIELD_CONDITION_CONDITION_FUNCTION, ValueMetaInterface.TYPE_STRING), condition.getFunctionDesc());
		table.addValue(new ValueMeta(Repository.FIELD_CONDITION_RIGHT_NAME, ValueMetaInterface.TYPE_STRING), condition.getRightValuename());

		long id_value = -1L;
		ValueMetaAndData v = condition.getRightExact();

		if (v != null)
		{
			
			// We have to make sure that all data is saved irrespective of locale differences.
			// Here is where we force that
			//
			ValueMetaInterface valueMeta = v.getValueMeta();
			valueMeta.setDecimalSymbol(ValueMetaAndData.VALUE_REPOSITORY_DECIMAL_SYMBOL);
			valueMeta.setGroupingSymbol(ValueMetaAndData.VALUE_REPOSITORY_GROUPING_SYMBOL);
			switch(valueMeta.getType())
			{
			case ValueMetaInterface.TYPE_NUMBER:
				valueMeta.setConversionMask(ValueMetaAndData.VALUE_REPOSITORY_NUMBER_CONVERSION_MASK);
				break;
			case ValueMetaInterface.TYPE_INTEGER:
				valueMeta.setConversionMask(ValueMetaAndData.VALUE_REPOSITORY_INTEGER_CONVERSION_MASK);
				break;
			case ValueMetaInterface.TYPE_DATE:
				valueMeta.setConversionMask(ValueMetaAndData.VALUE_REPOSITORY_DATE_CONVERSION_MASK);
				break;
			default:
				break;
			}
			String stringValue = valueMeta.getString(v.getValueData());
			
			id_value = insertValue(valueMeta.getName(), valueMeta.getTypeDesc(), stringValue, valueMeta.isNull(v.getValueData()), condition.getRightExactID());
			condition.setRightExactID(id_value);
		}
		table.addValue(new ValueMeta(Repository.FIELD_CONDITION_ID_VALUE_RIGHT, ValueMetaInterface.TYPE_INTEGER), new Long(id_value));

		repository.connectionDelegate.getDatabase().prepareInsert(table.getRowMeta(), tablename);
		repository.connectionDelegate.getDatabase().setValuesInsert(table);
		repository.connectionDelegate.getDatabase().insertRow();
		repository.connectionDelegate.getDatabase().closeInsert();

		return id;
	}

	
	public synchronized long insertValue(String name, String type, String value_str, boolean isnull, long id_value_prev) throws KettleException
	{
		long id_value = lookupValue(name, type, value_str, isnull);
		// if it didn't exist yet: insert it!!

		if (id_value < 0)
		{
			id_value = repository.connectionDelegate.getNextValueID();

			// Let's see if the same value is not yet available?
			String tablename = Repository.TABLE_R_VALUE;
			RowMetaAndData table = new RowMetaAndData();
			table.addValue(new ValueMeta(Repository.FIELD_VALUE_ID_VALUE, ValueMetaInterface.TYPE_INTEGER), Long.valueOf(id_value));
			table.addValue(new ValueMeta(Repository.FIELD_VALUE_NAME, ValueMetaInterface.TYPE_STRING), name);
			table.addValue(new ValueMeta(Repository.FIELD_VALUE_VALUE_TYPE, ValueMetaInterface.TYPE_STRING), type);
			table.addValue(new ValueMeta(Repository.FIELD_VALUE_VALUE_STR, ValueMetaInterface.TYPE_STRING), value_str);
			table.addValue(new ValueMeta(Repository.FIELD_VALUE_IS_NULL, ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(isnull));

			repository.connectionDelegate.getDatabase().prepareInsert(table.getRowMeta(), tablename);
			repository.connectionDelegate.getDatabase().setValuesInsert(table);
			repository.connectionDelegate.getDatabase().insertRow();
			repository.connectionDelegate.getDatabase().closeInsert();
		}

		return id_value;
	}

	
	public synchronized long lookupValue(String name, String type, String value_str, boolean isnull) throws KettleException
	{
		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta(Repository.FIELD_VALUE_NAME, ValueMetaInterface.TYPE_STRING), name);
		table.addValue(new ValueMeta(Repository.FIELD_VALUE_VALUE_TYPE, ValueMetaInterface.TYPE_STRING), type);
		table.addValue(new ValueMeta(Repository.FIELD_VALUE_VALUE_STR, ValueMetaInterface.TYPE_STRING), value_str);
		table.addValue(new ValueMeta(Repository.FIELD_VALUE_IS_NULL, ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(isnull));

		String sql = "SELECT " + quote(Repository.FIELD_VALUE_ID_VALUE) + " FROM " + quoteTable(Repository.TABLE_R_VALUE) + " ";
		sql += "WHERE " + quote(Repository.FIELD_VALUE_NAME) + "       = ? ";
		sql += "AND   " + quote(Repository.FIELD_VALUE_VALUE_TYPE) + " = ? ";
		sql += "AND   " + quote(Repository.FIELD_VALUE_VALUE_STR) + "  = ? ";
		sql += "AND   " + quote(Repository.FIELD_VALUE_IS_NULL) + "    = ? ";

		RowMetaAndData result = repository.connectionDelegate.getOneRow(sql, table.getRowMeta(), table.getData());
		if (result != null && result.getData()!=null && result.isNumeric(0))
			return result.getInteger(0, 0L);
		else
			return -1;
	}


	public synchronized int getNrConditions(long id_transforamtion) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+quoteTable(Repository.TABLE_R_TRANS_STEP_CONDITION)+" WHERE "+quote(Repository.FIELD_TRANS_STEP_CONDITION_ID_TRANSFORMATION)+" = " + id_transforamtion;
		RowMetaAndData r = repository.connectionDelegate.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}


	public synchronized int getNrSubConditions(long id_condition) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+quoteTable(Repository.TABLE_R_CONDITION)+" WHERE "+quote(Repository.FIELD_CONDITION_ID_CONDITION_PARENT)+" = " + id_condition;
		RowMetaAndData r = repository.connectionDelegate.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}



}
