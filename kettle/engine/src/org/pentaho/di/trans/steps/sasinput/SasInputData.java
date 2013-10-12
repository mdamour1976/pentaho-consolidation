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

package org.pentaho.di.trans.steps.sasinput;

import java.util.List;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Provides data for the XBaseInput step.
 * 
 * @author Matt
 * @since 09-OCT-2011
 * @version 4.3
 */
public class SasInputData extends BaseStepData implements StepDataInterface {
  public SasInputHelper helper;
  public RowMetaInterface outputRowMeta;
  public RowMetaInterface fileLayout;
  public List<Integer> fieldIndexes;

  public SasInputData() {
    super();

    helper = null;
  }

}
