package org.pentaho.reporting.platform.plugin.connection;

import org.pentaho.reporting.engine.classic.extensions.datasources.olap4j.connections.JndiConnectionProvider;
import org.pentaho.reporting.engine.classic.extensions.datasources.olap4j.connections.OlapConnectionProvider;
import org.pentaho.reporting.engine.classic.extensions.datasources.olap4j.parser.OlapConnectionReadHandler;
import org.pentaho.reporting.libraries.xmlns.parser.AbstractXmlReadHandler;
import org.pentaho.reporting.libraries.xmlns.parser.StringReadHandler;
import org.pentaho.reporting.libraries.xmlns.parser.XmlReadHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class PentahoOlap4JJndiConnectionReadHandler  extends AbstractXmlReadHandler
    implements OlapConnectionReadHandler
{
  private StringReadHandler pathReadHandler;
  private JndiConnectionProvider jndiConnectionProvider;

  public PentahoOlap4JJndiConnectionReadHandler()
  {
  }

  /**
   * Returns the handler for a child element.
   *
   * @param tagName the tag name.
   * @param atts    the attributes.
   * @return the handler or null, if the tagname is invalid.
   * @throws org.xml.sax.SAXException if there is a parsing error.
   */
  protected XmlReadHandler getHandlerForChild(final String uri,
                                              final String tagName,
                                              final Attributes atts)
      throws SAXException
  {
    if (isSameNamespace(uri) == false)
    {
      return null;
    }
    if ("path".equals(tagName))
    {
      pathReadHandler = new StringReadHandler();
      return pathReadHandler;
    }
    return null;
  }

  /**
   * Done parsing.
   *
   * @throws org.xml.sax.SAXException if there is a parsing error.
   */
  protected void doneParsing() throws SAXException
  {
    final JndiConnectionProvider provider = new JndiConnectionProvider();
    if (pathReadHandler != null)
    {
      provider.setConnectionPath(pathReadHandler.getResult());
    }
    jndiConnectionProvider = provider;
  }

  /**
   * Returns the object for this element or null, if this element does not
   * create an object.
   *
   * @return the object.
   * @throws org.xml.sax.SAXException if there is a parsing error.
   */
  public Object getObject() throws SAXException
  {
    return jndiConnectionProvider;
  }

  public OlapConnectionProvider getProvider()
  {
    return jndiConnectionProvider;
  }
}
