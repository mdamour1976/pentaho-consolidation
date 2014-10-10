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

package org.pentaho.di.core;

import java.util.Map;

/**
 * This interface gives you access to a map which is associated with an object to store the state of non-standard
 * objects. It can be used by plugins to store state-information details in transformations, jobs, steps, job-entries...
 * at runtime.
 *
 * @author matt
 *
 */
public interface ExtensionDataInterface {
  public Map<String, Object> getExtensionDataMap();
}
