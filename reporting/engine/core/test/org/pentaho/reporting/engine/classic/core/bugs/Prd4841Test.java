/*
 * This program is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 *  Foundation.
 *
 *  You should have received a copy of the GNU Lesser General Public License along with this
 *  program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 *  or from the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.
 *
 *  Copyright (c) 2006 - 2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.reporting.engine.classic.core.bugs;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.layout.ModelPrinter;
import org.pentaho.reporting.engine.classic.core.layout.model.LogicalPageBox;
import org.pentaho.reporting.engine.classic.core.testsupport.DebugReportRunner;

public class Prd4841Test
{
  @Before
  public void setUp() throws Exception
  {
    ClassicEngineBoot.getInstance().start();
  }

  @Test
  public void testGoldenSample() throws Exception
  {
    MasterReport report = DebugReportRunner.parseGoldenSampleReport("Prd-4841.prpt");
    LogicalPageBox page = DebugReportRunner.layoutPage(report, 0);
    ModelPrinter.INSTANCE.print(page);
  }
  @Test
  public void testGoldenSample2() throws Exception
  {
    MasterReport report = DebugReportRunner.parseGoldenSampleReport("Prd-2087-Keep-Together-0.prpt");
    LogicalPageBox page = DebugReportRunner.layoutPage(report, 0);
    ModelPrinter.INSTANCE.print(page);
  }
}
