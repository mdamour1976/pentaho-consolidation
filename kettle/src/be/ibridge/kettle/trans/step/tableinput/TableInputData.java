 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** It belongs to, is maintained by and is copyright 1999-2005 by     **
 **                                                                   **
 **      i-Bridge bvba                                                **
 **      Fonteinstraat 70                                             **
 **      9400 OKEGEM                                                  **
 **      Belgium                                                      **
 **      http://www.kettle.be                                         **
 **      info@kettle.be                                               **
 **                                                                   **
 **********************************************************************/
 

package be.ibridge.kettle.trans.step.tableinput;

import java.sql.ResultSet;

import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.trans.step.BaseStepData;
import be.ibridge.kettle.trans.step.StepDataInterface;


/**
 * @author Matt
 * @since 20-jan-2005
 */
public class TableInputData extends BaseStepData implements StepDataInterface
{
	public Row 		nextrow;
	public Row 		thisrow;
	public Database db;
	public Row 		parameters;
	public ResultSet rs;
	public String   lookupStep;
	
	public TableInputData()
	{
		super();
		
		db         = null;
		thisrow    = null;
		nextrow    = null;
		parameters = null;
		rs         = null;
		lookupStep = null;
	}


	
}
