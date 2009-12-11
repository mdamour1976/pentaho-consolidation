package org.pentaho.reporting.platform.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.text.DateFormatSymbols;
import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.databinding.types.xsd.Decimal;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IAcceptsRuntimeInputs;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IStreamingPojo;
import org.pentaho.platform.api.repository.IContentRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.jfreereport.helper.PentahoTableModel;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.reporting.engine.classic.core.AttributeNames;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.metadata.ReportProcessTaskRegistry;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.pdf.PdfPageableModule;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.plaintext.PlainTextPageableModule;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.plaintext.driver.TextFilePrinterDriver;
import org.pentaho.reporting.engine.classic.core.modules.output.table.csv.CSVTableModule;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.HtmlTableModule;
import org.pentaho.reporting.engine.classic.core.modules.output.table.rtf.RTFTableModule;
import org.pentaho.reporting.engine.classic.core.modules.output.table.xls.ExcelTableModule;
import org.pentaho.reporting.engine.classic.core.modules.parser.base.ReportGenerator;
import org.pentaho.reporting.engine.classic.core.parameters.DefaultParameterContext;
import org.pentaho.reporting.engine.classic.core.parameters.ListParameter;
import org.pentaho.reporting.engine.classic.core.parameters.ParameterContext;
import org.pentaho.reporting.engine.classic.core.parameters.ParameterDefinitionEntry;
import org.pentaho.reporting.engine.classic.core.parameters.ValidationResult;
import org.pentaho.reporting.engine.classic.core.parameters.ValidationMessage;
import org.pentaho.reporting.engine.classic.core.parameters.ParameterAttributeNames;
import org.pentaho.reporting.engine.classic.core.util.beans.BeanException;
import org.pentaho.reporting.engine.classic.core.util.beans.ConverterRegistry;
import org.pentaho.reporting.engine.classic.core.util.beans.ValueConverter;
import org.pentaho.reporting.engine.classic.core.util.ReportParameterValues;
import org.pentaho.reporting.engine.classic.extensions.modules.java14print.Java14PrintUtil;
import org.pentaho.reporting.libraries.base.config.Configuration;
import org.pentaho.reporting.libraries.base.util.StringUtils;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;
import org.pentaho.reporting.platform.plugin.messages.Messages;
import org.pentaho.reporting.platform.plugin.output.CSVOutput;
import org.pentaho.reporting.platform.plugin.output.HTMLOutput;
import org.pentaho.reporting.platform.plugin.output.PDFOutput;
import org.pentaho.reporting.platform.plugin.output.PageableHTMLOutput;
import org.pentaho.reporting.platform.plugin.output.RTFOutput;
import org.pentaho.reporting.platform.plugin.output.XLSOutput;
import org.xml.sax.InputSource;

public class SimpleReportingComponent implements IStreamingPojo, IAcceptsRuntimeInputs
{

  /**
   * The logging for logging messages from this component
   */
  private static final Log log = LogFactory.getLog(SimpleReportingComponent.class);

  public static final String OUTPUT_TARGET = "output-target"; //$NON-NLS-1$

  public static final String OUTPUT_TYPE = "output-type"; //$NON-NLS-1$
  public static final String MIME_TYPE_HTML = "text/html"; //$NON-NLS-1$
  public static final String MIME_TYPE_EMAIL = "mime-message/text/html"; //$NON-NLS-1$
  public static final String MIME_TYPE_PDF = "application/pdf"; //$NON-NLS-1$
  public static final String MIME_TYPE_XLS = "application/vnd.ms-excel"; //$NON-NLS-1$
  public static final String MIME_TYPE_RTF = "application/rtf"; //$NON-NLS-1$
  public static final String MIME_TYPE_CSV = "text/csv"; //$NON-NLS-1$
  public static final String MIME_TYPE_TXT = "text/plain"; //$NON-NLS-1$

  public static final String XLS_WORKBOOK_PARAM = "workbook"; //$NON-NLS-1$

  public static final String REPORTLOAD_RESURL = "res-url"; //$NON-NLS-1$
  public static final String REPORT_DEFINITION_INPUT = "report-definition"; //$NON-NLS-1$
  public static final String USE_CONTENT_REPOSITORY = "useContentRepository"; //$NON-NLS-1$
  public static final String REPORTHTML_CONTENTHANDLER_PATTERN = "content-handler-pattern"; //$NON-NLS-1$
  public static final String REPORTGENERATE_YIELDRATE = "yield-rate"; //$NON-NLS-1$
  public static final String ACCEPTED_PAGE = "accepted-page"; //$NON-NLS-1$
  public static final String PAGINATE_OUTPUT = "paginate"; //$NON-NLS-1$
  public static final String PRINT = "print"; //$NON-NLS-1$
  public static final String PRINTER_NAME = "printer-name"; //$NON-NLS-1$
  public static final String DASHBOARD_MODE = "dashboard-mode"; //$NON-NLS-1$
  private static final String MIME_GENERIC_FALLBACK = "application/octet-stream"; //$NON-NLS-1$

  /**
   * Static initializer block to guarantee that the ReportingComponent will be in a state where the reporting engine will be booted. We have a system listener
   * which will boot the reporting engine as well, but we do not want to solely rely on users having this setup correctly. The errors you receive if the engine
   * is not booted are not very helpful, especially to outsiders, so we are trying to provide multiple paths to success. Enjoy.
   */
  static
  {
    final ReportingSystemStartupListener startup = new ReportingSystemStartupListener();
    startup.startup(null);
  }

  /**
   * The output-type for the generated report, such as PDF, XLS, CSV, HTML, etc This must be the mime-type!
   */
  private String outputType;
  private String outputTarget;
  private MasterReport report;
  private Map<String, Object> inputs;
  private OutputStream outputStream;
  private InputStream reportDefinitionInputStream;
  private Boolean useContentRepository = Boolean.FALSE;
  private IActionSequenceResource reportDefinition;
  private String reportDefinitionPath;
  private IPentahoSession session;
  private boolean paginateOutput = false;
  private int acceptedPage = -1;
  private int pageCount = -1;
  private boolean dashboardMode;
  /*
   * These fields are for enabling printing
   */
  private boolean print = false;
  private String printer;

  /*
   * Default constructor
   */
  public SimpleReportingComponent()
  {
    this.inputs = Collections.emptyMap();
  }

  // ----------------------------------------------------------------------------
  // BEGIN BEAN METHODS
  // ----------------------------------------------------------------------------

  public String getOutputTarget()
  {
    return outputTarget;
  }

  public void setOutputTarget(final String outputTarget)
  {
    this.outputTarget = outputTarget;
  }

  /**
   * Sets the mime-type for determining which report output type to generate. This should be a mime-type for consistency with streaming output mime-types.
   *
   * @param outputType the desired output type (mime-type) for the report engine to generate
   */
  public void setOutputType(final String outputType)
  {
    this.outputType = outputType;
  }

  /**
   * Gets the output type, this should be a mime-type for consistency with streaming output mime-types.
   *
   * @return the current output type for the report
   */
  public String getOutputType()
  {
    return outputType;
  }

  /**
   * This method returns the resource for the report-definition, if available.
   *
   * @return the report-definition resource
   */
  public IActionSequenceResource getReportDefinition()
  {
    return reportDefinition;
  }

  /**
   * Sets the report-definition if it is provided to us by way of an action-sequence resource. The name must be reportDefinition or report-definition.
   *
   * @param reportDefinition a report-definition as seen (wrapped) by an action-sequence
   */
  public void setReportDefinition(final IActionSequenceResource reportDefinition)
  {
    this.reportDefinition = reportDefinition;
  }

  /**
   * This method will be called if an input is called reportDefinitionInputStream, or any variant of that with dashes report-definition-inputstream for example.
   * The primary purpose of this method is to facilitate unit testing.
   *
   * @param reportDefinitionInputStream any kind of InputStream which contains a valid report-definition
   */
  public void setReportDefinitionInputStream(final InputStream reportDefinitionInputStream)
  {
    this.reportDefinitionInputStream = reportDefinitionInputStream;
  }

  /**
   * Returns the path to the report definition (for platform use this is a path in the solution repository)
   *
   * @return reportdefinitionPath
   */
  public String getReportDefinitionPath()
  {
    return reportDefinitionPath;
  }

  /**
   * Sets the path to the report definition (platform path)
   *
   * @param reportDefinitionPath the path to the report definition.
   */
  public void setReportDefinitionPath(final String reportDefinitionPath)
  {
    this.reportDefinitionPath = reportDefinitionPath;
  }

  /**
   * Returns true if the report engine will be asked to use a paginated (HTML) output processor
   *
   * @return paginated
   */
  public boolean isPaginateOutput()
  {
    return paginateOutput;
  }

  /**
   * Set the paging mode used by the reporting engine. This will also be set if an input
   *
   * @param paginateOutput page mode
   */
  public void setPaginateOutput(final boolean paginateOutput)
  {
    this.paginateOutput = paginateOutput;
  }

  public int getAcceptedPage()
  {
    return acceptedPage;
  }

  public void setAcceptedPage(final int acceptedPage)
  {
    this.acceptedPage = acceptedPage;
  }

  /**
   * This method sets the IPentahoSession to use in order to access the pentaho platform file repository and content repository.
   *
   * @param session a valid pentaho session
   */
  public void setSession(final IPentahoSession session)
  {
    this.session = session;
  }

  public boolean isDashboardMode()
  {
    return dashboardMode;
  }

  public void setDashboardMode(final boolean dashboardMode)
  {
    this.dashboardMode = dashboardMode;
  }

  /**
   * This method returns the mime-type for the streaming output based on the effective output target.
   *
   * @return the mime-type for the streaming output
   * @see SimpleReportingComponent#computeEffectiveOutputTarget()
   */
  public String getMimeType()
  {
    try
    {
      final String outputTarget = computeEffectiveOutputTarget();
      if (log.isDebugEnabled())
      {
        log.debug(Messages.getString("ReportPlugin.logComputedOutputTarget", outputTarget));
      }
      if (HtmlTableModule.TABLE_HTML_STREAM_EXPORT_TYPE.equals(outputTarget))
      {
        return SimpleReportingComponent.MIME_TYPE_HTML;
      }
      if (HtmlTableModule.TABLE_HTML_PAGE_EXPORT_TYPE.equals(outputTarget))
      {
        return SimpleReportingComponent.MIME_TYPE_HTML;
      }
      if (ExcelTableModule.EXCEL_FLOW_EXPORT_TYPE.equals(outputTarget))
      {
        return SimpleReportingComponent.MIME_TYPE_XLS;
      }
      if (CSVTableModule.TABLE_CSV_STREAM_EXPORT_TYPE.equals(outputTarget))
      {
        return SimpleReportingComponent.MIME_TYPE_CSV;
      }
      if (RTFTableModule.TABLE_RTF_FLOW_EXPORT_TYPE.equals(outputTarget))
      {
        return SimpleReportingComponent.MIME_TYPE_RTF;
      }
      if (PdfPageableModule.PDF_EXPORT_TYPE.equals(outputTarget))
      {
        return SimpleReportingComponent.MIME_TYPE_PDF;
      }
      if (PlainTextPageableModule.PLAINTEXT_EXPORT_TYPE.equals(outputTarget))
      {
        return SimpleReportingComponent.MIME_TYPE_TXT;
      }
      if (SimpleReportingComponent.MIME_TYPE_EMAIL.equals(outputTarget))
      {
        return SimpleReportingComponent.MIME_TYPE_EMAIL;
      }
    }
    catch (IOException e)
    {
      if (log.isDebugEnabled())
      {
        log.warn(Messages.getString("ReportPlugin.logErrorMimeTypeFull"), e);
      }
      else if (log.isWarnEnabled())
      {
        log.warn(Messages.getString("ReportPlugin.logErrorMimeTypeShort",  e.getMessage()));
      }
    }
    catch (ResourceException e)
    {
      if (log.isDebugEnabled())
      {
        log.warn(Messages.getString("ReportPlugin.logErrorMimeTypeFull"), e);
      }
      else if (log.isWarnEnabled())
      {
        log.warn(Messages.getString("ReportPlugin.logErrorMimeTypeShort",  e.getMessage()));
      }
    }
    return MIME_GENERIC_FALLBACK;
  }

  /**
   * This method sets the OutputStream to write streaming content on.
   *
   * @param outputStream an OutputStream to write to
   */
  public void setOutputStream(final OutputStream outputStream)
  {
    this.outputStream = outputStream;
  }

  public void setUseContentRepository(final Boolean useContentRepository)
  {
    this.useContentRepository = useContentRepository;
  }

  /**
   * This method checks if the output is targeting a printer
   *
   * @return true if the output is supposed to go to a printer
   */
  public boolean isPrint()
  {
    return print;
  }

  /**
   * Set whether or not to send the report to a printer
   *
   * @param print a flag indicating whether the report should be printed.
   */
  public void setPrint(final boolean print)
  {
    this.print = print;
  }

  /**
   * This method gets the name of the printer the report will be sent to
   *
   * @return the name of the printer that the report will be sent to
   */
  public String getPrinter()
  {
    return printer;
  }

  /**
   * Set the name of the printer to send the report to
   *
   * @param printer the name of the printer that the report will be sent to, a null value will be interpreted as the default printer
   */
  public void setPrinter(final String printer)
  {
    this.printer = printer;
  }


  /**
   * This method sets the map of *all* the inputs which are available to this component. This allows us to use action-sequence inputs as parameters for our
   * reports.
   *
   * @param inputs a Map containing inputs
   */
  public void setInputs(final Map<String, Object> inputs)
  {
    if (inputs == null)
    {
      this.inputs = Collections.emptyMap();
      return;
    }

    this.inputs = inputs;
    if (inputs.containsKey(OUTPUT_TYPE))
    {
      setOutputType(String.valueOf(inputs.get(OUTPUT_TYPE)));
    }
    if (inputs.containsKey(OUTPUT_TARGET))
    {
      setOutputTarget(String.valueOf(inputs.get(OUTPUT_TARGET)));
    }
    if (inputs.containsKey(REPORT_DEFINITION_INPUT))
    {
      setReportDefinitionInputStream((InputStream) inputs.get(REPORT_DEFINITION_INPUT));
    }
    if (inputs.containsKey(USE_CONTENT_REPOSITORY))
    {
      setUseContentRepository((Boolean) inputs.get(USE_CONTENT_REPOSITORY));
    }
    if (inputs.containsKey(PAGINATE_OUTPUT))
    {
      paginateOutput = "true".equals(String.valueOf(inputs.get(PAGINATE_OUTPUT))); //$NON-NLS-1$
      if (paginateOutput && inputs.containsKey(ACCEPTED_PAGE))
      {
        acceptedPage = Integer.parseInt(String.valueOf(inputs.get(ACCEPTED_PAGE))); //$NON-NLS-1$
      }
    }
    if (inputs.containsKey(PRINT))
    {
      print = "true".equals(String.valueOf(inputs.get(PRINT))); //$NON-NLS-1$
    }
    if (inputs.containsKey(PRINTER_NAME))
    {
      printer = String.valueOf(inputs.get(PRINTER_NAME));
    }
    if (inputs.containsKey(DASHBOARD_MODE))
    {
      dashboardMode = "true".equals(String.valueOf(inputs.get(DASHBOARD_MODE))); //$NON-NLS-1$
    }
  }

  // ----------------------------------------------------------------------------
  // END BEAN METHODS
  // ----------------------------------------------------------------------------

  protected Object getInput(final String key, final Object defaultValue)
  {
    if (inputs != null)
    {
      final Object input = inputs.get(key);
      if (input != null)
      {
        return input;
      }
    }
    return defaultValue;
  }

  /**
   * Get the MasterReport for the report-definition, the MasterReport object will be cached as needed, using the PentahoResourceLoader.
   *
   * @return a parsed MasterReport object
   * @throws ResourceException
   * @throws IOException
   */
  public MasterReport getReport() throws ResourceException, IOException
  {
    if (report == null)
    {
      if (reportDefinitionInputStream != null)
      {
        final ReportGenerator generator = ReportGenerator.createInstance();
        final InputSource repDefInputSource = new InputSource(reportDefinitionInputStream);
        report = generator.parseReport(repDefInputSource, getDefinedResourceURL(null));
      }
      else if (reportDefinition != null)
      {
        // load the report definition as an action-sequence resource
        report = ReportCreator.createReport(reportDefinition.getAddress(), session);
      }
      else
      {
        report = ReportCreator.createReport(reportDefinitionPath, session);
      }
      report.setReportEnvironment(new PentahoReportEnvironment(report.getConfiguration()));
    }

    return report;
  }

  private boolean isValidOutputType(final String outputType)
  {
    return ReportProcessTaskRegistry.getInstance().isExportTypeRegistered(outputType);
  }

  /**
   * Computes the effective output target that will be used when running the report. This method does not
   * modify any of the properties of this class.
   * <p/>
   * The algorithm to determine the output target is as follows:
   * <ul>
   * <li>
   * If the report attribute "lock-preferred-output-type" is set, and the attribute preferred-output-type is set,
   * the report will always be exported to the specified output type.</li>
   * <li>If the component has the parameter "output-target" set, this output target will be used.</li>
   * <li>If the component has the parameter "output-type" set, the mime-type will be translated into a suitable
   * output target (depends on other parameters like paginate as well.</li>
   * <li>If neither output-target or output-type are specified, the report's preferred output type will be used.</li>
   * <li>If no preferred output type is set, we default to HTML export.</li>
   * </ul>
   * <p/>
   * If the output type given is invalid, the report will not be executed and calls to
   * <code>SimpleReportingComponent#getMimeType()</code> will yield the generic "application/octet-stream" response.
   *
   * @return
   * @throws IOException
   * @throws ResourceException
   */
  private String computeEffectiveOutputTarget() throws IOException, ResourceException
  {
    final MasterReport report = getReport();
    if (Boolean.TRUE.equals(report.getAttribute(AttributeNames.Core.NAMESPACE, AttributeNames.Core.LOCK_PREFERRED_OUTPUT_TYPE)))
    {
      // preferred output type is one of the engine's output-target identifiers. It is not a mime-type string.
      // The engine supports multiple subformats per mime-type (example HTML: zipped/streaming/flow/pageable)
      // The mime-mapping would be inaccurate.
      final Object preferredOutputType = report.getAttribute
          (AttributeNames.Core.NAMESPACE, AttributeNames.Core.PREFERRED_OUTPUT_TYPE);
      if (preferredOutputType != null)
      {
        final String preferredOutputTypeString = String.valueOf(preferredOutputType);
        if (isValidOutputType(preferredOutputTypeString))
        {
          // if it is a recognized process-type, then fine, return it.
          return preferredOutputTypeString;
        }

        final String mappedLegacyType = mapOutputTypeToOutputTarget(preferredOutputTypeString);
        if (mappedLegacyType != null)
        {
          log.warn(Messages.getString("ReportPlugin.warnLegacyLockedOutput", preferredOutputTypeString));
          return mappedLegacyType;
        }

        log.warn(Messages.getString("ReportPlugin.warnInvalidLockedOutput", preferredOutputTypeString));
      }
    }

    final String outputTarget = getOutputTarget();
    if (outputTarget != null)
    {
      if (isValidOutputType(outputTarget) == false)
      {
        log.warn(Messages.getString("ReportPlugin.warnInvalidOutputTarget", outputTarget));
      }
      // if a engine-level output target is given, use it as it is. We can assume that the user knows how to
      // map from that to a real mime-type.
      return outputTarget;
    }

    final String mappingFromParams = mapOutputTypeToOutputTarget(getOutputType());
    if (mappingFromParams != null)
    {
      return mappingFromParams;
    }

    // if nothing is specified explicity, we may as well ask the report what it prefers..
    final Object preferredOutputType = report.getAttribute(AttributeNames.Core.NAMESPACE, AttributeNames.Core.PREFERRED_OUTPUT_TYPE);
    if (preferredOutputType != null)
    {
      final String preferredOutputTypeString = String.valueOf(preferredOutputType);
      if (isValidOutputType(preferredOutputTypeString))
      {
        return preferredOutputTypeString;
      }

      final String mappedLegacyType = mapOutputTypeToOutputTarget(preferredOutputTypeString);
      if (mappedLegacyType != null)
      {
        log.warn(Messages.getString("ReportPlugin.warnLegacyPreferredOutput", preferredOutputTypeString));
        return mappedLegacyType;
      }

      log.warn(Messages.getString("ReportPlugin.warnInvalidPreferredOutput",
          preferredOutputTypeString,  HtmlTableModule.TABLE_HTML_STREAM_EXPORT_TYPE));
      return HtmlTableModule.TABLE_HTML_STREAM_EXPORT_TYPE;
    }

    // if you have come that far, it means you really messed up. Sorry, this error is not a error caused
    // by our legacy code - it is more likely that you just entered values that are totally wrong.
    log.error(Messages.getString("ReportPlugin.warnInvalidOutputType", getOutputType(),
        HtmlTableModule.TABLE_HTML_STREAM_EXPORT_TYPE));
    return HtmlTableModule.TABLE_HTML_STREAM_EXPORT_TYPE;
  }

  private String mapOutputTypeToOutputTarget(final String outputType)
  {
    // if the user has given a mime-type instead of a output-target, lets map it to the "best" choice. If the
    // user wanted full control, he would have used the output-target property instead.
    if (MIME_TYPE_CSV.equals(outputType))
    {
      return CSVTableModule.TABLE_CSV_STREAM_EXPORT_TYPE;
    }
    if (MIME_TYPE_HTML.equals(outputType))
    {
      if (isPaginateOutput())
      {
        return HtmlTableModule.TABLE_HTML_PAGE_EXPORT_TYPE;
      }
      return HtmlTableModule.TABLE_HTML_STREAM_EXPORT_TYPE;
    }
    if (MIME_TYPE_PDF.equals(outputType))
    {
      return PdfPageableModule.PDF_EXPORT_TYPE;
    }
    if (MIME_TYPE_RTF.equals(outputType))
    {
      return RTFTableModule.TABLE_RTF_FLOW_EXPORT_TYPE;
    }
    if (MIME_TYPE_XLS.equals(outputType))
    {
      return ExcelTableModule.EXCEL_FLOW_EXPORT_TYPE;
    }
    if (MIME_TYPE_EMAIL.equals(outputType))
    {
      return MIME_TYPE_EMAIL;
    }
    if (MIME_TYPE_TXT.equals(outputType))
    {
      return PlainTextPageableModule.PLAINTEXT_EXPORT_TYPE;
    }

    if ("pdf".equalsIgnoreCase(outputType)) //$NON-NLS-1$
    {
      log.warn(Messages.getString("ReportPlugin.warnDeprecatedPDF"));
      return PdfPageableModule.PDF_EXPORT_TYPE;
    }
    else if ("html".equalsIgnoreCase(outputType)) //$NON-NLS-1$
    {
      log.warn(Messages.getString("ReportPlugin.warnDeprecatedHTML"));
      if (isPaginateOutput())
      {
        return HtmlTableModule.TABLE_HTML_PAGE_EXPORT_TYPE;
      }
      return HtmlTableModule.TABLE_HTML_STREAM_EXPORT_TYPE;
    }
    else if ("csv".equalsIgnoreCase(outputType)) //$NON-NLS-1$
    {
      log.warn(Messages.getString("ReportPlugin.warnDeprecatedCSV"));
      return CSVTableModule.TABLE_CSV_STREAM_EXPORT_TYPE;
    }
    else if ("rtf".equalsIgnoreCase(outputType)) //$NON-NLS-1$
    {
      log.warn(Messages.getString("ReportPlugin.warnDeprecatedRTF"));
      return RTFTableModule.TABLE_RTF_FLOW_EXPORT_TYPE;
    }
    else if ("xls".equalsIgnoreCase(outputType)) //$NON-NLS-1$
    {
      log.warn(Messages.getString("ReportPlugin.warnDeprecatedXLS"));
      return ExcelTableModule.EXCEL_FLOW_EXPORT_TYPE;
    }
    else if ("txt".equalsIgnoreCase(outputType)) //$NON-NLS-1$
    {
      log.warn(Messages.getString("ReportPlugin.warnDeprecatedTXT"));
      return PlainTextPageableModule.PLAINTEXT_EXPORT_TYPE;
    }
    return null;
  }

  /**
   * Apply inputs (if any) to corresponding report parameters, care is taken when checking parameter types to perform any necessary casting and conversion.
   *
   * @param report  a MasterReport object to apply parameters to
   * @param context a ParameterContext for which the parameters will be under
   * @deprecated use the single parameter version instead. This method will now fail with an error if the
   *             report passed in is not the same as the report this component has.
   */
  public void applyInputsToReportParameters(final MasterReport report, final ParameterContext context)
  {
    try
    {
      if (getReport() != report)
      {
        throw new IllegalStateException(Messages.getString("ReportPlugin.errorForeignReportInput"));
      }
      final ValidationResult validationResult = applyInputsToReportParameters(context, null);
      if (validationResult.isEmpty() == false)
      {
        throw new IllegalStateException(Messages.getString("ReportPlugin.errorApplyInputsFailed"));
      }
    }
    catch (IOException e)
    {
      throw new IllegalStateException(Messages.getString("ReportPlugin.errorApplyInputsFailed"), e);
    }
    catch (ResourceException e)
    {
      throw new IllegalStateException(Messages.getString("ReportPlugin.errorApplyInputsFailed"), e);
    }
  }

  /**
   * Apply inputs (if any) to corresponding report parameters, care is taken when checking parameter types to perform any necessary casting and conversion.
   *
   * @param context a ParameterContext for which the parameters will be under
   * @param validationResult the validation result that will hold the warnings. If null, a new one will be created.
   * @throws java.io.IOException if the report of this component could not be parsed.
   * @throws ResourceException   if the report of this component could not be parsed.
   */
  public ValidationResult applyInputsToReportParameters(final ParameterContext context,
                                                        ValidationResult validationResult)
      throws IOException, ResourceException
  {
    if (validationResult == null)
    {
      validationResult = new ValidationResult();
    }
    // apply inputs to report
    if (inputs != null)
    {
      final MasterReport report = getReport();
      final ParameterDefinitionEntry[] params = report.getParameterDefinition().getParameterDefinitions();
      final ReportParameterValues parameterValues = report.getParameterValues();
      for (final ParameterDefinitionEntry param : params)
      {
        final String paramName = param.getName();
        Object value = inputs.get(paramName);
        final Object defaultValue = param.getDefaultValue(context);
        if (value == null && defaultValue != null)
        {
          value = defaultValue;
        }
        try
        {
          addParameter(context, parameterValues, param, value);
        }
        catch (Exception e)
        {
          if (log.isDebugEnabled())
          {
            log.warn(Messages.getString("ReportPlugin.logErrorParametrization"), e);
          }
          validationResult.addError(paramName, new ValidationMessage(e.getMessage()));
        }
      }
    }
    return validationResult;
  }

  private Object convert(final ParameterContext context,
                         final ParameterDefinitionEntry parameter,
                         final Class targetType, final Object rawValue)
      throws ReportProcessingException
  {
    if (targetType == null)
    {
      throw new NullPointerException();
    }

    if (rawValue == null)
    {
      return null;
    }
    if (targetType.isInstance(rawValue))
    {
      return rawValue;
    }

    if (targetType.isAssignableFrom(TableModel.class) && IPentahoResultSet.class.isAssignableFrom(rawValue.getClass()))
    {
      // wrap IPentahoResultSet to simulate TableModel
      return new PentahoTableModel((IPentahoResultSet) rawValue);
    }

    final String valueAsString = String.valueOf(rawValue);
    if (StringUtils.isEmpty(valueAsString))
    {
      return null;
    }

    if (targetType.equals(Timestamp.class))
    {
      try
      {
        return new Timestamp(new Long(valueAsString));
      }
      catch (NumberFormatException nfe)
      {
        // ignore, we try to parse it as real date now ..
      }
    }
    else if (targetType.equals(Time.class))
    {
      try
      {
        return new Time(new Long(valueAsString));
      }
      catch (NumberFormatException nfe)
      {
        // ignore, we try to parse it as real date now ..
      }
    }
    else if (targetType.equals(java.sql.Date.class))
    {
      try
      {
        return new java.sql.Date(new Long(valueAsString));
      }
      catch (NumberFormatException nfe)
      {
        // ignore, we try to parse it as real date now ..
      }
    }
    else if (targetType.equals(Date.class))
    {
      try
      {
        return new Date(new Long(valueAsString));
      }
      catch (NumberFormatException nfe)
      {
        // ignore, we try to parse it as real date now ..
      }
    }

    final String dataFormat = parameter.getParameterAttribute(ParameterAttributeNames.Core.NAMESPACE,
        ParameterAttributeNames.Core.DATA_FORMAT, context);
    if (dataFormat != null)
    {
      try
      {
        if (Number.class.isAssignableFrom(targetType))
        {
          final DecimalFormat format = new DecimalFormat(dataFormat, new DecimalFormatSymbols(LocaleHelper.getLocale()));
          format.setParseBigDecimal(true);
          final Number number = format.parse(valueAsString);
          final String asText = ConverterRegistry.toAttributeValue(number);
          return ConverterRegistry.toPropertyValue(asText, targetType);
        }
        else if (Date.class.isAssignableFrom(targetType))
        {
          final SimpleDateFormat format = new SimpleDateFormat(dataFormat, new DateFormatSymbols(LocaleHelper.getLocale()));
          format.setLenient(false);
          final Date number = format.parse(valueAsString);
          final String asText = ConverterRegistry.toAttributeValue(number);
          return ConverterRegistry.toPropertyValue(asText, targetType);
        }
      }
      catch (Exception e)
      {
        // again, ignore it .
      }
    }

    final ValueConverter valueConverter = ConverterRegistry.getInstance().getValueConverter(targetType);
    if (valueConverter != null)
    {
      try
      {
        return valueConverter.toPropertyValue(valueAsString);
      }
      catch (BeanException e)
      {
        throw new ReportProcessingException(Messages.getString
            ("ReportPlugin.unableToConvertParameter", parameter.getName())); //$NON-NLS-1$
      }
    }
    return rawValue;
  }

  private void addParameter(final ParameterContext report,
                            final ReportParameterValues parameterValues,
                            final ParameterDefinitionEntry param,
                            final Object value)
      throws ReportProcessingException
  {
    if (value == null)
    {
      parameterValues.put(param.getName(), null);
      return;
    }
    if (value.getClass().isArray())
    {
      final Class componentType;
      if (param.getValueType().isArray())
      {
        componentType = param.getValueType().getComponentType();
      }
      else
      {
        componentType = param.getValueType();
      }

      final int length = Array.getLength(value);
      final Object array = Array.newInstance(componentType, length);
      for (int i = 0; i < length; i++)
      {
        Array.set(array, i, convert(report, param, componentType, Array.get(value, i)));
      }
      parameterValues.put(param.getName(), array);
    }
    else if (isAllowMultiSelect(param))
    {
      // if the parameter allows multi selections, wrap this single input in an array
      // and re-call addParameter with it
      final Object[] array = new Object[1];
      array[0] = value;
      addParameter(report, parameterValues, param,array);
    }
    else
    {
      parameterValues.put(param.getName(), convert(report, param, param.getValueType(), value));
    }
  }

  private boolean isAllowMultiSelect(final ParameterDefinitionEntry parameter)
  {
    if (parameter instanceof ListParameter)
    {
      final ListParameter listParameter = (ListParameter) parameter;
      return listParameter.isAllowMultiSelection();
    }
    return false;
  }

  private URL getDefinedResourceURL(final URL defaultValue)
  {
    if (inputs == null || inputs.containsKey(REPORTLOAD_RESURL) == false)
    {
      return defaultValue;
    }

    try
    {
      final String inputStringValue = (String) getInput(REPORTLOAD_RESURL, null);
      return new URL(inputStringValue);
    }
    catch (Exception e)
    {
      return defaultValue;
    }
  }

  protected int getYieldRate()
  {
    final Object yieldRate = getInput(REPORTGENERATE_YIELDRATE, null);
    if (yieldRate instanceof Number)
    {
      final Number n = (Number) yieldRate;
      if (n.intValue() < 1)
      {
        return 0;
      }
      return n.intValue();
    }
    return 0;
  }

  /**
   * This method returns the number of logical pages which make up the report. This results of this method are available only after validate/execute have been
   * successfully called. This field has no setter, as it should never be set by users.
   *
   * @return the number of logical pages in the report
   */
  public int getPageCount()
  {
    return pageCount;
  }

  /**
   * This method will determine if the component instance 'is valid.' The validate() is called after all of the bean 'setters' have been called, so we may
   * validate on the actual values, not just the presence of inputs as we were historically accustomed to.
   * <p/>
   * Since we should have a list of all action-sequence inputs, we can determine if we have sufficient inputs to meet the parameter requirements of the
   * report-definition. This would include validation of values and ranges of values.
   *
   * @return true if valid
   * @throws Exception
   */
  public boolean validate() throws Exception
  {
    if (reportDefinition == null && reportDefinitionInputStream == null && reportDefinitionPath == null)
    {
      log.error(Messages.getString("ReportPlugin.reportDefinitionNotProvided")); //$NON-NLS-1$
      return false;
    }
    if (reportDefinition != null && reportDefinitionPath != null && session == null)
    {
      log.error(Messages.getString("ReportPlugin.noUserSession")); //$NON-NLS-1$
      return false;
    }
    if (outputStream == null && print == false)
    {
      log.error(Messages.getString("ReportPlugin.outputStreamRequired")); //$NON-NLS-1$
      return false;
    }
    return true;
  }

  /**
   * Perform the primary function of this component, this is, to execute. This method will be invoked immediately following a successful validate().
   *
   * @return true if successful execution
   * @throws Exception
   */
  public boolean execute() throws Exception
  {
    final MasterReport report = getReport();

    try
    {
      final ParameterContext parameterContext = new DefaultParameterContext(report);
      // open parameter context
      parameterContext.open();
      final ValidationResult vr = applyInputsToReportParameters(parameterContext, null);
      if (vr.isEmpty() == false)
      {
        return false;
      }
      parameterContext.close();

      if (isPrint())
      {
        // handle printing
        // basic logic here is: get the default printer, attempt to resolve the user specified printer, default back as needed
        PrintService printService = PrintServiceLookup.lookupDefaultPrintService();
        if (StringUtils.isEmpty(getPrinter()) == false)
        {
          final PrintService[] services = PrintServiceLookup.lookupPrintServices(DocFlavor.SERVICE_FORMATTED.PAGEABLE, null);
          for (final PrintService service : services)
          {
            if (service.getName().equals(printer))
            {
              printService = service;
            }
          }
          if ((printer == null) && (services.length > 0))
          {
            printService = services[0];
          }
        }
        Java14PrintUtil.printDirectly(report, printService);
        return true;
      }

      final String outputType = computeEffectiveOutputTarget();
      final Configuration globalConfig = ClassicEngineBoot.getInstance().getGlobalConfig();
      if (HtmlTableModule.TABLE_HTML_PAGE_EXPORT_TYPE.equals(outputType))
      {
        if (dashboardMode)
        {
          report.getReportConfiguration().setConfigProperty(HtmlTableModule.BODY_FRAGMENT, "true");
        }
        String contentHandlerPattern = (String) getInput(REPORTHTML_CONTENTHANDLER_PATTERN,
            globalConfig.getConfigProperty("org.pentaho.web.ContentHandler")); //$NON-NLS-1$
        if (useContentRepository)
        {
          // use the content repository
          contentHandlerPattern = (String) getInput(REPORTHTML_CONTENTHANDLER_PATTERN,
              globalConfig.getConfigProperty("org.pentaho.web.resource.ContentHandler")); //$NON-NLS-1$
          final IContentRepository contentRepository = PentahoSystem.get(IContentRepository.class, session);
          pageCount = PageableHTMLOutput.generate(session, report, acceptedPage, outputStream,
              contentRepository, contentHandlerPattern, getYieldRate());
          return true;
        }
        else
        {
          // don't use the content repository
          pageCount = PageableHTMLOutput.generate(report, acceptedPage, outputStream, contentHandlerPattern, getYieldRate());
          return true;
        }
      }
      if (HtmlTableModule.TABLE_HTML_STREAM_EXPORT_TYPE.equals(outputType))
      {
        if (dashboardMode)
        {
          report.getReportConfiguration().setConfigProperty(HtmlTableModule.BODY_FRAGMENT, "true");
        }
        String contentHandlerPattern = (String) getInput(REPORTHTML_CONTENTHANDLER_PATTERN, globalConfig
            .getConfigProperty("org.pentaho.web.ContentHandler")); //$NON-NLS-1$
        if (useContentRepository)
        {
          // use the content repository
          contentHandlerPattern = (String) getInput(REPORTHTML_CONTENTHANDLER_PATTERN, globalConfig.getConfigProperty(
              "org.pentaho.web.resource.ContentHandler")); //$NON-NLS-1$
          final IContentRepository contentRepository = PentahoSystem.get(IContentRepository.class, session);
          return HTMLOutput.generate(session, report, outputStream, contentRepository, contentHandlerPattern, getYieldRate());
        }
        else
        {
          // don't use the content repository
          return HTMLOutput.generate(report, outputStream, contentHandlerPattern, getYieldRate());
        }
      }
      if (PdfPageableModule.PDF_EXPORT_TYPE.equals(outputType))
      {
        return PDFOutput.generate(report, outputStream, getYieldRate());
      }
      if (ExcelTableModule.EXCEL_FLOW_EXPORT_TYPE.equals(outputType))
      {
        final InputStream templateInputStream = (InputStream) getInput(XLS_WORKBOOK_PARAM, null);
        return XLSOutput.generate(report, outputStream, templateInputStream, getYieldRate());
      }
      if (CSVTableModule.TABLE_CSV_STREAM_EXPORT_TYPE.equals(outputType))
      {
        return CSVOutput.generate(report, outputStream, getYieldRate());
      }
      if (RTFTableModule.TABLE_RTF_FLOW_EXPORT_TYPE.equals(outputType))
      {
        return RTFOutput.generate(report, outputStream, getYieldRate());
      }

      log.warn(Messages.getString("ReportPlugin.warnUnprocessableRequest", outputType));

    }
    catch (Throwable t)
    {
      log.error(Messages.getString("ReportPlugin.executionFailed"), t); //$NON-NLS-1$
    }
    // lets not pretend we were successfull, if the export type was not a valid one.
    return false;
  }

}
