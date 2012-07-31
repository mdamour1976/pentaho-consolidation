package org.pentaho.di.core.sql;

import java.util.List;

import org.pentaho.di.core.exception.KettleSQLException;
import org.pentaho.di.core.jdbc.ThinUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

import junit.framework.TestCase;

public class SQLTest extends TestCase {
  
  
  public void testSql01() throws KettleSQLException {
    RowMetaInterface rowMeta = generateTest3RowMeta();
    
    String sqlString = "SELECT A, B, C\nFROM Service\nWHERE B > 5\nORDER BY B DESC";
    
    SQL sql = new SQL(sqlString);
    
    assertEquals("Service", sql.getServiceName());
    sql.parse(rowMeta);
    
    assertEquals("A, B, C", sql.getSelectClause());
    List<SQLField> selectFields = sql.getSelectFields().getFields();
    assertEquals(3, selectFields.size());
    
    assertEquals("B > 5", sql.getWhereClause());
    SQLCondition whereCondition = sql.getWhereCondition();
    assertNotNull(whereCondition.getCondition());
    
    assertNull(sql.getGroupClause());
    assertNull(sql.getHavingClause());
    assertEquals("B DESC", sql.getOrderClause());
    List<SQLField> orderFields = sql.getOrderFields().getFields();
    assertEquals(1, orderFields.size());
    SQLField orderField = orderFields.get(0);
    assertTrue(orderField.isOrderField());
    assertFalse(orderField.isAscending());
    assertNull(orderField.getAlias());
    assertEquals("B", orderField.getValueMeta().getName().toUpperCase());
  }
  
  public void testSql02() throws KettleSQLException {
    RowMetaInterface rowMeta = generateTest3RowMeta();
    
    String sqlString = "SELECT A as \"FROM\", B as \"TO\", C\nFROM Service\nWHERE B > 5\nORDER BY B DESC";
    
    SQL sql = new SQL(sqlString);
    
    assertEquals("Service", sql.getServiceName());
    sql.parse(rowMeta);
    
    assertEquals("A as \"FROM\", B as \"TO\", C", sql.getSelectClause());
    assertEquals("B > 5", sql.getWhereClause());
    assertNull(sql.getGroupClause());
    assertNull(sql.getHavingClause());
    assertEquals("B DESC", sql.getOrderClause());
  }
  
  public void testSql03() throws KettleSQLException {
    RowMetaInterface rowMeta = generateTest3RowMeta();
    
    String sqlString = "SELECT A as \"FROM\", B as \"TO\", C, COUNT(*)\nFROM Service\nWHERE B > 5\nGROUP BY A,B,C\nHAVING COUNT(*) > 100\nORDER BY A,B,C";
    
    SQL sql = new SQL(sqlString);
    
    assertEquals("Service", sql.getServiceName());
    sql.parse(rowMeta);

    assertEquals("A as \"FROM\", B as \"TO\", C, COUNT(*)", sql.getSelectClause());
    assertEquals("B > 5", sql.getWhereClause());
    assertEquals("A,B,C", sql.getGroupClause());
    assertEquals("COUNT(*) > 100", sql.getHavingClause());
    assertEquals("A,B,C", sql.getOrderClause());
  }

  public void testSql04() throws KettleSQLException {
    RowMetaInterface rowMeta = generateTest3RowMeta();
    
    String sqlString = "SELECT *\nFROM Service\nWHERE B > 5\nORDER BY B DESC";
    
    SQL sql = new SQL(sqlString);
    
    assertEquals("Service", sql.getServiceName());
    sql.parse(rowMeta);
    
    assertEquals("*", sql.getSelectClause());
    List<SQLField> selectFields = sql.getSelectFields().getFields();
    assertEquals(3, selectFields.size());
    
    assertEquals("B > 5", sql.getWhereClause());
    SQLCondition whereCondition = sql.getWhereCondition();
    assertNotNull(whereCondition.getCondition());
    
    assertNull(sql.getGroupClause());
    assertNull(sql.getHavingClause());
    assertEquals("B DESC", sql.getOrderClause());
    List<SQLField> orderFields = sql.getOrderFields().getFields();
    assertEquals(1, orderFields.size());
    SQLField orderField = orderFields.get(0);
    assertTrue(orderField.isOrderField());
    assertFalse(orderField.isAscending());
    assertNull(orderField.getAlias());
    assertEquals("B", orderField.getValueMeta().getName().toUpperCase());
  }
  
  public void testSql05() throws KettleSQLException {
    RowMetaInterface rowMeta = generateTest3RowMeta();
    
    String sqlString = "SELECT count(*) as NrOfRows FROM Service";
    
    SQL sql = new SQL(sqlString);
    
    assertEquals("Service", sql.getServiceName());
    sql.parse(rowMeta);
    
    assertEquals("count(*) as NrOfRows", sql.getSelectClause());
    List<SQLField> selectFields = sql.getSelectFields().getFields();
    assertEquals(1, selectFields.size());
    SQLField countField = selectFields.get(0);
    assertTrue(countField.isCountStar());
    assertEquals("*", countField.getField());
    assertEquals("NrOfRows", countField.getAlias());
    
    assertNull(sql.getGroupClause());
    assertNotNull(sql.getGroupFields());
    assertNull(sql.getHavingClause());
    assertNull(sql.getOrderClause());
  }

  /**
   * Query generated by interactive reporting.
   * 
   * @throws KettleSQLException
   */
  public void testSql06() throws KettleSQLException {
    RowMetaInterface rowMeta = generateServiceRowMeta();
    
    String sqlString = "SELECT DISTINCT\n          BT_SERVICE_SERVICE.Category AS COL0\n         ,BT_SERVICE_SERVICE.Country AS COL1\n         ,BT_SERVICE_SERVICE.products_sold AS COL2\n         ,BT_SERVICE_SERVICE.sales_amount AS COL3\n"+
                       "FROM \n          Service BT_SERVICE_SERVICE\n"+
                       "ORDER BY\n          COL0";
    
    SQL sql = new SQL(ThinUtil.stripNewlines(sqlString));
    
    assertEquals("Service", sql.getServiceName());
    sql.parse(rowMeta);
    
    assertTrue(sql.getSelectFields().isDistinct());
    List<SQLField> selectFields = sql.getSelectFields().getFields();
    assertEquals(4, selectFields.size());
    assertEquals("COL0", selectFields.get(0).getAlias());
    assertEquals("COL1", selectFields.get(1).getAlias());
    assertEquals("COL2", selectFields.get(2).getAlias());
    assertEquals("COL3", selectFields.get(3).getAlias());
    
    List<SQLField> orderFields= sql.getOrderFields().getFields();
    assertEquals(1, orderFields.size());
  }

  /**
   * Query generated by Mondrian / Analyzer.
   * 
   * @throws KettleSQLException
   */
  public void testSql07() throws KettleSQLException {
    RowMetaInterface rowMeta = generateServiceRowMeta();
    
    String sqlString = "select \"Service\".\"Category\" as \"c0\" from \"Service\" as \"Service\" group by \"Service\".\"Category\" order by CASE WHEN \"Service\".\"Category\" IS NULL THEN 1 ELSE 0 END, \"Service\".\"Category\" ASC";
    
    SQL sql = new SQL(ThinUtil.stripNewlines(sqlString));
    
    assertEquals("Service", sql.getServiceName());
    sql.parse(rowMeta);
    
    assertFalse(sql.getSelectFields().isDistinct());
    List<SQLField> selectFields = sql.getSelectFields().getFields();
    assertEquals(1, selectFields.size());
    assertEquals("c0", selectFields.get(0).getAlias());
    
    List<SQLField> orderFields= sql.getOrderFields().getFields();
    assertEquals(2, orderFields.size());
  }
  
    
  public static RowMetaInterface generateTest2RowMeta() {
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta(new ValueMeta("A", ValueMetaInterface.TYPE_STRING, 50));
    rowMeta.addValueMeta(new ValueMeta("B", ValueMetaInterface.TYPE_INTEGER, 7));
    return rowMeta;
  }
  
  public static RowMetaInterface generateTest3RowMeta() {
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta(new ValueMeta("A", ValueMetaInterface.TYPE_STRING, 50));
    rowMeta.addValueMeta(new ValueMeta("B", ValueMetaInterface.TYPE_INTEGER, 7));
    rowMeta.addValueMeta(new ValueMeta("C", ValueMetaInterface.TYPE_INTEGER, 7));
    return rowMeta;
  }
  
  public static RowMetaInterface generateTest4RowMeta() {
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta(new ValueMeta("A", ValueMetaInterface.TYPE_STRING, 50));
    rowMeta.addValueMeta(new ValueMeta("B", ValueMetaInterface.TYPE_INTEGER, 7));
    rowMeta.addValueMeta(new ValueMeta("C", ValueMetaInterface.TYPE_STRING, 50));
    rowMeta.addValueMeta(new ValueMeta("D", ValueMetaInterface.TYPE_INTEGER, 7));
    return rowMeta;
  }

  public static RowMetaInterface generateServiceRowMeta() {
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta(new ValueMeta("Category", ValueMetaInterface.TYPE_STRING, 50));
    rowMeta.addValueMeta(new ValueMeta("Country", ValueMetaInterface.TYPE_INTEGER, 7));
    rowMeta.addValueMeta(new ValueMeta("products_sold", ValueMetaInterface.TYPE_INTEGER, 7));
    rowMeta.addValueMeta(new ValueMeta("sales_amount", ValueMetaInterface.TYPE_NUMBER, 7, 2));
    return rowMeta;
  }
}
