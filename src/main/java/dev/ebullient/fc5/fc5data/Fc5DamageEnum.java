package dev.ebullient.fc5.fc5data;

import io.quarkus.qute.TemplateData;

/**
 *
 * <pre>
 *     &lt;xs:simpleType name="damageEnum">
 *      &lt;xs:restriction base="xs:string">
 *          &lt;xs:enumeration value="B"/><!-- bludgeoning -->
 *          &lt;xs:enumeration value="P"/><!-- piercing -->
 *          &lt;xs:enumeration value="S"/><!-- slashing -->
 *          &lt;xs:enumeration value="A"/><!-- acid -->
 *          &lt;xs:enumeration value="C"/><!-- cold -->
 *          &lt;xs:enumeration value="F"/><!-- fire -->
 *          &lt;xs:enumeration value="FC"/><!-- force -->
 *          &lt;xs:enumeration value="L"/><!-- lightning -->
 *          &lt;xs:enumeration value="N"/><!-- necrotic -->
 *          &lt;xs:enumeration value="PS"/><!-- poison -->
 *          &lt;xs:enumeration value="R"/><!-- radiant -->
 *          &lt;xs:enumeration value="T"/><!-- thunder -->
 *          &lt;xs:enumeration value=""/>
 *      </xs:restriction>
 *  </xs:simpleType>
 * </pre>
 */
@TemplateData
public enum Fc5DamageEnum {
    BLUDGEONING("bludgeoning", "B"),
    PIERCING("piercing", "P"),
    SLASHING("slashing", "S"),
    ACID("acid", "A"),
    COLD("cold", "C"),
    FIRE("fire", "F"),
    FORCE("force", "FC"),
    LIGHTNING("lightning", "L"),
    NECROTIC("necrotic", "N"),
    POISON("poison", "PS"),
    RADIANT("radiant", "R"),
    THUNDER("thunder", "T"),
    UNKNOWN("unknown", "");

    private final String longName;
    private final String encodingValue;

    private Fc5DamageEnum(String longName, String xmlValue) {
        this.longName = longName;
        this.encodingValue = xmlValue;
    }

    public String value() {
        return longName;
    }

    public String getEncodedValue() {
        return encodingValue;
    }

    public static Fc5DamageEnum fromEncodedValue(String v) {
        if (v == null || v.isBlank()) {
            return UNKNOWN;
        }
        for (Fc5DamageEnum i : Fc5DamageEnum.values()) {
            if (i.encodingValue.equals(v)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Invalid/Unknown damage type " + v);
    }
}
