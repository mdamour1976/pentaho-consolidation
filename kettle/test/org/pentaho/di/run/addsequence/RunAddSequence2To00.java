package org.pentaho.di.run.addsequence;

import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.run.AllRunTests;
import org.pentaho.di.run.RunTestCase;
import org.pentaho.di.run.TimedTransRunner;

public class RunAddSequence2To00 extends RunTestCase
{
    public void test_ADD_SEQUENCE_04_Add2ToEmptyRow() throws Exception
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "test/org/pentaho/di/run/addsequence/Add2Sequences.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                rowCount*10000
        );
        assertTrue( timedTransRunner.runOldAndNew() );
        
        be.ibridge.kettle.core.Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }

}
