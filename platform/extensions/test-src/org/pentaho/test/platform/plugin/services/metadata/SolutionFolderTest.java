package org.pentaho.test.platform.plugin.services.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.concept.types.LocaleType;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.platform.plugin.service.metadata.MetadataDomainRepositoryTestWrapper;
import org.pentaho.platform.repository.solution.filebased.FileBasedSolutionRepository;
import org.pentaho.test.platform.engine.core.MicroPlatform;

/**
 * Purpose:  Tests the import of localization files that 
 *           exist in the solution folders where metadata.xmi
 *           files may exist.
 *           
 *           If a metadata.xmi file exists i the solution folder
 *           then all files named metadata_"locale_code".properties
 *           will be loaded into the domain that was created by the 
 *           loading of the metadata.xmi file. 
 *    
 * For each test a file named "mymodel.xmi" is created in this
 * folder.  The individual tests may call the methods 
 * 
 *      MetadataDomainRepository.getLocaleFromPropertyfilename()
 *      MetadataDomainRepository.getLocalePropertyFilenames()
 *      
 * The determines which method is to be tested and the criteria of the test.
 * Each test method's javadoc contains the goal of the test.
 *  
 * @author sflatley
 */
public class SolutionFolderTest  {
   
   private String SOLUTION_PATH;
   
   //  Legacy solution path and file name
   private String BI_DEVELOPERS_FOLDER_NAME = "bi-developers";
   private String LEGACY_XMI_FILENAME = "metadata.xmi";
   private String XMI_FILENAME_EXTENSION = ".xmi";
   private String BI_DEVELOPERS_FULL_PATH;
   private File biDevelopersSolutionFolder;
   
   //  localeTestUtil
   LocaleTestUtil localeTestUtil = null;
   
   /**
    * Creates the files system that mimics a BI Server solution.
    * Also creates a LocaleTestUtility that is used to create the
    * files needed for the tests.
    * 
    * A MicroPlatform is also created as it is needed for filtering the
    * property files that may exist in the metadata folder.
    * 
    * @throws Exception
    */
   @Before
   public void init() {
      
      //  create solution paths
      SOLUTION_PATH = System.getProperty("java.io.tmpdir");
      BI_DEVELOPERS_FULL_PATH = SOLUTION_PATH + "/" + BI_DEVELOPERS_FOLDER_NAME;
      biDevelopersSolutionFolder = new File(BI_DEVELOPERS_FULL_PATH);
      if (!biDevelopersSolutionFolder.exists()) {
         biDevelopersSolutionFolder.delete();
         biDevelopersSolutionFolder.mkdir();
      }
      
      //  utility to make this testing a bit easier
      localeTestUtil = new LocaleTestUtil();
      
      //  create a platform
      MicroPlatform mp = new MicroPlatform(SOLUTION_PATH);
      try {
         mp.define(ISolutionRepository.class, FileBasedSolutionRepository.class, Scope.GLOBAL);
         mp.start();
      }
      catch (PlatformInitializationException pie) {
         pie.printStackTrace();
      }
   }
   
//   @Test
//   public void testLocaleParseNoPath() {
//      
//      String filename = "mymodel_en_US.properties";
//      String prefix = "mymodel";
//      
//      MetadataDomainRepositoryTestWrapper metadataDomainRepository = new MetadataDomainRepositoryTestWrapper();
//      try {
//         String locale = metadataDomainRepository.getLocaleFromPropertyFilename(filename, prefix);
//         assertEquals("en_US", locale);
//      }
//      catch (DomainStorageException dse) {
//         fail(dse.getMessage());
//      }
//   }
   
//   @Test
//   public void testLocaleParseWithPath() {
//      
//      String filename = "/resources/metadata/mymodel_en_US.properties";
//      String prefix = "mymodel";
//      
//      MetadataDomainRepositoryTestWrapper metadataDomainRepository = new MetadataDomainRepositoryTestWrapper();
//      try {
//         String locale = metadataDomainRepository.getLocaleFromPropertyFilename(filename, prefix);
//         assertEquals("en_US", locale);
//      }
//      catch (DomainStorageException dse) {
//         fail(dse.getMessage());
//      }
//   }
   
   /**
    * Tests MetadataDomainRepository.getLocalePropertyFilenames()
    * where one xmi resource and no property file exists in the metadata folder.
    */
//   @Test
//   public void testNoLocaleFileDiscovery() {
//      
//      //  create the metadata.xmi file.
//      File metadataXmiFile=null;
//      MetadataDomainRepositoryTestWrapper metadataDomainRepository = new MetadataDomainRepositoryTestWrapper();
//      try {
//         metadataXmiFile = localeTestUtil.createFile(BI_DEVELOPERS_FULL_PATH, XMI_FILENAME_EXTENSION);
//         ISolutionFile[] localizationFiles = metadataDomainRepository.getLocalePropertyFiles(BI_DEVELOPERS_FOLDER_NAME+"/" + XMI_FILENAME_EXTENSION);
//         assertEquals(0, localizationFiles.length);
//      }
//      catch (IOException ioe) {
//         fail(ioe.getMessage());
//      }
//      catch (DomainStorageException dse) {
//         dse.printStackTrace();
//         fail(dse.getMessage());
//      }
//      finally {
//         if (metadataXmiFile != null) {
//            metadataXmiFile.delete();
//         }
//      }
//   }

   /**
    * Tests MetadataDomainRepository.getLocalePropertyFilenames()
    * when one xmi resource is in the meta data folder with one
    * property file.
    */
//   @Test
//   public void testOneLocaleFileDiscovery() {
//
//      //  create files
//      File en_us_properties = null;
//      File xmiMeta_xml = null;
//         
//      //  discover the localization files
//      MetadataDomainRepositoryTestWrapper metadataDomainRepository = new MetadataDomainRepositoryTestWrapper();
//      try {
//         
//         //  create files
//         xmiMeta_xml = localeTestUtil.createFile(BI_DEVELOPERS_FULL_PATH, LEGACY_XMI_FILENAME);
//         en_us_properties = localeTestUtil.createPropertiesFile("EN_US", BI_DEVELOPERS_FULL_PATH, LEGACY_XMI_FILENAME.substring(0, LEGACY_XMI_FILENAME.indexOf('.')));
//         
//         //  get a list of locale property files
//         ISolutionFile[] localizationFileNames = metadataDomainRepository.getLocalePropertyFiles(BI_DEVELOPERS_FOLDER_NAME+"/" + LEGACY_XMI_FILENAME);
//         
//         // we expect a list of one file - the one we just created
//         assertNotNull(localizationFileNames);
//         assertEquals(localizationFileNames.length , 1);
//         assertEquals(localizationFileNames[0].getFileName(), en_us_properties.getName());
//                  
//        // metadataDomainRepository.importLocalizations("metadata", BI_DEVELOPERS_FOLDER_NAME+"/" + MetadataDomainRepository.LEGACY_XMI_FILENAME);
//         
//      }
//      catch (IOException ioe) {
//         fail(ioe.getMessage());
//      }
//      catch (DomainStorageException dse) {
//         fail(dse.getMessage());
//      }
//      finally {
//         if (en_us_properties != null) { en_us_properties.delete(); }
//         if (xmiMeta_xml !=null) { xmiMeta_xml.delete(); }
//      }
//   }
   
   /**
    *  Tests MetadataDomainRepository.getLocalePropertyFilenames()
    *  when one xmi file and several property file exists 
    *  in the metadata folder.
    */
//   @Test
//   public void testMultiLocaleFileDiscovery() {
//
//      // create files
//      File xmiMeta_xml = null;
//      File en_us_properties = null;
//      File en_gb_properties = null;
//      File no_bok_properties = null;
//
//      //  discover the localization files
//      MetadataDomainRepositoryTestWrapper metadataDomainRepository = new MetadataDomainRepositoryTestWrapper();
//      try {
//                
//         // create files
//         xmiMeta_xml = localeTestUtil.createFile(BI_DEVELOPERS_FULL_PATH, LEGACY_XMI_FILENAME);
//         en_us_properties = localeTestUtil.createPropertiesFile("EN_US", BI_DEVELOPERS_FULL_PATH, LEGACY_XMI_FILENAME.substring(0, LEGACY_XMI_FILENAME.indexOf('.')));
//         en_gb_properties = localeTestUtil.createPropertiesFile("EN_GB", BI_DEVELOPERS_FULL_PATH, LEGACY_XMI_FILENAME.substring(0, LEGACY_XMI_FILENAME.indexOf('.')));
//         no_bok_properties = localeTestUtil.createPropertiesFile("NO_BOK", BI_DEVELOPERS_FULL_PATH, LEGACY_XMI_FILENAME.substring(0, LEGACY_XMI_FILENAME.indexOf('.')));
//
//         //  get the list of property files to import  
//         ISolutionFile[] localizationFiles = metadataDomainRepository.getLocalePropertyFiles(BI_DEVELOPERS_FOLDER_NAME + "/" + LEGACY_XMI_FILENAME);
//         
//         //  test the localization filenames for correctness
//         ArrayList<String> solutionFileNames = new ArrayList<String>();
//         for(ISolutionFile solutionFile: localizationFiles) {
//            solutionFileNames.add(solutionFile.getFileName());
//         }        
//         assertNotNull(localizationFiles);
//         assertEquals(3, localizationFiles.length);
//         assertTrue(solutionFileNames.contains(en_us_properties.getName()));
//         assertTrue(solutionFileNames.contains(en_gb_properties.getName()));
//         assertTrue(solutionFileNames.contains(no_bok_properties.getName()));         
//      }
//      catch (IOException ioe) {
//         fail(ioe.getMessage());
//      }
//      catch (DomainStorageException dse) {
//         fail(dse.getMessage());
//      }
//      finally {
//         if(en_us_properties != null) { en_us_properties.delete(); }
//         if(en_gb_properties != null) { en_gb_properties.delete(); }
//         if(no_bok_properties != null) { no_bok_properties.delete(); }
//         xmiMeta_xml.delete();
//      }
//   }
   
   @After
   public void cleanup() {
      if (biDevelopersSolutionFolder != null) {
         biDevelopersSolutionFolder.delete();
      }
   }
   
//   @Test
//   public void testImportLocalizationEmptyPropertyFile() {
//      //  create files
//      File en_us_properties = null;
//      File xmiMeta_xml = null;
//      Domain domain = new Domain();
//            
//      //  discover the localization files
//      MetadataDomainRepositoryTestWrapper metadataDomainRepository = new MetadataDomainRepositoryTestWrapper();
//      try {
//            
//         //  create files
//         xmiMeta_xml = localeTestUtil.createFile(BI_DEVELOPERS_FULL_PATH, LEGACY_XMI_FILENAME);
//         en_us_properties = localeTestUtil.createPropertiesFile("EN_US", BI_DEVELOPERS_FULL_PATH, LEGACY_XMI_FILENAME.substring(0, LEGACY_XMI_FILENAME.indexOf('.')));
//           
//         //  get a list of locale property files
//         ISolutionFile[] localizationFileNames = metadataDomainRepository.getLocalePropertyFiles(BI_DEVELOPERS_FOLDER_NAME+"/" + LEGACY_XMI_FILENAME);
//            
//         // we expect a list of one file - the one we just created
//         assertNotNull(localizationFileNames);
//         assertEquals(localizationFileNames.length , 1);
//         assertEquals(localizationFileNames[0].getFileName(), en_us_properties.getName());
//                     
//         metadataDomainRepository.importLocalizations(domain, BI_DEVELOPERS_FOLDER_NAME+"/" + LEGACY_XMI_FILENAME);
//            
//         }
//         catch (IOException ioe) {
//            fail(ioe.getMessage());
//         }
//         catch (DomainStorageException dse) {
//            assertTrue(dse.getMessage().contains("The property file metadata_EN_US.properties contains no properties"));
//         }
//         finally {
//            if (en_us_properties != null) { en_us_properties.delete(); }
//            if (xmiMeta_xml !=null) { xmiMeta_xml.delete(); }
//         }
//      }
   
//   @Test
//   public void testImportPropertyFile() {
//      //  create files
//      File en_us_properties = null;
//      File xmiMeta_xml = null;
//      Domain domain = new Domain();
//      LocaleType localeType = new LocaleType();
//
//            
//      //  discover the localization files
//      MetadataDomainRepositoryTestWrapper metadataDomainRepository = new MetadataDomainRepositoryTestWrapper();
//      try {
//            
//         //  create files
//         xmiMeta_xml = localeTestUtil.createFile(BI_DEVELOPERS_FULL_PATH, LEGACY_XMI_FILENAME);
//         en_us_properties = localeTestUtil.createPropertiesFile("EN_US", BI_DEVELOPERS_FULL_PATH, LEGACY_XMI_FILENAME.substring(0, LEGACY_XMI_FILENAME.indexOf('.')), true);
//           
//         //  get a list of locale property files
//         ISolutionFile[] localizationFileNames = metadataDomainRepository.getLocalePropertyFiles(BI_DEVELOPERS_FOLDER_NAME+"/" + LEGACY_XMI_FILENAME);
//            
//         // we expect a list of one file - the one we just created
//         assertNotNull(localizationFileNames);
//         assertEquals(localizationFileNames.length , 1);
//         assertEquals(localizationFileNames[0].getFileName(), en_us_properties.getName());
//               
//         localeType.setCode("en_US");
//         ArrayList<LocaleType> localeTypes = new ArrayList<LocaleType>();
//         localeTypes.add(localeType);
//         domain.setLocales(localeTypes);
//         
//         //  missing something in domain setup that is causing a null pointer exception.
//         
//         metadataDomainRepository.importLocalizations(domain, BI_DEVELOPERS_FOLDER_NAME+"/" + LEGACY_XMI_FILENAME);
//            
//         }
//         catch (IOException ioe) {
//            fail(ioe.getMessage());
//         }
//         catch (DomainStorageException dse) { 
//            fail(dse.getMessage());
//         }
//         finally {
//            if (en_us_properties != null) { en_us_properties.delete(); }
//            if (xmiMeta_xml !=null) { xmiMeta_xml.delete(); }
//         }
//      }   
}
