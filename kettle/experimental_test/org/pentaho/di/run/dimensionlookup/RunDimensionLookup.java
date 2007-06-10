package org.pentaho.di.run.dimensionlookup;

import junit.framework.TestCase;

import org.pentaho.di.run.AllRunTests;
import org.pentaho.di.run.TimedTransRunner;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;

public class RunDimensionLookup extends TestCase
{
    private static void createIndex() throws KettleException
    {
        AllRunTests.executeStatementsOnOldAndNew(
                "DROP TABLE DIM_CUSTOMER;",
                "CREATE TABLE DIM_CUSTOMER" + Const.CR + 
                "(" +  Const.CR + 
                "    customer_tk BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1)" +  Const.CR + 
                "    , version INTEGER" +  Const.CR + 
                "    , date_from TIMESTAMP" +  Const.CR + 
                "    , date_to TIMESTAMP" +  Const.CR + 
                "    , id INTEGER" +  Const.CR + 
                "    , name VARCHAR(30)" +  Const.CR + 
                "    , firstname VARCHAR(30)" +  Const.CR + 
                "    , zip INTEGER" +  Const.CR + 
                "    , city VARCHAR(30)" +  Const.CR + 
                "    , birthdate TIMESTAMP" +  Const.CR + 
                "    , street VARCHAR(11)" +  Const.CR + 
                "    , housenr INTEGER" +  Const.CR + 
                "    , stateCode VARCHAR(9)" +  Const.CR + 
                "    , state VARCHAR(30)" +  Const.CR + 
                "    )" +  Const.CR + 
                "    ;" + Const.CR +
                "    CREATE INDEX idx_DIM_CUSTOMER_lookup" +  Const.CR + 
                "     ON DIM_CUSTOMER" +  Const.CR + 
                "    ( " +  Const.CR + 
                "      id" +  Const.CR + 
                "    )" +  Const.CR + 
                "    ;" + Const.CR +
                "    CREATE INDEX idx_DIM_CUSTOMER_tk" +  Const.CR + 
                "     ON DIM_CUSTOMER" +  Const.CR + 
                "    ( " +  Const.CR + 
                "      customer_tk" +  Const.CR + 
                "    )" +  Const.CR + 
                "    ;"
            );
    }

    public void test__DIMENSION_LOOKUP_00() throws KettleException
    {
        System.out.println();
        System.out.println("DIMENSION LOOKUP");
        System.out.println("==================");
        System.out.println();
        createIndex();
    }
    
    public void test__DIMENSION_LOOKUP_01_InitialLoad() throws KettleException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/dimensionlookup/DimensionLookupInitialLoad.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                250000
            );
        timedTransRunner.runOldAndNew();
        
        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }

    public void test__DIMENSION_LOOKUP_02_Update20k() throws KettleException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/dimensionlookup/DimensionLookupUpdate20k.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                20000
            );
        timedTransRunner.runOldAndNew();
        
        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }

    public void test__DIMENSION_LOOKUP_03_TkLookupCacheOff() throws KettleXMLException, KettleDatabaseException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/dimensionlookup/DimensionLookupTKLookupCacheOff.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                250000
            );
        timedTransRunner.runOldAndNew();

        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    
    public void test__DIMENSION_LOOKUP_04_TkLookup() throws KettleXMLException, KettleDatabaseException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/dimensionlookup/DimensionLookupTKLookup.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                250000
            );
        timedTransRunner.runOldAndNew();

        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    
    public void test__DIMENSION_LOOKUP_05_TkLookupCache25k() throws KettleXMLException, KettleDatabaseException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/dimensionlookup/DimensionLookupTKLookupCache25k.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                250000
            );
        timedTransRunner.runOldAndNew();

        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }

    public void test__DIMENSION_LOOKUP_06_TkLookupCache50k() throws KettleXMLException, KettleDatabaseException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/dimensionlookup/DimensionLookupTKLookupCache50k.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                250000
            );
        timedTransRunner.runOldAndNew();

        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }

    public void test__DIMENSION_LOOKUP_07_TkLookupCacheAll() throws KettleXMLException, KettleDatabaseException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/dimensionlookup/DimensionLookupTKLookupCacheAll.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                250000
            );
        timedTransRunner.runOldAndNew();

        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    
    /*
    public void test__DIMENSION_LOOKUP_99() throws KettleDatabaseException
    {
        System.out.println();
        dropTable();
    }
    */
}
