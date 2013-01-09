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

package org.pentaho.reporting.engine.classic.extensions.swt.demo.util;
/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2008, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation; either version 2.1 of the License, or 
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, 
 * USA.  
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc. 
 * in the United States and other countries.]
 *
 * ------------------
 * SWTGraphics2D.java
 * ------------------
 * (C) Copyright 2006-2008, by Henry Proudhon and Contributors.
 *
 * Original Author:  Henry Proudhon (henry.proudhon AT ensmp.fr);
 * Contributor(s):   Cedric Chabanois (cchabanois AT no-log.org);
 *                   David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 14-Jun-2006 : New class (HP);
 * 29-Jan-2007 : Fixed the fillRect method (HP);
 * 31-Jan-2007 : Moved the dummy JPanel to SWTUtils.java,
 *               implemented the drawLine method (HP);
 * 07-Apr-2007 : Dispose some of the swt ressources, 
 *               thanks to silent for pointing this out (HP);
 * 23-May-2007 : Removed resource leaks by adding a resource pool (CC);
 * 15-Jun-2007 : Fixed compile error for JDK 1.4 (DG);
 * 22-Oct-2007 : Implemented clipping (HP);
 * 22-Oct-2007 : Implemented some AlphaComposite support (HP);
 * 23-Oct-2007 : Added mechanism for storing RenderingHints (which are 
 *               still ignored at this point) (DG);
 * 23-Oct-2007 : Implemented drawPolygon(), drawPolyline(), drawOval(),
 *               fillOval(), drawArc() and fillArc() (DG);
 * 27-Nov-2007 : Implemented a couple of drawImage() methods (DG);
 *
 */

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.graphics.Transform;

/**
 * This is a class utility to draw Graphics2D stuff on a swt composite.
 * It is presently developed to use JFreeChart with the Standard
 * Widget Toolkit but may be of a wider use later.
 * <p/>
 * constructor SWTGraphics2D (GC gc, SWTGraphics2D parent) and create()
 * are created by Baochuan Lu 8/17/2008
 */
public class SWTGraphics2D extends Graphics2D
{

  /**
   * The swt graphic composite
   */
  private GC gc;

  /**
   * The rendering hints.  For now, these are not used, but at least the
   * basic mechanism is present.
   */
  private RenderingHints hints;

  /**
   * A reference to the compositing rule to apply. This is necessary
   * due to the poor compositing interface of the SWT toolkit.
   */
  private java.awt.Composite composite;

  /**
   * A HashMap to store the Swt color resources.
   */
  private Map colorsPool = new HashMap();

  /**
   * A HashMap to store the Swt font resources.
   */
  private Map fontsPool = new HashMap();

  /**
   * A HashMap to store the Swt transform resources.
   */
  private Map transformsPool = new HashMap();

  /**
   * A List to store the Swt resources.
   */
  private List resourcePool = new ArrayList();

  /**
   * for keeping track of the state
   */
  private SWTGraphics2D parent;
  private Font savedFont;
  private Color savedColor;
  private Color savedBackground;
  private Shape savedClip;
  private AffineTransform savedTransform;

  /**
   * Creates a new instance.
   *
   * @param gc the graphics context.
   */
  public SWTGraphics2D(final GC gc)
  {
    super();
    this.gc = gc;
    this.hints = new RenderingHints(null);
    this.composite = AlphaComposite.getInstance(AlphaComposite.SRC, 1.0f);
  }

  protected SWTGraphics2D(final GC gc, final SWTGraphics2D parent)
  {
    this(gc);
    this.parent = parent;
    this.savedFont = parent.getFont();
    this.setFont(this.savedFont);
    this.savedColor = parent.getColor();
    this.setColor(this.savedColor);
    this.savedBackground = parent.getBackground();
    this.setBackground(this.savedBackground);
    this.savedClip = parent.getClip();
    this.setClip(this.savedClip);
    this.savedTransform = parent.getTransform();
    this.setTransform(this.savedTransform);

    this.setRenderingHints(parent.getRenderingHints());

    // share the pools between the parent object and this one
    this.colorsPool = parent.colorsPool;
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics#dispose()
  */

  public void dispose()
  {
    if (parent != null)
    {
      this.setFont(this.savedFont);
      this.setColor(this.savedColor);
      this.setBackground(this.savedBackground);
      this.setClip(this.savedClip);
      this.setTransform(this.savedTransform);
      // ignore the shared pools, they will be released once
      // the last Graphics2D
    }
    else
    {
      // the old dispose ...
      disposeResourcePool();
    }
  }


  /* (non-Javadoc)
  * @see java.awt.Graphics#create()
  */

  public SWTGraphics2D create()
  {
    //System.err.println("in SWTGraphics2D.create()");
    return new SWTGraphics2D(this.gc, this);
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics2D#getDeviceConfiguration()
  */

  public GraphicsConfiguration getDeviceConfiguration()
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Returns the current value for the specified hint key, or
   * <code>null</code> if no value is set.
   *
   * @param hintKey the hint key (<code>null</code> permitted).
   * @return The hint value, or <code>null</code>.
   * @see #setRenderingHint(Key, Object)
   */
  public Object getRenderingHint(final Key hintKey)
  {
    return this.hints.get(hintKey);
  }

  /**
   * Sets the value for a rendering hint.  For now, this graphics context
   * ignores all hints.
   *
   * @param hintKey   the key (<code>null</code> not permitted).
   * @param hintValue the value (must be compatible with the specified key).
   * @throws IllegalArgumentException if <code>hintValue</code> is not
   *                                  compatible with the <code>hintKey</code>.
   * @see #getRenderingHint(Key)
   */
  public void setRenderingHint(final Key hintKey, final Object hintValue)
  {
    this.hints.put(hintKey, hintValue);
  }

  /**
   * Returns a copy of the hints collection for this graphics context.
   *
   * @return A copy of the hints collection.
   */
  public RenderingHints getRenderingHints()
  {
    return (RenderingHints) this.hints.clone();
  }

  /**
   * Adds the hints in the specified map to the graphics context, replacing
   * any existing hints.  For now, this graphics context ignores all hints.
   *
   * @param hints the hints (<code>null</code> not permitted).
   * @see #setRenderingHints(Map)
   */
  public void addRenderingHints(final Map hints)
  {
    this.hints.putAll(hints);
  }

  /**
   * Replaces the existing hints with those contained in the specified
   * map.  Note that, for now, this graphics context ignores all hints.
   *
   * @param hints the hints (<code>null</code> not permitted).
   * @see #addRenderingHints(Map)
   */
  public void setRenderingHints(final Map hints)
  {
    if (hints == null)
    {
      throw new NullPointerException("Null 'hints' argument.");
    }
    this.hints = new RenderingHints(hints);
  }

  /**
   * Returns the current paint for this graphics context.
   *
   * @return The current paint.
   * @see #setPaint(Paint)
   */
  public Paint getPaint()
  {
    // TODO: it might be a good idea to keep a reference to the color
    // specified in setPaint() or setColor(), rather than creating a
    // new object every time getPaint() is called.
    return SWTUtils.toAwtColor(this.gc.getForeground());
  }

  /**
   * Sets the paint for this graphics context.  For now, this graphics
   * context only supports instances of {@link Color}.
   *
   * @param paint the paint (<code>null</code> not permitted).
   * @see #getPaint()
   * @see #setColor(Color)
   */
  public void setPaint(final Paint paint)
  {
    if (paint instanceof Color)
    {
      setColor((Color) paint);
    }
    else
    {
      throw new RuntimeException("Can only handle 'Color' at present.");
    }
  }

  /**
   * Returns the current color for this graphics context.
   *
   * @return The current color.
   * @see #setColor(Color)
   */
  public Color getColor()
  {
    // TODO: it might be a good idea to keep a reference to the color
    // specified in setPaint() or setColor(), rather than creating a
    // new object every time getPaint() is called.
    return SWTUtils.toAwtColor(this.gc.getForeground());
  }

  /**
   * Sets the current color for this graphics context.
   *
   * @param color the color.
   * @see #getColor()
   */
  public void setColor(final Color color)
  {
    final org.eclipse.swt.graphics.Color swtColor = getSwtColorFromPool(color);
    this.gc.setForeground(swtColor);
    // handle transparency and compositing.
    if (this.composite instanceof AlphaComposite)
    {
      final AlphaComposite acomp = (AlphaComposite) this.composite;
      switch (acomp.getRule())
      {
        case AlphaComposite.SRC_OVER:
          this.gc.setAlpha((int) (color.getAlpha() * acomp.getAlpha()));
          break;
        default:
          this.gc.setAlpha(color.getAlpha());
          break;
      }
    }
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics2D#setBackground(java.awt.Color)
  */

  public void setBackground(final Color color)
  {
    final org.eclipse.swt.graphics.Color swtColor = getSwtColorFromPool(color);
    this.gc.setBackground(swtColor);
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics2D#getBackground()
  */

  public Color getBackground()
  {
    return SWTUtils.toAwtColor(this.gc.getBackground());
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics#setPaintMode()
  */

  public void setPaintMode()
  {
    // TODO Auto-generated method stub
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics#setXORMode(java.awt.Color)
  */

  public void setXORMode(final Color color)
  {
    // TODO Auto-generated method stub
  }

  /**
   * Returns the current composite.
   *
   * @return The current composite.
   * @see #setComposite(Composite)
   */
  public Composite getComposite()
  {
    return this.composite;
  }

  /**
   * Sets the current composite.  This implementation currently supports
   * only the {@link AlphaComposite} class.
   *
   * @param comp the composite.
   */
  public void setComposite(final Composite comp)
  {
    this.composite = comp;
    if (comp instanceof AlphaComposite)
    {
      final AlphaComposite acomp = (AlphaComposite) comp;
      final int alpha = (int) (acomp.getAlpha() * 0xFF);
      this.gc.setAlpha(alpha);
    }
    else
    {
      System.out.println("warning, can only handle alpha composite at the moment.");
    }
  }

  /**
   * Returns the current stroke for this graphics context.
   *
   * @return The current stroke.
   * @see #setStroke(Stroke)
   */
  public Stroke getStroke()
  {
    return new BasicStroke(this.gc.getLineWidth(), this.gc.getLineCap(),
        this.gc.getLineJoin());
  }

  /**
   * Sets the stroke for this graphics context.  For now, this implementation
   * only recognises the {@link BasicStroke} class.
   *
   * @param stroke the stroke (<code>null</code> not permitted).
   * @see #getStroke()
   */
  public void setStroke(final Stroke stroke)
  {
    if (stroke instanceof BasicStroke)
    {
      final BasicStroke bs = (BasicStroke) stroke;
      // linewidth
      this.gc.setLineWidth((int) bs.getLineWidth());

      // line join
      switch (bs.getLineJoin())
      {
        case BasicStroke.JOIN_BEVEL:
          this.gc.setLineJoin(SWT.JOIN_BEVEL);
          break;
        case BasicStroke.JOIN_MITER:
          this.gc.setLineJoin(SWT.JOIN_MITER);
          break;
        case BasicStroke.JOIN_ROUND:
          this.gc.setLineJoin(SWT.JOIN_ROUND);
          break;
      }

      // line cap
      switch (bs.getEndCap())
      {
        case BasicStroke.CAP_BUTT:
          this.gc.setLineCap(SWT.CAP_FLAT);
          break;
        case BasicStroke.CAP_ROUND:
          this.gc.setLineCap(SWT.CAP_ROUND);
          break;
        case BasicStroke.CAP_SQUARE:
          this.gc.setLineCap(SWT.CAP_SQUARE);
          break;
      }

      // set the line style to solid by default
      this.gc.setLineStyle(SWT.LINE_SOLID);

      // apply dash style if any
      final float[] dashes = bs.getDashArray();
      if (dashes != null)
      {
        final int[] swtDashes = new int[dashes.length];
        for (int i = 0; i < swtDashes.length; i++)
        {
          swtDashes[i] = (int) dashes[i];
        }
        this.gc.setLineDash(swtDashes);
      }
    }
    else
    {
      throw new RuntimeException(
          "Can only handle 'Basic Stroke' at present.");
    }
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics2D#clip(java.awt.Shape)
  */

  public void clip(final Shape s)
  {
    final Path path = toSwtPath(s);
    this.gc.setClipping(path);
    path.dispose();
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics#getClipBounds()
  */

  public Rectangle getClipBounds()
  {
    final org.eclipse.swt.graphics.Rectangle clip = this.gc.getClipping();
    return new Rectangle(clip.x, clip.y, clip.width, clip.height);
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics#clipRect(int, int, int, int)
  */

  public void clipRect(final int x, final int y, final int width, final int height)
  {
    final org.eclipse.swt.graphics.Rectangle clip = this.gc.getClipping();
    clip.intersects(x, y, width, height);
    this.gc.setClipping(clip);
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics#getClip()
  */

  public Shape getClip()
  {
    return SWTUtils.toAwtRectangle(this.gc.getClipping());
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics#setClip(java.awt.Shape)
  */

  public void setClip(final Shape clip)
  {
    if (clip == null)
    {
      return;
    }
    final Path clipPath = toSwtPath(clip);
    this.gc.setClipping(clipPath);
    clipPath.dispose();
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics#setClip(int, int, int, int)
  */

  public void setClip(final int x, final int y, final int width, final int height)
  {
    this.gc.setClipping(x, y, width, height);
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics2D#getTransform()
  */

  public AffineTransform getTransform()
  {
    final Transform swtTransform = new Transform(this.gc.getDevice());
    this.gc.getTransform(swtTransform);
    final AffineTransform awtTransform = toAwtTransform(swtTransform);
    swtTransform.dispose();
    return awtTransform;
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics2D#setTransform(java.awt.geom.AffineTransform)
  */

  public void setTransform(final AffineTransform Tx)
  {
    final Transform transform = getSwtTransformFromPool(Tx);
    this.gc.setTransform(transform);
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics2D#transform(java.awt.geom.AffineTransform)
  */

  public void transform(final AffineTransform Tx)
  {
    final Transform swtTransform = new Transform(this.gc.getDevice());
    this.gc.getTransform(swtTransform);
    swtTransform.multiply(getSwtTransformFromPool(Tx));
    this.gc.setTransform(swtTransform);
    swtTransform.dispose();
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics2D#translate(int, int)
  */

  public void translate(final int x, final int y)
  {
    final Transform swtTransform = new Transform(this.gc.getDevice());
    this.gc.getTransform(swtTransform);
    swtTransform.translate(x, y);
    this.gc.setTransform(swtTransform);
    swtTransform.dispose();
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics2D#translate(double, double)
  */

  public void translate(final double tx, final double ty)
  {
    translate((int) tx, (int) ty);
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics2D#rotate(double)
  */

  public void rotate(final double theta)
  {
    final Transform swtTransform = new Transform(this.gc.getDevice());
    this.gc.getTransform(swtTransform);
    swtTransform.rotate((float) (theta * 180 / Math.PI));
    this.gc.setTransform(swtTransform);
    swtTransform.dispose();
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics2D#rotate(double, double, double)
  */

  public void rotate(final double theta, final double x, final double y)
  {
    // TODO Auto-generated method stub
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics2D#scale(double, double)
  */

  public void scale(final double scaleX, final double scaleY)
  {
    final Transform swtTransform = new Transform(this.gc.getDevice());
    this.gc.getTransform(swtTransform);
    swtTransform.scale((float) scaleX, (float) scaleY);
    this.gc.setTransform(swtTransform);
    swtTransform.dispose();
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics2D#shear(double, double)
  */

  public void shear(final double shearX, final double shearY)
  {
    final Transform swtTransform = new Transform(this.gc.getDevice());
    this.gc.getTransform(swtTransform);
    final Transform shear = new Transform(this.gc.getDevice(), 1.0f, (float) shearX,
        (float) shearY, 1.0f, 0, 0);
    swtTransform.multiply(shear);
    this.gc.setTransform(swtTransform);
    swtTransform.dispose();
  }

  /**
   * Draws the outline of the specified shape using the current stroke and
   * paint settings.
   *
   * @param shape the shape (<code>null</code> not permitted).
   * @see #getPaint()
   * @see #getStroke()
   * @see #fill(Shape)
   */
  public void draw(final Shape shape)
  {
    final Path path = toSwtPath(shape);
    this.gc.drawPath(path);
    path.dispose();
  }

  /**
   * Draws a line from (x1, y1) to (x2, y2) using the current stroke
   * and paint settings.
   *
   * @param x1 the x-coordinate for the starting point.
   * @param y1 the y-coordinate for the starting point.
   * @param x2 the x-coordinate for the ending point.
   * @param y2 the y-coordinate for the ending point.
   * @see #draw(Shape)
   */
  public void drawLine(final int x1, final int y1, final int x2, final int y2)
  {
    this.gc.drawLine(x1, y1, x2, y2);
  }

  /**
   * Draws the outline of the polygon specified by the given points, using
   * the current paint and stroke settings.
   *
   * @param xPoints the x-coordinates.
   * @param yPoints the y-coordinates.
   * @param npoints the number of points in the polygon.
   * @see #draw(Shape)
   */
  public void drawPolygon(final int[] xPoints, final int[] yPoints, final int npoints)
  {
    drawPolyline(xPoints, yPoints, npoints);
    if (npoints > 1)
    {
      this.gc.drawLine(xPoints[npoints - 1], yPoints[npoints - 1],
          xPoints[0], yPoints[0]);
    }
  }

  /**
   * Draws a sequence of connected lines specified by the given points, using
   * the current paint and stroke settings.
   *
   * @param xPoints the x-coordinates.
   * @param yPoints the y-coordinates.
   * @param npoints the number of points in the polygon.
   * @see #draw(Shape)
   */
  public void drawPolyline(final int[] xPoints, final int[] yPoints, final int npoints)
  {
    if (npoints > 1)
    {
      int x0 = xPoints[0];
      int y0 = yPoints[0];
      int x1 = 0;
      int y1 = 0;
      for (int i = 1; i < npoints; i++)
      {
        x1 = xPoints[i];
        y1 = yPoints[i];
        this.gc.drawLine(x0, y0, x1, y1);
        x0 = x1;
        y0 = y1;
      }
    }
  }

  /**
   * Draws an oval that fits within the specified rectangular region.
   *
   * @param x      the x-coordinate.
   * @param y      the y-coordinate.
   * @param width  the frame width.
   * @param height the frame height.
   * @see #fillOval(int, int, int, int)
   * @see #draw(Shape)
   */
  public void drawOval(final int x, final int y, final int width, final int height)
  {
    this.gc.drawOval(x, y, width - 1, height - 1);
  }

  /**
   * Draws an arc that is part of an ellipse that fits within the specified
   * framing rectangle.
   *
   * @param x        the x-coordinate.
   * @param y        the y-coordinate.
   * @param width    the frame width.
   * @param height   the frame height.
   * @param arcStart the arc starting point, in degrees.
   * @param arcAngle the extent of the arc.
   * @see #fillArc(int, int, int, int, int, int)
   */
  public void drawArc(final int x, final int y, final int width, final int height, final int arcStart,
                      final int arcAngle)
  {
    this.gc.drawArc(x, y, width - 1, height - 1, arcStart, arcAngle);
  }

  /**
   * Draws a rectangle with rounded corners that fits within the specified
   * framing rectangle.
   *
   * @param x         the x-coordinate.
   * @param y         the y-coordinate.
   * @param width     the frame width.
   * @param height    the frame height.
   * @param arcWidth  the width of the arc defining the roundedness of the
   *                  rectangle's corners.
   * @param arcHeight the height of the arc defining the roundedness of the
   *                  rectangle's corners.
   * @see #fillRoundRect(int, int, int, int, int, int)
   */
  public void drawRoundRect(final int x, final int y, final int width, final int height,
                            final int arcWidth, final int arcHeight)
  {
    this.gc.drawRoundRectangle(x, y, width - 1, height - 1, arcWidth,
        arcHeight);
  }

  /**
   * Fills the specified shape using the current paint.
   *
   * @param shape the shape (<code>null</code> not permitted).
   * @see #getPaint()
   * @see #draw(Shape)
   */
  public void fill(final Shape shape)
  {
    final Path path = toSwtPath(shape);
    // Note that for consistency with the AWT implementation, it is
    // necessary to switch temporarily the foreground and background
    // colours
    switchColors();
    this.gc.fillPath(path);
    switchColors();
    path.dispose();
  }

  /**
   * Fill a rectangle area on the swt graphic composite.
   * The <code>fillRectangle</code> method of the <code>GC</code>
   * class uses the background color so we must switch colors.
   *
   * @see java.awt.Graphics#fillRect(int, int, int, int)
   */
  public void fillRect(final int x, final int y, final int width, final int height)
  {
    this.switchColors();
    this.gc.fillRectangle(x, y, width, height);
    this.switchColors();
  }

  /**
   * Fills the specified rectangle with the current background colour.
   *
   * @param x      the x-coordinate for the rectangle.
   * @param y      the y-coordinate for the rectangle.
   * @param width  the width.
   * @param height the height.
   * @see #fillRect(int, int, int, int)
   */
  public void clearRect(final int x, final int y, final int width, final int height)
  {
    final Paint saved = getPaint();
    setPaint(getBackground());
    fillRect(x, y, width, height);
    setPaint(saved);
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics#fillPolygon(int[], int[], int)
  */

  public void fillPolygon(final int[] xPoints, final int[] yPoints, final int npoints)
  {
    // TODO Auto-generated method stub
  }

  /**
   * Draws a rectangle with rounded corners that fits within the specified
   * framing rectangle.
   *
   * @param x         the x-coordinate.
   * @param y         the y-coordinate.
   * @param width     the frame width.
   * @param height    the frame height.
   * @param arcWidth  the width of the arc defining the roundedness of the
   *                  rectangle's corners.
   * @param arcHeight the height of the arc defining the roundedness of the
   *                  rectangle's corners.
   * @see #drawRoundRect(int, int, int, int, int, int)
   */
  public void fillRoundRect(final int x, final int y, final int width, final int height,
                            final int arcWidth, final int arcHeight)
  {
    switchColors();
    this.gc.fillRoundRectangle(x, y, width - 1, height - 1, arcWidth,
        arcHeight);
    switchColors();
  }

  /**
   * Fills an oval that fits within the specified rectangular region.
   *
   * @param x      the x-coordinate.
   * @param y      the y-coordinate.
   * @param width  the frame width.
   * @param height the frame height.
   * @see #drawOval(int, int, int, int)
   * @see #fill(Shape)
   */
  public void fillOval(final int x, final int y, final int width, final int height)
  {
    switchColors();
    this.gc.fillOval(x, y, width - 1, height - 1);
    switchColors();
  }

  /**
   * Fills an arc that is part of an ellipse that fits within the specified
   * framing rectangle.
   *
   * @param x        the x-coordinate.
   * @param y        the y-coordinate.
   * @param width    the frame width.
   * @param height   the frame height.
   * @param arcStart the arc starting point, in degrees.
   * @param arcAngle the extent of the arc.
   * @see #drawArc(int, int, int, int, int, int)
   */
  public void fillArc(final int x, final int y, final int width, final int height, final int arcStart,
                      final int arcAngle)
  {
    switchColors();
    this.gc.fillArc(x, y, width - 1, height - 1, arcStart, arcAngle);
    switchColors();
  }

  /**
   * Returns the font in form of an awt font created
   * with the parameters of the font of the swt graphic
   * composite.
   *
   * @see java.awt.Graphics#getFont()
   */
  public Font getFont()
  {
    // retrieve the swt font description in an os indept way
    final FontData[] fontData = this.gc.getFont().getFontData();
    // create a new awt font with the appropiate data
    return SWTUtils.toAwtFont(this.gc.getDevice(), fontData[0], true);
  }

  /**
   * Set the font swt graphic composite from the specified
   * awt font. Be careful that the newly created swt font
   * must be disposed separately.
   *
   * @see java.awt.Graphics#setFont(java.awt.Font)
   */
  public void setFont(final Font font)
  {
    final org.eclipse.swt.graphics.Font swtFont = getSwtFontFromPool(font);
    this.gc.setFont(swtFont);
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics#getFontMetrics(java.awt.Font)
  */

  public FontMetrics getFontMetrics(final Font font)
  {
    return SWTUtils.DUMMY_PANEL.getFontMetrics(font);
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics2D#getFontRenderContext()
  */

  public FontRenderContext getFontRenderContext()
  {
    return new FontRenderContext(
        new AffineTransform(), true, true);
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics2D#drawGlyphVector(java.awt.font.GlyphVector,
  * float, float)
  */

  public void drawGlyphVector(final GlyphVector g, final float x, final float y)
  {
    // TODO Auto-generated method stub

  }

  /**
   * Draws a string on the receiver. note that
   * to be consistent with the awt method,
   * the y has to be modified with the ascent of the font.
   *
   * @see java.awt.Graphics#drawString(java.lang.String, int, int)
   */
  public void drawString(final String text, final int x, final int y)
  {
    final float fm = this.gc.getFontMetrics().getAscent();
    this.gc.drawString(text, x, (int) (y - fm), true);
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics2D#drawString(java.lang.String, float, float)
  */

  public void drawString(final String text, final float x, final float y)
  {
    final float fm = this.gc.getFontMetrics().getAscent();
    this.gc.drawString(text, (int) x, (int) (y - fm), true);
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics2D#drawString(
  * java.text.AttributedCharacterIterator, int, int)
  */

  public void drawString(final AttributedCharacterIterator iterator, final int x, final int y)
  {
    // TODO Auto-generated method stub
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics2D#drawString(
  * java.text.AttributedCharacterIterator, float, float)
  */

  public void drawString(final AttributedCharacterIterator iterator, final float x,
                         final float y)
  {
    // TODO Auto-generated method stub
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics2D#hit(java.awt.Rectangle, java.awt.Shape, boolean)
  */

  public boolean hit(final Rectangle rect, final Shape text, final boolean onStroke)
  {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics#copyArea(int, int, int, int, int, int)
  */

  public void copyArea(final int x, final int y, final int width, final int height, final int dx, final int dy)
  {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
  * @see java.awt.Graphics2D#drawImage(java.awt.Image,
  * java.awt.geom.AffineTransform, java.awt.image.ImageObserver)
  */

  public boolean drawImage(final Image image, final AffineTransform xform,
                           final ImageObserver obs)
  {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics2D#drawImage(java.awt.image.BufferedImage,
  * java.awt.image.BufferedImageOp, int, int)
  */

  public void drawImage(final BufferedImage image, final BufferedImageOp op, final int x,
                        final int y)
  {
    final org.eclipse.swt.graphics.Image im = new org.eclipse.swt.graphics.Image(
        this.gc.getDevice(), SWTUtils.convertToSWT(image));
    this.gc.drawImage(im, x, y);
    im.dispose();
  }

  /**
   * Draws an SWT image with the top left corner of the image aligned to the
   * point (x, y).
   *
   * @param image the image.
   * @param x     the x-coordinate.
   * @param y     the y-coordinate.
   */
  public void drawImage(final org.eclipse.swt.graphics.Image image, final int x, final int y)
  {
    this.gc.drawImage(image, x, y);
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics2D#drawRenderedImage(java.awt.image.RenderedImage,
  * java.awt.geom.AffineTransform)
  */

  public void drawRenderedImage(final RenderedImage image, final AffineTransform xform)
  {
    // TODO Auto-generated method stub
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics2D#drawRenderableImage(
  * java.awt.image.renderable.RenderableImage, java.awt.geom.AffineTransform)
  */

  public void drawRenderableImage(final RenderableImage image,
                                  final AffineTransform xform)
  {
    // TODO Auto-generated method stub

  }

  /**
   * Draws an image with the top left corner aligned to the point (x, y).
   *
   * @param image    the image.
   * @param x        the x-coordinate.
   * @param y        the y-coordinate.
   * @param observer ignored here.
   * @return <code>true</code> if the image has been drawn.
   */
  public boolean drawImage(final Image image, final int x, final int y,
                           final ImageObserver observer)
  {
    final ImageData data = SWTUtils.convertAWTImageToSWT(image);
    if (data == null)
    {
      return false;
    }
    final org.eclipse.swt.graphics.Image im = new org.eclipse.swt.graphics.Image(
        this.gc.getDevice(), data);
    this.gc.drawImage(im, x, y);
    im.dispose();
    return true;
  }

  /**
   * Draws an image with the top left corner aligned to the point (x, y),
   * and scaled to the specified width and height.
   *
   * @param image    the image.
   * @param x        the x-coordinate.
   * @param y        the y-coordinate.
   * @param width    the width for the rendered image.
   * @param height   the height for the rendered image.
   * @param observer ignored here.
   * @return <code>true</code> if the image has been drawn.
   */
  public boolean drawImage(final Image image, final int x, final int y, final int width, final int height,
                           final ImageObserver observer)
  {
    final ImageData data = SWTUtils.convertAWTImageToSWT(image);
    if (data == null)
    {
      return false;
    }
    final org.eclipse.swt.graphics.Image im = new org.eclipse.swt.graphics.Image(
        this.gc.getDevice(), data);
    final org.eclipse.swt.graphics.Rectangle bounds = im.getBounds();
    this.gc.drawImage(im, 0, 0, bounds.width, bounds.height, x, y, width,
        height);
    im.dispose();
    return true;
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics#drawImage(java.awt.Image, int, int,
  * java.awt.Color, java.awt.image.ImageObserver)
  */

  public boolean drawImage(final Image image, final int x, final int y, final Color bgcolor,
                           final ImageObserver observer)
  {
    if (image == null)
    {
      throw new IllegalArgumentException("Null 'image' argument.");
    }
    final int w = image.getWidth(null);
    final int h = image.getHeight(null);
    if (w == -1 || h == -1)
    {
      return false;
    }
    final Paint savedPaint = getPaint();
    fill(new Rectangle2D.Double(x, y, w, h));
    setPaint(savedPaint);
    return drawImage(image, x, y, observer);
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int,
  * java.awt.Color, java.awt.image.ImageObserver)
  */

  public boolean drawImage(final Image image, final int x, final int y, final int width, final int height,
                           final Color bgcolor, final ImageObserver observer)
  {
    if (image == null)
    {
      throw new IllegalArgumentException("Null 'image' argument.");
    }
    final int w = image.getWidth(null);
    final int h = image.getHeight(null);
    if (w == -1 || h == -1)
    {
      return false;
    }
    final Paint savedPaint = getPaint();
    fill(new Rectangle2D.Double(x, y, w, h));
    setPaint(savedPaint);
    return drawImage(image, x, y, width, height, observer);
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int,
  * int, int, int, int, java.awt.image.ImageObserver)
  */

  public boolean drawImage(final Image image, final int dx1, final int dy1, final int dx2, final int dy2,
                           final int sx1, final int sy1, final int sx2, final int sy2, final ImageObserver observer)
  {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
  * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int,
  * int, int, int, int, java.awt.Color, java.awt.image.ImageObserver)
  */

  public boolean drawImage(final Image image, final int dx1, final int dy1, final int dx2, final int dy2,
                           final int sx1, final int sy1, final int sx2, final int sy2, final Color bgcolor,
                           final ImageObserver observer)
  {
    // TODO Auto-generated method stub
    return false;
  }


  /**
   * Add given swt resource to the resource pool. All resources added
   * to the resource pool will be disposed when {@link #dispose()} is called.
   *
   * @param resource the resource to add to the pool.
   * @return the swt <code>Resource</code> just added.
   */
  private Resource addToResourcePool(final Resource resource)
  {
    this.resourcePool.add(resource);
    return resource;
  }

  /**
   * Dispose the resource pool.
   */
  private void disposeResourcePool()
  {
    for (Iterator it = this.resourcePool.iterator(); it.hasNext();)
    {
      final Resource resource = (Resource) it.next();
      resource.dispose();
    }
    this.fontsPool.clear();
    this.colorsPool.clear();
    this.transformsPool.clear();
    this.resourcePool.clear();
  }

  /**
   * Internal method to convert a AWT font object into
   * a SWT font resource. If a corresponding SWT font
   * instance is already in the pool, it will be used
   * instead of creating a new one.
   *
   * @param font The AWT font to convert.
   * @return The SWT font instance.
   */
  private org.eclipse.swt.graphics.Font getSwtFontFromPool(final Font font)
  {
    org.eclipse.swt.graphics.Font swtFont = (org.eclipse.swt.graphics.Font)
        this.fontsPool.get(font);
    if (swtFont == null)
    {
      swtFont = new org.eclipse.swt.graphics.Font(this.gc.getDevice(),
          SWTUtils.toSwtFontData(this.gc.getDevice(), font, true));
      addToResourcePool(swtFont);
      this.fontsPool.put(font, swtFont);
    }
    return swtFont;
  }

  /**
   * Internal method to convert a AWT color object into
   * a SWT color resource. If a corresponding SWT color
   * instance is already in the pool, it will be used
   * instead of creating a new one.
   *
   * @param awtColor The AWT color to convert.
   * @return A SWT color instance.
   */
  private org.eclipse.swt.graphics.Color getSwtColorFromPool(final Color awtColor)
  {
    org.eclipse.swt.graphics.Color swtColor =
        (org.eclipse.swt.graphics.Color)
            // we can't use the following valueOf() method, because it
            // won't compile with JDK1.4
            // this.colorsPool.get(Integer.valueOf(awtColor.getRGB()));
            this.colorsPool.get(new Integer(awtColor.getRGB()));
    if (swtColor == null)
    {
      swtColor = SWTUtils.toSwtColor(this.gc.getDevice(), awtColor);
      addToResourcePool(swtColor);
      // see comment above
      //this.colorsPool.put(Integer.valueOf(awtColor.getRGB()), swtColor);
      this.colorsPool.put(new Integer(awtColor.getRGB()), swtColor);
    }
    return swtColor;
  }

  /**
   * Internal method to convert a AWT transform object into
   * a SWT transform resource. If a corresponding SWT transform
   * instance is already in the pool, it will be used
   * instead of creating a new one.
   *
   * @param awtTransform The AWT transform to convert.
   * @return A SWT transform instance.
   */
  private Transform getSwtTransformFromPool(final AffineTransform awtTransform)
  {
    Transform t = (Transform) this.transformsPool.get(awtTransform);
    if (t == null)
    {
      t = new Transform(this.gc.getDevice());
      final double[] matrix = new double[6];
      awtTransform.getMatrix(matrix);
      t.setElements((float) matrix[0], (float) matrix[1],
          (float) matrix[2], (float) matrix[3],
          (float) matrix[4], (float) matrix[5]);
      addToResourcePool(t);
      this.transformsPool.put(awtTransform, t);
    }
    return t;
  }

  /**
   * Perform a switch between foreground and background
   * color of gc. This is needed for consistency with
   * the awt behaviour, and is required notably for the
   * filling methods.
   */
  private void switchColors()
  {
    final org.eclipse.swt.graphics.Color bg = this.gc.getBackground();
    final org.eclipse.swt.graphics.Color fg = this.gc.getForeground();
    this.gc.setBackground(fg);
    this.gc.setForeground(bg);
  }

  /**
   * Converts an AWT <code>Shape</code> into a SWT <code>Path</code>.
   *
   * @param shape the shape (<code>null</code> not permitted).
   * @return The path.
   */
  private Path toSwtPath(final Shape shape)
  {
    final float[] coords = new float[6];
    final Path path = new Path(this.gc.getDevice());
    final PathIterator pit = shape.getPathIterator(null);
    while (!pit.isDone())
    {
      final int type = pit.currentSegment(coords);
      switch (type)
      {
        case (PathIterator.SEG_MOVETO):
          path.moveTo(coords[0], coords[1]);
          break;
        case (PathIterator.SEG_LINETO):
          path.lineTo(coords[0], coords[1]);
          break;
        case (PathIterator.SEG_QUADTO):
          path.quadTo(coords[0], coords[1], coords[2], coords[3]);
          break;
        case (PathIterator.SEG_CUBICTO):
          path.cubicTo(coords[0], coords[1], coords[2],
              coords[3], coords[4], coords[5]);
          break;
        case (PathIterator.SEG_CLOSE):
          path.close();
          break;
        default:
          break;
      }
      pit.next();
    }
    return path;
  }

  /**
   * Converts an SWT transform into the equivalent AWT transform.
   *
   * @param swtTransform the SWT transform.
   * @return The AWT transform.
   */
  private AffineTransform toAwtTransform(final Transform swtTransform)
  {
    final float[] elements = new float[6];
    swtTransform.getElements(elements);
    return new AffineTransform(elements);
  }

}

