package org.pentaho.reporting.platform.plugin;

import junit.framework.TestCase;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory;
import org.pentaho.platform.api.engine.IPluginProvider;
import org.pentaho.platform.api.engine.IServiceManager;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.plugin.services.pluginmgr.SystemPathXmlPluginProvider;
import org.pentaho.platform.plugin.services.pluginmgr.servicemgr.DefaultServiceManager;
import org.pentaho.platform.repository.solution.filebased.FileBasedSolutionRepository;
import org.pentaho.reporting.platform.plugin.repository.PentahoNameGenerator;
import org.pentaho.reporting.platform.plugin.repository.TempDirectoryNameGenerator;
import org.pentaho.test.platform.engine.core.MicroPlatform;

/**
 * Todo: Document me!
 * <p/>
 * Date: 28.09.2010
 * Time: 13:31:28
 *
 * @author Thomas Morgner.
 */
public class ParameterTest extends TestCase
{
  private MicroPlatform microPlatform;

  public ParameterTest()
  {

  }

  @Override
  protected void setUp() throws Exception {
    microPlatform = new MicroPlatform("tests/integration-tests/resource/");
    microPlatform.define(ISolutionEngine.class, SolutionEngine.class);
    microPlatform.define(ISolutionRepository.class, FileBasedSolutionRepository.class);
    microPlatform.define(IPluginProvider.class, SystemPathXmlPluginProvider.class);
    microPlatform.define(IServiceManager.class, DefaultServiceManager.class, IPentahoDefinableObjectFactory.Scope.GLOBAL);
    microPlatform.define(PentahoNameGenerator.class, TempDirectoryNameGenerator.class, IPentahoDefinableObjectFactory.Scope.GLOBAL);

    microPlatform.start();
  }

  @Override
  protected void tearDown() throws Exception {
    microPlatform.stop();
  }

  public void testParameterProcessing() throws Exception
  {
    final ReportContentGenerator contentGenerator = new ReportContentGenerator();
    final ParameterXmlContentHandler handler = new ParameterXmlContentHandler(contentGenerator, false);
    handler.createParameterContent(System.out, "solution/test/reporting/Product Sales.prpt");
  }

}
