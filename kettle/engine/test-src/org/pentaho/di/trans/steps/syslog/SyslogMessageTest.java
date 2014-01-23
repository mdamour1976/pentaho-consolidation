package org.pentaho.di.trans.steps.syslog;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.productivity.java.syslog4j.SyslogConfigIF;
import org.productivity.java.syslog4j.SyslogIF;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * User: Dzmitry Stsiapanau Date: 1/23/14 Time: 11:04 AM
 */
public class SyslogMessageTest {

  private StepMockHelper<SyslogMessageMeta, SyslogMessageData> stepMockHelper;

  @Before
  public void setUp() throws Exception {
    stepMockHelper =
        new StepMockHelper<SyslogMessageMeta, SyslogMessageData>( "SYSLOG_MESSAGE TEST", SyslogMessageMeta.class,
            SyslogMessageData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        stepMockHelper.logChannelInterface );

  }

  @Test
  public void testDispose() throws Exception {
    SyslogMessageData data = new SyslogMessageData();
    SyslogIF syslog = mock( SyslogIF.class );
    SyslogConfigIF syslogConfigIF = mock( SyslogConfigIF.class, RETURNS_MOCKS );
    when( syslog.getConfig() ).thenReturn( syslogConfigIF );
    final Boolean[] initialized = new Boolean[] { Boolean.FALSE };
    doAnswer( new Answer<Object>() {
      public Object answer( InvocationOnMock invocation ) {
        initialized[0] = true;
        return initialized;
      }
    } ).when( syslog ).initialize( anyString(), (SyslogConfigIF) anyObject() );
    doAnswer( new Answer<Object>() {
      public Object answer( InvocationOnMock invocation ) {
        if ( !initialized[0] ) {
          throw new NullPointerException( "this.socket is null" );
        } else {
          initialized[0] = false;
        }
        return initialized;
      }
    } ).when( syslog ).shutdown();
    SyslogMessageMeta meta = new SyslogMessageMeta();
    SyslogMessage syslogMessage =
        new SyslogMessage( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
            stepMockHelper.trans );
    SyslogMessage sysLogMessageSpy = spy( syslogMessage );
    when( sysLogMessageSpy.getSyslog() ).thenReturn( syslog );
    meta.setServerName( "1" );
    meta.setMessageFieldName( "1" );
    sysLogMessageSpy.init( meta, data );
    sysLogMessageSpy.dispose( meta, data );
  }
}
