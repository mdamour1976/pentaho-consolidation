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
 
package org.pentaho.di.trans.steps.mapping;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.mappinginput.MappingInput;
import org.pentaho.di.trans.steps.mappingoutput.MappingOutput;




/**
 * Execute a mapping: a re-usuable transformation
 * 
 * @author Matt
 * @since 22-nov-2005
 */

public class Mapping extends BaseStep implements StepInterface
{
	private MappingMeta meta;
	private MappingData data;
	
	public Mapping(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
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
		meta=(MappingMeta)smi;
		data=(MappingData)sdi;
		
		// Start the mapping/sub-transformation threads
        //
        data.trans.startThreads();
        
        // The transformation still runs in the background and might have some more work to do.
        // Since everything is running in the MappingThreads we don't have to do anything else here but wait...
        //
        data.trans.waitUntilFinished();
        
        // Set some statistics from the mapping...
        // This will show up in Spoon, etc.
        //
    	Result result = data.trans.getResult();
    	setErrors(result.getNrErrors());
    	linesRead = result.getNrLinesRead();
    	linesWritten = result.getNrLinesWritten();
    	linesInput = result.getNrLinesInput();
    	linesOutput = result.getNrLinesOutput();
    	linesUpdated = result.getNrLinesUpdated();
    	linesRejected = result.getNrLinesRejected();
    	
    	return false;
	}

	private void setMappingParameters() throws KettleException {
		MappingParameters mappingParameters = meta.getMappingParameters();
		if (mappingParameters!=null) {
			// Just set the variables in the transformation statically.
			// This just means: set a number of variables:
			//
			for (int i=0;i<mappingParameters.getVariable().length;i++) {
				String name = mappingParameters.getVariable()[i];
				String value = environmentSubstitute(mappingParameters.getInputField()[i]);
				if (!Const.isEmpty(name) && !Const.isEmpty(value)) {
					data.mappingTransMeta.setVariable(name, value);
				}
			}
		}
	}

	public void prepareMappingExecution() throws KettleException {
        // Create the transformation from meta-data...
        LogWriter log = LogWriter.getInstance();
        data.trans = new Trans(log, data.mappingTransMeta);
        
        // We launch the transformation in the processRow when the first row is received.
        // This will allow the correct variables to be passed.
        // Otherwise the parent is the init() thread which will be gone once the init is done.
        if (!data.trans.prepareExecution(getTransMeta().getArguments())) {
        	throw new KettleException(Messages.getString("Mapping.Exception.UnableToPrepareExecutionOfMapping"));
        }
        
        // Before we add rowsets and all, we should note that the mapping step did not receive ANY input and output rowsets.
        // This is an exception to the general rule, built into Trans.prepareExecution()
        //
        // A Mapping Input step is supposed to read directly from the previous steps.
        // A Mapping Output step is supposed to write directly to the next steps.
        
        // OK, check the input mapping definitions and look up the steps to read from.
        // 
        StepInterface[] sourceSteps;
        for (MappingIODefinition inputDefinition : meta.getInputMappings()) {
        	// If we have a single step to read from, we use this
        	//
        	if (!Const.isEmpty(inputDefinition.getInputStepname())) {
        		StepInterface sourceStep = (StepInterface) getTrans().findRunThread(inputDefinition.getInputStepname());
            	if (sourceStep==null) {
            		throw new KettleException(Messages.getString("MappingDialog.Exception.StepNameNotFound", inputDefinition.getInputStepname()));
            	}
            	sourceSteps = new StepInterface[] { sourceStep, };
        	} 
        	else {
        		// We have no defined source step.
        		// That means that we're reading from all input steps that this mapping step has.
        		//
        		StepMeta[] prevSteps = getTransMeta().getPrevSteps(getStepMeta());
        		
    			// Let's read data from all the previous steps we find...
    			// The origin is the previous step
    			// The target is the Mapping Input step.
    			//
    			sourceSteps=new StepInterface[prevSteps.length];
    			for (int s=0;s<sourceSteps.length;s++) {
    				sourceSteps[s] = (StepInterface) getTrans().findRunThread(prevSteps[s].getName());
    			}
        	}
        	
        	// What step are we writing to?
        	MappingInput mappingInputTarget=null;
    		MappingInput[] mappingInputSteps = data.trans.findMappingInput();
        	if (Const.isEmpty(inputDefinition.getOutputStepname())) {
        		// No target was specifically specified.
        		// That means we only expect one "mapping input" step in the mapping...
        		
        		if (mappingInputSteps.length==0) {
        			throw new KettleException(Messages.getString("MappingDialog.Exception.OneMappingInputStepRequired"));
        		}
        		if (mappingInputSteps.length>1) {
        			throw new KettleException(Messages.getString("MappingDialog.Exception.OnlyOneMappingInputStepAllowed", ""+mappingInputSteps.length));
        		}
        		
        		mappingInputTarget = mappingInputSteps[0];
        	}
        	else {
        		// A target step was specified.  See if we can find it...
        		for (int s=0;s<mappingInputSteps.length && mappingInputTarget==null;s++) {
        			if (mappingInputSteps[s].getStepname().equals(inputDefinition.getOutputStepname())) {
        				mappingInputTarget = mappingInputSteps[s];
        			}
        		}
        		// If we still didn't find it it's a drag.
        		if (mappingInputTarget==null) {
            		throw new KettleException(Messages.getString("MappingDialog.Exception.StepNameNotFound", inputDefinition.getOutputStepname()));
        		}
        	}
        	
        	mappingInputTarget.setConnectorSteps(sourceSteps);
        	
        }
        
        // Now we have a List of connector threads.
        // If we start all these we'll be starting to pump data into the mapping
        // If we don't have any threads to start, nothings going in there...
        // However, before we send anything over, let's first explain to the mapping output steps where the data needs to go...
        //
        for (MappingIODefinition outputDefinition : meta.getOutputMappings()) {
        	// OK, what is the source (input) step in the mapping: it's the mapping output step...
        	// What step are we reading from here?
        	//
        	MappingOutput mappingOutputSource = (MappingOutput) getTrans().findRunThread(outputDefinition.getInputStepname());
        	if (mappingOutputSource==null) {
        		// No source step was specified: we're reading from a single Mapping Output step.
        		// We should verify this if this is really the case...
        		//
        		MappingOutput[] mappingOutputSteps = data.trans.findMappingOutput();
        		
        		if (mappingOutputSteps.length==0) {
        			throw new KettleException(Messages.getString("MappingDialog.Exception.OneMappingOutputStepRequired"));
        		}
        		if (mappingOutputSteps.length>1) {
        			throw new KettleException(Messages.getString("MappingDialog.Exception.OnlyOneMappingOutputStepAllowed", ""+mappingOutputSteps.length));
        		}
        		
        		mappingOutputSource = mappingOutputSteps[0];
        	}
        	
        	// To what step in this transformation are we writing to?
        	//
        	StepInterface[] targetSteps;
        	if (!Const.isEmpty(outputDefinition.getOutputStepname())) {
        		// If we have a target step specification for the output of the mapping, we need to send it over there...
        		//
            	StepInterface target = (StepInterface) getTrans().findRunThread(outputDefinition.getOutputStepname());
            	if (target==null) {
            		throw new KettleException(Messages.getString("MappingDialog.Exception.StepNameNotFound", outputDefinition.getOutputStepname()));
            	}
            	targetSteps = new StepInterface[] { target, };
        	}
        	else {
        		// No target step is specified.
        		// See if we can find the next steps in the transformation..
        		// 
        		
        		StepMeta[] nextSteps = getTransMeta().getNextSteps(getStepMeta());
    			// Let's send the data to all the next steps we find...
    			// The origin is the mapping output step
    			// The target is all the next steps after this mapping step.
    			//
    			targetSteps=new StepInterface[nextSteps.length];
    			for (int s=0;s<targetSteps.length;s++) {
    				targetSteps[s] = (StepInterface) getTrans().findRunThread(nextSteps[s].getName());
    			}
        	}
        	
        	// Now tell the mapping output step where to look...
        	//
        	mappingOutputSource.setConnectorSteps(targetSteps);
        	
        	// Is this mapping copying or distributing?
        	// Make sure the mapping output step mimics this behavior:
        	//
        	mappingOutputSource.setDistributed(isDistributed());        	
        }
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(MappingMeta)smi;
		data=(MappingData)sdi;
		
		if (super.init(smi, sdi))
		{
		    // First we need to load the mapping (transformation)
            try
            {
                Repository repository = Repository.getCurrentRepository();
                data.mappingTransMeta = MappingMeta.loadMappingMeta(meta.getFileName(), meta.getTransName(), meta.getDirectoryPath(), repository, this);
                if (data.mappingTransMeta!=null) // Do we have a mapping at all?
                {
                	// Set the parameters statically or dynamically
            		//
            		setMappingParameters();
                    
            		// OK, now prepare the execution of the mapping.
            		// This includes the allocation of RowSet buffers, the creation of the sub-transformation threads, etc.
            		//
            		prepareMappingExecution();
            		
                	// That's all for now...
                    return true;
                }
                else
                {
                    logError("No valid mapping was specified!");
                    return false;
                }
            }
            catch(Exception e)
            {
                logError("Unable to load the mapping transformation because of an error : "+e.toString());
                logError(Const.getStackTracker(e));
            }
            
		}
		return false;
	}
    
    public void dispose(StepMetaInterface smi, StepDataInterface sdi)
    {
        // Close the running transformation
        if (data.wasStarted)
        {
            // Wait until the child transformation has finished.
            data.trans.waitUntilFinished();
            
            // store some logging, close shop.
            try
            {
                data.trans.endProcessing("end"); //$NON-NLS-1$
            }
            catch(KettleException e)
            {
                log.logError(toString(), Messages.getString("Mapping.Log.UnableToLogEndOfTransformation")+e.toString()); //$NON-NLS-1$
            }
            
            // See if there was an error in the sub-transformation, in that case, flag error etc.
            if (data.trans.getErrors()>0)
            {
                logError(Messages.getString("Mapping.Log.ErrorOccurredInSubTransformation")); //$NON-NLS-1$
                setErrors(1);
            }
        }
        super.dispose(smi, sdi);
    }
    
    public void stopAll()
    {
        // Stop this step
        super.stopAll();
        
        // Also stop the mapping step.
        if ( data.trans != null  )
        {
            data.trans.stopAll();
        }
    }
	
	//
	// Run is were the action happens!
	public void run()
	{
		try
		{
			logBasic(Messages.getString("Mapping.Log.StartingToRun")); //$NON-NLS-1$
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError(Messages.getString("Mapping.Log.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
            logError(Const.getStackTracker(e));
            setErrors(1);
			stopAll();
			if (data.trans!=null) data.trans.stopAll();
		}
		finally
		{
			dispose(meta, data);
			logSummary();
			markStop();
		}
	}
}
