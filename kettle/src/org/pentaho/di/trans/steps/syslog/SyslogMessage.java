/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was Samatar Hassan and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.trans.steps.syslog;


import org.productivity.java.syslog4j.Syslog;


import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entries.syslog.SyslogDefs;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Write message to SyslogMessage
 *  *  
 * @author Samatar
 * @since 03-Juin-2008
 *
 */

public class SyslogMessage extends BaseStep implements StepInterface
{
	private static Class<?> PKG = SyslogMessageMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private SyslogMessageMeta meta;
    private SyslogMessageData data;
    
    public SyslogMessage(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
    {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }
    
   
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
    {
        meta=(SyslogMessageMeta)smi;
        data=(SyslogMessageData)sdi;


        Object[] r = getRow();      // Get row from input rowset & set row busy!
        if (r==null)  // no more input to be expected...
        {
            setOutputDone();
            return false;
        }
    	if(first)
    	{
    		first=false;
    		// Check if message field is provided
			if (Const.isEmpty(meta.getMessageFieldName()))
			{
				throw new KettleException(BaseMessages.getString(PKG, "SyslogMessage.Error.MessageFieldMissing"));
			}

			
			// cache the position of the source filename field				
			data.indexOfMessageFieldName =getInputRowMeta().indexOfValue(meta.getMessageFieldName());
			if (data.indexOfMessageFieldName<0)
			{
				// The field is unreachable !
				throw new KettleException(BaseMessages.getString(PKG, "SyslogMessage.Exception.CouldnotFindField",meta.getMessageFieldName())); //$NON-NLS-1$ //$NON-NLS-2$
			}

    	}
    	
        try
        {        	
        	// get message
        	String message= getInputRowMeta().getString(r,data.indexOfMessageFieldName);
        	
        	if(Const.isEmpty(message))
        	{
        		throw new KettleException(BaseMessages.getString(PKG, "SyslogMessage.Error.MessageEmpty"));
        	}

        	// Send message
        	SyslogDefs.sendMessage(data.syslog, SyslogDefs.getPriority(meta.getPriority()), 
        			message, meta.isAddTimestamp(), data.datePattern , meta.isAddHostName());

            putRow(getInputRowMeta(), r);  // copy row to output rowset(s);
                
            if (checkFeedback(getLinesRead())) 
            {
            	if(isDetailed()) logDetailed(BaseMessages.getString(PKG, "SyslogMessage.LineNumber",getLinesRead())); //$NON-NLS-1$
            }
        }
        catch(Exception e)
        {
            
            boolean sendToErrorRow=false;
            String errorMessage = null;
            
        	if (getStepMeta().isDoingErrorHandling())
        	{
                  sendToErrorRow = true;
                  errorMessage = e.toString();
        	}
        	else
        	{
	            logError(BaseMessages.getString(PKG, "SyslogMessage.ErrorInStepRunning")+e.getMessage()); //$NON-NLS-1$
	            setErrors(1);
	            stopAll();
	            setOutputDone();  // signal end to receiver(s)
	            return false;
        	}
        	if (sendToErrorRow)
        	{
        	   // Simply add this row to the error row
        	   putError(getInputRowMeta(), r,1, errorMessage, null, "SyslogMessage001");
        	}
        }
            
        return true;
    }
	
	
	 
    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta=(SyslogMessageMeta)smi;
        data=(SyslogMessageData)sdi;

        if (super.init(smi, sdi))
        {
        	String servername=environmentSubstitute(meta.getServerName());
    		
        	// Check target server
    		if(Const.isEmpty(servername)) {
    			logError(BaseMessages.getString(PKG, "SyslogMessage.MissingServerName"));
    		}
    		
    		// Check if message field is provided
			if (Const.isEmpty(meta.getMessageFieldName()))
			{
				logError(BaseMessages.getString(PKG, "SyslogMessage.Error.MessageFieldMissing"));
				return false;
			}

    		int nrPort=Const.toInt(environmentSubstitute(meta.getPort()), SyslogDefs.DEFAULT_PORT);
    		
			if(meta.isAddTimestamp()) {
				// add timestamp to message
				data.datePattern = environmentSubstitute(meta.getDatePattern());
				if(Const.isEmpty(data.datePattern )) {
					logError(BaseMessages.getString(PKG, "SyslogMessage.DatePatternEmpty"));
					return false;
				}
			}
			
    		try {
    			// Connect to syslog ...    			
    			data.syslog = Syslog.getInstance(SyslogDefs.DEFAULT_PROTOCOL_UDP);
    			data.syslog.getConfig().setHost(servername);
    			data.syslog.getConfig().setPort(nrPort);
    			data.syslog.getConfig().setFacility(meta.getFacility());
    			data.syslog.getConfig().setSendLocalName(false);
    			data.syslog.getConfig().setSendLocalTimestamp(false);
    			
    		} catch (Exception ex ) {
    			logError(BaseMessages.getString(PKG, "SyslogMessage.UnknownHost", servername, ex.getMessage()));
				logError(Const.getStackTracker(ex));
    			return false;
    		} 
            return true;
        }
        return false;
    }
        
    public void dispose(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta = (SyslogMessageMeta)smi;
        data = (SyslogMessageData)sdi;
       
        if(data.syslog!=null) {
        	// release resource on syslog
        	data.syslog.shutdown();
        }
        super.dispose(smi, sdi);
    }
}