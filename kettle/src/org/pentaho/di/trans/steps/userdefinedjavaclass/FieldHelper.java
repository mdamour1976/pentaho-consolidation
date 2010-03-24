/***** BEGIN LICENSE BLOCK *****
The contents of this package are subject to the GNU Lesser Public License
 (the "License"); you may not use this file except in compliance with
the License. You may obtain a copy of the License at
http://www.gnu.org/licenses/lgpl-2.1.txt

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the
License.

The Original Code is Kettle User Defined Java Class Step

The Initial Developer of the Original Code is
Daniel Einspanjer deinspanjer@mozilla.com
Portions created by the Initial Developer are Copyright (C) 2009
the Initial Developer. All Rights Reserved.

Contributor(s):
Matt Casters mcaster@pentaho.com

***** END LICENSE BLOCK *****/

package org.pentaho.di.trans.steps.userdefinedjavaclass;

import java.math.BigDecimal;
import java.util.Date;

import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;

public class FieldHelper
{
    private int index = -1;
    private ValueMetaInterface meta;

    public FieldHelper(RowMetaInterface rowMeta, String fieldName)
    {
        this.meta = rowMeta.searchValueMeta(fieldName);
        this.index = rowMeta.indexOfValue(fieldName);
        if (this.index == -1)
        {
            throw new IllegalArgumentException(String.format("FieldHelper could not be initialized. The field named '%s' not found.", fieldName));
        }
    }
    
    public Object getObject(Object[] dataRow)
    {
        return dataRow[index];
    }

    public BigDecimal getBigNumber(Object[] dataRow) throws KettleValueException
    {
        return meta.getBigNumber(dataRow[index]);
    }

    public byte[] getBinary(Object[] dataRow) throws KettleValueException
    {
        return meta.getBinary(dataRow[index]);
    }

    public Boolean getBoolean(Object[] dataRow) throws KettleValueException
    {
        return meta.getBoolean(dataRow[index]);
    }

    public Date getDate(Object[] dataRow) throws KettleValueException
    {
        return meta.getDate(dataRow[index]);
    }

    public Long getInteger(Object[] dataRow) throws KettleValueException
    {
        return meta.getInteger(dataRow[index]);
    }

    public Double getNumber(Object[] dataRow) throws KettleValueException
    {
        return meta.getNumber(dataRow[index]);
    }

    public String getString(Object[] dataRow) throws KettleValueException
    {
        return meta.getString(dataRow[index]);
    }

    public ValueMetaInterface getValueMeta()
    {
        return meta;
    }

    public int indexOfValue()
    {
        return index;
    }
    
    public void setValue(Object[] dataRow, Object value)
    {
        dataRow[index] = value;
    }
}
