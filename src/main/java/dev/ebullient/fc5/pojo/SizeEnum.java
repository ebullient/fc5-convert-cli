package dev.ebullient.fc5.pojo;

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
public enum SizeEnum {

    TINY("Tiny", "T"),
    SMALL("Small", "S"),
    MEDIUM("Medium", "M"),
    LARGE("Large", "L"),
    HUGE("Huge", "H"),
    GARGANTUAN("Gargantuan", "G"),
    UNKNOWN("Unknown", "");

    private final String longName;
    private final String encodedValue;

    private SizeEnum(String longName, String xmlValue) {
        this.longName = longName;
        this.encodedValue = xmlValue;
    }

    public String value() {
        return longName;
    }

    public String getEncodedValue() {
        return encodedValue;
    }

    public static SizeEnum fromEncodedValue(String v) {
        if (v == null || v.isBlank()) {
            return UNKNOWN;
        }
        for (SizeEnum i : SizeEnum.values()) {
            if (i.encodedValue.equals(v)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Invalid/Unknown size " + v);
    }
}
