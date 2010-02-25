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
package org.pentaho.di.ui.repository.repositoryexplorer.model;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryContent;
import org.pentaho.di.repository.VersionRepository;

public class UIJob extends UIRepositoryContent {

  public UIJob() {
  }

  public UIJob(RepositoryContent rc, UIRepositoryDirectory parent, Repository rep) {
    super(rc, parent, rep);
  }

  @Override
  public String getImage() {
    return "images/job.png"; //$NON-NLS-1$
  }

  @Override
  public void setName(String name) throws Exception {
    super.setName(name);
    rep.renameJob(this.getObjectId(), getRepositoryDirectory(), name);
    uiParent.fireCollectionChanged();
  }
  
  public void delete()throws Exception{
    rep.deleteJob(this.getObjectId());
    if(uiParent.getRepositoryObjects().contains(this))
      uiParent.getRepositoryObjects().remove(this);
  }
  
  public void move(UIRepositoryDirectory newParentDir) throws KettleException {
    if(newParentDir != null) {
      rep.renameJob(obj.getObjectId(), newParentDir.getDirectory(), null);
      newParentDir.refresh();
    }
  }
  
  public void restoreVersion(UIRepositoryObjectRevision revision, String commitMessage) throws KettleException {
    if((getRepository() != null) && getRepository() instanceof VersionRepository) {
      VersionRepository vr = (VersionRepository)getRepository();
      vr.restoreJob(this.getObjectId(), revision.getName(), commitMessage);
    }
  }

}
