//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2022.05.28 at 10:45:07 AM EDT
//

package dev.ebullient.fc5.xml;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for itemType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="itemType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="type" type="{}itemEnum"/>
 *         &lt;element name="magic" type="{}boolean"/>
 *         &lt;element name="detail" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="weight" type="{}double"/>
 *         &lt;element name="text" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="roll" type="{}roll"/>
 *         &lt;element name="value" type="{}double"/>
 *         &lt;element name="modifier" type="{}modifierType"/>
 *         &lt;element name="ac" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="strength" type="{}integer"/>
 *         &lt;element name="stealth" type="{}boolean"/>
 *         &lt;element name="dmg1" type="{}roll"/>
 *         &lt;element name="dmg2" type="{}roll"/>
 *         &lt;element name="dmgType" type="{}damageEnum"/>
 *         &lt;element name="property" type="{}propertyList"/>
 *         &lt;element name="range" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "itemType", propOrder = {
        "nameOrTypeOrMagic"
})
public class XmlItemType {

    @XmlElementRefs({
            @XmlElementRef(name = "name", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "text", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "dmg2", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "dmgType", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "roll", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "value", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "magic", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "modifier", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "ac", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "detail", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "dmg1", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "type", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "strength", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "range", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "weight", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "stealth", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "property", type = JAXBElement.class, required = false)
    })
    protected List<JAXBElement<?>> nameOrTypeOrMagic;

    /**
     * Gets the value of the nameOrTypeOrMagic property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the nameOrTypeOrMagic property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getNameOrTypeOrMagic().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link XmlModifierType }{@code >}
     * {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link XmlItemEnum }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     *
     */
    public List<JAXBElement<?>> getNameOrTypeOrMagic() {
        if (nameOrTypeOrMagic == null) {
            nameOrTypeOrMagic = new ArrayList<JAXBElement<?>>();
        }
        return this.nameOrTypeOrMagic;
    }
}
