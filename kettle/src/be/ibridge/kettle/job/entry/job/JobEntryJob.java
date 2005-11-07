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
 
package be.ibridge.kettle.job.entry.job;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.w3c.dom.Node;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.job.Job;
import be.ibridge.kettle.job.entry.JobEntryBase;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.repository.RepositoryDirectory;


/**
 * Recursive definition of a Job.  This step means that an entire Job has to be executed.
 * It can be the same Job, but just make sure that you don't get an endless loop.
 * Provide an escape routine using JobEval.
 * 
 * @author Matt
 * @since 01-10-2003, Rewritten on 18-06-2004
 * 
 */

public class JobEntryJob extends JobEntryBase implements Cloneable, JobEntryInterface
{	
	private String              jobname;
	private String              filename;
	private RepositoryDirectory directory;

	public  String  arguments[];
	public  boolean arg_from_previous;

	public  boolean set_logfile;
	public  String  logfile, logext;
	public  boolean add_date, add_time;
	public  int     loglevel;
	
	public  boolean parallel;
		
	
	public JobEntryJob(String name)
	{
		super(name, "");
		setType(JobEntryInterface.TYPE_JOBENTRY_JOB);
	}
	
	public JobEntryJob()
	{
		this("");
		clear();
	}
	
	public JobEntryJob(JobEntryBase jeb)
	{
		super(jeb);
	}

	public void setFileName(String n)
	{
		filename=n;
	}
	
	public String getFileName()
	{
		return filename;
	}

	public void setJobName(String jobname)
	{
		this.jobname=jobname;
	}
	
	public String getJobName()
	{
		return jobname;
	}
	
	public RepositoryDirectory getDirectory()
	{
		return directory;
	}
	
	public void setDirectory(RepositoryDirectory directory)
	{
		this.directory = directory;
	}
				
	public String getLogFilename()
	{
		String retval="";
		if (set_logfile)
		{
			retval+=logfile;
			Calendar cal = Calendar.getInstance();
			if (add_date)
			{
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
				retval+="_"+sdf.format(cal.getTime());
			}
			if (add_time)
			{
				SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
				retval+="_"+sdf.format(cal.getTime());
			}
			if (logext!=null && logext.length()>0)
			{
				retval+="."+logext;
			}
		}
		return retval;
	}
	
	public String getXML()
	{
		String retval ="";

		retval+=super.getXML();
		
		retval+="      "+XMLHandler.addTagValue("filename",          filename);
		retval+="      "+XMLHandler.addTagValue("jobname",           jobname);
		retval+="      "+XMLHandler.addTagValue("directory",         directory.getPath());
		retval+="      "+XMLHandler.addTagValue("arg_from_previous", arg_from_previous);
		retval+="      "+XMLHandler.addTagValue("set_logfile",       set_logfile);
		retval+="      "+XMLHandler.addTagValue("logfile",           logfile);
		retval+="      "+XMLHandler.addTagValue("logext",            logext);
		retval+="      "+XMLHandler.addTagValue("add_date",          add_date);
		retval+="      "+XMLHandler.addTagValue("add_time",          add_time);
		retval+="      "+XMLHandler.addTagValue("loglevel",          LogWriter.getLogLevelDesc(loglevel));

		if (arguments!=null)
		for (int i=0;i<arguments.length;i++)
		{
			retval+="      "+XMLHandler.addTagValue("argument"+i, arguments[i]);
		}
	
		return retval;
	}
					
	public void loadXML(Node entrynode, ArrayList databases, RepositoryDirectory directory_tree)
		throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
	
			setFileName( XMLHandler.getTagValue(entrynode, "filename") );
			setJobName( XMLHandler.getTagValue(entrynode, "jobname") );
			arg_from_previous = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "arg_from_previous") );
			set_logfile = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "set_logfile") );
			add_date = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "add_date") );
			add_time = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "add_time") );
			logfile = XMLHandler.getTagValue(entrynode, "logfile");
			logext = XMLHandler.getTagValue(entrynode, "logext");
			loglevel = LogWriter.getLogLevel( XMLHandler.getTagValue(entrynode, "loglevel"));
	
			// The directory...
		    directory = directory_tree.findDirectory( XMLHandler.getTagValue(entrynode, "directory") );
	
			// How many arguments?
			int argnr = 0;
			while ( XMLHandler.getTagValue(entrynode, "argument"+argnr)!=null) argnr++;
			arguments = new String[argnr];
			
			// Read them all...
			for (int a=0;a<argnr;a++) arguments[a]=XMLHandler.getTagValue(entrynode, "argument"+a);
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException("Unable to load 'job' job entry from XML node", xe);
		}
	}
	
	/*
	 * Load the jobentry from repository
	 */
	public void loadRep(Repository rep, long id_jobentry, ArrayList databases)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases);

			long id_job_attr = rep.getJobEntryAttributeInteger(id_jobentry, "id_job");
			Row r = rep.getJob(id_job_attr);
			if (r!=null) 
			{
				jobname = r.getString("NAME", null);
				long id_directory =  r.getInteger("ID_DIRECTORY", 0L);
				if (id_directory>0)
				{
					directory = rep.getDirectoryTree().findDirectory(id_directory);
				}
				else
				{
					directory = rep.getDirectoryTree();
				}
			}
			
			filename          = rep.getJobEntryAttributeString(id_jobentry, "filename");
			arg_from_previous = rep.getJobEntryAttributeBoolean(id_jobentry, "arg_from_previous");
	
			set_logfile       = rep.getJobEntryAttributeBoolean(id_jobentry, "set_logfile");
			add_date          = rep.getJobEntryAttributeBoolean(id_jobentry, "add_date");
			add_time          = rep.getJobEntryAttributeBoolean(id_jobentry, "arg_time");
			logfile           = rep.getJobEntryAttributeString(id_jobentry, "logfile");
			logext            = rep.getJobEntryAttributeString(id_jobentry, "logext");
			loglevel          = LogWriter.getLogLevel( rep.getJobEntryAttributeString(id_jobentry, "loglevel") );
	
			// How many arguments?
			int argnr = rep.countNrJobEntryAttributes(id_jobentry, "argument");
			arguments = new String[argnr];
			
			// Read them all...
			for (int a=0;a<argnr;a++) 
			{
				arguments[a]= rep.getJobEntryAttributeString(id_jobentry, a, "argument");
			}
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to load job entry of type 'job' from the repository with id_jobentry="+id_jobentry, dbe);
		}
	}
	
	// Save the attributes of this job entry
	//
	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);

			long id_job_attr = rep.getJobID(jobname, directory.getID());
			rep.saveJobEntryAttribute(id_job, getID(), "id_job", id_job_attr);
			rep.saveJobEntryAttribute(id_job, getID(), "file_name", filename);
			rep.saveJobEntryAttribute(id_job, getID(), "arg_from_previous", arg_from_previous);
			rep.saveJobEntryAttribute(id_job, getID(), "set_logfile", set_logfile);
			rep.saveJobEntryAttribute(id_job, getID(), "add_date", add_date);
			rep.saveJobEntryAttribute(id_job, getID(), "add_time", add_time);
			rep.saveJobEntryAttribute(id_job, getID(), "logfile", logfile);
			rep.saveJobEntryAttribute(id_job, getID(), "logext", logext);
			rep.saveJobEntryAttribute(id_job, getID(), "loglevel", LogWriter.getLogLevelDesc(loglevel));
			
			// save the arguments...
			if (arguments!=null)
			{
				for (int i=0;i<arguments.length;i++) 
				{
					rep.saveJobEntryAttribute(id_job, getID(), i, "argument", arguments[i]);
				}
			}
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type job to the repository with id_job="+id_job, dbe);
		}
	}	
	
	public Result execute(Result prev_result, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();

		Result result = prev_result;
		result.setEntryNr( nr );
		
		LogWriter logwriter = log;
		if (set_logfile) logwriter = LogWriter.getInstance(getLogFilename(), true, loglevel);
		
		Job job = new Job(logwriter, getName(), getFileName(), arguments);
		try
		{
			job.open(rep, getFileName(), getName(), directory.getPath());
			job.setJobEntryResults(parentJob.getJobEntryResults());//
			
			JobEntryJobRunner runner = new JobEntryJobRunner( job, prev_result, nr);
			new Thread(runner).start();
			
			while (!runner.isFinished() && !parentJob.isStopped())
			{
				try { Thread.sleep(100);}
				catch(InterruptedException e) { }
			}
			
			// if the parent-job was stopped, stop the sub-job too... 
			if (parentJob.isStopped())
			{
				job.stopAll();
				runner.waitUntilFinished(); // Wait until finished!
				job.endProcessing("stop");
			}
			else
			{
				job.endProcessing("end");
			}
			
			result = runner.getResult();
		}
		catch(KettleException je)
		{
			log.logError(toString(), "Unable to open job entry job with name ["+getName()+"]");
			result.setNrErrors(1);
		}
		
		if (set_logfile) logwriter.close();
		
		if (result.getNrErrors() > 0)
		{
			result.setResult( false );
		}
		else
		{
			result.setResult( true );
		}

		return result;
	}
	
	
	public void clear()
	{
		super.clear();
		
		jobname=null;
		filename=null;
		directory = new RepositoryDirectory();
		arguments=null;
		arg_from_previous=false;
		add_date=false;
		add_time=false;
		logfile=null;
		logext=null;
		set_logfile=false;
	}

	public boolean evaluates()
	{
		return true;
	}

	public boolean isUnconditional()
	{
		return true;
	}

}
