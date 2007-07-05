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
 

package org.pentaho.di.trans.steps.textfileoutput;

import java.io.OutputStream;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipOutputStream;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 22-jan-2005
 */
public class TextFileOutputData extends BaseStepData implements StepDataInterface
{
	public int splitnr;

	public int fieldnrs[];

	public NumberFormat nf;
	public DecimalFormat df;
	public DecimalFormatSymbols dfs;
	
	public SimpleDateFormat daf;
	public DateFormatSymbols dafs;

	public ZipOutputStream zip;
	public GZIPOutputStream gzip;
    
    public OutputStream writer;

    public DecimalFormat        defaultDecimalFormat;
    public DecimalFormatSymbols defaultDecimalFormatSymbols;

    public SimpleDateFormat  defaultDateFormat;
    public DateFormatSymbols defaultDateFormatSymbols;

    public Process cmdProc;

    public OutputStream fos;

    public RowMetaInterface outputRowMeta;

	public byte[] binarySeparator;
	public byte[] binaryEnclosure;
	public byte[] binaryNewline;

	public boolean hasEncoding;

    /**
	 * 
	 */
	public TextFileOutputData()
	{
		super();
		
		nf = NumberFormat.getInstance();
		df = (DecimalFormat)nf;
		dfs=new DecimalFormatSymbols();

		daf = new SimpleDateFormat();
		dafs= new DateFormatSymbols();
        
        defaultDecimalFormat = (DecimalFormat)NumberFormat.getInstance();
        defaultDecimalFormatSymbols =  new DecimalFormatSymbols();

        defaultDateFormat = new SimpleDateFormat();
        defaultDateFormatSymbols = new DateFormatSymbols();

        cmdProc = null;
	}
}
