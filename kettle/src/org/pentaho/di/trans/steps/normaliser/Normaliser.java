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
 
package org.pentaho.di.trans.steps.normaliser;

import java.util.ArrayList;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Normalise de-normalised input data.
 * 
 * @author Matt
 * @since 5-apr-2003
 */
public class Normaliser extends BaseStep implements StepInterface
{
	private NormaliserMeta meta;
	private NormaliserData data;

	public Normaliser(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(NormaliserMeta)smi;
		data=(NormaliserData)sdi; 
		
		Object[] r=getRow();   // get row from rowset, wait for our turn, indicate busy!
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		
		if (first) // INITIALISE
		{
            first = false;
            
            data.inputRowMeta = getInputRowMeta();
            data.outputRowMeta = data.inputRowMeta.clone();
            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
            
			// Get a unique list of occurrences...
            //
			data.type_occ = new ArrayList<String>();
			data.maxlen=0;
			for (int i=0;i<meta.getFieldValue().length;i++)
			{
				if (!data.type_occ.contains(meta.getFieldValue()[i])) {
					data.type_occ.add(meta.getFieldValue()[i]);
				}
				if (meta.getFieldValue()[i].length()>data.maxlen) {
					data.maxlen=meta.getFieldValue()[i].length();
				}
			}
			
			// Which fields are not impacted? We can just copy these, leave them alone.
			//
			data.copy_fieldnrs = new ArrayList<Integer>();
			
			for (int i=0;i<data.inputRowMeta.size();i++)
			{
				ValueMetaInterface v = data.inputRowMeta.getValueMeta(i);
				if (Const.indexOfString(v.getName(), meta.getFieldName())<0) {
					data.copy_fieldnrs.add(Integer.valueOf(i));
				}
			}
			
			// Cache lookup indexes of fields
			//
			data.fieldnrs=new int[meta.getFieldName().length];
			for (int i=0;i<meta.getFieldName().length;i++)
			{
				data.fieldnrs[i] = data.inputRowMeta.indexOfValue(meta.getFieldName()[i]);
				if (data.fieldnrs[i]<0)
				{
					logError(Messages.getString("Normaliser.Log.CouldNotFindFieldInRow",meta.getFieldName()[i])); //$NON-NLS-1$ //$NON-NLS-2$
					setErrors(1);
					stopAll();
					return false;
				}
			}
		}
		
		// Now do the normalization
		// Loop over the unique occurrences of the different types.
		//
		for (int e=0;e<data.type_occ.size();e++)
		{
			String typevalue = (String)data.type_occ.get(e);

			// Create an output row per type
			//
			Object[] outputRowData = RowDataUtil.allocateRowData(data.outputRowMeta.size());
			int outputIndex=0;
			
			// Copy the input row data, excluding the fields that are normalized...
			//
			for (int i=0;i<data.copy_fieldnrs.size();i++)
			{
				int nr = data.copy_fieldnrs.get(i);
				outputRowData[outputIndex++] = r[nr];
			}
			
			// Add the typefield_value
			//
			outputRowData[outputIndex++] = typevalue;
			
			// Then add the normalized fields...
			//
			for (int i=0;i<data.fieldnrs.length;i++)
			{
				Object value = r[data.fieldnrs[i]];
				if (meta.getFieldValue()[i].equalsIgnoreCase(typevalue))
				{
					outputRowData[outputIndex++] = value;
				}
			}
			
			// The row is constructed, now give it to the next step(s)...
			//
			putRow(data.outputRowMeta, outputRowData);
		}

        if (checkFeedback(linesRead)) logBasic(Messages.getString("Normaliser.Log.LineNumber")+linesRead); //$NON-NLS-1$
			
		return true;
	}
			
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(NormaliserMeta)smi;
		data=(NormaliserData)sdi;
		
		if (super.init(smi, sdi))
		{
		    // Add init code here.
		    return true;
		}
		return false;
	}

	//
	// Run is were the action happens!
	public void run()
	{
		try
		{
			logBasic(Messages.getString("System.Log.StartingToRun")); //$NON-NLS-1$
			
			while (processRow(meta, data) && !isStopped());
		}
		catch(Throwable t)
		{
			logError(Messages.getString("System.Log.UnexpectedError")+" : "); //$NON-NLS-1$ //$NON-NLS-2$
            logError(Const.getStackTracker(t));
            setErrors(1);
			stopAll();
		}
		finally
		{
			dispose(meta, data);
			logSummary();
			markStop();
		}
	}
}