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

package org.pentaho.reporting.engine.classic.core.layout.process.util;

import org.pentaho.reporting.engine.classic.core.layout.model.LayoutNodeTypes;
import org.pentaho.reporting.engine.classic.core.layout.model.LogicalPageBox;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderBox;

public class MinorAxisNodeContextPool extends StackedObjectPool<MinorAxisNodeContext>
{
  public MinorAxisNodeContextPool()
  {
  }

  public MinorAxisNodeContext create()
  {
    return new MinorAxisNodeContext(this);
  }

  public MinorAxisNodeContext createContext(final RenderBox box,
                                            final MinorAxisNodeContext context,
                                            final boolean blockLevelNode)
  {
    final MinorAxisNodeContext nodeContext;
    final int nodeType = box.getNodeType();
    final boolean horizontal;
    if (nodeType == LayoutNodeTypes.TYPE_BOX_LOGICALPAGE)
    {
      nodeContext = new MinorAxisLogicalPageContext((LogicalPageBox) box);
      horizontal = false;
    }
    else
    {
      nodeContext = get();
      nodeContext.reuseParent(context);

      final int layoutNodeType = box.getLayoutNodeType();
      if ((layoutNodeType & LayoutNodeTypes.MASK_BOX_ROW) == LayoutNodeTypes.MASK_BOX_ROW ||
          (layoutNodeType == LayoutNodeTypes.TYPE_BOX_TABLE_ROW))
      {
        horizontal = true;
      }
      else
      {
        horizontal = false;
      }
    }

    nodeContext.reuse(horizontal, blockLevelNode, box.isBoxOverflowX(), (nodeType != LayoutNodeTypes.TYPE_BOX_AUTOLAYOUT));
    return nodeContext;
  }
}
