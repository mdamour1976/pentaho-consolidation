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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Messages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.mappinginput.MappingInputMeta;
import org.pentaho.di.trans.steps.mappingoutput.MappingOutputMeta;
import org.w3c.dom.Node;



/**
 * Meta-data for the Mapping step: contains name of the (sub-)transformation to execute 
 * 
 * @since 22-nov-2005
 * @author Matt
 *
 */

public class MappingMeta extends BaseStepMeta implements StepMetaInterface
{
    private String transName;
    private String fileName;
    private String directoryPath;

    private List<MappingIODefinition> inputMappings;
    private List<MappingIODefinition> outputMappings;
    private MappingParameters         mappingParameters;

	public MappingMeta()
	{
		super(); // allocate BaseStepMeta
		
		inputMappings = new ArrayList<MappingIODefinition>();
    	outputMappings = new ArrayList<MappingIODefinition>();
    	mappingParameters = new MappingParameters();
	}
 
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException
	{
    	setDefault();
        try
        {
            readData(stepnode);
        }
        catch(KettleException e)
        {
            throw new KettleXMLException(Messages.getString("MappingMeta.Exception.ErrorLoadingTransformationStepFromXML"), e); //$NON-NLS-1$
        }
	}

	public Object clone()
	{
		Object retval = super.clone();
		return retval;
	}
	
	private void readData(Node stepnode) throws KettleException
	{
        transName      = XMLHandler.getTagValue(stepnode, "trans_name"); //$NON-NLS-1$
        fileName       = XMLHandler.getTagValue(stepnode, "filename"); //$NON-NLS-1$
        directoryPath  = XMLHandler.getTagValue(stepnode, "directory_path"); //$NON-NLS-1$
        
        Node mappingsNode  = XMLHandler.getSubNode(stepnode, "mappings"); //$NON-NLS-1$
                
        if (mappingsNode!=null)
        {
        	// Read all the input mapping definitions...
        	Node inputNode  = XMLHandler.getSubNode(mappingsNode, "input"); //$NON-NLS-1$
        	int nrInputMappings = XMLHandler.countNodes(inputNode, MappingIODefinition.XML_TAG); //$NON-NLS-1$
        	for (int i=0;i<nrInputMappings;i++) {
        		Node mappingNode = XMLHandler.getSubNodeByNr(inputNode, MappingIODefinition.XML_TAG, i);
        		MappingIODefinition inputMappingDefinition = new MappingIODefinition(mappingNode);
        		inputMappings.add(inputMappingDefinition);
        	}
        	Node outputNode  = XMLHandler.getSubNode(mappingsNode, "output"); //$NON-NLS-1$
        	int nrOutputMappings = XMLHandler.countNodes(outputNode, MappingIODefinition.XML_TAG); //$NON-NLS-1$
        	for (int i=0;i<nrOutputMappings;i++) {
        		Node mappingNode = XMLHandler.getSubNodeByNr(outputNode, MappingIODefinition.XML_TAG, i);
        		MappingIODefinition outputMappingDefinition = new MappingIODefinition(mappingNode);
        		outputMappings.add(outputMappingDefinition);
        	}
        	
        	// Load the mapping parameters too..
        	Node mappingParametersNode = XMLHandler.getSubNode(mappingsNode, MappingParameters.XML_TAG);
        	mappingParameters = new MappingParameters(mappingParametersNode);
        }
        else
        {
        	// backward compatibility...
        	//
            Node inputNode  = XMLHandler.getSubNode(stepnode, "input"); //$NON-NLS-1$
            Node outputNode = XMLHandler.getSubNode(stepnode, "output"); //$NON-NLS-1$
            
	        int nrInput  = XMLHandler.countNodes(inputNode, "connector"); //$NON-NLS-1$
	        int nrOutput = XMLHandler.countNodes(outputNode, "connector"); //$NON-NLS-1$
	        
	        MappingIODefinition inputMappingDefinition = new MappingIODefinition(); // null means: auto-detect
	        inputMappingDefinition.setMainDataPath(true);
	        
	        for (int i = 0; i < nrInput; i++) {
				Node inputConnector = XMLHandler.getSubNodeByNr(inputNode, "connector", i); //$NON-NLS-1$
				String inputField = XMLHandler.getTagValue(inputConnector, "field"); //$NON-NLS-1$
				String inputMapping = XMLHandler.getTagValue(inputConnector, "mapping"); //$NON-NLS-1$
				inputMappingDefinition.getValueRenames().add( new MappingValueRename(inputField, inputMapping) );
			}

	        MappingIODefinition outputMappingDefinition = new MappingIODefinition(); // null means: auto-detect
	        outputMappingDefinition.setMainDataPath(true);
	        
	        for (int i = 0; i < nrOutput; i++) {
				Node outputConnector = XMLHandler.getSubNodeByNr(outputNode, "connector", i); //$NON-NLS-1$
				String outputField = XMLHandler.getTagValue(outputConnector, "field"); //$NON-NLS-1$
				String outputMapping = XMLHandler.getTagValue(outputConnector, "mapping"); //$NON-NLS-1$
				outputMappingDefinition.getValueRenames().add( new MappingValueRename(outputField, outputMapping) );
			}
	        
	        // Don't forget to add these to the input and output mapping definitions...
	        //
	        inputMappings.add(inputMappingDefinition);
	        outputMappings.add(outputMappingDefinition);
	        
	        // The default is to have no mapping parameters: the concept didn't exist before.
	        mappingParameters = new MappingParameters();
        }
	}
    
    public void allocate()
    {
    }

    public String getXML()
    {
        StringBuffer retval = new StringBuffer();
        
        retval.append("    "+XMLHandler.addTagValue("trans_name", transName) ); //$NON-NLS-1$
        retval.append("    "+XMLHandler.addTagValue("filename", fileName )); //$NON-NLS-1$
        retval.append("    "+XMLHandler.addTagValue("directory_path", directoryPath )); //$NON-NLS-1$
        
        retval.append("    ").append(XMLHandler.openTag("mappings")).append(Const.CR); //$NON-NLS-1$ $NON-NLS-2$

        retval.append("      ").append(XMLHandler.openTag("input")).append(Const.CR); //$NON-NLS-1$ $NON-NLS-2$
        for (int i=0;i<inputMappings.size();i++)
        {
            retval.append(inputMappings.get(i).getXML());
        }
        retval.append("      ").append(XMLHandler.closeTag("input")).append(Const.CR); //$NON-NLS-1$ $NON-NLS-2$

        retval.append("      ").append(XMLHandler.openTag("output")).append(Const.CR); //$NON-NLS-1$ $NON-NLS-2$
        for (int i=0;i<outputMappings.size();i++)
        {
            retval.append(outputMappings.get(i).getXML());
        }
        retval.append("      ").append(XMLHandler.closeTag("output")).append(Const.CR); //$NON-NLS-1$ $NON-NLS-2$

        // Add the mapping parameters too
        //
        retval.append("      ").append(mappingParameters.getXML()).append(Const.CR); //$NON-NLS-1$
        
        retval.append("    ").append(XMLHandler.closeTag("mappings")).append(Const.CR); //$NON-NLS-1$ $NON-NLS-2$
        
        return retval.toString();
    }

	public void setDefault()
	{
        allocate();
	}
	
    public void getFields(RowMetaInterface row, String origin, RowMetaInterface info[], StepMeta nextStep, VariableSpace space) throws KettleStepException {
    	// First load some interesting data...
    	
    	// Then see which fields get added to the row.
    	//
        Repository repository = Repository.getCurrentRepository(); 
        TransMeta mappingTransMeta = null;
        try
        {
            mappingTransMeta = loadMappingMeta(fileName, transName, directoryPath, repository, space);
        }
        catch(KettleException e)
        {
            throw new KettleStepException(Messages.getString("MappingMeta.Exception.UnableToLoadMappingTransformation"), e);
        }
        
        // Keep track of all the fields that need renaming...
        //
        List<MappingValueRename> inputRenameList = new ArrayList<MappingValueRename>();
        
        /*
         * Before we ask the mapping outputs anything, we should teach the mapping input steps in the sub-transformation
         * about the data coming in...
         */
        for (MappingIODefinition definition : inputMappings) {
        	
        	RowMetaInterface inputRowMeta;
        	
        	if (definition.isMainDataPath() || Const.isEmpty(definition.getInputStepname()) ) {
        		// The row metadata, what we pass to the mapping input step definition.getOutputStep(), is "row"
        		// However, we do need to re-map some fields...
        		// 
        		inputRowMeta = row.clone();
        		for (MappingValueRename valueRename : definition.getValueRenames()) {
        			ValueMetaInterface valueMeta = inputRowMeta.searchValueMeta(valueRename.getSourceValueName());
        			if (valueMeta==null) {
        				throw new KettleStepException(Messages.getString("MappingMeta.Exception.UnableToFindField", valueRename.getSourceValueName()));
        			}
        			valueMeta.setName(valueRename.getTargetValueName());
        		}
        	}
        	else {
        		// The row metadata that goes to the info mapping input comes from the specified step
        		// In fact, it's one of the info steps that is going to contain this information...
        		//
        		String[] infoSteps = getInfoSteps();
        		int infoStepIndex = Const.indexOfString(definition.getInputStepname(), infoSteps);
        	    if (infoStepIndex<0) {
        	    	throw new KettleStepException(Messages.getString("MappingMeta.Exception.UnableToFindMetadataInfo", definition.getInputStepname()));
        	    }
        	    inputRowMeta = info[infoStepIndex].clone();
        	}
        	
    		// What is this mapping input step?
    		//
    		StepMeta mappingInputStep = mappingTransMeta.findMappingInputStep(definition.getOutputStepname());
    		
    		// We're certain it's a MappingInput step...
    		//
    		MappingInputMeta mappingInputMeta = (MappingInputMeta) mappingInputStep.getStepMetaInterface();

    		// Inform the mapping input step about what it's going to receive...
    		//
    		mappingInputMeta.setInputRowMeta(inputRowMeta);
    		
    		// What values are we changing names for?
    		//
    		mappingInputMeta.setValueRenames(definition.getValueRenames());
    		
    		// Keep a list of the input rename values that need to be changed back at the output
    		// 
    		if (definition.isRenamingOnOutput()) Mapping.addInputRenames(inputRenameList, definition.getValueRenames());
        }
        
        // All the mapping steps now know what they will be receiving.
        // That also means that the sub-transformation / mapping has everything it needs.
        // So that means that the MappingOutput steps know exactly what the output is going to be.
        // That could basically be anything.
        // It also could have absolutely no resemblance to what came in on the input.
        // The relative old approach is therefore no longer suited.
        // 
        // OK, but what we *can* do is have the MappingOutput step rename the appropriate fields.
        // The mapping step will tell this step how it's done.
        //
        // Let's look for the mapping output step that is relevant for this actual call...
        //
        MappingIODefinition mappingOutputDefinition = null;
    	if (nextStep==null) {
    		// This is the main step we read from...
    		// Look up the main step to write to.
    		// This is the output mapping definition with "main path" enabled.
    		//
    		for (MappingIODefinition definition : outputMappings) {
    			if (definition.isMainDataPath() || Const.isEmpty(definition.getOutputStepname())) {
    				// This is the definition to use...
    				//
    				mappingOutputDefinition = definition;
    			}
    		}
    	}
    	else {
    		// Is there an output mapping definition for this step?
    		// If so, we can look up the Mapping output step to see what has changed.
    		//
    		
    		for (MappingIODefinition definition : outputMappings) {
    			if (nextStep.getName().equals(definition.getOutputStepname()) || 
    			    definition.isMainDataPath() || 
    			    Const.isEmpty(definition.getOutputStepname())
    			    ) {
    				mappingOutputDefinition = definition;
    			}
    		}
    	}
    	
    	if (mappingOutputDefinition==null) {
    		throw new KettleStepException(Messages.getString("MappingMeta.Exception.UnableToFindMappingDefinition"));
    	}
    		
		// OK, now find the mapping output step in the mapping...
		// This method in TransMeta takes into account a number of things, such as the step not specified, etc.
		// The method never returns null but throws an exception.
		//
		StepMeta mappingOutputStep = mappingTransMeta.findMappingOutputStep(mappingOutputDefinition.getInputStepname());
		
		// We know it's a mapping output step...
		MappingOutputMeta mappingOutputMeta = (MappingOutputMeta) mappingOutputStep.getStepMetaInterface();

		// Change a few columns.
		mappingOutputMeta.setOutputValueRenames(mappingOutputDefinition.getValueRenames());
		
		// Perhaps we need to change a few input columns back to the original?
		//
		mappingOutputMeta.setInputValueRenames(inputRenameList);
		
		// Now we know wat's going to come out of there...
		// This is going to be the full row, including all the remapping, etc.
		//
		RowMetaInterface mappingOutputRowMeta = mappingTransMeta.getStepFields(mappingOutputStep);
		
		row.clear();
		row.addRowMeta(mappingOutputRowMeta);
    }
    
    @Override
    public String[] getInfoSteps() {

    	List<String> infoSteps = new ArrayList<String>();
    	// The infosteps are those steps that are specified in the input mappings
    	for (MappingIODefinition definition : inputMappings) {
    		if (!definition.isMainDataPath() && !Const.isEmpty(definition.getInputStepname())) {
    			infoSteps.add(definition.getInputStepname());
    		}
    	}
    	if (infoSteps.isEmpty()) return null;

    	return infoSteps.toArray(new String[infoSteps.size()]);
    }
    
    @Override
    public String[] getTargetSteps() {

    	List<String> targetSteps = new ArrayList<String>();
    	// The infosteps are those steps that are specified in the input mappings
    	for (MappingIODefinition definition : outputMappings) {
    		if (!definition.isMainDataPath() && !Const.isEmpty(definition.getOutputStepname())) {
    			targetSteps.add(definition.getOutputStepname());
    		}
    	}
    	if (targetSteps.isEmpty()) return null;

    	return targetSteps.toArray(new String[targetSteps.size()]);
    }

    public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
    	/*
    	 * TODO re-enable repository support
    	 * 
        transName        = rep.getStepAttributeString(id_step, "trans_name"); //$NON-NLS-1$
        fileName         = rep.getStepAttributeString(id_step, "filename"); //$NON-NLS-1$
        directoryPath    = rep.getStepAttributeString(id_step, "directory_path"); //$NON-NLS-1$
        
        int nrInput  = rep.countNrStepAttributes(id_step, "input_field"); //$NON-NLS-1$
        int nrOutput = rep.countNrStepAttributes(id_step, "output_field"); //$NON-NLS-1$

        allocate(nrInput, nrOutput);
        
        for (int i=0;i<nrInput;i++)
        {
            inputField[i]   = rep.getStepAttributeString(id_step, i, "input_field"); //$NON-NLS-1$
            inputMapping[i] = rep.getStepAttributeString(id_step, i, "input_mapping"); //$NON-NLS-1$
        }

        for (int i=0;i<nrOutput;i++)
        {
            outputField[i]   = rep.getStepAttributeString(id_step, i, "output_field"); //$NON-NLS-1$
            outputMapping[i] = rep.getStepAttributeString(id_step, i, "output_mapping"); //$NON-NLS-1$
        }
        */
	}
    
    public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException
    {
    	/*
    	 * TODO re-enable repository support
    	 * 
        rep.saveStepAttribute(id_transformation, id_step, "filename", fileName); //$NON-NLS-1$
        rep.saveStepAttribute(id_transformation, id_step, "trans_name", transName); //$NON-NLS-1$
        rep.saveStepAttribute(id_transformation, id_step, "directory_path", directoryPath); //$NON-NLS-1$
        
        if (inputField!=null)
        for (int i=0;i<inputField.length;i++)
        {
            rep.saveStepAttribute(id_transformation, id_step, i, "input_field",   inputField[i]); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, i, "input_mapping", inputMapping[i]); //$NON-NLS-1$
        }
        
        if (outputField!=null)
        for (int i=0;i<outputField.length;i++)
        {
            rep.saveStepAttribute(id_transformation, id_step, i, "output_field",   outputField[i]); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, i, "output_mapping", outputMapping[i]); //$NON-NLS-1$
        }
        */
    }

    public synchronized static final TransMeta loadMappingMeta(String fileName, String transName, String directoryPath, Repository rep, VariableSpace space) throws KettleException
    {
        TransMeta mappingTransMeta = null;
        
        String realFilename = space.environmentSubstitute(fileName);
        String realTransname = space.environmentSubstitute(transName);
        
        if ( !Const.isEmpty(realFilename))
        {
            try
            {
            	// OK, load the meta-data from file...
                mappingTransMeta = new TransMeta( realFilename, false ); // don't set internal variables: they belong to the parent thread!
                LogWriter.getInstance().logDetailed("Loading Mapping from repository", "Mapping transformation was loaded from XML file ["+realFilename+"]");
                // mappingTransMeta.setFilename(fileName);
           }
            catch(Exception e)
            {
                LogWriter.getInstance().logError("Loading Mapping from XML", "Unable to load transformation ["+realFilename+"] : "+e.toString());
                LogWriter.getInstance().logError("Loading Mapping from XML", Const.getStackTracker(e));
                throw new KettleException(e);
            }
        }
        else
        {
            // OK, load the meta-data from the repository...
            if (!Const.isEmpty(realTransname) && directoryPath!=null && rep!=null)
            {
                RepositoryDirectory repdir = rep.getDirectoryTree().findDirectory(directoryPath);
                if (repdir!=null)
                {
                    try
                    {
                        mappingTransMeta = new TransMeta(rep, realTransname, repdir);
                        LogWriter.getInstance().logDetailed("Loading Mapping from repository", "Mapping transformation ["+realTransname+"] was loaded from the repository");
                    }
                    catch(Exception e)
                    {
                        LogWriter.getInstance().logError("Loading Mapping from repository", "Unable to load transformation ["+realTransname+"] : "+e.toString());
                        LogWriter.getInstance().logError("Loading Mapping from repository", Const.getStackTracker(e));
                    }
                }
                else
                {
                    throw new KettleException(Messages.getString("MappingMeta.Exception.UnableToLoadTransformation",realTransname)+directoryPath); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }
        
        return mappingTransMeta;
    }
	

    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		if (prev==null || prev.size()==0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, Messages.getString("MappingMeta.CheckResult.NotReceivingAnyFields"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("MappingMeta.CheckResult.StepReceivingFields",prev.size()+""), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
		}

		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("MappingMeta.CheckResult.StepReceivingFieldsFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("MappingMeta.CheckResult.NoInputReceived"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
        
		/*
		 * TODO re-enable validation code for mappings...
		 * 
    	// Change the names of the fields if this is required by the mapping.
    	for (int i=0;i<inputField.length;i++)
		{
			if (inputField[i]!=null && inputField[i].length()>0)
			{
				if (inputMapping[i]!=null && inputMapping[i].length()>0)
				{
					if (!inputField[i].equals(inputMapping[i])) // rename these!
					{
						int idx = prev.indexOfValue(inputField[i]);
						if (idx<0)
						{
							cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("MappingMeta.CheckResult.MappingTargetFieldNotPresent",inputField[i]), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
							remarks.add(cr);
						}
					}
				}
				else
				{
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("MappingMeta.CheckResult.MappingTargetFieldNotSepecified",i+"",inputField[i]), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					remarks.add(cr);
				}
			}
			else
			{
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("MappingMeta.CheckResult.InputFieldNotSpecified",i+""), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
				remarks.add(cr);
			}
		}

    	// Then check the fields that get added to the row.
    	//
        
        Repository repository = Repository.getCurrentRepository(); 
        TransMeta mappingTransMeta = null;
        try
        {
            mappingTransMeta = loadMappingMeta(fileName, transName, directoryPath, repository);
        }
        catch(KettleException e)
        {
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("MappingMeta.CheckResult.UnableToLoadMappingTransformation")+":"+Const.getStackTracker(e), stepinfo); //$NON-NLS-1$
            remarks.add(cr);
        }

        if (mappingTransMeta!=null)
        {
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("MappingMeta.CheckResult.MappingTransformationSpecified"), stepinfo); //$NON-NLS-1$
            remarks.add(cr);

            StepMeta stepMeta = mappingTransMeta.getMappingOutputStep();
            
            if (stepMeta!=null)
            {
	            // See which fields are coming out of the mapping output step of the sub-transformation
	            // For these fields we check the existance
	            //
	            RowMetaInterface fields = null;
	            try
	            {
	            	fields = mappingTransMeta.getStepFields(stepMeta);
	
	            	boolean allOK = true;
	                
	                // Check the fields...
	                for (int i=0;i<outputMapping.length;i++)
	                {
	                    ValueMetaInterface v = fields.searchValueMeta(outputMapping[i]);
	                    if (v==null) // Not found!
	                    {
	                        cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("MappingMeta.CheckResult.MappingOutFieldSpecifiedCouldNotFound")+outputMapping[i], stepinfo); //$NON-NLS-1$
	                        remarks.add(cr);
	                        allOK=false;
	                    }
	                }
	                
	                if (allOK)
	                {
	                    cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("MappingMeta.CheckResult.AllOutputMappingFieldCouldBeFound"), stepinfo); //$NON-NLS-1$
	                    remarks.add(cr);
	                }
	            }
	            catch(KettleStepException e)
	            {
	                cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("MappingMeta.CheckResult.UnableToGetStepOutputFields")+stepMeta.getName()+"]", stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
	                remarks.add(cr);
	            }
            }
            else
            {
                cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("MappingMeta.CheckResult.NoMappingOutputStepSpecified"), stepinfo); //$NON-NLS-1$
                remarks.add(cr);
            }
        }
        else
        {
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("MappingMeta.CheckResult.NoMappingSpecified"), stepinfo); //$NON-NLS-1$
            remarks.add(cr);
        }
        */
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new Mapping(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new MappingData();
	}

    /**
     * @return the directoryPath
     */
    public String getDirectoryPath()
    {
        return directoryPath;
    }

    /**
     * @param directoryPath the directoryPath to set
     */
    public void setDirectoryPath(String directoryPath)
    {
        this.directoryPath = directoryPath;
    }

    /**
     * @return the fileName
     */
    public String getFileName()
    {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    /**
     * @return the transName
     */
    public String getTransName()
    {
        return transName;
    }

    /**
     * @param transName the transName to set
     */
    public void setTransName(String transName)
    {
        this.transName = transName;
    }

	/**
	 * @return the inputMappings
	 */
	public List<MappingIODefinition> getInputMappings() {
		return inputMappings;
	}

	/**
	 * @param inputMappings the inputMappings to set
	 */
	public void setInputMappings(List<MappingIODefinition> inputMappings) {
		this.inputMappings = inputMappings;
	}

	/**
	 * @return the outputMappings
	 */
	public List<MappingIODefinition> getOutputMappings() {
		return outputMappings;
	}

	/**
	 * @param outputMappings the outputMappings to set
	 */
	public void setOutputMappings(List<MappingIODefinition> outputMappings) {
		this.outputMappings = outputMappings;
	}

	/**
	 * @return the mappingParameters
	 */
	public MappingParameters getMappingParameters() {
		return mappingParameters;
	}

	/**
	 * @param mappingParameters the mappingParameters to set
	 */
	public void setMappingParameters(MappingParameters mappingParameters) {
		this.mappingParameters = mappingParameters;
	}
	
  @Override
  public List<ResourceReference> getResourceDependencies(TransMeta transMeta, StepMeta stepInfo) {
     List<ResourceReference> references = new ArrayList<ResourceReference>(5);
     String realFilename = transMeta.environmentSubstitute(fileName);
     String realTransname = transMeta.environmentSubstitute(transName);
     ResourceReference reference = new ResourceReference(stepInfo);
     references.add(reference);
     
     if (!Const.isEmpty(realFilename)) {
       // Add the filename to the references, including a reference to this step meta data.
       //
       reference.getEntries().add( new ResourceEntry(realFilename, ResourceType.ACTIONFILE));
     } else if (!Const.isEmpty(realTransname)) {
       // Add the filename to the references, including a reference to this step meta data.
       //
       reference.getEntries().add( new ResourceEntry(realTransname, ResourceType.ACTIONFILE));
       references.add(reference);
     }
     return references;
  }
  
	@Override
	public String exportResources(VariableSpace space, Map<String, ResourceDefinition> definitions, ResourceNamingInterface resourceNamingInterface) throws KettleException {
		try {
  		// Try to load the transformation from repository or file.
  		// Modify this recursively too...
  		// 
  		if (!Const.isEmpty(fileName)) {
  		  FileObject fileObject = KettleVFS.getFileObject(space.environmentSubstitute(fileName));
  		  // NOTE: there is no need to clone this step because the caller is responsible for this.
  			//
  			// First load the mapping metadata...
  			//
  			TransMeta mappingTransMeta = loadMappingMeta(fileName, null, null, null, space);
  			
  			String newFilename = resourceNamingInterface.nameResource(fileObject.getName().getBaseName(), fileObject.getParent().getName().getPath(), "ktr");
  			mappingTransMeta.setFilename(newFilename);
  
  			fileName = newFilename; // replace it BEFORE XML generation occurs! 
  
  			String xml = mappingTransMeta.getXML();
  			definitions.put(fileObject.getName().getPath(), new ResourceDefinition(newFilename, xml));
  			
  			return newFilename;
  		}
  		else {
  			return null;
  		}
    } catch (Exception e) {
      throw new KettleException(Messages.getString("MappingMeta.Exception.UnableToLoadTransformation",fileName)); //$NON-NLS-1$
    }
	}

}
