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

package org.pentaho.reporting.engine.classic.core.layout.process.util;

public class RowLevelPaginationShiftState implements PaginationShiftState
{
  private PaginationShiftState parent;
  private long shift;
  private long shiftForChilds;
  private StackedObjectPool<RowLevelPaginationShiftState> pool;

  public RowLevelPaginationShiftState()
  {
  }

  public RowLevelPaginationShiftState(final PaginationShiftState parent)
  {
    reuse(null, parent);
  }

  public void reuse (final StackedObjectPool<RowLevelPaginationShiftState> pool,
                     final PaginationShiftState parent)
  {
    if (parent == null)
    {
      throw new NullPointerException();
    }
    this.parent = parent;
    this.pool = pool;
    this.shiftForChilds = parent.getShiftForNextChild();
    this.shift = this.shiftForChilds;
  }

  public void suspendManualBreaks()
  {
  }

  public boolean isManualBreakSuspended()
  {
    return parent.isManualBreakSuspendedForChilds();
  }

  public boolean isManualBreakSuspendedForChilds()
  {
    return true;
  }

  public void updateShiftFromChild(final long absoluteValue)
  {
    this.shift = Math.max(shift, absoluteValue);
  }

  public long getShiftForNextChild()
  {
    return shiftForChilds;
  }

  public PaginationShiftState pop()
  {
    parent.updateShiftFromChild(shift);
    if (this.pool != null)
    {
      this.pool.free(this);
      this.pool = null;
    }
    return parent;
  }

  public void increaseShift(final long value)
  {
    this.shiftForChilds = Math.max(shiftForChilds, this.shiftForChilds + value);
    this.shift = Math.max(shift, shiftForChilds);
  }

  public void setShift(final long value)
  {
    this.shiftForChilds = Math.max(shiftForChilds, value);
    this.shift = Math.max(shift, shiftForChilds);
  }
}
