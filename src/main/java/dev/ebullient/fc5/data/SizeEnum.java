package dev.ebullient.fc5.data;

import io.quarkus.qute.TemplateData;

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
@TemplateData
public enum SizeEnum {

    T("Tiny"),
    S("Small"),
    M("Medium"),
    L("Large"),
    H("Huge"),
    G("Gargantuan"),
    UNKNOWN("Unknown");

    private final String prettyName;

    private SizeEnum(String prettyName) {
        this.prettyName = prettyName;
    }

    public String prettyName() {
        return prettyName;
    }

    public static SizeEnum fromValue(String v) {
        if (v == null || v.isBlank()) {
            return UNKNOWN;
        }
        return valueOf(v);
    }
}
