package org.pentaho.di.www;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.codec.binary.Base64;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


public class SlaveServerTransStatus
{
    public static final String XML_TAG = "transstatus";
    
    private String transName;
    private String statusDescription;
    private String errorDescription;
    private String loggingString;
    private List<StepStatus>   stepStatusList;
    
    public SlaveServerTransStatus()
    {
        stepStatusList = new ArrayList<StepStatus>();
    }
    
    /**
     * @param transName
     * @param statusDescription
     */
    public SlaveServerTransStatus(String transName, String statusDescription)
    {
        this();
        this.transName = transName;
        this.statusDescription = statusDescription;
    }
    
    public String getXML()
    {
        StringBuffer xml = new StringBuffer();
        
        xml.append("<"+XML_TAG+">").append(Const.CR);
        xml.append(XMLHandler.addTagValue("transname", transName));                
        xml.append(XMLHandler.addTagValue("status_desc", statusDescription));                
        xml.append(XMLHandler.addTagValue("error_desc", errorDescription));          
        
        xml.append("  <stepstatuslist>").append(Const.CR);
        for (int i = 0; i < stepStatusList.size(); i++)
        {
            StepStatus stepStatus = (StepStatus) stepStatusList.get(i);
            xml.append("    ").append(stepStatus.getXML()).append(Const.CR);
        }
        xml.append("  </stepstatuslist>").append(Const.CR);

        xml.append(XMLHandler.addTagValue("logging_string", XMLHandler.buildCDATA(loggingString)));          

        xml.append("</"+XML_TAG+">");
        
        return xml.toString();
    }
    
    public SlaveServerTransStatus(Node transStatusNode)
    {
        this();
        transName = XMLHandler.getTagValue(transStatusNode, "transname");
        statusDescription = XMLHandler.getTagValue(transStatusNode, "status_desc");
        errorDescription = XMLHandler.getTagValue(transStatusNode, "error_desc");
        
        Node statusListNode = XMLHandler.getSubNode(transStatusNode, "stepstatuslist");
        int nr = XMLHandler.countNodes(statusListNode, StepStatus.XML_TAG);
        for (int i=0;i<nr;i++)
        {
            Node stepStatusNode = XMLHandler.getSubNodeByNr(statusListNode, StepStatus.XML_TAG, i);
            stepStatusList.add(new StepStatus(stepStatusNode));
        }
        
        String loggingString64 = XMLHandler.getTagValue(transStatusNode, "logging_string");
        // This is a Base64 encoded GZIP compressed stream of data.
        try
        {
            byte[] bytes = new byte[] {};
            if (loggingString64!=null) bytes = Base64.decodeBase64(loggingString64.getBytes());
            if (bytes.length>0)
            {
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                GZIPInputStream gzip = new GZIPInputStream(bais);
                int c;
                StringBuffer buffer = new StringBuffer();
                while ( (c=gzip.read())!=-1) buffer.append((char)c);
                gzip.close();
                loggingString = buffer.toString();
            }
            else
            {
                loggingString="";
            }
        }
        catch(IOException e)
        {
            loggingString = "Unable to decode logging from remote server : "+e.toString()+Const.CR+Const.getStackTracker(e);
        }
    }
    
    public static SlaveServerTransStatus fromXML(String xml) throws KettleXMLException
    {
        Document document = XMLHandler.loadXMLString(xml);
        SlaveServerTransStatus status = new SlaveServerTransStatus(XMLHandler.getSubNode(document, XML_TAG));
        return status;
    }

    /**
     * @return the statusDescription
     */
    public String getStatusDescription()
    {
        return statusDescription;
    }
    /**
     * @param statusDescription the statusDescription to set
     */
    public void setStatusDescription(String statusDescription)
    {
        this.statusDescription = statusDescription;
    }
    /**
     * @return the transName
     */
    public String getTransName()
    {
        return transName;
    }
    /**
     * @param transName the transName to set
     */
    public void setTransName(String transName)
    {
        this.transName = transName;
    }

    /**
     * @return the errorDescription
     */
    public String getErrorDescription()
    {
        return errorDescription;
    }

    /**
     * @param errorDescription the errorDescription to set
     */
    public void setErrorDescription(String errorDescription)
    {
        this.errorDescription = errorDescription;
    }

    /**
     * @return the stepStatusList
     */
    public List<StepStatus> getStepStatusList()
    {
        return stepStatusList;
    }

    /**
     * @param stepStatusList the stepStatusList to set
     */
    public void setStepStatusList(List<StepStatus> stepStatusList)
    {
        this.stepStatusList = stepStatusList;
    }

    /**
     * @return the loggingString
     */
    public String getLoggingString()
    {
        return loggingString;
    }

    /**
     * @param loggingString the loggingString to set
     */
    public void setLoggingString(String loggingString)
    {
        this.loggingString = loggingString;
    }

    public boolean isRunning()
    {
        return getStatusDescription().equalsIgnoreCase(Trans.STRING_RUNNING) || getStatusDescription().equalsIgnoreCase(Trans.STRING_INITIALIZING);
    }

    public long getNrStepErrors()
    {
        long errors = 0L;
        for (int i=0;i<stepStatusList.size();i++)
        {
            StepStatus stepStatus = (StepStatus) stepStatusList.get(i);
            errors+=stepStatus.getErrors();
        }
        return errors;
    }
    
    public Result getResult(TransMeta transMeta)
    {
    	Result result = new Result();
    	
    	for (StepStatus stepStatus : stepStatusList) {
    		
			result.setNrErrors(result.getNrErrors()+stepStatus.getErrors());
			
			if (transMeta.getReadStep()    !=null && stepStatus.getStepname().equals(transMeta.getReadStep().getName())) {
				result.setNrLinesRead(result.getNrLinesRead()+ stepStatus.getLinesRead());
			}
			if (transMeta.getInputStep()   !=null && stepStatus.getStepname().equals(transMeta.getInputStep().getName())) {
				result.setNrLinesInput(result.getNrLinesInput() + stepStatus.getLinesInput());
			}
			if (transMeta.getWriteStep()   !=null && stepStatus.getStepname().equals(transMeta.getWriteStep().getName())) {
				result.setNrLinesWritten(result.getNrLinesWritten()+stepStatus.getLinesWritten());
			}
			if (transMeta.getOutputStep()  !=null && stepStatus.getStepname().equals(transMeta.getOutputStep().getName())) {
				result.setNrLinesOutput(result.getNrLinesOutput()+stepStatus.getLinesOutput());
			}
			if (transMeta.getUpdateStep()  !=null && stepStatus.getStepname().equals(transMeta.getUpdateStep().getName())) {
				result.setNrLinesUpdated(result.getNrLinesUpdated()+stepStatus.getLinesUpdated());
			}
            if (transMeta.getRejectedStep()!=null && stepStatus.getStepname().equals(transMeta.getRejectedStep().getName())) {
            	result.setNrLinesRejected(result.getNrLinesRejected()+stepStatus.getLinesRejected());
            }
    	}
    	
    	return result;
    }
}
