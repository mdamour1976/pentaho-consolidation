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
 
package be.ibridge.kettle.trans.step.socketreader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleEOFException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/**
 * Do nothing.  Pass all input data to the next steps.
 * 
 * @author Matt
 * @since 2-jun-2003
 */

public class SocketReader extends BaseStep implements StepInterface
{
	public static final String STRING_FINISHED = "Finished";
    private static final int TIMEOUT_IN_SECONDS = 30;
    private SocketReaderMeta meta;
	private SocketReaderData data;
	
	public SocketReader(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(SocketReaderMeta)smi;
		data=(SocketReaderData)sdi;

        try
        {
            Row r;
            
            if (first)
            {
                // Connect to the server socket (started during init)
                // Because the accept() call on the server socket can be called after we reached this code
                // it is best to build in a retry loop with a time-out here.
                // 
                long startTime = new Date().getTime();
                boolean connected=false;
                KettleException lastException=null;
                
                while ( !connected && ( TIMEOUT_IN_SECONDS > (new Date().getTime()-startTime)/1000 ) ) // timeout with retry until connected
                {
                    try
                    {
                        int port = Integer.parseInt( StringUtil.environmentSubstitute(meta.getPort()) );
                        int bufferSize = Integer.parseInt( StringUtil.environmentSubstitute(meta.getBufferSize()));
                        
                        data.socket = new Socket(StringUtil.environmentSubstitute(meta.getHostname()), port);
                        connected=true;

                        if (meta.isCompressed())
                        {
                            data.outputStream = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(data.socket.getOutputStream()), bufferSize));
                            data.inputStream  = new DataInputStream(new BufferedInputStream(new GZIPInputStream(data.socket.getInputStream()), bufferSize));
                        }
                        else
                        {
                            data.outputStream = new DataOutputStream(new BufferedOutputStream(data.socket.getOutputStream(), bufferSize));
                            data.inputStream  = new DataInputStream(new BufferedInputStream(data.socket.getInputStream(), bufferSize));
                        }
                        lastException=null;
                    }
                    catch(Exception e)
                    {
                        lastException=new KettleException("Unable to open socket to server "+StringUtil.environmentSubstitute(meta.getHostname())+" port "+StringUtil.environmentSubstitute(meta.getPort()), e);
                    }
                    
                    if (lastException!=null) // Sleep for a second
                    {
                        Thread.sleep(1000);
                    }
                }
                
                if (lastException!=null)
                {
                    logError("Error initialising step: "+lastException.toString());
                    logError(Const.getStackTracker(lastException));
                    throw lastException;
                }
                else
                {
                    if (data.inputStream==null) throw new KettleException("Unable to connect to the SocketWriter in the "+TIMEOUT_IN_SECONDS+"s timeout period.");
                }
                
                
                data.row = new Row(data.inputStream); // This is the metadata
                first=false;
            }
            r = new Row(data.inputStream, data.row.size(), data.row);
            
            linesInput++;
            
            if (checkFeedback(linesInput)) logBasic(Messages.getString("SocketReader.Log.LineNumber")+linesInput); //$NON-NLS-1$
            
            putRow(r);
        }
        catch(KettleEOFException e)
        {
            setOutputDone(); // finished reading.
            return false;
        }
        catch (Exception e)
        {
            throw new KettleException(e);
        }
        
		return true;
	}

    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(SocketReaderMeta)smi;
		data=(SocketReaderData)sdi;
		
		if (super.init(smi, sdi))
		{
            return true;
		}
		return false;
	}
    
    public void dispose(StepMetaInterface smi, StepDataInterface sdi)
    {
        // Ignore errors, we don't care
        // If we are here, it means all work is done
        // It's a lot of work to keep it all in sync for now we don't need to do that.
        // 
        try { data.inputStream.close(); } catch(Exception e) {}
        try { data.outputStream.close(); } catch(Exception e) {}
        try { data.socket.close(); } catch(Exception e) {}
        
        super.dispose(smi, sdi);
    }
	
	//
	// Run is were the action happens!
	public void run()
	{
		try
		{
			logBasic(Messages.getString("SocketReader.Log.StartingToRun")); //$NON-NLS-1$
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError(Messages.getString("SocketReader.Log.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
            logError(Const.getStackTracker(e));
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
