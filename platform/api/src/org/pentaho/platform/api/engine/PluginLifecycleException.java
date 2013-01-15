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
 * Copyright 2009 Pentaho Corporation.  All rights reserved.
 *
 */

package org.pentaho.platform.api.engine;

import org.pentaho.platform.api.util.PentahoCheckedChainedException;

public class PluginLifecycleException extends PentahoCheckedChainedException {

  private static final long serialVersionUID = 974L;

  public PluginLifecycleException() {
    super();
  }

  public PluginLifecycleException(final String message) {
    super(message);
  }

  public PluginLifecycleException(final String message, final Throwable reas) {
    super(message, reas);
  }

  public PluginLifecycleException(final Throwable reas) {
    super(reas);
  }
  
}
