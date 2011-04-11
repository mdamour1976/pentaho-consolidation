package org.pentaho.di.imp.rules;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.imp.rule.ImportValidationFeedback;
import org.pentaho.di.imp.rule.ImportValidationResultType;
import org.pentaho.di.imp.rule.ImporterRuleInterface;
import org.pentaho.di.trans.HasDatabasesInterface;
import org.w3c.dom.Node;

public class DatabaseConfigurationImportRule extends BaseImportRule implements ImporterRuleInterface {

  private DatabaseMeta databaseMeta;
  
  public DatabaseConfigurationImportRule() {
    super();
    databaseMeta=null; // not configured.
  }

  @Override
  public List<ImportValidationFeedback> verifyRule(Object subject) {
    
    List<ImportValidationFeedback> feedback = new ArrayList<ImportValidationFeedback>();
    
    if (!isEnabled()) {
      return feedback;
    }

    if (databaseMeta==null) {
      feedback.add( new ImportValidationFeedback(this, ImportValidationResultType.ERROR, "This rule contains no database to validate against.") );
      return feedback;
    }

    DatabaseMeta verify = null; 
    
    if (subject instanceof HasDatabasesInterface) {
      HasDatabasesInterface dbs = (HasDatabasesInterface)subject;
      verify = dbs.findDatabase(databaseMeta.getName());
    } else if (subject instanceof DatabaseMeta) {
      // See if this is the database to verify!  If it's not, simply ignore it.
      //
      if (databaseMeta.getName().equals(((DatabaseMeta)subject).getName())) {
        verify=(DatabaseMeta) subject;
      }
    }
    
    if (verify==null) {
      return feedback;
    }
    
    // Verify the database name if it's non-empty
    //
    if (!Const.isEmpty(databaseMeta.getDatabaseName())) {
      if (!databaseMeta.getDatabaseName().equals(verify.getDatabaseName())) {
        feedback.add( new ImportValidationFeedback(this, ImportValidationResultType.ERROR, "The name of the database is not set to the expected value '"+databaseMeta.getDatabaseName()+"'.") );
      }
    }

    // Verify the host name if it's non-empty
    //
    if (!Const.isEmpty(databaseMeta.getHostname())) {
      if (!databaseMeta.getHostname().equals(verify.getHostname())) {
        feedback.add( new ImportValidationFeedback(this, ImportValidationResultType.ERROR, "The host name of the database is not set to the expected value '"+databaseMeta.getHostname()+"'.") );
      }
    }

    // Verify the database port number if it's non-empty
    //
    if (!Const.isEmpty(databaseMeta.getDatabasePortNumberString())) {
      if (!databaseMeta.getDatabasePortNumberString().equals(verify.getDatabasePortNumberString())) {
        feedback.add( new ImportValidationFeedback(this, ImportValidationResultType.ERROR, "The database port of the database is not set to the expected value '"+databaseMeta.getDatabasePortNumberString()+"'.") );
      }
    }

    // Verify the user name if it's non-empty
    //
    if (!Const.isEmpty(databaseMeta.getUsername())) {
      if (!databaseMeta.getUsername().equals(verify.getUsername())) {
        feedback.add( new ImportValidationFeedback(this, ImportValidationResultType.ERROR, "The username of the database is not set to the expected value '"+databaseMeta.getUsername()+"'.") );
      }
    }

    // Verify the password if it's non-empty
    //
    if (!Const.isEmpty(databaseMeta.getPassword())) {
      if (!databaseMeta.getPassword().equals(verify.getPassword())) {
        feedback.add( new ImportValidationFeedback(this, ImportValidationResultType.ERROR, "The password of the database is not set to the expected value.") );
      }
    }
    
    if (feedback.isEmpty()) {
      feedback.add( new ImportValidationFeedback(this, ImportValidationResultType.APPROVAL, "The database connection was found and verified.") );
    }
    
    return feedback;
  }

  @Override
  public String getXML() {
    
    StringBuilder xml = new StringBuilder();
    xml.append(XMLHandler.openTag(XML_TAG));

    xml.append(super.getXML()); // id, enabled
    
    if (databaseMeta!=null) {
      xml.append(databaseMeta.getXML());
    }
    
    xml.append(XMLHandler.closeTag(XML_TAG));
    return xml.toString();
  }

  @Override
  public void loadXML(Node ruleNode) throws KettleException {
    super.loadXML(ruleNode);
    
    Node connectionNode = XMLHandler.getSubNode(ruleNode, DatabaseMeta.XML_TAG);
    if (connectionNode!=null) {
      databaseMeta = new DatabaseMeta(connectionNode);
    }
  }

  /**
   * @return the databaseMeta
   */
  public DatabaseMeta getDatabaseMeta() {
    return databaseMeta;
  }

  /**
   * @param databaseMeta the databaseMeta to set
   */
  public void setDatabaseMeta(DatabaseMeta databaseMeta) {
    this.databaseMeta = databaseMeta;
  }
}
