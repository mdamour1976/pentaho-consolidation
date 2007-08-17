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
 
package org.pentaho.di.job.entry;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryType;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceReference;
import org.w3c.dom.Node;




/**
 * Interface for the different JobEntry classes.
 * 
 * @author Matt
 * @since 18-06-04
 * 
 */

public interface JobEntryInterface
{
	
	public Result execute(Result prev_result, int nr, Repository rep, Job parentJob) throws KettleException;
	
	public void    clear();
	public long    getID();
	public void    setID(long id);
	public String  getName();
	public void    setName(String name);
	public String getConfigId();
	public void setConfigId(String configId);
	
	public String  getDescription();
	public void    setDescription(String description);
	public void    setChanged();
	public void    setChanged(boolean ch);
	public boolean hasChanged();

	public void    loadXML(Node entrynode, List<DatabaseMeta> databases, Repository rep) throws KettleXMLException;
	public String  getXML();
	public void    loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases) throws KettleException;
	public void    saveRep(Repository rep, long id_job) throws KettleException;
	
	public JobEntryType     getJobEntryType();
	public void     setJobEntryType(JobEntryType e);
	
	public String  getTypeCode();
    public String  getPluginID();


	public boolean isStart();
	public boolean isDummy();
	public Object  clone();
	
	public boolean resetErrorsBeforeExecution();
	public boolean evaluates();
	public boolean isUnconditional();
	
	public boolean isEvaluation();
	public boolean isTransformation();
	public boolean isJob();
	public boolean isShell();
	public boolean isMail();
	public boolean isSpecial();
    
    public List<SQLStatement> getSQLStatements(Repository repository) throws KettleException;
    public List<SQLStatement> getSQLStatements(Repository repository, VariableSpace space) throws KettleException;
    
    /**
     * Get the name of the class that implements the dialog for this job entry
     * JobEntryBase provides a default
     */
    public String getDialogClassName();
    
    public String getFilename();
    public String getRealFilename();
    
    /**
     * This method returns all the database connections that are used by the job entry.
     * @return an array of database connections meta-data.
     *         Return an empty array if no connections are used.
     */
    public DatabaseMeta[] getUsedDatabaseConnections();

    public void setPluginID(String id);
    
    /**
     * Allows JobEntry objects to check themselves for consistency
     * @param remarks List of CheckResult objects indicating check status
     * @param jobMeta JobMeta
     */
    public void check(List<CheckResultInterface> remarks, JobMeta jobMeta);
    
    /**
     * Get a list of all the resource dependencies that the step is depending on.
     * 
     * @return a list of all the resource dependencies that the step is depending on
     */
    public List<ResourceReference> getResourceDependencies(JobMeta jobMeta);

	/**
	 * Exports the object to a flat-file system, adding content with filename keys to a set of definitions.
	 * The supplied resource naming interface allows the object to name appropriately without worrying about those parts of the implementation specific details.
	 *  
	 * @param space The variable space to resolve (environment) variables with.
	 * @param definitions The map containing the filenames and content
	 * @param resourceNamingInterface The resource naming interface allows the object to name appropriately
	 * @return The filename for this object. (also contained in the definitions map)
	 * @throws KettleException in case something goes wrong during the export
	 */
	public String exportResources(VariableSpace space, Map<String, ResourceDefinition> definitions, ResourceNamingInterface namingInterface) throws KettleException;

}
