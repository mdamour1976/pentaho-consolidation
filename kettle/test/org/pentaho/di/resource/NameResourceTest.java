/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.resource;

import static org.junit.Assert.*;

import org.apache.commons.vfs.FileObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.resource.ResourceNamingInterface.FileNamingType;
import org.pentaho.di.trans.TransMeta;

/**
 * @author Sean Flatley
 *
 *         For testing the ResourceNamingInterface.nameResource() methods.
 *
 */
public class NameResourceTest {

  @BeforeClass
  public static void setupBeforeClass() throws KettleException {
    if ( !KettleClientEnvironment.isInitialized() ) {
      KettleClientEnvironment.init();
    }
  }

  /**
   * Test case for Unix style file name.
   *
   * @throws Exception
   */
  @Test
  public void testUnixStyle() throws Exception {
    testNamingResourceLegacyAndNew( "/home/users/test/csv input/data", "csv", "" );
  }

  /**
   * Test case for a windows path with a mapped drive
   *
   * @throws Exception
   */
  @Test
  public void testWindowsDriveLetter() throws Exception {
    testNamingResourceLegacyAndNew( "z:\\program files\\data files\\data", "txt", "" );
  }

  /**
   * Test case for UNC
   *
   * @throws Exception
   */
  @Test
  public void testUNC() throws Exception {
    testNamingResourceLegacyAndNew( "\\\\devju1\\pentaho\\data files\\data", "mdb", "" );
  }

  /**
   * Test case for VFS
   *
   * @throws Exception
   */
  @Test
  public void testVFS() throws Exception {
    testNamingResourceLegacyAndNew( "file:///c:/home/user/database/humanResources", "mdb", "" );
  }

  /**
   * Test case for Unix style file name.
   *
   * @throws Exception
   */
  @Test
  public void testUnixStylePathOnly() throws Exception {
    testNamingResourceLegacyAndNew( "/home/users/test/csv input/data", "csv", ".+" );
  }

  /**
   * Test case for a windows path with a mapped drive
   *
   * @throws Exception
   */
  @Test
  public void testWindowsDriveLetterPathOnly() throws Exception {
    testNamingResourceLegacyAndNew( "z:\\program files\\data files\\data", "txt", ".+" );
  }

  /**
   * Test case for UNC
   *
   * @throws Exception
   */
  @Test
  public void testUNCPathOnly() throws Exception {
    testNamingResourceLegacyAndNew( "\\\\devju1\\pentaho\\data files\\data", "mdb", ".+" );
  }

  /**
   * Test case for VFS
   *
   * @throws Exception
   */
  @Test
  public void testVFSPathOnly() throws Exception {
    testNamingResourceLegacyAndNew( "file:///c:/home/user/database/humanResources", "mdb", ".+" );
  }

  /**
   * This tests ResourceNamingInterface.nameResouce(), comparing the directory maps generated by the legacy and new
   * method.
   *
   * @param fileName
   * @param pathOnly
   *          Resolve the path - leave out the file name
   * @throws Exception
   *
   *           Legacy: namingResource(String, String, String, FileNamingType) New: namingResource(FileObject, TransMeta)
   */
  private void testNamingResourceLegacyAndNew( String fileName, String extension, String fileMask )
    throws Exception {

    // Create a new transformation.
    TransMeta transMeta = new TransMeta();

    FileObject fileObject = KettleVFS.getFileObject( fileName, transMeta );

    // This code is modeled after the legacy code in legacy step meta classes
    // that have an exportResources method that deal with file masks
    // e.g., ExcelInputMeta
    //
    // There is a big exception: where the legacy code does a "getName()"
    // this code does a "getURL()". This is because of the JIRA case
    // that resulted in the refactoring of ResourceNamingInterface.
    //
    // The UNC and VFS protocols where being dropped.
    // The code you see here would be the fix for that without adding
    // the new nameResource() to ResourceNamingInterface.
    //

    String path = null;
    String prefix = null;
    if ( Const.isEmpty( fileMask ) ) {
      prefix = fileObject.getName().getBaseName();
      path = fileObject.getParent().getURL().toString();
    } else {
      prefix = "";
      path = fileObject.getURL().toString();
    }

    // Create a resource naming interface to use the legacy method call
    ResourceNamingInterface resourceNamingInterface_LEGACY = new SequenceResourceNaming();

    // Create two resource naming interfaces, one for legacy call, the other for new method call
    ResourceNamingInterface resourceNamingInterface_NEW = new SequenceResourceNaming();

    // The old and new interfaces to get the file name.
    String resolvedFileName_LEGACY =
      resourceNamingInterface_LEGACY.nameResource( prefix, path, extension, FileNamingType.DATA_FILE );
    String resolvedFileName_NEW =
      resourceNamingInterface_NEW.nameResource( fileObject, transMeta, Const.isEmpty( fileMask ) );

    // get the variable name from both naming interfaces directory maps
    String pathFromMap_LEGACY = resourceNamingInterface_LEGACY.getDirectoryMap().get( path );
    String pathFromMap_NEW = resourceNamingInterface_NEW.getDirectoryMap().get( path );

    // The paths in both directories should be the same
    assertEquals( pathFromMap_LEGACY, pathFromMap_NEW );

    // The file names should be the same
    assertEquals( resolvedFileName_LEGACY, resolvedFileName_NEW );
  }
}
