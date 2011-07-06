 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 
package org.pentaho.di.trans.steps.symmetriccrypto.secretkeygenerator;

import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;



/**
 * Generate secret key.
 * for symmetric algorithms
 * @author Samatar
 * @since 01-4-2011
 */

public class SecretKeyGeneratorMeta extends BaseStepMeta implements StepMetaInterface {
	private static Class<?> PKG = SecretKeyGeneratorMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private String algorithm[];
	private String scheme[];
	private String secretKeyLength[];
	private String secretKeyCount[];
	
	private  String secretKeyFieldName;
	private  String secretKeyLengthFieldName;
	private  String algorithmFieldName;
	
	private boolean outputKeyInBinary;

	public SecretKeyGeneratorMeta() {
		super(); // allocate BaseStepMeta
	}

	/**
	 * @return Returns the fieldAlgorithm.
	 */
	public String[] getAlgorithm() {
		return algorithm;
	}
	
	/**
	 * @return Returns the scheme.
	 */
	public String[] getScheme(){
		return scheme;
	}
    
    /**
     * @return Returns the secretKeyFieldName.
     */
    public String getSecretKeyFieldName()
    {
        return secretKeyFieldName;
    }
    
    /**
     * @param secretKeyFieldName The secretKeyFieldName to set.
     */
    public void setSecretKeyFieldName(String secretKeyFieldName)
    {
        this.secretKeyFieldName = secretKeyFieldName;
    }
    
    /**
     * @return Returns the secretKeyLengthFieldName.
     */
    public String getSecretKeyLengthFieldName()
    {
        return secretKeyLengthFieldName;
    }
    
    /**
     * @param outputKeyInBinary The outputKeyInBinary to set.
     */
    public void setOutputKeyInBinary(boolean outputKeyInBinary)
    {
        this.outputKeyInBinary = outputKeyInBinary;
    }
    
    /**
     * @return Returns outputKeyInBinary secretKeyLengthFieldName.
     */
    public boolean isOutputKeyInBinary()
    {
        return outputKeyInBinary;
    }
    
    /**
     * @param secretKeyLengthFieldName The secretKeyLengthFieldName to set.
     */
    public void setSecretKeyLengthFieldName(String secretKeyLengthFieldName)
    {
        this.secretKeyLengthFieldName = secretKeyLengthFieldName;
    }
    /**
     * @return Returns the algorithmFieldName.
     */
    public String getAlgorithmFieldName()
    {
        return algorithmFieldName;
    }
    
    /**
     * @param algorithmFieldName The algorithmFieldName to set.
     */
    public void setAlgorithmFieldName(String algorithmFieldName)
    {
        this.algorithmFieldName = algorithmFieldName;
    }
	/**
	 * @param fieldName
	 *            The fieldAlgorithm to set.
	 */
	public void setFieldAlgorithm(String[] fieldName) {
		this.algorithm = fieldName;
	}
	
	/**
	 * @param scheme
	 *            The scheme to set.
	 */
	public void setScheme(String[] scheme) {
		this.scheme = scheme;
	}

	/**
	 * @return Returns the fieldType.
	 */
	public String[] getSecretKeyLength() {
		return secretKeyLength;
	}

	/**
	 * @return Returns the secretKeyCount.
	 */
	public String[] getSecretKeyCount() {
		return secretKeyCount;
	}
	/**
	 * @param fieldType
	 *            The fieldType to set.
	 */
	public void setFieldType(String[] fieldType) {
		this.secretKeyLength = fieldType;
	}

	
	public void loadXML(Node stepnode, List<DatabaseMeta> databases,
			Map<String, Counter> counters) throws KettleXMLException {
		readData(stepnode);
	}

	public void allocate(int count) {
		algorithm = new String[count];
		scheme = new String[count];
		secretKeyLength = new String[count];
		secretKeyCount = new String[count];
	}

	public Object clone() {
		SecretKeyGeneratorMeta retval = (SecretKeyGeneratorMeta) super.clone();

		int count = algorithm.length;

		retval.allocate(count);

		for (int i = 0; i < count; i++) {
			retval.algorithm[i] = algorithm[i];
			retval.scheme[i] = scheme[i];
			retval.secretKeyLength[i] = secretKeyLength[i];
			retval.secretKeyCount[i] = secretKeyCount[i];
		}

		return retval;
	}

	private void readData(Node stepnode) throws KettleXMLException {
		try {
			Node fields = XMLHandler.getSubNode(stepnode, "fields");
			int count = XMLHandler.countNodes(fields, "field");

			allocate(count);

			for (int i = 0; i < count; i++) {
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);

				algorithm[i] = XMLHandler.getTagValue(fnode, "algorithm");
				scheme[i] = XMLHandler.getTagValue(fnode, "scheme");
				secretKeyLength[i] = XMLHandler.getTagValue(fnode, "secretKeyLen");
				secretKeyCount[i] = XMLHandler.getTagValue(fnode, "secretKeyCount");
			}

            secretKeyFieldName=XMLHandler.getTagValue(stepnode, "secretKeyFieldName");
            secretKeyLengthFieldName=XMLHandler.getTagValue(stepnode, "secretKeyLengthFieldName");
            algorithmFieldName=XMLHandler.getTagValue(stepnode, "algorithmFieldName");
            
            outputKeyInBinary="Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "outputKeyInBinary"));	
            
		} catch (Exception e) {
			throw new KettleXMLException("Unable to read step information from XML", e);
		}
	}


	public void setDefault() {
		int count = 0;

		allocate(count);

		for (int i = 0; i < count; i++) {
			algorithm[i] = "field" + i;
			scheme[i] = "";
			secretKeyLength[i] = "";
			secretKeyCount[i] = "";
		}
		secretKeyFieldName = BaseMessages.getString(PKG, "SecretKeyGeneratorMeta.secretKeyField");
		secretKeyLengthFieldName = BaseMessages.getString(PKG, "SecretKeyGeneratorMeta.secretKeyLengthField");
		algorithmFieldName = BaseMessages.getString(PKG, "SecretKeyGeneratorMeta.algorithmField");
		
		outputKeyInBinary=false;
	}

	public void getFields(RowMetaInterface row, String name,
			RowMetaInterface[] info, StepMeta nextStep, VariableSpace space)
			throws KettleStepException {

		ValueMetaInterface v;
		if(isOutputKeyInBinary()){
			v= new ValueMeta(secretKeyFieldName, ValueMeta.TYPE_BINARY);
		}else {
			v = new ValueMeta(secretKeyFieldName, ValueMeta.TYPE_STRING);
		}
		v.setOrigin(name);
		row.addValueMeta(v);
		
		if(!Const.isEmpty(getAlgorithmFieldName())) {
			v = new ValueMeta(algorithmFieldName, ValueMeta.TYPE_STRING);
			v.setOrigin(name);
			row.addValueMeta(v);
		}
		
		if(!Const.isEmpty(getSecretKeyLengthFieldName())) {
			v = new ValueMeta(secretKeyLengthFieldName, ValueMeta.TYPE_INTEGER);
			v.setLength(ValueMeta.DEFAULT_INTEGER_LENGTH, 0);
			v.setOrigin(name);
			row.addValueMeta(v);
		}
		
	}

	public String getXML() {
		StringBuffer retval = new StringBuffer(200);

		retval.append("    <fields>").append(Const.CR);

		for (int i = 0; i < algorithm.length; i++) {
			retval.append("      <field>").append(Const.CR);
			retval.append("        ").append(XMLHandler.addTagValue("algorithm", algorithm[i]));
			retval.append("        ").append(XMLHandler.addTagValue("scheme", scheme[i]));
			retval.append("        ").append(XMLHandler.addTagValue("secretKeyLen",secretKeyLength[i]));
			retval.append("        ").append(XMLHandler.addTagValue("secretKeyCount",secretKeyCount[i]));
			retval.append("      </field>").append(Const.CR);
		}
		retval.append("    </fields>" + Const.CR);
		
		retval.append("    "+XMLHandler.addTagValue("secretKeyFieldName", secretKeyFieldName));
		retval.append("    "+XMLHandler.addTagValue("secretKeyLengthFieldName", secretKeyLengthFieldName));
		retval.append("    "+XMLHandler.addTagValue("algorithmFieldName", algorithmFieldName));
		retval.append("    "+XMLHandler.addTagValue("algorithmFieldName", algorithmFieldName));
		retval.append("    "+XMLHandler.addTagValue("outputKeyInBinary", outputKeyInBinary));
		return retval.toString();
	}

	public void readRep(Repository rep, ObjectId id_step,
			List<DatabaseMeta> databases, Map<String, Counter> counters)
			throws KettleException {
		try {
			int nrfields = rep.countNrStepAttributes(id_step, "cctype");

			allocate(nrfields);

			for (int i = 0; i < nrfields; i++) {
				algorithm[i] = rep.getStepAttributeString(id_step, i,"algorithm");
				scheme[i] = rep.getStepAttributeString(id_step, i,"scheme");
				secretKeyLength[i] = rep.getStepAttributeString(id_step, i,"secretKeyLen");
				secretKeyCount[i] = rep.getStepAttributeString(id_step, i,"secretKeyCount");
			}
            secretKeyFieldName = rep.getStepAttributeString(id_step, "secretKeyFieldName");
            secretKeyLengthFieldName = rep.getStepAttributeString(id_step, "secretKeyLengthFieldName");
            algorithmFieldName = rep.getStepAttributeString(id_step, "algorithmFieldName");
            outputKeyInBinary = rep.getStepAttributeBoolean(id_step, "outputKeyInBinary");

		} catch (Exception e) {
			throw new KettleException("Unexpected error reading step information from the repository", e);
		}
	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
	throws KettleException {
		try {
			for (int i = 0; i < algorithm.length; i++) {
				rep.saveStepAttribute(id_transformation, id_step, i, "algorithm", algorithm[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "scheme", scheme[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "secretKeyLen", secretKeyLength[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "secretKeyCount", secretKeyCount[i]);
			}
			rep.saveStepAttribute(id_transformation, id_step, "secretKeyFieldName",      secretKeyFieldName);
			rep.saveStepAttribute(id_transformation, id_step, "secretKeyLengthFieldName",      secretKeyLengthFieldName);
			rep.saveStepAttribute(id_transformation, id_step, "algorithmFieldName",      algorithmFieldName);
			rep.saveStepAttribute(id_transformation, id_step, "outputKeyInBinary",      outputKeyInBinary);
		} catch (Exception e) {
			throw new KettleException( "Unable to save step information to the repository for id_step=" + id_step, e);
		}

	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta,
			StepMeta stepMeta, RowMetaInterface prev, String input[],
			String output[], RowMetaInterface info) {
		// See if we have input streams leading to this step!
		int nrRemarks = remarks.size();
		for (int i = 0; i < algorithm.length; i++) {
			int len= Const.toInt(transMeta.environmentSubstitute(getSecretKeyLength()[i]), -1);
			if (len<0) {
				CheckResult cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,BaseMessages.getString(PKG, "SecretKeyGeneratorMeta.CheckResult.WrongLen",String.valueOf(i)), stepMeta);
				remarks.add(cr);
			}
			int size= Const.toInt(transMeta.environmentSubstitute(getSecretKeyCount()[i]), -1);
			if (size<0) {
				CheckResult cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,BaseMessages.getString(PKG, "SecretKeyGeneratorMeta.CheckResult.WrongSize",String.valueOf(i)), stepMeta);
				remarks.add(cr);
			}	
		}
		if (remarks.size() == nrRemarks) {
			CheckResult cr = new CheckResult(CheckResult.TYPE_RESULT_OK,BaseMessages.getString(PKG, "SecretKeyGeneratorMeta.CheckResult.AllTypesSpecified"),stepMeta);
			remarks.add(cr);
		}
		
		if(Const.isEmpty(getSecretKeyFieldName())) {
			CheckResult cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,BaseMessages.getString(PKG, "SecretKeyGeneratorMeta.CheckResult.secretKeyFieldMissing"), stepMeta);
			remarks.add(cr);
		}
	}

	public StepInterface getStep(StepMeta stepMeta,
			StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
			Trans trans) {
		return new SecretKeyGenerator(stepMeta, stepDataInterface, cnr, transMeta,	trans);
	}

	public StepDataInterface getStepData() {
		return new SecretKeyGeneratorData();
	}

}