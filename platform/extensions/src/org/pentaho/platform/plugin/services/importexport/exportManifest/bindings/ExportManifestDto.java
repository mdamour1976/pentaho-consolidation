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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.platform.web.http.api.resources.JobScheduleRequest;


/**
 * <p>Java class for ExportManifestDto complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ExportManifestDto">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ExportManifestInformation">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="exportDate" type="{http://www.w3.org/2001/XMLSchema}string" default="{date}" />
 *                 &lt;attribute name="exportBy" type="{http://www.w3.org/2001/XMLSchema}string" default="{user}" />
 *                 &lt;attribute name="rootFolder" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="ExportManifestMondrian" type="{http://www.pentaho.com/schema/}ExportManifestMondrian" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ExportManifestMetadata" type="{http://www.pentaho.com/schema/}ExportManifestMetadata" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ExportManifestSchedule" type="{http://www.pentaho.com/schema/}jobScheduleRequest" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ExportManifestDatasource" type="{http://www.pentaho.com/schema/}databaseConnection" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ExportManifestEntity" type="{http://www.pentaho.com/schema/}ExportManifestEntityDto" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExportManifestDto", propOrder = {
    "exportManifestInformation",
    "exportManifestMondrian",
    "exportManifestMetadata",
    "exportManifestSchedule",
    "exportManifestDatasource",
    "exportManifestEntity"
})
public class ExportManifestDto {

    @XmlElement(name = "ExportManifestInformation", required = true)
    protected ExportManifestDto.ExportManifestInformation exportManifestInformation;
    @XmlElement(name = "ExportManifestMondrian")
    protected List<ExportManifestMondrian> exportManifestMondrian;
    @XmlElement(name = "ExportManifestMetadata")
    protected List<ExportManifestMetadata> exportManifestMetadata;
    @XmlElement(name = "ExportManifestSchedule")
    protected List<JobScheduleRequest> exportManifestSchedule;
    @XmlElement(name = "ExportManifestDatasource")
    protected List<DatabaseConnection> exportManifestDatasource;
    @XmlElement(name = "ExportManifestEntity")
    protected List<ExportManifestEntityDto> exportManifestEntity;

    /**
     * Gets the value of the exportManifestInformation property.
     * 
     * @return
     *     possible object is
     *     {@link ExportManifestDto.ExportManifestInformation }
     *     
     */
    public ExportManifestDto.ExportManifestInformation getExportManifestInformation() {
        return exportManifestInformation;
    }

    /**
     * Sets the value of the exportManifestInformation property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExportManifestDto.ExportManifestInformation }
     *     
     */
    public void setExportManifestInformation(ExportManifestDto.ExportManifestInformation value) {
        this.exportManifestInformation = value;
    }

    /**
     * Gets the value of the exportManifestMondrian property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the exportManifestMondrian property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExportManifestMondrian().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ExportManifestMondrian }
     * 
     * 
     */
    public List<ExportManifestMondrian> getExportManifestMondrian() {
        if (exportManifestMondrian == null) {
            exportManifestMondrian = new ArrayList<ExportManifestMondrian>();
        }
        return this.exportManifestMondrian;
    }

    /**
     * Gets the value of the exportManifestMetadata property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the exportManifestMetadata property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExportManifestMetadata().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ExportManifestMetadata }
     * 
     * 
     */
    public List<ExportManifestMetadata> getExportManifestMetadata() {
        if (exportManifestMetadata == null) {
            exportManifestMetadata = new ArrayList<ExportManifestMetadata>();
        }
        return this.exportManifestMetadata;
    }

    /**
     * Gets the value of the exportManifestSchedule property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the exportManifestSchedule property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExportManifestSchedule().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JobScheduleRequest }
     * 
     * 
     */
    public List<JobScheduleRequest> getExportManifestSchedule() {
        if (exportManifestSchedule == null) {
            exportManifestSchedule = new ArrayList<JobScheduleRequest>();
        }
        return this.exportManifestSchedule;
    }

    /**
     * Gets the value of the exportManifestDatasource property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the exportManifestDatasource property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExportManifestDatasource().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DatabaseConnection }
     * 
     * 
     */
    public List<DatabaseConnection> getExportManifestDatasource() {
        if (exportManifestDatasource == null) {
            exportManifestDatasource = new ArrayList<DatabaseConnection>();
        }
        return this.exportManifestDatasource;
    }

    /**
     * Gets the value of the exportManifestEntity property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the exportManifestEntity property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExportManifestEntity().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ExportManifestEntityDto }
     * 
     * 
     */
    public List<ExportManifestEntityDto> getExportManifestEntity() {
        if (exportManifestEntity == null) {
            exportManifestEntity = new ArrayList<ExportManifestEntityDto>();
        }
        return this.exportManifestEntity;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="exportDate" type="{http://www.w3.org/2001/XMLSchema}string" default="{date}" />
     *       &lt;attribute name="exportBy" type="{http://www.w3.org/2001/XMLSchema}string" default="{user}" />
     *       &lt;attribute name="rootFolder" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class ExportManifestInformation {

        @XmlAttribute(name = "exportDate")
        protected String exportDate;
        @XmlAttribute(name = "exportBy")
        protected String exportBy;
        @XmlAttribute(name = "rootFolder", required = true)
        protected String rootFolder;

        /**
         * Gets the value of the exportDate property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getExportDate() {
            if (exportDate == null) {
                return "{date}";
            } else {
                return exportDate;
            }
        }

        /**
         * Sets the value of the exportDate property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setExportDate(String value) {
            this.exportDate = value;
        }

        /**
         * Gets the value of the exportBy property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getExportBy() {
            if (exportBy == null) {
                return "{user}";
            } else {
                return exportBy;
            }
        }

        /**
         * Sets the value of the exportBy property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setExportBy(String value) {
            this.exportBy = value;
        }

        /**
         * Gets the value of the rootFolder property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getRootFolder() {
            return rootFolder;
        }

        /**
         * Sets the value of the rootFolder property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setRootFolder(String value) {
            this.rootFolder = value;
        }

    }

}
