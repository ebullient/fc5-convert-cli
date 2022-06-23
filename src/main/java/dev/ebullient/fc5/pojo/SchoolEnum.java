package dev.ebullient.fc5.pojo;

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
public enum SchoolEnum implements ConvertedEnumType {

    Abjuration("abjuration", "A"),
    Conjuration("conjuration", "C"),
    Divination("divination", "D"),
    Enchantment("enchantment", "EN"),
    Evocation("evocation", "EV"),
    Illusion("illusion", "I"),
    Necromancy("necromancy", "N"),
    Transmutation("transmutation", "T"),
    None("none", "");

    private final String longName;
    private final String encodedValue;

    private SchoolEnum(String longName, String xmlValue) {
        this.longName = longName;
        this.encodedValue = xmlValue;
    }

    @Override
    public String value() {
        return longName;
    }

    @Override
    public String getEncodedValue() {
        return encodedValue;
    }

    public static SchoolEnum fromEncodedValue(String v) {
        if (v == null || v.isBlank()) {
            return None;
        }
        for (SchoolEnum p : SchoolEnum.values()) {
            if (p.encodedValue.equals(v)) {
                return p;
            }
        }
        throw new IllegalArgumentException("Invalid/Unknown school " + v);
    }
}
