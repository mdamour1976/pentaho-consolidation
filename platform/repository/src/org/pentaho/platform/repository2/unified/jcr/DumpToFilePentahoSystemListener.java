package org.pentaho.platform.repository2.unified.jcr;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.util.TraversingItemVisitor;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Creates an export of the JCR in various formats.
 * 
 * <p>To use:</p>
 * <ol>
 * <li>Add the following to the end of {@code systemListeners.xml}:
 * <pre>
 * &lt;bean id="dumpToFilePentahoSystemListener" 
 *   class="org.pentaho.platform.repository2.unified.jcr.DumpToFilePentahoSystemListener" /&gt;
 * </pre>
 * </li>
 * <li>Add one of the following system properties on the command line:
 * <pre>
 * -Dpentaho.repository.dumpToFile=/tmp/repodump
 * </pre>
 * or
 * <pre>
 * -Dpentaho.repository.dumpToFile.systemView=/tmp/repodump
 * </pre>
 * or
 * <pre>
 * -Dpentaho.repository.dumpToFile.documentView=/tmp/repodump
 * </pre>
 * </li>
 * </ol>
 * 
 * <p>
 * Uses PentahoSystem instead of Spring injection since its collaborators are not yet instantiated when this class is
 * instantiated.
 * </p>
 * 
 * @author mlowery
 */
public class DumpToFilePentahoSystemListener implements IPentahoSystemListener {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(DumpToFilePentahoSystemListener.class);

  /**
   * Exports the repository using a custom TraversingItemVisitor. (It is human-readable output that is not meant to be 
   * parsed.)
   */
  public static final String PROP_DUMP_TO_FILE = "pentaho.repository.dumpToFile"; //$NON-NLS-1$
  
  /**
   * Exports the repository using System View.
   */
  public static final String PROP_DUMP_TO_FILE_SYSTEM_VIEW = "pentaho.repository.dumpToFile.systemView"; //$NON-NLS-1$
  
  /**
   * Exports the repository using Document View.
   */  
  public static final String PROP_DUMP_TO_FILE_DOCUMENT_VIEW = "pentaho.repository.dumpToFile.documentView"; //$NON-NLS-1$

  private enum Mode { CUSTOM, SYS, DOC };
  
  // ~ Instance fields =================================================================================================

  // ~ Constructors ====================================================================================================

  // ~ Methods =========================================================================================================

  @Override
  public boolean startup(IPentahoSession pentahoSession) {
    String filename = System.getProperty(PROP_DUMP_TO_FILE);
    Mode tmpMode = Mode.CUSTOM;
    if (filename == null) {
      filename = System.getProperty(PROP_DUMP_TO_FILE_SYSTEM_VIEW);
      tmpMode = Mode.SYS;
    }
    if (filename == null) {
      filename = System.getProperty(PROP_DUMP_TO_FILE_DOCUMENT_VIEW);
      tmpMode = Mode.DOC;
    }
    final Mode mode = tmpMode;
    if (filename != null) {
      final JcrTemplate jcrTemplate = PentahoSystem.get(JcrTemplate.class, "jcrTemplate", pentahoSession); //$NON-NLS-1$
      TransactionTemplate txnTemplate = PentahoSystem.get(TransactionTemplate.class,
          "jcrTransactionTemplate", pentahoSession); //$NON-NLS-1$
      String repositoryAdminUsername = PentahoSystem.get(String.class, "repositoryAdminUsername", pentahoSession); //$NON-NLS-1$

      final String ZIP_EXTENSION = ".zip"; //$NON-NLS-1$
      // let the user know this is a zip
      if (!filename.endsWith(ZIP_EXTENSION)) {
        filename = filename + ZIP_EXTENSION;
      }
      logger.debug(String.format("dumping repository to file \"%s\"", filename)); //$NON-NLS-1$
      ZipOutputStream tmpOut = null;
      try {
        tmpOut = new
            ZipOutputStream(new BufferedOutputStream(FileUtils.openOutputStream(new File(filename))));
      } catch (IOException e) {
        IOUtils.closeQuietly(tmpOut);
        throw new RuntimeException(e);
      }
      final ZipOutputStream out = tmpOut;
      // stash existing session
      IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
      // run as repo super user
      PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession(repositoryAdminUsername));
      try {
        txnTemplate.execute(new TransactionCallbackWithoutResult() {
          public void doInTransactionWithoutResult(final TransactionStatus status) {
            jcrTemplate.execute(new JcrCallback() {
              public Object doInJcr(final Session session) throws RepositoryException, IOException {
                switch (mode) {
                  case SYS: {
                    final boolean SKIP_BINARY = false;
                    final boolean NO_RECURSE = false;
                    out.putNextEntry(new ZipEntry("repository.xml")); //$NON-NLS-1$
                    session.exportSystemView("/", out, SKIP_BINARY, NO_RECURSE); //$NON-NLS-1$
                    return null;
                  }
                  case DOC: {
                    final boolean SKIP_BINARY = false;
                    final boolean NO_RECURSE = false;
                    out.putNextEntry(new ZipEntry("repository.xml")); //$NON-NLS-1$
                    session.exportDocumentView("/", out, SKIP_BINARY, NO_RECURSE); //$NON-NLS-1$
                    return null;
                  }
                  default: {
                    out.putNextEntry(new ZipEntry("repository.txt")); //$NON-NLS-1$
                    session.getRootNode().accept(new DumpToFileTraversingItemVisitor(out));
                    return null;
                  }
                }
              }
            });
          }
        });
      } finally {
        // restore original session
        PentahoSessionHolder.setSession(origPentahoSession);
        IOUtils.closeQuietly(out);
      }
      logger.debug(String.format("dumped repository to file \"%s\"", filename)); //$NON-NLS-1$
    }
    return true;
  }

  protected IPentahoSession createRepositoryAdminPentahoSession(final String repositoryAdminUsername) {
    StandaloneSession pentahoSession = new StandaloneSession(repositoryAdminUsername);
    pentahoSession.setAuthenticated(repositoryAdminUsername);
    return pentahoSession;
  }

  @Override
  public void shutdown() {
  }
  
  public static class DumpToFileTraversingItemVisitor extends TraversingItemVisitor {
    
    private OutputStream out;
    
    public DumpToFileTraversingItemVisitor(final OutputStream out) {
     this.out = out; 
    }
    
    private static final String INDENT = "  "; //$NON-NLS-1$
    
    private static final String ENCODING = "UTF-8"; //$NON-NLS-1$
    
    private static final String NL = System.getProperty("line.separator"); //$NON-NLS-1$
    
    @Override
    protected void entering(final Property property, final int level) throws RepositoryException {
      StringBuilder buf = new StringBuilder();
      for (int i = 0; i < level; i++) {
        buf.append(INDENT);
      }
      propertyToString(property, buf);
      try {
        IOUtils.write(buf, out, ENCODING);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    protected void propertyToString(final Property property, final StringBuilder buf) throws RepositoryException {
      buf.append("@"); //$NON-NLS-1$
      buf.append(property.getName());
      buf.append("="); //$NON-NLS-1$
      try {
        Value value = property.getValue();
        valueToString(value, buf);
      } catch (ValueFormatException e) {
        // multi-valued
        Value[] values = property.getValues();
        buf.append("["); //$NON-NLS-1$
        for (int i = 0; i < values.length; i++) {
          if (i > 0) {
            buf.append(","); //$NON-NLS-1$
          }
          valueToString(values[i], buf);
        }
        buf.append("]"); //$NON-NLS-1$
      }
      buf.append(NL);
    }
    protected void valueToString(final Value value, final StringBuilder buf) throws RepositoryException {
      buf.append(value.getString());
      buf.append(" ("); //$NON-NLS-1$
      buf.append(PropertyType.nameFromValue(value.getType()));
      buf.append(")"); //$NON-NLS-1$
    }

    @Override
    protected void entering(final Node node, final int level) throws RepositoryException {
      StringBuilder buf = new StringBuilder();
      for (int i = 0; i < level; i++) {
        buf.append(INDENT);
      }
      nodeToString(node, buf);
      try {
        IOUtils.write(buf, out, ENCODING);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    protected void nodeToString(final Node node, final StringBuilder buf) throws RepositoryException {
      buf.append(node.getName());
      buf.append("/"); //$NON-NLS-1$
      buf.append(NL);
    }

    @Override
    protected void leaving(final Property property, final int level) throws RepositoryException {

    }

    @Override
    protected void leaving(final Node node, final int level) throws RepositoryException {

    }

  }


}
