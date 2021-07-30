package dev.ebullient.fc5.data;

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
public enum DamageEnum {
    bludgeoning("bludgeoning", "B"),
    piercing("piercing", "P"),
    slashing("slashing", "S"),
    acid("acid", "A"),
    cold("cold", "C"),
    fire("fire", "F"),
    force("force", "FC"),
    lightning("lightning", "L"),
    necrotic("necrotic", "N"),
    poison("poison", "PS"),
    radiant("radiant", "R"),
    thunder("thunder", "T"),
    unknown("unknown", "UNK");

    private final String longName;
    private final String xmlKey;

    private DamageEnum(String longName, String xmlKey) {
        this.longName = longName;
        this.xmlKey = xmlKey;
    }

    public String getLongName() {
        return longName;
    }

    public String getXmlKey() {
        return xmlKey;
    }

    public static DamageEnum fromValue(String v) {
        if (v == null || v.isBlank()) {
            return unknown;
        }
        for (DamageEnum i : DamageEnum.values()) {
            if (i.xmlKey.equals(v)) {
                return i;
            }
        }
        return unknown;
    }
}
