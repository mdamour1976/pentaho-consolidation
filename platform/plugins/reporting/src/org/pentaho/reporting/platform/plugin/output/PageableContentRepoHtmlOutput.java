package org.pentaho.reporting.platform.plugin.output;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.IContentLocation;
import org.pentaho.platform.api.repository.IContentRepository;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.UUIDUtil;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.layout.output.YieldReportListener;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.base.PageableReportProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.AllItemsHtmlPrinter;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.HtmlPrinter;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.PageableHtmlOutputProcessor;
import org.pentaho.reporting.libraries.base.util.StringUtils;
import org.pentaho.reporting.libraries.repository.ContentIOException;
import org.pentaho.reporting.libraries.repository.ContentLocation;
import org.pentaho.reporting.libraries.repository.DefaultNameGenerator;
import org.pentaho.reporting.libraries.repository.stream.StreamRepository;
import org.pentaho.reporting.platform.plugin.messages.Messages;
import org.pentaho.reporting.platform.plugin.repository.PentahoNameGenerator;
import org.pentaho.reporting.platform.plugin.repository.PentahoURLRewriter;
import org.pentaho.reporting.platform.plugin.repository.ReportContentRepository;

/**
 * Todo: Document me!
 * <p/>
 * Date: 02.02.11
 * Time: 17:46
 *
 * @author Thomas Morgner.
 */
public class PageableContentRepoHtmlOutput extends PageableHTMLOutput
{
  private String reportName;

  public PageableContentRepoHtmlOutput(final String contentHandlerPattern)
  {
    super(contentHandlerPattern);
  }

  protected PageableReportProcessor createReportProcessor(final MasterReport report,
                                                          final int yieldRate) throws ReportProcessingException
  {
    reportName = StringUtils.isEmpty(report.getName()) ? UUIDUtil.getUUIDAsString() : report.getName();
    setProxyOutputStream(new ProxyOutputStream());

    final PageableHtmlOutputProcessor outputProcessor = new PageableHtmlOutputProcessor(report.getConfiguration());
    final HtmlPrinter printer = new AllItemsHtmlPrinter(report.getResourceManager());
    printer.setUrlRewriter(new PentahoURLRewriter(getContentHandlerPattern(), true));
    outputProcessor.setPrinter(printer);

    final PageableReportProcessor proc = new PageableReportProcessor(report, outputProcessor);
    if (yieldRate > 0)
    {
      proc.addReportProgressListener(new YieldReportListener(yieldRate));
    }
    return super.createReportProcessor(report, yieldRate);
  }

  protected void reinitOutputTarget() throws ReportProcessingException, ContentIOException
  {
    final IPentahoSession session = PentahoSessionHolder.getSession();
    final String solutionPath = "report-content" + "/" + reportName + "/"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    final String thePath = solutionPath + session.getId() + "-" + System.currentTimeMillis();//$NON-NLS-1$//$NON-NLS-2$
    final IContentRepository contentRepository = PentahoSystem.get(IContentRepository.class, session);
    final IContentLocation pentahoContentLocation = contentRepository.newContentLocation(thePath, reportName, reportName, solutionPath, true);

    final ReportContentRepository repository = new ReportContentRepository(pentahoContentLocation, reportName);
    final ContentLocation dataLocation = repository.getRoot();
    final PentahoNameGenerator dataNameGenerator = PentahoSystem.get(PentahoNameGenerator.class);
    if (dataNameGenerator == null)
    {
      throw new IllegalStateException
          (Messages.getInstance().getString("ReportPlugin.errorNameGeneratorMissingConfiguration"));
    }
    dataNameGenerator.initialize(dataLocation, true);


    final StreamRepository targetRepository = new StreamRepository(null, getProxyOutputStream(), "report"); //$NON-NLS-1$
    final ContentLocation targetRoot = targetRepository.getRoot();

    final HtmlPrinter printer = getPrinter();
    printer.setContentWriter(targetRoot, new DefaultNameGenerator(targetRoot, "index", "html"));//$NON-NLS-1$//$NON-NLS-2$
    printer.setDataWriter(dataLocation, dataNameGenerator);
  }
}
