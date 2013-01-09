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
 * (C) Copyright 2000-2007, by Object Refinery Limited and Contributors.
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
 * -------------
 * SWTUtils.java
 * -------------
 * (C) Copyright 2006, 2007, by Henry Proudhon and Contributors.
 *
 * Original Author:  Henry Proudhon (henry.proudhon AT ensmp.fr);
 * Contributor(s):   Rainer Blessing;
 *                   David Gilbert (david.gilbert@object-refinery.com);
 *                   Christoph Beck.
 *
 * Changes
 * -------
 * 01-Aug-2006 : New class (HP);
 * 16-Jan-2007 : Use FontData.getHeight() instead of direct field access (RB); 
 * 31-Jan-2007 : Moved the dummy JPanel from SWTGraphics2D.java, 
 *               added a new convert method for mouse events (HP);
 * 12-Jul-2007 : Improved the mouse event conversion with buttons 
 *               and modifiers handling, patch sent by Christoph Beck (HP);
 * 27-Aug-2007 : Modified toAwtMouseEvent signature (HP);
 * 27-Nov-2007 : Moved convertToSWT() method from SWTGraphics2D and added
 *               convertAWTImageToSWT() (DG);
 * 
 */

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import javax.swing.JPanel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Utility class gathering some useful and general method.
 * Mainly convert forth and back graphical stuff between
 * awt and swt.
 */
public class SWTUtils
{
  private SWTUtils()
  {
  }

  private static final String Az = "ABCpqr";

  /**
   * A dummy JPanel used to provide font metrics.
   */
  protected static final JPanel DUMMY_PANEL = new JPanel();

  /**
   * Create a <code>FontData</code> object which encapsulate
   * the essential data to create a swt font. The data is taken
   * from the provided awt Font.
   * <p>Generally speaking, given a font size, the returned swt font
   * will display differently on the screen than the awt one.
   * Because the SWT toolkit use native graphical resources whenever
   * it is possible, this fact is platform dependent. To address
   * this issue, it is possible to enforce the method to return
   * a font with the same size (or at least as close as possible)
   * as the awt one.
   * <p>When the object is no more used, the user must explicitly
   * call the dispose method on the returned font to free the
   * operating system resources (the garbage collector won't do it).
   *
   * @param device         The swt device to draw on (display or gc device).
   * @param font           The awt font from which to get the data.
   * @param ensureSameSize A boolean used to enforce the same size
   *                       (in pixels) between the awt font and the newly created swt font.
   * @return a <code>FontData</code> object.
   */
  public static FontData toSwtFontData(final Device device, final java.awt.Font font,
                                       final boolean ensureSameSize)
  {
    final FontData fontData = new FontData();
    fontData.setName(font.getFamily());
    int style = SWT.NORMAL;
    switch (font.getStyle())
    {
      case java.awt.Font.PLAIN:
        style |= SWT.NORMAL;
        break;
      case java.awt.Font.BOLD:
        style |= SWT.BOLD;
        break;
      case java.awt.Font.ITALIC:
        style |= SWT.ITALIC;
        break;
      case (java.awt.Font.ITALIC + java.awt.Font.BOLD):
        style |= SWT.ITALIC | SWT.BOLD;
        break;
    }
    fontData.setStyle(style);
    // convert the font size (in pt for awt) to height in pixels for swt
    int height = (int) Math.round(font.getSize() * 72.0
        / device.getDPI().y);
    fontData.setHeight(height);
    // hack to ensure the newly created swt fonts will be rendered with the
    // same height as the awt one
    if (ensureSameSize)
    {
      final GC tmpGC = new GC(device);
      Font tmpFont = new Font(device, fontData);
      tmpGC.setFont(tmpFont);
      if (tmpGC.textExtent(Az).x
          > DUMMY_PANEL.getFontMetrics(font).stringWidth(Az))
      {
        while (tmpGC.textExtent(Az).x
            > DUMMY_PANEL.getFontMetrics(font).stringWidth(Az))
        {
          tmpFont.dispose();
          height--;
          fontData.setHeight(height);
          tmpFont = new Font(device, fontData);
          tmpGC.setFont(tmpFont);
        }
      }
      else if (tmpGC.textExtent(Az).x
          < DUMMY_PANEL.getFontMetrics(font).stringWidth(Az))
      {
        while (tmpGC.textExtent(Az).x
            < DUMMY_PANEL.getFontMetrics(font).stringWidth(Az))
        {
          tmpFont.dispose();
          height++;
          fontData.setHeight(height);
          tmpFont = new Font(device, fontData);
          tmpGC.setFont(tmpFont);
        }
      }
      tmpFont.dispose();
      tmpGC.dispose();
    }
    return fontData;
  }

  /**
   * Create an awt font by converting as much information
   * as possible from the provided swt <code>FontData</code>.
   * <p>Generally speaking, given a font size, an swt font will
   * display differently on the screen than the corresponding awt
   * one. Because the SWT toolkit use native graphical ressources whenever
   * it is possible, this fact is platform dependent. To address
   * this issue, it is possible to enforce the method to return
   * an awt font with the same height as the swt one.
   *
   * @param device         The swt device being drawn on (display or gc device).
   * @param fontData       The swt font to convert.
   * @param ensureSameSize A boolean used to enforce the same size
   *                       (in pixels) between the swt font and the newly created awt font.
   * @return An awt font converted from the provided swt font.
   */
  public static java.awt.Font toAwtFont(final Device device, final FontData fontData,
                                        final boolean ensureSameSize)
  {
    final int style;
    switch (fontData.getStyle())
    {
      case SWT.NORMAL:
        style = java.awt.Font.PLAIN;
        break;
      case SWT.ITALIC:
        style = java.awt.Font.ITALIC;
        break;
      case SWT.BOLD:
        style = java.awt.Font.BOLD;
        break;
      default:
        style = java.awt.Font.PLAIN;
        break;
    }
    int height = (int) Math.round(fontData.getHeight() * device.getDPI().y
        / 72.0);
    // hack to ensure the newly created awt fonts will be rendered with the
    // same height as the swt one
    if (ensureSameSize)
    {
      final GC tmpGC = new GC(device);
      final Font tmpFont = new Font(device, fontData);
      tmpGC.setFont(tmpFont);
      final JPanel DUMMY_PANEL = new JPanel();
      java.awt.Font tmpAwtFont = new java.awt.Font(fontData.getName(),
          style, height);
      if (DUMMY_PANEL.getFontMetrics(tmpAwtFont).stringWidth(Az)
          > tmpGC.textExtent(Az).x)
      {
        while (DUMMY_PANEL.getFontMetrics(tmpAwtFont).stringWidth(Az)
            > tmpGC.textExtent(Az).x)
        {
          height--;
          tmpAwtFont = new java.awt.Font(fontData.getName(), style,
              height);
        }
      }
      else if (DUMMY_PANEL.getFontMetrics(tmpAwtFont).stringWidth(Az)
          < tmpGC.textExtent(Az).x)
      {
        while (DUMMY_PANEL.getFontMetrics(tmpAwtFont).stringWidth(Az)
            < tmpGC.textExtent(Az).x)
        {
          height++;
          tmpAwtFont = new java.awt.Font(fontData.getName(), style,
              height);
        }
      }
      tmpFont.dispose();
      tmpGC.dispose();
    }
    return new java.awt.Font(fontData.getName(), style, height);
  }

  /**
   * Create an awt font by converting as much information
   * as possible from the provided swt <code>Font</code>.
   *
   * @param device The swt device to draw on (display or gc device).
   * @param font   The swt font to convert.
   * @return An awt font converted from the provided swt font.
   */
  public static java.awt.Font toAwtFont(final Device device, final Font font)
  {
    final FontData fontData = font.getFontData()[0];
    return toAwtFont(device, fontData, true);
  }

  /**
   * Creates an awt color instance to match the rgb values
   * of the specified swt color.
   *
   * @param color The swt color to match.
   * @return an awt color abject.
   */
  public static java.awt.Color toAwtColor(final Color color)
  {
    return new java.awt.Color(color.getRed(), color.getGreen(),
        color.getBlue());
  }

  /**
   * Creates a swt color instance to match the rgb values
   * of the specified awt paint. For now, this method test
   * if the paint is a color and then return the adequate
   * swt color. Otherwise plain black is assumed.
   *
   * @param device The swt device to draw on (display or gc device).
   * @param paint  The awt color to match.
   * @return a swt color object.
   */
  public static Color toSwtColor(final Device device, final java.awt.Paint paint)
  {
    final java.awt.Color color;
    if (paint instanceof java.awt.Color)
    {
      color = (java.awt.Color) paint;
    }
    else
    {
      try
      {
        throw new Exception("only color is supported at present... "
            + "setting paint to uniform black color");
      }
      catch (Exception e)
      {
        e.printStackTrace();
        color = new java.awt.Color(0, 0, 0);
      }
    }
    return new org.eclipse.swt.graphics.Color(device,
        color.getRed(), color.getGreen(), color.getBlue());
  }

  /**
   * Creates a swt color instance to match the rgb values
   * of the specified awt color. alpha channel is not supported.
   * Note that the dispose method will need to be called on the
   * returned object.
   *
   * @param device The swt device to draw on (display or gc device).
   * @param color  The awt color to match.
   * @return a swt color object.
   */
  public static Color toSwtColor(final Device device, final java.awt.Color color)
  {
    return new org.eclipse.swt.graphics.Color(device,
        color.getRed(), color.getGreen(), color.getBlue());
  }

  /**
   * Transform an awt Rectangle2d instance into a swt one.
   * The coordinates are rounded to integer for the swt object.
   *
   * @param rect2d The awt rectangle to map.
   * @return an swt <code>Rectangle</code> object.
   */
  public static Rectangle toSwtRectangle(final Rectangle2D rect2d)
  {
    return new Rectangle(
        (int) Math.round(rect2d.getMinX()),
        (int) Math.round(rect2d.getMinY()),
        (int) Math.round(rect2d.getWidth()),
        (int) Math.round(rect2d.getHeight()));
  }

  /**
   * Transform a swt Rectangle instance into an awt one.
   *
   * @param rect the swt <code>Rectangle</code>
   * @return a Rectangle2D.Double instance with
   *         the eappropriate location and size.
   */
  public static Rectangle2D toAwtRectangle(final Rectangle rect)
  {
    final Rectangle2D rect2d = new Rectangle2D.Double();
    rect2d.setRect(rect.x, rect.y, rect.width, rect.height);
    return rect2d;
  }

  /**
   * Returns an AWT point with the same coordinates as the specified
   * SWT point.
   *
   * @param p the SWT point (<code>null</code> not permitted).
   * @return An AWT point with the same coordinates as <code>p</code>.
   * @see #toSwtPoint(java.awt.Point)
   */
  public static Point2D toAwtPoint(final Point p)
  {
    return new java.awt.Point(p.x, p.y);
  }

  /**
   * Returns an SWT point with the same coordinates as the specified
   * AWT point.
   *
   * @param p the AWT point (<code>null</code> not permitted).
   * @return An SWT point with the same coordinates as <code>p</code>.
   * @see #toAwtPoint(Point)
   */
  public static Point toSwtPoint(final java.awt.Point p)
  {
    return new Point(p.x, p.y);
  }

  /**
   * Returns an SWT point with the same coordinates as the specified AWT
   * point (rounded to integer values).
   *
   * @param p the AWT point (<code>null</code> not permitted).
   * @return An SWT point with the same coordinates as <code>p</code>.
   * @see #toAwtPoint(Point)
   */
  public static Point toSwtPoint(final java.awt.geom.Point2D p)
  {
    return new Point((int) Math.round(p.getX()),
        (int) Math.round(p.getY()));
  }

  /**
   * Creates an AWT <code>MouseEvent</code> from a swt event.
   * This method helps passing SWT mouse event to awt components.
   *
   * @param event The swt event.
   * @return A AWT mouse event based on the given SWT event.
   */
  public static MouseEvent toAwtMouseEvent(final org.eclipse.swt.events.MouseEvent event)
  {
    int button = MouseEvent.NOBUTTON;
    switch (event.button)
    {
      case 1:
        button = MouseEvent.BUTTON1;
        break;
      case 2:
        button = MouseEvent.BUTTON2;
        break;
      case 3:
        button = MouseEvent.BUTTON3;
        break;
    }
    int modifiers = 0;
    if ((event.stateMask & SWT.CTRL) != 0)
    {
      modifiers |= InputEvent.CTRL_DOWN_MASK;
    }
    if ((event.stateMask & SWT.SHIFT) != 0)
    {
      modifiers |= InputEvent.SHIFT_DOWN_MASK;
    }
    if ((event.stateMask & SWT.ALT) != 0)
    {
      modifiers |= InputEvent.ALT_DOWN_MASK;
    }
    return new MouseEvent(DUMMY_PANEL, event.hashCode(),
        event.time, modifiers, event.x, event.y, 1, false, button);
  }

  /**
   * Converts an AWT image to SWT.
   *
   * @param image the image (<code>null</code> not permitted).
   * @return Image data.
   */
  public static ImageData convertAWTImageToSWT(final Image image)
  {
    if (image == null)
    {
      throw new IllegalArgumentException("Null 'image' argument.");
    }
    final int w = image.getWidth(null);
    final int h = image.getHeight(null);
    if (w == -1 || h == -1)
    {
      return null;
    }
    final BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    final Graphics g = bi.getGraphics();
    g.drawImage(image, 0, 0, null);
    g.dispose();
    return convertToSWT(bi);
  }

  /**
   * Converts a buffered image to SWT <code>ImageData</code>.
   *
   * @param bufferedImage the buffered image (<code>null</code> not
   *                      permitted).
   * @return The image data.
   */
  public static ImageData convertToSWT(final BufferedImage bufferedImage)
  {
    if (bufferedImage.getColorModel() instanceof DirectColorModel)
    {
      final DirectColorModel colorModel
          = (DirectColorModel) bufferedImage.getColorModel();
      final PaletteData palette = new PaletteData(colorModel.getRedMask(),
          colorModel.getGreenMask(), colorModel.getBlueMask());
      final ImageData data = new ImageData(bufferedImage.getWidth(),
          bufferedImage.getHeight(), colorModel.getPixelSize(),
          palette);
      final WritableRaster raster = bufferedImage.getRaster();
      final int[] pixelArray = new int[3];
      for (int y = 0; y < data.height; y++)
      {
        for (int x = 0; x < data.width; x++)
        {
          raster.getPixel(x, y, pixelArray);
          final int pixel = palette.getPixel(new RGB(pixelArray[0],
              pixelArray[1], pixelArray[2]));
          data.setPixel(x, y, pixel);
        }
      }
      return data;
    }
    else if (bufferedImage.getColorModel() instanceof IndexColorModel)
    {
      final IndexColorModel colorModel = (IndexColorModel)
          bufferedImage.getColorModel();
      final int size = colorModel.getMapSize();
      final byte[] reds = new byte[size];
      final byte[] greens = new byte[size];
      final byte[] blues = new byte[size];
      colorModel.getReds(reds);
      colorModel.getGreens(greens);
      colorModel.getBlues(blues);
      final RGB[] rgbs = new RGB[size];
      for (int i = 0; i < rgbs.length; i++)
      {
        rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF,
            blues[i] & 0xFF);
      }
      final PaletteData palette = new PaletteData(rgbs);
      final ImageData data = new ImageData(bufferedImage.getWidth(),
          bufferedImage.getHeight(), colorModel.getPixelSize(),
          palette);
      data.transparentPixel = colorModel.getTransparentPixel();
      final WritableRaster raster = bufferedImage.getRaster();
      final int[] pixelArray = new int[1];
      for (int y = 0; y < data.height; y++)
      {
        for (int x = 0; x < data.width; x++)
        {
          raster.getPixel(x, y, pixelArray);
          data.setPixel(x, y, pixelArray[0]);
        }
      }
      return data;
    }
    return null;
  }

}

