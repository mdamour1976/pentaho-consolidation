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
import java.util.Map;
import java.util.Properties;

public class ConnectionJDBC3 implements java.sql.Connection {
	private String url=null;
	// private Properties props=null;
	private boolean useUnicode;
	private CharsetInfo charsetInfo =null;
	public ConnectionJDBC3()
	{
		
	}

	public ConnectionJDBC3(String url, Properties props) {
		this.url = url;
		// this.props = props;
		
	}

	public void clearWarnings() throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void close() throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void commit() throws SQLException {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		return false;
	}

	public String getCatalog() throws SQLException {
		
		return "jdbckettle";
	}

	public int getHoldability() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public DatabaseMetaData getMetaData() throws SQLException {
		
		JDBCKettleMetaData md = new JDBCKettleMetaData(this,this.url);
		return md;
	}

	public int getTransactionIsolation() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public Map<String, Class<?>> getTypeMap() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public SQLWarning getWarnings() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isClosed() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isReadOnly() throws SQLException {
		// TODO Auto-generated method stub
		return true;
	}

	public String nativeSQL(String sql) throws SQLException {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		
	}

	public void rollback() throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void rollback(Savepoint savepoint) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setCatalog(String catalog) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setHoldability(int holdability) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setReadOnly(boolean readOnly) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public Savepoint setSavepoint() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Savepoint setSavepoint(String name) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setTransactionIsolation(int level) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		return 0;
	}

	public File getBufferDir() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getMaxPrecision() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getCharset() {
		 return charsetInfo.getCharset();
	}

	public String getURL() {
		
		return this.url;
	}

	public String getDatabaseProductName() {
		
		return "jdbc kettle";
	}

	public String getDatabaseProductVersion() {
		
		return "1.0";
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
}
