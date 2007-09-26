package org.pentaho.di.job;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class JobConfiguration
{
    public static final String XML_TAG = "job_configuration";
    
    private JobMeta jobMeta;
    private JobExecutionConfiguration jobExecutionConfiguration;
    
    /**
     * @param jobMeta
     * @param jobExecutionConfiguration
     */
    public JobConfiguration(JobMeta jobMeta, JobExecutionConfiguration jobExecutionConfiguration)
    {
        this.jobMeta = jobMeta;
        this.jobExecutionConfiguration = jobExecutionConfiguration;
    }
    
    public String getXML()
    {
        StringBuffer xml = new StringBuffer();
        
        xml.append("<"+XML_TAG+">").append(Const.CR);
        
        xml.append(jobMeta.getXML());
        xml.append(jobExecutionConfiguration.getXML());
        
        xml.append("</"+XML_TAG+">").append(Const.CR);
        
        return xml.toString();
    }
    
    public JobConfiguration(Node configNode) throws KettleXMLException
    {
        Node jobNode = XMLHandler.getSubNode(configNode, JobMeta.XML_TAG);
        jobMeta = new JobMeta(LogWriter.getInstance(), jobNode, null, null);
        Node trecNode = XMLHandler.getSubNode(configNode, JobExecutionConfiguration.XML_TAG);
        jobExecutionConfiguration = new JobExecutionConfiguration(trecNode);
    }
    
    public static final JobConfiguration fromXML(String xml) throws KettleXMLException
    {
        Document document = XMLHandler.loadXMLString(xml);
        Node configNode = XMLHandler.getSubNode(document, XML_TAG);
        return new JobConfiguration(configNode);
    }
    
    /**
     * @return the jobExecutionConfiguration
     */
    public JobExecutionConfiguration getJobExecutionConfiguration()
    {
        return jobExecutionConfiguration;
    }
    /**
     * @param jobExecutionConfiguration the jobExecutionConfiguration to set
     */
    public void setJobExecutionConfiguration(JobExecutionConfiguration jobExecutionConfiguration)
    {
        this.jobExecutionConfiguration = jobExecutionConfiguration;
    }
    /**
     * @return the job metadata
     */
    public JobMeta getJobMeta()
    {
        return jobMeta;
    }
    /**
     * @param jobMeta the job meta data to set
     */
    public void setJobMeta(JobMeta jobMeta)
    {
        this.jobMeta = jobMeta;
    }
}
