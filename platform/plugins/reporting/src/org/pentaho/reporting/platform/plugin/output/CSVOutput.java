package org.pentaho.reporting.platform.plugin.output;

import java.io.IOException;
import java.io.OutputStream;

import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.layout.output.YieldReportListener;
import org.pentaho.reporting.engine.classic.core.modules.output.table.base.StreamReportProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.table.csv.StreamCSVOutputProcessor;

public class CSVOutput
{

  public static boolean generate(final MasterReport report,
                                 final OutputStream outputStream,
                                 final int yieldRate) throws ReportProcessingException, IOException
  {
    StreamReportProcessor proc = null;
    try
    {
      final StreamCSVOutputProcessor target = new StreamCSVOutputProcessor(report.getConfiguration(), outputStream);
      proc = new StreamReportProcessor(report, target);

      if (yieldRate > 0)
      {
        proc.addReportProgressListener(new YieldReportListener(yieldRate));
      }
      proc.processReport();
      proc.close();
      proc = null;
      outputStream.close();
      return true;
    }
    finally
    {
      if (proc != null)
      {
        proc.close();
      }
    }
  }
}
