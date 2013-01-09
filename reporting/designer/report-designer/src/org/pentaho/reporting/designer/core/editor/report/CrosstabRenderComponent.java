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

package org.pentaho.reporting.designer.core.editor.report;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;

import org.pentaho.reporting.designer.core.ReportDesignerContext;
import org.pentaho.reporting.designer.core.editor.ReportRenderContext;
import org.pentaho.reporting.designer.core.editor.report.layouting.CrosstabRenderer;
import org.pentaho.reporting.designer.core.model.HorizontalPositionsModel;
import org.pentaho.reporting.designer.core.model.ModelUtility;
import org.pentaho.reporting.designer.core.model.lineal.LinealModel;
import org.pentaho.reporting.designer.core.settings.SettingsListener;
import org.pentaho.reporting.designer.core.util.BreakPositionsList;
import org.pentaho.reporting.engine.classic.core.Element;
import org.pentaho.reporting.engine.classic.core.ReportElement;
import org.pentaho.reporting.engine.classic.core.RootLevelBand;
import org.pentaho.reporting.engine.classic.core.Section;
import org.pentaho.reporting.engine.classic.core.event.ReportModelEvent;
import org.pentaho.reporting.engine.classic.core.event.ReportModelListener;
import org.pentaho.reporting.libraries.base.util.DebugLog;

/**
 * Todo: Document Me
 *
 * @author Thomas Morgner
 */
public class CrosstabRenderComponent extends AbstractRenderComponent
{

  private class RootBandChangeHandler implements SettingsListener, ReportModelListener
  {
    private RootBandChangeHandler()
    {
      updateGridSettings();
    }

    public void nodeChanged(final ReportModelEvent event)
    {
      final Object element = event.getElement();
      if (element instanceof ReportElement == false)
      {
        return;
      }

      final ReportElement reportElement = (ReportElement) element;
      final Section band = getRendererRoot().getElement();
      if (ModelUtility.isDescendant(band, reportElement))
      {
        rendererRoot.resetBounds();
        CrosstabRenderComponent.this.revalidate();
        CrosstabRenderComponent.this.repaint();
        return;
      }

      if (reportElement instanceof Section)
      {
        final Section section = (Section) reportElement;
        if (ModelUtility.isDescendant(section, band))
        {
          rendererRoot.resetBounds();
          CrosstabRenderComponent.this.revalidate();
          CrosstabRenderComponent.this.repaint();
        }
      }
    }

    public void settingsChanged()
    {
      updateGridSettings();

      // this is cheap, just repaint and we will be happy
      CrosstabRenderComponent.this.revalidate();
      CrosstabRenderComponent.this.repaint();

    }
  }

  private class AsyncChangeNotifier implements Runnable
  {
    public void run()
    {
      rendererRoot.fireChangeEvent();
    }
  }

  private class RequestFocusHandler extends MouseAdapter implements PropertyChangeListener
  {

    /**
     * Invoked when the mouse has been clicked on a component.
     */
    public void mouseClicked(final MouseEvent e)
    {
      requestFocusInWindow();
      setFocused(true);
      SwingUtilities.invokeLater(new AsyncChangeNotifier());
    }

    /**
     * This method gets called when a bound property is changed.
     *
     * @param evt A PropertyChangeEvent object describing the event source and the property that has changed.
     */

    public void propertyChange(final PropertyChangeEvent evt)
    {
      final Component owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
      final boolean oldFocused = isFocused();
      final boolean focused = (owner == CrosstabRenderComponent.this);
      if (oldFocused != focused)
      {
        setFocused(focused);
        repaint();
      }
      SwingUtilities.invokeLater(new AsyncChangeNotifier());
    }
  }

  private static final SelectionOverlayInformation[] EMPTY_OVERLAYS = new SelectionOverlayInformation[0];
  private BreakPositionsList horizontalEdgePositions;
  private BreakPositionsList verticalEdgePositions;
  private CrosstabRenderer rendererRoot;
  private RequestFocusHandler focusHandler;
  private RootBandChangeHandler changeHandler;

  public CrosstabRenderComponent(final ReportDesignerContext designerContext,
                                 final ReportRenderContext renderContext)
  {
    super(designerContext, renderContext);
    this.horizontalEdgePositions = new BreakPositionsList();
    this.verticalEdgePositions = new BreakPositionsList();
    this.changeHandler = new RootBandChangeHandler();

    focusHandler = new RequestFocusHandler();
    addMouseListener(focusHandler);
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("permanentFocusOwner", focusHandler); // NON-NLS
  }

  public void dispose()
  {
    super.dispose();

    KeyboardFocusManager.getCurrentKeyboardFocusManager().removePropertyChangeListener("permanentFocusOwner", focusHandler); // NON-NLS

    final ReportRenderContext renderContext = getRenderContext();
    renderContext.getReportDefinition().removeReportModelListener(changeHandler);
  }


  public Dimension getPreferredSize()
  {
    return super.getPreferredSize();
  }

  public Element getElementForLocation(final Point2D normalizedPoint, final boolean onlySelected)
  {
    return null;
  }

  protected RootLevelBand findRootBandForPosition(final Point2D point)
  {
    return null;
  }

  protected BreakPositionsList getHorizontalEdgePositions()
  {
    return horizontalEdgePositions;
  }

  protected BreakPositionsList getVerticalEdgePositions()
  {
    return verticalEdgePositions;
  }

  protected SelectionOverlayInformation[] getOverlayRenderers()
  {
    return EMPTY_OVERLAYS;
  }

  public void installRenderer(final CrosstabRenderer rendererRoot,
                              final LinealModel horizontalLinealModel,
                              final HorizontalPositionsModel horizontalPositionsModel)
  {
    this.rendererRoot = rendererRoot;
    super.installLineals(rendererRoot, horizontalLinealModel, horizontalPositionsModel);
  }

  public Element getDefaultElement()
  {
    if (rendererRoot == null)
    {
      return null;
    }
    return rendererRoot.getCrosstabGroup();
  }

  public CrosstabRenderer getRendererRoot()
  {
    return (CrosstabRenderer) getElementRenderer();
  }

  protected void paintSelectionRectangle(final Graphics2D g2)
  {

  }

  protected boolean isLocalElement(final ReportElement e)
  {
    return ModelUtility.isDescendant(rendererRoot.getCrosstabGroup(), e);
  }

}
