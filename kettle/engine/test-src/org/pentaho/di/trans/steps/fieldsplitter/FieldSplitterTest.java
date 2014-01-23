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

package org.pentaho.di.trans.steps.fieldsplitter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.QueueRowSet;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.metastore.api.IMetaStore;

/**
 * Tests for FieldSplitter step
 *
 * @author Pavel Sakun
 * @see FieldSplitter
 */
public class FieldSplitterTest {
  StepMockHelper<FieldSplitterMeta, FieldSplitterData> smh;

  @Before
  public void setUp() {
    smh =
        new StepMockHelper<FieldSplitterMeta, FieldSplitterData>( "Field Splitter", FieldSplitterMeta.class,
            FieldSplitterData.class );
    when( smh.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        smh.logChannelInterface );
    when( smh.trans.isRunning() ).thenReturn( true );
  }

  private RowSet mockInputRowSet() {
    return smh.getMockInputRowSet( new Object[][] { { "before", "b=b;c=c", "after" } } );
  }

  private FieldSplitterMeta mockProcessRowMeta() throws KettleStepException {
    FieldSplitterMeta processRowMeta = smh.processRowsStepMetaInterface;
    doReturn( "field to split" ).when( processRowMeta ).getSplitField();
    doCallRealMethod().when( processRowMeta ).getFields( any( RowMetaInterface.class ), anyString(),
        any( RowMetaInterface[].class ), any( StepMeta.class ), any( VariableSpace.class ), any( Repository.class ),
        any( IMetaStore.class ) );
    doReturn( new String[] { "a", "b" } ).when( processRowMeta ).getFieldName();
    doReturn( new int[] { ValueMetaInterface.TYPE_STRING, ValueMetaInterface.TYPE_STRING } ).when( processRowMeta )
        .getFieldType();
    doReturn( new String[] { "a=", "b=" } ).when( processRowMeta ).getFieldID();
    doReturn( new boolean[] { false, false } ).when( processRowMeta ).getFieldRemoveID();
    doReturn( new int[] { -1, -1 } ).when( processRowMeta ).getFieldLength();
    doReturn( new int[] { -1, -1 } ).when( processRowMeta ).getFieldPrecision();
    doReturn( new int[] { 0, 0 } ).when( processRowMeta ).getFieldTrimType();
    doReturn( new String[] { null, null } ).when( processRowMeta ).getFieldFormat();
    doReturn( new String[] { null, null } ).when( processRowMeta ).getFieldDecimal();
    doReturn( new String[] { null, null } ).when( processRowMeta ).getFieldGroup();
    doReturn( new String[] { null, null } ).when( processRowMeta ).getFieldCurrency();
    doReturn( new String[] { null, null } ).when( processRowMeta ).getFieldNullIf();
    doReturn( new String[] { null, null } ).when( processRowMeta ).getFieldIfNull();
    doReturn( ";" ).when( processRowMeta ).getDelimiter();

    return processRowMeta;
  }

  private RowMeta getInputRowMeta() {
    RowMeta inputRowMeta = new RowMeta();
    inputRowMeta.addValueMeta( new ValueMetaString( "before" ) );
    inputRowMeta.addValueMeta( new ValueMetaString( "field to split" ) );
    inputRowMeta.addValueMeta( new ValueMetaString( "after" ) );

    return inputRowMeta;
  }

  @Test
  public void testSplitFields() throws KettleException {
    KettleEnvironment.init();

    FieldSplitter step = new FieldSplitter( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans );
    step.init( smh.initStepMetaInterface, smh.stepDataInterface );
    step.setInputRowMeta( getInputRowMeta() );
    step.getInputRowSets().add( mockInputRowSet() );
    step.getOutputRowSets().add( new QueueRowSet() );

    boolean hasMoreRows;
    do {
      hasMoreRows = step.processRow( mockProcessRowMeta(), smh.processRowsStepDataInterface );
    } while ( hasMoreRows );

    RowSet outputRowSet = step.getOutputRowSets().get( 0 );
    Object[] actualRow = outputRowSet.getRow();
    Object[] expectedRow = new Object[] { "before", null, "b=b", "after" };

    Assert.assertEquals( "Output row is of an unexpected length", expectedRow.length, outputRowSet.getRowMeta().size() );

    for ( int i = 0; i < expectedRow.length; i++ ) {
      Assert.assertEquals( "Unexpected output value at index " + i, expectedRow[i], actualRow[i] );
    }
  }
}
