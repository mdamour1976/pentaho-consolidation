/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.joinrows;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Condition;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;



/*
 * Created on 02-jun-2003
 *
 */
 
public class JoinRowsMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = JoinRowsMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private String  directory;
	private String  prefix;
	private int     cacheSize;
	
	/**Which step is providing the lookup data?*/
	private StepMeta mainStep;
	
	/**Which step is providing the lookup data?*/
	private String   mainStepname;
	
	/**Optional condition to limit the join (where clause)*/
	private Condition condition;

	/**
	 * @return Returns the lookupFromStep.
	 */
	public StepMeta getMainStep()
	{
		return mainStep;
	}
	
	/**
	 * @param lookupFromStep The lookupFromStep to set.
	 */
	public void setMainStep(StepMeta lookupFromStep)
	{
		this.mainStep = lookupFromStep;
	}
	
	/**
	 * @return Returns the lookupFromStepname.
	 */
	public String getMainStepname()
	{
		return mainStepname;
	}
	
	/**
	 * @param lookupFromStepname The lookupFromStepname to set.
	 */
	public void setMainStepname(String lookupFromStepname)
	{
		this.mainStepname = lookupFromStepname;
	}

	/**
     * @param cacheSize The cacheSize to set.
     */
    public void setCacheSize(int cacheSize)
    {
        this.cacheSize = cacheSize;
    }
    
    /**
     * @return Returns the cacheSize.
     */
    public int getCacheSize()
    {
        return cacheSize;
    }
    
    
    
    /**
     * @return Returns the directory.
     */
    public String getDirectory()
    {
        return directory;
    }
    
    /**
     * @param directory The directory to set.
     */
    public void setDirectory(String directory)
    {
        this.directory = directory;
    }
    
    /**
     * @return Returns the prefix.
     */
    public String getPrefix()
    {
        return prefix;
    }
    
    /**
     * @param prefix The prefix to set.
     */
    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }
    
    /**
     * @return Returns the condition.
     */
    public Condition getCondition()
    {
        return condition;
    }
    
    /**
     * @param condition The condition to set.
     */
    public void setCondition(Condition condition)
    {
        this.condition = condition;
    }
    
	public JoinRowsMeta()
	{
		super(); // allocate BaseStepMeta
		condition = new Condition();
	}
	
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	public Object clone()
	{
		JoinRowsMeta retval = (JoinRowsMeta)super.clone();

		return retval;
	}
	
	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
			directory  = XMLHandler.getTagValue(stepnode, "directory"); //$NON-NLS-1$
			prefix     = XMLHandler.getTagValue(stepnode, "prefix"); //$NON-NLS-1$
			cacheSize = Const.toInt( XMLHandler.getTagValue(stepnode, "cache_size"), -1); //$NON-NLS-1$
			
			mainStepname = XMLHandler.getTagValue(stepnode, "main"); //$NON-NLS-1$

			Node compare = XMLHandler.getSubNode(stepnode, "compare"); //$NON-NLS-1$
			Node condnode = XMLHandler.getSubNode(compare, "condition"); //$NON-NLS-1$
	
			// The new situation...
			if (condnode!=null)
			{
				condition = new Condition(condnode);
			}
			else
			{
				condition = new Condition();
			}

		}
		catch(Exception e)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "JoinRowsMeta.Exception.UnableToReadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		directory = "%%java.io.tmpdir%%";; //$NON-NLS-1$
		prefix     = "out"; //$NON-NLS-1$
		cacheSize = 500;

		mainStepname = null;
	}

	public String getXML() throws KettleException
	{
        StringBuffer retval = new StringBuffer(300);

		retval.append("      ").append(XMLHandler.addTagValue("directory",  directory)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      ").append(XMLHandler.addTagValue("prefix",     prefix)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      ").append(XMLHandler.addTagValue("cache_size", cacheSize)); //$NON-NLS-1$ //$NON-NLS-2$
		
		retval.append("      ").append(XMLHandler.addTagValue("main", getLookupStepname())); //$NON-NLS-1$ //$NON-NLS-2$

		retval.append("    <compare>").append(Const.CR); //$NON-NLS-1$
		
		if (condition!=null)
		{
			retval.append(condition.getXML());
		}
		
		retval.append("    </compare>").append(Const.CR); //$NON-NLS-1$

		return retval.toString();	
	}

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
		try
		{
			directory        =      rep.getStepAttributeString (id_step, "directory"); //$NON-NLS-1$
			prefix           =      rep.getStepAttributeString (id_step, "prefix"); //$NON-NLS-1$
			cacheSize        = (int)rep.getStepAttributeInteger(id_step, "cache_size"); //$NON-NLS-1$
	
			mainStepname     =  rep.getStepAttributeString (id_step, "main"); //$NON-NLS-1$

			condition = rep.loadConditionFromStepAttribute(id_step, "id_condition");
			if (condition==null) {
				condition = new Condition();
			}
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "JoinRowsMeta.Exception.UnexpectedErrorInReadStepInfoFromRepository"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "directory",       directory); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "prefix",          prefix); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "cache_size",      cacheSize); //$NON-NLS-1$
	
			rep.saveStepAttribute(id_transformation, id_step, "main",  getLookupStepname()); //$NON-NLS-1$
			
			rep.saveConditionStepAttribute(id_transformation, id_step, "id_condition", condition);
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "JoinRowsMeta.Exception.UnableToSaveStepInfoToRepository")+id_step, e); //$NON-NLS-1$
		}
	}

	public void getFields(RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
	  if (space instanceof TransMeta) {
	    TransMeta transMeta = (TransMeta) space;
	    StepMeta[] steps = transMeta.getPrevSteps(transMeta.findStep(origin));
	    StepMeta mainStep = transMeta.findStep(getMainStepname());
	    rowMeta.clear();
	    if (mainStep!=null) {
	      rowMeta.addRowMeta(transMeta.getStepFields(mainStep));
	    }
	    for (StepMeta step : steps) {
	      if (mainStep==null || !step.equals(mainStep)) {
	        rowMeta.addRowMeta(transMeta.getStepFields(step));
	      }
	    }
	  }
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		
		if (prev!=null && prev.size()>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "JoinRowsMeta.CheckResult.StepReceivingDatas",prev.size()+""), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
			
			// Check the sort directory
            String realDirectory = transMeta.environmentSubstitute(directory); 
			File f = new File( realDirectory );
			if (f.exists())
			{
				if (f.isDirectory())
				{
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, "["+realDirectory+BaseMessages.getString(PKG, "JoinRowsMeta.CheckResult.DirectoryExists"), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$
					remarks.add(cr);
				}
				else
				{
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, "["+realDirectory+BaseMessages.getString(PKG, "JoinRowsMeta.CheckResult.DirectoryExistsButNotValid"), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$
					remarks.add(cr);
				}
			}
			else
			{
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "JoinRowsMeta.CheckResult.DirectoryDoesNotExist",realDirectory), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$
				remarks.add(cr);
			}
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "JoinRowsMeta.CheckResult.CouldNotFindFieldsFromPreviousSteps"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "JoinRowsMeta.CheckResult.StepReceivingInfoFromOtherSteps"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "JoinRowsMeta.CheckResult.NoInputReceived"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
	}

	public String getLookupStepname()
	{
		if (mainStep!=null && 
			mainStep.getName()!=null &&
			mainStep.getName().length()>0
		   ) 
			return mainStep.getName();
		return null;
	}

	/**
	 * @param steps optionally search the info step in a list of steps
	 */
	public void searchInfoAndTargetSteps(List<StepMeta> steps)
	{
		mainStep = StepMeta.findStep(steps, mainStepname);
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new JoinRows(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new JoinRowsData();
	}

    public boolean excludeFromRowLayoutVerification()
    {
        return true;
    }
    
    
}
