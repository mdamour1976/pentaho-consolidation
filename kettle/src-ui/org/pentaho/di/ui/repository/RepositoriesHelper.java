package org.pentaho.di.ui.repository;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryLoader;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryPluginMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.repository.dialog.RepositoryDialogInterface;
import org.pentaho.di.ui.repository.model.RepositoriesModel;
import org.pentaho.di.ui.repository.repositoryexplorer.RepositoryExplorer;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.components.XulConfirmBox;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.util.XulDialogCallback;

public class RepositoriesHelper {
  private static Class<?> PKG = RepositoriesHelper.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
  private Shell shell;
  private PropsUI props;
  private RepositoriesMeta input;
  private Repository repository;
  private String prefRepositoryName;
  private ResourceBundle messages;
  private RepositoriesModel model;
  private XulMessageBox messageBox;
  private XulConfirmBox confirmBox;
  public RepositoriesHelper(RepositoriesModel model
      , XulMessageBox messagebox, XulConfirmBox confirmBox, ResourceBundle messages, Shell shell) {
    this.props = PropsUI.getInstance();
    this.input = new RepositoriesMeta();
    this.repository = null;
    this.model = model;
    this.messageBox = messagebox;
    this.confirmBox = confirmBox;
    this.messages = messages;
    this.shell = shell;
    try {
      this.input.readData();
      List<RepositoryMeta> repositoryList = new ArrayList<RepositoryMeta>();
      for(int i=0; i<this.input.nrRepositories();i++) {
        repositoryList.add(this.input.getRepository(i));
      }
      model.setAvailableRepositories(repositoryList);
    } catch (Exception e) {
      messageBox.setTitle(messages.getString("Dialog.Error"));//$NON-NLS-1$
      messageBox.setAcceptLabel(messages.getString("Dialog.Ok"));//$NON-NLS-1$
      messageBox.setMessage(BaseMessages.getString(PKG, "RepositoryLogin.ErrorReadingRepositoryDefinitions", e.getLocalizedMessage()));//$NON-NLS-1$
      messageBox.open();
    }
  }
  public void newRepository() {
    List<RepositoryPluginMeta> pluginMetaList = RepositoryLoader.getInstance().getPluginMetaList();
    String[] names = new String[pluginMetaList.size()];
    for (int i = 0; i < names.length; i++) {
      RepositoryPluginMeta meta = pluginMetaList.get(i);
      names[i] = meta.getName() + " : " + meta.getDescription(); //$NON-NLS-1$
    }

    // TODO: make this a bit fancier!
    //
    EnterSelectionDialog selectRepositoryType = new EnterSelectionDialog(this.shell, names, "Select the repository type", "Select the repository type to create"); //$NON-NLS-1$//$NON-NLS-2$
    String choice = selectRepositoryType.open();
    if (choice != null) {
      int index = selectRepositoryType.getSelectionNr();
      RepositoryPluginMeta pluginMeta = pluginMetaList.get(index);
      String id = pluginMeta.getId();

      try {
        // With this ID we can create a new Repository object...
        //
        RepositoryMeta repositoryMeta = RepositoryLoader.createRepositoryMeta(id);
        RepositoryDialogInterface dialog = getRepositoryDialog(pluginMeta, repositoryMeta, input, this.shell);
        RepositoryMeta meta = dialog.open();
        if (meta != null) {
          input.addRepository(meta);
          fillRepositories();
          model.setSelectedRepository(meta);
          writeData();
        }
      } catch (Exception e) {
        messageBox.setTitle(messages.getString("Dialog.Error"));//$NON-NLS-1$
        messageBox.setAcceptLabel(messages.getString("Dialog.Ok"));//$NON-NLS-1$
        messageBox.setMessage(BaseMessages.getString(PKG, "RepositoryLogin.ErrorCreatingRepository", e.getLocalizedMessage()));//$NON-NLS-1$
        messageBox.open();
      }
    }
  }

  public void editRepository() {
    try {
        RepositoryPluginMeta pluginMeta = null; 
        RepositoryMeta ri = input.searchRepository(model.getSelectedRepository().getName());
        if (ri != null) {
          pluginMeta = RepositoryLoader.getInstance().findPluginMeta(ri.getId());
          if (pluginMeta == null) {
            throw new KettleException("Unable to find repository plugin for id [" + ri.getId() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
          }
        }
          RepositoryDialogInterface dd = getRepositoryDialog(pluginMeta, ri, input, this.shell);
          if (dd.open() != null) {
            fillRepositories();
            int idx = input.indexOfRepository(ri);
            model.setSelectedRepository(input.getRepository(idx));
            writeData();
          }
    } catch (Exception e) {
      messageBox.setTitle(messages.getString("Dialog.Error"));//$NON-NLS-1$
      messageBox.setAcceptLabel(messages.getString("Dialog.Ok"));//$NON-NLS-1$
      messageBox.setMessage(BaseMessages.getString(PKG, "RepositoryLogin.ErrorEditingRepository", e.getLocalizedMessage()));//$NON-NLS-1$
      messageBox.open();
    }
  }

  public void deleteRepository() {

      final RepositoryMeta repositoryMeta = input.searchRepository(model.getSelectedRepository().getName());
      if (repositoryMeta != null) {
        confirmBox.setTitle(messages.getString("RepositoryLogin.ConfirmDeleteRepositoryDialog.Title"));//$NON-NLS-1$
        confirmBox.setMessage(messages.getString("RepositoryLogin.ConfirmDeleteRepositoryDialog.Message"));//$NON-NLS-1$
        confirmBox.setAcceptLabel(messages.getString("Dialog.Ok"));//$NON-NLS-1$
        confirmBox.setCancelLabel(messages.getString("Dialog.Cancel"));//$NON-NLS-1$
        confirmBox.addDialogCallback(new XulDialogCallback<Object>() {
  
          public void onClose(XulComponent sender, Status returnCode, Object retVal) {
            if (returnCode == Status.ACCEPT) {
              int idx = input.indexOfRepository(repositoryMeta);
              input.removeRepository(idx);
              fillRepositories();
              writeData();
            }
          }
  
          public void onError(XulComponent sender, Throwable t) {
            messageBox.setTitle(messages.getString("Dialog.Error"));//$NON-NLS-1$
            messageBox.setAcceptLabel(messages.getString("Dialog.Ok"));//$NON-NLS-1$
            messageBox.setMessage(BaseMessages.getString(RepositoryExplorer.class,
                "RepositoryLogin.UnableToDeleteRepository", t.getLocalizedMessage()));//$NON-NLS-1$
            messageBox.open();
          }
        });
        confirmBox.open();
      }
      
  }
  protected RepositoryDialogInterface getRepositoryDialog(RepositoryPluginMeta pluginMeta, RepositoryMeta repositoryMeta, RepositoriesMeta input2, Shell shell) throws Exception {
    ClassLoader classLoader = RepositoryLoader.getInstance().getClassLoader(pluginMeta);
    Class<?> dialogClass = classLoader.loadClass(pluginMeta.getDialogClassName());
    Constructor<?> constructor = dialogClass.getConstructor(Shell.class, Integer.TYPE, RepositoryMeta.class, RepositoriesMeta.class);
    return (RepositoryDialogInterface) constructor.newInstance(new Object[] { shell, Integer.valueOf(SWT.NONE), repositoryMeta, input, });
  }
  

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getMetaData() {
    fillRepositories();

    String repname = props.getLastRepository();
    if (repname != null) {
      model.setSelectedRepositoryUsingName(repname);
      String username = props.getLastRepositoryLogin();
      if (username != null) {
        model.setUsername(username);
      }
    }

    // Do we have a preferred repository name to select
    if (prefRepositoryName != null) {
      model.setSelectedRepositoryUsingName(prefRepositoryName);
    }

    model.setShowDialogAtStartup(props.showRepositoriesDialogAtStartup());
    
  }
  

  
  public void fillRepositories() {
    model.getAvailableRepositories().clear();
    for (int i = 0; i < input.nrRepositories(); i++) {
      model.addToAvailableRepositories(input.getRepository(i));
    }
  }
  
  public Repository getConnectedRepository() {
    return repository;
  }

  public void setPreferredRepositoryName(String repname) {
    prefRepositoryName = repname;
  }
  
  public void loginToRepository() throws KettleException {
      RepositoryMeta repinfo = input.getRepository(model.getRepositoryIndex(model.getSelectedRepository()));
      repository = RepositoryLoader.getInstance().createRepositoryObject(repinfo.getId());
      repository.init(repinfo);
      repository.connect(model.getUsername(), model.getPassword());
      props.setLastRepository(repinfo.getName());
      props.setLastRepositoryLogin(model.getUsername());
  }
  
  public void updateShowDialogOnStartup(boolean value) {
    props.setRepositoriesDialogAtStartupShown(value);
  }
  
  private void writeData() {
    try {
      input.writeData();
    } catch (Exception e) {
      messageBox.setTitle(messages.getString("Dialog.Error"));//$NON-NLS-1$
      messageBox.setAcceptLabel(messages.getString("Dialog.Ok"));//$NON-NLS-1$
      messageBox.setMessage(messages.getString("RepositoryLogin.ErrorSavingRepositoryDefinition"));//$NON-NLS-1$
      messageBox.open();
    } 
  }
}
