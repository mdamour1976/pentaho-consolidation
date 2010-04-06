package org.pentaho.di.core.logging;

import org.pentaho.di.core.Const;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.RepositoryDirectory;

public class LoggingObject implements LoggingObjectInterface {
	
	private String logChannelId;
	private LoggingObjectType objectType;
	private String objectName;
	private String objectCopy;
	private RepositoryDirectory repositoryDirectory;
	private String filename;
	private ObjectId objectId;
	private ObjectRevision objectRevision;
	private int logLevel = LogWriter.LOG_LEVEL_DEFAULT;
	
	private LoggingObjectInterface parent;
	
	public LoggingObject(Object object) {
		if (object instanceof LoggingObjectInterface) grabLoggingObjectInformation((LoggingObjectInterface)object);
		else grabObjectInformation(object);
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof LoggingObject)) return false;
		if (obj == this) return true;
		
		try {
			LoggingObject loggingObject = (LoggingObject) obj;
	
			// See if we recognize the repository ID, this is an absolute match
			//
			if (loggingObject.getObjectId()!=null && loggingObject.getObjectId().equals(getObjectId())) {
				return true;
			}
			
			// If the filename is the same, it's the same object...
			//
			if (!Const.isEmpty(loggingObject.getFilename()) && loggingObject.getFilename().equals(getFilename())) {
				return true;
			}
			
			// See if the name & type and parent name & type is the same.
			// This will catch most matches except for the most exceptional use-case.
			//
			if ( (loggingObject.getObjectName()==null && getObjectName()!=null ) || (loggingObject.getObjectName()!=null && getObjectName()==null )) {
				return false;
			}
			
			if ( ( (loggingObject.getObjectName()==null && getObjectName()==null ) || (loggingObject.getObjectName().equals(getObjectName()))) && loggingObject.getObjectType().equals(getObjectType())) {
				
				// If there are multiple copies of this object, they both need their own channel
				//
				if (!Const.isEmpty(getObjectCopy()) && !getObjectCopy().equals(loggingObject.getObjectCopy())) {
					return false;
				}
				
				LoggingObjectInterface parent1 = loggingObject.getParent();
				LoggingObjectInterface parent2 = getParent();
				
				if ((parent1!=null && parent2==null) || (parent1==null && parent2!=null)) return false;
				if (parent1==null && parent2==null) return true;
				
				// This goes to the parent recursively...
				//
				if (parent1.equals(parent2)) {
					return true;
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

		return false;
	}
	
	private void grabLoggingObjectInformation(LoggingObjectInterface loggingObject) {
		objectType = loggingObject.getObjectType();
		objectName = loggingObject.getObjectName();
		repositoryDirectory = loggingObject.getRepositoryDirectory();
		filename = loggingObject.getFilename();
		objectId = loggingObject.getObjectId();
		objectRevision = loggingObject.getObjectRevision();
		objectCopy = loggingObject.getObjectCopy();
		logLevel = loggingObject.getLogLevel();
		
		if (loggingObject.getParent()!=null) {
			getParentLoggingObject(loggingObject.getParent());
		}
	}

	private void grabObjectInformation(Object object) {
		objectType = LoggingObjectType.GENERAL;
		objectName = object.toString(); // name of class or name of object..
		
		parent = null;
	}

	private void getParentLoggingObject(Object parentObject) {
		
		if (parentObject==null) {
			return;
		}
		
		LoggingRegistry registry = LoggingRegistry.getInstance();
		
		// Extract the hierarchy information from the parentObject...
		//
		LoggingObject check = new LoggingObject(parentObject);
		LoggingObjectInterface loggingObject = registry.findExistingLoggingSource(check);
		if (loggingObject==null) {
			String logChannelId = registry.registerLoggingSource(check);
			loggingObject = check;
			check.setLogChannelId(logChannelId);
		}
		
		parent = loggingObject;
	}
	
	/**
	 * @return the name
	 */
	public String getObjectName() {
		return objectName;
	}
	/**
	 * @param name the name to set
	 */
	public void setObjectName(String name) {
		this.objectName = name;
	}
	/**
	 * @return the repositoryDirectory
	 */
	public RepositoryDirectory getRepositoryDirectory() {
		return repositoryDirectory;
	}
	/**
	 * @param repositoryDirectory the repositoryDirectory to set
	 */
	public void setRepositoryDirectory(RepositoryDirectory repositoryDirectory) {
		this.repositoryDirectory = repositoryDirectory;
	}
	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}
	/**
	 * @param filename the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}
	/**
	 * @return the objectId
	 */
	public ObjectId getObjectId() {
		return objectId;
	}
	/**
	 * @param objectId the objectId to set
	 */
	public void setObjectId(ObjectId objectId) {
		this.objectId = objectId;
	}

	/**
	 * @return the objectRevision
	 */
	public ObjectRevision getObjectRevision() {
		return objectRevision;
	}

	/**
	 * @param objectRevision the objectRevision to set
	 */
	public void setObjectRevision(ObjectRevision objectRevision) {
		this.objectRevision = objectRevision;
	}

	/**
	 * @return the id
	 */
	public String getLogChannelId() {
		return logChannelId;
	}

	/**
	 * @param id the id to set
	 */
	public void setLogChannelId(String logChannelId) {
		this.logChannelId = logChannelId;
	}

	/**
	 * @return the parent
	 */
	public LoggingObjectInterface getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(LoggingObjectInterface parent) {
		this.parent = parent;
	}

	/**
	 * @return the objectType
	 */
	public LoggingObjectType getObjectType() {
		return objectType;
	}

	/**
	 * @param objectType the objectType to set
	 */
	public void setObjectType(LoggingObjectType objectType) {
		this.objectType = objectType;
	}

	/**
	 * @return the copy
	 */
	public String getObjectCopy() {
		return objectCopy;
	}

	/**
	 * @param copy the copy to set
	 */
	public void setObjectCopy(String objectCopy) {
		this.objectCopy = objectCopy;
	}

  public int getLogLevel() {
    return logLevel;
  }

  public void setLogLevel(int logLevel) {
    this.logLevel = logLevel;
  }
}
