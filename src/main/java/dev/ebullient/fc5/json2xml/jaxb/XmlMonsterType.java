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
 * Java class for monsterType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="monsterType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="size" type="{}sizeEnum"/>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="alignment" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ac" type="{}acType"/>
 *         &lt;element name="hp" type="{}hpType"/>
 *         &lt;element name="speed" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="str" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="dex" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="con" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="int" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="wis" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="cha" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="save" type="{}abilityBonusList"/>
 *         &lt;element name="skill" type="{}skillBonusList"/>
 *         &lt;element name="resist" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="vulnerable" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="immune" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="conditionImmune" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="senses" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="passive" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="languages" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="cr" type="{}crType"/>
 *         &lt;element name="trait" type="{}traitType"/>
 *         &lt;element name="action" type="{}traitType"/>
 *         &lt;element name="legendary" type="{}traitType"/>
 *         &lt;element name="reaction" type="{}traitType"/>
 *         &lt;element name="spells" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="slots" type="{}slotsType"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="environment" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "monsterType", propOrder = {
        "nameOrSizeOrType"
})
public class XmlMonsterType implements Comparable<Object> {

    @XmlElementRefs({
            @XmlElementRef(name = "dex", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "action", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "slots", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "con", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "skill", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "conditionImmune", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "type", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "name", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "legendary", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "save", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "trait", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "environment", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "senses", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "resist", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "hp", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "languages", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "description", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "alignment", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "passive", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "cr", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "str", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "cha", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "reaction", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "wis", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "spells", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "size", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "ac", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "speed", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "immune", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "vulnerable", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "int", type = JAXBElement.class, required = false)
    })
    protected List<JAXBElement<?>> nameOrSizeOrType;

    /**
     * Gets the value of the nameOrSizeOrType property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the nameOrSizeOrType property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getNameOrSizeOrType().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     * {@link JAXBElement }{@code <}{@link XmlTraitType }{@code >}
     * {@link JAXBElement }{@code <}{@link XmlSlotsType }{@code >}
     * {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link XmlTraitType }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link XmlTraitType }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     * {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     * {@link JAXBElement }{@code <}{@link XmlTraitType }{@code >}
     * {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link XmlSizeEnum }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     *
     *
     */
    public List<JAXBElement<?>> getNameOrSizeOrType() {
        if (nameOrSizeOrType == null) {
            nameOrSizeOrType = new ArrayList<JAXBElement<?>>();
        }
        return this.nameOrSizeOrType;
    }

    public int compareTo(Object o) {
        if (this.getClass().equals(o.getClass())) {
            JAXBElement<?> thisName = this.nameOrSizeOrType.stream()
                    .filter(e -> "name".equals(e.getName().getLocalPart()))
                    .findFirst().get();
            JAXBElement<?> thatName = ((XmlMonsterType) o).nameOrSizeOrType.stream()
                    .filter(e -> "name".equals(e.getName().getLocalPart()))
                    .findFirst().get();
            return thisName.getValue().toString().compareTo(thatName.getValue().toString());
        }
        return this.getClass().getSimpleName().compareTo(o.getClass().getSimpleName());
    }
}
