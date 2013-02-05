package org.pentaho.platform.plugin.services.importer;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;

/**
 * A struct-like object for bundling related objects together for import into the platform. Bundles contain all
 * information necessary for import into the system. While this interface includes a hash-map optional parameters
 * function, it should be subclassed if many properties are accessed this way.
 *
 * @author mlowery, nbaker, tband
 */
public interface IPlatformImportBundle {

  /**
   * This allows for arbitrary parent-child trees to be imported. Note this does not support the folder/file
   * paradigm and is instead a logical relationship between import bundles.
   *
   * @return a list of "child" bundles.
   */
  List<IPlatformImportBundle> getChildBundles();


  /**
   * Optional content name. Repository content this will be stored based on this name
   *
   * @return optional name
   */
  String getName();

  /**
   * Optional InputStream for content with a binary component.
   *
   * @return optional InputStream
   */
  InputStream getInputStream() throws IOException;

  /**
   * Optional character set for the binary InputStream. UTF-8 will be used by default for in the case of binary text
   * content
   *
   * @return Optional character set for the associated InputStream
   */
  String getCharset();

  /**
   * mime-type to be used to resolve an IPlatformImportHandler. If not set the IPlatformImporter will attempt to
   * resolve a mime-type based on the configured IPlatformImportMimeResolver
   *
   * @return mime-type
   */
  String getMimeType();
  
  /**
   * Convenience method for extra properties. A subclass would be preferred if there are a great number of properties
   * accessed from this method.
   *
   * @param prop
   * @return property Object
   */
  Object getProperty(String prop);
  
  /**
   * pass in flag to allow overwrite in repository (if exists)
   * @return boolean
   */
  boolean overwriteInRepossitory();
  
  /**
   * Ability to use the export manifest during import to apply ACL and File settings
   * @return
   */
  RepositoryFileAcl getAcl();

  void setAcl(RepositoryFileAcl acl);

  boolean isOverwriteAclSettings();

  /**
   * use the import manifest ACL settings and overwrite existing settings
   * @param overwriteAclSettings
   */
  void setOverwriteAclSettings(boolean overwriteAclSettings);

  boolean isRetainOwnership();

  /**
   * retain the file metadata ownership
   * @param retainOwnership
   */
  public abstract void setRetainOwnership(boolean retainOwnership);
  
  boolean isApplyAclSettings() ;

  /**
   * use the import manfiest file to apply ACL settings to files and folders
   * @param applyAclSettings
   */
  void setApplyAclSettings(boolean applyAclSettings) ;


}
