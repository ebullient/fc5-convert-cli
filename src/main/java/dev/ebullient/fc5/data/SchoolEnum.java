package dev.ebullient.fc5.data;

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

    abjuration("abjuration", "A"),
    conjuration("conjuration", "C"),
    divination("divination", "D"),
    enchantment("enchantment", "EN"),
    evocation("evocation", "EV"),
    illusion("illusion", "I"),
    necromancy("necromancy", "N"),
    transmutation("transmutation", "T");

    private final String longName;
    private final String xmlKey;

    private SchoolEnum(String longName, String xmlType) {
        this.longName = longName;
        this.xmlKey = xmlType;
    }

    public static SchoolEnum fromXmlType(String v) {
        if (v == null || v.isBlank()) {
            return null;
        }
        for (SchoolEnum p : SchoolEnum.values()) {
            if (p.xmlKey.equals(v)) {
                return p;
            }
        }
        return null;
    }

    public String longName() {
        return longName;
    }

}
