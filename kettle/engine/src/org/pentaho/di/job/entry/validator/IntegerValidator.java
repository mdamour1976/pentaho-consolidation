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

package org.pentaho.di.job.entry.validator;

import static org.apache.commons.validator.util.ValidatorUtils.getValueAsString;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.addFailureRemark;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.getLevelOnFail;

import java.util.List;

import org.apache.commons.validator.GenericTypeValidator;
import org.apache.commons.validator.GenericValidator;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.CheckResultSourceInterface;

/**
 * Fails if a field's value is not an integer.
 */
public class IntegerValidator implements JobEntryValidator {

  public static final IntegerValidator INSTANCE = new IntegerValidator();

  private String VALIDATOR_NAME = "integer";

  public boolean validate( CheckResultSourceInterface source, String propertyName, List<CheckResultInterface> remarks,
      ValidatorContext context ) {

    Object result = null;
    String value = null;

    value = getValueAsString( source, propertyName );

    if ( GenericValidator.isBlankOrNull( value ) ) {
      return true;
    }

    result = GenericTypeValidator.formatInt( value );

    if ( result == null ) {
      addFailureRemark( source, propertyName, VALIDATOR_NAME, remarks, getLevelOnFail( context, VALIDATOR_NAME ) );
      return false;
    }
    return true;

  }

  public String getName() {
    return VALIDATOR_NAME;
  }

}
