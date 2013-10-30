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

package org.pentaho.reporting.engine.classic.core.modules.parser.bundle.writer.elements;

import java.io.IOException;

import org.pentaho.reporting.engine.classic.core.CrosstabElement;
import org.pentaho.reporting.engine.classic.core.Element;
import org.pentaho.reporting.engine.classic.core.modules.parser.bundle.writer.BundleWriterException;
import org.pentaho.reporting.engine.classic.core.modules.parser.bundle.writer.BundleWriterState;
import org.pentaho.reporting.libraries.docbundle.WriteableDocumentBundle;
import org.pentaho.reporting.libraries.xmlns.writer.XmlWriter;

/**
 * Implementation for crosstab element write handler.
 *
 * @author Sulaiman Karmali
 */
public class CrosstabElementWriteHandler extends AbstractElementWriteHandler
{
  public CrosstabElementWriteHandler()
  {
  }

  /**
   * Writes a single element as XML structure.
   *
   * @param bundle    the bundle to which to write to.
   * @param state     the current write-state.
   * @param xmlWriter the xml writer.
   * @param element   the element.
   * @throws java.io.IOException if an IO error occurred.
   * @throws org.pentaho.reporting.engine.classic.core.modules.parser.bundle.writer.BundleWriterException
   *                             if an Bundle writer.
   */
  public void writeElement(final WriteableDocumentBundle bundle,
                           final BundleWriterState state,
                           final XmlWriter xmlWriter,
                           final Element element)
          throws IOException, BundleWriterException
  {
    if (bundle == null)
    {
      throw new NullPointerException();
    }
    if (state == null)
    {
      throw new NullPointerException();
    }
    if (xmlWriter == null)
    {
      throw new NullPointerException();
    }
    if (element == null)
    {
      throw new NullPointerException();
    }

    writeSubReport(bundle, state, xmlWriter, (CrosstabElement)element);
  }
}
