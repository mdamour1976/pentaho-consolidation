package org.pentaho.pdi.core.row;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;

import be.ibridge.kettle.core.exception.KettleValueException;

public interface ValueMetaInterface extends Cloneable
{
    /** Value type indicating that the value has no type set */
    public static final int TYPE_NONE        = 0;
    
    /** Value type indicating that the value contains a floating point double precision number. */
    public static final int TYPE_NUMBER      = 1;
    
    /** Value type indicating that the value contains a text String. */
    public static final int TYPE_STRING      = 2;
    
    /** Value type indicating that the value contains a Date. */
    public static final int TYPE_DATE        = 3;
    
    /** Value type indicating that the value contains a boolean. */
    public static final int TYPE_BOOLEAN     = 4;
    
    /** Value type indicating that the value contains a long integer. */
    public static final int TYPE_INTEGER     = 5;
    
    /** Value type indicating that the value contains a floating point precision number with arbitrary precision. */
    public static final int TYPE_BIGNUMBER   = 6;
    
    /** Value type indicating that the value contains an Object. */
    public static final int TYPE_SERIALIZABLE= 7;
    
    /** Value type indicating that the value contains binary data: BLOB, CLOB, ... */
    public static final int TYPE_BINARY      = 8;

    
    
    
    /** The storage type is the same as the indicated value type */
    public static final int STORAGE_TYPE_NORMAL = 0; 
    
    /** The storage type is indexed.  This means that the value is a simple integer index referencing the values in getIndex() */
    public static final int STORAGE_TYPE_INDEXED = 2; 
    
    
    

    public String   getName();
    public void     setName(String name);
    
    public int      getLength();
    public void     setLength(int length);
    
    public int      getPrecision();
    public void     setPrecision(int precision);
    
    public String   getOrigin();
    public void     setOrigin(String origin);
    
    public String   getComments();
    public void     setComments(String comments);
    
    public int      getType();
    public void     setType(int type);

    public int      getStorageType();
    public void     setStorageType(int storageType);
    
    public Object[] getIndex();
    public void     setIndex(Object[] index);
    
    public String   getConversionMask();
    public void     setConversionMask(String conversionMask);
    
    public String getDecimalSymbol();
    public void setDecimalSymbol(String decimalSymbol);

    public String getGroupingSymbol();
    public void setGroupingSymbol(String groupingSymbol);
    
    public String getCurrencySymbol();
    public void setCurrencySymbol(String currencySymbol);
    
    public DecimalFormat getDecimalFormat();

    public String   getStringEncoding();
    public void     setStringEncoding(String stringEncoding);
    
    /* Conversion methods */
    
    public Object cloneValueData(Object object) throws KettleValueException;
    
    /** Convert the supplied data to a String */
    public String convertToString(Object object) throws KettleValueException;

    /** Convert the supplied data to a Number */
    public Double convertToNumber(Object object) throws KettleValueException;

    /** Convert the supplied data to a BigNumber */
    public BigDecimal convertToBigNumber(Object object) throws KettleValueException;

    /** Convert the supplied data to an Integer*/
    public Long convertToInteger(Object object) throws KettleValueException;

    /** Convert the supplied data to a Date */
    public Date convertToDate(Object object) throws KettleValueException;

    /** Convert the supplied data to a Boolean */
    public Boolean convertToBoolean(Object object) throws KettleValueException;

    /** Convert the supplied data to binary data */
    public byte[] convertToBinary(Object object) throws KettleValueException;

    /**
     * @return a copy of this value meta object
     */
    public Object clone();
    
    /**
     * Checks wheter or not the value is a String.
     * @return true if the value is a String.
     */
    public boolean isString();

    /**
     * Checks whether or not this value is a Date
     * @return true if the value is a Date
     */
    public boolean isDate();

    /**
     * Checks whether or not the value is a Big Number
     * @return true is this value is a big number
     */
    public boolean isBigNumber();

    /**
     * Checks whether or not the value is a Number
     * @return true is this value is a number
     */
    public boolean isNumber();

    /**
     * Checks whether or not this value is a boolean
     * @return true if this value has type boolean.
     */
    public boolean isBoolean();

    /**
     * Checks whether or not this value is of type Serializable
     * @return true if this value has type Serializable
     */
    public boolean isSerializableType();

    /**
     * Checks whether or not this value is of type Binary
     * @return true if this value has type Binary
     */
    public boolean isBinary(); 
    
    /**
     * Checks whether or not this value is an Integer
     * @return true if this value is an integer
     */
    public boolean isInteger();

    /**
     * Checks whether or not this Value is Numeric
     * A Value is numeric if it is either of type Number or Integer
     * @return true if the value is either of type Number or Integer
     */
    public boolean isNumeric();
}
