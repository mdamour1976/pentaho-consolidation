/*!
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
* Foundation.
*
* You should have received a copy of the GNU Lesser General Public License along with this
* program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
* or from the Free Software Foundation, Inc.,
* 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*
* This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU Lesser General Public License for more details.
*
* Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
*/

package org.pentaho.reporting.designer.core.actions.report.preview;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;

import org.pentaho.reporting.designer.core.actions.AbstractReportContextAction;
import org.pentaho.reporting.designer.core.actions.ActionMessages;
import org.pentaho.reporting.designer.core.util.ExternalToolLauncher;
import org.pentaho.reporting.designer.core.util.IconLoader;
import org.pentaho.reporting.designer.core.util.exceptions.UncaughtExceptionsModel;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.modules.gui.base.PreviewParametersDialog;
import org.pentaho.reporting.engine.classic.core.modules.gui.commonswing.ExceptionDialog;
import org.pentaho.reporting.engine.classic.core.modules.gui.commonswing.ReportProgressDialog;
import org.pentaho.reporting.engine.classic.core.modules.output.table.base.StreamReportProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.table.rtf.StreamRTFOutputProcessor;
import org.pentaho.reporting.libraries.designtime.swing.LibSwingUtil;

/**
 * Todo: Document Me
 *
 * @author Thomas Morgner
 */
public final class PreviewRtfAction extends AbstractReportContextAction
{
  public PreviewRtfAction()
  {
    putValue(Action.NAME, ActionMessages.getString("PreviewRtfAction.Text"));
    putValue(Action.SHORT_DESCRIPTION, ActionMessages.getString("PreviewRtfAction.Description"));
    putValue(Action.MNEMONIC_KEY, ActionMessages.getOptionalMnemonic("PreviewRtfAction.Mnemonic"));
    putValue(Action.ACCELERATOR_KEY, ActionMessages.getOptionalKeyStroke("PreviewRtfAction.Accelerator"));
    putValue(Action.SMALL_ICON, IconLoader.getInstance().getPreviewRTFIcon());
  }

  /**
   * Invoked when an action occurs.
   */
  public void actionPerformed(final ActionEvent e)
  {
    if (getActiveContext() == null)
    {
      return;
    }

    final MasterReport reportElement = getActiveContext().getContextRoot();
    final Component parent = getReportDesignerContext().getView().getParent();
    final Window window = LibSwingUtil.getWindowAncestor(parent);
    if (PreviewParametersDialog.process(window, reportElement))
    {
      final ReportProgressDialog dialog;
      if (window instanceof JDialog)
      {
        dialog = new ReportProgressDialog((JDialog) window);
      }
      else if (window instanceof JFrame)
      {
        dialog = new ReportProgressDialog((JFrame) window);
      }
      else
      {
        dialog = new ReportProgressDialog();
      }

      final Thread t = new Thread(new ExportTask(reportElement, dialog));
      t.setDaemon(true);
      t.start();
    }
  }

  private static class ExportTask implements Runnable
  {
    private MasterReport report;
    private ReportProgressDialog progressDialog;

    private ExportTask(final MasterReport report,
                       final ReportProgressDialog progressDialog)
    {
      this.report = report;
      this.progressDialog = progressDialog;
    }

    public void run()
    {
      try
      {
        final File tempFile = File.createTempFile("report-designer-preview", ".rtf");//$NON-NLS-1$
        tempFile.deleteOnExit();
        final FileOutputStream fout = new FileOutputStream(tempFile);
        try
        {
          final BufferedOutputStream bout = new BufferedOutputStream(fout);
          final StreamRTFOutputProcessor target =
              new StreamRTFOutputProcessor(report.getConfiguration(), bout, report.getResourceManager());
          final StreamReportProcessor reportProcessor = new StreamReportProcessor(report, target);

          reportProcessor.addReportProgressListener(progressDialog);
          progressDialog.setVisibleInEDT(true);

          reportProcessor.processReport();
          reportProcessor.close();
          bout.flush();
          reportProcessor.removeReportProgressListener(progressDialog);
        }
        finally
        {
          fout.close();
        }
        progressDialog.setVisibleInEDT(false);

        ExternalToolLauncher.openRTF(tempFile);
      }
      catch (Exception e1)
      {
        UncaughtExceptionsModel.getInstance().addException(e1);
        progressDialog.dispose();
        final String errorMessage = ActionMessages.getString("PreviewReport.Error.Text");
        final String errorTitle = ActionMessages.getString("PreviewReport.Error.Title");
        ExceptionDialog.showExceptionDialog(progressDialog.getParent(), errorTitle, errorMessage, e1);
      }
    }
  }

}
