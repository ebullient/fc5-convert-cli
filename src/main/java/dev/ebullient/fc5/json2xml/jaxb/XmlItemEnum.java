//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2022.05.28 at 10:45:07 AM EDT
//

package dev.ebullient.fc5.json2xml.jaxb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

import dev.ebullient.fc5.pojo.ItemEnum;

/**
 * <p>
 * Java class for itemEnum.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * <p>
 *
 * <pre>
 * &lt;simpleType name="itemEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="LA"/>
 *     &lt;enumeration value="MA"/>
 *     &lt;enumeration value="HA"/>
 *     &lt;enumeration value="S"/>
 *     &lt;enumeration value="M"/>
 *     &lt;enumeration value="R"/>
 *     &lt;enumeration value="A"/>
 *     &lt;enumeration value="RD"/>
 *     &lt;enumeration value="ST"/>
 *     &lt;enumeration value="WD"/>
 *     &lt;enumeration value="RG"/>
 *     &lt;enumeration value="P"/>
 *     &lt;enumeration value="SC"/>
 *     &lt;enumeration value="W"/>
 *     &lt;enumeration value="G"/>
 *     &lt;enumeration value="$"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 *
 */
@XmlType(name = "itemEnum")
@XmlEnum
public enum XmlItemEnum {

    $,
    A,
    G,
    HA,
    LA,
    M,
    MA,
    P,
    R,
    RD,
    RG,
    S,
    SC,
    ST,
    W,
    WD;

    public String value() {
        return name();
    }

    public static XmlItemEnum mapValue(ItemEnum v) {
        return valueOf(v.getEncodedValue());
    }
}
