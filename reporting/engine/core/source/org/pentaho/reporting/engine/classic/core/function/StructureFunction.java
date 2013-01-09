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

package org.pentaho.reporting.engine.classic.core.function;

/**
 * A structure function is a annonymous function that modifes the structure of the report or computes changes to the
 * report definition on the current report.
 * <p/>
 * Structure functions are considered to be part of the initial master-report. All events that were generated by
 * subreports are marked as deep-traversing events. The report-definition that is passed in the report-state is always
 * the report definition of the (sub) report that generated the event.
 *
 * @author Thomas Morgner
 */
public interface StructureFunction extends Function
{
  public int getProcessingPriority();
}
