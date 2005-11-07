package be.ibridge.kettle.trans.step.calculator;

import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.repository.Repository;

public class CalculatorMetaFunction implements Cloneable
{
    public static final String XML_TAG = "calculation";  
    
    public static final int CALC_NONE               =  0;
    public static final int CALC_ADD                =  1;
    public static final int CALC_SUBTRACT           =  2;
    public static final int CALC_MULTIPLY           =  3;
    public static final int CALC_DIVIDE             =  4;
    public static final int CALC_SQUARE             =  5;
    public static final int CALC_SQUARE_ROOT        =  6;
    public static final int CALC_PERCENT_1          =  7;
    public static final int CALC_PERCENT_2          =  8;
    public static final int CALC_PERCENT_3          =  9;
    public static final int CALC_COMBINATION_1      = 10;
    public static final int CALC_COMBINATION_2      = 11;
    public static final int CALC_ROUND_1            = 12;
    public static final int CALC_ROUND_2            = 13;
    public static final int CALC_ADD_DAYS           = 14;
    public static final int CALC_CONSTANT           = 15;
    
    public static final String calc_desc[] = 
        { 
            "-", 
            "ADD",
            "SUBTRACT",
            "MULTIPLY",
            "DIVIDE",
            "SQUARE",
            "SQUARE_ROOT",
            "PERCENT_1",
            "PERCENT_2",
            "PERCENT_3",
            "COMBINATION_1",
            "COMBINATION_2",
            "ROUND_1",
            "ROUND_2",
            "ADD_DAYS",
            "CONSTANT"
        };
    
    public static final String calc_desc_long[] = 
        { 
            "-", 
            "A + B", 
            "A - B", 
            "A * B",
            "A / B", 
            "A * A", 
            "SQRT( A )", 
            "100 * A / B", 
            "A - ( A * B / 100 )", 
            "A + ( A * B / 100 )", 
            "A + B * C", 
            "SQRT( A*A + B*B )", 
            "ROUND( A )",
            "ROUND( A , B )",
            "Date A + B Days",
            "Set field to constant value A",
        };
   
    private String fieldName;
    private int    calcType;
    private String fieldA;
    private String fieldB;
    private String fieldC;

    private int    valueType;
    private int    valueLength;
    private int    valuePrecision;
    
    private boolean removedFromResult;
    
    /**
     * @param fieldName
     * @param calcType
     * @param fieldA
     * @param fieldB
     * @param fieldC
     * @param valueType
     * @param valueLength
     * @param valuePrecision
     */
    public CalculatorMetaFunction(String fieldName, int calcType, String fieldA, String fieldB, String fieldC, int valueType, int valueLength, int valuePrecision, boolean removedFromResult)
    {
        this.fieldName = fieldName;
        this.calcType = calcType;
        this.fieldA = fieldA;
        this.fieldB = fieldB;
        this.fieldC = fieldC;
        this.valueType = valueType;
        this.valueLength = valueLength;
        this.valuePrecision = valuePrecision;
        this.removedFromResult = removedFromResult;
    }

    public Object clone()
    {
        try
        {
            CalculatorMetaFunction retval = (CalculatorMetaFunction) super.clone();
            return retval;
        }
        catch(CloneNotSupportedException e)
        {
            return null;
        }
    }
    
    public String getXML()
    {
        String xml="";
        
        xml+="<"+XML_TAG+">";
        
        xml+=XMLHandler.addTagValue("field_name",      fieldName);
        xml+=XMLHandler.addTagValue("calc_type",       getCalcTypeDesc());
        xml+=XMLHandler.addTagValue("field_a",         fieldA);
        xml+=XMLHandler.addTagValue("field_b",         fieldB);
        xml+=XMLHandler.addTagValue("field_c",         fieldC);
        xml+=XMLHandler.addTagValue("value_type",      Value.getTypeDesc(valueType));
        xml+=XMLHandler.addTagValue("value_length",    valueLength);
        xml+=XMLHandler.addTagValue("value_precision", valuePrecision);
        xml+=XMLHandler.addTagValue("remove",          removedFromResult);
        
        xml+="</"+XML_TAG+">";
     
        return xml;
    }
    
    public CalculatorMetaFunction(Node calcnode)
    {
        fieldName      = XMLHandler.getTagValue(calcnode, "field_name");
        calcType       = getCalcFunctionType( XMLHandler.getTagValue(calcnode, "calc_type") );
        fieldA         = XMLHandler.getTagValue(calcnode, "field_a");
        fieldB         = XMLHandler.getTagValue(calcnode, "field_b");
        fieldC         = XMLHandler.getTagValue(calcnode, "field_c");
        valueType      = Value.getType( XMLHandler.getTagValue(calcnode, "value_type") );
        valueLength    = Const.toInt( XMLHandler.getTagValue(calcnode, "value_length"), -1 );
        valuePrecision = Const.toInt( XMLHandler.getTagValue(calcnode, "value_precision"), -1 );
        removedFromResult = "Y".equalsIgnoreCase(XMLHandler.getTagValue(calcnode, "remove"));
    }
    
    public void saveRep(Repository rep, long id_transformation, long id_step, int nr) throws KettleException
    {
        rep.saveStepAttribute(id_transformation, id_step, nr, "field_name",          fieldName);
        rep.saveStepAttribute(id_transformation, id_step, nr, "calc_type",           getCalcTypeDesc());
        rep.saveStepAttribute(id_transformation, id_step, nr, "field_a",             fieldA);
        rep.saveStepAttribute(id_transformation, id_step, nr, "field_b",             fieldB);
        rep.saveStepAttribute(id_transformation, id_step, nr, "field_c",             fieldC);
        rep.saveStepAttribute(id_transformation, id_step, nr, "value_type",          Value.getTypeDesc(valueType));
        rep.saveStepAttribute(id_transformation, id_step, nr, "value_length",        valueLength);
        rep.saveStepAttribute(id_transformation, id_step, nr, "value_precision",     valuePrecision);
        rep.saveStepAttribute(id_transformation, id_step, nr, "remove",              removedFromResult);
    }

    public CalculatorMetaFunction(Repository rep, long id_step, int nr) throws KettleException
    {
        fieldName      = rep.getStepAttributeString(id_step, nr, "field_name");
        calcType       = getCalcFunctionType( rep.getStepAttributeString(id_step, nr, "calc_type") );
        fieldA         = rep.getStepAttributeString(id_step, nr, "field_a");
        fieldB         = rep.getStepAttributeString(id_step, nr, "field_b");
        fieldC         = rep.getStepAttributeString(id_step, nr, "field_c");
        valueType      = Value.getType( rep.getStepAttributeString(id_step, nr, "value_type") );
        valueLength    = (int)rep.getStepAttributeInteger(id_step, nr,  "value_length");
        valuePrecision = (int)rep.getStepAttributeInteger(id_step, nr, "value_precision");
        removedFromResult = rep.getStepAttributeBoolean(id_step, nr, "remove");
    }
    
    public static final int getCalcFunctionType(String desc)
    {
        for (int i=1;i<calc_desc.length;i++) if (calc_desc[i].equalsIgnoreCase(desc)) return i;
        for (int i=1;i<calc_desc_long.length;i++) if (calc_desc_long[i].equalsIgnoreCase(desc)) return i;
        
        return CALC_NONE;
    }
    
    public static final String getCalcFunctionDesc(int type)
    {
        if (type<0 || type>=calc_desc.length) return null;
        return calc_desc[type];
    }

    public static final String getCalcFunctionLongDesc(int type)
    {
        if (type<0 || type>=calc_desc_long.length) return null;
        return calc_desc_long[type];
    }

    
    /**
     * @return Returns the calcType.
     */
    public int getCalcType()
    {
        return calcType;
    }

    /**
     * @param calcType The calcType to set.
     */
    public void setCalcType(int calcType)
    {
        this.calcType = calcType;
    }
    
    public String getCalcTypeDesc()
    {
        return getCalcFunctionDesc(calcType);
    }

    public String getCalcTypeLongDesc()
    {
        return getCalcFunctionLongDesc(calcType);
    }

    /**
     * @return Returns the fieldA.
     */
    public String getFieldA()
    {
        return fieldA;
    }

    /**
     * @param fieldA The fieldA to set.
     */
    public void setFieldA(String fieldA)
    {
        this.fieldA = fieldA;
    }

    /**
     * @return Returns the fieldB.
     */
    public String getFieldB()
    {
        return fieldB;
    }

    /**
     * @param fieldB The fieldB to set.
     */
    public void setFieldB(String fieldB)
    {
        this.fieldB = fieldB;
    }

    /**
     * @return Returns the fieldC.
     */
    public String getFieldC()
    {
        return fieldC;
    }

    /**
     * @param fieldC The fieldC to set.
     */
    public void setFieldC(String fieldC)
    {
        this.fieldC = fieldC;
    }

    /**
     * @return Returns the fieldName.
     */
    public String getFieldName()
    {
        return fieldName;
    }

    /**
     * @param fieldName The fieldName to set.
     */
    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    /**
     * @return Returns the valueLength.
     */
    public int getValueLength()
    {
        return valueLength;
    }

    /**
     * @param valueLength The valueLength to set.
     */
    public void setValueLength(int valueLength)
    {
        this.valueLength = valueLength;
    }

    /**
     * @return Returns the valuePrecision.
     */
    public int getValuePrecision()
    {
        return valuePrecision;
    }

    /**
     * @param valuePrecision The valuePrecision to set.
     */
    public void setValuePrecision(int valuePrecision)
    {
        this.valuePrecision = valuePrecision;
    }

    /**
     * @return Returns the valueType.
     */
    public int getValueType()
    {
        return valueType;
    }

    /**
     * @param valueType The valueType to set.
     */
    public void setValueType(int valueType)
    {
        this.valueType = valueType;
    }

    /**
     * @return Returns the removedFromResult.
     */
    public boolean isRemovedFromResult()
    {
        return removedFromResult;
    }

    /**
     * @param removedFromResult The removedFromResult to set.
     */
    public void setRemovedFromResult(boolean removedFromResult)
    {
        this.removedFromResult = removedFromResult;
    }
}
