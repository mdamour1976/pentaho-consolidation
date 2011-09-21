/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2006 - 2008 Pentaho Corporation.  All rights reserved.
 *
 * Created Dec 19, 2008 
 * @author aphillips
 */

package org.pentaho.platform.api.engine;

/**
 * An exception raised when an {@link IPlatformPlugin} fails to register.
 * @author aphillips
 */
public class PlatformPluginRegistrationException extends Exception {

  private static final long serialVersionUID = 1791609786938478691L;
  
  public PlatformPluginRegistrationException(String message, Throwable cause) {
    super(message, cause);
  }
  public PlatformPluginRegistrationException(String message) {
    super(message);
  }
}
