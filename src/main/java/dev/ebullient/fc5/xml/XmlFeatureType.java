//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2022.05.28 at 10:45:07 AM EDT
//

package dev.ebullient.fc5.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for featureType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="featureType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="text" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="special" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="modifier" type="{}modifierType"/>
 *         &lt;element name="proficiency" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/choice>
 *       &lt;attribute name="optional" type="{}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "featureType", propOrder = {
        "nameOrTextOrSpecial"
})
public class XmlFeatureType {

    @XmlElementRefs({
            @XmlElementRef(name = "name", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "special", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "modifier", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "proficiency", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "text", type = JAXBElement.class, required = false)
    })
    protected List<JAXBElement<?>> nameOrTextOrSpecial;
    @XmlAttribute(name = "optional")
    protected String optional;

    /**
     * Gets the value of the nameOrTextOrSpecial property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the nameOrTextOrSpecial property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getNameOrTextOrSpecial().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link XmlModifierType }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     *
     */
    public List<JAXBElement<?>> getNameOrTextOrSpecial() {
        if (nameOrTextOrSpecial == null) {
            nameOrTextOrSpecial = new ArrayList<JAXBElement<?>>();
        }
        return this.nameOrTextOrSpecial;
    }

    /**
     * Gets the value of the optional property.
     *
     * @return
     *         possible object is
     *         {@link String }
     *
     */
    public String getOptional() {
        return optional;
    }

    /**
     * Sets the value of the optional property.
     *
     * @param value
     *        allowed object is
     *        {@link String }
     *
     */
    public void setOptional(String value) {
        this.optional = value;
    }

    public int compareTo(Object o) {
        if (this.getClass().equals(o.getClass())) {
            JAXBElement<?> thisName = this.nameOrTextOrSpecial.stream()
                    .filter(e -> "name".equals(e.getName().getLocalPart()))
                    .findFirst().get();
            JAXBElement<?> thatName = ((XmlFeatureType) o).nameOrTextOrSpecial.stream()
                    .filter(e -> "name".equals(e.getName().getLocalPart()))
                    .findFirst().get();
            return thisName.getValue().toString().compareTo(thatName.getValue().toString());
        }
        return this.getClass().getSimpleName().compareTo(o.getClass().getSimpleName());
    }
}
