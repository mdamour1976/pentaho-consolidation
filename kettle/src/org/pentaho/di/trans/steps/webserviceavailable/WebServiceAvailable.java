/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.trans.steps.webserviceavailable;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Check if a webservice is available
 *  *  
 * @author Samatar
 * @since 03-01-2010
 *
 */

public class WebServiceAvailable extends BaseStep implements StepInterface
{
	private static Class<?> PKG = WebServiceAvailableMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	
    private WebServiceAvailableMeta meta;
    private WebServiceAvailableData data;
    
    public WebServiceAvailable(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
    {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }
    
   
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
    {
        meta=(WebServiceAvailableMeta)smi;
        data=(WebServiceAvailableData)sdi;

        Object[] r = getRow();      // Get row from input rowset & set row busy!
        if (r==null)  // no more input to be expected...
        {
            setOutputDone();
            return false;
        }
           
        
        if(first){
    		first=false;
			// get the RowMeta
			data.previousRowMeta = getInputRowMeta().clone();
			data.NrPrevFields=data.previousRowMeta.size();
			data.outputRowMeta = data.previousRowMeta;
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			
    		// Check is URL field is provided
			if (Const.isEmpty(meta.getURLField())){
				logError(BaseMessages.getString(PKG, "WebServiceAvailable.Error.FilenameFieldMissing"));
				throw new KettleException(BaseMessages.getString(PKG, "WebServiceAvailable.Error.FilenameFieldMissing"));
			}
			
			// cache the position of the field			
			data.indexOfURL =data.previousRowMeta.indexOfValue(meta.getURLField());
			if (data.indexOfURL<0){
				// The field is unreachable !
				logError(BaseMessages.getString(PKG, "WebServiceAvailable.Exception.CouldnotFindField")+ "[" + meta.getURLField()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
				throw new KettleException(BaseMessages.getString(PKG, "WebServiceAvailable.Exception.CouldnotFindField",meta.getURLField())); //$NON-NLS-1$ //$NON-NLS-2$
			}
    	}// End If first 
        
        try  {
        	
        	// get url
        	String url= data.previousRowMeta.getString(r, data.indexOfURL);
        	
        	if(Const.isEmpty(url)) throw new KettleException(BaseMessages.getString(PKG, "WebServiceAvailable.Error.URLEmpty"));
            
        	if(isDetailed()) logDetailed( BaseMessages.getString(PKG, "WebServiceAvailable.Log.CheckingURL", url));
        	
        	boolean WebServiceAvailable=false;
        	
        	InputStream in=null;
        	
        	try {
        		URLConnection conn = new URL(url).openConnection();   
        		conn.setConnectTimeout(data.connectTimeOut);   
        		conn.setReadTimeout(data.readTimeOut);   
        		in = conn.getInputStream();
        		// Web service is available
        		WebServiceAvailable=true;
        	}catch(Exception e) {
        		if(isDebug()) logDebug( BaseMessages.getString(PKG, "WebServiceAvailable.Error.ServiceNotReached", url, e.toString()));
            	
        	}finally {
        		if(in!=null){
            		try{
            			in.close();
            		}catch(Exception e){};
            	}
        	}
	        
	    	// addwebservice available to the row
            putRow(data.outputRowMeta, RowDataUtil.addValueData(r, data.NrPrevFields, WebServiceAvailable));  // copy row to output rowset(s);
           
            
	        if (isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "FileExists.LineNumber",getLinesRead()+" : "+getInputRowMeta().getString(r)));
        } catch(Exception e) {
            boolean sendToErrorRow=false;
            String errorMessage = null;
            
        	if (getStepMeta().isDoingErrorHandling()) {
                  sendToErrorRow = true;
                  errorMessage = e.toString();
        	} else {
	            logError(BaseMessages.getString(PKG, "WebServiceAvailable.ErrorInStepRunning")+e.getMessage()); //$NON-NLS-1$
	            setErrors(1);
	            stopAll();
	            setOutputDone();  // signal end to receiver(s)
	            return false;
        	}
        	if (sendToErrorRow) {
        	   // Simply add this row to the error row
        	   putError(getInputRowMeta(), r, 1, errorMessage, meta.getResultFieldName(), "WebServiceAvailable001");
        	}
        }
            
        return true;
    }

    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta=(WebServiceAvailableMeta)smi;
        data=(WebServiceAvailableData)sdi;

        if (super.init(smi, sdi))
        {
        	if(Const.isEmpty(meta.getResultFieldName()))
        	{
        		logError( BaseMessages.getString(PKG, "WebServiceAvailable.Error.ResultFieldMissing"));
        		return false;
        	}
        	data.connectTimeOut= Const.toInt(environmentSubstitute(meta.getConnectTimeOut()), 0);
        	data.readTimeOut= Const.toInt(environmentSubstitute(meta.getReadTimeOut()), 0);
            return true;
        }
        return false;
    }
        
    public void dispose(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta = (WebServiceAvailableMeta)smi;
        data = (WebServiceAvailableData)sdi;

        super.dispose(smi, sdi);
    }
    public String toString()
    {
        return this.getClass().getName();
    }
}
