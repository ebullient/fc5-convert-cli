//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2022.05.28 at 10:45:07 AM EDT
//

package dev.ebullient.fc5.json2xml.jaxb;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlElementRefs;
import jakarta.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for classType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="classType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="hd" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="proficiency" type="{}abilityAndSkillList"/>
 *         &lt;element name="spellAbility" type="{}abilityEnum"/>
 *         &lt;element name="numSkills" type="{}integer"/>
 *         &lt;element name="autolevel" type="{}autolevelType"/>
 *         &lt;element name="armor" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="weapons" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="tools" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="slotsReset" type="{}resetEnum"/>
 *         &lt;element name="wealth" type="{}rollFormula"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "classType", propOrder = {
        "nameOrHdOrProficiency"
})
public class XmlClassType implements Comparable<Object> {

    @XmlElementRefs({
            @XmlElementRef(name = "autolevel", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "wealth", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "name", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "spellAbility", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "weapons", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "slotsReset", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "numSkills", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "hd", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "armor", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "tools", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "proficiency", type = JAXBElement.class, required = false)
    })
    protected List<JAXBElement<?>> nameOrHdOrProficiency;

    /**
     * Gets the value of the nameOrHdOrProficiency property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the nameOrHdOrProficiency property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getNameOrHdOrProficiency().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link XmlAutolevelType }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link XmlResetEnum }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     *
     */
    public List<JAXBElement<?>> getNameOrHdOrProficiency() {
        if (nameOrHdOrProficiency == null) {
            nameOrHdOrProficiency = new ArrayList<JAXBElement<?>>();
        }
        return this.nameOrHdOrProficiency;
    }

    public int compareTo(Object o) {
        if (this.getClass().equals(o.getClass())) {
            JAXBElement<?> thisName = this.nameOrHdOrProficiency.stream()
                    .filter(e -> "name".equals(e.getName().getLocalPart()))
                    .findFirst().get();
            JAXBElement<?> thatName = ((XmlClassType) o).nameOrHdOrProficiency.stream()
                    .filter(e -> "name".equals(e.getName().getLocalPart()))
                    .findFirst().get();
            return thisName.getValue().toString().compareTo(thatName.getValue().toString());
        }
        return this.getClass().getSimpleName().compareTo(o.getClass().getSimpleName());
    }
}
