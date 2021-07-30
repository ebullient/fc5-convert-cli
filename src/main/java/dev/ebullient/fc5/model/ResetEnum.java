package dev.ebullient.fc5.model;

/**
 * <p>
 * Java class for resetEnum.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * <p>
 * 
 * <pre>
 * &lt;simpleType name="resetEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="L"/>
 *     &lt;enumeration value="S"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
public enum ResetEnum {

    L,
    S;

    public String value() {
        return name();
    }

    public static ResetEnum fromValue(String v) {
        return valueOf(v);
    }

}
