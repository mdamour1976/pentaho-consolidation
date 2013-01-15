/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved. 
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
 *
 * @created Feb 2, 2009 
 * @author wseyler
 */

package org.pentaho.platform.api.engine;
  /**
   * A "something" that refers to a session.
   *
   * This is mainly used to ensure that sessions don't leak.
   * 
   * @author <a href="mailto:andreas.kohn@fredhopper.com">Andreas Kohn</a>
   * @see BISERVER-2639
   */
  /* TODO: should provide a getSession(), or ideally a 'clearSessionIfCurrent(IPentahoSession s)' to facilitate
   * easy cleaning.
   */
public interface ISessionContainer {
  /**
   * Set the session for this session container.
   * 
   * @param sess
   *            The IPentahoSession to set
   */
  public void setSession(IPentahoSession sess);
}
