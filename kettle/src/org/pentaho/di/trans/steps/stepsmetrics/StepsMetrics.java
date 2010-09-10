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
 
package org.pentaho.di.trans.steps.stepsmetrics;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;

import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.BaseStepData.StepExecutionStatus;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Ouptu step metrics
 * 
 * @author Samatar
 * @since 30-06-2008
 */

public class StepsMetrics extends BaseStep implements StepInterface
{
	private static Class<?> PKG = StepsMetrics.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private StepsMetricsMeta meta;
	private StepsMetricsData data;
	
	public HashSet<StepInterface> stepInterfaces;
	
	public StepsMetrics(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(StepsMetricsMeta)smi;
		data=(StepsMetricsData)sdi;

		if(first) {
			first=false;
			String  stepnames[]=null;  
			int stepnrs=0;
			if(meta.getStepName()!=null && meta.getStepName().length>0)  {
				stepnames=meta.getStepName();
				stepnrs=stepnames.length;
			}else  {
				// error
				throw new KettleException(BaseMessages.getString(PKG, "StepsMetrics.Error.NotSteps"));
			}
			// check for output fields
			data.realstepnamefield=environmentSubstitute(meta.getStepNameFieldName());
			data.realstepidfield=environmentSubstitute(meta.getStepIdFieldName());
			data.realsteplinesinputfield=environmentSubstitute(meta.getStepLinesInputFieldName());
			data.realsteplinesoutputfield=environmentSubstitute(meta.getStepLinesOutputFieldName());
			data.realsteplinesreadfield=environmentSubstitute(meta.getStepLinesReadFieldName());
			data.realsteplineswrittentfield=environmentSubstitute(meta.getStepLinesWrittenFieldName());
			data.realsteplinesupdatedfield=environmentSubstitute(meta.getStepLinesUpdatedFieldName());
			data.realsteplineserrorsfield=environmentSubstitute(meta.getStepLinesErrorsFieldName());
			data.realstepsecondsfield=environmentSubstitute(meta.getStepSecondsFieldName());
			
			// Get target stepnames
			String[] targetSteps= getTransMeta().getNextStepNames(getStepMeta());
			
			data.stepInterfaces= new ConcurrentHashMap<Integer, StepInterface>();
			for(int i=0;i<stepnrs;i++) {
				// We can not get metrics from current step
				if(stepnames[i].equals(getStepname())) throw new KettleException("You can not get metrics for the current step ["+stepnames[i]+"]!");
				if(targetSteps!=null) {
					// We can not metrics from the target steps
					for(int j=0; j<targetSteps.length; j++) {
						if(stepnames[i].equals(targetSteps[j])) throw new KettleException("You can not get metrics for the target step ["+targetSteps[i]+"]!");
					}
				}
				
				int CopyNr=Const.toInt(meta.getStepCopyNr()[i],0);
				StepInterface si =getTrans().getStepInterface(stepnames[i], CopyNr);
				if(si!=null)
					data.stepInterfaces.put(i, getDispatcher().findBaseSteps(stepnames[i]).get(CopyNr));
				else{
					if(meta.getStepRequired()[i].equals(StepsMetricsMeta.YES))
						throw new KettleException("We cannot get step ["+stepnames[i]+"] CopyNr="+CopyNr+"!");
				}
			}
            
            data.outputRowMeta = new RowMeta();
    		meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
		} // end if first
		
	
		 data.continueLoop=true;
	     // Wait until all specified steps have finished!
	      while (data.continueLoop && !isStopped())  {
	    	  data.continueLoop = false;
	    	  Iterator<Entry<Integer, StepInterface>> it= data.stepInterfaces.entrySet().iterator();
	    	  while(it.hasNext())  {
	    		  Entry<Integer, StepInterface> e = it.next();
	    		  StepInterface step = e.getValue();
	
	    		  if (step.getStatus()!=StepExecutionStatus.STATUS_FINISHED)  {
	    			  // This step is still running...
	    			  data.continueLoop=true;
	    		  }else{
	    			  // We have done with this step.
	    			  // remove it from the map
	    			  data.stepInterfaces.remove(e.getKey());
	    			  if(log.isDetailed()) logDetailed("Finished running step ["+ step.getStepname()+"(" + step.getCopy() +")].");
	
    				  // Build an empty row based on the meta-data		  
    				  Object[] rowData=buildEmptyRow();
    				  incrementLinesRead();
	    			  
    				  int index=0;
    				  // step finished
	    			  // output step metrics
	    			  if(!Const.isEmpty(data.realstepnamefield)) rowData[index++]= step.getStepname();
	    			  if(!Const.isEmpty(data.realstepidfield)) rowData[index++]= step.getStepID();
	    			  if(!Const.isEmpty(data.realsteplinesinputfield)) rowData[index++]= (long) step.getLinesInput();
	    			  if(!Const.isEmpty(data.realsteplinesoutputfield)) rowData[index++]= (long) step.getLinesOutput();
	    			  if(!Const.isEmpty(data.realsteplinesreadfield)) rowData[index++]= (long) step.getLinesRead();
	    			  if(!Const.isEmpty(data.realsteplinesupdatedfield)) rowData[index++]= (long) step.getLinesUpdated();
	    			  if(!Const.isEmpty(data.realsteplineswrittentfield)) rowData[index++]= (long) step.getLinesWritten();
	    			  if(!Const.isEmpty(data.realsteplineserrorsfield)) rowData[index++]= (long) step.getErrors();  
	      			  if(!Const.isEmpty(data.realstepsecondsfield)) rowData[index++]= (long) step.getRuntime();  

	    			  // Send row to the buffer
	    			  putRow(data.outputRowMeta, rowData);
	    		  }
	    	   }
    		  if (data.continueLoop) {
		       	try {
					Thread.sleep(200);
				   } catch (Exception d){};
	    	  } 
		}
	      
       setOutputDone();
       return false;
	}

	/**
	 * Build an empty row based on the meta-data...
	 * 
	 * @return
	 */
	
	private Object[] buildEmptyRow()
	{
	    Object[] rowData = RowDataUtil.allocateRowData(data.outputRowMeta.size());
	
		 return rowData;
	}
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(StepsMetricsMeta)smi;
		data=(StepsMetricsData)sdi;
		
		if (super.init(smi, sdi))
		{
		    // Add init code here.
		    return true;
		}
		return false;
	}
	
}
