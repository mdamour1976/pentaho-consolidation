package org.pentaho.di.trans.steps.infobrightoutput;

import java.io.IOException;
import java.sql.SQLException;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Uses the native named pipe load capability of BrightHouse to load kettle-sourced data into
 * a BrightHouse table.
 */
public class InfobrightLoader extends BaseStep implements StepInterface {
  
  private final KettleRecordPopulator populator;
  
  private InfobrightLoaderMetadata meta;
  private InfobrightLoaderStepData data;

  /**
   * Standard constructor.  Does nothing special.
   * 
   * @param stepMeta
   * @param stepDataInterface
   * @param copyNr
   * @param transMeta
   * @param trans
   */
  public InfobrightLoader(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans) {
    super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    WindowsJNILibraryUtil.fixJavaLibraryPath(); // TODO move to Windows-specific class
    populator = new KettleRecordPopulator();
  }

  /** {@inheritDoc}
   * @see org.pentaho.di.trans.step.StepInterface#processRow(org.pentaho.di.trans.step.StepMetaInterface, org.pentaho.di.trans.step.StepDataInterface)
   */
  public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
    meta = (InfobrightLoaderMetadata) smi;
    data = (InfobrightLoaderStepData) sdi;

    Object[] row = getRow();
    
    // no more input to be expected...
    if (row == null) {
      setOutputDone();
      return false;
    }
    if (first) {
      first = false;
      data.outputRowMeta = getInputRowMeta().clone();
      meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
      data.insertRowMeta = getInputRowMeta().clone();
    }

    try {
      Object[] outputRowData = writeToLoader(row, data.insertRowMeta);
      if (outputRowData != null) {
        putRow(data.outputRowMeta, row); // in case we want it go further...
        incrementLinesOutput();
      }

      if (checkFeedback(getLinesRead())) {
        if(log.isBasic()) {
          logBasic("linenr " + getLinesRead());
        }
      }
    } catch(Exception e) {
      logError("Because of an error, this step can't continue: " + e.getMessage());
      e.printStackTrace();
      setErrors(1);
      stopAll();
      setOutputDone();  // signal end to receiver(s)
      return false;
    }
    return true;
  }

  /** {@inheritDoc}
   * @see org.pentaho.di.trans.step.BaseStep#init(org.pentaho.di.trans.step.StepMetaInterface, org.pentaho.di.trans.step.StepDataInterface)
   */
  public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
    boolean res = false;
    meta = (InfobrightLoaderMetadata) smi;
    data = (InfobrightLoaderStepData) sdi;
    
    if (super.init(smi, sdi)) {
      try {
        data.databaseSetup(meta, this);
        res = true;

      } catch (Exception ex) {
        logError("An error occurred intialising this step", ex);
        stopAll();
        setErrors(1);
      }
    }
    return res;
  }

  /**
   * Called by Kettle to start component once initialized.  All data processing is done inside
   * this loop. 
   */
  public void run() {
    try {
      logBasic(Messages.getString("BrightHouseLoader.Log.StartingToRun"));
      while (processRow(meta, data) && !isStopped())
        {}
    } catch (Exception e) {
      logError(Messages.getString("BrightHouseLoader.Log.UnexpectedError") + " : " + e.toString());
      logError(Const.getStackTracker(e));
      setErrors(1);
      stopAll();
    } finally {
      dispose(meta, data); // gtf: OutputStream gets closed here
      logSummary();
      markStop();
    }
  }

  /** {@inheritDoc}
   * @see org.pentaho.di.trans.step.BaseStep#stopRunning(org.pentaho.di.trans.step.StepMetaInterface, org.pentaho.di.trans.step.StepDataInterface)
   */
  @Override
  public void stopRunning(StepMetaInterface smi, StepDataInterface sdi) {
    if (data.loader != null) {
      logDebug("Trying to kill the loader statement...");
      try {
        data.loader.killQuery();
        logDebug("Loader statement killed.");
      } catch (SQLException sqle) {
        logError(Messages.getString("BrightHouseLoader.Log.FailedToKillQuery") + " : " + sqle.toString());
        logError(Const.getStackTracker(sqle));
      }
    }
  }
  
  /** {@inheritDoc}
   * @see org.pentaho.di.trans.step.BaseStep#dispose(org.pentaho.di.trans.step.StepMetaInterface, org.pentaho.di.trans.step.StepDataInterface)
   */
  @Override
  public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
    try {
      meta = (InfobrightLoaderMetadata) smi;
      data = (InfobrightLoaderStepData) sdi;
      if (data != null) {
        data.dispose(); // gtf: OutputStream gets closed here
      }
    } catch (Exception ioe) {
      logError("Unexpected error disposing of step data", ioe);      
    } finally {
      super.dispose(smi, sdi);      
    }
  }
  
  private Object[] writeToLoader(Object[] row, RowMetaInterface rowMeta) throws KettleException {

    Object[] outputRowData = row; // TODO set to null if there's an error
    try {
      populator.populate(data.record, row, rowMeta);
      data.record.writeTo(data.loader.getOutputStream());
      logRowlevel("loading: ..."); // does it make sense to have this for binary format?
    } catch (IOException ex) {
      throw new KettleException(ex);
    }
    return outputRowData;
  }
}
