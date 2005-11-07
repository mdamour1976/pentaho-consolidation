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
 

package be.ibridge.kettle.trans.step;

/**
 * This class is the base class for the StepDataInterface and 
 * contains the methods to set and retrieve the status of the step data.
 * 
 * @author Matt
 * @since 20-jan-2005
 */
public class BaseStepData
{
	private int status;

	/**
	 * 
	 */
	public BaseStepData()
	{
		status = StepDataInterface.STATUS_EMPTY;
	}
	
	/**
	 * Set the status of the step data.
	 * @param status the new status.
	 */
	public void setStatus(int status)
	{
		this.status = status;
	}
	
	/**
	 * Get the status of this step data.
	 * @return the status of the step data
	 */
	public int getStatus()
	{
		return status;
	}

	public boolean isEmpty()         { return status == StepDataInterface.STATUS_EMPTY;    }
	public boolean isInitialising()  { return status == StepDataInterface.STATUS_INIT;     }
	public boolean isRunning()       { return status == StepDataInterface.STATUS_RUNNING;  }
	public boolean isIdle()          { return status == StepDataInterface.STATUS_IDLE;     }
	public boolean isFinished()      { return status == StepDataInterface.STATUS_FINISHED; }
	public boolean isDisposed()      { return status == StepDataInterface.STATUS_DISPOSED; }

}
