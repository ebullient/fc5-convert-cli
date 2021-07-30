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
    B("bludgeoning"),
    P("piercing"),
    S("slashing"),
    A("acid"),
    C("cold"),
    F("fire"),
    FC("force"),
    L("lightning"),
    N("necrotic"),
    PS("poison"),
    R("radiant"),
    T("thunder"),
    UNKNOWN("unknown");

    private final String value;

    DamageEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DamageEnum fromValue(String v) {
        for (DamageEnum d : DamageEnum.values()) {
            if (d.value.equals(v)) {
                return d;
            }
        }
        return UNKNOWN;
    }
}
