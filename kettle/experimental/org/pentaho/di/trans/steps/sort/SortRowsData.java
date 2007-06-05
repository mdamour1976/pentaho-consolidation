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
 

package org.pentaho.di.trans.steps.sort;

import java.util.ArrayList;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;



/**
 * @author Matt
 * @since 24-jan-2005
 */
public class SortRowsData extends BaseStepData implements StepDataInterface
{
	public ArrayList files;
	public Object[]  buffer;
    public int       bufferSize;
    public int       getBufferIndex;

	public ArrayList fis, gzis, dis;
	public ArrayList rowbuffer;

	public int     fieldnrs[];      // the corresponding field numbers;
    public FileObject fil;
    public RowMetaInterface outputRowMeta;

	/**
	 * 
	 */
	public SortRowsData()
	{
		super();
		
		files=new ArrayList();
		fis  =new ArrayList();
		dis  =new ArrayList();
		gzis = new ArrayList();
	}

}
