 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

 
package be.ibridge.kettle.core.value;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.XMLInterface;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleEOFException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleFileException;
import be.ibridge.kettle.core.exception.KettleValueException;
import be.ibridge.kettle.repository.Repository;


/**
 * This class is one of the core classes of the Kettle framework.
 * It contains everything you need to manipulate atomic data (Values/Fields/...)
 * and to describe it in the form of meta-data. (name, length, precision, etc.)
 * 
 * @author Matt
 * @since Beginning 2003
 */

public class Value implements Cloneable, XMLInterface, Serializable
{
    private static final long serialVersionUID = -6310073485210258622L;
    
    /**
	 * Value type indicating that the value has no type set.
	 */
	public static final int VALUE_TYPE_NONE       = 0;
	/**
	 * Value type indicating that the value contains a floating point double precision number.
	 */
	public static final int VALUE_TYPE_NUMBER      = 1;
	/**
	 * Value type indicating that the value contains a text String.
	 */
	public static final int VALUE_TYPE_STRING      = 2;
	/**
	 * Value type indicating that the value contains a Date.
	 */
	public static final int VALUE_TYPE_DATE        = 3;
	/**
	 * Value type indicating that the value contains a boolean.
	 */
	public static final int VALUE_TYPE_BOOLEAN     = 4;
	/**
	 * Value type indicating that the value contains a long integer.
	 */
	public static final int VALUE_TYPE_INTEGER     = 5;
    /**
     * Value type indicating that the value contains a floating point precision number with arbitrary precision.
     */
    public static final int VALUE_TYPE_BIGNUMBER   = 6;

	/**
	 * The descriptions of the value types.
	 */
	private static final String value_type_desc[]=
		{
			"-",
			"Number", "String", "Date", "Boolean", "Integer", "BigNumber"
		};
	
	private ValueInterface value;
	
	private String      name;
	private String      origin;
	private boolean     NULL;
	
	/**
	 * Constructs a new Value of type EMPTY
	 *
	 */
	public Value()
	{
		clearValue();
	}
		
	/**
	 * Constructs a new Value with a name.
	 * 
	 * @param name Sets the name of the Value
	 */
	public Value(String name)
	{
		clearValue();
		setName(name);
	}

	/**
	 * Constructs a new Value with a name and a type.
	 * 
	 * @param name Sets the name of the Value
	 * @param val_type Sets the type of the Value (Value.VALUE_TYPE_*)
	 */
	public Value(String name, int val_type)
	{
		clearValue();
		newValue(val_type);
		setName(name);
	}

	/**
	 * This method allocates a new value of the appropriate type..
	 * @param val_type The new type of value
	 */
	private void newValue(int val_type)
	{
		switch(val_type)
		{
		case VALUE_TYPE_NUMBER   : value = new ValueNumber(); break;
		case VALUE_TYPE_STRING   : value = new ValueString(); break;
		case VALUE_TYPE_DATE     : value = new ValueDate(); break;
		case VALUE_TYPE_BOOLEAN  : value = new ValueBoolean(); break;
		case VALUE_TYPE_INTEGER  : value = new ValueInteger(); break;
        case VALUE_TYPE_BIGNUMBER: value = new ValueBigNumber(); break;
		default: value = null;
		}
	}
	
	/**
	 * Convert the value to another type.  This only works if a value has been set previously.
	 * That is the reason this method is private. Rather, use the public method setType(int type).
	 *  
	 * @param val_type The type to convert to.
	 */
	private void convertTo(int val_type)
	{
		if (value!=null)
		{
			switch(val_type)
			{
			case VALUE_TYPE_NUMBER    : value = new ValueNumber(value.getNumber()); break;
			case VALUE_TYPE_STRING    : value = new ValueString(value.getString()); break;
			case VALUE_TYPE_DATE      : value = new ValueDate(value.getDate()); break;
			case VALUE_TYPE_BOOLEAN   : value = new ValueBoolean(value.getBoolean()); break;
			case VALUE_TYPE_INTEGER   : value = new ValueInteger(value.getInteger()); break;
            case VALUE_TYPE_BIGNUMBER : value = new ValueBigNumber(value.getBigNumber()); break;
			default: value = null;
			}
		}
	}

	/**
	 * Constructs a new Value with a name, a type, length and precision.
	 * 
	 * @param name Sets the name of the Value
	 * @param val_type Sets the type of the Value (Value.VALUE_TYPE_*)
	 * @param length The length of the value
	 * @param precision The precision of the value
	 */
	public Value(String name, int val_type, int length, int precision)
	{
		this(name, val_type);
		setLength(length, precision);
	}

    /**
     * Constructs a new Value of Type VALUE_TYPE_BIGNUMBER, with a name, containing a BigDecimal number
     * 
     * @param name Sets the name of the Value
     * @param bignum The number to store in this Value
     */
    public Value(String name, BigDecimal bignum)
    {
        clearValue();
        setValue(bignum);
        setName(name);
    }


    /**
	 * Constructs a new Value of Type VALUE_TYPE_NUMBER, with a name, containing a number
	 * 
	 * @param name Sets the name of the Value
	 * @param num The number to store in this Value
	 */
	public Value(String name, double num)
	{
		clearValue();
		setValue(num);
		setName(name);
	}

	/**
	 * Constructs a new Value of Type VALUE_TYPE_STRING, with a name, containing a String
	 * 
	 * @param name Sets the name of the Value
	 * @param str The text to store in this Value
	 */
	public Value(String name, StringBuffer str)
	{
		this(name, str.toString());
	}

	/**
	 * Constructs a new Value of Type VALUE_TYPE_STRING, with a name, containing a String
	 * 
	 * @param name Sets the name of the Value
	 * @param str The text to store in this Value
	 */
	public Value(String name, String str)
	{
		clearValue();
		setValue(str);
		setName(name);
	}

	/**
	 * Constructs a new Value of Type VALUE_TYPE_DATE, with a name, containing a Date
	 * 
	 * @param name Sets the name of the Value
	 * @param dat The date to store in this Value
	 */
	public Value(String name, Date dat)
	{
		clearValue();
		setValue(dat);
		setName(name);
	}

	/**
	 * Constructs a new Value of Type VALUE_TYPE_BOOLEAN, with a name, containing a boolean value
	 * 
	 * @param name Sets the name of the Value
	 * @param bool The boolean to store in this Value
	 */
	public Value(String name, boolean bool)
	{
		clearValue();
		setValue(bool);
		setName(name);
	}

	/**
	 * Constructs a new Value of Type VALUE_TYPE_INTEGER, with a name, containing an integer number
	 * 
	 * @param name Sets the name of the Value
	 * @param l The integer to store in this Value
	 */
	public Value(String name, long l)
	{
		clearValue();
		setValue(l);
		setName(name);
	}
	
	/**
	 * Constructs a new Value as a copy of another value and renames it...
	 * 
	 * @param name The new name of the copied Value
	 * @param v The value to be copied
	 */
	public Value(String name, Value v)
	{
		this(v);
		setName(name);
	}
	
	/**
	 * Constructs a new Value as a copy of another value
	 * 
	 * @param v The Value to be copied
	 */
	public Value(Value v)
	{
		if (v!=null)
		{
			setType( v.getType() );
			value = v.getValueCopy();
			if (v.getName()!=null) setName(new String(v.getName())); else name=null;
			setLength(v.getLength(), v.getPrecision());
			setNull(v.isNull());
			if (v.origin!=null) setOrigin(new String(v.origin)); else origin=null;
		}
		else
		{
			clearValue();
		}
	}

	public Object clone()
	{
		Value retval = null;
		try
		{
			retval = (Value)super.clone();
		}
		catch(CloneNotSupportedException e)
		{
			retval=null;
		}
		return retval;
	}
			

	/**
	 * Build a copy of this Value
	 * @return a copy of another value
	 * 
	 */
	public Value Clone()
	{
		Value v = new Value(this);
		return v;
	}

	/**
	 * Clears the content and name of a Value 
	 * 
	 */
	public void clearValue()
	{
		value    = null;
		name     = null;
		NULL     = false;
		origin  = null;
	}

	private ValueInterface getValueCopy()
	{
		if (value==null) return null;
		return (ValueInterface)value.clone();
	}


	/**
	 * Sets the name of a Value
	 * 
	 * @param name The new name of the value
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Obtain the name of a Value
	 * 
	 * @return The name of the Value
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * This method allows you to set the origin of the Value by means of the name of the originating step.
	 * 
	 * @param step_of_origin The step of origin.
	 */
	public void setOrigin(String step_of_origin)
	{
		origin = step_of_origin;
	}
	
	/**
	 * Obtain the origin of the step.
	 * 
	 * @return The name of the originating step
	 */
	public String getOrigin()
	{
		return origin;
	}
	
    /**
     * Sets the value to a BigDecimal number value.
     * @param num The number value to set the value to
     */
    public void setValue(BigDecimal num)
    {
        if (value==null || value.getType()!=VALUE_TYPE_BIGNUMBER)  value = new ValueBigNumber(num);
        else value.setBigNumber(num);
        
        setNull(false);
    }
    
	/**
	 * Sets the value to a double Number value.
	 * @param num The number value to set the value to
	 */
	public void setValue(double num)
	{
		if (value==null || value.getType()!=VALUE_TYPE_NUMBER)  value = new ValueNumber(num);
		else value.setNumber(num);
		setNull(false);
	}
	
	/**
	 * Sets the Value to a String text
	 * @param str The StringBuffer to get the text from
	 */
	public void setValue(StringBuffer str)
	{
		if (value==null || value.getType()!=VALUE_TYPE_STRING)  value = new ValueString(str.toString());
		else value.setString(str.toString());
		setNull(str==null);
	}
	
	/**
	 * Sets the Value to a String text
	 * @param str The String to get the text from
	 */
	public void setValue(String str)
	{
		if (value==null || value.getType()!=VALUE_TYPE_STRING)  value = new ValueString(str);
		else value.setString(str);
		setNull(str==null);
	}

	/**
	 * Sets the Value to a Date
	 * @param dat The Date to set the Value to
	 */
	public void setValue(Date dat)
	{
		if (value==null || value.getType()!=VALUE_TYPE_DATE)  value = new ValueDate(dat);
		else value.setDate(dat);
		setNull(dat==null);
	}
	
	/**
	 * Sets the Value to a boolean
	 * @param bool The boolean to set the Value to
	 */
	public void setValue(boolean bool)
	{
		if (value==null || value.getType()!=VALUE_TYPE_BOOLEAN)  value = new ValueBoolean(bool);
		else value.setBoolean(bool);
		setNull(false);
	}

	/**
	 * Sets the Value to a long integer
	 * @param b The byte to convert to a long integer to which the Value is set.
	 */
	public void setValue(byte b)
	{
		setValue((long)b);
	}

	/**
	 * Sets the Value to a long integer
	 * @param i The integer to convert to a long integer to which the Value is set.
	 */
	public void setValue(int i)
	{
		setValue((long)i);
	}

	/**
	 * Sets the Value to a long integer
	 * @param l The long integer to which the Value is set.
	 */
	public void setValue(long l)
	{
		if (value==null || value.getType()!=VALUE_TYPE_INTEGER)  value = new ValueInteger(l);
		else value.setInteger(l);
		setNull(false);
	}

	/**
	 * Copy the Value from another Value.
	 * It doesn't copy the name.
	 * @param v The Value to copy the settings and value from
	 */
	public void setValue(Value v)
	{
		if (v!=null)
		{
			value = v.getValueCopy();
			setNull(v.isNull());
			if (v.origin!=null) setOrigin(new String(v.origin)); else origin=null;
		}
		else
		{
			clearValue();
		}
	}
    
    /**
     * Get the BigDecimal number of this Value.
     * If the value is not of type BIG_NUMBER, a conversion is done first. 
     * @return the double precision floating point number of this Value.
     */
   public BigDecimal getBigNumber()
    {
        if (value==null || isNull()) return null;
        return value.getBigNumber();
    }
    
	
	/**
	 * Get the double precision floating point number of this Value.
	 * If the value is not of type NUMBER, a conversion is done first. 
	 * @return the double precision floating point number of this Value.
	 */
	public double getNumber()
	{
		if (value==null || isNull()) return 0.0;
		return value.getNumber();
	}

	/**
	 * Get the String text representing this value.
	 * If the value is not of type STRING, a conversion if done first.
	 * @return the String text representing this value.
	 */
	public String getString()
	{
		if (value==null || isNull()) return null;
		return value.getString();
	}
	
	/**
	 * Get the length of the String representing this value.
	 * @return the length of the String representing this value.
	 */
	public int getStringLength()
	{
		String s = getString();
		if (s==null) return 0;
		return s.length();
	}
		
	/**
	 * Get the Date of this Value.
	 * If the Value is not of type DATE, a conversion is done first.
	 * @return the Date of this Value.
	 */
	public Date getDate()
	{
		if (value==null || isNull()) return null;
		return value.getDate();
	}
	
	/**
	 * Get the boolean value of this Value.
	 * If the Value is not of type BOOLEAN, it will be converted.
	 * <p>Strings: "YES", "Y", "TRUE" (case insensitive) to true, the rest false
	 * <p>Number: 0.0 is false, the rest is true.
	 * <p>Integer: 0 is false, the rest is true.
	 * <p>Date: always false.
	 * @return the boolean representation of this Value.
	 */
	public boolean getBoolean()
	{
		if (value==null || isNull()) return false;
		return value.getBoolean();
	}

	/**
	 * Get the long integer representation of this value.
	 * If the Value is not of type INTEGER, it will be converted:
	 * <p>String: try to convert to a long value, 0L if it didn't work.
	 * <p>Number: round the double value and return the resulting long integer.
	 * <p>Date: return the number of miliseconds after <code>1970:01:01 00:00:00</code>
	 * <p>Date: always false.
	 * 
	 * @return the long integer representation of this value.
	 */
	public long getInteger()
	{
		if (value==null || isNull()) return 0L;
		return value.getInteger();
	}
	
	/**
	 * Set the type of this Value
	 * @param val_type The type to which the Value will be set.
	 */
	public void setType(int val_type)
	{
		if (value==null) newValue(val_type);
		else // Convert the value to the appropriate type...
		{
			convertTo(val_type);
		}
	}
	
	/**
	 * Returns the type of this Value
	 * @return the type of this Value
	 */
	public int getType()
	{
		if (value==null) return VALUE_TYPE_NONE;
		return value.getType();
	}
	
	/**
	 * Checks whether or not this Value is empty.
	 * A value is empty if it has the type VALUE_TYPE_EMPTY
	 * @return true if the value is empty.
	 */
	public boolean isEmpty()
	{
		if (value==null) return true;
		return false;
	}
	
	/**
	 * Checks wheter or not the value is a String.
	 * @return true if the value is a String.
	 */
	public boolean isString()
	{
		if (value==null) return false;
		return value.getType()==VALUE_TYPE_STRING;
	}

	/**
	 * Checks whether or not this value is a Date
	 * @return true if the value is a Date
	 */
	public boolean isDate()
	{
		if (value==null) return false;
		return value.getType()==VALUE_TYPE_DATE;
	}

    /**
     * Checks whether or not the value is a Big Number
     * @return true is this value is a big number
     */
    public boolean isBigNumber()
    {
        if (value==null) return false;
        return value.getType()==VALUE_TYPE_BIGNUMBER;
    }

	/**
	 * Checks whether or not the value is a Number
	 * @return true is this value is a number
	 */
	public boolean isNumber()
	{
		if (value==null) return false;
		return value.getType()==VALUE_TYPE_NUMBER;
	}

	/**
	 * Checks whether or not this value is a boolean
	 * @return true if this value has type boolean.
	 */
	public boolean isBoolean()
	{
		if (value==null) return false;
		return value.getType()==VALUE_TYPE_BOOLEAN;
	}

	/**
	 * Checks whether or not this value is an Integer
	 * @return true if this value is an integer
	 */
	public boolean isInteger()
	{
		if (value==null) return false;
		return value.getType()==VALUE_TYPE_INTEGER;
	}

	/**
	 * Checks whether or not this Value is Numeric
	 * A Value is numeric if it is either of type Number or Integer
	 * @return true if the value is either of type Number or Integer
	 */
	public boolean isNumeric()
	{
		return isInteger() || isNumber() || isBigNumber();
	}
	
	/**
	 * Checks whether or not the specified type is either Integer or Number
	 * @param t the type to check
	 * @return true if the type is Integer or Number
	 */
	public static final boolean isNumeric(int t)
	{
		return t==VALUE_TYPE_INTEGER || t==VALUE_TYPE_NUMBER || t==VALUE_TYPE_BIGNUMBER;
	}

	/**
	 * Returns a padded to length String text representation of this Value
	 * @return a padded to length String text representation of this Value
	 */
	public String toString()
	{
		return toString(true);
	}

	/**
	 * a String text representation of this Value, optionally padded to the specified length
	 * @param pad true if you want to pad the resulting String
	 * @return a String text representation of this Value, optionally padded to the specified length
	 */
	public String toString(boolean pad)
	{
		String retval;
		
		switch(getType())
		{
		case VALUE_TYPE_STRING :  retval=toStringString(pad);  break;
		case VALUE_TYPE_NUMBER :  retval=toStringNumber(pad);  break;
		case VALUE_TYPE_DATE   :  retval=toStringDate();    break;
		case VALUE_TYPE_BOOLEAN:  retval=toStringBoolean(); break;
		case VALUE_TYPE_INTEGER:  retval=toStringInteger(pad); break;
        case VALUE_TYPE_BIGNUMBER: retval=toStringBigNumber(pad); break;
		default: retval=""; break; 
		}
		
		return retval;
	}

	/**
	 * Returns a String describing the meta-data (type & length description) of this Value. 
	 * @return a String describing the meta-data (type & length description) of this Value.
	 */
	public String toStringMeta()
	{
		String retval=getTypeDesc();
		
		switch(getType())
		{
		case VALUE_TYPE_STRING :  
			if (getLength()>0) retval+="("+getLength()+")";  
			break;
		case VALUE_TYPE_NUMBER :
        case VALUE_TYPE_BIGNUMBER :
			if (getLength()>0)
			{
				retval+="("+getLength();
				if (getPrecision()>0)
				{
					retval+=", "+getPrecision();
				}
				retval+=")";
			}
			break;
		case VALUE_TYPE_INTEGER:  
			if (getLength()>0)
			{
				retval+="("+getLength()+")";
			}
			break;
		default: break; 
		}
		
		return retval;
	}
	
	/**
	 * Converts a String Value to String optionally padded to the specified length.
	 * @param pad true if you want to pad the resulting string to length.
	 * @return a String optionally padded to the specified length.
	 */
	private String toStringString(boolean pad)
	{
		String retval=null;
		
		if (value==null) return null;
		
		if (value.getLength()<=0)  // No length specified!
		{
			if (isNull() || value.getString()==null) 
				retval = Const.NULL_STRING;
			else            
				retval = value.getString();
		}
		else
		{
			StringBuffer ret;
			
			if (isNull() || value.getString()==null) ret=new StringBuffer(Const.NULL_STRING);
			else          ret=new StringBuffer(value.getString());
			
			if (pad) 
			{
				int length = value.getLength();
				if (length>16384) length=16384; // otherwise we get OUT OF MEMORY errors for CLOBS.
				Const.rightPad(ret, length);
			}

			retval=ret.toString();
		}
		return retval;
	}
		
	/**
	 * Converts a Number value to a String, optionally padding the result to the specified length.
     * @param pad true if you want to pad the resulting string to length.
	 * @return a String optionally padded to the specified length.
	 */
	private String toStringNumber(boolean pad)
	{
		String retval;
		
		if (value==null) return null;

		if (pad)
		{
			if (value.getLength()<1)
			{
				if (isNull()) retval=Const.NULL_NUMBER; 
				else
				{
					DecimalFormat form= new DecimalFormat();
                    form.applyPattern( " ##########0.0########;-#########0.0########" );
                    // System.out.println("local.pattern = ["+form.toLocalizedPattern()+"]");
                    retval=form. format(value.getNumber());
				} 
			}
			else
			{
				if (isNull())
				{
					StringBuffer ret=new StringBuffer(Const.NULL_NUMBER);
					Const.rightPad(ret, value.getLength());
					retval=ret.toString();
				}
				else
				{
					StringBuffer fmt=new StringBuffer();
					int i;
					DecimalFormat form;
					
					if (value.getNumber()>=0) fmt.append(" "); // to compensate for minus sign.
	
					if (value.getPrecision()<0)  // Default: two decimals
					{
						if (value.getLength()<0)           //  format: 1234,56 (-1,-1) 
						{
							fmt.append("0.00");
						}
						else                    //  format 0000001234,00 --> (10,-1)
						{
							for (i=0;i<value.getLength();i++) fmt.append("0");
							fmt.append(".00"); // for the .00
						}
					}
					else
					if (value.getLength()==0) // No decimals 0000001234 --> (10,0)
					{
						for (i=0;i<value.getLength();i++) fmt.append("0"); // all zeroes.
					}
					else  // Floating point format   00001234,56  --> (12,2)
					{
							for (i=0;i<=value.getLength();i++) fmt.append("0"); // all zeroes.
							int pos = value.getLength()-value.getPrecision()+1-(value.getNumber()<0?1:0);
							if (pos>=0 && pos <fmt.length())
							{
								fmt.setCharAt(value.getLength()-value.getPrecision()+1-(value.getNumber()<0?1:0), '.'); // one 'comma'
							}
					}
					form= new DecimalFormat(fmt.toString());
					retval=form.format(value.getNumber());
				}
			}
		}
		else
		{
			if (isNull()) retval=Const.NULL_NUMBER; 
			else retval=""+value.getNumber();
		}
		return retval;
	}
	
	/**
	 * Converts a Date value to a String.
	 * The date has format: <code>yyyy/MM/dd HH:mm:ss.SSS</code>
	 * @return a String representing the Date Value.
	 */
	private String toStringDate()
	{
		String retval;
		if (value==null) return null;

		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
		
		if (getLength()<=0)
		{
			if (isNull() || value.getDate()==null) retval=Const.NULL_DATE;
			else          
			{
				retval=df.format(value.getDate()).toString();
			} 
		}
		else
		{
			StringBuffer ret;
			if (isNull() || value.getDate()==null)
				 ret=new StringBuffer(Const.NULL_DATE);
			else ret=new StringBuffer(df.format(value.getDate()).toString());
			Const.rightPad(ret, getLength()<=10?10:getLength());
			retval=ret.toString();
		}
		return retval;
	} 
	
	/**
	 * Returns a String representing the boolean value.
	 * If the length is 1: it will be "Y" & "N", otherwise "true" & "false" 
	 * @return a String representing the boolean value.
	 */
	private String toStringBoolean()
	{
		String retval;
		if (value==null) return null;

		if (isNull()) 
		{
			retval=Const.NULL_BOOLEAN;
		} 
		else
		{
			if (getLength()==1)
			{
				retval=value.getBoolean()?"Y":"N";
			}
			else
			{
				retval=value.getBoolean()?"true":"false";
			}
		}
		
		if (getLength()>=0)
		{
			StringBuffer ret=new StringBuffer(retval);
			Const.rightPad(ret, getLength());
			retval=ret.toString();
		}
		
		return retval;
	}

	/**
	 * Converts an Integer value to a String, optionally padding the result to the specified length.
     * @param pad true if you want to pad the resulting string to length.
	 * @return a String optionally padded to the specified length.
	 */
	private String toStringInteger(boolean pad)
	{
		String retval;
		if (value==null) return null;
		
		if (getLength()<1)
		{
			if (isNull()) retval=Const.NULL_INTEGER; 
			else
			{
				DecimalFormat form= new DecimalFormat(" ###############0;-###############0");
				retval=form.format(value.getInteger());
			} 
		}
		else
		{
			if (isNull())
			{
				StringBuffer ret=new StringBuffer(Const.NULL_INTEGER);
				Const.rightPad(ret, getLength());
				retval=ret.toString();
			}
			else
			{
				StringBuffer fmt=new StringBuffer();
				int i;
				DecimalFormat form;
				
				if (value.getInteger()>=0) fmt.append(" "); // to compensate for minus sign.

				if (getPrecision()<0)  // Default: two decimals
				{
					if (getLength()<0)           //  format: 1234,56 (-1,-1) 
					{
						fmt.append("0.00");
					}
					else                    //  format 0000001234,00 --> (10,-1)
					{
						for (i=0;i<getLength();i++) fmt.append("0");
						fmt.append(".00"); // for the .00
					}
				}
				else
				if (getPrecision()==0) // No decimals 0000001234 --> (10,0)
				{
					for (i=0;i<getLength();i++) fmt.append("0"); // all zeroes.
				}
				else  // Floating point format   00001234,56  --> (12,2)
				{
						for (i=0;i<=getLength();i++) fmt.append("0"); // all zeroes.
						fmt.setCharAt(getLength()-getPrecision()+1-(value.getInteger()<0?1:0), '.');     // one 'comma'
				}
				form= new DecimalFormat(fmt.toString());
				retval=form.format(value.getInteger());
			}
		}
		return retval;
	}
    
    /**
     * Converts a BigNumber value to a String, optionally padding the result to the specified length. // TODO: BigNumber padding
     * @param pad true if you want to pad the resulting string to length.
     * @return a String optionally padded to the specified length.
     */
    private String toStringBigNumber(boolean pad)
    {
        if (value.getBigNumber()==null) return null;
        String retval = value.getString();

        // Localise . to ,
        if (Const.DEFAULT_DECIMAL_SEPARATOR!='.')
        {
            retval = retval.replace('.', Const.DEFAULT_DECIMAL_SEPARATOR);
        }
        
        return retval;
    }

			
	/**
	 * Sets the length of the Number, Integer or String to the specified length
	 * Note: no truncation of the value takes place, this is meta-data only!
	 * @param l the length to which you want to set the Value.
	 */
	public void setLength(int l)
	{
		if (value==null) return;
		value.setLength(l);
	}
	
	/**
	 * Sets the length and the precision of the Number, Integer or String to the specified length & precision
	 * Note: no truncation of the value takes place, this is meta-data only!
	 * @param l the length to which you want to set the Value.
	 * @param p the precision to which you want to set this Value
	 */
	public void setLength(int l, int p)
	{
		if (value==null) return;
		value.setLength(l,p);
	}
	
	/**
	 * Get the length of this Value.
	 * @return the length of this Value.
	 */
	public int getLength()
	{
		if (value==null) return -1;
		return value.getLength();
	}
	
	/**
	 * get the precision of this Value
	 * @return the precision of this Value.
	 */
	public int getPrecision()
	{
		if (value==null) return -1;
		return value.getPrecision();
	}

	/**
	 * Sets the precision of this Value
	 * Note: no rounding or truncation takes place, this is meta-data only!
	 * @param p the precision to which you want to set this Value.
	 */
	public void setPrecision(int p)
	{
		if (value==null) return;
		value.setPrecision(p);
	}
	
	/**
	 * Return the type of a value in a textual form: "String", "Number", "Integer", "Boolean", "Date", ...
	 * @return A String describing the type of value.
	 */
	public String getTypeDesc()
	{
		if (value==null) return "Unknown";
		return value.getTypeDesc();
	}

	/**
	 * Return the type of a value in a textual form: "String", "Number", "Integer", "Boolean", "Date", ... given a certain integer type
	 * @param t the type to convert to text. 
	 * @return A String describing the type of a certain value.
	 */
	public static final String getTypeDesc(int t)
	{
		return value_type_desc[t];
	}
	
	/**
	 * Convert the String description of a type to an integer type.
	 * @param desc The description of the type to convert
	 * @return The integer type of the given String.  (Value.TYPE_VALUE_...)
	 */
	public static final int getType(String desc)
	{
		int i;
		
		for (i=1;i<value_type_desc.length;i++)
		{
			if (value_type_desc[i].equalsIgnoreCase(desc))
			{
				return i; 
			}
		}
		
		return VALUE_TYPE_NONE;
	}
	
	/**
	 * get an array of String describing the possible types a Value can have.
	 * @return an array of String describing the possible types a Value can have.
	 */
	public static final String[] getTypes()
	{
		String retval[] = new String[value_type_desc.length-1];
		for (int i=1;i<value_type_desc.length;i++)
		{
			retval[i-1]=value_type_desc[i];
		}
		return retval;
	}

	/**
	 * get an array of String describing the possible types a Value can have.
	 * @return an array of String describing the possible types a Value can have.
	 */
	public static final String[] getAllTypes()
	{
		String retval[] = new String[value_type_desc.length];
		for (int i=0;i<value_type_desc.length;i++)
		{
			retval[i]=value_type_desc[i];
		}
		return retval;
	}

	/**
	 * Sets the Value to null, no type is being changed.
	 *
	 */
	public void setNull()
	{
		setNull(true);
	}
	
	/**
	 * Sets or unsets a value to null, no type is being changed.
	 * @param n true if you want the value to be null, false if you don't want this to be the case.
	 */
	public void setNull(boolean n)
	{
		NULL=n;
	}
	
	/**
	 * Checks wheter or not a value is null.
	 * @return true if the Value is null.
	 */
	public boolean isNull()
	{
		return NULL;
	}
    
    /**
     * Write the object to an ObjectOutputStream
     * @param out
     * @throws IOException
     */
    private void writeObject(java.io.ObjectOutputStream out) throws IOException
    {
        writeObj(new DataOutputStream(out));
    }
    
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        readObj(new DataInputStream(in));
    }

    public void writeObj(DataOutputStream dos) throws IOException
    {
        int type=getType();

        // Handle type
        dos.writeInt( getType() );
        
        // Handle name-length
        dos.writeInt( name.length() );  
        
        // Write name
        dos.writeChars( name );

        // length & precision
        dos.writeInt( getLength() );
        dos.writeInt( getPrecision() );

        // NULL?
        dos.writeBoolean(isNull());
        
        // Handle Content -- only when not NULL
        if (!isNull())
        {
            switch(type)
            {
            case VALUE_TYPE_STRING : 
            case VALUE_TYPE_BIGNUMBER:
                if (getString()!=null && getString().getBytes().length>0) 
                {
                    dos.writeInt(getString().getBytes().length);
                    dos.writeChars(getString());
                }
                else
                {
                    dos.writeInt(0);
                }
                break;
            case VALUE_TYPE_DATE   : 
                dos.writeBoolean(getDate()!=null);
                if (getDate()!=null)   
                {
                    dos.writeLong(getDate().getTime());
                }
                break;
            case VALUE_TYPE_NUMBER : 
                dos.writeDouble(getNumber()); 
                break;
            case VALUE_TYPE_BOOLEAN: 
                dos.writeBoolean(getBoolean()); 
                break;
            case VALUE_TYPE_INTEGER: 
                dos.writeLong(getInteger()); 
                break;
            default: break; // nothing
            }
        }
    }
	
	/**
	 * Write the value, including the meta-data to a DataOutputStream
	 * @param outputStream the OutputStream to write to .
	 * @throws KettleFileException if something goes wrong.
	 */
	public void write(OutputStream outputStream) throws KettleFileException
	{
		try
		{
            writeObj(new DataOutputStream(outputStream));
		}
		catch(Exception e)
		{
			throw new KettleFileException("Unable to write value to output stream", e);
		}
	}
    
    public void readObj(DataInputStream dis) throws IOException
    {
        // type
        int theType = dis.readInt(); 
        newValue(theType);
        
        // name-length
        int nameLength=dis.readInt();  
        
        // name
        StringBuffer nameBuffer=new StringBuffer();
        for (int i=0;i<nameLength;i++) nameBuffer.append( dis.readChar() );
        setName(new String(nameBuffer));
        
        // length & precision
        setLength( dis.readInt(), dis.readInt() ); 
        
        // Null?
        setNull( dis.readBoolean() );
        
        // Read the values
        if (!isNull())
        {
            switch(getType())
            {
            case VALUE_TYPE_STRING:
            case VALUE_TYPE_BIGNUMBER:
                // Handle lengths
                int dataLength=dis.readInt();
                if (dataLength>0)
                {
                    StringBuffer   val_string=new StringBuffer();
                    for (int i=0;i<dataLength;i++) val_string.append(dis.readChar());
                    setValue( val_string.toString() );
                }
                if (theType==VALUE_TYPE_BIGNUMBER) 
                {
                    try
                    {
                        convertString(theType);
                    }
                    catch(KettleValueException e)
                    {
                        throw new IOException("Unable to convert String to BigNumber while reading from data input stream ["+getString()+"]");
                    }
                }
                break;
            case VALUE_TYPE_DATE:
                if (dis.readBoolean())
                {
                    setValue(new Date(dis.readLong()));
                }
                break;
            case VALUE_TYPE_NUMBER:
                setValue( dis.readDouble() ); 
                break;
            case VALUE_TYPE_INTEGER:
                setValue( dis.readLong() );
                break;
            case VALUE_TYPE_BOOLEAN:
                setValue( dis.readBoolean() );
                break;
            }
        }
    }

    /**
     * Read the Value, including meta-data from a DataInputStream
     * @param is The InputStream to read the value from
     * @throws KettleFileException when the Value couldn't be created by reading it from the DataInputStream.
     */
	public Value(InputStream is) throws KettleFileException
	{
		try
		{
            readObj(new DataInputStream(is));
		}
		catch(EOFException e)
		{
			throw new KettleEOFException("End of file reached", e);
		}
		catch(Exception e)
		{
			throw new KettleFileException("Error reading from data input stream", e);
		}
	}

	/**
	 * Write the data of this Value, without the meta-data to a DataOutputStream
	 * @param dos The DataOutputStream to write the data to
	 * @return true if all went well, false if something went wrong.
	 */
	public boolean writeData(DataOutputStream dos) throws KettleFileException
	{
		try
		{
			// Is the value NULL?
			dos.writeBoolean(isNull());
			
			// Handle Content -- only when not NULL
			if (!isNull())
			{
				switch(getType())
				{
				case VALUE_TYPE_STRING : 
                case VALUE_TYPE_BIGNUMBER:
					if (getString()!=null && getString().getBytes().length>0) 
					{
						dos.writeInt(getString().getBytes().length);
						dos.writeChars(getString());
					}
					else
					{
						dos.writeInt(0);
					}
					break;
				case VALUE_TYPE_DATE   : 
					dos.writeBoolean(getDate()!=null);
					if (getDate()!=null)   
					{
						dos.writeLong(getDate().getTime());
					}
					break;
				case VALUE_TYPE_NUMBER : 
					dos.writeDouble(getNumber()); 
					break;
				case VALUE_TYPE_BOOLEAN: 
					dos.writeBoolean(getBoolean()); 
					break;
				case VALUE_TYPE_INTEGER: 
					dos.writeLong(getInteger()); 
					break;
				default: break; // nothing
				}
			}
		}
		catch(IOException e)
		{
			throw new KettleFileException("Unable to write value data to output stream", e);
		}
		
		return true;
	}

	/**
	 * Read the data of a Value from a DataInputStream, the meta-data of the value has to be set before calling this method!
	 * @param dis the DataInputStream to read from
	 * @throws KettleFileException when the value couldn't be read from the DataInputStream
	 */
	public Value(Value metaData, DataInputStream dis) throws KettleFileException
	{
		setValue(metaData);
		setName(metaData.getName());
		
		try
		{
			// Is the value NULL?
			setNull(dis.readBoolean());
						
			// Read the values
			if (!isNull())
			{
				switch(getType())
				{
				case VALUE_TYPE_STRING:
					// Handle lengths
					int dataLength=dis.readInt();
					if (dataLength>0)
					{
						StringBuffer   val_string=new StringBuffer();
						for (int i=0;i<dataLength;i++) val_string.append(dis.readChar());
						setValue( val_string.toString() );
					}
                    if (metaData.isBigNumber()) convertString(metaData.getType());
					break;
				case VALUE_TYPE_DATE:
					if (dis.readBoolean())
					{
						setValue(new Date(dis.readLong()));
					}
					break;
				case VALUE_TYPE_NUMBER:
					setValue( dis.readDouble() ); 
					break;
				case VALUE_TYPE_INTEGER:
					setValue( dis.readLong() );
					break;
				case VALUE_TYPE_BOOLEAN:
					setValue( dis.readBoolean() );
					break;
				}
			}			
		}
		catch(EOFException e)
		{
			throw new KettleEOFException("End of file reached", e);
		}
		catch(Exception e)
		{
			throw new KettleEOFException("Error reading value data from stream", e);
		}
	}

    /**
     * Compare 2 values of the same or different type!
     * The comparison of Strings is case insensitive
     * @param v the value to compare with.
     * @return -1 if The value was smaller, 1 bigger and 0 if both values are equal.
     */
    public int compare(Value v)
    {
        return compare(v, true);
    }

	/**
	 * Compare 2 values of the same or different type!
	 * @param v the value to compare with.
     * @param caseInsensitive True if you want the comparison to be case insensitive
	 * @return -1 if The value was smaller, 1 bigger and 0 if both values are equal.
	 */
	public int compare(Value v, boolean caseInsensitive)
	{
		boolean n1 =   isNull() || (  isString() && (   getString()==null ||   getString().length()==0 )) || (  isDate() &&   getDate()==null) || (  isBigNumber() &&   getBigNumber()==null);
		boolean n2 = v.isNull() || (v.isString() && ( v.getString()==null || v.getString().length()==0 )) || (v.isDate() && v.getDate()==null) || (v.isBigNumber() && v.getBigNumber()==null);
		
		// null is always smaller! 
		if ( n1 && !n2) return -1; 
		if (!n1 &&  n2) return  1; 
		if ( n1 &&  n2) return  0; 
		
		switch(getType())
		{
		case VALUE_TYPE_BOOLEAN:
		    {
    			if ( getBoolean() &&  v.getBoolean() || 
    			    !getBoolean() && !v.getBoolean()) return  0;  // true == true, false == false
    			if ( getBoolean() && !v.getBoolean()) return  1;  // true  > false
    			return -1;  // false < true
            }

		case VALUE_TYPE_DATE   :
            {
			    return Double.compare(getNumber(), v.getNumber());
            }
			
		case VALUE_TYPE_NUMBER :
            {
			    return Double.compare(getNumber(), v.getNumber());
            }

		case VALUE_TYPE_STRING:
            {
    			String one = Const.rtrim(getString());
    			String two = Const.rtrim(v.getString());
                
                if (caseInsensitive) 
                    return one.compareToIgnoreCase(two);
                else 
                    return one.compareTo(two);
            }
            
		case VALUE_TYPE_INTEGER:
            {
			    return Double.compare(getNumber(), v.getNumber());
            }

        case VALUE_TYPE_BIGNUMBER:
            {
                return getBigNumber().compareTo(v.getBigNumber());
            }
		}
		
		// Still here?  Not possible!  But hey, give back 0, mkay?
		
		return 0;
	}
	
	public boolean equals(Object v)
	{
		if (compare((Value)v)==0) 
			return true; 
		else 
			return false;
	}
	
	/**
	 * Check whether this value is equal to the String supplied.
	 * @param string The string to check for equality
	 * @return true if the String representation of the value is equal to string. (ignoring case)
	 */
	public boolean isEqualTo(String string)
	{
		return getString().equalsIgnoreCase(string);
	}

    /**
     * Check whether this value is equal to the BigDecimal supplied.
     * @param number The BigDecimal to check for equality
     * @return true if the BigDecimal representation of the value is equal to number. 
     */
    public boolean isEqualTo(BigDecimal number)
    {
        return getBigNumber().equals(number);
    }
    
	/**
	 * Check whether this value is equal to the Number supplied.
	 * @param number The Number to check for equality
	 * @return true if the Number representation of the value is equal to number. 
	 */
	public boolean isEqualTo(double number)
	{
		return getNumber() == number;
	}

	/**
	 * Check whether this value is equal to the Integer supplied.
	 * @param number The Integer to check for equality
	 * @return true if the Integer representation of the value is equal to number. 
	 */
	public boolean isEqualTo(long number)
	{
		return getInteger() == number;
	}

	/**
	 * Check whether this value is equal to the Integer supplied.
	 * @param number The Integer to check for equality
	 * @return true if the Integer representation of the value is equal to number. 
	 */
	public boolean isEqualTo(int number)
	{
		return getInteger() == number;
	}

	/**
	 * Check whether this value is equal to the Integer supplied.
	 * @param number The Integer to check for equality
	 * @return true if the Integer representation of the value is equal to number. 
	 */
	public boolean isEqualTo(byte number)
	{
		return getInteger() == number;
	}

	/**
	 * Check whether this value is equal to the Date supplied.
	 * @param date The Date to check for equality
	 * @return true if the Date representation of the value is equal to date. 
	 */
	public boolean isEqualTo(Date date)
	{
		return getDate() == date;
	}

	public int hashCode()
	{
		int hash=0; // name.hashCode(); -> Name shouldn't be part of hashCode()!
		
		if (isNull())
		{
			switch(getType())
			{
			case VALUE_TYPE_BOOLEAN   : hash^= 1; break;
			case VALUE_TYPE_DATE      : hash^= 2; break;
			case VALUE_TYPE_NUMBER    : hash^= 4; break;
			case VALUE_TYPE_STRING    : hash^= 8; break;
			case VALUE_TYPE_INTEGER   : hash^=16; break;
            case VALUE_TYPE_BIGNUMBER : hash^=32; break;
			case VALUE_TYPE_NONE      : break;
			default: break;
			}
		}
		else
		{
			switch(getType())
			{
			case VALUE_TYPE_BOOLEAN   :                           hash^=Boolean.valueOf(getBoolean()).hashCode(); break;
			case VALUE_TYPE_DATE      : if (getDate()!=null)      hash^=getDate().hashCode(); break;
			case VALUE_TYPE_INTEGER   :
			case VALUE_TYPE_NUMBER    :                           hash^=(new Double(getNumber())).hashCode(); break;
			case VALUE_TYPE_STRING    : if (getString()!=null)    hash^=getString().hashCode(); break;
			case VALUE_TYPE_BIGNUMBER : if (getBigNumber()!=null) hash^=getBigNumber().hashCode(); break;
			case VALUE_TYPE_NONE      : break;
			default: break;
			}
		}
		
		return hash;
	}
	


	// OPERATORS & COMPARATORS
	
	public Value and(Value v)
	{
		long n1 = getInteger();
		long n2 = v.getInteger();
		
		long res = n1 & n2;
		
		setValue(res);
		
		return this;
	}

	public Value xor(Value v)
	{
		long n1 = getInteger();
		long n2 = v.getInteger();
		
		long res = n1 ^ n2;
		
		setValue(res);
		
		return this;
	}
	
	public Value or(Value v)
	{
		long n1 = getInteger();
		long n2 = v.getInteger();
		
		long res = n1 | n2;
		
		setValue(res);
		
		return this;
	}

	public Value bool_and(Value v)
	{
		boolean b1 = getBoolean();
		boolean b2 = v.getBoolean();
		
		boolean res = b1 && b2;
		
		setValue(res);
		
		return this;
	}
	
	public Value bool_or(Value v)
	{
		boolean b1 = getBoolean();
		boolean b2 = v.getBoolean();
		
		boolean res = b1 || b2;
		
		setValue(res);
		
		return this;
	}

	public Value bool_xor(Value v)
	{
		boolean b1 = getBoolean();
		boolean b2 = v.getBoolean();
		
		boolean res = b1&&b2 ? false : !b1&&!b2 ? false : true;
		
		setValue(res);
		
		return this;
	}

	public Value bool_not()
	{
		if (getBoolean()) value.setBoolean(!getBoolean());		
		return this;
	}
	
	
	public Value greater_equal(Value v)
	{
		if (compare(v)>=0) setValue(true); else setValue(false);
		return this;
	}
	
	public Value smaller_equal(Value v)
	{
		if (compare(v)<=0) setValue(true); else setValue(false);
		return this;
	}
	
	public Value different(Value v)
	{
		if (compare(v)!=0) setValue(true); else setValue(false);
		return this;
	}
	
	public Value equal(Value v)
	{
		if (compare(v)==0) setValue(true); else setValue(false);
		return this;
	}
	
	public Value like(Value v)
	{
		String cmp=v.getString();
				
		// Is cmp part of look?
		int idx=getString().indexOf(cmp);
			
		if (idx<0) setValue(false); else setValue(true);

		return this;
	}
	
	public Value greater(Value v)
	{
		if (compare(v)>0) setValue(true); else setValue(false);
		return this;
	}
	
	public Value smaller(Value v)
	{
		if (compare(v)<0) setValue(true); else setValue(false);
		return this;
	}
	
	public Value minus(BigDecimal v) throws KettleValueException { return minus(new Value("tmp", v)); }
    public Value minus(double     v) throws KettleValueException { return minus(new Value("tmp", v)); }
	public Value minus(long       v) throws KettleValueException { return minus(new Value("tmp", v)); }
	public Value minus(int        v) throws KettleValueException { return minus(new Value("tmp", (long)v)); }
	public Value minus(byte       v) throws KettleValueException { return minus(new Value("tmp", (long)v)); }
	public Value minus(Value  v) throws KettleValueException
	{
		switch(getType())
		{
            case VALUE_TYPE_BIGNUMBER : value.setBigNumber(getBigNumber().subtract(v.getBigNumber())); break;
			case VALUE_TYPE_NUMBER    : value.setNumber(getNumber()-v.getNumber()); break;
			case VALUE_TYPE_INTEGER   : value.setInteger(getInteger()-v.getInteger()); break;
			case VALUE_TYPE_BOOLEAN   : 
			case VALUE_TYPE_STRING    : 
			default:
				throw new KettleValueException("Subtraction can only be done with numbers!");
		}
		return this;
	}

    public Value plus(BigDecimal v) { return plus(new Value("tmp", v)); }
	public Value plus(double     v) { return plus(new Value("tmp", v)); }
	public Value plus(long       v) { return plus(new Value("tmp", v)); }
	public Value plus(int        v) { return plus(new Value("tmp", (long)v)); }
	public Value plus(byte       v) { return plus(new Value("tmp", (long)v)); }
	public Value plus(Value v)
	{
		switch(getType())
		{
            case VALUE_TYPE_BIGNUMBER : setValue(getBigNumber().add(v.getBigNumber())); break;
			case VALUE_TYPE_NUMBER    : setValue(getNumber()+v.getNumber()); break;
			case VALUE_TYPE_INTEGER   : setValue(getInteger()+v.getInteger()); break;
			case VALUE_TYPE_BOOLEAN   : setValue(getBoolean()|v.getBoolean()); break;
			case VALUE_TYPE_STRING    : setValue(getString()+v.getString()); break;
			default: break;
		}
		return this;
	}
	
    public Value divide(BigDecimal v) throws KettleValueException { return divide(new Value("tmp", v)); }
	public Value divide(double     v) throws KettleValueException { return divide(new Value("tmp", v)); }
	public Value divide(long       v) throws KettleValueException { return divide(new Value("tmp", v)); }
	public Value divide(int        v) throws KettleValueException { return divide(new Value("tmp", (long)v)); }
	public Value divide(byte       v) throws KettleValueException { return divide(new Value("tmp", (long)v)); }
	public Value divide(Value v)  throws KettleValueException
	{
		switch(getType())
		{
            case VALUE_TYPE_BIGNUMBER : setValue(getBigNumber().divide(v.getBigNumber(), BigDecimal.ROUND_UP)); break;
			case VALUE_TYPE_NUMBER    : setValue(getNumber()/v.getNumber()); break;
			case VALUE_TYPE_INTEGER   : setValue(getInteger()/v.getInteger()); break;
			case VALUE_TYPE_BOOLEAN   : 
			case VALUE_TYPE_STRING    : 
			default:
				throw new KettleValueException("Division can only be done with numeric data!");
		}
		return this;
	}
	
    public Value multiply(BigDecimal v) throws KettleValueException { return multiply(new Value("tmp", v)); }
    public Value multiply(double     v) throws KettleValueException { return multiply(new Value("tmp", v)); }
	public Value multiply(long       v) throws KettleValueException { return multiply(new Value("tmp", v)); }
	public Value multiply(int        v) throws KettleValueException { return multiply(new Value("tmp", (long)v)); }
	public Value multiply(byte       v) throws KettleValueException { return multiply(new Value("tmp", (long)v)); }
	public Value multiply(Value      v) throws KettleValueException
	{
		// a number and a string!
		if ((v.isString() && isNumeric()) || (v.isNumeric() && isString()))
		{
			StringBuffer s;
			String append="";
			int n;
			if (v.isString())
			{ 
				s=new StringBuffer(v.getString());
				append=v.getString();
				n=(int)getInteger();
			}
			else
			{
				s=new StringBuffer(getString());
				append=getString();
				n=(int)v.getInteger();
			}
			
			if (n==0) s.setLength(0);
			else
			for (int i=1;i<n;i++) s.append(append);
			
			setValue(s);
		}
		else
        // big numbers
        if (isBigNumber() || v.isBigNumber())
        {
            setValue( getBigNumber().multiply(v.getBigNumber()) );
        }
        else
		// numbers
		if (isNumber() || v.isNumber())
		{
			setValue( getNumber()*v.getNumber() );
		}
		else
		// integers
		if (isInteger() || v.isInteger())
		{
			setValue( getInteger()*v.getInteger() );
		}
		else
		{
			 throw new KettleValueException("Multiplication can only be done with numbers or a number and a string!");
		}
		return this;
	}
	
	
	
	// FUNCTIONS!!
	

	// implement the ABS function, arguments in args[]
	public Value abs() throws KettleValueException
	{
		if (isNull()) return this;

        if (isBigNumber())
        {
            setValue(getBigNumber().abs());
        }
        else
		if (isNumber())
		{
			setValue(Math.abs(getNumber()));
		}
		else
		if (isInteger())
		{
			setValue(Math.abs(getInteger()));
		}
		else
		{
			throw new KettleValueException("Function ABS only works with a number");
		}
		return this;
	}

	// implement the ACOS function, arguments in args[]
	public Value acos() throws KettleValueException
	{
		if (isNull()) return this;

		if (isNumeric())
		{
			setValue(Math.acos(getNumber()));
		}
		else
		{
			throw new KettleValueException("Function ACOS only works with numeric data");
		}
		return this;
	}

	// implement the ASIN function, arguments in args[]
	public Value asin() throws KettleValueException
	{
		if (isNull()) return this;

		if (isNumeric())
		{
			setValue(Math.asin(getNumber()));
		}
		else
		{
			 throw new KettleValueException("Function ASIN only works with numeric data");
		}
		return this;
	}

	// implement the ATAN function, arguments in args[]
	public Value atan() throws KettleValueException
	{
		if (isNull()) return this;

		if (isNumeric())
		{
			setValue(Math.atan(getNumber()));
		}
		else
		{
			 throw new KettleValueException("Function ATAN only works with numeric data");
		}
		return this;
	}

	// implement the ATAN2 function, arguments in args[]
	public Value atan2(Value  arg0) throws KettleValueException { return atan2(arg0.getNumber()); }
	public Value atan2(double arg0) throws KettleValueException
	{
		if (isNull()) return this;
		
		if (isNumeric())
		{
			setValue(Math.atan2(getNumber(), arg0));
		}
		else
		{
			 throw new KettleValueException("Function ATAN2 only works with numbers");
		}
		return this;
	}

	// implement the CEIL function, arguments in args[]
	public Value ceil() throws KettleValueException
	{
		if (isNull()) return this;
		
		if (isNumeric())
		{
			setValue(Math.ceil(getNumber()));
		}
		else
		{
			 throw new KettleValueException("Function CEIL only works with a number");
		}
		return this;
	}

	// implement the COS function, arguments in args[]
	public Value cos() throws KettleValueException
	{
		if (isNull()) return this;
		
		if (isNumeric())
		{
			setValue(Math.cos(getNumber()));
		}
		else
		{
			throw new KettleValueException("Function COS only works with a number");
		}
		return this;
	}

	// implement the EXP function, arguments in args[]
	public Value exp() throws KettleValueException
	{
		if (isNull()) return this;
		
		if (isNumeric())
		{
			setValue(Math.exp(getNumber()));
		}
		else
		{
			throw new KettleValueException("Function EXP only works with a number");
		}
		return this;
	}

	// implement the FLOOR function, arguments in args[]
	public Value floor() throws KettleValueException
	{
		if (isNull()) return this;
		
		if (isNumeric())
		{
			setValue(Math.floor(getNumber()));
		}
		else
		{
			 throw new KettleValueException("Function FLOOR only works with a number");
		}
		return this;
	}

	// implement the INITCAP function, arguments in args[]
	public Value initcap() throws KettleValueException
	{
		if (isNull()) return this;
		
		if (getType()==VALUE_TYPE_STRING)
		{
			if (getString()==null)
			{
				setNull();
			}
			else
			{
				StringBuffer change=new StringBuffer(getString());
				boolean new_word;
				int i;
				char lower, upper, ch;
					
				new_word=true;
				for (i=0 ; i<getString().length() ; i++)
				{
					lower=change.substring(i,i+1).toLowerCase().charAt(0); // Lowercase is default.
					upper=change.substring(i,i+1).toUpperCase().charAt(0); // Uppercase for new words.
					ch=upper;
			
					if (new_word)
					{ 
					  change.setCharAt(i, upper);
					}
					else
					{          
					  change.setCharAt(i, lower);  
					}
	
					new_word = false;
			
					if ( !(ch>='A' && ch<='Z') && 
						 !(ch>='0' && ch<='9') &&
						 ch!='_'
					   ) new_word = true;
				}
			
				setValue( change.toString() );
			}
		}
		else
		{
			throw new KettleValueException("Function INITCAP only works with a string");
		}
		return this;
	}

	// implement the LENGTH function, arguments in args[]
	public Value length() throws KettleValueException
	{
		if (isNull()) 
		{
			setType(VALUE_TYPE_INTEGER);
			setValue(0L);
			return this;
		} 
		
		if (getType()==VALUE_TYPE_STRING)
		{
			setValue((double)getString().length());
		}
		else
		{
			 throw new KettleValueException("Function LENGTH only works with a string");
		}
		return this;
	}

	// implement the LOG function, arguments in args[]
	public Value log() throws KettleValueException
	{
		if (isNull()) return this;
		
		if (isNumeric())
		{
			setValue(Math.log(getNumber()));
		}
		else
		{
			throw new KettleValueException("Function LOG only works with a number");
		}
		
		return this;
	}

	// implement the LOWER function, arguments in args[]
	public Value lower()
	{
		if (isNull())
		{
			setType(VALUE_TYPE_STRING);
		}
		else
		{
			setValue( getString().toLowerCase() );
		}

		return this;
	}


	// implement the LPAD function: left pad strings or numbers...
	public Value lpad(Value len) { return lpad((int)len.getNumber(), " "); }
	public Value lpad(Value len, Value padstr) { return lpad((int)len.getNumber(), padstr.getString());	}
	public Value lpad(int len) { return lpad(len, " "); }
	public Value lpad(int len, String padstr)
	{
		if (isNull())
		{
			setType(VALUE_TYPE_STRING);
		}
		else
		{
			if (getType()!=VALUE_TYPE_STRING) // also lpad other types!
			{
				setValue(getString());
			}
			
			if (getString()!=null)
			{
				StringBuffer result = new StringBuffer(getString());
		
				int pad=len;
				int l= ( pad-result.length() ) / padstr.length() + 1;
				int i;
				
				for (i=0;i<l;i++) result.insert(0, padstr);
							
				// Maybe we added one or two too many!
				i=result.length();
				while (i>pad && pad>0)
				{
					result.deleteCharAt(0);
					i--;
				}
				setValue(result.toString());
			}
			else
			{
				setNull();
			}
		}
		setLength(len);

		return this;
	}

	// implement the LTRIM function
	public Value ltrim()
	{
		if (isNull())
		{
			setType(VALUE_TYPE_STRING);
		}
		else
		{
			if (getString()!=null)
			{
				StringBuffer s;
				if (getType()==VALUE_TYPE_STRING)
				{
					s = new StringBuffer(getString());
				}
				else
				{
					s = new StringBuffer(toString());
				}
				
				// Left trim
				while (s.length()>0 && s.charAt(0)==' ') s.deleteCharAt(0);
				
				setValue(s);
			}
			else
			{
				setNull();
			}
		}
		
		return this;
	}

	// implement the MOD function, arguments in args[]
	public Value mod(Value      arg)  throws KettleValueException { return mod(arg.getNumber()); }
    public Value mod(BigDecimal arg)  throws KettleValueException { return mod(arg.doubleValue()); }
    public Value mod(long       arg)  throws KettleValueException { return mod((double)arg); }
    public Value mod(int        arg)  throws KettleValueException { return mod((double)arg); }
    public Value mod(byte       arg)  throws KettleValueException { return mod((double)arg); }
	public Value mod(double arg0) throws KettleValueException
	{
		if (isNull()) return this;
		
		if (isNumeric())
		{
			double n1=getNumber();
			double n2=arg0;
			
			setValue( n1 - (n2 * Math.floor(n1 /n2 )) );
		}
		else
		{
			 throw new KettleValueException("Function MOD only works with numeric data");
		}

		return this;
	}

	// implement the NVL function, arguments in args[]
	public Value nvl(Value alt)
	{
		if (isNull()) setValue(alt);
		return this;
	}

	// implement the POWER function, arguments in args[]
	public Value power(BigDecimal arg) throws KettleValueException{ return power(new Value("tmp", arg)); }
    public Value power(double     arg) throws KettleValueException{ return power(new Value("tmp", arg)); }
	public Value power(Value v) throws KettleValueException
	{
		if (isNull()) return this;
		
        else if (isNumeric())
		{
			setValue( Math.pow(getNumber(), v.getNumber()) );
		}
		else
		{
			throw new KettleValueException("Function POWER only works with numeric data");
		}
		return this;
	}

	// implement the REPLACE function, arguments in args[]
	public Value replace(Value repl, Value with) { return replace(repl.getString(), with.getString()); }
	public Value replace(String repl, String with)
	{
		if (isNull()) return this;
		if (getString()==null) 
		{
			setNull();
		}
		else
		{
			setValue( Const.replace(getString(), repl, with) );
		}
		return this;
	}

	/**
	 * Rounds of to the nearest integer.<p>  
	 * See also: java.lang.Math.round()
	 * 
	 * @return The rounded Number value.
	 */
	public Value round() throws KettleValueException
	{
		if (isNull()) return this;

		if (isNumeric())
		{
			setValue( (double)Math.round(getNumber()) );
		}
		else
		{
			throw new KettleValueException("Function ROUND only works with a number");
		}
		return this;
	}
    
    /**
     * Rounds the Number value to a certain number decimal places.
     * @param decimalPlaces
     * @return The rounded Number Value
     * @throws KettleValueException in case it's not a number (or other problem).
     */
    public Value round(int decimalPlaces) throws KettleValueException
    {
        if (isNull()) return this;
        
        if (isNumeric())
        {
            if (isBigNumber())
            {
                // Multiply by 10^decimalPlaces
                // For example 123.458343938437, Decimalplaces = 2
                // 
                BigDecimal bigDec = getBigNumber();
                // System.out.println("ROUND decimalPlaces : "+decimalPlaces+", bigNumber = "+bigDec);
                bigDec = bigDec.setScale(decimalPlaces, BigDecimal.ROUND_HALF_UP); 
                // System.out.println("ROUND finished result         : "+bigDec);
                setValue( bigDec );
            }
            else
            {
                setValue( (double)Const.round(getNumber(), decimalPlaces) );
            }
        }
        else
        {
            throw new KettleValueException("Function ROUND only works with a number");
        }
        return this;

    }

	// implement the RPAD function, arguments in args[]
	public Value rpad(Value len) { return rpad((int)len.getNumber(), " "); }
	public Value rpad(Value len, Value padstr) { return rpad((int)len.getNumber(), padstr.getString());	}
	public Value rpad(int len) { return rpad(len, " "); }
	public Value rpad(int len, String padstr)
	{
		if (isNull())
		{
			setType(VALUE_TYPE_STRING);
		}
		else
		{
			if (getType()!=VALUE_TYPE_STRING) // also rpad other types!
			{
				setValue(getString());
			}
			if (getString()!=null)
			{
				StringBuffer result = new StringBuffer(getString());
		
				int pad=len;
				int l= ( pad-result.length() ) / padstr.length() + 1;
				int i;
				
				for (i=0;i<l;i++) result.append(padstr);
				
				// Maybe we added one or two too many!
				i=result.length();
				while (i>pad && pad>0)
				{
					result.deleteCharAt(i-1);
					i--;
				}
				setValue(result.toString());
			}
			else
			{
				setNull();
			}
		}
		setLength(len);

		return this;
	}

	// implement the RTRIM function, arguments in args[]
	public Value rtrim()
	{
		if (isNull())
		{
			setType(VALUE_TYPE_STRING);
		}
		else
		{
			StringBuffer s;
			if (getType()==VALUE_TYPE_STRING)
			{
				s = new StringBuffer(getString());
			}
			else
			{
				s = new StringBuffer(toString());
			}
			
			// Rigth trim
			while (s.length()>0 && s.charAt(s.length()-1)==' ') s.deleteCharAt(s.length()-1);
			
			setValue(s);
		}
		return this;
	}

	// implement the SIGN function, arguments in args[]
	public Value sign() throws KettleValueException
	{
		if (isNull()) return this;
        if (isNumber())
        {
            int cmp = getBigNumber().compareTo(new BigDecimal(0L)); 
            if (cmp>0) value.setBigNumber(new BigDecimal(1L));
            else if (cmp<0) value.setBigNumber(new BigDecimal(-1L));
            else value.setBigNumber(new BigDecimal(0L));
        }
        else
		if (isNumber())
		{
			if (getNumber()>0) value.setNumber(1.0);
			else if (getNumber()<0) value.setNumber(-1.0);
			else value.setNumber(0.0);
		}
		else
		if (isInteger())
		{
			if (getInteger()>0) value.setInteger(1);
			else if (getInteger()<0) value.setInteger(-1);
			else value.setInteger(0);
		}
		else
		{
			throw new KettleValueException("Function SIGN only works with a number");
		}
		
		return this;
	}

	// implement the SIN function, arguments in args[]
	public Value sin() throws KettleValueException
	{
		if (isNull()) return this;
		if (isNumeric())
		{
			setValue( Math.sin(getNumber()) );
		}
		else
		{
			throw new KettleValueException("Function SIN only works with a number");
		}

		return this;
	}

	// implement the SQRT function, arguments in args[]
	public Value sqrt() throws KettleValueException
	{
		if (isNull()) return this;
		if (isNumeric())
		{
			setValue( Math.sqrt(getNumber()) );
		}
		else
		{
			throw new KettleValueException("Function SQRT only works with a number");
		}

		return this;
	}

	// implement the SUBSTR function, arguments in args[]
	public Value substr(Value from, Value to) { return substr((int)from.getNumber(), (int)to.getNumber()); }
	public Value substr(Value from) { return substr((int)from.getNumber(), -1); }
	public Value substr(int from) { return substr(from, -1);  }
	public Value substr(int from, int to)
	{
		if (isNull()) 
		{
			setType(VALUE_TYPE_STRING);
			return this;
		} 

		setValue( getString() );
		
		if (getString()!=null)
		{
            if (to<0 && from>=0) 
            {
                setValue( getString().substring(from) );
            }
            else if (to>=0 && from>=0)
            {
                setValue( getString().substring(from, to) );
            }
		}
		else
		{
			setNull();
		}
		if (!isString()) setType(VALUE_TYPE_STRING);
		
		return this;
	}

	// implement the RIGHTSTR function, arguments in args[]
	public Value rightstr(Value len) { return rightstr((int)len.getNumber()); }
	public Value rightstr(int len) 
	{
		if (isNull()) 
		{
			setType(VALUE_TYPE_STRING);
			return this;
		} 

		setValue( getString() );
		
		int tot_len = getString()!=null?getString().length():0;
		
		if (tot_len>0)
		{
			int totlen = getString().length();
			
			int f = totlen-len;
			if (f<0) f=0;
				
			setValue( getString().substring(f) );
		}
		else
		{
			setNull();
		}
		if (!isString()) setType(VALUE_TYPE_STRING);
		
		return this;
	}

	// implement the LEFTSTR function, arguments in args[]
	public Value leftstr(Value len) 
	{ 
		return leftstr((int)len.getNumber()); 
	}
	
	public Value leftstr(int len) 
	{
		if (isNull()) 
		{
			setType(VALUE_TYPE_STRING);
			return this;
		} 

		setValue( getString() );
		
		int tot_len = getString()!=null?getString().length():0;
		
		if (tot_len>0)
		{
			int totlen = getString().length();
			
			int f = totlen-len;
			if (f>0)
			{
				setValue( getString().substring(0,len) );
			}
		}
		else
		{
			setNull();
		}
		if (!isString()) setType(VALUE_TYPE_STRING);
		
		return this;
	}

	public Value startsWith(Value string)
	{
		return startsWith(string.getString());
	}

	public Value startsWith(String string)
	{
		if (isNull()) 
		{
			setType(VALUE_TYPE_BOOLEAN);
			return this;
		} 
		
		if (string==null)
		{
			setValue(false);
			setNull();
			return this;
		}

		setValue( getString().startsWith(string) );
		
		return this;
	}

	
	// implement the SYSDATE function, arguments in args[]
	public Value sysdate()
	{
		setValue( Calendar.getInstance().getTime() );
		
		return this;
	}

	// implement the TAN function, arguments in args[]
	public Value tan() throws KettleValueException
	{
		if (isNull()) return this;
		
		if (isNumeric())
		{
			setValue( Math.tan(getNumber()) );
		}
		else
		{
			throw new KettleValueException("Function TAN only works on a number");
		}

		return this;
	}

	// implement the TO_CHAR function, arguments in args[]
	// number: NUM2STR( 123.456 )   : default format
	// number: NUM2STR( 123.456, '###,##0.000') : format 
	// number: NUM2STR( 123.456, '###,##0.000', '.') : grouping
	// number: NUM2STR( 123.456, '###,##0.000', '.', ',') : decimal
	// number: NUM2STR( 123.456, '###,##0.000', '.', ',', '�') : currency

	public Value num2str() throws KettleValueException { return num2str(null, null, null, null); }
	public Value num2str(String arg0) throws KettleValueException { return num2str(arg0, null, null, null); }
	public Value num2str(String arg0, String arg1) throws KettleValueException { return num2str(arg0, arg1, null, null); }
	public Value num2str(String arg0, String arg1, String arg2) throws KettleValueException { return num2str(arg0, arg1, arg2, null); }
	public Value num2str(String arg0, String arg1, String arg2, String arg3) throws KettleValueException
	{
		if (isNull())
		{
			setType(VALUE_TYPE_STRING);
		}
		else
		{
			// Number to String conversion...
			if (getType()==VALUE_TYPE_NUMBER || getType()==VALUE_TYPE_INTEGER)
			{
				NumberFormat         nf  = NumberFormat.getInstance();
				DecimalFormat        df  = (DecimalFormat)nf;
				DecimalFormatSymbols dfs =new DecimalFormatSymbols();
			
				if (arg3!=null) dfs.setCurrencySymbol( arg3 );
				if (arg2!=null) dfs.setGroupingSeparator( arg2.charAt(0) );
				if (arg1!=null) dfs.setDecimalSeparator( arg1.charAt(0) );
				df.setDecimalFormatSymbols(dfs); // in case of 4, 3 or 2
				if (arg0!=null) df.applyPattern(arg0);
				try
				{
					setValue( nf.format(getNumber()) );
				}
				catch(Exception e)
				{
					setType(VALUE_TYPE_STRING);
					setNull();
					throw new KettleValueException("Couldn't convert Number to String "+e.toString());
				}
			}
			else
			{
				throw new KettleValueException("Function NUM2STR only works on Numbers and Integers");
			}
		}
		return this;
	}
	
	// date:   TO_CHAR( <date> , 'yyyy/mm/dd HH:mm:ss'
	public Value dat2str() throws KettleValueException { return dat2str(null, null); }
	public Value dat2str(String arg0) throws KettleValueException { return dat2str(arg0, null); }
	public Value dat2str(String arg0, String arg1) throws KettleValueException
	{
		if (isNull())
		{
			setType(VALUE_TYPE_STRING);
		}
		else
		{
			if (getType()==VALUE_TYPE_DATE)
			{
				SimpleDateFormat     df  = new SimpleDateFormat();
			
				DateFormatSymbols dfs = new DateFormatSymbols();
				if (arg1!=null) dfs.setLocalPatternChars(arg1);
				if (arg0!=null) df.applyPattern(arg0);
				try
				{
					setValue( df.format(getDate()) );
				}
				catch(Exception e)
				{
					setType(VALUE_TYPE_STRING);
					setNull();
					throw new KettleValueException("TO_CHAR Couldn't convert Date to String "+e.toString());
				}
			}
			else
			{
				throw new KettleValueException("Function DAT2STR only works on a date");
			}
		}
		
		return this;
	}

	// implement the TO_DATE function, arguments in args[]
	public Value num2dat() throws KettleValueException
	{
		if (isNull())
		{
			setType(VALUE_TYPE_DATE);
		}
		else
		{
			if (isNumeric())
			{
				value = new ValueDate();
				value.setInteger(getInteger());
				setType(VALUE_TYPE_DATE);
				setLength(-1,-1);
			}
			else
			{
				throw new KettleValueException("Function NUM2DAT only works on a number");
			}
		}
		return this;
	}
	
	public Value str2dat(String arg0) throws KettleValueException { return str2dat(arg0, null); }
	public Value str2dat(String arg0, String arg1) throws KettleValueException
	{
		if (isNull())
		{
			setType(VALUE_TYPE_DATE);
		}
		else
		{
			// System.out.println("Convert string ["+string+"] to date using pattern '"+arg0+"'");

			SimpleDateFormat     df  = new SimpleDateFormat();
			
			DateFormatSymbols dfs = new DateFormatSymbols();
			if (arg1!=null) dfs.setLocalPatternChars(arg1);
			if (arg0!=null) df.applyPattern(arg0);

			try
			{
				value.setDate( df.parse(getString()) );
				setType(VALUE_TYPE_DATE);
				setLength(-1,-1);
			}
			catch(Exception e)
			{
				setType(VALUE_TYPE_DATE);
				setNull();
				throw new KettleValueException("TO_DATE Couldn't convert String to Date"+e.toString());
			}
		}
		return this;
	}

	// implement the TO_NUMBER function, arguments in args[]
	public Value str2num() throws KettleValueException { return str2num(null, null, null, null); }
	public Value str2num(String pattern) throws KettleValueException { return str2num(pattern, null, null, null); }
	public Value str2num(String pattern, String decimal) throws KettleValueException { return str2num(pattern, decimal, null, null); }
	public Value str2num(String pattern, String decimal, String grouping) throws KettleValueException { return str2num(pattern, decimal, grouping, null); }
	public Value str2num(String pattern, String decimal, String grouping, String currency) throws KettleValueException
	{
		// 0 : pattern
		// 1 : Decimal separator
		// 2 : Grouping separator
		// 3 : Currency symbol

		if (isNull())
		{
			setType(VALUE_TYPE_STRING);
		}
		else
		{
			if (getType()==VALUE_TYPE_STRING)
			{
				if (getString()==null)
				{
					setNull();
					setValue(0.0);
				} 
				else
				{
					NumberFormat         nf  = NumberFormat.getInstance();
					DecimalFormat        df  = (DecimalFormat)nf;
					DecimalFormatSymbols dfs =new DecimalFormatSymbols();
						
					if ( currency!=null && currency.length()>0 ) dfs.setCurrencySymbol( currency );
					if ( grouping!=null && grouping.length()>0 ) dfs.setGroupingSeparator( grouping.charAt(0) );
					if ( decimal !=null && decimal .length()>0 ) dfs.setDecimalSeparator( decimal.charAt(0) );
		            if ( pattern !=null && pattern .length()>0 ) df.applyPattern( pattern );
					try
					{
						setValue( nf.parse(getString()).doubleValue() );
					}
					catch(Exception e)
					{
						setType(VALUE_TYPE_NUMBER);
						setNull();
						throw new KettleValueException("Couldn't convert string to number "+e.toString());
					}
				}
			}
			else
			{
				throw new KettleValueException("Function STR2NUM works only on strings");
			}
		}
	 	return this;
	}
	
	public Value dat2num() throws KettleValueException
	{
		if (isNull())
		{
			setType(VALUE_TYPE_INTEGER);
			return this;
		}

		if (getType()==VALUE_TYPE_DATE)
		{
			if (getString()==null)
			{
				setNull();
				setValue(0L);
			} 
			else
			{
				setValue(getInteger());
			}
		}
		else
		{
			throw new KettleValueException("Function DAT2NUM works only on dates");
		}
		return this;
	}

	/**
	 * Performs a right and left trim of spaces in the string.
	 * If the value is not a string a conversion to String is performed first.
	 * 
	 * @return The trimmed string value.
	 */
	public Value trim()
	{
		if (isNull())
		{
			setType(VALUE_TYPE_STRING);
			return this;
		}
		
		String str = Const.trim(getString());
		setValue(str);

		return this;
	}

	// implement the UPPER function, arguments in args[]
	public Value upper()
	{
		if (isNull())
		{
			setType(VALUE_TYPE_STRING);
			return this;
		}
		
		setValue( getString().toUpperCase() );
		
		return this;
	}

	// implement the E function, arguments in args[]
	public Value e()
	{
		setValue(Math.E);
		return this;
	}

	// implement the PI function, arguments in args[]
	public Value pi()
	{
		setValue(Math.PI);
		return this;
	}

	// implement the DECODE function, arguments in args[]
	public Value v_decode(Value args[]) throws KettleValueException
	{
		int i;
		boolean found;
		// Decode takes as input the first argument...
		// The next pair
		
		// Limit to 3, 5, 7, 9, ... arguments

		if (args.length>=3 && (args.length%2)==1)
		{
			i=0;
			found=false;
			while (i<args.length-1 && !found)
			{
				if (this.equals(args[i]))
				{
					setValue(args[i+1]);
					found=true;
				}
				i+=2;
			}
			if (!found) setValue(args[args.length-1]);
		}
		else
		{
			// ERROR with nr of arguments
			throw new KettleValueException("Function DECODE can't have "+args.length+" arguments!");
		}
		
		return this;
	}

	// implement the IF function, arguments in args[]
	// IF( <condition>, <then value>, <else value>)
	public Value v_if(Value args[]) throws KettleValueException
	{
		if (getType()==VALUE_TYPE_BOOLEAN)
		{
			if (args.length==1)
			{
				if (getBoolean()) setValue(args[0]); else setNull();
			}
			else
			if (args.length==2)
			{
				if (getBoolean()) setValue(args[0]); else setValue(args[1]);
			}
		}
		else
		{
			throw new KettleValueException("Function DECODE can't have "+args.length+" arguments!");
		}
		return this;
	}

		
	
	
	
	
	
	
	
	
	
	
	
	
	
	// implement the ADD_MONTHS function, one argument
	public Value add_months(int months) throws KettleValueException
	{
		if (getType()==VALUE_TYPE_DATE)
		{
			if (!isNull() && getDate()!=null)
			{
				Calendar cal = Calendar.getInstance();
				cal.setTime(getDate());
				int year  = cal.get(Calendar.YEAR);
				int month = cal.get(Calendar.MONTH);
				int day   = cal.get(Calendar.DAY_OF_MONTH);
				
				month+=months;
				
				int newyear =  year+(int)Math.floor(month/12);
				int newmonth   = month%12;
				
				cal.set(newyear, newmonth, 1);
				int newday = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
				if (newday<day) cal.set(Calendar.DAY_OF_MONTH, newday);
				else            cal.set(Calendar.DAY_OF_MONTH, day);

				setValue( cal.getTime() );
			}
		}
		else
		{
			throw new KettleValueException("Function add_months only works on a date!");
		}
		return this;
	}
    
    /** Add a number of days to a Date value
     * 
     * @param days The number of days to add to the current date value
     * @return The resulting value
     * @throws KettleValueException
     */
    public Value add_days(long days) throws KettleValueException
    {
        if (getType()==VALUE_TYPE_DATE)
        {
            if (!isNull() && getDate()!=null)
            {
                Calendar cal = Calendar.getInstance();
                cal.setTime(getDate());
                cal.add(Calendar.DAY_OF_YEAR, (int)days);
                
                setValue( cal.getTime() );
            }
        }
        else
        {
            throw new KettleValueException("Function add_days only works on a date!");
        }
        return this;
    }

	// implement the LAST_DAY function, arguments in args[]
	public Value last_day() throws KettleValueException
	{
		if (getType()==VALUE_TYPE_DATE)
		{
			Calendar cal=Calendar.getInstance();
			cal.setTime(getDate());
			int last_day = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
			cal.set(Calendar.DAY_OF_MONTH, last_day);

			setValue( cal.getTime() );
		}
		else
		{
			throw new KettleValueException("Function last_day only works on a date");
		}

		return this;
	}

	public Value first_day() throws KettleValueException
	{
		if (getType()==VALUE_TYPE_DATE)
		{
			Calendar cal=Calendar.getInstance();
			cal.setTime(getDate());
			cal.set(Calendar.DAY_OF_MONTH, 1);
			setValue( cal.getTime() );
		}
		else
		{
			throw new KettleValueException("Function first_day only works on a date");
		}

		return this;
	}


	// implement the TRUNC function, version without arguments
	public Value trunc() throws KettleValueException
	{
		if (isNull()) return this; // don't do anything, leave it at NULL!

        if (isBigNumber())
        {
            getBigNumber().setScale(0, BigDecimal.ROUND_FLOOR);
        }
        else
		if (isNumber())
		{
			setValue( Math.floor(getNumber()) );
		}
		else
		if (isInteger())
		{
			// Nothing
		}
		else
		if (isDate())
		{
			Calendar cal=Calendar.getInstance();
			cal.setTime(getDate());

			cal.set(Calendar.MILLISECOND, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.HOUR_OF_DAY, 0);

			setValue( cal.getTime() );
		}
		else
		{
			throw new KettleValueException("Function TRUNC only works on numbers and dates");
		}
		
		return this;
	}

	// implement the TRUNC function, arguments in args[]
	public Value trunc(double level) throws KettleValueException { return trunc((int)level); }

	public Value trunc(int level) throws KettleValueException
	{
		if (isNull()) return this; // don't do anything, leave it at NULL!

        if (isBigNumber())
        {
            getBigNumber().setScale(level, BigDecimal.ROUND_FLOOR);
        }
        else
		if (isNumber())
		{
			double pow=Math.pow(10, level);
			setValue( Math.floor( getNumber() * pow ) / pow );
		}
		else
		if (isInteger())
		{
            // Nothing!
		}
		else
		if (isDate())
		{
			Calendar cal=Calendar.getInstance();
			cal.setTime(getDate());
			
			switch((int)level)
			{
			// MONTHS
			case 5: cal.set(Calendar.MONTH, 1);
			// DAYS
			case 4: cal.set(Calendar.DAY_OF_MONTH, 1);
			// HOURS 
			case 3: cal.set(Calendar.HOUR_OF_DAY, 0);
			// MINUTES
			case 2: cal.set(Calendar.MINUTE, 0);
			// SECONDS
			case 1: cal.set(Calendar.SECOND, 0);  break;
			default:
				throw new KettleValueException("Argument of TRUNC of date has to be between 1 and 5");
			}
		}
		else
		{
			throw new KettleValueException("Function TRUNC only works with numbers and dates");
		}
		
		return this;
	}
	
	/* Some javascript extensions...
	 * 
	 */

	public static final Value getInstance() { return new Value(); }

	public String getClassName() { return "Value"; }
	
	public void jsConstructor() 
	{ 
	}
	 
	public void jsConstructor(String name) 
	{ 
		setName(name); 
	}

	public void jsConstructor(String name, String value) 
	{ 
		setName(name); 
		setValue(value);
	}
	

	/**
	 * Produce the XML representation of this value.
	 * @return a String containing the XML to represent this Value.
	 */
	public String getXML()
	{
		String retval="";
		
		retval+=XMLHandler.addTagValue("name", getName(), false);
		retval+=XMLHandler.addTagValue("type", getTypeDesc(), false);
		retval+=XMLHandler.addTagValue("text", toString(false), false);
		retval+=XMLHandler.addTagValue("length", getLength(), false);
		retval+=XMLHandler.addTagValue("precision", getPrecision(), false);
		retval+=XMLHandler.addTagValue("isnull", isNull(), false);

		return retval;
	}
	
	/**
	 * Construct a new Value and read the data from XML
	 * @param valnode The XML Node to read from.
	 */
	public Value(Node valnode)
	{
		this();
		loadXML(valnode);
	}
	
	/**
	 * Read the data for this Value from an XML Node
	 * @param valnode The XML Node to read from
	 * @return true if all went well, false if something went wrong.
	 */
	public boolean loadXML(Node valnode)
	{
		try
		{
			String valname =  XMLHandler.getTagValue(valnode, "name");
			int valtype    =  getType( XMLHandler.getTagValue(valnode, "type") );
			String text    =  XMLHandler.getTagValue(valnode, "text");
			boolean isnull =  "Y".equalsIgnoreCase(XMLHandler.getTagValue(valnode, "isnull"));
			int len        =  Const.toInt(XMLHandler.getTagValue(valnode, "length"), -1);
			int prec       =  Const.toInt(XMLHandler.getTagValue(valnode, "precision"), -1);
			
			setName(valname);
			setValue(text);
			setLength(len, prec);
			
			if (valtype!=VALUE_TYPE_STRING)	
			{
				trim();
				convertString(valtype);
			}
	
			if (isnull) setNull();
		}
		catch(Exception e)
		{
			setNull();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Convert this Value from type String to another type
	 * @param newtype The Value type to convert to.
	 */
	public void convertString(int newtype) throws KettleValueException
	{
		switch(newtype)
		{
		case VALUE_TYPE_STRING    : break;
		case VALUE_TYPE_NUMBER    : setValue( getNumber() ); break;
		case VALUE_TYPE_DATE      : setValue( getDate() ); break;
		case VALUE_TYPE_BOOLEAN   : setValue( getBoolean() ); break;
		case VALUE_TYPE_INTEGER   : setValue( getInteger() ); break;
        case VALUE_TYPE_BIGNUMBER : setValue( getBigNumber() ); break;
		default: 
            throw new KettleValueException("Please specify the type to convert to from String type.");
		}
	}
	
	public Value(Repository rep, long id_value)
		throws KettleException
	{
		try
		{
			Row r = rep.getValue(id_value);
			if (r!=null)
			{
				name          = r.getString("NAME", null);
				String valstr = r.getString("VALUE_STR", null);
				setValue( valstr );
				int valtype   = getType( r.getString("VALUE_TYPE", null) );
				setType(valtype);
				setNull( r.getBoolean("IS_NULL", false) );
			}
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to load Value from repository with id_value="+id_value, dbe);
		}
	}


}
