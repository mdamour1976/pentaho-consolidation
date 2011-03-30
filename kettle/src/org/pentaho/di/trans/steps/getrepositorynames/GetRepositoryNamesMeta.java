/* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

/* 
 * 
 * Created on 4-apr-2003
 * 
 */

package org.pentaho.di.trans.steps.getrepositorynames;

import java.util.List;
import java.util.Map;

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
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

public class GetRepositoryNamesMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = GetRepositoryNamesMeta.class; // i18n!

  /** Array of directories */
  private String          directory[];

  /** Name wild card (regular expression) */
  private String          nameMask[];

  /** Name Wild card to exclude (regular expression) */
  private String          excludeNameMask[];

  /**
   * Array of boolean values as string, indicating if we need to fetch sub
   * folders.
   */
  private boolean includeSubFolders[];

  /** Filter indicating object type filter */
  private ObjectTypeSelection      objectTypeSelection;


  /** Flag indicating that a row number field should be included in the output */
  private boolean         includeRowNumber;

  /** The name of the field in the output containing the row number */
  private String          rowNumberField;

  public GetRepositoryNamesMeta() {
    super(); // allocate BaseStepMeta
    
    objectTypeSelection=ObjectTypeSelection.All;
  }

  /**
   * @return Returns the rowNumberField.
   */
  public String getRowNumberField() {
    return rowNumberField;
  }



  public Object clone() {
    GetRepositoryNamesMeta retval = (GetRepositoryNamesMeta) super.clone();

    int nrfiles = directory.length;

    retval.allocate(nrfiles);

    for (int i = 0; i < nrfiles; i++) {
      retval.directory[i] = directory[i];
      retval.nameMask[i] = nameMask[i];
      retval.excludeNameMask[i] = excludeNameMask[i];
      retval.includeSubFolders[i] = includeSubFolders[i];
    }

    return retval;
  }

  public void allocate(int nrfiles) {
    directory = new String[nrfiles];
    nameMask = new String[nrfiles];
    excludeNameMask = new String[nrfiles];
    includeSubFolders = new boolean[nrfiles];
  }

  public void setDefault() {
    objectTypeSelection = ObjectTypeSelection.All;
    includeRowNumber = true;
    rowNumberField = "rownr";

    int nrfiles = 1;
    allocate(nrfiles);

    for (int i = 0; i < nrfiles; i++) {
      directory[i] = "/";
      nameMask[i] = ".*";
      excludeNameMask[i] = "";
      includeSubFolders[i] = true;
    }
  }

  public void getFields(RowMetaInterface row, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException {

    // the directory and name of the object
    //
    ValueMetaInterface object = new ValueMeta("object", ValueMeta.TYPE_STRING);
    object.setLength(500);
    object.setPrecision(-1);
    object.setOrigin(origin);
    row.addValueMeta(object);

    // the directory
    //
    ValueMetaInterface directory = new ValueMeta("directory", ValueMeta.TYPE_STRING);
    directory.setLength(500);
    directory.setPrecision(-1);
    directory.setOrigin(origin);
    row.addValueMeta(directory);

    // the name
    //
    ValueMetaInterface name = new ValueMeta("name", ValueMeta.TYPE_STRING);
    name.setLength(500);
    name.setPrecision(-1);
    name.setOrigin(origin);
    row.addValueMeta(name);

    // the object type
    //
    ValueMetaInterface objectType = new ValueMeta("object_type", ValueMeta.TYPE_STRING);
    objectType.setLength(500);
    objectType.setPrecision(-1);
    objectType.setOrigin(origin);
    row.addValueMeta(objectType);

    // object_id
    //
    ValueMetaInterface objectId = new ValueMeta("object_id", ValueMeta.TYPE_STRING);
    object.setLength(500);
    object.setPrecision(-1);
    objectId.setOrigin(origin);
    row.addValueMeta(objectId);

    // modified by
    //
    ValueMetaInterface modifiedBy = new ValueMeta("modified_by", ValueMeta.TYPE_STRING);
    object.setLength(500);
    object.setPrecision(-1);
    modifiedBy.setOrigin(origin);
    row.addValueMeta(modifiedBy);

    // modified date
    //
    ValueMetaInterface modifiedDate = new ValueMeta("modified_date", ValueMeta.TYPE_DATE);
    modifiedDate.setOrigin(origin);
    row.addValueMeta(modifiedDate);
    
    if (includeRowNumber) {
      ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(rowNumberField), ValueMeta.TYPE_INTEGER);
      v.setLength(ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0);
      v.setOrigin(origin);
      row.addValueMeta(v);
    }

  }

  public String getXML() {
    StringBuffer retval = new StringBuffer(300);

    retval.append("    ").append(XMLHandler.addTagValue("object_type", objectTypeSelection.toString()));
    retval.append("    ").append(XMLHandler.addTagValue("rownum", includeRowNumber));
    retval.append("    ").append(XMLHandler.addTagValue("rownum_field", rowNumberField));

    retval.append("    <file>").append(Const.CR);

    for (int i = 0; i < directory.length; i++) {
      retval.append("      ").append(XMLHandler.addTagValue("directory", directory[i]));
      retval.append("      ").append(XMLHandler.addTagValue("name_mask", nameMask[i]));
      retval.append("      ").append(XMLHandler.addTagValue("exclude_name_mask", excludeNameMask[i]));
      retval.append("      ").append(XMLHandler.addTagValue("include_subfolders", includeSubFolders[i]));
    }
    retval.append("    </file>").append(Const.CR);

    return retval.toString();
  }

  public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
    try {

      includeRowNumber = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "rownum"));
      rowNumberField = XMLHandler.getTagValue(stepnode, "rownum_field");

      Node filenode = XMLHandler.getSubNode(stepnode, "file");
      int nrfiles = XMLHandler.countNodes(filenode, "directory");

      allocate(nrfiles);

      for (int i = 0; i < nrfiles; i++) {
        Node filenamenode = XMLHandler.getSubNodeByNr(filenode, "directory", i);
        Node filemasknode = XMLHandler.getSubNodeByNr(filenode, "name_mask", i);
        Node excludefilemasknode = XMLHandler.getSubNodeByNr(filenode, "exclude_name_mask", i);
        Node includeSubFoldersnode = XMLHandler.getSubNodeByNr(filenode, "include_subfolders", i);
        directory[i] = XMLHandler.getNodeValue(filenamenode);
        nameMask[i] = XMLHandler.getNodeValue(filemasknode);
        excludeNameMask[i] = XMLHandler.getNodeValue(excludefilemasknode);
        includeSubFolders[i] = "Y".equalsIgnoreCase( XMLHandler.getNodeValue(includeSubFoldersnode) );
      }
    } catch (Exception e) {
      throw new KettleXMLException("Unable to load step info from XML", e);
    }
  }

  public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {
    try {
      int nrfiles = rep.countNrStepAttributes(id_step, "file_name");
      String objectTypeString = rep.getStepAttributeString(id_step, "object_type");
      if (objectTypeSelection!=null) {
        objectTypeSelection = ObjectTypeSelection.valueOf(objectTypeString);
      } 
      if (objectTypeSelection==null) objectTypeSelection=ObjectTypeSelection.All;
      includeRowNumber = rep.getStepAttributeBoolean(id_step, "rownum");
      rowNumberField = rep.getStepAttributeString(id_step, "rownum_field");
      
      allocate(nrfiles);

      for (int i = 0; i < nrfiles; i++) {
        directory[i] = rep.getStepAttributeString(id_step, i, "directory");
        nameMask[i] = rep.getStepAttributeString(id_step, i, "name_mask");
        excludeNameMask[i] = rep.getStepAttributeString(id_step, i, "exclude_name_mask");
        includeSubFolders[i] = rep.getStepAttributeBoolean(id_step, i, "include_subfolders");
      }
    } catch (Exception e) {
      throw new KettleException("Unexpected error reading step information from the repository", e);
    }
  }

  public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException {
    try {
      rep.saveStepAttribute(id_transformation, id_step, "object_type", objectTypeSelection.toString());
      rep.saveStepAttribute(id_transformation, id_step, "rownum", includeRowNumber);
      rep.saveStepAttribute(id_transformation, id_step, "rownum_field", rowNumberField);

      for (int i = 0; i < directory.length; i++) {
        rep.saveStepAttribute(id_transformation, id_step, i, "directory", directory[i]);
        rep.saveStepAttribute(id_transformation, id_step, i, "name_mask", nameMask[i]);
        rep.saveStepAttribute(id_transformation, id_step, i, "exclude_name_mask", excludeNameMask[i]);
        rep.saveStepAttribute(id_transformation, id_step, i, "include_subfolders", includeSubFolders[i]);
      }
    } catch (Exception e) {
      throw new KettleException("Unable to save step information to the repository for id_step=" + id_step, e);
    }
  }

  public boolean[] getIncludeSubFolders() {
    return includeSubFolders;
  }

  public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info) {
    CheckResult cr;

    if (input.length > 0) {
      cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "GetRepositoryNamesMeta.CheckResult.NoInputError"), stepinfo);
    } else {
      cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "GetRepositoryNamesMeta.CheckResult.NoInputOk"), stepinfo);
    }
    remarks.add(cr);
  }

  public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans) {
    return new GetRepositoryNames(stepMeta, stepDataInterface, cnr, transMeta, trans);
  }

  public StepDataInterface getStepData() {
    return new GetRepositoryNamesData();
  }

  /**
   * @return the includeRowNumber
   */
  public boolean isIncludeRowNumber() {
    return includeRowNumber;
  }

  /**
   * @param includeRowNumber the includeRowNumber to set
   */
  public void setIncludeRowNumber(boolean includeRowNumber) {
    this.includeRowNumber = includeRowNumber;
  }

  /**
   * @param includeSubFolders the includeSubFolders to set
   */
  public void setIncludeSubFolders(boolean[] includeSubFolders) {
    this.includeSubFolders = includeSubFolders;
  }

  /**
   * @param rowNumberField the rowNumberField to set
   */
  public void setRowNumberField(String rowNumberField) {
    this.rowNumberField = rowNumberField;
  }

  /**
   * @return the objectTypeSelection
   */
  public ObjectTypeSelection getObjectTypeSelection() {
    return objectTypeSelection;
  }

  /**
   * @param objectTypeSelection the objectTypeSelection to set
   */
  public void setObjectTypeSelection(ObjectTypeSelection objectTypeSelection) {
    this.objectTypeSelection = objectTypeSelection;
  }

  /**
   * @return the directory
   */
  public String[] getDirectory() {
    return directory;
  }

  /**
   * @param directory the directory to set
   */
  public void setDirectory(String[] directory) {
    this.directory = directory;
  }

  /**
   * @return the nameMask
   */
  public String[] getNameMask() {
    return nameMask;
  }

  /**
   * @param nameMask the nameMask to set
   */
  public void setNameMask(String[] nameMask) {
    this.nameMask = nameMask;
  }

  /**
   * @return the excludeNameMask
   */
  public String[] getExcludeNameMask() {
    return excludeNameMask;
  }

  /**
   * @param excludeNameMask the excludeNameMask to set
   */
  public void setExcludeNameMask(String[] excludeNameMask) {
    this.excludeNameMask = excludeNameMask;
  }
}