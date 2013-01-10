//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.01.10 at 09:40:29 AM EST 
//


package org.pentaho.platform.repository2.unified.ExportManifest;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RepositoryFileAclDto complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RepositoryFileAclDto">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="aces" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="permissions" type="{http://www.w3.org/2001/XMLSchema}integer" maxOccurs="unbounded" minOccurs="0"/>
 *                   &lt;element name="recipient" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="recipientType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="entriesInheriting" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="owner" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ownerType" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RepositoryFileAclDto", propOrder = {
    "aces",
    "entriesInheriting",
    "owner",
    "ownerType"
})
public class RepositoryFileAclDto {

    @XmlElement(required = true)
    protected List<RepositoryFileAclDto.Aces> aces;
    protected boolean entriesInheriting;
    @XmlElement(required = true)
    protected String owner;
    @XmlElement(required = true)
    protected BigInteger ownerType;

    /**
     * Gets the value of the aces property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the aces property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAces().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RepositoryFileAclDto.Aces }
     * 
     * 
     */
    public List<RepositoryFileAclDto.Aces> getAces() {
        if (aces == null) {
            aces = new ArrayList<RepositoryFileAclDto.Aces>();
        }
        return this.aces;
    }

    /**
     * Gets the value of the entriesInheriting property.
     * 
     */
    public boolean isEntriesInheriting() {
        return entriesInheriting;
    }

    /**
     * Sets the value of the entriesInheriting property.
     * 
     */
    public void setEntriesInheriting(boolean value) {
        this.entriesInheriting = value;
    }

    /**
     * Gets the value of the owner property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Sets the value of the owner property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOwner(String value) {
        this.owner = value;
    }

    /**
     * Gets the value of the ownerType property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getOwnerType() {
        return ownerType;
    }

    /**
     * Sets the value of the ownerType property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setOwnerType(BigInteger value) {
        this.ownerType = value;
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
     *       &lt;sequence>
     *         &lt;element name="permissions" type="{http://www.w3.org/2001/XMLSchema}integer" maxOccurs="unbounded" minOccurs="0"/>
     *         &lt;element name="recipient" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="recipientType" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "permissions",
        "recipient",
        "recipientType"
    })
    public static class Aces {

        protected List<BigInteger> permissions;
        @XmlElement(required = true)
        protected String recipient;
        @XmlElement(required = true)
        protected String recipientType;

        /**
         * Gets the value of the permissions property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the permissions property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getPermissions().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link BigInteger }
         * 
         * 
         */
        public List<BigInteger> getPermissions() {
            if (permissions == null) {
                permissions = new ArrayList<BigInteger>();
            }
            return this.permissions;
        }

        /**
         * Gets the value of the recipient property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getRecipient() {
            return recipient;
        }

        /**
         * Sets the value of the recipient property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setRecipient(String value) {
            this.recipient = value;
        }

        /**
         * Gets the value of the recipientType property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getRecipientType() {
            return recipientType;
        }

        /**
         * Sets the value of the recipientType property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setRecipientType(String value) {
            this.recipientType = value;
        }

    }

}
