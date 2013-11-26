/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.07.25 at 11:25:28 AM EDT 
//

package org.pentaho.platform.plugin.services.importexport.exportManifest.bindings;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for qualifiedDayOfWeek complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="qualifiedDayOfWeek">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="dayOfWeek" type="{http://www.pentaho.com/schema/}dayOfWeek" minOccurs="0"/>
 *         &lt;element name="qualifier" type="{http://www.pentaho.com/schema/}dayOfWeekQualifier" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "qualifiedDayOfWeek", propOrder = { "dayOfWeek", "qualifier" } )
public class QualifiedDayOfWeek {

  protected DayOfWeek dayOfWeek;
  protected DayOfWeekQualifier qualifier;

  /**
   * Gets the value of the dayOfWeek property.
   * 
   * @return possible object is {@link DayOfWeek }
   * 
   */
  public DayOfWeek getDayOfWeek() {
    return dayOfWeek;
  }

  /**
   * Sets the value of the dayOfWeek property.
   * 
   * @param value
   *          allowed object is {@link DayOfWeek }
   * 
   */
  public void setDayOfWeek( DayOfWeek value ) {
    this.dayOfWeek = value;
  }

  /**
   * Gets the value of the qualifier property.
   * 
   * @return possible object is {@link DayOfWeekQualifier }
   * 
   */
  public DayOfWeekQualifier getQualifier() {
    return qualifier;
  }

  /**
   * Sets the value of the qualifier property.
   * 
   * @param value
   *          allowed object is {@link DayOfWeekQualifier }
   * 
   */
  public void setQualifier( DayOfWeekQualifier value ) {
    this.qualifier = value;
  }

}
