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
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 *
 * Created Mar 03, 2012
 * @author Ezequiel Cuellar
 */

package org.pentaho.mantle.client.admin;

import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.ui.xul.gwt.tags.GwtDialog;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class RoleDialog extends GwtDialog {

	private TextBox roleName;
	private UserRolesAdminPanelController controller;
	private Button acceptBtn = new Button(Messages.getString("ok"));
	private Button cancelBtn = new Button(Messages.getString("cancel"));

	public RoleDialog(UserRolesAdminPanelController controller) {
		setWidth(270);
		setHeight(140);
		getButtonPanel();
		setTitle(Messages.getString("newRole"));

		roleName = new TextBox();

		acceptBtn.setStylePrimaryName("pentaho-button");
		acceptBtn.addClickHandler(new AcceptListener());
		cancelBtn.setStylePrimaryName("pentaho-button");
		cancelBtn.addClickHandler(new CancelListener());

		this.controller = controller;
	}

	public Panel getButtonPanel() {
		HorizontalPanel hp = new HorizontalPanel();
		hp.add(acceptBtn);
		hp.setCellWidth(acceptBtn, "100%");
		hp.setCellHorizontalAlignment(acceptBtn, HorizontalPanel.ALIGN_RIGHT);
		hp.add(cancelBtn);
		return hp;
	}

	public Panel getDialogContents() {

		HorizontalPanel hp = new HorizontalPanel();
		hp.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
		hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

		VerticalPanel vp = new VerticalPanel();
		Label nameLabel = new Label(Messages.getString("name") + ":");
		vp.add(nameLabel);
		vp.add(roleName);

		hp.add(vp);
		return hp;
	}

	class AcceptListener implements ClickHandler {
		public void onClick(ClickEvent event) {
			String name = roleName.getText();
			if (!StringUtils.isEmpty(name)) {
				controller.saveRole(name);
				hide();
			}
		}
	}

	class CancelListener implements ClickHandler {
		public void onClick(ClickEvent event) {
			hide();
		}
	}
}
