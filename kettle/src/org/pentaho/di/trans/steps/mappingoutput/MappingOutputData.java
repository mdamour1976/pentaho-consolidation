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
 

package org.pentaho.di.trans.steps.mappingoutput;

import java.util.List;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.steps.mapping.MappingValueRename;




/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class MappingOutputData extends BaseStepData implements StepDataInterface
{

	public StepInterface[] targetSteps;
    public String[] outputField;
    public String[] outputMapping;
    public RowMetaInterface outputRowMeta;
	public List<MappingValueRename> inputValueRenames;
	public List<MappingValueRename> outputValueRenames;
    
    /**
	 * 
	 */
	public MappingOutputData()
	{
		super();
	}

}
