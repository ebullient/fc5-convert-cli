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
public enum SizeEnum implements ConvertedEnumType {

    TINY("Tiny", "T"),
    SMALL("Small", "S"),
    MEDIUM("Medium", "M"),
    LARGE("Large", "L"),
    HUGE("Huge", "H"),
    GARGANTUAN("Gargantuan", "G"),
    UNKNOWN("Unknown", "");

    private final String longName;
    private final String xmlValue;

    private SizeEnum(String longName, String xmlValue) {
        this.longName = longName;
        this.xmlValue = xmlValue;
    }

    @Override
    public String value() {
        return longName;
    }

    @Override
    public String getXmlValue() {
        return xmlValue;
    }

    public static SizeEnum fromXmlValue(String v) {
        if (v == null || v.isBlank()) {
            return UNKNOWN;
        }
        for (SizeEnum i : SizeEnum.values()) {
            if (i.xmlValue.equals(v)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Invalid/Unknown size " + v);
    }
}
