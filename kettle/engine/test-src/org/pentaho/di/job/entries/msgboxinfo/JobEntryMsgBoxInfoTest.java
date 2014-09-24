/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
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
package org.pentaho.di.job.entries.msgboxinfo;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.Map;

import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;

public class JobEntryMsgBoxInfoTest extends JobEntryLoadSaveTestSupport<JobEntryMsgBoxInfo> {

  @Override
  protected Class<JobEntryMsgBoxInfo> getJobEntryClass() {
    return JobEntryMsgBoxInfo.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return asList(
        "bodymessage",
        "titremessage" );
  }

  @Override
  protected Map<String, String> createGettersMap() {
    return toMap(
        "bodymessage", "getBodyMessage",
        "titremessage", "getTitleMessage" );
  }

  @Override
  protected Map<String, String> createSettersMap() {
    return toMap(
        "bodymessage", "setBodyMessage",
        "titremessage", "setTitleMessage" );
  }

}
