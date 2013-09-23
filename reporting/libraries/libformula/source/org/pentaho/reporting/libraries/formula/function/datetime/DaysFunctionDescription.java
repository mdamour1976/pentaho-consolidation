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
* Copyright (c) 2006 - 2013 Pentaho Corporation and Contributors.  All rights reserved.
*/

package org.pentaho.reporting.libraries.formula.function.datetime;

import org.pentaho.reporting.libraries.formula.function.AbstractFunctionDescription;
import org.pentaho.reporting.libraries.formula.function.FunctionCategory;
import org.pentaho.reporting.libraries.formula.typing.Type;
import org.pentaho.reporting.libraries.formula.typing.coretypes.DateTimeType;
import org.pentaho.reporting.libraries.formula.typing.coretypes.NumberType;

/**
 * Describes DayFunction function.
 *
 * @see DayFunction
 *
 * @author Cedric Pronzato
 */
public class DaysFunctionDescription extends AbstractFunctionDescription
{
  private static final long serialVersionUID = 8306010409074007316L;

  public DaysFunctionDescription()
  {
    super("DAYS", "org.pentaho.reporting.libraries.formula.function.datetime.Days-Function");
  }

  public Type getValueType()
  {
    return NumberType.GENERIC_NUMBER;
  }

  public int getParameterCount()
  {
    return 2;
  }

  public Type getParameterType(final int position)
  {
    return DateTimeType.DATE_TYPE;
  }

  public boolean isParameterMandatory(final int position)
  {
    return true;
  }

  public FunctionCategory getCategory()
  {
    return DateTimeFunctionCategory.CATEGORY;
  }
}
