package org.pentaho.di.core.row;

import java.math.BigDecimal;
import java.util.Date;

import junit.framework.TestCase;

import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

import be.ibridge.kettle.core.exception.KettleValueException;

public class RowTest extends TestCase
{
    public void testNormalStringConversion() throws KettleValueException
    {
        Object[] rowData1 = new Object[] { "sampleString", new Date(1178535853203L), new Double(9123.00), new Long(12345), new BigDecimal("123456789012345678.9349"), new Boolean(true), };
        RowMetaInterface rowMeta1 = createTestRowMetaNormalStringConversion1();
        
        assertEquals("sampleString", rowMeta1.getString(rowData1, 0));        
        assertEquals("2007/05/07 13:04:13.203", rowMeta1.getString(rowData1, 1));        
        assertEquals("9,123.00", rowMeta1.getString(rowData1, 2));        
        assertEquals("0012345", rowMeta1.getString(rowData1, 3));        
        assertEquals("123456789012345678.9349", rowMeta1.getString(rowData1, 4));
        assertEquals("Y", rowMeta1.getString(rowData1, 5));
        
        Object[] rowData2 = new Object[] { null, new Date(1178535853203L), new Double(9123.9), new Long(12345), new BigDecimal("123456789012345678.9349"), new Boolean(false), };
        RowMetaInterface rowMeta2 = createTestRowMetaNormalStringConversion2();
        
        assertTrue( rowMeta2.getString(rowData2, 0)==null);        
        assertEquals("20070507130413", rowMeta2.getString(rowData2, 1));        
        assertEquals("9.123,9", rowMeta2.getString(rowData2, 2));        
        assertEquals("0012345", rowMeta2.getString(rowData2, 3));        
        assertEquals("123456789012345678.9349", rowMeta2.getString(rowData2, 4));
        assertEquals("false", rowMeta2.getString(rowData2, 5));
    }
    
    public void testIndexedStringConversion() throws KettleValueException
    {
        String colors[] = new String[] { "Green", "Red", "Blue", "Yellow", null, };
        Date   dates[]  = new Date[]   { new Date(1178535853203L), null, new Date(1178334949349L), new Date(1178384924736L), };

        RowMetaInterface rowMeta = createTestRowMetaIndexedStringConversion1(colors, dates);

        Object[] rowData1 = new Object[] { new Integer(0), new Integer(0), }; 
        Object[] rowData2 = new Object[] { new Integer(1), new Integer(1), }; 
        Object[] rowData3 = new Object[] { new Integer(2), new Integer(2), }; 
        Object[] rowData4 = new Object[] { new Integer(3), new Integer(3), }; 
        Object[] rowData5 = new Object[] { new Integer(4), new Integer(0), }; 
        
        assertEquals("Green", rowMeta.getString(rowData1, 0));
        assertEquals("2007/05/07 13:04:13.203", rowMeta.getString(rowData1, 1));

        assertEquals("Red", rowMeta.getString(rowData2, 0));
        assertTrue(null == rowMeta.getString(rowData2, 1));

        assertEquals("Blue", rowMeta.getString(rowData3, 0));
        assertEquals("2007/05/05 05:15:49.349", rowMeta.getString(rowData3, 1));

        assertEquals("Yellow", rowMeta.getString(rowData4, 0));
        assertEquals("2007/05/05 19:08:44.736", rowMeta.getString(rowData4, 1));

        assertTrue(null == rowMeta.getString(rowData5, 0));
        assertEquals("2007/05/07 13:04:13.203", rowMeta.getString(rowData5, 1));
    }
    
    
    private RowMetaInterface createTestRowMetaNormalStringConversion1()
    {
        RowMetaInterface rowMeta = new RowMeta();

        // A string object
        ValueMetaInterface meta1 = new ValueMeta("stringValue", ValueMetaInterface.TYPE_STRING, 30);
        rowMeta.addValueMeta(meta1);
        
        ValueMetaInterface meta2 = new ValueMeta("dateValue", ValueMetaInterface.TYPE_DATE);
        rowMeta.addValueMeta(meta2);

        ValueMetaInterface meta3 = new ValueMeta("numberValue", ValueMetaInterface.TYPE_NUMBER, 5, 2);
        meta3.setConversionMask("#,##0.00");
        meta3.setDecimalSymbol(".");
        meta3.setGroupingSymbol(",");
        rowMeta.addValueMeta(meta3);

        ValueMetaInterface meta4 = new ValueMeta("integerValue", ValueMetaInterface.TYPE_INTEGER, 7);
        meta4.setConversionMask("0000000");
        meta4.setDecimalSymbol(".");
        meta4.setGroupingSymbol(",");
        rowMeta.addValueMeta(meta4);

        ValueMetaInterface meta5 = new ValueMeta("bigNumberValue", ValueMetaInterface.TYPE_BIGNUMBER, 30, 7);
        meta5.setDecimalSymbol(".");
        rowMeta.addValueMeta(meta5);

        ValueMetaInterface meta6 = new ValueMeta("booleanValue", ValueMetaInterface.TYPE_BOOLEAN);
        rowMeta.addValueMeta(meta6);

        return rowMeta;
    }
    
    private RowMetaInterface createTestRowMetaNormalStringConversion2()
    {
        RowMetaInterface rowMeta = new RowMeta();

        // A string object
        ValueMetaInterface meta1 = new ValueMeta("stringValue", ValueMetaInterface.TYPE_STRING, 30);
        meta1.setStorageType(ValueMetaInterface.STORAGE_TYPE_INDEXED);
        rowMeta.addValueMeta(meta1);
        
        ValueMetaInterface meta2 = new ValueMeta("dateValue", ValueMetaInterface.TYPE_DATE);
        meta2.setConversionMask("yyyyMMddHHmmss");
        rowMeta.addValueMeta(meta2);

        ValueMetaInterface meta3 = new ValueMeta("numberValue", ValueMetaInterface.TYPE_NUMBER, 5, 2);
        meta3.setConversionMask("###,##0.##");
        meta3.setDecimalSymbol(",");
        meta3.setGroupingSymbol(".");
        rowMeta.addValueMeta(meta3);

        ValueMetaInterface meta4 = new ValueMeta("integerValue", ValueMetaInterface.TYPE_INTEGER, 7);
        meta4.setConversionMask("0000000");
        meta4.setDecimalSymbol(",");
        meta4.setGroupingSymbol(".");
        rowMeta.addValueMeta(meta4);

        ValueMetaInterface meta5 = new ValueMeta("bigNumberValue", ValueMetaInterface.TYPE_BIGNUMBER, 30, 7);
        meta5.setDecimalSymbol(",");
        rowMeta.addValueMeta(meta5);

        ValueMetaInterface meta6 = new ValueMeta("booleanValue", ValueMetaInterface.TYPE_BOOLEAN, 3);
        rowMeta.addValueMeta(meta6);

        return rowMeta;
    }
    
    private RowMetaInterface createTestRowMetaIndexedStringConversion1(String[] colors, Date[] dates)
    {
        RowMetaInterface rowMeta = new RowMeta();

        // A string object, indexed.
        ValueMetaInterface meta1 = new ValueMeta("stringValue", ValueMetaInterface.TYPE_STRING, 30);
        meta1.setIndex(colors);
        meta1.setStorageType(ValueMetaInterface.STORAGE_TYPE_INDEXED);
        rowMeta.addValueMeta(meta1);
        
        ValueMetaInterface meta2 = new ValueMeta("dateValue", ValueMetaInterface.TYPE_DATE);
        meta2.setIndex(dates);
        meta2.setStorageType(ValueMetaInterface.STORAGE_TYPE_INDEXED);
        rowMeta.addValueMeta(meta2);

        return rowMeta;
    }
}
