//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.01.10 at 09:40:29 AM EST 
//


package org.pentaho.platform.repository2.unified.ExportManifest;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the textxsd package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ExportManifestDto_QNAME = new QName("http://www.example.org/ExportManifest/", "ExportManifestDto");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: textxsd
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link RepositoryFileAclDto.Aces }
     * 
     */
    public RepositoryFileAclDto.Aces createRepositoryFileAclDtoAces() {
        return new RepositoryFileAclDto.Aces();
    }

    /**
     * Create an instance of {@link ExportManifestDto }
     * 
     */
    public ExportManifestDto createExportManifestDto() {
        return new ExportManifestDto();
    }

    /**
     * Create an instance of {@link RepositoryFileAclDto }
     * 
     */
    public RepositoryFileAclDto createRepositoryFileAclDto() {
        return new RepositoryFileAclDto();
    }

    /**
     * Create an instance of {@link ExportManifestDto.ExportManifestEntity }
     * 
     */
    public ExportManifestDto.ExportManifestEntity createExportManifestDtoExportManifestEntity() {
        return new ExportManifestDto.ExportManifestEntity();
    }

    /**
     * Create an instance of {@link CustomProperty }
     * 
     */
    public CustomProperty createCustomProperty() {
        return new CustomProperty();
    }

    /**
     * Create an instance of {@link ExportManifestPropertyDto }
     * 
     */
    public ExportManifestPropertyDto createExportManifestPropertyDto() {
        return new ExportManifestPropertyDto();
    }

    /**
     * Create an instance of {@link RepositoryFileDto }
     * 
     */
    public RepositoryFileDto createRepositoryFileDto() {
        return new RepositoryFileDto();
    }

    /**
     * Create an instance of {@link ExportManifestDto.ManifestInformation }
     * 
     */
    public ExportManifestDto.ManifestInformation createExportManifestDtoManifestInformation() {
        return new ExportManifestDto.ManifestInformation();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExportManifestDto }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.example.org/ExportManifest/", name = "ExportManifestDto")
    public JAXBElement<ExportManifestDto> createExportManifestDto(ExportManifestDto value) {
        return new JAXBElement<ExportManifestDto>(_ExportManifestDto_QNAME, ExportManifestDto.class, null, value);
    }

}
