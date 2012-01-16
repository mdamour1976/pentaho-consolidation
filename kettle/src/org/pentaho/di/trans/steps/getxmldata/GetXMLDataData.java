/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.getxmldata;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

import org.dom4j.Document;
import org.dom4j.tree.AbstractNode;

/**
 * @author Samatar
 * @since 21-06-2007
 */
public class GetXMLDataData extends BaseStepData implements StepDataInterface 
{
	public String thisline, nextline, lastline;
	public Object[] previousRow;
	public RowMetaInterface inputRowMeta;
	public RowMetaInterface outputRowMeta;
	public RowMetaInterface convertRowMeta;
	public int nr_repeats;
	
	public NumberFormat nf;
	public DecimalFormat df;
	public DecimalFormatSymbols dfs;
	public SimpleDateFormat daf;
	public DateFormatSymbols dafs;
	
    public int nrInputFields;
    public String PathValue;
    public String prunePath; // identical to meta.getPrunePath() with some conditions set at init(), null when no pruning
    public boolean stopPruning; // used for a trick to stop the reader in pruning mode
    public boolean errorInRowButContinue; //true when actual row has an error and error handling is active: means continue (error handling in this step should be redesigned)
    public String tokenStart;
    public String tokenEnd;
    public int nodenr;
    public int nodesize;
    public List<AbstractNode> an;
    public Object[] readrow;
    public int totalpreviousfields;
    public Map<String, String> NAMESPACE = new HashMap<String, String>();
	public List<String> NSPath = new ArrayList<String>();
	
	public int nrReadRow;
    
    
	/**
	 * The XML files to read
	 */
	public FileInputList files;

	public FileObject file;
	public int     filenr;
	
	public FileInputStream fr;
	public BufferedInputStream is;
    public Document document;
    public String itemElement;
    public int itemCount;
    public int itemPosition;
    public long rownr;
    public int indexOfXmlField;
    
    RowMetaInterface      outputMeta;
    
	public String filename;
	public String shortFilename;
	public String path;	
	public String extension;	
	public boolean hidden;	
	public Date lastModificationDateTime;	
	public String uriName;	
	public String rootUriName;	
	public long size;

	/**
	 * 
	 */
	public GetXMLDataData()
	{
		super();

		thisline=null;
		nextline=null;
		nf = NumberFormat.getInstance();
		df = (DecimalFormat)nf;
		dfs=new DecimalFormatSymbols();
		daf = new SimpleDateFormat();
		dafs= new DateFormatSymbols();

		nr_repeats=0;
		previousRow=null;
		filenr = 0;
		
		fr=null;
		is=null;
		indexOfXmlField=-1;
		
		nrInputFields=-1;
		PathValue=null;
		tokenStart="@_";
		tokenEnd="-";
		nodenr=0;
		nodesize=0;
		an=null;
		readrow=null;
		totalpreviousfields=0;
		prunePath="";
		stopPruning=false;
		errorInRowButContinue=false;
		nrReadRow=0;
	}
}
