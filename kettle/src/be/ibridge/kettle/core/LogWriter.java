 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

 

package be.ibridge.kettle.core;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;

import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleFileException;
import be.ibridge.kettle.core.logging.Log4jConsoleAppender;
import be.ibridge.kettle.core.logging.Log4jFileAppender;
import be.ibridge.kettle.core.logging.Log4jKettleLayout;
import be.ibridge.kettle.core.logging.Log4jMessage;
import be.ibridge.kettle.core.logging.Log4jStringAppender;


/**
 * This class handles the logging.
 * 
 * @author Matt
 * @since 25-04-2003
 *
 */
public class LogWriter 
{
	private static LogWriter logWriter;
	
	public static final int LOG_LEVEL_ERROR      =  0;
	public static final int LOG_LEVEL_NOTHING    =  1;
	public static final int LOG_LEVEL_MINIMAL    =  2;
	public static final int LOG_LEVEL_BASIC      =  3;
	public static final int LOG_LEVEL_DETAILED   =  4;
	public static final int LOG_LEVEL_DEBUG      =  5;
	public static final int LOG_LEVEL_ROWLEVEL   =  6;
	
	public static final String logLevelDescription[] = 
		{
			"Error",
			"Nothing",
			"Minimal",
			"Basic",
			"Detailed",
			"Debug",
			"Rowlevel"
		};

	public static final String log_level_desc_long[] = 
		{
			"Error logging only",
			"Nothing at all",
			"Minimal logging",
			"Basic logging",
			"Detailed logging",
			"Debugging",
			"Rowlevel (very detailed)"
		};
	
	// String...
	private int type;
	private int level;
	private String filter;
    
    // Log4j
    private Logger               rootLogger;
    private Log4jConsoleAppender consoleAppender;
    private Log4jStringAppender  stringAppender;
    private Log4jFileAppender    fileAppender;
    
    public static final Log4jKettleLayout KETTLE_LAYOUT = new Log4jKettleLayout(true);

    private File realFilename;

	public static final LogWriter getInstance()
	{
		if (logWriter!=null) return logWriter;
		
        return getInstance(LOG_LEVEL_BASIC);
	}
	
	public static final LogWriter getInstance(int lvl)
	{
		if (logWriter != null)
        {
            logWriter.setLogLevel(lvl);
            return logWriter;
        }
		
		logWriter = new LogWriter(lvl);
		
		return logWriter;
	}
    
    private LogWriter()
    {
        rootLogger = Logger.getRootLogger();
        
        // Create the console appender, don't add it yet!
        consoleAppender = new Log4jConsoleAppender();
        consoleAppender.setLayout(KETTLE_LAYOUT);
        consoleAppender.setName("AppendToConsole");

        // Create the string appender, don't add it yet!
        stringAppender  = new Log4jStringAppender();
        stringAppender.setLayout(KETTLE_LAYOUT);
        stringAppender.setName("AppendToString");
    }

	// Default: screen --> out
	private LogWriter(int lvl)
	{
        this();

        // Check if there already is a console appender (ConsoleAppender) (in the app server for example)
        // 
        boolean found = false;
        Enumeration appenders = rootLogger.getAllAppenders();
        
        while(appenders.hasMoreElements())
        {
            Object appender = appenders.nextElement();
            if (appender instanceof ConsoleAppender || appender instanceof Log4jConsoleAppender) found=true;
        }

        if (!found)
        {
            rootLogger.addAppender(consoleAppender);
        }
        
		level  = lvl;
		filter = null;
	}

	/**
	 * Get a new log instance for the specified file if it is not open yet! 
	 * @param filename The log file to open
     * @param exact is this an exact filename (false: prefix of name in temp directory)
	 * @param level The log level
	 * @return the LogWriter object
	 */
	public static final LogWriter getInstance(String filename, boolean exact, int level) throws KettleException
	{
		if (logWriter != null) 
	    {
			// OK, see if we have a file appender already for this 
			if (logWriter.rootLogger.getAppender(LogWriter.createFileAppenderName(filename, exact))==null)
			{
				logWriter.fileAppender = createFileAppender(filename, exact);
                logWriter.addAppender(logWriter.fileAppender);
			}
			return logWriter;
	    }
		
		logWriter = new LogWriter(filename, exact, level);
		return logWriter;
	}
	
	private LogWriter(String filename, boolean exact, int level)
	{
        this();
        
		this.level = level;
                
		try
		{
            fileAppender = createFileAppender(filename, exact); 
			addAppender(fileAppender);
		}
		catch(Exception e)
		{
			System.out.println("ERROR OPENING LOG FILE ["+filename+"] --> "+e.toString());
		}
	}
	
	public static final Log4jFileAppender createFileAppender(String filename, boolean exact) throws KettleException
	{
		try
		{
            File file;
	        if (!exact)
	        {
	            file = File.createTempFile(filename+".", ".log");
	            file.deleteOnExit();
	        }
	        else
	        {
	            file = new File(filename);
	        }
	        File realFile = file.getAbsoluteFile();
	
	        Log4jFileAppender appender = new Log4jFileAppender(realFile);
	        appender.setLayout(KETTLE_LAYOUT);
	        appender.setName(LogWriter.createFileAppenderName(filename, exact));
            
            return appender;
		}
		catch(IOException e)
		{
			throw new KettleFileException("Unable to add Kettle file appender to Log4J", e);
		}
    }
	
	public static final String createFileAppenderName(String filename, boolean exact)
	{
		if (!exact)
		{
			return "<temp file> : "+filename;
		}
		else
		{
			return filename;
		}
	}

	public int getType()
	{
		return type;
	}
	
	public void setType(int type)
	{
		this.type = type;
	}

	public boolean close()
	{
		boolean retval=true;
		try
		{
			// Close all appenders...
            Logger logger = Logger.getRootLogger();
            Enumeration loggers = logger.getAllAppenders();
            while (loggers.hasMoreElements())
            {
                Appender appender = (Appender) loggers.nextElement();
                appender.close();
            }
            rootLogger.removeAllAppenders();
            logWriter=null;
		}
		catch(Exception e) 
		{ 
			retval=false; 
		}
		
		return retval;
	}
	
	public void setLogLevel(int lvl)
	{
		level = lvl;
	}

	public void setLogLevel(String lvl)
	{
		level = getLogLevel(lvl);
	}
	
	public int getLogLevel()
	{
		return level;
	}

	public String getLogLevelDesc()
	{
		return logLevelDescription[level];
	}
	
	public void enableTime()
	{
        KETTLE_LAYOUT.setTimeAdded(true);
	}

	public void disableTime()
	{
		KETTLE_LAYOUT.setTimeAdded(false);
	}
	
	public boolean getTime()
	{
        return KETTLE_LAYOUT.isTimeAdded();
	}

	public void setTime(boolean tim)
	{
        KETTLE_LAYOUT.setTimeAdded(tim);
	}

	public void println(int lvl, String msg)
	{
		println(lvl, "General", msg);
	}
	
	public void println(int lvl, String subj, String msg)
	{
        String subject = subj;
        if (subject==null) subject="Kettle";
        
		// Are the message filtered?
		if (filter!=null && filter.length()>0)
        {
            if (subject.indexOf(filter)<0 && msg.indexOf(filter)<0)
            {
                return; // "filter" not found in row: don't show!
            }
        }
		    
		if (level==0) return;  // Nothing, not even errors...
		if (level<lvl) return; // not for our eyes.
		
        
        Logger logger = Logger.getLogger(subject);
        
        Log4jMessage message = new Log4jMessage(msg, subject, lvl);
        
        switch(level)
        {
        case LOG_LEVEL_ERROR:    logger.error(message); break;
        case LOG_LEVEL_ROWLEVEL: 
        case LOG_LEVEL_DEBUG:    logger.debug(message); break;
        default:                 logger.info(message); break;
        }
	}
	
	public void logMinimal(String subject, String message)  { println(LOG_LEVEL_MINIMAL, subject, message) ; }
    public void logBasic(String subject, String message)    { println(LOG_LEVEL_BASIC, subject, message) ; }
	public void logDetailed(String subject, String message) { println(LOG_LEVEL_DETAILED, subject, message); }
	public void logDebug(String subject, String message)    { println(LOG_LEVEL_DEBUG, subject, message); }
	public void logRowlevel(String subject, String message) { println(LOG_LEVEL_ROWLEVEL, subject, message); }
	public void logError(String subject, String message)    { println(LOG_LEVEL_ERROR, subject, message); }
	
    /**
     *  @deprecated  Please get the file appender yourself and work from there.
     *   
     */
	public Object getStream() 
    {
		return null; // Will fail so that people fix this.
	}
	
	public void setFilter(String filter)
	{
        this.filter=filter;
	}
	
	public String getFilter()
	{
		return filter;
	}

	public static final int getLogLevel(String lvl)
	{
		if (lvl==null) return LOG_LEVEL_ERROR;
		for (int i=0;i<logLevelDescription.length;i++)
		{
			if (logLevelDescription[i].equalsIgnoreCase(lvl)) return i;
		}
		for (int i=0;i<log_level_desc_long.length;i++)
		{
			if (log_level_desc_long[i].equalsIgnoreCase(lvl)) return i;
		}
		
		return LOG_LEVEL_BASIC;
	}

	public static final String getLogLevelDesc(int l)
	{
		if (l<0 || l>=logLevelDescription.length) return logLevelDescription[LOG_LEVEL_BASIC];
		return logLevelDescription[l];
	}
	
    /**
     * Please try to get the file appender yourself using the static constructor and work from there
     */
	public FileInputStream getFileInputStream() throws IOException
	{
		return new FileInputStream(fileAppender.getFile());
	}
    
    /**
     * Get the file input stream for a certain appender.
     * The appender is looked up using the filename
     * @param filename The exact filename (with path: c:\temp\logfile.txt) or just a filename (spoon.log)
     * @param exact true if this is the exact filename or just the last part of the complete path.
     * @return The file input stream of the appender
     * @throws IOException in case the appender ocan't be found
     */
    public FileInputStream getFileInputStream(String filename, boolean exact) throws IOException
    {
        Logger logger = Logger.getRootLogger();
        Appender appender = logger.getAppender(createFileAppenderName(filename, exact));
        if (appender==null)
        {
            throw new IOException("Unable to find appender for file: "+filename+" (exact="+exact+")");
        }
        return new FileInputStream(((Log4jFileAppender)appender).getFile());
    }
    
    public boolean isBasic()
    {
        return level>=LOG_LEVEL_BASIC;
    }

    public boolean isDetailed()
    {
        return level>=LOG_LEVEL_DETAILED;
    }

    public boolean isDebug()
    {
        return level>=LOG_LEVEL_DEBUG;
    }

    public boolean isRowLevel()
    {
        return level>=LOG_LEVEL_ROWLEVEL;
    }

    /**
     * @return Returns the realFilename.
     */
    public File getRealFilename()
    {
        return realFilename;
    }

    /**
     * @param realFilename The realFilename to set.
     */
    public void setRealFilename(File realFilename)
    {
        this.realFilename = realFilename;
    }
    

    public void startStringCapture()
    {
        Logger logger = Logger.getRootLogger();
        logger.addAppender(stringAppender);
    }
    

    public void endStringCapture()
    {
        Logger logger = Logger.getRootLogger();
        logger.removeAppender(stringAppender);
    }

    /**
     * @return The logging text from since startStringCapture() is called until endStringCapture().
     */
    public String getString()
    {
        return stringAppender.getBuffer().toString();
    }

    public void setString(String string)
    {
        stringAppender.setBuffer(new StringBuffer(string));
    }

    public void addAppender(Log4jFileAppender appender)
    {
        Logger logger = Logger.getRootLogger();
        logger.addAppender(appender);
    }
    
    public void removeAppender(Appender appender)
    {
        Logger logger = Logger.getRootLogger();
        logger.removeAppender(appender);
    }

    public Log4jConsoleAppender getConsoleAppender()
    {
        return consoleAppender;
    }
    
    public Log4jStringAppender getStringAppender()
    {
        return stringAppender;
    }
}
