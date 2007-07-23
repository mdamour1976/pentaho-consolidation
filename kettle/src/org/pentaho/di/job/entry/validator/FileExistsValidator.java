package org.pentaho.di.job.entry.validator;

import java.io.IOException;
import java.util.List;

import org.apache.commons.validator.util.ValidatorUtils;
import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.CheckResultSourceInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;

/**
 * Fails if a field's value is a filename and the file does not exist.
 *
 * @author mlowery
 */
public class FileExistsValidator extends AbstractFileValidator
{

  public static final FileExistsValidator INSTANCE = new FileExistsValidator();

  static final String VALIDATOR_NAME = "fileExists"; //$NON-NLS-1$

  public boolean validate(CheckResultSourceInterface source, String propertyName, List<CheckResultInterface> remarks,
      ValidatorContext context)
  {

    String filename = ValidatorUtils.getValueAsString(source, propertyName);
    VariableSpace variableSpace = getVariableSpace(source, propertyName, remarks, context);

    if (null == variableSpace)
    {
      return false;
    }

    String realFileName = variableSpace.environmentSubstitute(filename);
    FileObject fileObject = null;
    try
    {
      fileObject = KettleVFS.getFileObject(realFileName);
      if (fileObject == null || (fileObject != null && !fileObject.exists()))
      {
        JobEntryValidatorUtils.addFailureRemark(source, propertyName, VALIDATOR_NAME, remarks, JobEntryValidatorUtils
            .getLevelOnFail(context, VALIDATOR_NAME));
        return false;
      }
      try
      {
        fileObject.close(); // Just being paranoid
      } catch (IOException ignored)
      {
      }
    } catch (Exception e)
    {
      JobEntryValidatorUtils.addExceptionRemark(source, propertyName, VALIDATOR_NAME, remarks, e);
      return false;
    }
    return true;
  }

  public String getName()
  {
    return VALIDATOR_NAME;
  }

}
