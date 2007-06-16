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
package org.pentaho.di.trans;
import java.util.ArrayList;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;


/*
 * Created on 5-apr-2004
 *
 */

public class TransDependency implements XMLInterface
{
	public static final String XML_TAG = "dependency";
    
    private DatabaseMeta db;
	private String tablename;
	private String fieldname;
	
	private long id;
	
	public TransDependency(DatabaseMeta db, String tablename, String fieldname)
	{
		this.db = db;
		this.tablename = tablename;
		this.fieldname = fieldname;
	}

	public TransDependency()
	{
		this(null, null, null);
	}
    
    public String getXML()
    {
        StringBuffer xml = new StringBuffer();
        
        xml.append("      ").append(XMLHandler.openTag(XML_TAG)).append(Const.CR); //$NON-NLS-1$
        xml.append("        ").append(XMLHandler.addTagValue("connection", db==null?"":db.getName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        xml.append("        ").append(XMLHandler.addTagValue("table",      tablename)); //$NON-NLS-1$ //$NON-NLS-2$
        xml.append("        ").append(XMLHandler.addTagValue("field",      fieldname)); //$NON-NLS-1$ //$NON-NLS-2$
        xml.append("       ").append(XMLHandler.closeTag(XML_TAG)).append(Const.CR); //$NON-NLS-1$
        
        return xml.toString();
    }
    
	public TransDependency(Node depnode, ArrayList databases) throws KettleXMLException
	{
		try
		{
			String depcon    = XMLHandler.getTagValue(depnode, "connection"); //$NON-NLS-1$
			db = DatabaseMeta.findDatabase(databases, depcon);
			tablename        = XMLHandler.getTagValue(depnode, "table"); //$NON-NLS-1$
			fieldname        = XMLHandler.getTagValue(depnode, "field"); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("TransDependency.Exception.UnableToLoadTransformation"), e); //$NON-NLS-1$
		}
	}

	public void setID(long id)
	{
		this.id = id;
	}
	
	public long getID()
	{
		return id;
	}
	
	public void setDatabase(DatabaseMeta db)
	{
		this.db = db;
	}
	
	public DatabaseMeta getDatabase()
	{
		return db;
	}
	
	public void setTablename(String tablename)
	{
		this.tablename = tablename;
	}
	
	public String getTablename()
	{
		return tablename;
	}
	
	public void setFieldname(String fieldname)
	{
		this.fieldname = fieldname;
	}
	
	public String getFieldname()
	{
		return fieldname;
	}

	public TransDependency(Repository rep, long id_dependency, ArrayList databases) throws KettleException
	{
		try
		{
			setID(id_dependency);
			
			RowMetaAndData r = rep.getTransDependency(id_dependency);
			
			if (r!=null)
			{
				long id_connection = r.getInteger("ID_DATABASE", 0); //$NON-NLS-1$
				db        = DatabaseMeta.findDatabase(databases, id_connection);
				tablename = r.getString("TABLE_NAME", null); //$NON-NLS-1$
				fieldname = r.getString("FIELD_NAME", null); //$NON-NLS-1$
			}
		}
		catch(KettleException dbe)
		{
			throw new KettleException(Messages.getString("TransDependency.Exception.UnableToLoadTransformationDependency")+id_dependency, dbe); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, long id_transformation) throws KettleException
	{
		try
		{
			long id_database = db==null?-1:db.getID();
			
			setID( rep.insertDependency(id_transformation, id_database, tablename, fieldname) );
		}
		catch(KettleException dbe)
		{
			throw new KettleException(Messages.getString("TransDependency.Exception.UnableToSaveTransformationDepency")+id_transformation, dbe); //$NON-NLS-1$
		}
	}
}
