//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.07.18 at 02:56:23 PM EDT 
//


package org.pentaho.platform.repository2.unified.exportManifest.bindings;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for scheduleLifetime complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="scheduleLifetime">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="start" type="{http://www.pentaho.com/schema/}scheduleTime"/>
 *         &lt;element name="stop" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "scheduleLifetime", propOrder = {
    "start",
    "stop"
})
public class ScheduleLifetime {

    @XmlElement(required = true)
    protected String start;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar stop;

    /**
     * Gets the value of the start property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStart() {
        return start;
    }

    /**
     * Sets the value of the start property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStart(String value) {
        this.start = value;
    }

    /**
     * Gets the value of the stop property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getStop() {
        return stop;
    }

    /**
     * Sets the value of the stop property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setStop(XMLGregorianCalendar value) {
        this.stop = value;
    }

}
