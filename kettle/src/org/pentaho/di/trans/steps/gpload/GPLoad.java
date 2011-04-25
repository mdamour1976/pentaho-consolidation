 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.trans.steps.gpload;

//
// The "designer" notes of the Greenplum bulkloader:
// ----------------------------------------------
//
// - "Enclosed" is used in the loader instead of "optionally enclosed" as optionally
//   encloses kind of destroys the escaping.
// - A Boolean is output as Y and N (as in the text output step e.g.). If people don't
//   like this they can first convert the boolean value to something else before loading
//   it.
// - Filters (besides data and datetime) are not supported as it slows down.
//
// 

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/**
 * Performs a bulk load to an Greenplum table.
 *
 * Based on (copied from) Sven Boden's Oracle Bulk Loader step
 * @author Luke Lonergan, Matt Casters, Sean Flatley
 * @since  28-mar-2008, 17-dec-2010
 */
public class GPLoad extends BaseStep implements StepInterface
{
	private static Class<?> PKG = GPLoadMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private static String INDENT = "    ";
	private static String GPLOAD_YAML_VERSION = "VERSION: 1.0.0.1";
	private static String SINGLE_QUOTE = "'";
	private static String OPEN_BRACKET = "[";
	private static String CLOSE_BRACKET = "]";
	private static String SPACE_PADDED_DASH = " - ";
	private static String COLON = ":";
	private static int GPLOAD_MAX_ERRORS_DEFAULT = 0;
			
	Process gploadProcess = null;
	
	private GPLoadMeta meta;
	protected GPLoadData data;
	private GPLoadDataOutput output = null;
		
	/*
	 * Local copy of the transformation "preview" property. We only forward
	 * the rows upon previewing, we don't do any of the real stuff.
	 */
	private boolean preview = false;
  
  //
  // This class continually reads from the stream, and sends it to the log
  // if the logging level is at least basic level.
  //
  final private class StreamLogger extends Thread
  {
    private InputStream input;
    private String type;
    
    StreamLogger(InputStream is, String type) {
      this.input = is;
      this.type = type + ">"; //$NON-NLS-1$
    }

    public void run() {
      try
      {
        final BufferedReader br = new BufferedReader(new InputStreamReader(input));
        String line;
        while ( (line = br.readLine()) != null)
        {
          // Only perform the concatenation if at basic level. Otherwise,
          // this just reads from the stream.
          if (log.isBasic()) { 
            logBasic(type + line);
          }
        }
      } 
      catch (IOException ioe)
      {
          ioe.printStackTrace();  
      }
      
    }
    
  }
	
	
	public GPLoad(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	/**
	 * Get the contents of the control file as specified in the meta object
	 * 
	 * @param meta the meta object to model the control file after
	 * 
	 * @return a string containing the control file contents
	 */
	public String getControlFileContents(GPLoadMeta meta, RowMetaInterface rm, Object[] r) throws KettleException	{

      String tableFields[] = meta.getFieldTable();
      boolean matchColumn[] = meta.getMatchColumn();
      boolean updateColumn[] = meta.getUpdateColumn();
      
      //  TODO:  All this validation could be placed in it's own method,
      
      //  table name validation 
      DatabaseMeta databaseMeta = meta.getDatabaseMeta();
      String schemaName = meta.getSchemaName();
      String targetTablename = meta.getTableName();
      
      //  TODO:  What is schema name to a GreenPlum database?
      //  Testing has been with an empty schema name
      //  We will set it to an empty string if it is null
      //  If it is not null then we will process what it is
      if (schemaName == null) {
         schemaName = "";
      }
      
      if (targetTablename == null) {
         throw new KettleException(BaseMessages.getString(PKG, "GPLoad.Exception.TargetTableNameMissing"));
      }
      targetTablename = environmentSubstitute(targetTablename).trim();
      if (Const.isEmpty(targetTablename)) {
         throw new KettleException(BaseMessages.getString(PKG, "GPLoad.Exception.TargetTableNameMissing"));
      }
      
      targetTablename = databaseMeta.getQuotedSchemaTableCombination(
          environmentSubstitute(meta.getSchemaName()),
          environmentSubstitute(meta.getTableName()));
      
      String loadAction = meta.getLoadAction();
	         
      //  match and update column verification
      if (loadAction.equalsIgnoreCase(GPLoadMeta.ACTION_MERGE) || loadAction.equalsIgnoreCase(GPLoadMeta.ACTION_UPDATE)) {
         
         //  throw an exception if we don't have match columns
         if (matchColumn == null) {
            throw new KettleException(BaseMessages.getString(PKG, "GPLoad.Exception.MatchColumnsNeeded"));
         }
         
         if (!meta.hasMatchColumn()) {
            throw new KettleException(BaseMessages.getString(PKG, "GPLoad.Exception.MatchColumnsNeeded"));
         }
         
         //  throw an exception if we don't have any update columns
         if (updateColumn == null) {
            throw new KettleException(BaseMessages.getString(PKG, "GPLoad.Exception.UpdateColumnsNeeded"));
         }
         
         if (!meta.hasUpdateColumn()) {
            throw new KettleException(BaseMessages.getString(PKG, "GPLoad.Exception.UpdateColumnsNeeded"));
         }    
      }

      //  data file validation
      String dataFilename = meta.getDataFile();
      if (!Const.isEmpty(dataFilename)) {
         dataFilename = environmentSubstitute(dataFilename).trim();
      }
      if (Const.isEmpty(dataFilename)) {
         throw new KettleException(BaseMessages.getString(PKG, "GPload.Exception.DataFileMissing"));
      }

      //  delimiter validation
      String delimiter = meta.getDelimiter();
      if (!Const.isEmpty(delimiter)) {
         delimiter = environmentSubstitute(delimiter).trim();
      }
      if (Const.isEmpty(delimiter)) {
         throw new KettleException(BaseMessages.getString(PKG, "GPload.Exception.DelimiterMissing"));
      }
      
      //  Now we start building the contents
		StringBuffer contents = new StringBuffer(1000);
		
		// Source: GP Admin Guide 3.3.6, page 635:
		contents.append(GPLoad.GPLOAD_YAML_VERSION).append(Const.CR);
      contents.append("DATABASE: ").append(environmentSubstitute(databaseMeta.getDatabaseName())).append(Const.CR);
      contents.append("USER: ").append(environmentSubstitute(databaseMeta.getUsername())).append(Const.CR);
      contents.append("HOST: ").append(environmentSubstitute(databaseMeta.getHostname())).append(Const.CR);
      contents.append("PORT: ").append(environmentSubstitute(databaseMeta.getDatabasePortNumberString())).append(Const.CR);
      contents.append("GPLOAD:").append(Const.CR);
      contents.append(GPLoad.INDENT).append("INPUT: ").append(Const.CR);
      contents.append(GPLoad.INDENT).append("- SOURCE: ").append(Const.CR);

      //  Add a LOCAL_HOSTS section
      //  We first check to see if the array has any elements
      //  if so we proceed with the string building - if not we do not add LOCAL_HOSTNAME section.
      String[] localHosts = meta.getLocalHosts();
      String stringLocalHosts = null;
      if (!Const.isEmpty(localHosts)) {
         StringBuilder sbLocalHosts = new StringBuilder();
         String trimmedAndSubstitutedLocalHost;
         for (String localHost: localHosts) {
            trimmedAndSubstitutedLocalHost = environmentSubstitute(localHost.trim());
            if (!Const.isEmpty(trimmedAndSubstitutedLocalHost)) {
                sbLocalHosts.append(GPLoad.INDENT).append(GPLoad.INDENT).append(GPLoad.SPACE_PADDED_DASH).append(trimmedAndSubstitutedLocalHost).append(Const.CR);
            }
         }
         stringLocalHosts = sbLocalHosts.toString();
         if (!Const.isEmpty(stringLocalHosts)) {
            contents.append(GPLoad.INDENT).append("- LOCAL_HOSTNAME: ").append(Const.CR).append(stringLocalHosts);
         }
      }
        
      //  Add a PORT section if we have a port
      String masterPort = meta.getMasterPort();
      if (!Const.isEmpty(masterPort)) {
         masterPort = environmentSubstitute(meta.getMasterPort().trim());
         if (!Const.isEmpty(masterPort)) {
            contents.append(GPLoad.INDENT).append("- PORT: ").append(masterPort).append(Const.CR);
         }
      }
        
      // TODO: Stream to a temporary file and then bulk load OR optionally stream to a named pipe (like MySQL bulk loader)
      dataFilename = GPLoad.SINGLE_QUOTE + environmentSubstitute(dataFilename) + GPLoad.SINGLE_QUOTE;
      contents.append(GPLoad.INDENT).append(GPLoad.INDENT).append("FILE: ").append(GPLoad.OPEN_BRACKET).append(dataFilename).append(GPLoad.CLOSE_BRACKET).append(Const.CR);
       
      //  columns
      if (tableFields.length > 0) {
         contents.append(GPLoad.INDENT).append("- COLUMNS: ").append(Const.CR);
         
         for (String columnName: tableFields) {     
            contents.append(GPLoad.INDENT).append(GPLoad.INDENT).append(GPLoad.SPACE_PADDED_DASH).append(databaseMeta.quoteField(columnName)).append(GPLoad.COLON).append(Const.CR);
         }
      }
      
      // See also page 155 for formatting information & escaping
      // delimiter validation should have been perfomed
      contents.append(GPLoad.INDENT).append("- FORMAT: TEXT").append(Const.CR);
      contents.append(GPLoad.INDENT).append("- DELIMITER: ").append(GPLoad.SINGLE_QUOTE).append(delimiter).append(GPLoad.SINGLE_QUOTE).append(Const.CR);
        
      // TODO: implement escape character, null_as
      // TODO: test what happens when a single quote is specified- can we specify a single quiote within doubole quotes then?
      String enclosure = meta.getEnclosure();
      
      //  For enclosure we do a null check.  !Const.isEmpty will be true if the string is empty.
      //  it is ok to have an empty string
      if (enclosure != null) {
         enclosure = environmentSubstitute(meta.getEnclosure());
      }
      else {
         enclosure = "";
      }
      contents.append(GPLoad.INDENT).append("- QUOTE: ").append(GPLoad.SINGLE_QUOTE).append(enclosure).append(GPLoad.SINGLE_QUOTE).append(Const.CR);
      contents.append(GPLoad.INDENT).append("- HEADER: FALSE").append(Const.CR);
        
      // TODO: implement database encoding support
      // contents.append("    - ENCODING: ").append(Const.CR);
      
      //  Max errors
      String maxErrors = meta.getMaxErrors();
      if (maxErrors == null) {
         maxErrors = GPLoadMeta.MAX_ERRORS_DEFAULT;
      }
      else {
         maxErrors = environmentSubstitute(maxErrors);
         try {
            if (Integer.valueOf(maxErrors) < 0) {
               throw new KettleException(BaseMessages.getString(PKG, "GPLoad.Exception.MaxErrorsInvalid"));
            }
         }
         catch (NumberFormatException nfe) {
            throw new KettleException(BaseMessages.getString(PKG, "GPLoad.Exception.MaxErrorsInvalid"));
         }
      }

      contents.append(GPLoad.INDENT).append("- ERROR_LIMIT: ").append(meta.getMaxErrors()).append(Const.CR);
        
      String errorTableName = meta.getErrorTableName(); 
      if (!Const.isEmpty(errorTableName)) {
         errorTableName = environmentSubstitute(errorTableName).trim();
         if (!Const.isEmpty(errorTableName)) {
            contents.append(GPLoad.INDENT).append("- ERROR_TABLE: ").append(meta.getErrorTableName()).append(Const.CR);
         }
      }
        
      //--------------  OUTPUT section
      
      contents.append(GPLoad.INDENT).append("OUTPUT:").append(Const.CR);

      //  TODO: not too sure this has to be done
      String tableName = databaseMeta.getQuotedSchemaTableCombination(
          environmentSubstitute(meta.getSchemaName()),
          environmentSubstitute(meta.getTableName()));

      contents.append(GPLoad.INDENT).append("- TABLE: ").append(tableName).append(Const.CR);
      contents.append(GPLoad.INDENT).append("- MODE: ").append(meta.getLoadAction()).append(Const.CR);
      
      // TODO: add support for MATCH_COLUMNS, UPDATE_COLUMN, UPDATE_CONDITION, MAPPING
      // TODO: add support for BEFORE and AFTER SQL

      //  if we have match columns then add the specification
    	if (meta.hasMatchColumn()) {
    	   contents.append(GPLoad.INDENT).append("- MATCH_COLUMNS: ").append(Const.CR);
    	
    	   for (int i=0; i < matchColumn.length; i++) {
    	      if (matchColumn[i]) {
       		   contents.append(GPLoad.INDENT).append(GPLoad.INDENT).append(GPLoad.SPACE_PADDED_DASH).append(databaseMeta.quoteField(tableFields[i])).append(Const.CR);
       	   }
    	   }
    	}
    	
    	//  if we have update columns then add the specification
    	if (meta.hasUpdateColumn()) {
         contents.append(GPLoad.INDENT).append("- UPDATE_COLUMNS: ").append(Const.CR);
         
         for (int i=0; i < updateColumn.length; i++) {
            if (updateColumn[i]) {
               contents.append(GPLoad.INDENT).append(GPLoad.INDENT).append(GPLoad.SPACE_PADDED_DASH).append(databaseMeta.quoteField(tableFields[i])).append(Const.CR);
            }
         }
    	}
    	
		return contents.toString();
	}
	
	/**
	 * Create a control file.
	 * 
	 * @param filename
	 * @param meta
	 * @throws KettleException
	 */
	public void createControlFile(String filename, Object[] row, GPLoadMeta meta) throws KettleException
	{
		File controlFile = new File(filename);
		FileWriter fw = null;
		
		try
		{
			controlFile.createNewFile();
			fw = new FileWriter(controlFile);
 	      fw.write(getControlFileContents(meta, getInputRowMeta(), row));	       
		}
		catch ( IOException ex )
		{
		    throw new KettleException(ex.getMessage(), ex);
		}
		finally
		{
			try  {
			    if ( fw != null )
				    fw.close();
			}
			catch ( Exception ex ) {}
		}
	}
	
	/**
	 * Create the command line for a psql process depending on the meta
	 * information supplied.
	 * 
	 * @param meta The meta data to create the command line from
	 * @param password Use the real password or not
	 * 
	 * @return The string to execute.
	 * 
	 * @throws KettleException Upon any exception
	 */
	public String createCommandLine(GPLoadMeta meta, boolean password) throws KettleException
	{
	   StringBuffer sb = new StringBuffer(300);
	   
	   if ( meta.getGploadPath() != null )
	   {
		   try
		   {
	           FileObject fileObject = KettleVFS.getFileObject(environmentSubstitute(meta.getGploadPath()), getTransMeta());
  	      	   String psqlexec = KettleVFS.getFilename(fileObject);
		       //sb.append('\'').append(psqlexec).append('\'');
  	      	   sb.append(psqlexec);
  	       }
	       catch ( Exception ex )
	       {
	           throw new KettleException("Error retrieving sqlldr string", ex);
	       }		       
	   }
	   else
	   {
		   throw new KettleException("No psql application specified");
	   }

	   if ( meta.getControlFile() != null )
	   {
		   try
		   {
	           FileObject fileObject = KettleVFS.getFileObject(environmentSubstitute(meta.getControlFile()), getTransMeta());   
		   
	           sb.append(" -f ");
	           //sb.append('\'').append(KettleVFS.getFilename(fileObject)).append('\'');
	           sb.append(KettleVFS.getFilename(fileObject));
  	       }
	       catch ( Exception ex )
	       {
	           throw new KettleException("Error retrieving controlfile string", ex);
	       }		   
	   }
	   else
	   {
		   throw new KettleException("No control file specified");
	   }
	   	   
	   if ( meta.getLogFile() != null )
	   {
		   try 
		   {
		       FileObject fileObject = KettleVFS.getFileObject(environmentSubstitute(meta.getLogFile()), getTransMeta());   
	   
		       sb.append(" -l ");
		       sb.append('\'').append(KettleVFS.getFilename(fileObject)).append('\'');
		   }
		   catch ( Exception ex )
		   {
		       throw new KettleException("Error retrieving logfile string", ex);
		   }
	   }

	   // hostname, port and so on are passed through the control file
	   //

	   return sb.toString(); 
	}
	
	public boolean execute(GPLoadMeta meta, boolean wait) throws KettleException
	{
        Runtime rt = Runtime.getRuntime();

        try  
        {
            gploadProcess = rt.exec(createCommandLine(meta, true));
            // any error message?
            StreamLogger errorLogger = new 
                StreamLogger(gploadProcess.getErrorStream(), "ERROR");
        
            // any output?
            StreamLogger outputLogger = new 
                StreamLogger(gploadProcess.getInputStream(), "OUTPUT");
            
            // kick them off
            errorLogger.start();
            outputLogger.start();                              

            if ( wait ) 
            {
                // any error???
            	int exitVal = gploadProcess.waitFor();
				logBasic(BaseMessages.getString(PKG, "GPLoad.Log.ExitValuePsqlPath", "" + exitVal)); //$NON-NLS-1$
            }
        }
        catch ( Exception ex )
        {
        	// Don't throw the message upwards, the message contains the password.
        	throw new KettleException("Error while executing \'" + createCommandLine(meta, false) + "\'");
        }
        
        return true;
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(GPLoadMeta)smi;
		data=(GPLoadData)sdi;

		try
		{
			Object[] r=getRow();  // Get row from input rowset & set row busy!
			if (r==null)          // no more input to be expected...
			{
				setOutputDone();

				if ( ! preview )
				{
					if ( output != null )
					{
						// Close the output
						try  {
							output.close();
						}
						catch ( IOException e )
						{
							throw new KettleException("Error while closing output", e); 
						}

						output = null;
					}

					String loadMethod = meta.getLoadMethod();
					if ( GPLoadMeta.METHOD_AUTO_END.equals(loadMethod))
					{
						execute(meta, true);
					}
				//	else if ( GPLoadMeta.METHOD_AUTO_CONCURRENT.equals(meta.getLoadMethod()) )
				//	{
				//		try 
				//		{
				//			if ( psqlProcess != null )
				//			{
				//				int exitVal = psqlProcess.waitFor();
				//				logBasic(BaseMessages.getString(PKG, "GPLoad.Log.ExitValueSqlldr", "" + exitVal)); //$NON-NLS-1$
				//			}
				//			else
				//			{
				//				throw new KettleException("Internal error: no sqlldr process running");
				//			}
				//		}
				//		catch ( Exception ex )
				//		{
				//			throw new KettleException("Error while executing sqlldr", ex);
				//		}
				//	}
				}			
				return false;
			}

			if ( ! preview )
			{
				if (first)
				{
					first=false;
					createControlFile(environmentSubstitute(meta.getControlFile()), r, meta);
					output = new GPLoadDataOutput(meta, log.getLogLevel());

				//	if ( GPLoadMeta.METHOD_AUTO_CONCURRENT.equals(meta.getLoadMethod()) )
				//	{
				//		execute(meta, false);
				//	}
					output.open(this, gploadProcess);
				}
				output.writeLine(getInputRowMeta(), r);
			}
			putRow(getInputRowMeta(), r);
			incrementLinesOutput();
		
		}
		catch(KettleException e)
		{
			logError(BaseMessages.getString(PKG, "GPLoad.Log.ErrorInStep")+e.getMessage()); //$NON-NLS-1$
			setErrors(1);
			stopAll();
			setOutputDone();  // signal end to receiver(s)
			return false;
		} 

		return true;
	}

    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(GPLoadMeta)smi;
		data=(GPLoadData)sdi;
		
		Trans trans = getTrans();
		preview = trans.isPreview();
		
		if (super.init(smi, sdi))
		{			
			return true;
		}
		return false;
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
	    meta = (GPLoadMeta)smi;
	    data = (GPLoadData)sdi;

	    super.dispose(smi, sdi);
	    
	    if ( !preview && meta.isEraseFiles() )
	    {
	       // Erase the created cfg/dat files if requested. We don't erase
	       // the rest of the files because it would be "stupid" to erase them
	       // right after creation. If you don't want them, don't fill them in.
	       FileObject fileObject = null;
	       
	       String method = meta.getLoadMethod();
	       if (  // GPLoadMeta.METHOD_AUTO_CONCURRENT.equals(method) ||
	    		 GPLoadMeta.METHOD_AUTO_END.equals(method))
	       { 
	 	       if ( meta.getControlFile() != null )
		       {
			       try
			       {
		               fileObject = KettleVFS.getFileObject(environmentSubstitute(meta.getControlFile()), getTransMeta());			   
		               fileObject.delete();
		               fileObject.close();
	  	           }
		           catch ( Exception ex )
		           {
		               logError("Error deleting control file \'" + KettleVFS.getFilename(fileObject) + "\': " + ex.getMessage());
		           }
		       }
	      }

	       if (  GPLoadMeta.METHOD_AUTO_END.equals(method) )
	       {
	    	   // In concurrent mode the data is written to the control file.
	 	       if ( meta.getDataFile() != null )
		       {
			       try
			       {
		               fileObject = KettleVFS.getFileObject(environmentSubstitute(meta.getDataFile()), getTransMeta());			   
		               fileObject.delete();
		               fileObject.close();
	  	           }
		           catch ( Exception ex )
		           {
		               logError("Error deleting data file \'" + KettleVFS.getFilename(fileObject) + "\': " + ex.getMessage(), ex);
		           }
		       }
	       }	       
	       
	       if (  GPLoadMeta.METHOD_MANUAL.equals(method) )
	       {
	           logBasic("Deletion of files is not compatible with \'manual load method\'");	   
	       }
	    }
	}	
}