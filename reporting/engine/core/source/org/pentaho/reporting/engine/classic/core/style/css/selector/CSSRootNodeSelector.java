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

package org.pentaho.reporting.engine.classic.core.style.css.selector;

import java.io.Serializable;

import org.pentaho.reporting.engine.classic.core.style.css.namespaces.NamespaceCollection;
import org.w3c.css.sac.SimpleSelector;

public class CSSRootNodeSelector extends AbstractSelector implements SimpleSelector, Serializable
{
  public CSSRootNodeSelector()
  {
  }

  protected SelectorWeight createWeight()
  {
    return new SelectorWeight(0, 0, 0, 1);
  }

  /**
   * An integer indicating the type of <code>Selector</code>
   */
  public short getSelectorType()
  {
    return SAC_ROOT_NODE_SELECTOR;
  }

  public String print(final NamespaceCollection namespaces)
  {
    // todo: Not yet supported by parser
    return ":root";
  }
}
