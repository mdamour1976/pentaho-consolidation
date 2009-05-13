package org.pentaho.di.trans.steps.webservices;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;


public class WebServiceData extends BaseStepData implements StepDataInterface 
{
	public String realUrl;
	
	public RowMetaInterface outputRowMeta;
	
	public Map<String,Integer> indexMap;
	
	public List<Object[]> argumentRows;
	
	public WebServiceData() {
		argumentRows = new ArrayList<Object[]>();
	}
	
}
