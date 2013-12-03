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

package org.pentaho.di.trans.steps.switchcase;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.QueueRowSet;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SwitchCaseTest {

  private StepMockHelper<SwitchCaseMeta, SwitchCaseData> mockHelper;

  @Before
  public void setUp() throws Exception {
    mockHelper =
        new StepMockHelper<SwitchCaseMeta, SwitchCaseData>( "Switch Case", SwitchCaseMeta.class, SwitchCaseData.class );
    when( mockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        mockHelper.logChannelInterface );
    when( mockHelper.trans.isRunning() ).thenReturn( true );
  }

  @After
  public void tearDown() throws Exception {
    mockHelper.cleanUp();
  }

  /**
   * PDI 6900. Test that process row works correctly. 
   * Simulate step workload when input and output row sets already 
   * created and mapped to specified case values.
   * 
   * @throws KettleException
   */
  @Test
  public void testProcessRow() throws KettleException {
    SwitchCaseCustom krasavez = new SwitchCaseCustom( mockHelper );
    krasavez.first = false;

    // create two output row sets
    RowSet rowSetOne = new QueueRowSet();
    RowSet rowSetTwo = new QueueRowSet();

    // this row set should contain only '3'.
    krasavez.data.outputMap.put( 3, rowSetOne );
    krasavez.data.outputMap.put( 3, rowSetTwo );

    // this row set contains nulls only
    RowSet rowSetNullOne = new QueueRowSet();
    RowSet rowSetNullTwo = new QueueRowSet();
    krasavez.data.nullRowSetSet.add( rowSetNullOne );
    krasavez.data.nullRowSetSet.add( rowSetNullTwo );

    // this row set contains all expect null or '3'
    RowSet def = new QueueRowSet();
    krasavez.data.defaultRowSetSet.add( def );

    // generate some data (see method implementation)
    // expected: 5 times null,
    // expected 1*2 = 2 times 3
    // expected 5*2 + 5 = 15 rows generated
    // expected 15 - 5 - 2 = 8 rows go to default.
    // System.out.println( krasavez.getInputDataOverview() );
    // 1, 1, null, 2, 2, null, 3, 3, null, 4, 4, null, 5, 5, null
    krasavez.generateData( 1, 5, 2 );

    // call method under test
    krasavez.processRow();

    Assert.assertEquals( "First row set collects 2 rows", 2, rowSetOne.size() );
    Assert.assertEquals( "Second row set collects 2 rows", 2, rowSetTwo.size() );

    Assert.assertEquals( "First null row set collects 5 rowa", 5, rowSetNullOne.size() );
    Assert.assertEquals( "Second null row set collects 5 rowa", 5, rowSetNullTwo.size() );

    Assert.assertEquals( "Default row set collects the rest of rows", 8, def.size() );

    // now - check the data is correct in every row set:
    Assert.assertEquals( "First row set contains only 3: ", true, isRowSetContainsValue( rowSetOne, new Object[] { 3 },
        new Object[] {} ) );
    Assert.assertEquals( "Second row set contains only 3: ", true, isRowSetContainsValue( rowSetTwo,
        new Object[] { 3 }, new Object[] {} ) );

    Assert.assertEquals( "First null row set contains only null: ", true, isRowSetContainsValue( rowSetNullOne,
        new Object[] { null }, new Object[] {} ) );
    Assert.assertEquals( "Second null row set contains only null: ", true, isRowSetContainsValue( rowSetNullTwo,
        new Object[] { null }, new Object[] {} ) );

    Assert.assertEquals( "Default row set do not contains null or 3, but other", true, isRowSetContainsValue( def,
        new Object[] { 1, 2, 4, 5 }, new Object[] { 3, null } ) );
  }

  private boolean isRowSetContainsValue( RowSet rowSet, Object[] allowed, Object[] illegal ) {
    boolean ok = true;

    Set<Object> yes = new HashSet<Object>();
    yes.addAll( Arrays.asList( allowed ) );
    Set<Object> no = new HashSet<Object>();
    no.addAll( Arrays.asList( illegal ) );

    for ( int i = 0; i < rowSet.size(); i++ ) {
      Object[] row = rowSet.getRow();
      Object val = row[0];
      ok = yes.contains( val ) && !no.contains( val );
      if ( !ok ) {
        // this is not ok now
        return false;
      }
    }
    return ok;
  }

  /**
   * PDI-6900 Check that SwichCase step can correctly set up input values to output rowsets.
   * 
   * @throws KettleException
   * @throws URISyntaxException
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   */
  @Test
  public void testCreateOutputValueMapping() throws KettleException, URISyntaxException, ParserConfigurationException,
    SAXException, IOException {
    SwitchCaseCustom krasavez = new SwitchCaseCustom( mockHelper );

    //load step info value-case mapping from xml.  
    krasavez.meta.loadXML( loadStepXmlMetadata(), Collections.<DatabaseMeta> emptyList(), mock( IMetaStore.class ) );

    KeyToRowSetMap expectedNN = new KeyToRowSetMap();
    Set<RowSet> nulls = new HashSet<RowSet>();

    // create real steps for all targets
    List<SwitchCaseTarget> list = krasavez.meta.getCaseTargets();
    for ( SwitchCaseTarget item : list ) {
      StepMetaInterface smInt = new DummyTransMeta();
      StepMeta stepMeta = new StepMeta( item.caseTargetStepname, smInt );
      item.caseTargetStep = stepMeta;

      // create and put row set for this
      RowSet rw = new QueueRowSet();
      krasavez.map.put( item.caseTargetStepname, rw );

      // null values goes to null rowset
      if ( item.caseValue != null ) {
        expectedNN.put( item.caseValue, rw );
      } else {
        nulls.add( rw );
      }
    }

    // create default step
    StepMetaInterface smInt = new DummyTransMeta();
    StepMeta stepMeta = new StepMeta( krasavez.meta.getDefaultTargetStepname(), smInt );
    krasavez.meta.setDefaultTargetStep( stepMeta );
    RowSet rw = new QueueRowSet();
    krasavez.map.put( krasavez.meta.getDefaultTargetStepname(), rw );

    krasavez.createOutputValueMapping();

    // inspect step output data:
    Set<RowSet> ones = krasavez.data.outputMap.get( "1" );
    Assert.assertEquals( "Output map for 1 values contains 2 row sets", 2, ones.size() );

    Set<RowSet> twos = krasavez.data.outputMap.get( "2" );
    Assert.assertEquals( "Output map for 2 values contains 1 row sets", 1, twos.size() );

    Assert.assertEquals( "Null row set contains 2 items: ", 2, krasavez.data.nullRowSetSet.size() );
    Assert.assertEquals( "We have at least one default rowset", 1, krasavez.data.defaultRowSetSet.size() );

    // check that rowsets data is correct:
    Set<RowSet> rowsets = expectedNN.get( "1" );
    for ( RowSet rowset : rowsets ) {
      Assert.assertTrue( "Output map for 1 values contains expected row set", ones.contains( rowset ) );
    }
    rowsets = expectedNN.get( "2" );
    for ( RowSet rowset : rowsets ) {
      Assert.assertTrue( "Output map for 2 values contains expected row set", twos.contains( rowset ) );
    }
    for ( RowSet rowset : krasavez.data.nullRowSetSet ) {
      Assert.assertTrue( "Output map for null values contains expected row set", nulls.contains( rowset ) );
    }
    // we have already check that there is only one item.
    for ( RowSet rowset : krasavez.data.defaultRowSetSet ) {
      Assert.assertTrue( "Output map for default case contains expected row set", rowset.equals( rw ) );
    }
  }

  /**
   * Load local xml data for case-value mapping, step info. 
   * @return
   * @throws URISyntaxException
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   */
  Node loadStepXmlMetadata() throws URISyntaxException, ParserConfigurationException, SAXException, IOException {
    String PKG = SwitchCaseTest.class.getPackage().getName().replace( ".", "/" );
    PKG = PKG + "/";
    URL url = SwitchCaseTest.class.getClassLoader().getResource( PKG + "SwitchCaseTest.xml" );
    File file = new File( url.toURI() );
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    Document doc = dBuilder.parse( file );
    NodeList nList = doc.getElementsByTagName( "step" );
    Node stepnode = nList.item( 0 );
    return stepnode;
  }

  /**
   * Switch case step ancestor with overridden methods to have ability to simulate normal transformation execution.
   * 
   */
  private class SwitchCaseCustom extends SwitchCase {

    Queue<Object[]> input = new LinkedList<Object[]>();
    RowMetaInterface rowMetaInterface;

    // we will use real data and meta.
    SwitchCaseData data = new SwitchCaseData();
    SwitchCaseMeta meta = new SwitchCaseMeta();

    Map<String, RowSet> map = new HashMap<String, RowSet>();

    SwitchCaseCustom( StepMockHelper<SwitchCaseMeta, SwitchCaseData> mockHelper ) throws KettleValueException {
      super( mockHelper.stepMeta, mockHelper.stepDataInterface, 0, mockHelper.transMeta, mockHelper.trans );
      // this.mockHelper = mockHelper;
      init( meta, data );

      // call to convert value will returns same value.
      data.valueMeta = mock( ValueMetaInterface.class );
      when( data.valueMeta.convertData( any( ValueMetaInterface.class ), any() ) ).thenAnswer( new Answer<Object>() {
        @Override
        public Object answer( InvocationOnMock invocation ) throws Throwable {
          Object[] objArr = invocation.getArguments();
          return ( objArr != null && objArr.length > 1 ) ? objArr[1] : null;
        }
      } );
      // same when call to convertDataFromString
      when(
          data.valueMeta.convertDataFromString( Mockito.anyString(), any( ValueMetaInterface.class ), Mockito
              .anyString(), Mockito.anyString(), Mockito.anyInt() ) ).thenAnswer( new Answer<Object>() {
        @Override
        public Object answer( InvocationOnMock invocation ) throws Throwable {
          Object[] objArr = invocation.getArguments();
          return ( objArr != null && objArr.length > 1 ) ? objArr[0] : null;
        }
      } );
      // null-check
      when( data.valueMeta.isNull( any() ) ).thenAnswer( new Answer<Object>() {
        @Override
        public Object answer( InvocationOnMock invocation ) throws Throwable {
          Object[] objArr = invocation.getArguments();
          Object obj = objArr[0];
          return obj == null;
        }
      } );

    }

    /**
     * used for input row generation
     * 
     * @param start
     * @param finish
     * @param copy
     */
    void generateData( int start, int finish, int copy ) {
      input.clear();
      for ( int i = start; i <= finish; i++ ) {
        for ( int j = 0; j < copy; j++ ) {
          input.add( new Object[] { i } );
        }
        input.add( new Object[] { null } );
      }
    }

    /**
     * useful to see generated data as String
     * 
     * @return
     */
    @SuppressWarnings( "unused" )
    public String getInputDataOverview() {
      StringBuilder sb = new StringBuilder();
      for ( Object[] row : input ) {
        sb.append( row[0] + ", " );
      }
      return sb.toString();
    }

    /**
     * mock step data processing
     */
    @Override
    public Object[] getRow() throws KettleException {
      return input.poll();
    }

    /**
     * simulate concurrent execution
     * 
     * @throws KettleException
     */
    public void processRow() throws KettleException {
      boolean run = false;
      do {
        run = processRow( meta, data );
      } while ( run );
    }

    @Override
    public RowSet findOutputRowSet( String targetStep ) throws KettleStepException {
      return map.get( targetStep );
    }

    @Override
    public RowMetaInterface getInputRowMeta() {
      if ( rowMetaInterface == null ) {
        rowMetaInterface = getDynamicRowMetaInterface();
      }
      return rowMetaInterface;
    }

    private RowMetaInterface getDynamicRowMetaInterface() {
      RowMetaInterface inputRowMeta = mock( RowMetaInterface.class );
      return inputRowMeta;
    }
  }
}
