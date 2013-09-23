/*
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License, version 2 as published by the Free Software
* Foundation.
*
* You should have received a copy of the GNU Lesser General Public License along with this
* program; if not, you can obtain a copy at http://www.gnu.org/licenses/lgpl-2.0.html
* or from the Free Software Foundation, Inc.,
* 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*
* This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU Lesser General Public License for more details.
*
* Copyright 2008 Bayon Technologies, Inc.  All rights reserved.
*/

package org.pentaho.di.jdbc;

import java.io.File;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.pentaho.di.core.Const;

public class ConnectionJDBC3 implements java.sql.Connection {
	private String url=null;
	private boolean useUnicode;
	private CharsetInfo charsetInfo =null;
	public ConnectionJDBC3()
	{
		
	}

	public ConnectionJDBC3(String url, Properties props) {
		this.url = url;
	}

	public void clearWarnings() throws SQLException {
	}

	public void close() throws SQLException {
	}

	public void commit() throws SQLException {
	}

	public Statement createStatement() throws SQLException {
		KettleJDBCStatement st = new KettleJDBCStatement(this,this.url);
		return st;
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency)
			throws SQLException {
		KettleJDBCStatement st = new KettleJDBCStatement(this,this.url);
		return st;
	}

	public Statement createStatement(int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		KettleJDBCStatement st = new KettleJDBCStatement(this,this.url);
		return st;
	}

	public boolean getAutoCommit() throws SQLException {
		return true;
	}

	public String getCatalog() throws SQLException {
		
		return "jdbckettle";
	}

	public int getHoldability() throws SQLException {
		return 0;
	}

	public DatabaseMetaData getMetaData() throws SQLException {
		
		JDBCKettleMetaData md = new JDBCKettleMetaData(this,this.url);
		return md;
	}

	public int getTransactionIsolation() throws SQLException {	
		return Connection.TRANSACTION_READ_UNCOMMITTED;
	}

	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return new HashMap<String, Class<?>>();
	}

	public SQLWarning getWarnings() throws SQLException {
		return null;
	}

	public boolean isClosed() throws SQLException {
		return false;
	}

	public boolean isReadOnly() throws SQLException {
		return true;
	}

	public String nativeSQL(String sql) throws SQLException {
		return null;
	}

	public CallableStatement prepareCall(String sql) throws SQLException {
		throw new UnsupportedOperationException();
	}

	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		throw new UnsupportedOperationException();
	}

	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		throw new UnsupportedOperationException();
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		KettleJDBCPreparedStatement pstm = new KettleJDBCPreparedStatement(this,sql,url);
		return pstm;
	}

	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
			throws SQLException {
		KettleJDBCPreparedStatement pstm = new KettleJDBCPreparedStatement(this,sql,url);
		return pstm;
	}

	public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
			throws SQLException {
		throw new UnsupportedOperationException();
	}

	public PreparedStatement prepareStatement(String sql, String[] columnNames)
			throws SQLException {
		
		throw new UnsupportedOperationException();
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		
		return prepareStatement(sql);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		
		return prepareStatement(sql);
	}

	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
	}

	public void rollback() throws SQLException {
	}

	public void rollback(Savepoint savepoint) throws SQLException {
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
	}

	public void setCatalog(String catalog) throws SQLException {
	}

	public void setHoldability(int holdability) throws SQLException {
	}

	public void setReadOnly(boolean readOnly) throws SQLException {
	}

	public Savepoint setSavepoint() throws SQLException {
		return null;
	}

	public Savepoint setSavepoint(String name) throws SQLException {
		return null;
	}

	public void setTransactionIsolation(int level) throws SQLException {
	}

	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
	}
	
	 /**
     * Retrieve the sendParametersAsUnicode flag.
     *
     * @return <code>boolean</code> true if parameters should be sent as unicode.
     */
    protected boolean getUseUnicode() {
        return this.useUnicode;
    }

	public long getLobBuffer() {
		return 0;
	}

	public File getBufferDir() {
		return null;
	}

	public int getMaxPrecision() {
		return 0;
	}

	public String getCharset() {
		 return charsetInfo.getCharset();
	}

	public String getURL() {
		
		return this.url;
	}

	public String getDatabaseProductName() {
		return "Pentaho Data Integration";
	}

	public String getDatabaseProductVersion() {
		return Const.VERSION;
	}

  public boolean isWrapperFor(Class<?> arg0) throws SQLException {
    return false;
  }

  public <T> T unwrap(Class<T> arg0) throws SQLException {
    return null;
  }

  public Array createArrayOf(String arg0, Object[] arg1) throws SQLException {
    return null;
  }

  public Blob createBlob() throws SQLException {
    return null;
  }

  public Clob createClob() throws SQLException {
    return null;
  }

  public NClob createNClob() throws SQLException {
    return null;
  }

  public SQLXML createSQLXML() throws SQLException {
    return null;
  }

  public Struct createStruct(String arg0, Object[] arg1) throws SQLException {
    return null;
  }

  public Properties getClientInfo() throws SQLException {
    return null;
  }

  public String getClientInfo(String arg0) throws SQLException {
    return null;
  }

  public boolean isValid(int arg0) throws SQLException {
    return false;
  }

  public void setClientInfo(Properties arg0) throws SQLClientInfoException {
  }

  public void setClientInfo(String arg0, String arg1) throws SQLClientInfoException {
  }

  public void setSchema(String schema) throws SQLException {
    throw new SQLException("Unsupported method");
  }

  public String getSchema() throws SQLException {
    throw new SQLException("Unsupported method");
  }

  public void abort(Executor executor) throws SQLException {
    throw new SQLException("Unsupported method");
  }

  public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
    throw new SQLException("Unsupported method");
  }

  public int getNetworkTimeout() throws SQLException {
    throw new SQLException("Unsupported method");
  }
}
