package dev.ebullient.fc5.fc5data;

/**
 * <p>
 * Java class for resetEnum.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * <p>
 *
 * <pre>
 * &lt;simpleType name="resetEnum"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="L"/&gt;
 *     &lt;enumeration value="S"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 *
 */
public enum Fc5ResetEnum {

    LongRest("long rest", "L"),
    ShortRest("short rest", "S"),
    None("none", "");

    private final String longName;
    private final String xmlValue;

    private Fc5ResetEnum(String longName, String xmlValue) {
        this.longName = longName;
        this.xmlValue = xmlValue;
    }

    public String getEncodedValue() {
        return xmlValue;
    }

    public String value() {
        return longName;
    }

    public static Fc5ResetEnum fromXmlValue(String v) {
        if (v == null || v.isBlank()) {
            return None;
        }
        switch (v.toLowerCase()) {
            case "l":
                return LongRest;
            case "s":
                return ShortRest;
        }
        throw new IllegalArgumentException("Invalid/Unknown reset interval " + v);
    }

}
