package org.pentaho.di.trans.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.Condition;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.jdbc.TransDataService;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.sql.SQL;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransAdapter;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.di.trans.step.StepInterface;

public class SqlTransExecutor {
  private TransMeta serviceTransMeta;
  private String serviceStepName;
  private String sqlQuery;

  private TransMeta genTransMeta;
  
  private Trans serviceTrans;
  private Trans genTrans;
  
  private RowMetaInterface serviceFields;
  private List<TransDataService> services;
  private String serviceName;
  private TransDataService service;
  private SQL sql;
  private Repository repository;
  private RowMetaInterface resultStepFields;
  private int rowLimit;
  private Map<String, String> parameters;
  private List<String> parameterNames;
  private String resultStepName;

  /**
   * Create a new SqlTransExecutor without parameters
   * @param sqlQuery
   * @param services
   * @throws KettleException 
   */
  public SqlTransExecutor(String sqlQuery, List<TransDataService> services) throws KettleException {
    this(sqlQuery, services, new HashMap<String, String>(), null, 0);
  }
  
  
  /**
   * @param sqlQuery
   * @param services
   * @param parameters
   * 
   * @throws KettleException 
   */
  public SqlTransExecutor(String sqlQuery, List<TransDataService> services, Map<String, String> parameters) throws KettleException {
    this(sqlQuery, services, parameters, null, 0);
  }
  
  /**
   * @param sqlQuery
   * @param services
   * @param repository
   * @throws KettleException 
   */
  public SqlTransExecutor(String sqlQuery, List<TransDataService> services, Map<String, String> parameters, Repository repository, int rowLimit) throws KettleException {
    this.sqlQuery = sqlQuery;
    this.services = services;
    this.parameters = parameters;
    this.repository = repository;
    this.rowLimit = rowLimit;
    
    prepareExecution();
  }
  
  private void prepareExecution() throws KettleException {
    sql = new SQL(sqlQuery);
    serviceName = sql.getServiceName();
    
    // Dual
    if (Const.isEmpty(serviceName) || "dual".equalsIgnoreCase(serviceName)) {
      service = new TransDataService("dual", null, null, null, null);
      service.setDual(true);
      serviceFields = new RowMeta(); // nothing to report from dual
    } else {
      service = findService(serviceName);
      
      if (service==null) {
        throw new KettleException("Unable to find service with name '"+service+"' and SQL: "+sqlQuery);
      }

      // TODO: allow for repository transformation loading...
      //
      serviceTransMeta = loadTransMeta(repository);
      serviceTransMeta.setName(calculateTransname(sql, true));
      serviceTransMeta.activateParameters();
      
      // The dummy step called "Output" provides the output fields...
      //
      serviceStepName = service.getServiceStepName();
      serviceFields = serviceTransMeta.getStepFields(serviceStepName);
    }
  }

  private TransDataService findService(String name) {
    for (TransDataService s : services) {
      if (s.getName().equalsIgnoreCase(name)) return s;
    }
    return null;
  }


  private void extractConditionParameters(Condition condition, Map<String, String> map) {

    if (condition.isAtomic()) {
      if (condition.getFunction()==Condition.FUNC_TRUE) {
        map.put(condition.getLeftValuename(), condition.getRightExactString());
      }
    } else {
      for (Condition sub : condition.getChildren()) {
        extractConditionParameters(sub, map);
      }
    }
  }


  public void executeQuery(RowListener resultRowListener) throws KettleException {
    // Continue parsing of the SQL, map to fields, extract conditions, parameters, ...
    //
    sql.parse(serviceFields);

    if (!service.isDual()) {
      // Parameters: see which ones are defined in the SQL
      //
      Map<String, String> conditionParameters = new HashMap<String, String>();
      if (sql.getWhereCondition()!=null) {
        extractConditionParameters(sql.getWhereCondition().getCondition(), conditionParameters);
      }
      parameters.putAll(conditionParameters); // overwrite the defaults for this query
      
      for (String name : conditionParameters.keySet()) {
        serviceTransMeta.setParameterValue(name, conditionParameters.get(name));
      }
      serviceTransMeta.activateParameters();
      
      // Prepare the execution of this service transformation
      //
      serviceTrans = new Trans(serviceTransMeta);
      serviceTrans.prepareExecution(null);
    }
    
    parameterNames = new ArrayList<String>();
    for (String parameterName : serviceTransMeta.listParameters()) {
      parameterNames.add(parameterName);
    }
    
    // Generate a transformation
    //
    SqlTransMeta sqlTransMeta = new SqlTransMeta(sql, rowLimit);
    genTransMeta = sqlTransMeta.generateTransMeta();
    resultStepName = sqlTransMeta.getResultStepName();
    
    // Prepare execution of the generated transformation
    //
    genTrans = new Trans(genTransMeta);
    genTrans.prepareExecution(null);

    if (!service.isDual()) {
      // This is where we will inject the rows from the service transformation step
      //
      final RowProducer rowProducer = genTrans.addRowProducer(sqlTransMeta.getInjectorStepName(), 0);
  
      // Now connect the 2 transformations with listeners and injector
      //
      StepInterface serviceStep = serviceTrans.findRunThread(serviceStepName);
      serviceStep.addRowListener(new RowAdapter() { @Override
      public void rowWrittenEvent(RowMetaInterface rowMeta, Object[] row) throws KettleStepException {
        // Simply pass along the row to the other transformation (to the Injector step)
        //
        LogChannelInterface log = serviceTrans.getLogChannel();
        try {
          if (log.isRowLevel()) {
            log.logRowlevel("Passing along row: "+rowMeta.getString(row));
          }
        } catch (KettleValueException e) {
        }
  
        rowProducer.putRow(rowMeta, row);
      } });
    
      // Let the other transformation know when there are no more rows
      //
      serviceTrans.addTransListener(new TransAdapter() {
        @Override
        public void transFinished(Trans trans) throws KettleException {
          rowProducer.finished();
        }
      });
    }
      
    // Give back the eventual result rows...
    //
    StepInterface resultStep = genTrans.findRunThread(sqlTransMeta.getResultStepName());
    resultStep.addRowListener(resultRowListener);    
    
    // Get the result row metadata
    //
    resultStepFields = genTransMeta.getStepFields(sqlTransMeta.getResultStepName());
    
    // Start both transformations
    //
    genTrans.startThreads();
    if (!service.isDual()) {
      serviceTrans.startThreads();   
    }
  }
  
  private TransMeta loadTransMeta(Repository repository) throws KettleException {
    TransMeta transMeta = null;
    
    if (!Const.isEmpty(service.getFileName())) {
      try {
        // OK, load the meta-data from file...
        //
        // Don't set internal variables: they belong to the parent thread!
        //
        transMeta = new TransMeta(service.getFileName(), false); 
        transMeta.getLogChannel().logDetailed("Service transformation was loaded from XML file [" + service.getFileName()+ "]");
      } catch (Exception e) {
        throw new KettleException("Unable to load service transformation for service '"+serviceName+"'", e);
      }
    } else {
      throw new KettleException("Loading from a repository is not supported yet (or the filename is not specified for a service)");
    }
    return transMeta;
  }

  public void waitUntilFinished() {
    if (!service.isDual()) {
      serviceTrans.waitUntilFinished();
    }
    genTrans.waitUntilFinished();
  }

  /**
   * @return the serviceTransMeta
   */
  public TransMeta getServiceTransMeta() {
    return serviceTransMeta;
  }

  /**
   * @return the serviceStepName
   */
  public String getServiceStepName() {
    return serviceStepName;
  }

  /**
   * @return the sqlQuery
   */
  public String getSqlQuery() {
    return sqlQuery;
  }

  /**
   * @return the genTransMeta
   */
  public TransMeta getGenTransMeta() {
    return genTransMeta;
  }

  /**
   * @return the serviceTrans
   */
  public Trans getServiceTrans() {
    return serviceTrans;
  }

  /**
   * @return the genTrans
   */
  public Trans getGenTrans() {
    return genTrans;
  }

  /**
   * @return the serviceFields
   */
  public RowMetaInterface getServiceFields() {
    return serviceFields;
  }

  /**
   * @return the serviceName
   */
  public String getServiceName() {
    return serviceName;
  }

  /**
   * @return the resultStepFields
   */
  public RowMetaInterface getResultStepFields() {
    return resultStepFields;
  }

  /**
   * @param resultStepFields the resultStepFields to set
   */
  public void setResultStepFields(RowMetaInterface resultStepFields) {
    this.resultStepFields = resultStepFields;
  }

  /**
   * @return the rowLimit
   */
  public int getRowLimit() {
    return rowLimit;
  }
  
  /**
   * Calculate the name of the generated transformation based on the SQL
   * @return the generated name;
   */
  public static String calculateTransname(SQL sql, boolean isService) {
    StringBuilder sbsql = new StringBuilder(sql.getServiceName()+" - "+(isService?"Service data":"Execute SQL")+" - "+sql.getSqlString());
    for (int i=sbsql.length()-1;i>=0;i--)
    {
      if (sbsql.charAt(i)=='\n' || sbsql.charAt(i)=='\r') sbsql.setCharAt(i, ' ');
    }
    return sbsql.toString();
  }


  /**
   * @return the services
   */
  public List<TransDataService> getServices() {
    return services;
  }


  /**
   * @param servicesMap the servicesMap to set
   */
  public void setServices(List<TransDataService> services) {
    this.services = services;
  }


  /**
   * @return the sql
   */
  public SQL getSql() {
    return sql;
  }


  /**
   * @return the parameterNames
   */
  public List<String> getParameterNames() {
    return parameterNames;
  }


  /**
   * @return the resultStepName
   */
  public String getResultStepName() {
    return resultStepName;
  }
}
