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
 * Copyright (c) 2008 - 2009 Pentaho Corporation, .  All rights reserved.
 */

package org.pentaho.reporting.engine.classic.extensions.parsers.reportdesigner.converter;

import java.net.URL;
import java.net.MalformedURLException;

import org.xml.sax.Locator;
import org.pentaho.reporting.libraries.xmlns.parser.ParseException;

public class URLConverter implements ObjectConverter
{
  public URLConverter()
  {
  }

  public Object convertFromString(final String s, final Locator locator) throws ParseException
  {
    try
    {
      return new URL(s);
    }
    catch (MalformedURLException e)
    {
      throw new ParseException(e.getMessage());
    }
  }
}
