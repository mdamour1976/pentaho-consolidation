/*!
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
* Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
*/

package org.pentaho.reporting.engine.classic.core.layout.build;

import org.pentaho.reporting.engine.classic.core.InvalidReportStateException;
import org.pentaho.reporting.engine.classic.core.layout.model.LayoutNodeTypes;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderBox;
import org.pentaho.reporting.engine.classic.core.states.ReportStateKey;

/**
 * A layout context that delays adding the content to the parent box until the content is closed and
 * that only adds the content if the element is not empty.
 */
public class DefaultLayoutModelBuilderContext implements LayoutModelBuilderContext, Cloneable
{
  private LayoutModelBuilderContext parent;
  private RenderBox renderBox;
  private boolean empty;
  private boolean commited;
  private boolean keepWrapperBoxAlive;
  private boolean autoGeneratedWrapperBox;

  public DefaultLayoutModelBuilderContext(final LayoutModelBuilderContext parent,
                                          final RenderBox renderBox)
  {
    if (renderBox == null)
    {
      throw new NullPointerException();
    }
    this.parent = parent;
    this.renderBox = renderBox;
    this.empty = true;

    if (renderBox.getNodeType() == LayoutNodeTypes.TYPE_BOX_AUTOLAYOUT)
    {
      commitAsEmpty();
    }
  }

  public RenderBox getRenderBox()
  {
    return renderBox;
  }

  public LayoutModelBuilderContext getParent()
  {
    return parent;
  }

  public boolean isEmpty()
  {
    return empty;
  }

  public boolean mergeSection(final ReportStateKey stateKey)
  {
    return false;
  }

  public void setEmpty(final boolean empty)
  {
    if (empty == true && this.empty == false)
    {
      throw new IllegalStateException();
    }
    this.empty = empty;
    if (empty == false)
    {
      if (parent != null && renderBox.getParent() == null)
      {
        if (commited == false)
        {
          parent.addChild(renderBox);
          commited = true;
        }
        parent.setEmpty(false);
      }
    }
  }

  public void commitAsEmpty()
  {
    if (empty == false)
    {
      return;
    }
    
    if (parent != null && renderBox.getParent() == null)
    {
      if (commited == false)
      {
        parent.addChild(renderBox);
        commited = true;
      }
    }
  }

  public boolean isKeepWrapperBoxAlive()
  {
    return keepWrapperBoxAlive;
  }

  /**
   * A post-fix box stays open after the origin-box is closed.
   *
   * @param keepWrapperBoxAlive
   */
  public void setKeepWrapperBoxAlive(final boolean keepWrapperBoxAlive)
  {
    this.keepWrapperBoxAlive = keepWrapperBoxAlive;
  }

  public boolean isAutoGeneratedWrapperBox()
  {
    return autoGeneratedWrapperBox;
  }

  /**
   * A prefix box is closed immediately after the origin-box is closed. Prefix boxes are not merged with
   * silbling boxes.
   *
   * @param autoGeneratedWrapperBox
   */
  public void setAutoGeneratedWrapperBox(final boolean autoGeneratedWrapperBox)
  {
    this.autoGeneratedWrapperBox = autoGeneratedWrapperBox;
  }

  public LayoutModelBuilderContext close()
  {
    this.renderBox.close();
    if (isEmpty() == false)
    {
      if (renderBox.getParent() == null)
      {
        throw new InvalidReportStateException();
      }
    }
    else if (renderBox.getNodeType() == LayoutNodeTypes.TYPE_BOX_AUTOLAYOUT)
    {
      // remove yourself if empty ...
      final RenderBox parentRenderBox = renderBox.getParent();
      if (parentRenderBox == null)
      {
        throw new InvalidReportStateException();
      }
      parentRenderBox.remove(renderBox);
    }
    return this.parent;
  }

  public void addChild(final RenderBox child)
  {
    this.renderBox.addChild(child);
  }

  public void removeChild(final RenderBox child)
  {
    this.renderBox.remove(child);
  }

  public Object clone()
  {
    try
    {
      return super.clone();
    }
    catch (CloneNotSupportedException e)
    {
      throw new IllegalStateException(e);
    }
  }

  public LayoutModelBuilderContext deriveForPagebreak()
  {
    final DefaultLayoutModelBuilderContext clone = (DefaultLayoutModelBuilderContext) clone();
    if (parent != null)
    {
      clone.parent = parent.deriveForPagebreak();
    }
    return clone;
  }

  public LayoutModelBuilderContext deriveForStorage(final RenderBox clonedRoot)
  {
    if (clonedRoot == null)
    {
      throw new NullPointerException();
    }
    final DefaultLayoutModelBuilderContext clone = (DefaultLayoutModelBuilderContext) clone();
    if (parent == null)
    {
      clone.renderBox = clonedRoot;
    }
    else
    {
      clone.parent = parent.deriveForStorage(clonedRoot);
      if (isEmpty())
      {
        clone.renderBox = (RenderBox) renderBox.derive(true);
      }
      else
      {
        clone.renderBox = (RenderBox) clone.parent.getRenderBox().findNodeById(renderBox.getInstanceId());
        if (clone.renderBox == null)
        {
          throw new IllegalStateException();
        }
      }
    }
    return clone;
  }

  public void validateAfterCommit()
  {

  }

  public void performParanoidModelCheck()
  {

  }

  public void restoreStateAfterRollback()
  {

  }

  public int getDepth()
  {
    if (parent == null)
    {
      return 1;
    }
    return 1 + parent.getDepth();
  }
}
