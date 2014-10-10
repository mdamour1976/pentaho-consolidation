/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.xmlinput;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.zip.ZipInputStream;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author Matt
 * @since 22-jan-2005
 */
public class XMLInputData extends BaseStepData implements StepDataInterface {
  public String thisline, nextline, lastline;

  public Object[] previousRow;

  public int nr_repeats;

  public NumberFormat nf;

  public DecimalFormat df;

  public DecimalFormatSymbols dfs;

  public SimpleDateFormat daf;

  public DateFormatSymbols dafs;

  public RowMetaInterface outputRowMeta;

  public RowMetaInterface previousRowMeta;

  public List<FileObject> files;

  public boolean last_file;

  public FileObject file;

  public int filenr;

  public FileInputStream fr;

  public ZipInputStream zi;

  public BufferedInputStream is;

  public Document document;

  public Node section;

  public String itemElement;

  public int itemCount;

  public int itemPosition;

  public long rownr;

  public RowMetaInterface convertRowMeta;

  public XMLInputData() {
    super();

    thisline = null;
    nextline = null;
    nf = NumberFormat.getInstance();
    df = (DecimalFormat) nf;
    dfs = new DecimalFormatSymbols();
    daf = new SimpleDateFormat();
    dafs = new DateFormatSymbols();

    nr_repeats = 0;
    previousRow = null;
    filenr = 0;

    fr = null;
    zi = null;
    is = null;
  }

}
