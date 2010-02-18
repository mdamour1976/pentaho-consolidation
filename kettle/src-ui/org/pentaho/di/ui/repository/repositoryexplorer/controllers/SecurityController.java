/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2009 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.di.ui.repository.repositoryexplorer.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.IRole;
import org.pentaho.di.repository.ObjectRecipient;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositorySecurityManager;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.repository.ObjectRecipient.Type;
import org.pentaho.di.ui.repository.repositoryexplorer.ControllerInitializationException;
import org.pentaho.di.ui.repository.repositoryexplorer.RepositoryExplorer;
import org.pentaho.di.ui.repository.repositoryexplorer.UIObjectCreationException;
import org.pentaho.di.ui.repository.repositoryexplorer.model.IUIRole;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIObjectRegistery;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryRole;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryUser;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UISecurity;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UISecurityRole;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UISecurityUser;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UISecurity.Mode;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulConfirmBox;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.components.XulRadio;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.util.XulDialogCallback;

/**
 *
 * This is the XulEventHandler for the browse panel of the repository explorer. It sets up the bindings for  
 * browse functionality.
 * 
 */
public class SecurityController extends AbstractXulEventHandler implements ISecurityController{

  private ResourceBundle messages;

  public static final int ROLE_DECK = 0;

  public static final int USER_DECK = 1;

  private XulRadio roleRadioButton;

  private XulRadio userRadioButton;

  private XulTree userDetailTable;

  private XulTree roleDetailTable;

  private XulDialog userDialog;

  private XulDialog roleDialog;

  private XulDeck userRoleDeck;

  private XulListbox roleListBox;

  private XulListbox userListBox;

  private XulTextbox username;

  private XulTextbox userPassword;

  private XulTextbox userDescription;

  private XulListbox availableRoles;

  private XulListbox assignedRoles;

  private XulTextbox rolename;

  private XulTextbox roleDescription;

  private XulListbox availableUsers;

  private XulListbox assignedUsers;

  private XulButton roleEditButton;

  private XulButton roleRemoveButton;

  private XulButton userEditButton;

  private XulButton userRemoveButton;

  private XulButton addUserToRoleButton;

  private XulButton removeUserFromRoleButton;

  private XulButton addRoleToUserButton;

  private XulButton removeRoleFromUserButton;

  private XulButton assignRoleToUserButton;

  private XulButton unassignRoleFromUserButton;

  private XulButton assignUserToRoleButton;

  private XulButton unassignUserFromRoleButton;
  
  private RepositorySecurityManager rsm;
  
  protected Repository repository;
  
  protected UISecurity security;

  BindingFactory bf;

  Binding securityBinding;

  Binding roleDetailBinding;

  Binding userDetailBinding;

  protected UISecurityUser securityUser;

  protected UISecurityRole securityRole;

  XulConfirmBox confirmBox = null;

  public XulConfirmBox getConfirmBox() {
    return confirmBox;
  }

  public void setConfirmBox(XulConfirmBox confirmBox) {
    this.confirmBox = confirmBox;
  }

  public XulMessageBox getMessageBox() {
    return messageBox;
  }

  public void setMessageBox(XulMessageBox messageBox) {
    this.messageBox = messageBox;
  }

  XulMessageBox messageBox = null;


  public SecurityController() {
  }

  public void init() throws ControllerInitializationException{
    try {
      createModel();
      securityRole = new UISecurityRole();
      securityUser = new UISecurityUser();
      confirmBox = (XulConfirmBox) document.createElement("confirmbox");//$NON-NLS-1$
      messageBox = (XulMessageBox) document.createElement("messagebox");//$NON-NLS-1$
    } catch (Exception e) {
      throw new ControllerInitializationException(e);
    }
    if (bf != null) {
      createBindings();
    }
  }


  protected void createModel() throws Exception{
      security = new UISecurity(rsm);
  }

  protected void createBindings() {
    //User Role Binding

    roleRadioButton = (XulRadio) document.getElementById("role-radio-button");//$NON-NLS-1$
    userRadioButton = (XulRadio) document.getElementById("user-radio-button");//$NON-NLS-1$

    roleEditButton = (XulButton) document.getElementById("role-edit");//$NON-NLS-1$
    roleRemoveButton = (XulButton) document.getElementById("role-remove");//$NON-NLS-1$
    userEditButton = (XulButton) document.getElementById("user-edit");//$NON-NLS-1$
    userRemoveButton = (XulButton) document.getElementById("user-remove");//$NON-NLS-1$

    addUserToRoleButton = (XulButton) document.getElementById("add-user-to-role");//$NON-NLS-1$
    removeUserFromRoleButton = (XulButton) document.getElementById("remove-user-from-role");//$NON-NLS-1$
    addRoleToUserButton = (XulButton) document.getElementById("add-role-to-user");//$NON-NLS-1$
    removeRoleFromUserButton = (XulButton) document.getElementById("remove-role-from-user");//$NON-NLS-1$

    userDialog = (XulDialog) document.getElementById("add-user-dialog");//$NON-NLS-1$
    roleDialog = (XulDialog) document.getElementById("add-role-dialog");//$NON-NLS-1$
    userRoleDeck = (XulDeck) document.getElementById("user-role-deck");//$NON-NLS-1$
    roleListBox = (XulListbox) document.getElementById("roles-list");//$NON-NLS-1$
    userListBox = (XulListbox) document.getElementById("users-list");//$NON-NLS-1$
    roleDetailTable = (XulTree) document.getElementById("role-detail-table");//$NON-NLS-1$
    userDetailTable = (XulTree) document.getElementById("user-detail-table");//$NON-NLS-1$

    // Add User Binding

    username = (XulTextbox) document.getElementById("user-name");//$NON-NLS-1$
    userPassword = (XulTextbox) document.getElementById("user-password");//$NON-NLS-1$
    userDescription = (XulTextbox) document.getElementById("user-description");//$NON-NLS-1$
    availableRoles = (XulListbox) document.getElementById("available-roles-list");//$NON-NLS-1$
    assignedRoles = (XulListbox) document.getElementById("selected-roles-list");//$NON-NLS-1$
    assignRoleToUserButton = (XulButton) document.getElementById("assign-role-to-user");//$NON-NLS-1$
    unassignRoleFromUserButton = (XulButton) document.getElementById("unassign-role-from-user");//$NON-NLS-1$

    bf.setBindingType(Binding.Type.BI_DIRECTIONAL);
    bf.createBinding(securityUser, "name", username, "value");//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(securityUser, "password", userPassword, "value");//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(securityUser, "description", userDescription, "value");//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(securityUser, "assignedRoles", assignedRoles, "elements");//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(securityUser, "availableRoles", availableRoles, "elements");//$NON-NLS-1$ //$NON-NLS-2$

    // Binding to convert role array to a role list object and vice versa
    BindingConvertor<List<UIRepositoryRole>, Object[]> arrayToListRoleConverter = new BindingConvertor<List<UIRepositoryRole>, Object[]>() {

      @Override
      public Object[] sourceToTarget(List<UIRepositoryRole> roles) {
        return roles == null ? null : roles.toArray();
      }

      @Override
      public List<UIRepositoryRole> targetToSource(Object[] roles) {
        return roles == null ? null : Arrays.<UIRepositoryRole>asList((UIRepositoryRole[])roles);
      }

    };

    // Binding to convert user array to a user list object and vice versa
    BindingConvertor<List<UIRepositoryUser>, Object[]> arrayToListUserConverter = new BindingConvertor<List<UIRepositoryUser>, Object[]>() {

      @Override
      public Object[] sourceToTarget(List<UIRepositoryUser> users) {
        return users == null ? null : users.toArray();
      }

      @Override
      public List<UIRepositoryUser> targetToSource(Object[] users) {
        return users == null ? null : Arrays.<UIRepositoryUser>asList((UIRepositoryUser[])users);
      }

    };

    bf.createBinding(securityUser, "availableSelectedRoles", availableRoles, "selectedItems", arrayToListRoleConverter);//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(securityUser, "assignedSelectedRoles", assignedRoles, "selectedItems", arrayToListRoleConverter);//$NON-NLS-1$ //$NON-NLS-2$

    bf.createBinding(security, "selectedUserIndex", userListBox, "selectedIndex");//$NON-NLS-1$ //$NON-NLS-2$

    BindingConvertor<Integer, Boolean> accumulatorButtonConverter = new BindingConvertor<Integer, Boolean>() {

      @Override
      public Boolean sourceToTarget(Integer value) {
        if (value != null && value >= 0) {
          return true;
        }
        return false;
      }

      @Override
      public Integer targetToSource(Boolean value) {
        return null;
      }
    };

    bf.setBindingType(Binding.Type.ONE_WAY);
    bf.createBinding(assignedRoles,
        "selectedIndex", securityUser, "roleUnassignmentPossible", accumulatorButtonConverter);//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(availableRoles,
        "selectedIndex", securityUser, "roleAssignmentPossible", accumulatorButtonConverter);//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(securityUser, "roleUnassignmentPossible", unassignRoleFromUserButton, "!disabled");//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(securityUser, "roleAssignmentPossible", assignRoleToUserButton, "!disabled");//$NON-NLS-1$ //$NON-NLS-2$

    // Add Role Binding
    rolename = (XulTextbox) document.getElementById("role-name");//$NON-NLS-1$
    roleDescription = (XulTextbox) document.getElementById("role-description");//$NON-NLS-1$
    availableUsers = (XulListbox) document.getElementById("available-users-list");//$NON-NLS-1$
    assignedUsers = (XulListbox) document.getElementById("selected-users-list");//$NON-NLS-1$
    assignUserToRoleButton = (XulButton) document.getElementById("assign-user-to-role");//$NON-NLS-1$
    unassignUserFromRoleButton = (XulButton) document.getElementById("unassign-user-from-role");//$NON-NLS-1$

    bf.setBindingType(Binding.Type.BI_DIRECTIONAL);
    bf.createBinding(securityRole, "name", rolename, "value");//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(securityRole, "description", roleDescription, "value");//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(securityRole, "assignedUsers", assignedUsers, "elements");//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(securityRole, "availableUsers", availableUsers, "elements"); //$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(securityRole, "availableSelectedUsers", availableUsers, "selectedItems", arrayToListUserConverter);//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(securityRole, "assignedSelectedUsers", assignedUsers, "selectedItems", arrayToListUserConverter);//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(security, "selectedRoleIndex", roleListBox, "selectedIndex");//$NON-NLS-1$ //$NON-NLS-2$

    bf.setBindingType(Binding.Type.ONE_WAY);
    bf.createBinding(assignedUsers,
        "selectedIndex", securityRole, "userUnassignmentPossible", accumulatorButtonConverter);//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(availableUsers,
        "selectedIndex", securityRole, "userAssignmentPossible", accumulatorButtonConverter); //$NON-NLS-1$ //$NON-NLS-2$  
    bf.createBinding(securityRole, "userUnassignmentPossible", unassignUserFromRoleButton, "!disabled");//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(securityRole, "userAssignmentPossible", assignUserToRoleButton, "!disabled");//$NON-NLS-1$ //$NON-NLS-2$

    try {
      bf.setBindingType(Binding.Type.ONE_WAY);

      BindingConvertor<Integer, Boolean> buttonConverter = new BindingConvertor<Integer, Boolean>() {

        @Override
        public Boolean sourceToTarget(Integer value) {
          if (value != null && value >= 0) {
            return false;
          }
          return true;
        }

        @Override
        public Integer targetToSource(Boolean value) {
          // TODO Auto-generated method stub
          return null;
        }
      };
      BindingConvertor<Object, Boolean> removeButtonConverter = new BindingConvertor<Object, Boolean>() {

        @Override
        public Boolean sourceToTarget(Object value) {
          if (value != null) {
            return false;
          }
          return true;
        }

        @Override
        public Object targetToSource(Boolean value) {
          // TODO Auto-generated method stub
          return null;
        }
      };
      bf.createBinding(roleListBox, "selectedIndex", roleEditButton, "disabled", buttonConverter);//$NON-NLS-1$ //$NON-NLS-2$
      bf.createBinding(roleListBox, "selectedIndex", roleRemoveButton, "disabled", buttonConverter);//$NON-NLS-1$ //$NON-NLS-2$
      bf.createBinding(userListBox, "selectedIndex", userEditButton, "disabled", buttonConverter);//$NON-NLS-1$ //$NON-NLS-2$
      bf.createBinding(userListBox, "selectedIndex", userRemoveButton, "disabled", buttonConverter);//$NON-NLS-1$ //$NON-NLS-2$

      bf.setBindingType(Binding.Type.ONE_WAY);
      // Action based security permissions
      bf.createBinding(userListBox, "selectedItem", security, "selectedUser");//$NON-NLS-1$ //$NON-NLS-2$
      bf.createBinding(roleListBox, "selectedItem", security, "selectedRole");//$NON-NLS-1$ //$NON-NLS-2$
      bf.createBinding(roleListBox, "selectedIndex", addUserToRoleButton, "disabled", buttonConverter);//$NON-NLS-1$ //$NON-NLS-2$
      bf.createBinding(roleDetailTable, "selectedItem", removeUserFromRoleButton, "disabled", removeButtonConverter);//$NON-NLS-1$ //$NON-NLS-2$
      bf.createBinding(userListBox, "selectedIndex", addRoleToUserButton, "disabled", buttonConverter);//$NON-NLS-1$ //$NON-NLS-2$
      bf.createBinding(userDetailTable, "selectedItem", removeRoleFromUserButton, "disabled", removeButtonConverter);//$NON-NLS-1$ //$NON-NLS-2$
      bf.createBinding(security, "roleList", roleListBox, "elements").fireSourceChanged();//$NON-NLS-1$ //$NON-NLS-2$
      bf.createBinding(security, "userList", userListBox, "elements").fireSourceChanged();//$NON-NLS-1$ //$NON-NLS-2$
      bf.createBinding(roleListBox, "selectedItem", security, "selectedRole");//$NON-NLS-1$ //$NON-NLS-2$
      bf.createBinding(userListBox, "selectedItem", security, "selectedUser");//$NON-NLS-1$ //$NON-NLS-2$

      roleDetailBinding = bf.createBinding(security, "selectedRole", roleDetailTable, "elements",//$NON-NLS-1$ //$NON-NLS-2$
          new BindingConvertor<UIRepositoryRole, List<UIRepositoryUser>>() {

            @Override
            public List<UIRepositoryUser> sourceToTarget(UIRepositoryRole rr) {
              return new ArrayList<UIRepositoryUser>(rr.getUsers());
            }

            @Override
            public UIRepositoryRole targetToSource(List<UIRepositoryUser> arg0) {
              // TODO Auto-generated method stub
              return null;
            }

          });
      userDetailBinding = bf.createBinding(security, "selectedUser", userDetailTable, "elements",//$NON-NLS-1$ //$NON-NLS-2$
          new BindingConvertor<UIRepositoryUser, List<IUIRole>>() {

            @Override
            public List<IUIRole> sourceToTarget(UIRepositoryUser ru) {
                return new ArrayList<IUIRole>(ru.getRoles());
            }

            @Override
            public UIRepositoryUser targetToSource(List<IUIRole> arg0) {
              // TODO Auto-generated method stub
              return null;
            }

          });
      bf.createBinding(security, "selectedDeck", userRoleDeck, "selectedIndex",//$NON-NLS-1$ //$NON-NLS-2$
          new BindingConvertor<ObjectRecipient.Type, Integer>() {

            @Override
            public Integer sourceToTarget(Type arg0) {
              if (arg0 == Type.ROLE) {
                roleRadioButton.setSelected(true);
                userRadioButton.setSelected(false);
                return 0;
              } else if (arg0 == Type.USER) {
                roleRadioButton.setSelected(false);
                userRadioButton.setSelected(true);
                return 1;
              } else
                return -1;
            }

            @Override
            public Type targetToSource(Integer arg0) {
              return null;
            }

          });
      BindingConvertor<Mode, Boolean> modeBindingConverter = new BindingConvertor<Mode, Boolean>() {

        @Override
        public Boolean sourceToTarget(Mode arg0) {
          if (arg0.equals(Mode.ADD)) {
            return false;
          }
          return true;
        }

        @Override
        public Mode targetToSource(Boolean arg0) {
          // TODO Auto-generated method stub
          return null;
        }

      };
      BindingConvertor<Mode, Boolean> anotherModeBindingConverter = new BindingConvertor<Mode, Boolean>() {

        @Override
        public Boolean sourceToTarget(Mode arg0) {
          if (arg0.equals(Mode.EDIT_MEMBER)) {
            return true;
          } else
            return false;
        }

        @Override
        public Mode targetToSource(Boolean arg0) {
          // TODO Auto-generated method stub
          return null;
        }

      };
      bf.createBinding(securityRole, "mode", rolename, "disabled", modeBindingConverter);//$NON-NLS-1$ //$NON-NLS-2$
      bf.createBinding(securityRole, "mode", roleDescription, "disabled", anotherModeBindingConverter);//$NON-NLS-1$ //$NON-NLS-2$

      bf.createBinding(securityUser, "mode", username, "disabled", modeBindingConverter);//$NON-NLS-1$ //$NON-NLS-2$
      bf.createBinding(securityUser, "mode", userPassword, "disabled", anotherModeBindingConverter);//$NON-NLS-1$ //$NON-NLS-2$
      bf.createBinding(securityUser, "mode", userDescription, "disabled", anotherModeBindingConverter);//$NON-NLS-1$ //$NON-NLS-2$
    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
    changeToRoleDeck();
  }

  public void setBindingFactory(BindingFactory bf) {
    this.bf = bf;
  }

  public BindingFactory getBindingFactory() {
    return this.bf;
  }

  public String getName() {
    return "iSecurityController"; //$NON-NLS-1$
  }

  public RepositorySecurityManager getRepositorySecurityManager() {
    return rsm;
  }

  public void setRepositorySecurityManager(RepositorySecurityManager rsm) {
    this.rsm = rsm;
  }

  public void assignUsersToRole() {
    securityRole.assignUsers(Arrays.asList(availableUsers.getSelectedItems()));
  }

  public void unassignUsersFromRole() {
    securityRole.unassignUsers(Arrays.asList(assignedUsers.getSelectedItems()));
  }

  public void assignRolesToUser() {
    securityUser.assignRoles(Arrays.asList(availableRoles.getSelectedItems()));
  }

  public void unassignRolesFromUser() {
    securityUser.unassignRoles(Arrays.asList(assignedRoles.getSelectedItems()));
  }

  public void showAddUserDialog() throws Exception {
    try {
      if (rsm != null && rsm.getRoles() != null) {
        securityUser.clear();
        securityUser.setAvailableRoles(convertToUIRoleModel(rsm.getRoles()));
      }
      securityUser.setMode(Mode.ADD);
      userDialog.setTitle(messages.getString("AddUserDialog.Title"));//$NON-NLS-1$
      userDialog.show();
    } catch (KettleException e) {
      messageBox.setTitle(messages.getString("Dialog.Error"));//$NON-NLS-1$
      messageBox.setAcceptLabel(messages.getString("Dialog.Ok"));//$NON-NLS-1$
      messageBox.setMessage(BaseMessages.getString(RepositoryExplorer.class,
          "SecurityController.AddUser.UnableToShowAddUser", e.getLocalizedMessage()));//$NON-NLS-1$
      messageBox.open();
    }
  }

  public void cancelAddUserDialog() throws Exception {
    userDialog.hide();
  }

  public void cancelAddRoleDialog() throws Exception {
    roleDialog.hide();
  }

  /**
   * addRole method is called when user has click ok on a add role dialog. The method add the 
   * role
   * @throws Exception
   */
  private void addUser() throws Exception {
    if (rsm != null) {
      try {
        rsm.saveUserInfo(securityUser.getUserInfo());
        security.addUser(new UIRepositoryUser(securityUser.getUserInfo()));
      } catch (KettleException ke) {
        messageBox.setTitle(messages.getString("Dialog.Error"));//$NON-NLS-1$
        messageBox.setAcceptLabel(messages.getString("Dialog.Ok"));//$NON-NLS-1$
        messageBox.setMessage(BaseMessages.getString(RepositoryExplorer.class,
            "AddUser.UnableToAddUser", ke.getLocalizedMessage()));//$NON-NLS-1$
        messageBox.open();
      }
    }
    userDialog.hide();
  }

  public void showEditUserDialog() throws Exception {
    if (rsm != null && rsm.getRoles() != null) {
      securityUser.clear();
      securityUser.setUser(security.getSelectedUser(), convertToUIRoleModel(rsm.getRoles()));
      securityUser.setMode(Mode.EDIT);
      userDialog.setTitle(messages.getString("EditUserDialog.Title"));//$NON-NLS-1$
      userDialog.show();
    }
  }

  /**
   * updateUser method is called when user has click ok on a edit user dialog. The method updates the 
   * user
   * @throws Exception
   */

  private void updateUser() throws Exception {
    if (rsm != null) {
      try {
        UIRepositoryUser uiUser = security.getSelectedUser();
        Set<IUIRole> previousRoleList = new HashSet<IUIRole>();
        previousRoleList.addAll(uiUser.getRoles());
        uiUser.setDescription(securityUser.getDescription());
        uiUser.setPassword(securityUser.getPassword());
        uiUser.setRoles(new HashSet<IUIRole>(securityUser.getAssignedRoles()));
        rsm.updateUser(uiUser.getUserInfo());
        security.updateUser(uiUser, previousRoleList);
      } catch (KettleException ke) {
        messageBox.setTitle(messages.getString("Dialog.Error"));//$NON-NLS-1$
        messageBox.setAcceptLabel(messages.getString("Dialog.Ok"));//$NON-NLS-1$
        messageBox.setMessage(BaseMessages.getString(RepositoryExplorer.class,
            "UpdateUser.UnableToUpdateUser", ke.getLocalizedMessage()));//$NON-NLS-1$
        messageBox.open();
      }
    }
    userDialog.hide();
  }

  public void showAddRoleDialog() throws Exception {
    try {
      if (rsm != null && rsm.getUsers() != null) {
        securityRole.clear();
        securityRole.setAvailableUsers(convertToUIUserModel(rsm.getUsers()));
      }
      roleDialog.setTitle(messages.getString("AddRoleDialog.Title"));//$NON-NLS-1$
      roleDialog.show();
    } catch (KettleException e) {
      messageBox.setTitle(messages.getString("Dialog.Error"));//$NON-NLS-1$
      messageBox.setAcceptLabel(messages.getString("Dialog.Ok"));//$NON-NLS-1$
      messageBox.setMessage(BaseMessages.getString(RepositoryExplorer.class,
          "SecurityController.AddRole.UnableToShowAddRole", e.getLocalizedMessage()));//$NON-NLS-1$
      messageBox.open();

    }
  }

  public void showAddUserToRoleDialog() throws Exception {
    if (rsm != null && rsm.getUsers() != null) {
      securityRole.clear();
      securityRole.setRole(security.getSelectedRole(), convertToUIUserModel(rsm.getUsers()));
      securityRole.setMode(Mode.EDIT_MEMBER);
    }
    roleDialog.setTitle(messages.getString("AddUserToRoleDialog.Title"));//$NON-NLS-1$
    roleDialog.show();
  }

  public void showAddRoleToUserDialog() throws Exception {
    if (rsm != null && rsm.getRoles() != null) {
      securityUser.clear();
      securityUser.setUser(security.getSelectedUser(), convertToUIRoleModel(rsm.getRoles()));
      securityUser.setMode(Mode.EDIT_MEMBER);
      userDialog.setTitle(messages.getString("AddRoleToUserDialog.Title"));//$NON-NLS-1$
      userDialog.show();
    }
  }

  public void removeRolesFromUser() throws Exception {
    security.removeRolesFromSelectedUser(userDetailTable.getSelectedItems());
    rsm.updateUser(security.getSelectedUser().getUserInfo());
  }

  public void removeUsersFromRole() throws Exception {
    security.removeUsersFromSelectedRole(roleDetailTable.getSelectedItems());
    rsm.updateRole(security.getSelectedRole().getRole());
  }

  /**
   * addRole method is called when user has click ok on a add role dialog. The method add the 
   * role
   * @throws Exception
   */

  private void addRole() throws Exception {
    if (rsm != null) {
      try {
        IRole role = securityRole.getRole(rsm);
        rsm.createRole(role);
        security.addRole(UIObjectRegistery.getInstance().constructUIRepositoryRole(role));
      } catch (KettleException ke) {
        messageBox.setTitle(messages.getString("Dialog.Error"));//$NON-NLS-1$
        messageBox.setAcceptLabel(messages.getString("Dialog.Ok"));//$NON-NLS-1$
        messageBox.setMessage(BaseMessages.getString(RepositoryExplorer.class,
            "AddRole.UnableToAddRole", ke.getLocalizedMessage()));//$NON-NLS-1$
        messageBox.open();
      }
    }
    roleDialog.hide();
  }

  /**
   * updateRole method is called when user has click ok on a edit role dialog. The method updates the 
   * role
   * @throws Exception
   */

  private void updateRole() throws Exception {
    if (rsm != null) {
      try {
        IUIRole uiRole = security.getSelectedRole();
        Set<UIRepositoryUser> previousUserList = new HashSet<UIRepositoryUser>();
        previousUserList.addAll(uiRole.getUsers());
        uiRole.setDescription(securityRole.getDescription());
        uiRole.setUsers(new HashSet<UIRepositoryUser>(securityRole.getAssignedUsers()));
        rsm.updateRole(uiRole.getRole());
        security.updateRole(uiRole, previousUserList);
        roleDialog.hide();
      } catch (KettleException ke) {
        messageBox.setTitle(messages.getString("Dialog.Error"));//$NON-NLS-1$
        messageBox.setAcceptLabel(messages.getString("Dialog.Ok"));//$NON-NLS-1$
        messageBox.setMessage(BaseMessages.getString(RepositoryExplorer.class,
            "UpdateRole.UnableToUpdateRole", ke.getLocalizedMessage()));//$NON-NLS-1$
        messageBox.open();
      }
    }
  }

  /**
   * removeRole method is called when user has click on a remove button in a role deck. It first
   * displays a confirmation message to the user and once the user selects ok, it remove the role
   * @throws Exception
   */

  public void removeRole() throws Exception {
    confirmBox.setTitle(messages.getString("ConfirmDialog.Title"));//$NON-NLS-1$
    confirmBox.setMessage(messages.getString("RemoveRoleConfirmDialog.Message"));//$NON-NLS-1$
    confirmBox.setAcceptLabel(messages.getString("Dialog.Ok"));//$NON-NLS-1$
    confirmBox.setCancelLabel(messages.getString("Dialog.Cancel"));//$NON-NLS-1$
    confirmBox.addDialogCallback(new XulDialogCallback<Object>() {

      public void onClose(XulComponent sender, Status returnCode, Object retVal) {
        if (returnCode == Status.ACCEPT) {
          if (rsm != null) {
            if (security != null && security.getSelectedRole() != null) {
              try {
                rsm.deleteRole(security.getSelectedRole().getName());
                security.removeRole(security.getSelectedRole().getName());
              } catch (KettleException ke) {
                messageBox.setTitle(messages.getString("Dialog.Error"));//$NON-NLS-1$
                messageBox.setAcceptLabel(messages.getString("Dialog.Ok"));//$NON-NLS-1$
                messageBox.setMessage(BaseMessages.getString(RepositoryExplorer.class,
                    "RemoveRole.UnableToRemoveRole", ke.getLocalizedMessage()));//$NON-NLS-1$
                messageBox.open();
              }
            } else {
              messageBox.setTitle(messages.getString("Dialog.Error"));//$NON-NLS-1$
              messageBox.setAcceptLabel(messages.getString("Dialog.Ok"));//$NON-NLS-1$
              messageBox.setMessage(messages.getString("RemoveRole.NoRoleSelected"));//$NON-NLS-1$
              messageBox.open();
            }
          }
        }
      }

      public void onError(XulComponent sender, Throwable t) {
        messageBox.setTitle(messages.getString("Dialog.Error"));//$NON-NLS-1$
        messageBox.setAcceptLabel(messages.getString("Dialog.Ok"));//$NON-NLS-1$
        messageBox.setMessage(BaseMessages.getString(RepositoryExplorer.class,
            "RemoveRole.UnableToRemoveRole", t.getLocalizedMessage()));//$NON-NLS-1$
        messageBox.open();
      }
    });
    confirmBox.open();
  }

  public void showEditRoleDialog() throws Exception {
    if (rsm != null && rsm.getUsers() != null) {
      securityRole.clear();
      securityRole.setRole(security.getSelectedRole(), convertToUIUserModel(rsm.getUsers()));
      securityRole.setMode(Mode.EDIT);
      roleDialog.setTitle(messages.getString("EditRoleDialog.Title"));//$NON-NLS-1$
      roleDialog.show();
    }
  }

  /**
   * removeUser method is called when user has click on a remove button in a user deck. It first
   * displays a confirmation message to the user and once the user selects ok, it remove the user
   * @throws Exception
   */
  public void removeUser() throws Exception {
    confirmBox.setTitle(messages.getString("ConfirmDialog.Title"));//$NON-NLS-1$
    confirmBox.setMessage(messages.getString("RemoveUserConfirmDialog.Message"));//$NON-NLS-1$
    confirmBox.setAcceptLabel(messages.getString("Dialog.Ok"));//$NON-NLS-1$
    confirmBox.setCancelLabel(messages.getString("Dialog.Cancel"));//$NON-NLS-1$
    confirmBox.addDialogCallback(new XulDialogCallback<Object>() {

      public void onClose(XulComponent sender, Status returnCode, Object retVal) {
        if (returnCode == Status.ACCEPT) {
          if (rsm != null) {
            if (security != null && security.getSelectedUser() != null) {
              try {
                rsm.delUser(security.getSelectedUser().getName());
                security.removeUser(security.getSelectedUser().getName());
              } catch (KettleException ke) {
                messageBox.setTitle(messages.getString("Dialog.Error"));//$NON-NLS-1$
                messageBox.setAcceptLabel(messages.getString("Dialog.Ok"));//$NON-NLS-1$
                messageBox.setMessage(BaseMessages.getString(RepositoryExplorer.class,
                    "RemoveUser.UnableToRemoveUser", ke.getLocalizedMessage()));//$NON-NLS-1$
                messageBox.open();
              }
            } else {
              messageBox.setTitle(messages.getString("Dialog.Error"));//$NON-NLS-1$
              messageBox.setAcceptLabel(messages.getString("Dialog.Ok"));//$NON-NLS-1$
              messageBox.setMessage(messages.getString("RemoveUser.NoUserSelected"));//$NON-NLS-1$
              messageBox.open();
            }
          }
        }
      }

      public void onError(XulComponent sender, Throwable t) {
        messageBox.setTitle(messages.getString("Dialog.Error"));//$NON-NLS-1$
        messageBox.setAcceptLabel(messages.getString("Dialog.Ok"));//$NON-NLS-1$
        messageBox.setMessage(BaseMessages.getString(RepositoryExplorer.class,
            "RemoveUser.UnableToRemoveUser", t.getLocalizedMessage()));//$NON-NLS-1$
        messageBox.open();
      }
    });
    confirmBox.open();
  }

  public void changeToRoleDeck() {
    security.setSelectedDeck(ObjectRecipient.Type.ROLE);
  }

  public void changeToUserDeck() {
    security.setSelectedDeck(ObjectRecipient.Type.USER);
  }

  /**
   * saveUser method is called when the user click on the ok button of a Add or Edit User dialog
   * Depending on the mode it calls add of update user method
   * @throws Exception
   */

  public void saveUser() throws Exception {
    if (securityUser.getMode().equals(Mode.ADD)) {
      addUser();
    } else {
      updateUser();
    }
  }

  /**
   * saveRole method is called when the user click on the ok button of a Add or Edit Role dialog
   * Depending on the mode it calls add of update role method
   * @throws Exception
   */
  public void saveRole() throws Exception {
    if (securityRole.getMode().equals(Mode.ADD)) {
      addRole();
    } else {
      updateRole();
    }
  }

  private List<IUIRole> convertToUIRoleModel(List<IRole> roles) throws UIObjectCreationException {
    List<IUIRole> rroles = new ArrayList<IUIRole>();
    for (IRole role : roles) {
      rroles.add(UIObjectRegistery.getInstance().constructUIRepositoryRole(role));
    }
    return rroles;
  }

  private List<UIRepositoryUser> convertToUIUserModel(List<UserInfo> users) {
    List<UIRepositoryUser> rusers = new ArrayList<UIRepositoryUser>();
    for (UserInfo user : users) {
      rusers.add(new UIRepositoryUser(user));
    }
    return rusers;
  }

  public void setMessages(ResourceBundle messages) {
    this.messages = messages;
  }

  public ResourceBundle getMessages() {
    return messages;
  }
  
  public UISecurity getSecurity() {
    return security;
  }

  public void setSecurity(UISecurity security) {
    this.security = security;
  }

  public Repository getRepository() {
    return this.repository;
  }

  public void setRepository(Repository repository) {
    this.repository = repository;
  }


}
