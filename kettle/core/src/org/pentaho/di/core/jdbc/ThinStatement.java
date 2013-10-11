/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.core.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

import org.pentaho.di.cluster.HttpUtil;
import org.pentaho.di.core.variables.Variables;

public class ThinStatement implements Statement {

  protected ThinConnection connection;
  protected ThinResultSet resultSet;

  protected int maxRows;

  public ThinStatement( ThinConnection connection, int resultSetType, int resultSetConcurrency ) {
    this( connection );
  }

  public ThinStatement( ThinConnection connection, int resultSetType, int resultSetConcurrency, int resultSetHoldability ) {
    this( connection );
  }

  public ThinStatement( ThinConnection connection ) {
    this.connection = connection;
  }

  @Override
  public boolean isWrapperFor( Class<?> iface ) throws SQLException {
    return false;
  }

  @Override
  public <T> T unwrap( Class<T> iface ) throws SQLException {
    return null;
  }

  @Override
  public void addBatch( String arg0 ) throws SQLException {
  }

  @Override
  public void cancel() throws SQLException {
    if ( resultSet != null ) {
      resultSet.cancel();
    }
  }

  @Override
  public void clearBatch() throws SQLException {
    throw new SQLException( "Batch update statements are not supported by the thin Kettle JDBC driver" );
  }

  @Override
  public void clearWarnings() throws SQLException {
  }

  @Override
  public void close() throws SQLException {
    // Nothing to close
  }

  @Override
  public boolean execute( String sql ) throws SQLException {
    return execute( sql, 0 );
  }

  @Override
  public boolean execute( String sql, int autoGeneratedKeys ) throws SQLException {
    return executeQuery( sql ) != null;
  }

  @Override
  public boolean execute( String sql, int[] arg1 ) throws SQLException {
    return executeQuery( sql ) != null;
  }

  @Override
  public boolean execute( String sql, String[] arg1 ) throws SQLException {
    return executeQuery( sql ) != null;
  }

  @Override
  public int[] executeBatch() throws SQLException {
    throw new SQLException( "Batch update statements are not supported by the thin Kettle JDBC driver" );
  }

  @Override
  public ResultSet executeQuery( String sql ) throws SQLException {
    try {
      String url =
          HttpUtil.constructUrl( new Variables(), connection.getHostname(), connection.getPort(), connection
              .getWebAppName(), connection.getService() + "/sql/" );
      resultSet = new ThinResultSet( this, url, connection.getUsername(), connection.getPassword(), sql );
      return resultSet;
    } catch ( Exception e ) {
      throw new SQLException( "Unable to execute query: ", e );
    }
  }

  @Override
  public int executeUpdate( String sql ) throws SQLException {
    throw new SQLException( "The thin Kettle JDBC driver is read-only" );
  }

  @Override
  public int executeUpdate( String sql, int arg1 ) throws SQLException {
    return executeUpdate( sql );
  }

  @Override
  public int executeUpdate( String sql, int[] arg1 ) throws SQLException {
    return executeUpdate( sql );
  }

  @Override
  public int executeUpdate( String sql, String[] arg1 ) throws SQLException {
    return executeUpdate( sql );
  }

  @Override
  public Connection getConnection() throws SQLException {
    return connection;
  }

  @Override
  public int getFetchDirection() throws SQLException {
    return ResultSet.FETCH_FORWARD;
  }

  @Override
  public int getFetchSize() throws SQLException {
    return 1;
  }

  @Override
  public ResultSet getGeneratedKeys() throws SQLException {
    throw new SQLException( "The thin Kettle JDBC driver is read-only" );
  }

  @Override
  public int getMaxFieldSize() throws SQLException {
    return 0;
  }

  @Override
  public boolean getMoreResults() throws SQLException {
    resultSet.close();
    return true;
  }

  @Override
  public boolean getMoreResults( int arg0 ) throws SQLException {
    resultSet.close();
    return true;
  }

  @Override
  public int getQueryTimeout() throws SQLException {
    return 0;
  }

  @Override
  public ResultSet getResultSet() throws SQLException {
    return resultSet;
  }

  @Override
  public int getResultSetConcurrency() throws SQLException {
    return resultSet.getConcurrency();
  }

  @Override
  public int getResultSetHoldability() throws SQLException {
    return resultSet.getHoldability();
  }

  @Override
  public int getResultSetType() throws SQLException {
    return resultSet.getType();
  }

  @Override
  public int getUpdateCount() throws SQLException {
    return 0;
  }

  @Override
  public SQLWarning getWarnings() throws SQLException {
    return null;
  }

  @Override
  public boolean isClosed() throws SQLException {
    return resultSet.isClosed();
  }

  @Override
  public boolean isPoolable() throws SQLException {
    return false;
  }

  @Override
  public void setCursorName( String arg0 ) throws SQLException {
  }

  @Override
  public void setEscapeProcessing( boolean arg0 ) throws SQLException {
  }

  @Override
  public void setFetchDirection( int arg0 ) throws SQLException {
  }

  @Override
  public void setFetchSize( int arg0 ) throws SQLException {
  }

  @Override
  public void setMaxFieldSize( int arg0 ) throws SQLException {
  }

  @Override
  public void setPoolable( boolean arg0 ) throws SQLException {
  }

  @Override
  public void setQueryTimeout( int arg0 ) throws SQLException {
  }

  /**
   * @return the maxRows
   */
  @Override
  public int getMaxRows() {
    return maxRows;
  }

  /**
   * @param maxRows
   *          the maxRows to set
   */
  @Override
  public void setMaxRows( int maxRows ) {
    this.maxRows = maxRows;
  }

  public void closeOnCompletion() throws SQLException {
  }

  public boolean isCloseOnCompletion() throws SQLException {
    return false;
  }

}
