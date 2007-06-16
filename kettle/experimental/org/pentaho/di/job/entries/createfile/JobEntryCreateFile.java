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
 
package org.pentaho.di.job.entries.createfile;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.vfs.FileObject;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Result;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import org.pentaho.di.core.util.StringUtil;
import be.ibridge.kettle.core.vfs.KettleVFS;




/**
 * This defines a 'create file' job entry. Its main use would be to create empty
 * trigger files that can be used to control the flow in ETL cycles.
 * 
 * @author Sven Boden
 * @since 28-01-2007
 *
 */
public class JobEntryCreateFile extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private String filename;
	private boolean failIfFileExists;
	
	public JobEntryCreateFile(String n)
	{
		super(n, "");
		filename=null;
		failIfFileExists=true;
		setID(-1L);
		setType(JobEntryInterface.TYPE_JOBENTRY_CREATE_FILE);
	}

	public JobEntryCreateFile()
	{
		this("");
	}

	public JobEntryCreateFile(JobEntryBase jeb)
	{
		super(jeb);
	}

    public Object clone()
    {
        JobEntryCreateFile je = (JobEntryCreateFile) super.clone();
        return je;
    }
    
	public String getXML()
	{
        StringBuffer retval = new StringBuffer(50);
		
		retval.append(super.getXML());		
		retval.append("      ").append(XMLHandler.addTagValue("filename",   filename));
		retval.append("      ").append(XMLHandler.addTagValue("fail_if_file_exists", failIfFileExists));
		
		return retval.toString();
	}
	
	public void loadXML(Node entrynode, ArrayList databases, Repository rep)
		throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
			filename = XMLHandler.getTagValue(entrynode, "filename");
			failIfFileExists = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "fail_if_file_exists"));
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException("Unable to load job entry of type 'create file' from XML node", xe);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, ArrayList databases)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases);
			filename = rep.getJobEntryAttributeString(id_jobentry, "filename");
			failIfFileExists = rep.getJobEntryAttributeBoolean(id_jobentry, "fail_if_file_exists");
		}
		catch(KettleException dbe)
		{
			throw new KettleException("Unable to load job entry of type 'create file' from the repository for id_jobentry="+id_jobentry, dbe);
		}
	}
	
	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);
			
			rep.saveJobEntryAttribute(id_job, getID(), "filename", filename);
            rep.saveJobEntryAttribute(id_job, getID(), "fail_if_file_exists", failIfFileExists);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type 'create file' to the repository for id_job="+id_job, dbe);
		}
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
	}
	
	public String getFilename()
	{
		return filename;
	}
    
    public String getRealFilename()
    {
        return StringUtil.environmentSubstitute(getFilename());
    }
	
	public Result execute(Result previousResult, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();
		Result result = previousResult;
		result.setResult( false );
	
		if (filename!=null)
		{
            String realFilename = getRealFilename();
            FileObject fileObject = null;
			try {
				fileObject = KettleVFS.getFileObject(realFilename);

				if ( fileObject.exists() )
				{
					if ( isFailIfFileExists() )
					{
						// File exists and fail flag is on.
					    result.setResult( false );
					    log.logError(toString(), "File ["+realFilename+"] exists, failing.");
					}
					else
					{
						// File already exists, no reason to try to create it
					    result.setResult( true );
					    log.logBasic(toString(), "File ["+realFilename+"] already exists, not recreating.");
					}
				}
				else
				{
					//  No file yet, create an empty file.
					fileObject.createFile();					
					log.logBasic(toString(), "File ["+realFilename+"] created!");
					result.setResult( true );
				}
			} catch (IOException e) {
				log.logError(toString(), "Could not create file ["+realFilename+"], exception: " + e.getMessage());
				result.setResult( false );
				result.setNrErrors(1);					
			}
            finally {
            	if ( fileObject != null )
            	{
            		try  {
            		     fileObject.close();
            		}
            		catch ( IOException ex ) {};
            	}
            }			
		}
		else
		{			
			log.logError(toString(), "No filename is defined.");
		}
		
		return result;
	}

	public boolean evaluates()
	{
		return true;
	}
    
    public JobEntryDialogInterface getDialog(Shell shell,JobEntryInterface jei,JobMeta jobMeta,String jobName,Repository rep) {
        return new JobEntryCreateFileDialog(shell,this,jobMeta);
    }

	public boolean isFailIfFileExists() {
		return failIfFileExists;
	}

	public void setFailIfFileExists(boolean failIfFileExists) {
		this.failIfFileExists = failIfFileExists;
	}
}