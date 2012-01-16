/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.step;

import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;

public class RowAdapter implements RowListener {

	public RowAdapter() {
	}
	
	public void errorRowWrittenEvent(RowMetaInterface rowMeta, Object[] row) throws KettleStepException {
	}

	public void rowReadEvent(RowMetaInterface rowMeta, Object[] row) throws KettleStepException {
	}

	public void rowWrittenEvent(RowMetaInterface rowMeta, Object[] row) throws KettleStepException {
	}

}
