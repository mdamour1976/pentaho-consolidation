package org.pentaho.reporting.engine.classic.extensions.datasources.xquery;

import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.DataRow;
import org.pentaho.reporting.engine.classic.core.metadata.DataFactoryMetaData;
import org.pentaho.reporting.engine.classic.core.metadata.DefaultDataFactoryCore;

public class XQueryDataFactoryCore extends DefaultDataFactoryCore
{
  public XQueryDataFactoryCore()
  {
  }

  public String[] getReferencedFields(final DataFactoryMetaData metaData,
                                      final DataFactory element,
                                      final String query,
                                      final DataRow parameter)
  {
    return super.getReferencedFields(metaData, element, query, parameter);
  }

  public Object getQueryHash(final DataFactoryMetaData dataFactoryMetaData,
                             final DataFactory dataFactory,
                             final String queryName,
                             final DataRow parameter)
  {
    return super.getQueryHash(dataFactoryMetaData, dataFactory, queryName, parameter);
  }
}
