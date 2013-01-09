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
 * Copyright (c) 2001 - 2009 Object Refinery Ltd, Pentaho Corporation and Contributors.  All rights reserved.
 */

package org.pentaho.reporting.engine.classic.extensions.parsers.reportdesigner.model;

public class Guideline
{
  private boolean active;
  private double position;

  public Guideline()
  {
  }

  public boolean isActive()
  {
    return active;
  }

  public void setActive(final boolean active)
  {
    this.active = active;
  }

  public double getPosition()
  {
    return position;
  }

  public void setPosition(final double position)
  {
    this.position = position;
  }

  public String externalize()
  {
    return "(" + active + ',' + position + ')';
  }

  public String toString()
  {
    return "org.pentaho.reporting.engine.classic.extensions.parsers.reportdesigner.model.Guideline{" +
        "active=" + active +
        ", position=" + position +
        '}';
  }
}
