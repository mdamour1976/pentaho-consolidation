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
 

package org.pentaho.di.trans.steps.mappinginput;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;



/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class MappingInputData extends BaseStepData implements StepDataInterface
{

	public boolean finished;
	public StepInterface[] sourceSteps;
	public String[] sourceFieldname;
	public String[] targetFieldname;
	public boolean linked;
	public RowMetaInterface outputRowMeta;

    /**
	 * 
	 */
	public MappingInputData()
	{
		super();
		linked=false;
	}

}
