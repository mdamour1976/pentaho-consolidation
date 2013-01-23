/*
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
 * Copyright (c) 2009 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.reporting.designer.core.editor.report.elements;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Window;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.util.Locale;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.pentaho.reporting.designer.core.Messages;
import org.pentaho.reporting.designer.core.ReportDesignerContext;
import org.pentaho.reporting.designer.core.actions.elements.InsertCrosstabGroupAction;
import org.pentaho.reporting.designer.core.editor.ReportRenderContext;
import org.pentaho.reporting.designer.core.editor.parameters.SubReportDataSourceDialog;
import org.pentaho.reporting.designer.core.editor.report.DndElementOverlay;
import org.pentaho.reporting.designer.core.editor.report.ReportElementDragHandler;
import org.pentaho.reporting.designer.core.editor.report.ReportElementEditorContext;
import org.pentaho.reporting.designer.core.model.CachedLayoutData;
import org.pentaho.reporting.designer.core.model.ModelUtility;
import org.pentaho.reporting.designer.core.util.exceptions.UncaughtExceptionsModel;
import org.pentaho.reporting.designer.core.util.undo.BandedSubreportEditUndoEntry;
import org.pentaho.reporting.designer.core.util.undo.ElementEditUndoEntry;
import org.pentaho.reporting.designer.core.util.undo.UndoManager;
import org.pentaho.reporting.engine.classic.core.AbstractRootLevelBand;
import org.pentaho.reporting.engine.classic.core.Band;
import org.pentaho.reporting.engine.classic.core.CrosstabElement;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.DetailsFooter;
import org.pentaho.reporting.engine.classic.core.DetailsHeader;
import org.pentaho.reporting.engine.classic.core.Element;
import org.pentaho.reporting.engine.classic.core.PageFooter;
import org.pentaho.reporting.engine.classic.core.PageHeader;
import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.engine.classic.core.RootLevelBand;
import org.pentaho.reporting.engine.classic.core.Watermark;
import org.pentaho.reporting.engine.classic.core.metadata.ElementMetaData;
import org.pentaho.reporting.engine.classic.core.metadata.ElementType;
import org.pentaho.reporting.engine.classic.core.style.ElementStyleKeys;
import org.pentaho.reporting.engine.classic.core.style.ElementStyleSheet;
import org.pentaho.reporting.engine.classic.core.util.geom.StrictGeomUtility;
import org.pentaho.reporting.engine.classic.extensions.parsers.reportdesigner.ReportDesignerParserModule;
import org.pentaho.reporting.libraries.designtime.swing.LibSwingUtil;

/**
 * Crosstab drag handler
 *
 * @author Sulaiman Karmali
 */
public class CrosstabReportElementDragHandler implements ReportElementDragHandler
{
  protected static final Float DEFAULT_WIDTH = new Float(100);
  protected static final Float DEFAULT_HEIGHT = new Float(20);

  private DndElementOverlay representation;

  public CrosstabReportElementDragHandler()
  {
    representation = new DndElementOverlay();
  }

  public int dragStarted(final DropTargetDragEvent event,
                         final ReportElementEditorContext dragContext,
                         final ElementMetaData elementMetaData,
                         final String fieldName)
  {
    final Container representationContainer = dragContext.getRepresentationContainer();
    final ReportRenderContext renderContext = dragContext.getRenderContext();
    final Point pos = event.getLocation();
    final Point2D point = dragContext.normalize(pos);
    if (point.getX() < 0 || point.getY() < 0)
    {
      representationContainer.removeAll();
      return DnDConstants.ACTION_NONE;
    }

    final Element rootBand = findRootBand(dragContext, point);
    if (rootBand instanceof PageHeader ||
        rootBand instanceof PageFooter ||
        rootBand instanceof DetailsHeader ||
        rootBand instanceof DetailsFooter ||
        rootBand instanceof Watermark)
    {
      representationContainer.removeAll();
      return DnDConstants.ACTION_NONE;
    }

    representation.setZoom(renderContext.getZoomModel().getZoomAsPercentage());
    representation.setVisible(true);
    representation.setText(elementMetaData.getDisplayName(Locale.getDefault()));
    representation.setLocation(pos.x, pos.y);
    representation.setSize(representation.getMinimumSize());
    representationContainer.removeAll();
    representationContainer.add(representation);
    return DnDConstants.ACTION_COPY;
  }

  private Element findRootBand(final ReportElementEditorContext dragContext,
                               final Point2D point)
  {
    Element element = dragContext.getElementForLocation(point, false);
    while (element != null && ((element instanceof RootLevelBand) == false))
    {
      element = element.getParent();
    }

    if (element != null)
    {
      return element;
    }

    return dragContext.getDefaultElement();
  }

  public int dragUpdated(final DropTargetDragEvent event,
                         final ReportElementEditorContext dragContext,
                         final ElementMetaData elementMetaData,
                         final String fieldName)
  {
    return dragStarted(event, dragContext, elementMetaData, fieldName);
  }

  public void dragAborted(final DropTargetEvent event,
                          final ReportElementEditorContext dragContext)
  {
    final Container representationContainer = dragContext.getRepresentationContainer();
    representationContainer.removeAll();
  }

  public void drop(final DropTargetDropEvent event,
                   final ReportElementEditorContext dragContext,
                   final ElementMetaData elementMetaData,
                   final String fieldName)
  {
    final Point2D point = dragContext.normalize(event.getLocation());
    final Element rootBand = findRootBand(dragContext, point);
    if (rootBand instanceof PageHeader ||
        rootBand instanceof PageFooter ||
        rootBand instanceof DetailsHeader ||
        rootBand instanceof DetailsFooter ||
        rootBand instanceof Watermark)
    {
      event.rejectDrop();
      return;
    }

    try
    {
      // Prompt for existing or new datasource
      final ReportRenderContext activeContext = dragContext.getDesignerContext().getActiveContext();
      if (activeContext == null)
      {
        return;
      }

      // Create a crosstab subreport element
      final ElementType type = elementMetaData.create();
      final CrosstabElement visualElement = new CrosstabElement();
      visualElement.setElementType(type);

      // Hide all bands except for Details
      visualElement.getRelationalGroup(0).getHeader().setAttribute(ReportDesignerParserModule.NAMESPACE, ReportDesignerParserModule.HIDE_IN_LAYOUT_GUI_ATTRIBUTE,Boolean.TRUE);
      visualElement.getRelationalGroup(0).getFooter().setAttribute(ReportDesignerParserModule.NAMESPACE, ReportDesignerParserModule.HIDE_IN_LAYOUT_GUI_ATTRIBUTE,Boolean.TRUE);
      visualElement.getPageHeader().setAttribute(ReportDesignerParserModule.NAMESPACE, ReportDesignerParserModule.HIDE_IN_LAYOUT_GUI_ATTRIBUTE, Boolean.TRUE);
      visualElement.getReportHeader().setAttribute(ReportDesignerParserModule.NAMESPACE, ReportDesignerParserModule.HIDE_IN_LAYOUT_GUI_ATTRIBUTE, Boolean.TRUE);
      visualElement.getDetailsFooter().setAttribute(ReportDesignerParserModule.NAMESPACE, ReportDesignerParserModule.HIDE_IN_LAYOUT_GUI_ATTRIBUTE, Boolean.TRUE);
      visualElement.getDetailsHeader().setAttribute(ReportDesignerParserModule.NAMESPACE, ReportDesignerParserModule.HIDE_IN_LAYOUT_GUI_ATTRIBUTE, Boolean.TRUE);
      visualElement.getReportFooter().setAttribute(ReportDesignerParserModule.NAMESPACE, ReportDesignerParserModule.HIDE_IN_LAYOUT_GUI_ATTRIBUTE, Boolean.TRUE);
      visualElement.getPageFooter().setAttribute(ReportDesignerParserModule.NAMESPACE, ReportDesignerParserModule.HIDE_IN_LAYOUT_GUI_ATTRIBUTE, Boolean.TRUE);
      visualElement.getNoDataBand().setAttribute(ReportDesignerParserModule.NAMESPACE, ReportDesignerParserModule.HIDE_IN_LAYOUT_GUI_ATTRIBUTE, Boolean.TRUE);
      visualElement.getWatermark().setAttribute(ReportDesignerParserModule.NAMESPACE, ReportDesignerParserModule.HIDE_IN_LAYOUT_GUI_ATTRIBUTE, Boolean.TRUE);

      type.configureDesignTimeDefaults(visualElement, Locale.getDefault());

      // Update crosstab styles
      final ElementStyleSheet styleSheet = visualElement.getStyle();
      styleSheet.setStyleProperty(ElementStyleKeys.MIN_WIDTH, DEFAULT_WIDTH);
      styleSheet.setStyleProperty(ElementStyleKeys.MIN_HEIGHT, DEFAULT_HEIGHT);

      final Element element = dragContext.getElementForLocation(point, false);
      final Band band;
      if (element instanceof Band)
      {
        band = (Band) element;
      }
      else if (element != null)
      {
        band = element.getParent();
      }
      else
      {
        final Element defaultEntry = dragContext.getDefaultElement();
        if (defaultEntry instanceof Band == false)
        {
          event.rejectDrop();
          dragContext.getRepresentationContainer().removeAll();
          return;
        }
        band = (Band) defaultEntry;
      }

      event.acceptDrop(DnDConstants.ACTION_COPY);

      styleSheet.setStyleProperty(ElementStyleKeys.POS_X, new Float(Math.max(0, point.getX() - getParentX(band))));
      styleSheet.setStyleProperty(ElementStyleKeys.POS_Y, new Float(Math.max(0, point.getY() - getParentY(band))));

      SwingUtilities.invokeLater(new CrosstabConfigureHandler(visualElement, band, dragContext, rootBand == band));

      representation.setVisible(false);
      dragContext.getRepresentationContainer().removeAll();
      event.dropComplete(true);
    }
    catch (final Exception e)
    {
      UncaughtExceptionsModel.getInstance().addException(e);
      dragContext.getRepresentationContainer().removeAll();
      event.dropComplete(false);
    }
  }

  private double getParentX(final Band band)
  {
    final CachedLayoutData data = ModelUtility.getCachedLayoutData(band);
    return StrictGeomUtility.toExternalValue(data.getX());
  }

  private double getParentY(final Band band)
  {
    final CachedLayoutData data = ModelUtility.getCachedLayoutData(band);
    return StrictGeomUtility.toExternalValue(data.getY());
  }

  private static class CrosstabConfigureHandler implements Runnable
  {
    private CrosstabElement subReport;
    private Band parent;
    private ReportElementEditorContext dragContext;
    private boolean rootband;

    private CrosstabConfigureHandler(final CrosstabElement subReport,
                                     final Band parent,
                                     final ReportElementEditorContext dragContext,
                                     final boolean rootband)
    {
      this.subReport = subReport;
      this.parent = parent;
      this.dragContext = dragContext;
      this.rootband = rootband;
    }

    public void run()
    {
      final ReportRenderContext context = dragContext.getRenderContext();
      if (rootband)
      {
        final int result = JOptionPane.showOptionDialog(dragContext.getRepresentationContainer(),
                                                        Messages.getString("CrosstabReportElementDragHandler.BandedOrInlineSubreportQuestion"),
                                                        Messages.getString("CrosstabReportElementDragHandler.InsertSubreport"),
                                                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                                                        new String[]{Messages.getString("CrosstabReportElementDragHandler.Inline"),
                                                          Messages.getString("CrosstabReportElementDragHandler.Banded"),
                                                          Messages.getString("CrosstabReportElementDragHandler.Cancel")},
                                                          Messages.getString("CrosstabReportElementDragHandler.Inline"));
        if (result == JOptionPane.CLOSED_OPTION || result == 2)
        {
          return;
        }

        if (result == 0)
        {
          final UndoManager undo = context.getUndo();
          undo.addChange(Messages.getString("SubreportReportElementDragHandler.UndoEntry"),
                         new ElementEditUndoEntry(parent.getObjectID(), parent.getElementCount(), null, subReport));
          parent.addElement(subReport);
        }
        else
        {
          final AbstractRootLevelBand arb = (AbstractRootLevelBand) parent;
          final UndoManager undo = context.getUndo();
          undo.addChange(Messages.getString("SubreportReportElementDragHandler.UndoEntry"),
                         new BandedSubreportEditUndoEntry(parent.getObjectID(), arb.getSubReportCount(), null, subReport));
          arb.addSubReport(subReport);
        }
      }
      else
      {
        final UndoManager undo = context.getUndo();
        undo.addChange(Messages.getString("SubreportReportElementDragHandler.UndoEntry"),
                       new ElementEditUndoEntry(parent.getObjectID(), parent.getElementCount(), null, subReport));
        parent.addElement(subReport);
      }

      final ReportDesignerContext designerContext = dragContext.getDesignerContext();
      final Component theParent = designerContext.getParent();
      final Window window = LibSwingUtil.getWindowAncestor(theParent);

      // Prompt user to either create or use an existing data-source.
      final SubReportDataSourceDialog crosstabDataSourceDialog;
      crosstabDataSourceDialog = new SubReportDataSourceDialog((JFrame)window);
      final String queryName = crosstabDataSourceDialog.performSelection(designerContext);

      // User has selected a query in the data source dialog
      final DataFactory dataFactory = crosstabDataSourceDialog.getSubReportDataFactory();
      if ((dataFactory != null) && (queryName != null))
      {
        subReport.setDataFactory(dataFactory);
        subReport.setQuery(queryName);

        try
        {
          // Create the new subreport tab - this is where the contents of the Crosstab
          // dialog will go.
          designerContext.addSubReport(designerContext.getActiveContext(), subReport);

          // Invoke Crosstab dialog
          InsertCrosstabGroupAction crosstabAction = new InsertCrosstabGroupAction();
          crosstabAction.setReportDesignerContext(designerContext);
          crosstabAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
        }
        catch (ReportDataFactoryException e)
        {
          UncaughtExceptionsModel.getInstance().addException(e);
        }
      }

      dragContext.getRenderContext().getSelectionModel().setSelectedElements(new Object[]{subReport});
    }
  }
}
