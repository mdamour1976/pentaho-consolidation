/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.trans.steps.infobrightoutput;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.w3c.dom.Node;

import com.infobright.etl.model.DataFormat;
import com.infobright.io.InfobrightNamedPipeLoader;


/**
 * Metadata for the Infobright loader.
 *
 * @author geoffrey.falk@infobright.com
 */
public class InfobrightLoaderMeta extends TableOutputMeta implements StepMetaInterface {

  private static final String TAG_DATA_FORMAT = "data_format";
  private static final String TAG_CHARSET = "charset";
  private static final String TAG_AGENT_PORT = "agent_port";
  private static final String TAG_DEBUG_FILE = "debug_file";
  
  private DataFormat dataFormat;
  private boolean rejectErrors = false;
  private Charset charset;
  private int agentPort;
  private String debugFile;
  
  /**
   * Default constructor.
   */
  public InfobrightLoaderMeta()
  {
    super();
    setIgnoreErrors(false);
    setTruncateTable(false);
  }

  /** {@inheritDoc}
   * @see org.pentaho.di.trans.step.StepMetaInterface#getStep(org.pentaho.di.trans.step.StepMeta, org.pentaho.di.trans.step.StepDataInterface, int, org.pentaho.di.trans.TransMeta, org.pentaho.di.trans.Trans)
   */
  public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
  {
    InfobrightLoader loader = new InfobrightLoader(stepMeta, stepDataInterface, cnr, tr, trans);
    return loader;
  }
  
  /** {@inheritDoc}
   * @see org.pentaho.di.trans.step.StepMetaInterface#getStepData()
   */
  public StepDataInterface getStepData()
  {
    return new InfobrightLoaderData();
  }

  /** {@inheritDoc}
   * @see org.pentaho.di.trans.step.BaseStepMeta#clone()
   */
  public Object clone()
  {
    InfobrightLoaderMeta retval = (InfobrightLoaderMeta) super.clone();
    return retval;
  }

  public String getInfobrightProductType() {
    return dataFormat.getDisplayText();
  }

  public void setDataFormat(DataFormat dataFormat) {
    this.dataFormat = dataFormat;
  }
  
  public void setDefault() {
    this.dataFormat = DataFormat.TXT_VARIABLE; // default for ICE
    // this.dataFormat = DataFormat.BINARY; // default for IEE
    this.agentPort = InfobrightNamedPipeLoader.AGENT_DEFAULT_PORT;
    this.charset = InfobrightNamedPipeLoader.DEFAULT_CHARSET;
  }

  public String getDebugFile() {
    return debugFile;
  }
  
  public void setCharset(Charset charset2) {
    this.charset = charset2;
  }
  
  public void setAgentPort(int agentPort2) {
    this.agentPort = agentPort2;
  }
  
  public void setDebugFile(String debugFile) {
    if ("".equals(debugFile.trim())) {
      this.debugFile = null;
    } else {
      this.debugFile = debugFile;
    }
  }
  
  @Override
  public String getXML() {
    String ret = super.getXML();
    ret = ret + new String("    "+XMLHandler.addTagValue(TAG_DATA_FORMAT, dataFormat.toString()));
    ret = ret + new String("    "+XMLHandler.addTagValue(TAG_AGENT_PORT, agentPort));
    ret = ret + new String("    "+XMLHandler.addTagValue(TAG_CHARSET, charset.name()));
    ret = ret + new String("    "+XMLHandler.addTagValue(TAG_DEBUG_FILE, debugFile));
    return ret;
  }

  //@SuppressWarnings("unchecked")
  @Override
  public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
      throws KettleXMLException {
    super.loadXML(stepnode, databases, counters);
    try {
      dataFormat = Enum.valueOf(DataFormat.class, XMLHandler.getTagValue(stepnode, TAG_DATA_FORMAT));
      agentPort = Integer.parseInt(Const.NVL(XMLHandler.getTagValue(stepnode, TAG_AGENT_PORT),
          Integer.toString(InfobrightNamedPipeLoader.AGENT_DEFAULT_PORT)));
      String charsetName = XMLHandler.getTagValue(stepnode, TAG_CHARSET);
      charset = (charsetName == null ? InfobrightNamedPipeLoader.DEFAULT_CHARSET :
        Charset.forName(charsetName));
      debugFile = XMLHandler.getTagValue(stepnode, TAG_DEBUG_FILE);
    } catch(Exception e) {
      throw new KettleXMLException("Unable to load step info from XML", e);
    }
  }

  @Override
  public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {
    super.readRep(rep, id_step, databases, counters);
    try {
      dataFormat = Enum.valueOf(DataFormat.class, rep.getStepAttributeString(id_step, TAG_DATA_FORMAT));
      String agentPortStr = rep.getStepAttributeString(id_step, TAG_AGENT_PORT);
      if (agentPortStr == null) {
        agentPort = InfobrightNamedPipeLoader.AGENT_DEFAULT_PORT;
      } else {
        agentPort = Integer.parseInt(agentPortStr);
      }
      String charsetName = rep.getStepAttributeString(id_step, TAG_CHARSET);
      charset = (charsetName == null ? InfobrightNamedPipeLoader.DEFAULT_CHARSET :
        Charset.forName(charsetName));
      debugFile    = rep.getStepAttributeString(id_step, TAG_DEBUG_FILE);
    } catch (Exception e) {
      throw new KettleException("Unexpected error reading step information from the repository", e);
    }
  }

  @Override
  public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException {
    super.saveRep(rep, id_transformation, id_step);
    rep.saveStepAttribute(id_transformation, id_step, TAG_DATA_FORMAT, dataFormat.toString());
    rep.saveStepAttribute(id_transformation, id_step, TAG_AGENT_PORT, agentPort);
    rep.saveStepAttribute(id_transformation, id_step, TAG_CHARSET, charset.name());
    rep.saveStepAttribute(id_transformation, id_step, TAG_DEBUG_FILE, debugFile);
  }
    
  /** @return the rejectErrors */
  public boolean isRejectErrors() {
    return rejectErrors;
  }

  /** @param rejectErrors the rejectErrors to set. */
  public void setRejectErrors(boolean rejectErrors) {
    this.rejectErrors = rejectErrors;
  }

  public int getAgentPort() {
    return agentPort;
  }

  public Charset getCharset() {
    return charset;
  }
}
