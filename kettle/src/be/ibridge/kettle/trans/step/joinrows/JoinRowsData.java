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
 

package be.ibridge.kettle.trans.step.joinrows;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.RowSet;
import be.ibridge.kettle.trans.step.BaseStepData;
import be.ibridge.kettle.trans.step.StepDataInterface;


/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class JoinRowsData extends BaseStepData implements StepDataInterface
{
	public File            file[];
	public FileInputStream fileInputStream[];
	public DataInputStream dataInputStream[];
	public Row             row[];
	public int             size[];
	public int             position[];
	public boolean         restart[];
	public RowSet          rs[];
	public ArrayList       cache[];
	
	public boolean         caching;

	public FileOutputStream fileOutputStream[];
	public DataOutputStream dataOutputStream[];
	
	public Row 				joinrow[];

	/**
	 * Keep track of which file temp file we're using... 
	 */
	public int filenr;

	/**
	 * 
	 */
	public JoinRowsData()
	{
		super();
	}

}
