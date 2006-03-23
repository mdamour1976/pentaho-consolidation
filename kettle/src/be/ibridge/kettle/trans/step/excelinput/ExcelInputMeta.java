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


/* 
 * 
 * Created on 4-apr-2003
 * 
 */

package be.ibridge.kettle.trans.step.excelinput;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;
import be.ibridge.kettle.trans.step.fileinput.FileInputList;


public class ExcelInputMeta extends BaseStepMeta implements StepMetaInterface
{
	private static final String NO = "N";

	private static final String YES = "Y";
	public final static int TYPE_TRIM_NONE  = 0;
	public final static int TYPE_TRIM_LEFT  = 1;
	public final static int TYPE_TRIM_RIGHT = 2;
	public final static int TYPE_TRIM_BOTH  = 3;
	
	public final static String type_trim_desc[] = { "none", "left", "right", "both" };

	public  static final String STRING_SEPARATOR = " \t --> ";

	/**
	 * The filenames to load or directory in case a filemask was set.
	 */
	private  String  fileName[];
	
	/**
	 * The regular expression to use (null means: no mask)
	 */
	private  String  fileMask[];
	
	/** Array of boolean values as string, indicating if a file is required. */
	private  String  fileRequired[];

	/**
	 * The fieldname that holds the name of the file
	 */
	private String       fileField;

	/**
	 * The names of the sheets to load.
	 * Null means: all sheets...
	 */
	private String       sheetName[];

	/**
	 * The row-nr where we start processing.
	 */
	private int          startRow[];

	/**
	 * The column-nr where we start processing.
	 */
	private int          startColumn[];

	/**
	 * The fieldname that holds the name of the sheet
	 */
	private String       sheetField;
	
	
	/**
	 * The cell-range starts with a header-row
	 */
	private  boolean startsWithHeader;
	
	/**
	 * Stop reading when you hit an empty row.
	 */
	private  boolean stopOnEmpty;
	
	/**
	 * Avoid empty rows in the result.
	 */
	private boolean ignoreEmptyRows;
	
	/**
	 * The fieldname containing the row number.
	 * An empty (null) value means that no row number is included in the output.
	 */
	private  String  rowNumberField;

	/**
	 * The maximum number of rows that this step writes to the next step.
	 */
	private  long    rowLimit;

	/**
	 * The names of the fields to read in the range.
	 * Note: the number of columns in the range has to match field.length
	 */
	private  String fieldName[];
	
	/**
	 * The data types of the fields 
	 */
	private  int    fieldType[];

	/**
	 * The lengths of the fields
	 */
	private  int fieldLength[];
	
	/**
	 * The precisions of the fields.
	 */
	private  int fieldPrecision[];
	
	/**
	 * Specifies how to trim the (text) field
	 */
	private  int fieldTrimType[];
	
	/**
	 * Repeat the previous field value if this one is empty
	 */
	private  boolean fieldRepeat[];
	
    /** Strict types : will generate erros */
    private boolean strictTypes;
	
	/** Ignore error : turn into warnings */
    private boolean errorIgnored;
    
    /** If error line are skipped, you can replay without introducing doubles.*/
	private boolean errorLineSkipped;
	
    /** The directory that will contain bad line files */
    private String badLineFilesDestinationDirectory;
    
    /** The extension of bad line files */
    private String badLineFilesExtension;
    
    /** The directory that will contain error line files */
    private String errorLineFilesDestinationDirectory;
    
    /** The extension of error line files */
    private String errorLineFilesExtension;
    
    /** The directory that will contain line number files */
    private String lineNumberFilesDestinationDirectory;
    
    /** The extension of line number files */
    private String lineNumberFilesExtension;
	
	
	public ExcelInputMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
	
	

    /**
     * @return Returns the fieldLength.
     */
    public int[] getFieldLength()
    {
        return fieldLength;
    }
    
    /**
     * @param fieldLength The fieldLength to set.
     */
    public void setFieldLength(int[] fieldLength)
    {
        this.fieldLength = fieldLength;
    }
    
    /**
     * @return Returns the fieldName.
     */
    public String[] getFieldName()
    {
        return fieldName;
    }
    
    /**
     * @param fieldName The fieldName to set.
     */
    public void setFieldName(String[] fieldName)
    {
        this.fieldName = fieldName;
    }
    
    /**
     * @return Returns the fieldPrecision.
     */
    public int[] getFieldPrecision()
    {
        return fieldPrecision;
    }
    
    /**
     * @param fieldPrecision The fieldPrecision to set.
     */
    public void setFieldPrecision(int[] fieldPrecision)
    {
        this.fieldPrecision = fieldPrecision;
    }
    
    /**
     * @return Returns the fieldRepeat.
     */
    public boolean[] getFieldRepeat()
    {
        return fieldRepeat;
    }
    
    /**
     * @param fieldRepeat The fieldRepeat to set.
     */
    public void setFieldRepeat(boolean[] fieldRepeat)
    {
        this.fieldRepeat = fieldRepeat;
    }
    
    /**
     * @return Returns the fieldTrimType.
     */
    public int[] getFieldTrimType()
    {
        return fieldTrimType;
    }
    
    /**
     * @param fieldTrimType The fieldTrimType to set.
     */
    public void setFieldTrimType(int[] fieldTrimType)
    {
        this.fieldTrimType = fieldTrimType;
    }
    
    /**
     * @return Returns the fieldType.
     */
    public int[] getFieldType()
    {
        return fieldType;
    }
    
    /**
     * @param fieldType The fieldType to set.
     */
    public void setFieldType(int[] fieldType)
    {
        this.fieldType = fieldType;
    }
    
    /**
     * @return Returns the fileField.
     */
    public String getFileField()
    {
        return fileField;
    }
    
    /**
     * @param fileField The fileField to set.
     */
    public void setFileField(String fileField)
    {
        this.fileField = fileField;
    }
    
    /**
     * @return Returns the fileMask.
     */
    public String[] getFileMask()
    {
        return fileMask;
    }
    
    /**
     * @param fileMask The fileMask to set.
     */
    public void setFileMask(String[] fileMask)
    {
        this.fileMask = fileMask;
    }
    
    /**
     * @return Returns the fileName.
     */
    public String[] getFileName()
    {
        return fileName;
    }
    
    /**
     * @param fileName The fileName to set.
     */
    public void setFileName(String[] fileName)
    {
        this.fileName = fileName;
    }
    
    /**
     * @return Returns the ignoreEmptyRows.
     */
    public boolean ignoreEmptyRows()
    {
        return ignoreEmptyRows;
    }
    
    /**
     * @param ignoreEmptyRows The ignoreEmptyRows to set.
     */
    public void setIgnoreEmptyRows(boolean ignoreEmptyRows)
    {
        this.ignoreEmptyRows = ignoreEmptyRows;
    }
    
    /**
     * @return Returns the rowLimit.
     */
    public long getRowLimit()
    {
        return rowLimit;
    }
    
    /**
     * @param rowLimit The rowLimit to set.
     */
    public void setRowLimit(long rowLimit)
    {
        this.rowLimit = rowLimit;
    }
    
    /**
     * @return Returns the rowNumberField.
     */
    public String getRowNumberField()
    {
        return rowNumberField;
    }
    
    /**
     * @param rowNumberField The rowNumberField to set.
     */
    public void setRowNumberField(String rowNumberField)
    {
        this.rowNumberField = rowNumberField;
    }
    
    /**
     * @return Returns the sheetField.
     */
    public String getSheetField()
    {
        return sheetField;
    }
    
    /**
     * @param sheetField The sheetField to set.
     */
    public void setSheetField(String sheetField)
    {
        this.sheetField = sheetField;
    }
    
    /**
     * @return Returns the sheetName.
     */
    public String[] getSheetName()
    {
        return sheetName;
    }
    
    /**
     * @param sheetName The sheetName to set.
     */
    public void setSheetName(String[] sheetName)
    {
        this.sheetName = sheetName;
    }
    
    /**
     * @return Returns the startColumn.
     */
    public int[] getStartColumn()
    {
        return startColumn;
    }
    
    /**
     * @param startColumn The startColumn to set.
     */
    public void setStartColumn(int[] startColumn)
    {
        this.startColumn = startColumn;
    }
    
    /**
     * @return Returns the startRow.
     */
    public int[] getStartRow()
    {
        return startRow;
    }
    
    /**
     * @param startRow The startRow to set.
     */
    public void setStartRow(int[] startRow)
    {
        this.startRow = startRow;
    }
    
    /**
     * @return Returns the startsWithHeader.
     */
    public boolean startsWithHeader()
    {
        return startsWithHeader;
    }
    
    /**
     * @param startsWithHeader The startsWithHeader to set.
     */
    public void setStartsWithHeader(boolean startsWithHeader)
    {
        this.startsWithHeader = startsWithHeader;
    }
    
    /**
     * @return Returns the stopOnEmpty.
     */
    public boolean stopOnEmpty()
    {
        return stopOnEmpty;
    }
    
    /**
     * @param stopOnEmpty The stopOnEmpty to set.
     */
    public void setStopOnEmpty(boolean stopOnEmpty)
    {
        this.stopOnEmpty = stopOnEmpty;
    }
    
    
    
	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	public Object clone()
	{
		ExcelInputMeta retval = (ExcelInputMeta)super.clone();
		
		int nrfiles  = fileName.length;
		int nrsheets = sheetName.length;
		int nrfields = fieldName.length;

		retval.allocate(nrfiles, nrsheets, nrfields);
		
		for (int i=0;i<nrfields;i++)
		{
			retval.fieldName[i]     = fieldName[i];
			retval.fieldType[i]     = fieldType[i];
			retval.fieldLength[i]    = fieldLength[i];
			retval.fieldPrecision[i] = fieldPrecision[i];
			retval.fieldTrimType[i] = fieldTrimType[i];
			retval.fieldRepeat[i]    = fieldRepeat[i];
		}
		
		for (int i=0;i<nrfiles;i++)
		{
			retval.fileName[i] = fileName[i];
			retval.fileMask[i] = fileMask[i];
		}
		
		for (int i=0;i<nrsheets;i++)
		{
			retval.sheetName[i] = sheetName[i];
		}
		
		return retval;
	}

	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
			startsWithHeader          = YES.equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "header"));
			String nempty   = XMLHandler.getTagValue(stepnode, "noempty");
			ignoreEmptyRows         = YES.equalsIgnoreCase(nempty) || nempty==null;
			String soempty  = XMLHandler.getTagValue(stepnode, "stoponempty");
			stopOnEmpty     = YES.equalsIgnoreCase(soempty) || nempty==null;
			rowNumberField    = XMLHandler.getTagValue(stepnode, "rownum_field");
			rowNumberField    = XMLHandler.getTagValue(stepnode, "rownumfield");
			rowLimit           = Const.toLong(XMLHandler.getTagValue(stepnode, "limit"), 0);
			sheetField      = XMLHandler.getTagValue(stepnode, "sheetfield");
			fileField       = XMLHandler.getTagValue(stepnode, "filefield");
					
			Node filenode   = XMLHandler.getSubNode(stepnode, "file");
			Node sheetsnode = XMLHandler.getSubNode(stepnode, "sheets");
			Node fields     = XMLHandler.getSubNode(stepnode, "fields");
			int nrfiles   = XMLHandler.countNodes(filenode,   "name");
			int nrsheets  = XMLHandler.countNodes(sheetsnode, "sheet");
			int nrfields  = XMLHandler.countNodes(fields,     "field");
	
			allocate(nrfiles, nrsheets, nrfields);		
	
			for (int i=0;i<nrfiles;i++)
			{
				Node filenamenode = XMLHandler.getSubNodeByNr(filenode, "name", i); 
				Node filemasknode = XMLHandler.getSubNodeByNr(filenode, "filemask", i); 
				Node fileRequirednode = XMLHandler.getSubNodeByNr(filenode, "file_required", i);
				fileName[i] = XMLHandler.getNodeValue(filenamenode);
				fileMask[i] = XMLHandler.getNodeValue(filemasknode);
				fileRequired[i] = XMLHandler.getNodeValue(fileRequirednode);
			}
			
			for (int i=0;i<nrfields;i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
				
				fieldName[i]        = XMLHandler.getTagValue(fnode, "name");
				fieldType[i]        = Value.getType(XMLHandler.getTagValue(fnode, "type"));
				fieldLength[i]      = Const.toInt(XMLHandler.getTagValue(fnode, "length"), -1);
				fieldPrecision[i]   = Const.toInt(XMLHandler.getTagValue(fnode, "precision"), -1);
				String srepeat      = XMLHandler.getTagValue(fnode, "repeat");
				fieldTrimType[i]    = getTrimType(XMLHandler.getTagValue(fnode, "trim_type"));
				
				if (srepeat!=null) fieldRepeat[i] = YES.equalsIgnoreCase(srepeat); 
				else               fieldRepeat[i]=false;
			}
			
			for (int i=0;i<nrsheets;i++)
			{
				Node snode = XMLHandler.getSubNodeByNr(sheetsnode, "sheet", i);
				
				sheetName[i] = XMLHandler.getTagValue(snode, "name");
				startRow[i]  = Const.toInt(XMLHandler.getTagValue(snode, "startrow"), 0);
				startColumn[i]  = Const.toInt(XMLHandler.getTagValue(snode, "startcol"), 0);
			}
			
            strictTypes = YES.equalsIgnoreCase( XMLHandler.getTagValue(stepnode, "strict_types") );
            errorIgnored = YES.equalsIgnoreCase( XMLHandler.getTagValue(stepnode, "error_ignored") );
            errorLineSkipped = YES.equalsIgnoreCase( XMLHandler.getTagValue(stepnode, "error_line_skipped") );
            badLineFilesDestinationDirectory = XMLHandler.getTagValue(stepnode, "bad_line_files_destination_directory");
            badLineFilesExtension = XMLHandler.getTagValue(stepnode, "bad_line_files_extension");
            errorLineFilesDestinationDirectory = XMLHandler.getTagValue(stepnode, "error_line_files_destination_directory");
            errorLineFilesExtension = XMLHandler.getTagValue(stepnode, "error_line_files_extension");
            lineNumberFilesDestinationDirectory = XMLHandler.getTagValue(stepnode, "line_number_files_destination_directory");
            lineNumberFilesExtension = XMLHandler.getTagValue(stepnode, "line_number_files_extension");
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to read step information from XML", e);
		}
	}
	
	public void allocate(int nrfiles, int nrsheets, int nrfields)
	{
		fileName   = new String [nrfiles];
		fileMask   = new String [nrfiles];
		fileRequired = new String[nrfiles];
		
		sheetName  = new String [nrsheets];
		startRow   = new int    [nrsheets];
		startColumn   = new int    [nrsheets];
		
		fieldName      = new String [nrfields];
		fieldType      = new int    [nrfields];
		fieldLength     = new int    [nrfields];
		fieldPrecision  = new int    [nrfields];
		fieldTrimType  = new int    [nrfields];
		fieldRepeat     = new boolean[nrfields];
	}
	
	public void setDefault()
	{
		startsWithHeader     = true;
		ignoreEmptyRows    = true;
		rowNumberField = "";
		
		int nrfiles=0;
		int nrfields=0;
		int nrsheets=0;

		allocate(nrfiles, nrsheets, nrfields);	
		
		for (int i=0;i<nrfiles;i++) 
		{
			fileName[i]="filename"+(i+1);
			fileMask[i]="";
			fileRequired[i] = NO;
		}
		
		for (int i=0;i<nrfields;i++)
		{
			fieldName[i]      = "field"+i;				
			fieldType[i]      = Value.VALUE_TYPE_NUMBER;
			fieldLength[i]    = 9;
			fieldPrecision[i] = 2;
			fieldTrimType[i]   = TYPE_TRIM_NONE;
			fieldRepeat[i]      = false;
		}
			
		rowLimit=0L;
		
		strictTypes = false;
		errorIgnored = false;
		errorLineSkipped = false;
		badLineFilesDestinationDirectory = null;
        badLineFilesExtension = "bad";
        errorLineFilesDestinationDirectory = null;
        errorLineFilesExtension = "error";
        lineNumberFilesDestinationDirectory = null;
        lineNumberFilesExtension = "line";
	}
	
	public Row getFields(Row r, String name, Row info)
	{
		Row row;
		if (r==null) row=new Row(); // give back values
		else         row=r;         // add to the existing row of values...
		
		int i;
		for (i=0;i<fieldName.length;i++)
		{
			int type=fieldType[i];
			if (type==Value.VALUE_TYPE_NONE) type=Value.VALUE_TYPE_STRING;
			Value v=new Value(fieldName[i], type);
			v.setLength(fieldLength[i], fieldPrecision[i]);
			v.setOrigin(name);
			row.addValue(v);
		}
		if (fileField!=null && fileField.length()>0)
		{
			Value v = new Value(fileField, Value.VALUE_TYPE_STRING);
			v.setLength(250, -1);
			v.setOrigin(name);
			row.addValue(v);
		}
		if (sheetField!=null && sheetField.length()>0)
		{
			Value v = new Value(sheetField, Value.VALUE_TYPE_STRING);
			v.setLength(250, -1);
			v.setOrigin(name);
			row.addValue(v);
		}
		if (rowNumberField!=null && rowNumberField.length()>0)
		{
			Value v = new Value(rowNumberField, Value.VALUE_TYPE_NUMBER);
			v.setLength(7, 0);
			v.setOrigin(name);
			row.addValue(v);
		}
		return row;
	}
	
	public String getXML()
	{
		String retval="";
		
		retval+="    "+XMLHandler.addTagValue("header",          startsWithHeader);
		retval+="    "+XMLHandler.addTagValue("noempty",         ignoreEmptyRows);
		retval+="    "+XMLHandler.addTagValue("stoponempty",     stopOnEmpty);
		retval+="    "+XMLHandler.addTagValue("filefield",       fileField);
		retval+="    "+XMLHandler.addTagValue("sheetfield",      sheetField);
		retval+="    "+XMLHandler.addTagValue("rownumfield",     rowNumberField);
		retval+="    "+XMLHandler.addTagValue("sheetfield",      sheetField);
		retval+="    "+XMLHandler.addTagValue("filefield",       fileField);
		retval+="    "+XMLHandler.addTagValue("limit",           rowLimit);

		/*
		 * Describe the files to read
		 */
		retval+="    <file>"+Const.CR;
		for (int i=0;i<fileName.length;i++)
		{
			retval+="      "+XMLHandler.addTagValue("name",     fileName[i]);
			retval+="      "+XMLHandler.addTagValue("filemask", fileMask[i]);
			retval+="      "+XMLHandler.addTagValue("file_required", fileRequired[i]);
		}
		retval+="      </file>"+Const.CR;

		/*
		 * Describe the fields to read
		 */
		retval+="    <fields>"+Const.CR;
		for (int i=0;i<fieldName.length;i++)
		{
			retval+="      <field>"+Const.CR;
			retval+="        "+XMLHandler.addTagValue("name",      fieldName[i]);
			retval+="        "+XMLHandler.addTagValue("type",      Value.getTypeDesc(fieldType[i]));
			retval+="        "+XMLHandler.addTagValue("length",    fieldLength[i]);
			retval+="        "+XMLHandler.addTagValue("precision", fieldPrecision[i]);
			retval+="        "+XMLHandler.addTagValue("trim_type", getTrimTypeDesc( fieldTrimType[i] ));
			retval+="        "+XMLHandler.addTagValue("repeat",    fieldRepeat[i]);
			retval+="        </field>"+Const.CR;
		}
		retval+="      </fields>"+Const.CR;

		/*
		 * Describe the sheets to load... 
		 */
		retval+="    <sheets>"+Const.CR;
		for (int i=0;i<sheetName.length;i++)
		{
			retval+="      <sheet>"+Const.CR;
			retval+="        "+XMLHandler.addTagValue("name",      sheetName[i]);
			retval+="        "+XMLHandler.addTagValue("startrow",  startRow[i]);
			retval+="        "+XMLHandler.addTagValue("startcol",  startColumn[i]);
			retval+="        </sheet>"+Const.CR;
		}
		retval+="      </sheets>"+Const.CR;
		
        // ERROR HANDLING
        retval += "    " + XMLHandler.addTagValue("strict_types", strictTypes);
        retval += "    " + XMLHandler.addTagValue("error_ignored", errorIgnored);
        retval += "    " + XMLHandler.addTagValue("error_line_skipped", errorLineSkipped);
        
        retval += "    " + XMLHandler.addTagValue("bad_line_files_destination_directory", badLineFilesDestinationDirectory);
        retval += "    " + XMLHandler.addTagValue("bad_line_files_extension", badLineFilesExtension);
        retval += "    " + XMLHandler.addTagValue("error_line_files_destination_directory", errorLineFilesDestinationDirectory);
        retval += "    " + XMLHandler.addTagValue("error_line_files_extension", errorLineFilesExtension);
        retval += "    " + XMLHandler.addTagValue("line_number_files_destination_directory", lineNumberFilesDestinationDirectory);
        retval += "    " + XMLHandler.addTagValue("line_number_files_extension", lineNumberFilesExtension);

		
		
		return retval;
	}
	
	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
			startsWithHeader  =      rep.getStepAttributeBoolean(id_step, "header");
			ignoreEmptyRows   =      rep.getStepAttributeBoolean(id_step, "noempty");  
			stopOnEmpty       =      rep.getStepAttributeBoolean(id_step, "stoponempty");  
			fileField         =      rep.getStepAttributeString (id_step, "filefield");
			sheetField        =      rep.getStepAttributeString (id_step, "sheetfield");
			rowNumberField    =      rep.getStepAttributeString (id_step, "rownumfield");
			rowLimit          = (int)rep.getStepAttributeInteger(id_step, "limit");
	
			int nrfiles     = rep.countNrStepAttributes(id_step, "file_name");
			int nrsheets    = rep.countNrStepAttributes(id_step, "sheet_name");
			int nrfields    = rep.countNrStepAttributes(id_step, "field_name");
			
			allocate(nrfiles, nrsheets, nrfields);

            // System.out.println("Counted "+nrfiles+" files to read and "+nrsheets+" sheets, "+nrfields+" fields.");
			for (int i=0;i<nrfiles;i++)
			{
				fileName[i] =      rep.getStepAttributeString (id_step, i, "file_name"    );
				fileMask[i] =      rep.getStepAttributeString (id_step, i, "file_mask"    );
				fileRequired[i] = rep.getStepAttributeString(id_step, i, "file_required");
                if(!YES.equalsIgnoreCase(fileRequired[i]))
                	fileRequired[i] = NO;
			}

			for (int i=0;i<nrsheets;i++)
			{
				sheetName[i] =      rep.getStepAttributeString (id_step, i, "sheet_name"      );
				startRow[i]  = (int)rep.getStepAttributeInteger(id_step, i, "sheet_startrow"  );
				startColumn[i]  = (int)rep.getStepAttributeInteger(id_step, i, "sheet_startcol"  );
			}

			for (int i=0;i<nrfields;i++)
			{
				fieldName[i]     =                rep.getStepAttributeString (id_step, i, "field_name");
				fieldType[i]     = Value.getType( rep.getStepAttributeString (id_step, i, "field_type") );
				fieldLength[i]    = (int)          rep.getStepAttributeInteger(id_step, i, "field_length");
				fieldPrecision[i] = (int)          rep.getStepAttributeInteger(id_step, i, "field_precision");
				fieldTrimType[i] = getTrimType(   rep.getStepAttributeString (id_step, i, "field_trim_type") );
				fieldRepeat[i]    =                rep.getStepAttributeBoolean(id_step, i, "field_repeat");
			}		
			
            strictTypes = rep.getStepAttributeBoolean(id_step, 0, "strict_types", false);
			errorIgnored = rep.getStepAttributeBoolean(id_step, 0, "error_ignored", false);
            errorLineSkipped = rep.getStepAttributeBoolean(id_step, 0, "error_line_skipped", false);
            
            badLineFilesDestinationDirectory = rep.getStepAttributeString(id_step, "bad_line_files_dest_dir");
            badLineFilesExtension = rep.getStepAttributeString(id_step, "bad_line_files_ext");
            errorLineFilesDestinationDirectory = rep.getStepAttributeString(id_step, "error_line_files_dest_dir");
            errorLineFilesExtension = rep.getStepAttributeString(id_step, "error_line_files_ext");
            lineNumberFilesDestinationDirectory = rep.getStepAttributeString(id_step, "line_number_files_dest_dir");
            lineNumberFilesExtension = rep.getStepAttributeString(id_step, "line_number_files_ext");
		}
		catch(Exception e)
		{
			throw new KettleException("Unexpected error reading step information from the repository", e);
		}
	}
	
	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "header",          startsWithHeader);
			rep.saveStepAttribute(id_transformation, id_step, "noempty",         ignoreEmptyRows);
			rep.saveStepAttribute(id_transformation, id_step, "stoponempty",     stopOnEmpty);
			rep.saveStepAttribute(id_transformation, id_step, "filefield",       fileField);
			rep.saveStepAttribute(id_transformation, id_step, "sheetfield",      sheetField);
			rep.saveStepAttribute(id_transformation, id_step, "rownumfield",     rowNumberField);
			rep.saveStepAttribute(id_transformation, id_step, "limit",           rowLimit);
			
			for (int i=0;i<fileName.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "file_name",     fileName[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "file_mask",     fileMask[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "file_required", fileRequired[i]);
			}
	
			for (int i=0;i<sheetName.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "sheet_name",      sheetName[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "sheet_startrow",  startRow[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "sheet_startcol",  startColumn[i]);
			}
	
			for (int i=0;i<fieldName.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "field_name",      fieldName[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "field_type",      Value.getTypeDesc(fieldType[i]));
				rep.saveStepAttribute(id_transformation, id_step, i, "field_length",    fieldLength[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "field_precision", fieldPrecision[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "field_trim_type", getTrimTypeDesc( fieldTrimType[i] ));
				rep.saveStepAttribute(id_transformation, id_step, i, "field_repeat",    fieldRepeat[i]);
			}
			
            rep.saveStepAttribute(id_transformation, id_step, "strict_types", strictTypes);
			rep.saveStepAttribute(id_transformation, id_step, "error_ignored", errorIgnored);
            rep.saveStepAttribute(id_transformation, id_step, "error_line_skipped", errorLineSkipped);
            
            rep.saveStepAttribute(id_transformation, id_step, "bad_line_files_dest_dir", badLineFilesDestinationDirectory);
            rep.saveStepAttribute(id_transformation, id_step, "bad_line_files_ext", badLineFilesExtension);
            rep.saveStepAttribute(id_transformation, id_step, "error_line_files_dest_dir", errorLineFilesDestinationDirectory);
            rep.saveStepAttribute(id_transformation, id_step, "error_line_files_ext", errorLineFilesExtension);
             rep.saveStepAttribute(id_transformation, id_step, "line_number_files_dest_dir", lineNumberFilesDestinationDirectory);
            rep.saveStepAttribute(id_transformation, id_step, "line_number_files_ext", lineNumberFilesExtension);
		}
		catch(Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step="+id_step, e);
		}

	}
	
	public final static int getTrimType(String tt)
	{
		if (tt==null) return 0;
		
		for (int i=0;i<type_trim_desc.length;i++)
		{
			if (type_trim_desc[i].equalsIgnoreCase(tt)) return i;
		}
		return 0;
	}

	public final static String getTrimTypeDesc(int i)
	{
		if (i<0 || i>=type_trim_desc.length) return type_trim_desc[0];
		return type_trim_desc[i];	
	}
	
	public String[] getFilePaths()
    {
    	return FileInputList.createFilePathList(fileName, fileMask, fileRequired);
    }
    
    public FileInputList getFileList()
    {
    	return FileInputList.createFileList(fileName, fileMask, fileRequired);
    }
	


	public void check(ArrayList remarks, StepMeta stepinfo, Row prev, String input[], String output[], Row info)
	{
		CheckResult cr;

		// See if we get input...
		if (input.length>0)
		{		
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "This step is not expecting nor reading any input", stepinfo);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Not receiving any input from other steps.", stepinfo);
			remarks.add(cr);
		}
		
		FileInputList fileList = getFileList();
		if (fileList.nrOfFiles() == 0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "No files can be found to read.", stepinfo);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "This step is reading "+fileList.nrOfFiles()+" files.", stepinfo);
			remarks.add(cr);
		}
	}
	
	public Row getEmptyFields()
	{
		Row row = new Row();
		for (int i=0;i<fieldName.length;i++)
		{
			Value v = new Value(fieldName[i], fieldType[i]);
			row.addValue(v);
		}
		
		return row;
	}
	
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new ExcelInputDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new ExcelInput(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new ExcelInputData();
	}

	public String getBadLineFilesDestinationDirectory() {
		return badLineFilesDestinationDirectory;
	}

	public void setBadLineFilesDestinationDirectory(
			String badLineFilesDestinationDirectory) {
		this.badLineFilesDestinationDirectory = badLineFilesDestinationDirectory;
	}

	public String getBadLineFilesExtension() {
		return badLineFilesExtension;
	}

	public void setBadLineFilesExtension(String badLineFilesExtension) {
		this.badLineFilesExtension = badLineFilesExtension;
	}

	public boolean isErrorIgnored() {
		return errorIgnored;
	}

	public void setErrorIgnored(boolean errorIgnored) {
		this.errorIgnored = errorIgnored;
	}

	public String getErrorLineFilesDestinationDirectory() {
		return errorLineFilesDestinationDirectory;
	}

	public void setErrorLineFilesDestinationDirectory(
			String errorLineFilesDestinationDirectory) {
		this.errorLineFilesDestinationDirectory = errorLineFilesDestinationDirectory;
	}

	public String getErrorLineFilesExtension() {
		return errorLineFilesExtension;
	}

	public void setErrorLineFilesExtension(String errorLineFilesExtension) {
		this.errorLineFilesExtension = errorLineFilesExtension;
	}

	public String getLineNumberFilesDestinationDirectory() {
		return lineNumberFilesDestinationDirectory;
	}

	public void setLineNumberFilesDestinationDirectory(
			String lineNumberFilesDestinationDirectory) {
		this.lineNumberFilesDestinationDirectory = lineNumberFilesDestinationDirectory;
	}

	public String getLineNumberFilesExtension() {
		return lineNumberFilesExtension;
	}

	public void setLineNumberFilesExtension(String lineNumberFilesExtension) {
		this.lineNumberFilesExtension = lineNumberFilesExtension;
	}

	public boolean isErrorLineSkipped() {
		return errorLineSkipped;
	}

	public void setErrorLineSkipped(boolean errorLineSkipped) {
		this.errorLineSkipped = errorLineSkipped;
	}
	
	public boolean isStrictTypes() {
		return strictTypes;
	}

	public void setStrictTypes(boolean strictTypes) {
		this.strictTypes = strictTypes;
	}




	public String[] getFileRequired() {
		return fileRequired;
	}




	public void setFileRequired(String[] fileRequired) {
		this.fileRequired = fileRequired;
	}


}
