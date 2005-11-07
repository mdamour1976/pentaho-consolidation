 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** It belongs to, is maintained by and is copyright 1999-2005 by     **
 **                                                                   **
 **      i-Bridge bvba                                                **
 **      Fonteinstraat 70                                             **
 **      9400 OKEGEM                                                  **
 **      Belgium                                                      **
 **      http://www.kettle.be                                         **
 **      info@kettle.be                                               **
 **                                                                   **
 **********************************************************************/
 

package be.ibridge.kettle.core.exception;

/**
 * This exception is used by the Database class.
 *  
 * @author Matt
 * @since 9-12-2004
 *
 */
public class KettleDatabaseBatchException extends KettleDatabaseException
{
    public static final long serialVersionUID = 0x8D8EA0264F7A1C0EL;
    
    private int updateCounts[];

	/**
	 * Constructs a new throwable with null as its detail message.
	 */
	public KettleDatabaseBatchException()
	{
		super();
	}

	/**
	 * Constructs a new throwable with the specified detail message.
	 * @param message - the detail message. The detail message is saved for later retrieval by the getMessage() method.
	 */
	public KettleDatabaseBatchException(String message)
	{
		super(message);
	}

	/**
	 * Constructs a new throwable with the specified cause and a detail message of (cause==null ? null : cause.toString()) (which typically contains the class and detail message of cause).
	 * @param cause the cause (which is saved for later retrieval by the getCause() method). (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
	 */
	public KettleDatabaseBatchException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * Constructs a new throwable with the specified detail message and cause.
	 * @param message the detail message (which is saved for later retrieval by the getMessage() method).
	 * @param cause the cause (which is saved for later retrieval by the getCause() method). (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
	 */
	public KettleDatabaseBatchException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	/**
     * @return Returns the updateCounts.
     */
    public int[] getUpdateCounts()
    {
        return updateCounts;
    }
    
    /**
     * @param updateCounts The updateCounts to set.
     */
    public void setUpdateCounts(int[] updateCounts)
    {
        this.updateCounts = updateCounts;
    }
}
