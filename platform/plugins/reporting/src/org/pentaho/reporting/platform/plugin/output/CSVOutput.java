package org.pentaho.reporting.platform.plugin.output;

import java.io.IOException;
import java.io.OutputStream;

import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.layout.output.YieldReportListener;
import org.pentaho.reporting.engine.classic.core.modules.output.table.base.StreamReportProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.table.csv.StreamCSVOutputProcessor;
import org.pentaho.reporting.libraries.repository.ContentIOException;

public class CSVOutput implements ReportOutputHandler
{

  public int paginate(final MasterReport report,
                      final int yieldRate) throws ReportProcessingException, IOException
  {
    return 0;
  }

  public boolean generate(final MasterReport report,
                          final int acceptedPage,
                          final OutputStream outputStream,
                          final int yieldRate) throws ReportProcessingException, IOException, ContentIOException
  {
    final StreamCSVOutputProcessor target = new StreamCSVOutputProcessor(report.getConfiguration(), outputStream);
    final StreamReportProcessor proc = new StreamReportProcessor(report, target);

    if (yieldRate > 0)
    {
      proc.addReportProgressListener(new YieldReportListener(yieldRate));
    }
    
    try
    {
      proc.processReport();
      proc.close();
      return true;
    }
    finally
    {
      proc.close();
    }
  }

  public void close()
  {

  }
}
