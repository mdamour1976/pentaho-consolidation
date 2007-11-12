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

package org.pentaho.di.core.database;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * Contains Oracle specific information through static final members 
 * 
 * @author Matt
 * @since  11-mrt-2005
 */
public class OracleDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface
{
	/**
	 * Construct a new database connections.  Note that not all these parameters are not allways mandatory.
	 * 
	 * @param name The database name
	 * @param access The type of database access
	 * @param host The hostname or IP address
	 * @param db The database name
	 * @param port The port on which the database listens.
	 * @param user The username
	 * @param pass The password
	 */
	public OracleDatabaseMeta(String name, String access, String host, String db, String port, String user, String pass)
	{
		super(name, access, host, db, port, user, pass);
	}
	
	public OracleDatabaseMeta()
	{
	}

	public String getDatabaseTypeDesc()
	{
		return "ORACLE";
	}

	public String getDatabaseTypeDescLong()
	{
		return "Oracle";
	}
	
	/**
	 * @return Returns the databaseType.
	 */
	public int getDatabaseType()
	{
		return DatabaseMeta.TYPE_DATABASE_ORACLE;
	}
		
	public int[] getAccessTypeList()
	{
		return new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_ODBC, DatabaseMeta.TYPE_ACCESS_OCI, DatabaseMeta.TYPE_ACCESS_JNDI };
	}
	
	public int getDefaultDatabasePort()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_NATIVE) return 1521;
		return -1;
	}
	
	/**
	 * @return Whether or not the database can use auto increment type of fields (pk)
	 */
	public boolean supportsAutoInc()
	{
		return false;
	}
	
	/**
	 * @see org.pentaho.di.core.database.DatabaseInterface#getLimitClause(int)
	 */
	public String getLimitClause(int nrRows)
	{
		return " WHERE ROWNUM <= "+nrRows;
	}
	
	/**
	 * Returns the minimal SQL to launch in order to determine the layout of the resultset for a given database table
	 * @param tableName The name of the table to determine the layout for
	 * @return The SQL to launch.
	 */
	public String getSQLQueryFields(String tableName)
	{
	    return "SELECT /*+FIRST_ROWS*/ * FROM "+tableName+" WHERE ROWNUM < 1";
	}

    public String getSQLTableExists(String tablename)
    {
        return getSQLQueryFields(tablename);
    }
    
    public boolean needsToLockAllTables()
    {
        return false;
    }
	
	public String getDriverClass()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_ODBC)
		{
			return "sun.jdbc.odbc.JdbcOdbcDriver";
		}
		else
		{
			return "oracle.jdbc.driver.OracleDriver";
		}
	}


	
    public String getURL(String hostname, String port, String databaseName) throws KettleDatabaseException
    {
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_ODBC)
		{
			return "jdbc:odbc:"+databaseName;
		}
		else
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_NATIVE)
		{
			return "jdbc:oracle:thin:@"+hostname+":"+port+":"+databaseName;
		}
		else // OCI
		{
		    // Let's see if we have an database name
            if (getDatabaseName()!=null && getDatabaseName().length()>0)
            {
                // Has the user specified hostname & port number?
                if (getHostname()!=null && getHostname().length()>0 && getDatabasePortNumberString()!=null && getDatabasePortNumberString().length()>0) {
                    // User wants the full url
                    return "jdbc:oracle:oci:@(description=(address=(host="+getHostname()+")(protocol=tcp)(port="+getDatabasePortNumberString()+"))(connect_data=(sid="+getDatabaseName()+")))";
                } else {
                    // User wants the shortcut url
                    return "jdbc:oracle:oci:@"+getDatabaseName();
                }               
            }
            else
            {
                throw new KettleDatabaseException("Unable to construct a JDBC URL: at least the database name must be specified");
            }
		}
	}
    
    /**
     * Oracle doesn't support options in the URL, we need to put these in a Properties object at connection time...
     */
    public boolean supportsOptionsInURL()
    {
        return false;
    }

	/**
	 * @return true if the database supports sequences
	 */
	public boolean supportsSequences()
	{
		return true;
	}

    /**
     * Check if a sequence exists.
     * @param sequenceName The sequence to check
     * @return The SQL to get the name of the sequence back from the databases data dictionary
     */
    public String getSQLSequenceExists(String sequenceName)
    {
        return "SELECT * FROM USER_SEQUENCES WHERE SEQUENCE_NAME = '"+sequenceName.toUpperCase()+"'";
    }
    
    /**
     * Get the current value of a database sequence
     * @param sequenceName The sequence to check
     * @return The current value of a database sequence
     */
    public String getSQLCurrentSequenceValue(String sequenceName)
    {
        return "SELECT "+sequenceName+".currval FROM DUAL";
    }

    /**
     * Get the SQL to get the next value of a sequence. (Oracle only) 
     * @param sequenceName The sequence name
     * @return the SQL to get the next value of a sequence. (Oracle only)
     */
    public String getSQLNextSequenceValue(String sequenceName)
    {
        return "SELECT "+sequenceName+".nextval FROM dual";
    }


	/**
	 * @return true if we need to supply the schema-name to getTables in order to get a correct list of items.
	 */
	public boolean useSchemaNameForTableList()
	{
		return true;
	}

	/**
	 * @return true if the database supports synonyms
	 */
	public boolean supportsSynonyms()
	{
		return true;
	}

	/**
	 * Generates the SQL statement to add a column to the specified table
	 * @param tablename The table to add
	 * @param v The column defined as a value
	 * @param tk the name of the technical key field
	 * @param use_autoinc whether or not this field uses auto increment
	 * @param pk the name of the primary key field
	 * @param semicolon whether or not to add a semi-colon behind the statement.
	 * @return the SQL statement to add a column to the specified table
	 */
	public String getAddColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon)
	{
		return "ALTER TABLE "+tablename+" ADD ( "+getFieldDefinition(v, tk, pk, use_autoinc, true, false)+" ) ";
	}

	/**
	 * Generates the SQL statement to drop a column from the specified table
	 * @param tablename The table to add
	 * @param v The column defined as a value
	 * @param tk the name of the technical key field
	 * @param use_autoinc whether or not this field uses auto increment
	 * @param pk the name of the primary key field
	 * @param semicolon whether or not to add a semi-colon behind the statement.
	 * @return the SQL statement to drop a column from the specified table
	 */
	public String getDropColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon)
	{
		return "ALTER TABLE "+tablename+" DROP ( "+v.getName()+" ) "+Const.CR;
	}

	/**
	 * Generates the SQL statement to modify a column in the specified table
	 * @param tablename The table to add
	 * @param v The column defined as a value
	 * @param tk the name of the technical key field
	 * @param use_autoinc whether or not this field uses auto increment
	 * @param pk the name of the primary key field
	 * @param semicolon whether or not to add a semi-colon behind the statement.
	 * @return the SQL statement to modify a column in the specified table
	 */
	public String getModifyColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon)
	{
        ValueMetaInterface tmpColumn = v.clone(); 
        int threeoh = v.getName().length()>=30 ? 30 : v.getName().length();
        
        tmpColumn.setName(v.getName().substring(0,threeoh)+"_KTL"); // should always be less then 35
        
        String sql="";
        
        // Create a new tmp column
        sql+=getAddColumnStatement(tablename, tmpColumn, tk, use_autoinc, pk, semicolon)+";"+Const.CR;
        // copy the old data over to the tmp column
        sql+="UPDATE "+tablename+" SET "+tmpColumn.getName()+"="+v.getName()+";"+Const.CR;
        // drop the old column
        sql+=getDropColumnStatement(tablename, v, tk, use_autoinc, pk, semicolon)+";"+Const.CR;
        // create the wanted column
        sql+=getAddColumnStatement(tablename, v, tk, use_autoinc, pk, semicolon)+";"+Const.CR;
        // copy the data from the tmp column to the wanted column (again)  
        // All this to avoid the rename clause as this is not supported on all Oracle versions
        sql+="UPDATE "+tablename+" SET "+v.getName()+"="+tmpColumn.getName()+";"+Const.CR;
        // drop the temp column
        sql+=getDropColumnStatement(tablename, tmpColumn, tk, use_autoinc, pk, semicolon);
        
        return sql;
	}

	public String getFieldDefinition(ValueMetaInterface v, String tk, String pk, boolean use_autoinc, boolean add_fieldname, boolean add_cr)
	{
		StringBuffer retval=new StringBuffer(128);
		
		String fieldname = v.getName();
		int    length    = v.getLength();
		int    precision = v.getPrecision();
		
		if (add_fieldname) retval.append(fieldname).append(' ');
		
		int type         = v.getType();
		switch(type)
		{
		case ValueMetaInterface.TYPE_DATE   : retval.append("DATE"); break;
		case ValueMetaInterface.TYPE_BOOLEAN: retval.append("CHAR(1)"); break;
		case ValueMetaInterface.TYPE_NUMBER : 
        case ValueMetaInterface.TYPE_BIGNUMBER: 
			retval.append("NUMBER"); 
			if (length>0)
			{
				retval.append('(').append(length);
				if (precision>0)
				{
					retval.append(", ").append(precision);
				}
				retval.append(')');
			}
			break;
		case ValueMetaInterface.TYPE_INTEGER:  
			retval.append("INTEGER"); 
			break;			
		case ValueMetaInterface.TYPE_STRING:
			if (length>=DatabaseMeta.CLOB_LENGTH)
			{
				retval.append("CLOB");
			}
			else
			{
				if (length>0 && length<=2000)
				{
					retval.append("VARCHAR2(").append(length).append(')');
				}
				else
				{
                    if (length<=0)
                    {
                        retval.append("VARCHAR2(2000)"); // We don't know, so we just use the maximum...
                    }
                    else
                    {
                        retval.append("CLOB"); 
                    }
				}
			}
			break;
        case ValueMetaInterface.TYPE_BINARY: // the BLOB can contain binary data.
            {
                retval.append("BLOB");
            }
            break;
		default:
			retval.append(" UNKNOWN");
			break;
		}
		
		if (add_cr) retval.append(Const.CR);
		
		return retval.toString();
	}
	
	/* (non-Javadoc)
	 * @see com.ibridge.kettle.core.database.DatabaseInterface#getReservedWords()
	 */
	public String[] getReservedWords()
	{
		return new String[] 
	     {
			"ACCESS", "ADD", "ALL", "ALTER", "AND", "ANY", "ARRAYLEN", "AS", "ASC", "AUDIT", "BETWEEN",
			"BY", "CHAR", "CHECK", "CLUSTER", "COLUMN", "COMMENT", "COMPRESS", "CONNECT", "CREATE", "CURRENT", "DATE",
			"DECIMAL", "DEFAULT", "DELETE", "DESC", "DISTINCT", "DROP", "ELSE", "EXCLUSIVE", "EXISTS", "FILE", "FLOAT",
			"FOR", "FROM", "GRANT", "GROUP", "HAVING", "IDENTIFIED", "IMMEDIATE", "IN", "INCREMENT", "INDEX", "INITIAL",
			"INSERT", "INTEGER", "INTERSECT", "INTO", "IS", "LEVEL", "LIKE", "LOCK", "LONG", "MAXEXTENTS", "MINUS",
			"MODE", "MODIFY", "NOAUDIT", "NOCOMPRESS", "NOT", "NOTFOUND", "NOWAIT", "NULL", "NUMBER", "OF", "OFFLINE",
			"ON", "ONLINE", "OPTION", "OR", "ORDER", "PCTFREE", "PRIOR", "PRIVILEGES", "PUBLIC", "RAW", "RENAME",
			"RESOURCE", "REVOKE", "ROW", "ROWID", "ROWLABEL", "ROWNUM", "ROWS", "SELECT", "SESSION", "SET", "SHARE",
			"SIZE", "SMALLINT", "SQLBUF", "START", "SUCCESSFUL", "SYNONYM", "SYSDATE", "TABLE", "THEN", "TO", "TRIGGER",
			"UID", "UNION", "UNIQUE", "UPDATE", "USER", "VALIDATE", "VALUES", "VARCHAR", "VARCHAR2", "VIEW", "WHENEVER",
			"WHERE", "WITH"
		 };
	}
	
	/**
	 * @return The SQL on this database to get a list of stored procedures.
	 */
	public String getSQLListOfProcedures()
	{
		return  "SELECT DISTINCT DECODE(package_name, NULL, '', package_name||'.')||object_name FROM user_arguments"; 
	}

    public String getSQLLockTables(String tableNames[])
    {
        StringBuffer sql=new StringBuffer(128);
        for (int i=0;i<tableNames.length;i++)
        {
            sql.append("LOCK TABLE ").append(tableNames[i]).append(" IN EXCLUSIVE MODE;").append(Const.CR);
        }
        return sql.toString();
    }
    
    public String getSQLUnlockTables(String tableNames[])
    {
        return null; // commit handles the unlocking!
    }
    
    /**
     * @return extra help text on the supported options on the selected database platform.
     */
    public String getExtraOptionsHelpText()
    {
        return  "http://www.oracle.com/technology/tech/java/sqlj_jdbc/htdocs/jdbc_faq.htm#05_00";
    }

    public String[] getUsedLibraries()
    {
        return new String[] { "ojdbc14.jar", "orai18n.jar" };
    }
}
