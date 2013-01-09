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
 * Copyright (c) 2001 - 2009 Object Refinery Ltd, Pentaho Corporation and Contributors..  All rights reserved.
 */

package org.pentaho.reporting.engine.classic.core.modules.parser.bundle.layout;

import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.libraries.xmlns.parser.AbstractReadHandlerFactory;

@Deprecated
public class BandedRootElementReadHandlerFactory extends AbstractReadHandlerFactory<ElementReadHandler>
{
  private static final String PREFIX_SELECTOR =
      "org.pentaho.reporting.engine.classic.core.modules.parser.bundle.banded-root-element-factory-prefix.";

  private static BandedRootElementReadHandlerFactory readHandlerFactory;

  public static synchronized BandedRootElementReadHandlerFactory getInstance()
  {
    if (readHandlerFactory == null)
    {
      readHandlerFactory = new BandedRootElementReadHandlerFactory();
      readHandlerFactory.configureGlobal(ClassicEngineBoot.getInstance().getGlobalConfig(), PREFIX_SELECTOR);
    }
    return readHandlerFactory;
  }

  private BandedRootElementReadHandlerFactory()
  {
  }

  protected Class<ElementReadHandler> getTargetClass()
  {
    return ElementReadHandler.class;
  }
}
