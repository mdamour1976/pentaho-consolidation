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
 * Copyright 2010 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Jan, 2010 
 * @author James Dixon
 * 
 */
package org.pentaho.commons.util.repository.type;

public class PropertyHtml extends PropertyString {

  public PropertyHtml(String name) {
    super( name, new PropertyType( PropertyType.STRING ) );
  }
  
  public PropertyHtml( String name, String value ) {
    super( name, new PropertyType( PropertyType.STRING ) );
    setValue( value );
  }
}
