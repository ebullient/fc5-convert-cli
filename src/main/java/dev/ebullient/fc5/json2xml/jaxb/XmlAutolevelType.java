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
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlElementRefs;
import jakarta.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for autolevelType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="autolevelType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="feature" type="{}featureType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="slots" type="{}slotsType" minOccurs="0"/>
 *         &lt;element name="counter" type="{}counterType" minOccurs="0"/>
 *       &lt;/choice>
 *       &lt;attribute name="level" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="scoreImprovement" type="{}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "autolevelType", propOrder = {
        "content"
})
public class XmlAutolevelType {

    @XmlElementRefs({
            @XmlElementRef(name = "slots", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "feature", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "counter", type = JAXBElement.class, required = false)
    })
    protected List<JAXBElement<?>> content;
    @XmlAttribute(name = "level", required = true)
    protected BigInteger level;
    @XmlAttribute(name = "scoreImprovement")
    protected String scoreImprovement;

    /**
     * Gets the value of the content property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the content property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getContent().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * {@link JAXBElement }{@code <}{@link XmlFeatureType }{@code >}
     * {@link JAXBElement }{@code <}{@link XmlCounterType }{@code >}
     * {@link JAXBElement }{@code <}{@link XmlSlotsType }{@code >}
     *
     *
     */
    public List<JAXBElement<?>> getContent() {
        if (content == null) {
            content = new ArrayList<JAXBElement<?>>();
        }
        return this.content;
    }

    /**
     * Gets the value of the level property.
     *
     * @return
     *         possible object is
     *         {@link BigInteger }
     *
     */
    public BigInteger getLevel() {
        return level;
    }

    /**
     * Sets the value of the level property.
     *
     * @param value
     *        allowed object is
     *        {@link BigInteger }
     *
     */
    public void setLevel(BigInteger value) {
        this.level = value;
    }

    /**
     * Gets the value of the scoreImprovement property.
     *
     * @return
     *         possible object is
     *         {@link String }
     *
     */
    public String getScoreImprovement() {
        return scoreImprovement;
    }

    /**
     * Sets the value of the scoreImprovement property.
     *
     * @param value
     *        allowed object is
     *        {@link String }
     *
     */
    public void setScoreImprovement(String value) {
        this.scoreImprovement = value;
    }

}
