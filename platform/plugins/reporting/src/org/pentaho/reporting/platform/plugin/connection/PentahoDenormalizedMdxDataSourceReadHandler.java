package org.pentaho.reporting.platform.plugin.connection;

import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.AbstractMDXDataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.mondrian.parser.DenormalizedMDXDataSourceReadHandler;
import org.xml.sax.SAXException;

/**
 * Todo: Document me!
 * <p/>
 * Date: 16.04.2010
 * Time: 16:31:30
 *
 * @author Thomas Morgner.
 */
public class PentahoDenormalizedMdxDataSourceReadHandler extends DenormalizedMDXDataSourceReadHandler

{
  public PentahoDenormalizedMdxDataSourceReadHandler()
  {
  }

  /**
   * Done parsing.
   *
   * @throws org.xml.sax.SAXException if there is a parsing error.
   */
  protected void doneParsing() throws SAXException
  {
    super.doneParsing();
    final AbstractMDXDataFactory o = (AbstractMDXDataFactory) getObject();
    o.setMondrianConnectionProvider(new PentahoMondrianConnectionProvider());
  }
}
