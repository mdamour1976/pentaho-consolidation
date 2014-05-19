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

package org.pentaho.di.trans.steps.singlethreader;

import java.util.ArrayList;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.SingleThreadedTransExecutor;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransMeta.TransformationType;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.TransStepUtil;
import org.pentaho.di.trans.steps.mapping.MappingValueRename;
import org.pentaho.di.trans.steps.mappinginput.MappingInputData;

/**
 * Execute a mapping: a re-usuable transformation
 *
 * @author Matt
 * @since 22-nov-2005
 */
public class SingleThreader extends BaseStep implements StepInterface {
  private static Class<?> PKG = SingleThreaderMeta.class; // for i18n purposes, needed by Translator2!!

  private SingleThreaderMeta meta;
  private SingleThreaderData data;

  public SingleThreader( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  /**
   * Process rows in batches of N rows. The sub-transformation will execute in a single thread.
   */
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (SingleThreaderMeta) smi;
    setData( (SingleThreaderData) sdi );

    Object[] row = getRow();
    if ( row == null ) {
      if ( getData().batchCount > 0 ) {
        getData().batchCount = 0;
        return execOneIteration();
      }

      setOutputDone();
      return false;
    }

    if ( first ) {
      first = false;

      getData().startTime = System.currentTimeMillis();
    }

    // Add the row to the producer...
    //
    getData().rowProducer.putRow( getInputRowMeta(), row );
    getData().batchCount++;

    if ( getStepMeta().isDoingErrorHandling() ) {
      getData().errorBuffer.add( row );
    }

    boolean countWindow = getData().batchSize > 0 && getData().batchCount >= getData().batchSize;
    boolean timeWindow = getData().batchTime > 0 && ( System.currentTimeMillis() - getData().startTime ) > getData().batchTime;

    if ( countWindow || timeWindow ) {
      getData().batchCount = 0;

      boolean more = execOneIteration();
      if ( !more ) {
        setOutputDone();
        return false;
      }
      getData().startTime = System.currentTimeMillis();
    }
    return true;
  }

  private boolean execOneIteration() {
    boolean more = false;
    try {
      more = getData().executor.oneIteration();
      if ( getData().executor.isStopped() || getData().executor.getErrors() > 0 ) {
        return handleError();
      }
    } catch ( Exception e ) {
      setErrors( 1L );
      stopAll();
      logError( BaseMessages.getString( PKG, "SingleThreader.Log.ErrorOccurredInSubTransformation" ) );
      return false;
    } finally {
      if ( getStepMeta().isDoingErrorHandling() ) {
        getData().errorBuffer.clear();
      }
    }
    return more;
  }

  private boolean handleError() throws KettleStepException {
    if ( getStepMeta().isDoingErrorHandling() ) {
      int lastLogLine = KettleLogStore.getLastBufferLineNr();
      StringBuffer logText =
        KettleLogStore.getAppender().getBuffer( getData().mappingTrans.getLogChannelId(), false, getData().lastLogLine );
      getData().lastLogLine = lastLogLine;

      for ( Object[] row : getData().errorBuffer ) {
        putError( getInputRowMeta(), row, 1L, logText.toString(), null, "STR-001" );
      }

      getData().executor.clearError();

      return true; // continue
    } else {
      setErrors( 1 );
      stopAll();
      logError( BaseMessages.getString( PKG, "SingleThreader.Log.ErrorOccurredInSubTransformation" ) );
      return false; // stop running
    }
  }

  private void passParameters() throws KettleException {

    String[] parameters;
    String[] parameterValues;

    if ( meta.isPassingAllParameters() ) {
      // We pass the values for all the parameters from the parent transformation
      //
      parameters = getData().mappingTransMeta.listParameters();
      parameterValues = new String[parameters.length];
      for ( int i = 0; i < parameters.length; i++ ) {
        parameterValues[i] = getVariable( parameters[i] );
      }
    } else {
      // We pass down the listed variables with the specified values...
      //
      parameters = meta.getParameters();
      parameterValues = new String[parameters.length];
      for ( int i = 0; i < parameters.length; i++ ) {
        parameterValues[i] = environmentSubstitute( meta.getParameterValues()[i] );
      }
    }

    for ( int i = 0; i < parameters.length; i++ ) {
      String value = Const.NVL( parameterValues[i], "" );

      getData().mappingTrans.setParameterValue( parameters[i], value );
    }

    getData().mappingTrans.activateParameters();
  }

  public void prepareMappingExecution() throws KettleException {
    // Set the type to single threaded in case the user forgot...
    //
    getData().mappingTransMeta.setTransformationType( TransformationType.SingleThreaded );

    // Create the transformation from meta-data...
    //
    getData().mappingTrans = new Trans( getData().mappingTransMeta, getTrans() );

    // Pass the parameters down to the sub-transformation.
    //
    passParameters();

    // Disable thread priority managment as it will slow things down needlessly.
    // The single threaded engine doesn't use threads and doesn't need row locking.
    //
    getData().mappingTrans.getTransMeta().setUsingThreadPriorityManagment( false );

    // Leave a path up so that we can set variables in sub-transformations...
    //
    getData().mappingTrans.setParentTrans( getTrans() );

    // Pass down the safe mode flag to the mapping...
    //
    getData().mappingTrans.setSafeModeEnabled( getTrans().isSafeModeEnabled() );

    // Pass down the metrics gathering flag to the mapping...
    //
    getData().mappingTrans.setGatheringMetrics( getTrans().isGatheringMetrics() );

    // Also set the name of this step in the mapping transformation for logging purposes
    //
    getData().mappingTrans.setMappingStepName( getStepname() );

    initServletConfig();

    // prepare the execution
    //
    getData().mappingTrans.prepareExecution( null );

    // If the inject step is a mapping input step, tell it all is OK...
    //
    if ( getData().injectStepMeta.isMappingInput() ) {
      MappingInputData mappingInputData =
        (MappingInputData) getData().mappingTrans.findDataInterface( getData().injectStepMeta.getName() );
      mappingInputData.sourceSteps = new StepInterface[0];
      mappingInputData.valueRenames = new ArrayList<MappingValueRename>();
    }

    // Add row producer & row listener
    //
    getData().rowProducer = getData().mappingTrans.addRowProducer( meta.getInjectStep(), 0 );

    StepInterface retrieveStep = getData().mappingTrans.getStepInterface( meta.getRetrieveStep(), 0 );
    retrieveStep.addRowListener( new RowAdapter() {
      @Override
      public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
        // Simply pass it along to the next steps after the SingleThreader
        //
        SingleThreader.this.putRow( rowMeta, row );
      }
    } );

    getData().mappingTrans.startThreads();

    // Create the executor...
    //
    getData().executor = new SingleThreadedTransExecutor( getData().mappingTrans );

    // We launch the transformation in the processRow when the first row is received.
    // This will allow the correct variables to be passed.
    // Otherwise the parent is the init() thread which will be gone once the init is done.
    //
    try {
      boolean ok = getData().executor.init();
      if ( !ok ) {
        throw new KettleException( BaseMessages.getString(
          PKG, "SingleThreader.Exception.UnableToInitSingleThreadedTransformation" ) );
      }
    } catch ( KettleException e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "SingleThreader.Exception.UnableToPrepareExecutionOfMapping" ), e );
    }

    // Add the mapping transformation to the active sub-transformations map in the parent transformation
    //
    getTrans().getActiveSubtransformations().put( getStepname(), getData().mappingTrans );
  }

  void initServletConfig() {
    TransStepUtil.initServletConfig( getTrans(), getData().getMappingTrans() );
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (SingleThreaderMeta) smi;
    setData( (SingleThreaderData) sdi );

    if ( super.init( smi, sdi ) ) {
      // First we need to load the mapping (transformation)
      try {
        // The batch size...
        //
        getData().batchSize = Const.toInt( environmentSubstitute( meta.getBatchSize() ), 0 );
        getData().batchTime = Const.toInt( environmentSubstitute( meta.getBatchTime() ), 0 );

        // Pass the repository down to the metadata object...
        //
        meta.setRepository( getTransMeta().getRepository() );

        getData().mappingTransMeta = SingleThreaderMeta.loadSingleThreadedTransMeta( meta, meta.getRepository(), this );
        if ( getData().mappingTransMeta != null ) { // Do we have a mapping at all?

          // Validate the inject and retrieve step names
          //
          String injectStepName = environmentSubstitute( meta.getInjectStep() );
          getData().injectStepMeta = getData().mappingTransMeta.findStep( injectStepName );
          if ( getData().injectStepMeta == null ) {
            logError( "The inject step with name '"
              + injectStepName + "' couldn't be found in the sub-transformation" );
          }

          String retrieveStepName = environmentSubstitute( meta.getRetrieveStep() );
          if ( !Const.isEmpty( retrieveStepName ) ) {
            getData().retrieveStepMeta = getData().mappingTransMeta.findStep( retrieveStepName );
            if ( getData().retrieveStepMeta == null ) {
              logError( "The retrieve step with name '"
                + retrieveStepName + "' couldn't be found in the sub-transformation" );
            }
          }

          // OK, now prepare the execution of the mapping.
          // This includes the allocation of RowSet buffers, the creation of the
          // sub-transformation threads, etc.
          //
          prepareMappingExecution();

          if ( getStepMeta().isDoingErrorHandling() ) {
            getData().errorBuffer = new ArrayList<Object[]>();
          }

          // That's all for now...
          //
          return true;
        } else {
          logError( "No valid mapping was specified!" );
          return false;
        }
      } catch ( Exception e ) {
        logError( "Unable to load the mapping transformation because of an error : " + e.toString() );
        logError( Const.getStackTracker( e ) );
      }

    }
    return false;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    // dispose of the single threading execution engine
    //
    try {
      getData().executor.dispose();
    } catch ( KettleException e ) {
      log.logError( "Error disposing of sub-transformation: ", e );
    }

    super.dispose( smi, sdi );
  }

  public void stopRunning( StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface ) throws KettleException {
    if ( getData().mappingTrans != null ) {
      getData().mappingTrans.stopAll();
    }
  }

  public void stopAll() {
    // Stop the mapping step.
    if ( getData().mappingTrans != null ) {
      getData().mappingTrans.stopAll();
    }

    // Also stop this step
    super.stopAll();
  }

  public Trans getMappingTrans() {
    return getData().mappingTrans;
  }

  // Method is defined as package-protected in order to be accessible by unit tests
  SingleThreaderData getData() {
    return data;
  }

  private void setData( SingleThreaderData data ) {
    this.data = data;
  }
}
