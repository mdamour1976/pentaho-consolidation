package org.pentaho.di.core;

public interface CheckResultInterface {

  public static final int TYPE_RESULT_NONE = 0;

  public static final int TYPE_RESULT_OK = 1;

  public static final int TYPE_RESULT_COMMENT = 2;

  public static final int TYPE_RESULT_WARNING = 3;

  public static final int TYPE_RESULT_ERROR = 4;

  public static final String typeDesc[] = {
                                           "", //$NON-NLS-1$
                                           Messages.getString("CheckResult.OK"), //$NON-NLS-1$
                                           Messages.getString("CheckResult.Remark"), //$NON-NLS-1$
                                           Messages.getString("CheckResult.Warning"), //$NON-NLS-1$
                                           Messages.getString("CheckResult.Error") //$NON-NLS-1$
                                           };

  /**
   * @return The type of the Check Result (0-4)
   */
  public int getType();
  /**
   * @return The internationalized type description
   */
  public String getTypeDesc();
  /**
   * @return The text of the check result.
   */
  public String getText();
  /**
   * @return The source of the check result
   */
  public CheckResultSourceInterface getSourceInfo();
  /**
   * @return String description of the check result
   */
  public String toString();
  /**
   * @return The component-specific result code. 
   */
  public String getErrorCode();
  /**
   * Sets the component-specific result/error code.
   * @param errorCode Unchecked string that can be used for validation
   */
  public void setErrorCode(String errorCode);
  /**
   * Sets the check-result type
   * @param value The type from 0-4
   */
  public void setType(int value);
  /**
   * Sets the text for the check-result
   * @param value 
   */
  public void setText(String value);
  
}
