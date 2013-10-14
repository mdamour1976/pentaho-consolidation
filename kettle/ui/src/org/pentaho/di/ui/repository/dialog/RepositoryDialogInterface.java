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

package org.pentaho.di.ui.repository.dialog;

import org.pentaho.di.repository.RepositoryMeta;

public interface RepositoryDialogInterface {
  public static enum MODE {
    ADD, EDIT
  }

  /**
   * Open the dialog
   * 
   * @param mode
   *          (Add or Edit)
   * @return the description of the repository
   * @throws RepositoryAlreadyExistException
   */
  public RepositoryMeta open( final MODE mode );
}
