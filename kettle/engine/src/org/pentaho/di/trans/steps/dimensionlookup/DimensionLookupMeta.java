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

package org.pentaho.di.trans.steps.dimensionlookup;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.DatabaseImpact;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * @author Matt
 * @since on 14-may-2003
 *
 *        WANTED: Interconnected Dynamic Lookups --> http://www.datawarehouse.com/article/?articleId=5354
 *
 *        The idea is here to create a central 'dimension' cache process, seperated from the other Kettle processes.
 *        Kettle then connects over a socket to this daemon-like process to check wether a certain dimension entry is
 *        present. Perhaps a more general caching service should be considered.
 *
 */

public class DimensionLookupMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = DimensionLookupMeta.class; // for i18n purposes, needed by Translator2!!

  public static final int TYPE_UPDATE_DIM_INSERT = 0;
  public static final int TYPE_UPDATE_DIM_UPDATE = 1;
  public static final int TYPE_UPDATE_DIM_PUNCHTHROUGH = 2;
  public static final int TYPE_UPDATE_DATE_INSUP = 3;
  public static final int TYPE_UPDATE_DATE_INSERTED = 4;
  public static final int TYPE_UPDATE_DATE_UPDATED = 5;
  public static final int TYPE_UPDATE_LAST_VERSION = 6;

  public static final String[] typeDesc = {
    BaseMessages.getString( PKG, "DimensionLookupMeta.TypeDesc.Insert" ),
    BaseMessages.getString( PKG, "DimensionLookupMeta.TypeDesc.Update" ),
    BaseMessages.getString( PKG, "DimensionLookupMeta.TypeDesc.PunchThrough" ),
    BaseMessages.getString( PKG, "DimensionLookupMeta.TypeDesc.DateInsertedOrUpdated" ),
    BaseMessages.getString( PKG, "DimensionLookupMeta.TypeDesc.DateInserted" ),
    BaseMessages.getString( PKG, "DimensionLookupMeta.TypeDesc.DateUpdated" ),
    BaseMessages.getString( PKG, "DimensionLookupMeta.TypeDesc.LastVersion" ), };

  public static final String[] typeCodes = { // for saving to the repository
    "Insert", "Update", "Punch through", "DateInsertedOrUpdated", "DateInserted", "DateUpdated", "LastVersion", };

  public static final String[] typeDescLookup = ValueMeta.getTypes();

  public static final int START_DATE_ALTERNATIVE_NONE = 0;
  public static final int START_DATE_ALTERNATIVE_SYSDATE = 1;
  public static final int START_DATE_ALTERNATIVE_START_OF_TRANS = 2;
  public static final int START_DATE_ALTERNATIVE_NULL = 3;
  public static final int START_DATE_ALTERNATIVE_COLUMN_VALUE = 4;

  private static final String[] startDateAlternativeCodes = { "none",
    "sysdate",
    "trans_start",
    "null",
    "column_value",
  };

  private static final String[] startDateAlternativeDescs = {
    BaseMessages.getString( PKG, "DimensionLookupMeta.StartDateAlternative.None.Label" ),
    BaseMessages.getString( PKG, "DimensionLookupMeta.StartDateAlternative.Sysdate.Label" ),
    BaseMessages.getString( PKG, "DimensionLookupMeta.StartDateAlternative.TransStart.Label" ),
    BaseMessages.getString( PKG, "DimensionLookupMeta.StartDateAlternative.Null.Label" ),
    BaseMessages.getString( PKG, "DimensionLookupMeta.StartDateAlternative.ColumnValue.Label" ), };

  /** The lookup schema name */
  private String schemaName;

  /** The lookup table */
  private String tableName;

  /** The database connection */
  private DatabaseMeta databaseMeta;

  /** Update the dimension or just lookup? */
  private boolean update;

  /** Fields used to look up a value in the dimension */
  private String[] keyStream;

  /** Fields in the dimension to use for lookup */
  private String[] keyLookup;

  /** The field to use for date range lookup in the dimension */
  private String dateField;

  /** The 'from' field of the date range in the dimension */
  private String dateFrom;

  /** The 'to' field of the date range in the dimension */
  private String dateTo;

  /** Fields containing the values in the input stream to update the dimension with */
  private String[] fieldStream;

  /** Fields in the dimension to update or retrieve */
  private String[] fieldLookup;

  /** The type of update to perform on the fields: insert, update, punch-through */
  private int[] fieldUpdate;

  /** Name of the technical key (surrogate key) field to return from the dimension */
  private String keyField;

  /** New name of the technical key field */
  private String keyRename;

  /** Use auto increment field as TK */
  private boolean autoIncrement;

  /** The name of the version field */
  private String versionField;

  /** Sequence name to get the sequence from */
  private String sequenceName;

  /** The number of rows between commits */
  private int commitSize;

  /** Flag to indicate the use of batch updates, default disabled for backward compatibility */
  private boolean useBatchUpdate;

  /** The year to use as minus infinity in the dimensions date range */
  private int minYear;

  /** The year to use as plus infinity in the dimensions date range */
  private int maxYear;

  /** Which method to use for the creation of the tech key */
  private String techKeyCreation = null;

  public static String CREATION_METHOD_AUTOINC = "autoinc";
  public static String CREATION_METHOD_SEQUENCE = "sequence";
  public static String CREATION_METHOD_TABLEMAX = "tablemax";

  /** The size of the cache in ROWS : -1 means: not set, 0 means: cache all */
  private int cacheSize;

  /** Flag to indicate we're going to use an alternative start date */
  private boolean usingStartDateAlternative;

  /** The type of alternative */
  private int startDateAlternative;

  /** The field name in case we select the column value option as an alternative start date */
  private String startDateFieldName;

  private boolean preloadingCache;

  public DimensionLookupMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the tablename.
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * @param tablename
   *          The tablename to set.
   */
  public void setTableName( String tablename ) {
    this.tableName = tablename;
  }

  /**
   * @return Returns the database.
   */
  public DatabaseMeta getDatabaseMeta() {
    return databaseMeta;
  }

  /**
   * @param database
   *          The database to set.
   */
  public void setDatabaseMeta( DatabaseMeta database ) {
    this.databaseMeta = database;
  }

  /**
   * @return Returns the update.
   */
  public boolean isUpdate() {
    return update;
  }

  /**
   * @param update
   *          The update to set.
   */
  public void setUpdate( boolean update ) {
    this.update = update;
  }

  /**
   * @return Returns the autoIncrement.
   */
  public boolean isAutoIncrement() {
    return autoIncrement;
  }

  /**
   * @param autoIncrement
   *          The autoIncrement to set.
   */
  public void setAutoIncrement( boolean autoIncrement ) {
    this.autoIncrement = autoIncrement;
  }

  /**
   * Set the way how the technical key field should be created.
   *
   * @param techKeyCreation
   *          which method to use for the creation of the technical key.
   */
  public void setTechKeyCreation( String techKeyCreation ) {
    this.techKeyCreation = techKeyCreation;
  }

  /**
   * Get the way how the technical key field should be created.
   *
   * @return creation way for the technical key.
   */
  public String getTechKeyCreation() {
    return this.techKeyCreation;
  }

  /**
   * @return Returns the commitSize.
   */
  public int getCommitSize() {
    return commitSize;
  }

  /**
   * @param commitSize
   *          The commitSize to set.
   */
  public void setCommitSize( int commitSize ) {
    this.commitSize = commitSize;
  }

  /**
   * @return Returns the dateField.
   */
  public String getDateField() {
    return dateField;
  }

  /**
   * @param dateField
   *          The dateField to set.
   */
  public void setDateField( String dateField ) {
    this.dateField = dateField;
  }

  /**
   * @return Returns the dateFrom.
   */
  public String getDateFrom() {
    return dateFrom;
  }

  /**
   * @param dateFrom
   *          The dateFrom to set.
   */
  public void setDateFrom( String dateFrom ) {
    this.dateFrom = dateFrom;
  }

  /**
   * @return Returns the dateTo.
   */
  public String getDateTo() {
    return dateTo;
  }

  /**
   * @param dateTo
   *          The dateTo to set.
   */
  public void setDateTo( String dateTo ) {
    this.dateTo = dateTo;
  }

  /**
   * @return Fields in the dimension to update or retrieve.
   */
  public String[] getFieldLookup() {
    return fieldLookup;
  }

  /**
   * @param fieldLookup
   *          sets the fields in the dimension to update or retrieve.
   */
  public void setFieldLookup( String[] fieldLookup ) {
    this.fieldLookup = fieldLookup;
  }

  /**
   * @return Fields containing the values in the input stream to update the dimension with.
   */
  public String[] getFieldStream() {
    return fieldStream;
  }

  /**
   * @param fieldStream
   *          The fields containing the values in the input stream to update the dimension with.
   */
  public void setFieldStream( String[] fieldStream ) {
    this.fieldStream = fieldStream;
  }

  /**
   * @return Returns the fieldUpdate.
   */
  public int[] getFieldUpdate() {
    return fieldUpdate;
  }

  /**
   * @param fieldUpdate
   *          The fieldUpdate to set.
   */
  public void setFieldUpdate( int[] fieldUpdate ) {
    this.fieldUpdate = fieldUpdate;
  }

  /**
   * @return Returns the keyField.
   */
  public String getKeyField() {
    return keyField;
  }

  /**
   * @param keyField
   *          The keyField to set.
   */
  public void setKeyField( String keyField ) {
    this.keyField = keyField;
  }

  /**
   * @return Returns the keyLookup.
   */
  public String[] getKeyLookup() {
    return keyLookup;
  }

  /**
   * @param keyLookup
   *          The keyLookup to set.
   */
  public void setKeyLookup( String[] keyLookup ) {
    this.keyLookup = keyLookup;
  }

  /**
   * @return Returns the keyRename.
   */
  public String getKeyRename() {
    return keyRename;
  }

  /**
   * @param keyRename
   *          The keyRename to set.
   */
  public void setKeyRename( String keyRename ) {
    this.keyRename = keyRename;
  }

  /**
   * @return Returns the keyStream.
   */
  public String[] getKeyStream() {
    return keyStream;
  }

  /**
   * @param keyStream
   *          The keyStream to set.
   */
  public void setKeyStream( String[] keyStream ) {
    this.keyStream = keyStream;
  }

  /**
   * @return Returns the maxYear.
   */
  public int getMaxYear() {
    return maxYear;
  }

  /**
   * @param maxYear
   *          The maxYear to set.
   */
  public void setMaxYear( int maxYear ) {
    this.maxYear = maxYear;
  }

  /**
   * @return Returns the minYear.
   */
  public int getMinYear() {
    return minYear;
  }

  /**
   * @param minYear
   *          The minYear to set.
   */
  public void setMinYear( int minYear ) {
    this.minYear = minYear;
  }

  /**
   * @return Returns the sequenceName.
   */
  public String getSequenceName() {
    return sequenceName;
  }

  /**
   * @param sequenceName
   *          The sequenceName to set.
   */
  public void setSequenceName( String sequenceName ) {
    this.sequenceName = sequenceName;
  }

  /**
   * @return Returns the versionField.
   */
  public String getVersionField() {
    return versionField;
  }

  /**
   * @param versionField
   *          The versionField to set.
   */
  public void setVersionField( String versionField ) {
    this.versionField = versionField;
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore )
    throws KettleXMLException {
    readData( stepnode, databases );
  }

  public void allocate( int nrkeys, int nrfields ) {
    keyStream = new String[nrkeys];
    keyLookup = new String[nrkeys];

    fieldStream = new String[nrfields];
    fieldLookup = new String[nrfields];
    fieldUpdate = new int[nrfields];
  }

  public Object clone() {
    DimensionLookupMeta retval = (DimensionLookupMeta) super.clone();

    int nrkeys = keyStream.length;
    int nrfields = fieldStream.length;

    retval.allocate( nrkeys, nrfields );

    for ( int i = 0; i < nrkeys; i++ ) {
      retval.keyStream[i] = keyStream[i];
      retval.keyLookup[i] = keyLookup[i];
    }

    for ( int i = 0; i < nrfields; i++ ) {
      retval.fieldStream[i] = fieldStream[i];
      retval.fieldLookup[i] = fieldLookup[i];
      retval.fieldUpdate[i] = fieldUpdate[i];
    }

    return retval;
  }

  public static final int getUpdateType( boolean upd, String ty ) {
    if ( upd ) {
      for ( int i = 0; i < typeCodes.length; i++ ) {
        if ( typeCodes[i].equalsIgnoreCase( ty ) ) {
          return i;
        }
      }
      // for compatibility:
      for ( int i = 0; i < typeDesc.length; i++ ) {
        if ( typeDesc[i].equalsIgnoreCase( ty ) ) {
          return i;
        }
      }
      if ( "Y".equalsIgnoreCase( ty ) ) {
        return TYPE_UPDATE_DIM_PUNCHTHROUGH;
      }

      return TYPE_UPDATE_DIM_INSERT; // INSERT is the default: don't lose information.
    } else {
      int retval = ValueMeta.getType( ty );
      if ( retval == ValueMetaInterface.TYPE_NONE ) {
        retval = ValueMetaInterface.TYPE_STRING;
      }
      return retval;
    }
  }

  public static final String getUpdateType( boolean upd, int t ) {
    if ( !upd ) {
      return ValueMeta.getTypeDesc( t );
    } else {
      return typeDesc[t];
    }
  }

  public static final String getUpdateTypeCode( boolean upd, int t ) {
    if ( !upd ) {
      return ValueMeta.getTypeDesc( t );
    } else {
      return typeCodes[t];
    }
  }

  public static final int getStartDateAlternative( String string ) {
    for ( int i = 0; i < startDateAlternativeCodes.length; i++ ) {
      if ( startDateAlternativeCodes[i].equalsIgnoreCase( string ) ) {
        return i;
      }
    }
    for ( int i = 0; i < startDateAlternativeDescs.length; i++ ) {
      if ( startDateAlternativeDescs[i].equalsIgnoreCase( string ) ) {
        return i;
      }
    }
    return START_DATE_ALTERNATIVE_NONE;
  }

  public static final String getStartDateAlternativeCode( int alternative ) {
    return startDateAlternativeCodes[alternative];
  }

  public static final String getStartDateAlternativeDesc( int alternative ) {
    return startDateAlternativeDescs[alternative];
  }

  public static final String[] getStartDateAlternativeCodes() {
    return startDateAlternativeCodes;
  }

  public static final String[] getStartDateAlternativeDescriptions() {
    return startDateAlternativeDescs;
  }

  public static final boolean isUpdateTypeWithoutArgument( boolean update, int type ) {
    if ( !update ) {
      return false; // doesn't apply
    }

    switch ( type ) {
      case TYPE_UPDATE_DATE_INSUP:
      case TYPE_UPDATE_DATE_INSERTED:
      case TYPE_UPDATE_DATE_UPDATED:
      case TYPE_UPDATE_LAST_VERSION:
        return true;
      default:
        return false;
    }
  }

  public void setDefault() {
    int nrkeys, nrfields;

    schemaName = "";
    tableName = BaseMessages.getString( PKG, "DimensionLookupMeta.DefualtTableName" );
    databaseMeta = null;
    commitSize = 100;
    update = true;

    nrkeys = 0;
    nrfields = 0;

    allocate( nrkeys, nrfields );

    // Read keys to dimension
    for ( int i = 0; i < nrkeys; i++ ) {
      keyStream[i] = "key" + i;
      keyLookup[i] = "keylookup" + i;
    }

    for ( int i = 0; i < nrfields; i++ ) {
      fieldStream[i] = "field" + i;
      fieldLookup[i] = "lookup" + i;
      fieldUpdate[i] = DimensionLookupMeta.TYPE_UPDATE_DIM_INSERT;
    }

    // Only one date is supported
    // No datefield: use system date...
    dateField = "";
    dateFrom = "date_from";
    dateTo = "date_to";

    minYear = Const.MIN_YEAR;
    maxYear = Const.MAX_YEAR;

    keyField = "";
    keyRename = "";
    autoIncrement = false;
    versionField = "version";

    cacheSize = 5000;
    preloadingCache = false;
  }

  public void getFields( RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {

    // Change all the fields to normal storage, this is the fastest way to handle lazy conversion.
    // It doesn't make sense to use it in the SCD context but people try it anyway
    //
    for ( ValueMetaInterface valueMeta : row.getValueMetaList() ) {
      valueMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_NORMAL );

      // Also change the trim type to "None" as this can cause trouble
      // during compare of the data when there are leading/trailing spaces in the target table
      //
      valueMeta.setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );
    }

    // technical key can't be null
    if ( Const.isEmpty( keyField ) ) {
      String message =
        BaseMessages.getString( PKG, "DimensionLookupMeta.Error.NoTechnicalKeySpecified" );
      logError( message );
      throw new KettleStepException( message );
    }

    ValueMetaInterface v = new ValueMeta( keyField, ValueMetaInterface.TYPE_INTEGER );
    if ( keyRename != null && keyRename.length() > 0 ) {
      v.setName( keyRename );
    }

    v.setLength( 9 );
    v.setPrecision( 0 );
    v.setOrigin( name );
    row.addValueMeta( v );

    // retrieve extra fields on lookup?
    // Don't bother if there are no return values specified.
    if ( !update && fieldLookup.length > 0 ) {
      Database db = null;
      try {
        // Get the rows from the table...
        if ( databaseMeta != null ) {
          db = new Database( loggingObject, databaseMeta );

          RowMetaInterface extraFields = getDatabaseTableFields( db, schemaName, tableName );

          for ( int i = 0; i < fieldLookup.length; i++ ) {
            v = extraFields.searchValueMeta( fieldLookup[i] );
            if ( v == null ) {
              String message =
                BaseMessages.getString(
                  PKG, "DimensionLookupMeta.Exception.UnableToFindReturnField", fieldLookup[i] );
              logError( message );
              throw new KettleStepException( message );
            }

            // If the field needs to be renamed, rename
            if ( fieldStream[i] != null && fieldStream[i].length() > 0 ) {
              v.setName( fieldStream[i] );
            }
            v.setOrigin( name );
            row.addValueMeta( v );
          }
        } else {
          String message =
            BaseMessages.getString( PKG, "DimensionLookupMeta.Exception.UnableToRetrieveDataTypeOfReturnField" );
          logError( message );
          throw new KettleStepException( message );
        }
      } catch ( Exception e ) {
        String message =
          BaseMessages.getString( PKG, "DimensionLookupMeta.Exception.UnableToRetrieveDataTypeOfReturnField2" );
        logError( message );
        throw new KettleStepException( message, e );
      } finally {
        if ( db != null ) {
          db.disconnect();
        }
      }
    }
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer( 512 );

    retval.append( "      " ).append( XMLHandler.addTagValue( "schema", schemaName ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "table", tableName ) );
    retval
      .append( "      " ).append(
        XMLHandler.addTagValue( "connection", databaseMeta == null ? "" : databaseMeta.getName() ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "commit", commitSize ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "update", update ) );

    retval.append( "      <fields>" ).append( Const.CR );
    for ( int i = 0; i < keyStream.length; i++ ) {
      retval.append( "        <key>" ).append( Const.CR );
      retval.append( "          " ).append( XMLHandler.addTagValue( "name", keyStream[i] ) );
      retval.append( "          " ).append( XMLHandler.addTagValue( "lookup", keyLookup[i] ) );
      retval.append( "        </key>" ).append( Const.CR );
    }

    retval.append( "        <date>" ).append( Const.CR );
    retval.append( "          " ).append( XMLHandler.addTagValue( "name", dateField ) );
    retval.append( "          " ).append( XMLHandler.addTagValue( "from", dateFrom ) );
    retval.append( "          " ).append( XMLHandler.addTagValue( "to", dateTo ) );
    retval.append( "        </date>" ).append( Const.CR );

    if ( fieldStream != null ) {
      for ( int i = 0; i < fieldStream.length; i++ ) {
        retval.append( "        <field>" ).append( Const.CR );
        retval.append( "          " ).append( XMLHandler.addTagValue( "name", Const.NVL( fieldStream[i], "" ) ) );
        retval.append( "          " ).append( XMLHandler.addTagValue( "lookup", Const.NVL( fieldLookup[i], "" ) ) );
        retval.append( "          " ).append(
          XMLHandler.addTagValue( "update", getUpdateTypeCode( update, fieldUpdate[i] ) ) );
        retval.append( "        </field>" ).append( Const.CR );
      }
    }

    retval.append( "        <return>" ).append( Const.CR );
    retval.append( "          " ).append( XMLHandler.addTagValue( "name", keyField ) );
    retval.append( "          " ).append( XMLHandler.addTagValue( "rename", keyRename ) );
    retval.append( "          " ).append( XMLHandler.addTagValue( "creation_method", techKeyCreation ) );
    retval.append( "          " ).append( XMLHandler.addTagValue( "use_autoinc", autoIncrement ) );
    retval.append( "          " ).append( XMLHandler.addTagValue( "version", versionField ) );
    retval.append( "        </return>" ).append( Const.CR );

    retval.append( "      </fields>" ).append( Const.CR );

    // If sequence is empty: use auto-increment field!
    retval.append( "      " ).append( XMLHandler.addTagValue( "sequence", sequenceName ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "min_year", minYear ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "max_year", maxYear ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "cache_size", cacheSize ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "preload_cache", preloadingCache ) );

    retval.append( "      " ).append(
      XMLHandler.addTagValue( "use_start_date_alternative", usingStartDateAlternative ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "start_date_alternative", getStartDateAlternativeCode( startDateAlternative ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "start_date_field_name", startDateFieldName ) );

    return retval.toString();
  }

  private void readData( Node stepnode, List<? extends SharedObjectInterface> databases )
    throws KettleXMLException {
    try {
      String upd;
      int nrkeys, nrfields;
      String commit;

      schemaName = XMLHandler.getTagValue( stepnode, "schema" );
      tableName = XMLHandler.getTagValue( stepnode, "table" );
      String con = XMLHandler.getTagValue( stepnode, "connection" );
      databaseMeta = DatabaseMeta.findDatabase( databases, con );
      commit = XMLHandler.getTagValue( stepnode, "commit" );
      commitSize = Const.toInt( commit, 0 );

      upd = XMLHandler.getTagValue( stepnode, "update" );
      if ( upd.equalsIgnoreCase( "Y" ) ) {
        update = true;
      } else {
        update = false;
      }

      Node fields = XMLHandler.getSubNode( stepnode, "fields" );

      nrkeys = XMLHandler.countNodes( fields, "key" );
      nrfields = XMLHandler.countNodes( fields, "field" );

      allocate( nrkeys, nrfields );

      // Read keys to dimension
      for ( int i = 0; i < nrkeys; i++ ) {
        Node knode = XMLHandler.getSubNodeByNr( fields, "key", i );

        keyStream[i] = XMLHandler.getTagValue( knode, "name" );
        keyLookup[i] = XMLHandler.getTagValue( knode, "lookup" );
      }

      // Only one date is supported
      // No datefield: use system date...
      Node dnode = XMLHandler.getSubNode( fields, "date" );
      dateField = XMLHandler.getTagValue( dnode, "name" );
      dateFrom = XMLHandler.getTagValue( dnode, "from" );
      dateTo = XMLHandler.getTagValue( dnode, "to" );

      for ( int i = 0; i < nrfields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );

        fieldStream[i] = XMLHandler.getTagValue( fnode, "name" );
        fieldLookup[i] = XMLHandler.getTagValue( fnode, "lookup" );
        upd = XMLHandler.getTagValue( fnode, "update" );
        fieldUpdate[i] = getUpdateType( update, upd );
      }

      if ( update ) {
        // If this is empty: use auto-increment field!
        sequenceName = XMLHandler.getTagValue( stepnode, "sequence" );
      }

      maxYear = Const.toInt( XMLHandler.getTagValue( stepnode, "max_year" ), Const.MAX_YEAR );
      minYear = Const.toInt( XMLHandler.getTagValue( stepnode, "min_year" ), Const.MIN_YEAR );

      keyField = XMLHandler.getTagValue( fields, "return", "name" );
      keyRename = XMLHandler.getTagValue( fields, "return", "rename" );
      autoIncrement = !"N".equalsIgnoreCase( XMLHandler.getTagValue( fields, "return", "use_autoinc" ) );
      versionField = XMLHandler.getTagValue( fields, "return", "version" );

      setTechKeyCreation( XMLHandler.getTagValue( fields, "return", "creation_method" ) );

      cacheSize = Const.toInt( XMLHandler.getTagValue( stepnode, "cache_size" ), -1 );
      preloadingCache = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "preload_cache" ) );

      usingStartDateAlternative =
        "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "use_start_date_alternative" ) );
      startDateAlternative =
        getStartDateAlternative( XMLHandler.getTagValue( stepnode, "start_date_alternative" ) );
      startDateFieldName = XMLHandler.getTagValue( stepnode, "start_date_field_name" );
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "DimensionLookupMeta.Exception.UnableToLoadStepInfoFromXML" ), e );
    }
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
    throws KettleException {
    try {
      databaseMeta = rep.loadDatabaseMetaFromStepAttribute( id_step, "id_connection", databases );

      schemaName = rep.getStepAttributeString( id_step, "schema" );
      tableName = rep.getStepAttributeString( id_step, "table" );
      commitSize = (int) rep.getStepAttributeInteger( id_step, "commit" );
      update = rep.getStepAttributeBoolean( id_step, "update" );

      int nrkeys = rep.countNrStepAttributes( id_step, "lookup_key_name" );
      int nrfields = rep.countNrStepAttributes( id_step, "field_update" );

      allocate( nrkeys, nrfields );

      for ( int i = 0; i < nrkeys; i++ ) {
        keyStream[i] = rep.getStepAttributeString( id_step, i, "lookup_key_name" );
        keyLookup[i] = rep.getStepAttributeString( id_step, i, "lookup_key_field" );
      }

      dateField = rep.getStepAttributeString( id_step, "date_name" );
      dateFrom = rep.getStepAttributeString( id_step, "date_from" );
      dateTo = rep.getStepAttributeString( id_step, "date_to" );

      for ( int i = 0; i < nrfields; i++ ) {
        fieldStream[i] = rep.getStepAttributeString( id_step, i, "field_name" );
        fieldLookup[i] = rep.getStepAttributeString( id_step, i, "field_lookup" );
        fieldUpdate[i] = getUpdateType( update, rep.getStepAttributeString( id_step, i, "field_update" ) );
      }

      keyField = rep.getStepAttributeString( id_step, "return_name" );
      keyRename = rep.getStepAttributeString( id_step, "return_rename" );
      autoIncrement = rep.getStepAttributeBoolean( id_step, "use_autoinc" );
      versionField = rep.getStepAttributeString( id_step, "version_field" );
      techKeyCreation = rep.getStepAttributeString( id_step, "creation_method" );

      sequenceName = rep.getStepAttributeString( id_step, "sequence" );
      minYear = (int) rep.getStepAttributeInteger( id_step, "min_year" );
      maxYear = (int) rep.getStepAttributeInteger( id_step, "max_year" );

      cacheSize = (int) rep.getStepAttributeInteger( id_step, "cache_size" );
      preloadingCache = rep.getStepAttributeBoolean( id_step, "preload_cache" );

      usingStartDateAlternative = rep.getStepAttributeBoolean( id_step, "use_start_date_alternative" );
      startDateAlternative =
        getStartDateAlternative( rep.getStepAttributeString( id_step, "start_date_alternative" ) );
      startDateFieldName = rep.getStepAttributeString( id_step, "start_date_field_name" );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "DimensionLookupMeta.Exception.UnexpectedErrorReadingStepInfoFromRepository" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
    throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "schema", schemaName );
      rep.saveStepAttribute( id_transformation, id_step, "table", tableName );
      rep.saveDatabaseMetaStepAttribute( id_transformation, id_step, "id_connection", databaseMeta );
      rep.saveStepAttribute( id_transformation, id_step, "commit", commitSize );
      rep.saveStepAttribute( id_transformation, id_step, "update", update );

      for ( int i = 0; i < keyStream.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "lookup_key_name", keyStream[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "lookup_key_field", keyLookup[i] );
      }

      rep.saveStepAttribute( id_transformation, id_step, "date_name", dateField );
      rep.saveStepAttribute( id_transformation, id_step, "date_from", dateFrom );
      rep.saveStepAttribute( id_transformation, id_step, "date_to", dateTo );

      if ( fieldStream != null ) {
        for ( int i = 0; i < fieldStream.length; i++ ) {
          rep.saveStepAttribute( id_transformation, id_step, i, "field_name", fieldStream[i] );
          rep.saveStepAttribute( id_transformation, id_step, i, "field_lookup", fieldLookup[i] );
          rep.saveStepAttribute( id_transformation, id_step, i, "field_update", getUpdateTypeCode(
            update, fieldUpdate[i] ) );
        }
      }

      rep.saveStepAttribute( id_transformation, id_step, "return_name", keyField );
      rep.saveStepAttribute( id_transformation, id_step, "return_rename", keyRename );
      rep.saveStepAttribute( id_transformation, id_step, "creation_method", techKeyCreation );

      // For the moment still save 'use_autoinc' for backwards compatibility
      // (Sven Boden).
      rep.saveStepAttribute( id_transformation, id_step, "use_autoinc", autoIncrement );
      rep.saveStepAttribute( id_transformation, id_step, "version_field", versionField );

      rep.saveStepAttribute( id_transformation, id_step, "sequence", sequenceName );
      rep.saveStepAttribute( id_transformation, id_step, "min_year", minYear );
      rep.saveStepAttribute( id_transformation, id_step, "max_year", maxYear );

      rep.saveStepAttribute( id_transformation, id_step, "cache_size", cacheSize );
      rep.saveStepAttribute( id_transformation, id_step, "preload_cache", preloadingCache );

      rep.saveStepAttribute( id_transformation, id_step, "use_start_date_alternative", usingStartDateAlternative );
      rep
        .saveStepAttribute(
          id_transformation, id_step, "start_date_alternative",
          getStartDateAlternativeCode( startDateAlternative ) );
      rep.saveStepAttribute( id_transformation, id_step, "start_date_field_name", startDateFieldName );

      // Also, save the step-database relationship!
      if ( databaseMeta != null ) {
        rep.insertStepDatabase( id_transformation, id_step, databaseMeta.getObjectId() );
      }

    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "DimensionLookupMeta.Exception.UnableToLoadDimensionLookupInfoFromRepository" ), dbe );
    }
  }

  public Date getMinDate() {
    Calendar mincal = Calendar.getInstance();
    mincal.set( Calendar.YEAR, minYear );
    mincal.set( Calendar.MONTH, 0 );
    mincal.set( Calendar.DAY_OF_MONTH, 1 );
    mincal.set( Calendar.HOUR_OF_DAY, 0 );
    mincal.set( Calendar.MINUTE, 0 );
    mincal.set( Calendar.SECOND, 0 );
    mincal.set( Calendar.MILLISECOND, 0 );

    return mincal.getTime();
  }

  public Date getMaxDate() {
    Calendar mincal = Calendar.getInstance();
    mincal.set( Calendar.YEAR, maxYear );
    mincal.set( Calendar.MONTH, 11 );
    mincal.set( Calendar.DAY_OF_MONTH, 31 );
    mincal.set( Calendar.HOUR_OF_DAY, 23 );
    mincal.set( Calendar.MINUTE, 59 );
    mincal.set( Calendar.SECOND, 59 );
    mincal.set( Calendar.MILLISECOND, 999 );

    return mincal.getTime();
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    if ( update ) {
      checkUpdate( remarks, stepMeta, prev );
    } else {
      checkLookup( remarks, stepMeta, prev );
    }

    if ( techKeyCreation != null ) {
      // post 2.2 version
      if ( !( CREATION_METHOD_AUTOINC.equals( techKeyCreation )
        || CREATION_METHOD_SEQUENCE.equals( techKeyCreation ) || CREATION_METHOD_TABLEMAX
          .equals( techKeyCreation ) ) ) {
        String error_message =
          BaseMessages.getString( PKG, "DimensionLookupMeta.CheckResult.ErrorTechKeyCreation" )
            + ": " + techKeyCreation + "!";
        CheckResult cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      }
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      CheckResult cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "DimensionLookupMeta.CheckResult.StepReceiveInfoOK" ), stepMeta );
      remarks.add( cr );
    } else {
      CheckResult cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "DimensionLookupMeta.CheckResult.NoInputReceiveFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    }
  }

  private void checkUpdate( List<CheckResultInterface> remarks, StepMeta stepinfo, RowMetaInterface prev ) {
    CheckResult cr;
    String error_message = "";

    if ( databaseMeta != null ) {
      Database db = new Database( loggingObject, databaseMeta );
      // TODO SB: Share VariableSpace
      try {
        db.connect();
        if ( !Const.isEmpty( tableName ) ) {
          boolean first = true;
          boolean error_found = false;
          error_message = "";

          String schemaTable = databaseMeta.getQuotedSchemaTableCombination( schemaName, tableName );
          RowMetaInterface r = db.getTableFields( schemaTable );
          if ( r != null ) {
            for ( int i = 0; i < fieldLookup.length; i++ ) {
              String lufield = fieldLookup[i];
              logDebug( BaseMessages.getString( PKG, "DimensionLookupMeta.Log.CheckLookupField" )
                + i + " --> " + lufield + " in lookup table..." );
              ValueMetaInterface v = r.searchValueMeta( lufield );
              if ( v == null ) {
                if ( first ) {
                  first = false;
                  error_message +=
                    BaseMessages.getString(
                      PKG, "DimensionLookupMeta.CheckResult.MissingCompareFieldsInTargetTable" )
                      + Const.CR;
                }
                error_found = true;
                error_message += "\t\t" + lufield + Const.CR;
              }
            }
            if ( error_found ) {
              cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo );
            } else {
              cr =
                new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
                  PKG, "DimensionLookupMeta.CheckResult.AllLookupFieldFound" ), stepinfo );
            }
            remarks.add( cr );

            /* Also, check the fields: tk, version, from-to, ... */
            if ( keyField != null && keyField.length() > 0 ) {
              if ( r.indexOfValue( keyField ) < 0 ) {
                error_message =
                  BaseMessages.getString( PKG, "DimensionLookupMeta.CheckResult.TechnicalKeyNotFound", keyField )
                    + Const.CR;
                cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo );
              } else {
                error_message =
                  BaseMessages.getString( PKG, "DimensionLookupMeta.CheckResult.TechnicalKeyFound", keyField )
                    + Const.CR;
                cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, error_message, stepinfo );
              }
              remarks.add( cr );
            } else {
              error_message =
                BaseMessages.getString( PKG, "DimensionLookupMeta.CheckResult.TechnicalKeyRequired" ) + Const.CR;
              remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo ) );
            }

            if ( versionField != null && versionField.length() > 0 ) {
              if ( r.indexOfValue( versionField ) < 0 ) {
                error_message =
                  BaseMessages.getString(
                    PKG, "DimensionLookupMeta.CheckResult.VersionFieldNotFound", versionField )
                    + Const.CR;
                cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo );
              } else {
                error_message =
                  BaseMessages
                    .getString( PKG, "DimensionLookupMeta.CheckResult.VersionFieldFound", versionField )
                    + Const.CR;
                cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, error_message, stepinfo );
              }
              remarks.add( cr );
            } else {
              error_message =
                BaseMessages.getString( PKG, "DimensionLookupMeta.CheckResult.VersionKeyRequired" ) + Const.CR;
              remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo ) );
            }

            if ( dateFrom != null && dateFrom.length() > 0 ) {
              if ( r.indexOfValue( dateFrom ) < 0 ) {
                error_message =
                  BaseMessages.getString(
                    PKG, "DimensionLookupMeta.CheckResult.StartPointOfDaterangeNotFound", dateFrom )
                    + Const.CR;
                cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo );
              } else {
                error_message =
                  BaseMessages.getString(
                    PKG, "DimensionLookupMeta.CheckResult.StartPointOfDaterangeFound", dateFrom )
                    + Const.CR;
                cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, error_message, stepinfo );
              }
              remarks.add( cr );
            } else {
              error_message =
                BaseMessages.getString( PKG, "DimensionLookupMeta.CheckResult.StartKeyRequired" ) + Const.CR;
              remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo ) );
            }

            if ( dateTo != null && dateTo.length() > 0 ) {
              if ( r.indexOfValue( dateTo ) < 0 ) {
                error_message =
                  BaseMessages.getString(
                    PKG, "DimensionLookupMeta.CheckResult.EndPointOfDaterangeNotFound", dateTo )
                    + Const.CR;
                cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo );
              } else {
                error_message =
                  BaseMessages.getString(
                    PKG, "DimensionLookupMeta.CheckResult.EndPointOfDaterangeFound", dateTo )
                    + Const.CR;
                cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, error_message, stepinfo );
              }
              remarks.add( cr );
            } else {
              error_message =
                BaseMessages.getString( PKG, "DimensionLookupMeta.CheckResult.EndKeyRequired" ) + Const.CR;
              remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo ) );
            }
          } else {
            error_message = BaseMessages.getString( PKG, "DimensionLookupMeta.CheckResult.CouldNotReadTableInfo" );
            cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo );
            remarks.add( cr );
          }
        }

        // Look up fields in the input stream <prev>
        if ( prev != null && prev.size() > 0 ) {
          boolean first = true;
          error_message = "";
          boolean error_found = false;

          for ( int i = 0; i < fieldStream.length; i++ ) {
            logDebug( BaseMessages.getString( PKG, "DimensionLookupMeta.Log.CheckField", i
              + " --> " + fieldStream[i] ) );
            ValueMetaInterface v = prev.searchValueMeta( fieldStream[i] );
            if ( v == null ) {
              if ( first ) {
                first = false;
                error_message +=
                  BaseMessages.getString( PKG, "DimensionLookupMeta.CheckResult.MissongFields" ) + Const.CR;
              }
              error_found = true;
              error_message += "\t\t" + fieldStream[i] + Const.CR;
            }
          }
          if ( error_found ) {
            cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo );
          } else {
            cr =
              new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
                PKG, "DimensionLookupMeta.CheckResult.AllFieldsFound" ), stepinfo );
          }
          remarks.add( cr );
        } else {
          error_message =
            BaseMessages.getString( PKG, "DimensionLookupMeta.CheckResult.CouldNotReadFieldsFromPreviousStep" )
              + Const.CR;
          cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo );
          remarks.add( cr );
        }

        // Check sequence
        if ( databaseMeta.supportsSequences()
          && CREATION_METHOD_SEQUENCE.equals( getTechKeyCreation() ) && sequenceName != null
          && sequenceName.length() != 0 ) {
          if ( db.checkSequenceExists( sequenceName ) ) {
            error_message =
              BaseMessages.getString( PKG, "DimensionLookupMeta.CheckResult.SequenceExists", sequenceName );
            cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, error_message, stepinfo );
            remarks.add( cr );
          } else {
            error_message +=
              BaseMessages
                .getString( PKG, "DimensionLookupMeta.CheckResult.SequenceCouldNotFound", sequenceName );
            cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo );
            remarks.add( cr );
          }
        }
      } catch ( KettleException e ) {
        error_message =
          BaseMessages.getString( PKG, "DimensionLookupMeta.CheckResult.CouldNotConectToDB" ) + e.getMessage();
        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo );
        remarks.add( cr );
      }
    } else {
      error_message = BaseMessages.getString( PKG, "DimensionLookupMeta.CheckResult.InvalidConnectionName" );
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo );
      remarks.add( cr );
    }
  }

  private void checkLookup( List<CheckResultInterface> remarks, StepMeta stepinfo, RowMetaInterface prev ) {
    int i;
    boolean error_found = false;
    String error_message = "";
    boolean first;
    CheckResult cr;

    if ( databaseMeta != null ) {
      Database db = new Database( loggingObject, databaseMeta );
      // TODO SB: share variable space
      try {
        db.connect();

        if ( !Const.isEmpty( tableName ) ) {
          String schemaTable = databaseMeta.getQuotedSchemaTableCombination( schemaName, tableName );
          RowMetaInterface tableFields = db.getTableFields( schemaTable );
          if ( tableFields != null ) {
            if ( prev != null && prev.size() > 0 ) {
              // Start at the top, see if the key fields exist:
              first = true;
              boolean warning_found = false;
              for ( i = 0; i < keyStream.length; i++ ) {
                // Does the field exist in the input stream?
                String strfield = keyStream[i];
                ValueMetaInterface strvalue = prev.searchValueMeta( strfield ); //
                if ( strvalue == null ) {
                  if ( first ) {
                    first = false;
                    error_message +=
                      BaseMessages.getString( PKG, "DimensionLookupMeta.CheckResult.KeyhasProblem" ) + Const.CR;
                  }
                  error_found = true;
                  error_message +=
                    "\t\t"
                      + keyField
                      + BaseMessages.getString( PKG, "DimensionLookupMeta.CheckResult.KeyNotPresentInStream" )
                      + Const.CR;
                } else {
                  // does the field exist in the dimension table?
                  String dimfield = keyLookup[i];
                  ValueMetaInterface dimvalue = tableFields.searchValueMeta( dimfield );
                  if ( dimvalue == null ) {
                    if ( first ) {
                      first = false;
                      error_message +=
                        BaseMessages.getString( PKG, "DimensionLookupMeta.CheckResult.KeyhasProblem2" )
                          + Const.CR;
                    }
                    error_found = true;
                    error_message +=
                      "\t\t"
                        + dimfield
                        + BaseMessages.getString(
                          PKG, "DimensionLookupMeta.CheckResult.KeyNotPresentInDimensiontable" )
                        + schemaTable + ")" + Const.CR;
                  } else {
                    // Is the streamvalue of the same type as the dimension value?
                    if ( strvalue.getType() != dimvalue.getType() ) {
                      if ( first ) {
                        first = false;
                        error_message +=
                          BaseMessages.getString( PKG, "DimensionLookupMeta.CheckResult.KeyhasProblem3" )
                            + Const.CR;
                      }
                      warning_found = true;
                      error_message +=
                        "\t\t"
                          + strfield
                          + " ("
                          + strvalue.getOrigin()
                          + BaseMessages
                            .getString( PKG, "DimensionLookupMeta.CheckResult.KeyNotTheSameTypeAs" )
                          + dimfield + " (" + schemaTable + ")" + Const.CR;
                      error_message +=
                        BaseMessages
                          .getString( PKG, "DimensionLookupMeta.CheckResult.WarningInfoInDBConversion" );
                    }
                  }
                }
              }
              if ( error_found ) {
                cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo );
              } else if ( warning_found ) {
                cr = new CheckResult( CheckResultInterface.TYPE_RESULT_WARNING, error_message, stepinfo );
              } else {
                cr =
                  new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
                    PKG, "DimensionLookupMeta.CheckResult.AllKeysFieldsFound" ), stepinfo );
              }
              remarks.add( cr );

              // In case of lookup, the first column of the UpIns dialog table contains the table field
              error_found = false;
              for ( i = 0; i < fieldLookup.length; i++ ) {
                String lufield = fieldLookup[i];
                if ( lufield != null && lufield.length() > 0 ) {
                  // Checking compare field: lufield
                  ValueMetaInterface v = tableFields.searchValueMeta( lufield );
                  if ( v == null ) {
                    if ( first ) {
                      first = false;
                      error_message +=
                        BaseMessages.getString(
                          PKG, "DimensionLookupMeta.CheckResult.FieldsToRetrieveNotExistInDimension" )
                          + Const.CR;
                    }
                    error_found = true;
                    error_message += "\t\t" + lufield + Const.CR;
                  }
                }
              }
              if ( error_found ) {
                cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo );
              } else {
                cr =
                  new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
                    PKG, "DimensionLookupMeta.CheckResult.AllFieldsToRetrieveFound" ), stepinfo );
              }
              remarks.add( cr );

              /* Also, check the fields: tk, version, from-to, ... */
              if ( tableFields.indexOfValue( keyField ) < 0 ) {
                error_message =
                  BaseMessages.getString( PKG, "DimensionLookupMeta.CheckResult.TechnicalKeyNotFound", keyField )
                    + Const.CR;
                cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo );
              } else {
                error_message =
                  BaseMessages.getString( PKG, "DimensionLookupMeta.CheckResult.TechnicalKeyFound", keyField )
                    + Const.CR;
                cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, error_message, stepinfo );
              }
              remarks.add( cr );

              if ( tableFields.indexOfValue( versionField ) < 0 ) {
                error_message =
                  BaseMessages.getString(
                    PKG, "DimensionLookupMeta.CheckResult.VersionFieldNotFound", versionField )
                    + Const.CR;
                cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo );
              } else {
                error_message =
                  BaseMessages
                    .getString( PKG, "DimensionLookupMeta.CheckResult.VersionFieldFound", versionField )
                    + Const.CR;
                cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, error_message, stepinfo );
              }
              remarks.add( cr );

              if ( tableFields.indexOfValue( dateFrom ) < 0 ) {
                error_message =
                  BaseMessages.getString(
                    PKG, "DimensionLookupMeta.CheckResult.StartOfDaterangeFieldNotFound", dateFrom )
                    + Const.CR;
                cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo );
              } else {
                error_message =
                  BaseMessages.getString(
                    PKG, "DimensionLookupMeta.CheckResult.StartOfDaterangeFieldFound", dateFrom )
                    + Const.CR;
                cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, error_message, stepinfo );
              }
              remarks.add( cr );

              if ( tableFields.indexOfValue( dateTo ) < 0 ) {
                error_message =
                  BaseMessages.getString(
                    PKG, "DimensionLookupMeta.CheckResult.EndOfDaterangeFieldNotFound", dateTo )
                    + Const.CR;
                cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo );
              } else {
                error_message =
                  BaseMessages.getString(
                    PKG, "DimensionLookupMeta.CheckResult.EndOfDaterangeFieldFound", dateTo );
                cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, error_message, stepinfo );
              }
              remarks.add( cr );
            } else {
              error_message =
                BaseMessages.getString(
                  PKG, "DimensionLookupMeta.CheckResult.CouldNotReadFieldsFromPreviousStep" )
                  + Const.CR;
              cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo );
              remarks.add( cr );
            }
          } else {
            error_message = BaseMessages.getString( PKG, "DimensionLookupMeta.CheckResult.CouldNotReadTableInfo" );
            cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo );
            remarks.add( cr );
          }
        }
      } catch ( KettleException e ) {
        error_message =
          BaseMessages.getString( PKG, "DimensionLookupMeta.CheckResult.CouldNotConnectDB" ) + e.getMessage();
        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo );
        remarks.add( cr );
      }
    } else {
      error_message = BaseMessages.getString( PKG, "DimensionLookupMeta.CheckResult.InvalidConnection" );
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo );
      remarks.add( cr );
    }
  }

  public RowMetaInterface getTableFields() {
    RowMetaInterface fields = null;
    if ( databaseMeta != null ) {
      Database db = new Database( loggingObject, databaseMeta );
      try {
        db.connect();
        fields = db.getTableFields( databaseMeta.getQuotedSchemaTableCombination( schemaName, tableName ) );
      } catch ( KettleDatabaseException dbe ) {
        logError( BaseMessages.getString( PKG, "DimensionLookupMeta.Log.DatabaseErrorOccurred" )
          + dbe.getMessage() );
      } finally {
        db.disconnect();
      }
    }
    return fields;
  }

  public SQLStatement getSQLStatements( TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
    Repository repository, IMetaStore metaStore ) {
    SQLStatement retval = new SQLStatement( stepMeta.getName(), databaseMeta, null ); // default: nothing to do!

    if ( update ) { // Only bother in case of update, not lookup!
      logDebug( BaseMessages.getString( PKG, "DimensionLookupMeta.Log.Update" ) );
      if ( databaseMeta != null ) {
        if ( prev != null && prev.size() > 0 ) {
          String schemaTable = databaseMeta.getQuotedSchemaTableCombination( schemaName, tableName );
          if ( !Const.isEmpty( schemaTable ) ) {
            Database db = new Database( loggingObject, databaseMeta );
            db.shareVariablesWith( transMeta );
            try {
              db.connect();

              String sql = "";

              // How does the table look like?
              //
              RowMetaInterface fields = new RowMeta();

              // First the technical key
              //
              ValueMetaInterface vkeyfield = new ValueMeta( keyField, ValueMetaInterface.TYPE_INTEGER );
              vkeyfield.setLength( 10 );
              fields.addValueMeta( vkeyfield );

              // The the version
              //
              ValueMetaInterface vversion = new ValueMeta( versionField, ValueMetaInterface.TYPE_INTEGER );
              vversion.setLength( 5 );
              fields.addValueMeta( vversion );

              // The date from
              //
              ValueMetaInterface vdatefrom = new ValueMeta( dateFrom, ValueMetaInterface.TYPE_DATE );
              fields.addValueMeta( vdatefrom );

              // The date to
              //
              ValueMetaInterface vdateto = new ValueMeta( dateTo, ValueMetaInterface.TYPE_DATE );
              fields.addValueMeta( vdateto );

              String errors = "";

              // Then the keys
              //
              for ( int i = 0; i < keyLookup.length; i++ ) {
                ValueMetaInterface vprev = prev.searchValueMeta( keyStream[i] );
                if ( vprev != null ) {
                  ValueMetaInterface field = vprev.clone();
                  field.setName( keyLookup[i] );
                  fields.addValueMeta( field );
                } else {
                  if ( errors.length() > 0 ) {
                    errors += ", ";
                  }
                  errors += keyStream[i];
                }
              }

              //
              // Then the fields to update...
              //
              for ( int i = 0; i < fieldLookup.length; i++ ) {
                ValueMetaInterface vprev = prev.searchValueMeta( fieldStream[i] );
                if ( vprev != null ) {
                  ValueMetaInterface field = vprev.clone();
                  field.setName( fieldLookup[i] );
                  fields.addValueMeta( field );
                } else {
                  if ( errors.length() > 0 ) {
                    errors += ", ";
                  }
                  errors += fieldStream[i];
                }
              }

              // Finally, the special update fields...
              //
              for ( int i = 0; i < fieldUpdate.length; i++ ) {
                ValueMetaInterface valueMeta = null;
                switch ( fieldUpdate[i] ) {
                  case TYPE_UPDATE_DATE_INSUP:
                  case TYPE_UPDATE_DATE_INSERTED:
                  case TYPE_UPDATE_DATE_UPDATED:
                    valueMeta = new ValueMeta( fieldLookup[i], ValueMetaInterface.TYPE_DATE );
                    break;
                  case TYPE_UPDATE_LAST_VERSION:
                    valueMeta = new ValueMeta( fieldLookup[i], ValueMetaInterface.TYPE_BOOLEAN );
                    break;
                  default:
                    break;
                }
                if ( valueMeta != null ) {
                  fields.addValueMeta( valueMeta );
                }
              }

              if ( errors.length() > 0 ) {
                retval.setError( BaseMessages
                  .getString( PKG, "DimensionLookupMeta.ReturnValue.UnableToFindFields" )
                  + errors );
              }

              logDebug( BaseMessages.getString( PKG, "DimensionLookupMeta.Log.GetDDLForTable" )
                + schemaTable + "] : " + fields.toStringMeta() );

              sql +=
                db.getDDL( schemaTable, fields, ( sequenceName != null && sequenceName.length() != 0 )
                  ? null : keyField, autoIncrement, null, true );

              logDebug( "sql =" + sql );

              String[] idx_fields = null;

              // Key lookup dimensions...
              if ( !Const.isEmpty( keyLookup ) ) {
                idx_fields = new String[keyLookup.length];
                for ( int i = 0; i < keyLookup.length; i++ ) {
                  idx_fields[i] = keyLookup[i];
                }
              } else {
                retval.setError( BaseMessages.getString(
                  PKG, "DimensionLookupMeta.ReturnValue.NoKeyFieldsSpecified" ) );
              }

              if ( !Const.isEmpty( idx_fields ) && !db.checkIndexExists( schemaName, tableName, idx_fields ) ) {
                String indexname = "idx_" + tableName + "_lookup";
                sql +=
                  db.getCreateIndexStatement(
                    schemaName, tableName, indexname, idx_fields, false, false, false, true );
              }

              // (Bitmap) index on technical key
              idx_fields = new String[] { keyField };
              if ( !Const.isEmpty( keyField ) ) {
                if ( !db.checkIndexExists( schemaName, tableName, idx_fields ) ) {
                  String indexname = "idx_" + tableName + "_tk";
                  sql +=
                    db.getCreateIndexStatement(
                      schemaName, tableName, indexname, idx_fields, true, false, true, true );
                }
              } else {
                retval.setError( BaseMessages.getString(
                  PKG, "DimensionLookupMeta.ReturnValue.TechnicalKeyFieldRequired" ) );
              }

              // The optional Oracle sequence
              if ( CREATION_METHOD_SEQUENCE.equals( getTechKeyCreation() ) && !Const.isEmpty( sequenceName ) ) {
                if ( !db.checkSequenceExists( schemaName, sequenceName ) ) {
                  sql += db.getCreateSequenceStatement( schemaName, sequenceName, 1L, 1L, -1L, true );
                }
              }

              if ( sql.length() == 0 ) {
                retval.setSQL( null );
              } else {
                retval.setSQL( transMeta.environmentSubstitute( sql ) );
              }
            } catch ( KettleDatabaseException dbe ) {
              retval.setError( BaseMessages.getString( PKG, "DimensionLookupMeta.ReturnValue.ErrorOccurred" )
                + dbe.getMessage() );
            } finally {
              db.disconnect();
            }
          } else {
            retval.setError( BaseMessages.getString(
              PKG, "DimensionLookupMeta.ReturnValue.NoTableDefinedOnConnection" ) );
          }
        } else {
          retval.setError( BaseMessages.getString( PKG, "DimensionLookupMeta.ReturnValue.NotReceivingAnyFields" ) );
        }
      } else {
        retval
          .setError( BaseMessages.getString( PKG, "DimensionLookupMeta.ReturnValue.NoConnectionDefiendInStep" ) );
      }
    }

    return retval;
  }

  public void analyseImpact( List<DatabaseImpact> impact, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, Repository repository,
    IMetaStore metaStore ) {
    if ( prev != null ) {
      if ( !update ) {
        // Lookup: we do a lookup on the natural keys + the return fields!
        for ( int i = 0; i < keyLookup.length; i++ ) {
          ValueMetaInterface v = prev.searchValueMeta( keyStream[i] );

          DatabaseImpact ii =
            new DatabaseImpact(
              DatabaseImpact.TYPE_IMPACT_READ, transMeta.getName(), stepMeta.getName(), databaseMeta
                .getDatabaseName(), tableName, keyLookup[i], keyStream[i], v != null ? v.getOrigin() : "?",
              "", v == null ? "" : "Type = " + v.toStringMeta() );
          impact.add( ii );
        }

        // Return fields...
        for ( int i = 0; i < fieldLookup.length; i++ ) {
          ValueMetaInterface v = prev.searchValueMeta( fieldStream[i] );

          DatabaseImpact ii =
            new DatabaseImpact(
              DatabaseImpact.TYPE_IMPACT_READ, transMeta.getName(), stepMeta.getName(), databaseMeta
                .getDatabaseName(), tableName, fieldLookup[i], fieldLookup[i], v == null ? "" : v != null
                ? v.getOrigin() : "?", "", v == null ? "" : "Type = " + v.toStringMeta() );
          impact.add( ii );
        }
      } else {
        // Update: insert/update on all specified fields...
        // Lookup: we do a lookup on the natural keys + the return fields!
        for ( int i = 0; i < keyLookup.length; i++ ) {
          ValueMetaInterface v = prev.searchValueMeta( keyStream[i] );

          DatabaseImpact ii =
            new DatabaseImpact(
              DatabaseImpact.TYPE_IMPACT_READ_WRITE, transMeta.getName(), stepMeta.getName(), databaseMeta
                .getDatabaseName(), tableName, keyLookup[i], keyStream[i], v == null ? "" : v.getOrigin(),
              "", v == null ? "" : "Type = " + v.toStringMeta() );
          impact.add( ii );
        }

        // Return fields...
        for ( int i = 0; i < fieldLookup.length; i++ ) {
          ValueMetaInterface v = prev.searchValueMeta( fieldStream[i] );

          DatabaseImpact ii =
            new DatabaseImpact(
              DatabaseImpact.TYPE_IMPACT_READ_WRITE, transMeta.getName(), stepMeta.getName(), databaseMeta
                .getDatabaseName(), tableName, fieldLookup[i], fieldLookup[i], v == null ? "" : v
                .getOrigin(), "", v == null ? "" : "Type = " + v.toStringMeta() );
          impact.add( ii );
        }
      }
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
    Trans trans ) {
    return new DimensionLookup( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  public StepDataInterface getStepData() {
    return new DimensionLookupData();
  }

  public DatabaseMeta[] getUsedDatabaseConnections() {
    if ( databaseMeta != null ) {
      return new DatabaseMeta[] { databaseMeta };
    } else {
      return super.getUsedDatabaseConnections();
    }
  }

  /**
   * @return the schemaName
   */
  public String getSchemaName() {
    return schemaName;
  }

  /**
   * @param schemaName
   *          the schemaName to set
   */
  public void setSchemaName( String schemaName ) {
    this.schemaName = schemaName;
  }

  /**
   * @return the cacheSize
   */
  public int getCacheSize() {
    return cacheSize;
  }

  /**
   * @param cacheSize
   *          the cacheSize to set
   */
  public void setCacheSize( int cacheSize ) {
    this.cacheSize = cacheSize;
  }

  /**
   * @return the usingStartDateAlternative
   */
  public boolean isUsingStartDateAlternative() {
    return usingStartDateAlternative;
  }

  /**
   * @param usingStartDateAlternative
   *          the usingStartDateAlternative to set
   */
  public void setUsingStartDateAlternative( boolean usingStartDateAlternative ) {
    this.usingStartDateAlternative = usingStartDateAlternative;
  }

  /**
   * @return the startDateAlternative
   */
  public int getStartDateAlternative() {
    return startDateAlternative;
  }

  /**
   * @param startDateAlternative
   *          the startDateAlternative to set
   */
  public void setStartDateAlternative( int startDateAlternative ) {
    this.startDateAlternative = startDateAlternative;
  }

  /**
   * @return the startDateFieldName
   */
  public String getStartDateFieldName() {
    return startDateFieldName;
  }

  /**
   * @param startDateFieldName
   *          the startDateFieldName to set
   */
  public void setStartDateFieldName( String startDateFieldName ) {
    this.startDateFieldName = startDateFieldName;
  }

  /**
   * @return the preloadingCache
   */
  public boolean isPreloadingCache() {
    return preloadingCache;
  }

  /**
   * @param preloadingCache
   *          the preloadingCache to set
   */
  public void setPreloadingCache( boolean preloadingCache ) {
    this.preloadingCache = preloadingCache;
  }

  /**
   * @return the useBatchUpdate
   */
  public boolean useBatchUpdate() {
    return useBatchUpdate;
  }

  /**
   * @param useBatchUpdate
   *          the useBatchUpdate to set
   */
  public void setUseBatchUpdate( boolean useBatchUpdate ) {
    this.useBatchUpdate = useBatchUpdate;
  }

  protected RowMetaInterface getDatabaseTableFields( Database db, String schemaName, String tableName )
    throws KettleDatabaseException {
    // First try without connecting to the database... (can be S L O W)
    String schemaTable = databaseMeta.getQuotedSchemaTableCombination( schemaName, tableName );
    RowMetaInterface extraFields = db.getTableFields( schemaTable );
    if ( extraFields == null ) { // now we need to connect
      db.connect();
      extraFields = db.getTableFields( schemaTable );
    }
    return extraFields;
  }
}
