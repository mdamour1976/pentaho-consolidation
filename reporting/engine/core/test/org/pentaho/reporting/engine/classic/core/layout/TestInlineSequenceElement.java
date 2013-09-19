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
 * Copyright (c) 2005-2011 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.reporting.engine.classic.core.layout;

import org.pentaho.reporting.engine.classic.core.layout.model.RenderNode;
import org.pentaho.reporting.engine.classic.core.layout.process.layoutrules.InlineSequenceElement;

class TestInlineSequenceElement implements InlineSequenceElement
{
  private Classification classification;

  TestInlineSequenceElement(final Classification classification)
  {
    this.classification = classification;
  }

  public long getMinimumWidth(final RenderNode node)
  {
    return 10;
  }

  public long getMaximumWidth(final RenderNode node)
  {
    return 10;
  }

  public boolean isPreserveWhitespace(final RenderNode node)
  {
    return false;
  }

  public int getClassification()
  {
    return classification.ordinal();
  }

  public Classification getType()
  {
    return classification;
  }
}
