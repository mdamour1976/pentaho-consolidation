//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.07.18 at 02:56:23 PM EDT 
//


package org.pentaho.platform.repository2.unified.exportManifest.bindings;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ExportManifestMondrian complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ExportManifestMondrian">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="catalogName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="xmlaEnabled" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="parameters" type="{http://www.pentaho.com/schema/}Parameters" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="file" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExportManifestMondrian", propOrder = {
    "catalogName",
    "xmlaEnabled",
    "parameters"
})
public class ExportManifestMondrian {

    @XmlElement(required = true)
    protected String catalogName;
    protected boolean xmlaEnabled;
    @XmlElement(type = org.pentaho.platform.repository2.unified.exportManifest.bindings.Parameters.class)
    protected org.pentaho.platform.repository2.unified.exportManifest.Parameters parameters;
    @XmlAttribute(name = "file")
    protected String file;

    /**
     * Gets the value of the catalogName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCatalogName() {
        return catalogName;
    }

    /**
     * Sets the value of the catalogName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCatalogName(String value) {
        this.catalogName = value;
    }

    /**
     * Gets the value of the xmlaEnabled property.
     * 
     */
    public boolean isXmlaEnabled() {
        return xmlaEnabled;
    }

    /**
     * Sets the value of the xmlaEnabled property.
     * 
     */
    public void setXmlaEnabled(boolean value) {
        this.xmlaEnabled = value;
    }

    /**
     * Gets the value of the parameters property.
     * 
     * @return
     *     possible object is
     *     {@link org.pentaho.platform.repository2.unified.exportManifest.bindings.Parameters }
     *     
     */
    public org.pentaho.platform.repository2.unified.exportManifest.Parameters getParameters() {
        return parameters;
    }

    /**
     * Sets the value of the parameters property.
     * 
     * @param value
     *     allowed object is
     *     {@link org.pentaho.platform.repository2.unified.exportManifest.bindings.Parameters }
     *     
     */
    public void setParameters(org.pentaho.platform.repository2.unified.exportManifest.Parameters value) {
        this.parameters = value;
    }

    /**
     * Gets the value of the file property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFile() {
        return file;
    }

    /**
     * Sets the value of the file property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFile(String value) {
        this.file = value;
    }

}
