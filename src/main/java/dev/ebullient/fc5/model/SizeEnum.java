package dev.ebullient.fc5.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for sizeEnum.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * <p>
 * 
 * <pre>
 * &lt;simpleType name="sizeEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="T"/>
 *     &lt;enumeration value="S"/>
 *     &lt;enumeration value="M"/>
 *     &lt;enumeration value="L"/>
 *     &lt;enumeration value="H"/>
 *     &lt;enumeration value="G"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "sizeEnum")
@XmlEnum
public enum SizeEnum {

    T,
    S,
    M,
    L,
    H,
    G,
    UNKNOWN;

    public String value() {
        return name();
    }

    public static SizeEnum fromValue(String v) {
        return valueOf(v);
    }

}
