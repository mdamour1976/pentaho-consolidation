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
* Copyright (c) 2001 - 2013 Object Refinery Ltd, Pentaho Corporation and Contributors..  All rights reserved.
*/

package org.pentaho.reporting.engine.classic.core.util.beans;

/**
 * A class that handles the conversion of {@link Boolean} attributes to and from their {@link String} representation.
 *
 * @author Thomas Morgner
 */
public class BooleanValueConverter implements ValueConverter
{

  /**
   * Creates a new value converter.
   */
  public BooleanValueConverter()
  {
  }

  /**
   * Converts the attribute to a string.
   *
   * @param o the attribute ({@link Boolean} expected).
   * @return A string representing the {@link Boolean} value.
   */
  public String toAttributeValue(final Object o) throws BeanException
  {
    if (o == null)
    {
      throw new NullPointerException();
    }
    if (o instanceof Boolean)
    {
      return o.toString();
    }
    throw new BeanException("Failed to convert object of type " + o.getClass() + ": Not a boolean.");
  }

  /**
   * Converts a string to a {@link Boolean}.
   *
   * @param s the string.
   * @return a {@link Boolean}.
   */
  public Object toPropertyValue(final String s)
  {
    if (s == null)
    {
      throw new NullPointerException();
    }
    return Boolean.valueOf(s.trim());
  }
}
