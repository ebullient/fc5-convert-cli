//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2022.05.28 at 10:45:07 AM EDT
//

package dev.ebullient.fc5.json2xml.jaxb;

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
 * Java class for featType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="featType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="prerequisite" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="special" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="text" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="proficiency" type="{}abilityAndSkillList" minOccurs="0"/>
 *         &lt;element name="modifier" type="{}modifierType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "featType", propOrder = {
        "nameOrPrerequisiteOrSpecial"
})
public class XmlFeatType implements Comparable<Object> {

    @XmlElementRefs({
            @XmlElementRef(name = "special", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "name", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "proficiency", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "modifier", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "prerequisite", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "text", type = JAXBElement.class, required = false)
    })
    protected List<JAXBElement<?>> nameOrPrerequisiteOrSpecial;

    /**
     * Gets the value of the nameOrPrerequisiteOrSpecial property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the nameOrPrerequisiteOrSpecial property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getNameOrPrerequisiteOrSpecial().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link XmlModifierType }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     *
     */
    public List<JAXBElement<?>> getNameOrPrerequisiteOrSpecial() {
        if (nameOrPrerequisiteOrSpecial == null) {
            nameOrPrerequisiteOrSpecial = new ArrayList<JAXBElement<?>>();
        }
        return this.nameOrPrerequisiteOrSpecial;
    }

    public int compareTo(Object o) {
        if (this.getClass().equals(o.getClass())) {
            JAXBElement<?> thisName = this.nameOrPrerequisiteOrSpecial.stream()
                    .filter(e -> "name".equals(e.getName().getLocalPart()))
                    .findFirst().get();
            JAXBElement<?> thatName = ((XmlFeatType) o).nameOrPrerequisiteOrSpecial.stream()
                    .filter(e -> "name".equals(e.getName().getLocalPart()))
                    .findFirst().get();
            return thisName.getValue().toString().compareTo(thatName.getValue().toString());
        }
        return this.getClass().getSimpleName().compareTo(o.getClass().getSimpleName());
    }
}
