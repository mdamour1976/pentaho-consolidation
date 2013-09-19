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
 * Copyright (c) 2007 - 2009 Pentaho Corporation and Contributors.  All rights reserved.
 */

package org.pentaho.reporting.libraries.css.resolver.tokens.statics;

import java.text.Format;

import org.pentaho.reporting.libraries.css.resolver.tokens.types.FormattedTextType;

/**
 * Creation-Date: 04.07.2006, 20:16:16
 *
 * @author Thomas Morgner
 */
public class FormattedContentToken extends StaticToken
        implements FormattedTextType
{
  private Object original;
  private Format format;
  private String text;

  public FormattedContentToken(final Object original,
                               final Format format,
                               final String text)
  {
    this.format = format;
    this.original = original;
    this.text = text;
  }

  public Object getOriginal()
  {
    return original;
  }

  public Format getFormat()
  {
    return format;
  }

  public String getText()
  {
    return text;
  }
}
