 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 

package org.pentaho.di.trans.steps.gettablenames;

import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/*
 * Created on 03-Juin-2008
 * 
 */

public class GetTableNamesMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = GetTableNamesMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	
    /** database connection */
    private DatabaseMeta database;

    /** function result: new value name */
    private String       tablenamefieldname;
    private String objecttypefieldname;
    private String issystemobjectfieldname;
    
    private boolean includeTable;
    private boolean includeView;
    private boolean includeProcedure;
    private boolean includeSynonym;

    public GetTableNamesMeta()
    {
        super(); // allocate BaseStepMeta
    }
  

    /**
     * @return Returns the database.
     */
    public DatabaseMeta getDatabase()
    {
        return database;
    }

    /**
     * @param database The database to set.
     */
    public void setDatabase(DatabaseMeta database)
    {
        this.database = database;
    }


    /**
     * @return Returns the resultName.
     */
    public String getTablenameFieldName()
    {
        return tablenamefieldname;
    }

    /**
     * @param tablenamefieldname The tablenamefieldname to set.
     */
    public void setTablenameFieldName(String tablenamefieldname)
    {
        this.tablenamefieldname = tablenamefieldname;
    }
    /**
     * @param objecttypefieldname The objecttypefieldname to set.
     */
    public void setObjectTypeFieldName(String objecttypefieldname)
    {
        this.objecttypefieldname = objecttypefieldname;
    }
    /**
     * @param issystemobjectfieldname The issystemobjectfieldname to set.
     */
    public void setIsSystemObjectFieldName(String issystemobjectfieldname)
    {
        this.issystemobjectfieldname = issystemobjectfieldname;
    }
    
    /**
     * @return Returns the objecttypefieldname.
     */
    public String getObjectTypeFieldName()
    {
        return objecttypefieldname;
    }
    /**
     * @return Returns the issystemobjectfieldname.
     */
    public String isSystemObjectFieldName()
    {
        return issystemobjectfieldname;
    }
    
    
    public void setIncludeTable(boolean includetable)
    {
    	this.includeTable=includetable;
    }
    public boolean isIncludeTable()
    {
    	return this.includeTable;
    }
    public void setIncludeView(boolean includeView)
    {
    	this.includeView=includeView;
    }
    public boolean isIncludeView()
    {
    	return this.includeView;
    }
    public void setIncludeProcedure(boolean includeProcedure)
    {
    	this.includeProcedure=includeProcedure;
    }
    public boolean isIncludeProcedure()
    {
    	return this.includeProcedure;
    }
    public void setIncludeSyonym(boolean includeSynonym)
    {
    	this.includeSynonym=includeSynonym;
    }
    public boolean isIncludeSyonym()
    {
    	return this.includeSynonym;
    }
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) 
    throws KettleXMLException {
		readData(stepnode, databases);
	}

    public Object clone()
    {
        GetTableNamesMeta retval = (GetTableNamesMeta) super.clone();
       
        return retval;
    }

    public void setDefault()
    {
        database = null;
        includeTable=true;
        includeProcedure=true;
        includeView=true;
        includeSynonym=true;
        tablenamefieldname = "tablename"; //$NON-NLS-1$
        objecttypefieldname="type";
        issystemobjectfieldname="is system";
    }

	public void getFields(RowMetaInterface r, String name, RowMetaInterface info[], StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		String realtablename=space.environmentSubstitute(tablenamefieldname);
		if (!Const.isEmpty(realtablename))
		{
			ValueMetaInterface v = new ValueMeta(realtablename, ValueMeta.TYPE_STRING); 
			v.setLength(500);
			v.setPrecision(-1);
			v.setOrigin(name);
			r.addValueMeta(v);
		}
		 
		String realObjectType=space.environmentSubstitute(objecttypefieldname);
		if (!Const.isEmpty(realObjectType))
		{
			ValueMetaInterface v = new ValueMeta(realObjectType, ValueMeta.TYPE_STRING); 
			v.setLength(500);
			v.setPrecision(-1);
			v.setOrigin(name);
			r.addValueMeta(v);
		}
		String sysobject=space.environmentSubstitute(issystemobjectfieldname);
		if (!Const.isEmpty(sysobject))
		{
			ValueMetaInterface v = new ValueMeta(sysobject, ValueMeta.TYPE_BOOLEAN); 
			v.setOrigin(name);
			r.addValueMeta(v);
		}
    }

    public String getXML()
    {
        StringBuffer retval = new StringBuffer();

        retval.append("    " + XMLHandler.addTagValue("connection", database == null ? "" : database.getName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        retval.append("    " + XMLHandler.addTagValue("tablenamefieldname", tablenamefieldname)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    " + XMLHandler.addTagValue("objecttypefieldname", objecttypefieldname)); 
        retval.append("    " + XMLHandler.addTagValue("issystemobjectfieldname", issystemobjectfieldname)); 
        retval.append("    " + XMLHandler.addTagValue("includeTable", includeTable));
        retval.append("    " + XMLHandler.addTagValue("includeView", includeView));
        retval.append("    " + XMLHandler.addTagValue("includeProcedure", includeProcedure));
        retval.append("    " + XMLHandler.addTagValue("includeSynonym", includeSynonym));
        return retval.toString();
    }

	private void readData(Node stepnode, List<? extends SharedObjectInterface> databases) throws KettleXMLException
	{
        try
        {

            String con = XMLHandler.getTagValue(stepnode, "connection"); //$NON-NLS-1$
            database = DatabaseMeta.findDatabase(databases, con);
            tablenamefieldname = XMLHandler.getTagValue(stepnode, "tablenamefieldname"); //$NON-NLS-1$
            objecttypefieldname = XMLHandler.getTagValue(stepnode, "objecttypefieldname"); 
            issystemobjectfieldname = XMLHandler.getTagValue(stepnode, "issystemobjectfieldname"); 
            includeTable  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "includeTable"));
            includeView  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "includeView"));
            includeProcedure  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "includeProcedure"));
            includeSynonym  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "includeSynonym"));
        }
        catch (Exception e)
        {
            throw new KettleXMLException(BaseMessages.getString(PKG, "GetTableNamesMeta.Exception.UnableToReadStepInfo"), e); //$NON-NLS-1$
        }
    }

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) 
	throws KettleException {
		try
		{
			database = rep.loadDatabaseMetaFromStepAttribute(id_step, "id_connection", databases);
            tablenamefieldname = rep.getStepAttributeString(id_step, "tablenamefieldname"); //$NON-NLS-1$
            objecttypefieldname = rep.getStepAttributeString(id_step, "objecttypefieldname");
            issystemobjectfieldname = rep.getStepAttributeString(id_step, "issystemobjectfieldname");
            includeTable = rep.getStepAttributeBoolean(id_step, "includeTable"); 
            includeView = rep.getStepAttributeBoolean(id_step, "includeView");
            includeProcedure = rep.getStepAttributeBoolean(id_step, "includeProcedure");
            includeSynonym = rep.getStepAttributeBoolean(id_step, "includeSynonym");
        }
        catch (Exception e)
        {
            throw new KettleException(BaseMessages.getString(PKG, "GetTableNamesMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
        }
    }

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
	throws KettleException
	{
        try
        {
    		rep.saveDatabaseMetaStepAttribute(id_transformation, id_step, "id_connection", database);
            rep.saveStepAttribute(id_transformation, id_step, "tablenamefieldname", tablenamefieldname); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "objecttypefieldname", objecttypefieldname);
            rep.saveStepAttribute(id_transformation, id_step, "issystemobjectfieldname", issystemobjectfieldname);
            // Also, save the step-database relationship!
            if (database != null) rep.insertStepDatabase(id_transformation, id_step, database.getObjectId());
            rep.saveStepAttribute(id_transformation, id_step, "includeTable", includeTable);
            rep.saveStepAttribute(id_transformation, id_step, "includeView", includeView);
            rep.saveStepAttribute(id_transformation, id_step, "includeProcedure", includeProcedure);
            rep.saveStepAttribute(id_transformation, id_step, "includeSynonym", includeSynonym);
        }
        catch (Exception e)
        {
            throw new KettleException(BaseMessages.getString(PKG, "GetTableNamesMeta.Exception.UnableToSaveStepInfo") + id_step, e); //$NON-NLS-1$
        }
    }

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
        CheckResult cr;
        String error_message = ""; //$NON-NLS-1$

        if (database == null)
        {
            error_message = BaseMessages.getString(PKG, "GetTableNamesMeta.CheckResult.InvalidConnection"); //$NON-NLS-1$
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
            remarks.add(cr);
        }
        if (Const.isEmpty(tablenamefieldname))
        {
            error_message = BaseMessages.getString(PKG, "GetTableNamesMeta.CheckResult.TablenameFieldNameMissing"); //$NON-NLS-1$
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
            remarks.add(cr);
        }
        else
        {
            error_message = BaseMessages.getString(PKG, "GetTableNamesMeta.CheckResult.TablenameFieldNameOK"); //$NON-NLS-1$
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, error_message, stepMeta);
            remarks.add(cr);
        }
       
        // See if we have input streams leading to this step!
        if (input.length > 0)
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "GetTableNamesMeta.CheckResult.NoInpuReceived"), stepMeta); //$NON-NLS-1$
        else
        	cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "GetTableNamesMeta.CheckResult.ReceivingInfoFromOtherSteps"), stepMeta); //$NON-NLS-1$
        remarks.add(cr);   
        

    }

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
    {
        return new GetTableNames(stepMeta, stepDataInterface, cnr, transMeta, trans);
    }

    public StepDataInterface getStepData()
    {
        return new GetTableNamesData();
    }

    public DatabaseMeta[] getUsedDatabaseConnections()
    {
        if (database != null)
        {
            return new DatabaseMeta[] { database };
        }
        else
        {
            return super.getUsedDatabaseConnections();
        }
    }
    public boolean supportsErrorHandling()
    {
        return true;
    }
}
