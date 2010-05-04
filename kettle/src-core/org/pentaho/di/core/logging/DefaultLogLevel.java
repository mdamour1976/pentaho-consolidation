/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.core.logging;

/**
 * Retains the default log level during execution
 * 
 * @author matt
 */
public class DefaultLogLevel {
  private static DefaultLogLevel defaultLogLevel;
  
  private LogLevel logLevel;
  
  private DefaultLogLevel() {
    logLevel = LogLevel.BASIC;
  }
  
  private static DefaultLogLevel getInstance() {
    if (defaultLogLevel==null) {
      defaultLogLevel = new DefaultLogLevel(); 
    }
    return defaultLogLevel;
  }
  
  /**
   * @return The default log level for this application
   */
  public static LogLevel getLogLevel() {
    return getInstance().logLevel;
  }
  
  /**
   * @param logLevel Set the default log level for this application
   */
  public static void setLogLevel(LogLevel logLevel) {
    getInstance().logLevel = logLevel;
  }
  
  
}
