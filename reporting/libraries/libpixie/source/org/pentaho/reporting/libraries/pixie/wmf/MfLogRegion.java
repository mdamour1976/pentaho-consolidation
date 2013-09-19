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
 * Copyright (c) 2000 - 2009 Pentaho Corporation, Object Refinery Limited and Contributors.  All rights reserved.
 */

package org.pentaho.reporting.libraries.pixie.wmf;

import java.awt.Rectangle;

/**
 * A Wmf logical region definition.
 */
public class MfLogRegion implements WmfObject
{
  private int x;
  private int y;
  private int w;
  private int h;

  public int getType()
  {
    return OBJ_REGION;
  }

  public MfLogRegion()
  {
  }

  public void setBounds(final int x, final int y, final int w, final int h)
  {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
  }

  public Rectangle getBounds()
  {
    return new Rectangle(x, y, w, h);
  }
}
