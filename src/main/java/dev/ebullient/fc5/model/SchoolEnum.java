package dev.ebullient.fc5.model;

import io.quarkus.qute.TemplateData;

/**
 * <p>
 * Java class for schoolEnum.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * <p>
 * 
 * <pre>
 * &lt;simpleType name="schoolEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="A"/>
 *     &lt;enumeration value="C"/>
 *     &lt;enumeration value="D"/>
 *     &lt;enumeration value="EN"/>
 *     &lt;enumeration value="EV"/>
 *     &lt;enumeration value="I"/>
 *     &lt;enumeration value="N"/>
 *     &lt;enumeration value="T"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@TemplateData
public enum SchoolEnum {

    A,
    C,
    D,
    EN,
    EV,
    I,
    N,
    T,
    UNKNOWN;

    public String value() {
        return name();
    }

    public static SchoolEnum fromValue(String v) {
        return valueOf(v);
    }

}
