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

package org.pentaho.di.trans.steps.simplemapping;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransMeta.TransformationType;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.mapping.MappingParameters;
import org.pentaho.di.trans.steps.mapping.MappingValueRename;
import org.pentaho.di.trans.steps.mappinginput.MappingInput;
import org.pentaho.di.trans.steps.mappingoutput.MappingOutput;

/**
 * Execute a mapping: a re-usuable transformation
 * 
 * @author Matt
 * @since 22-nov-2005
 */
public class SimpleMapping extends BaseStep implements StepInterface
{
	private static Class<?> PKG = SimpleMappingMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private SimpleMappingMeta meta;
	private SimpleMappingData data;
	
	public SimpleMapping(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
    /**
     * Process a single row.  In our case, we send one row of data to a piece of transformation.
     * In the transformation, we look up the MappingInput step to send our rows to it.
     * As a consequence, for the time being, there can only be one MappingInput and one MappingOutput step in the Mapping.
     */
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		try
		{
			meta=(SimpleMappingMeta)smi;
			data=(SimpleMappingData)sdi;
			
			if (first) {
			  first=false;

		    // Rows read are injected into the one available Mapping Input step
		    //
		    String mappingInputStepname = data.mappingInput.getStepname();
		    RowProducer rowProducer = data.mappingTrans.addRowProducer(mappingInputStepname, 0);
		    data.rowDataInputMapper = new RowDataInputMapper(meta.getInputMapping(), rowProducer);

        // Rows produced by the mapping are read and passed on.
		    //
		    String mappingOutputStepname = data.mappingOutput.getStepname();
		    StepInterface outputStepInterface = data.mappingTrans.findStepInterface(mappingOutputStepname, 0);
		    RowOutputDataMapper outputDataMapper = new RowOutputDataMapper(meta.getInputMapping(), 
		        meta.getOutputMapping(), new PutRowInterface() {
          
          @Override
          public void putRow(RowMetaInterface rowMeta, Object[] rowData) throws KettleStepException {
            SimpleMapping.this.putRow(rowMeta, rowData); 
          }
        });
		    outputStepInterface.addRowListener(outputDataMapper);
			  
		    
        // Start the mapping/sub-transformation threads
        //
        data.mappingTrans.startThreads();
			}

			// The data we read we pass to the mapping
			//
		  Object[] row = getRow();
		  if (row!=null) {
		    data.rowDataInputMapper.putRow(getInputRowMeta(), row);
		  } else {
		    data.rowDataInputMapper.finished();
		    data.mappingTrans.waitUntilFinished();
		    setOutputDone();
		    return false;
		  }

      return true;    		    	     
    }
    catch(Throwable t)
    {
      // Some unexpected situation occurred.
      // Better to stop the mapping transformation.
      //
      if (data.mappingTrans!=null) {
        data.mappingTrans.stopAll();
      }
      
      // Forward the exception...
      //
      throw new KettleException(t);
    }
	}

	private void setMappingParameters() throws KettleException {
		MappingParameters mappingParameters = meta.getMappingParameters();
		if (mappingParameters!=null) {
			
	    String[] parameters;
	    String[] parameterValues;
	    
	    if (mappingParameters.isInheritingAllVariables()) {
	      // We pass the values for all the parameters from the parent transformation
	      //
	      parameters = data.mappingTransMeta.listParameters();
	      parameterValues = new String[parameters.length];
	      for (int i=0;i<parameters.length;i++) {
	        parameterValues[i] = getVariable(parameters[i]);
	      }
	    } else {
	      // We pass down the listed variables with the specified values...
	      //
	      parameters = mappingParameters.getVariable();
	      parameterValues = new String[parameters.length];
	      for (int i=0;i<parameters.length;i++) {
	        parameterValues[i] = environmentSubstitute(mappingParameters.getInputField()[i]);
	      }
	    }
	    
	    for (int i=0;i<parameters.length;i++) {
	      String value = Const.NVL(parameterValues[i], "");
	      
	      data.mappingTrans.setParameterValue(parameters[i], value);
	    }
	    
	    data.mappingTrans.activateParameters();
		}
	}

  public void prepareMappingExecution() throws KettleException {
        
    // Create the transformation from meta-data...
    //
    data.mappingTrans = new Trans(data.mappingTransMeta, getTrans());

    // Set the parameters values in the mapping.
    //
    setMappingParameters();

    if (data.mappingTransMeta.getTransformationType() != TransformationType.Normal) {
      data.mappingTrans.getTransMeta().setUsingThreadPriorityManagment(false);
    }

    // Leave a path up so that we can set variables in sub-transformations...
    //
    data.mappingTrans.setParentTrans(getTrans());

    // Pass down the safe mode flag to the mapping...
    //
    data.mappingTrans.setSafeModeEnabled(getTrans().isSafeModeEnabled());

    // Pass down the metrics gathering flag:
    //
    data.mappingTrans.setGatheringMetrics(getTrans().isGatheringMetrics());
    
    // Also set the name of this step in the mapping transformation for logging
    // purposes
    //
    data.mappingTrans.setMappingStepName(getStepname());
    
    // Also pass servlet information (if any)
    //
    data.mappingTrans.setServletPrintWriter(getTrans().getServletPrintWriter());
    data.mappingTrans.setServletReponse(getTrans().getServletResponse());
    data.mappingTrans.setServletRequest(getTrans().getServletRequest());

    // We launch the transformation in the processRow when the first row is
    // received.
    // This will allow the correct variables to be passed.
    // Otherwise the parent is the init() thread which will be gone once the
    // init is done.
    //
    try {
      data.mappingTrans.prepareExecution(getTrans().getArguments());
    } catch (KettleException e) {
      throw new KettleException(BaseMessages.getString(PKG, "SimpleMapping.Exception.UnableToPrepareExecutionOfMapping"), e);
    }

    // If there is no read/write logging step set, we can insert the data from
    // the first mapping input/output step...
    //
    MappingInput[] mappingInputs = data.mappingTrans.findMappingInput();
    if (mappingInputs.length==0) {
      throw new KettleException("The simple mapping step needs one Mapping Input step to write to in the sub-transformation");
    }
    if (mappingInputs.length>1) {
      throw new KettleException("The simple mapping step does not support multiple Mapping Input steps to write to in the sub-transformation");
    }
    
    data.mappingInput = mappingInputs[0];
    data.mappingInput.setConnectorSteps(new StepInterface[0], new ArrayList<MappingValueRename>(), null);
    
    // LogTableField readField = data.mappingTransMeta.getTransLogTable().findField(TransLogTable.ID.LINES_READ);
    // if (readField.getSubject() == null) {
    //  readField.setSubject(data.mappingInput.getStepMeta());
    // }

    MappingOutput[] mappingOutputs = data.mappingTrans.findMappingOutput();
    if (mappingOutputs.length==0) {
      throw new KettleException("The simple mapping step needs one Mapping Output step to read from in the sub-transformation");
    }
    if (mappingOutputs.length>1) {
      throw new KettleException("The simple mapping step does not support multiple Mapping Output steps to read from in the sub-transformation");
    }
    data.mappingOutput = mappingOutputs[0];
    
    // LogTableField writeField = data.mappingTransMeta.getTransLogTable().findField(TransLogTable.ID.LINES_WRITTEN);
    // if (writeField.getSubject() == null && data.mappingOutputs != null && data.mappingOutputs.length >= 1) {
    //   writeField.setSubject(data.mappingOutputs[0].getStepMeta());
    // }

    // Finally, add the mapping transformation to the active sub-transformations
    // map in the parent transformation
    //
    getTrans().getActiveSubtransformations().put(getStepname(), data.mappingTrans);
  }

	public static void addInputRenames(List<MappingValueRename> renameList, List<MappingValueRename> addRenameList) {
		for (MappingValueRename rename : addRenameList) {
			if (renameList.indexOf(rename)<0) {
				renameList.add(rename);
			}
		}
	}

  public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
    meta = (SimpleMappingMeta) smi;
    data = (SimpleMappingData) sdi;

    if (super.init(smi, sdi)) {
      // First we need to load the mapping (transformation)
      try {
        // Pass the repository down to the metadata object...
        //
        meta.setRepository(getTransMeta().getRepository());

        data.mappingTransMeta = SimpleMappingMeta.loadMappingMeta(meta, meta.getRepository(), meta.getMetaStore(), this);
        if (data.mappingTransMeta != null) // Do we have a mapping at all?
        {

          // OK, now prepare the execution of the mapping.
          // This includes the allocation of RowSet buffers, the creation of the
          // sub-transformation threads, etc.
          //
          prepareMappingExecution();
          
          // That's all for now...
          return true;
        } else {
          logError("No valid mapping was specified!");
          return false;
        }
      } catch (Exception e) {
        logError("Unable to load the mapping transformation because of an error : " + e.toString());
        logError(Const.getStackTracker(e));
      }

    }
    return false;
  }
    
  public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
    // Close the running transformation
    if (data.wasStarted) {
      // Wait until the child transformation has finished.
      data.mappingTrans.waitUntilFinished();

      // Remove it from the list of active sub-transformations...
      //
      getTrans().getActiveSubtransformations().remove(getStepname());

      // See if there was an error in the sub-transformation, in that case, flag error etc.
      if (data.mappingTrans.getErrors() > 0) {
        logError(BaseMessages.getString(PKG, "SimpleMapping.Log.ErrorOccurredInSubTransformation"));
        setErrors(1);
      }
    }
    super.dispose(smi, sdi);
  }
    
  public void stopRunning(StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface)
      throws KettleException {
    if (data.mappingTrans != null) {
      data.mappingTrans.stopAll();
    }
  }
    
  public void stopAll() {
    // Stop the mapping step.
    if (data.mappingTrans != null) {
      data.mappingTrans.stopAll();
    }

    // Also stop this step
    super.stopAll();
  }
	    
    public Trans getMappingTrans() {
    	return data.mappingTrans;
    }
    
    /**
     * For preview of the main data path, make sure we pass the row listener down to the Mapping Output step...
     */
    public void addRowListener(RowListener rowListener)
    {
        MappingOutput[] mappingOutputs = data.mappingTrans.findMappingOutput();
        if (mappingOutputs==null || mappingOutputs.length==0) return; // Nothing to do here...
        
    	// Simple case: one output mapping step : add the row listener over there
    	//
        /*
        if (mappingOutputs.length==1) {
        	mappingOutputs[0].addRowListener(rowListener);
        } else {
        	// Find the main data path...
        	//
        	
        	
        }
        */
        
        // Add the row listener to all the outputs in the mapping...
        //
        for (MappingOutput mappingOutput : mappingOutputs) {
        	mappingOutput.addRowListener(rowListener);
        }
    }
}