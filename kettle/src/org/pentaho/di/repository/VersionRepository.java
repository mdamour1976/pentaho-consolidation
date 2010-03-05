package org.pentaho.di.repository;

import org.pentaho.di.core.exception.KettleException;

public interface VersionRepository extends IRepositoryService{
  
  /**
   * Restore a job from the given revision. The state of the specified revision becomes
   * the current / latest state of the job.
   * @param id_job id of the job
   * @param revision revision to restore
   * @throws KettleException
   */
  public void restoreJob(ObjectId id_job, String revision, String versionComment) throws KettleException;
  
  /**
   * Restore a transformation from the given revision. The state of the specified revision becomes
   * the current / latest state of the transformation.
   * @param id_transformation id of the transformation
   * @param revision revision to restore
   * @throws KettleException
   */
  public void restoreTransformation(ObjectId id_transformation, String revision, String versionComment) throws KettleException;
}
