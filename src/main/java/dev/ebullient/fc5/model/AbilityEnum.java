package dev.ebullient.fc5.model;

import io.quarkus.qute.TemplateData;

/**
 * <pre>
 *  &lt;xs:simpleType name="abilityEnum">
 *    &lt;xs:restriction base="xs:string">
 *        &lt;xs:enumeration value="Strength"/>
 *        &lt;xs:enumeration value="Dexterity"/>
 *        &lt;xs:enumeration value="Constitution"/>
 *        &lt;xs:enumeration value="Intelligence"/>
 *        &lt;xs:enumeration value="Wisdom"/>
 *        &lt;xs:enumeration value="Charisma"/>
 *        &lt;xs:enumeration value=""/>
 *    &lt;/xs:restriction>
 *  &lt;/xs:simpleType>
 * </pre>
 */
@TemplateData
public enum AbilityEnum {
    Strength,
    Dexterity,
    Constitution,
    Intelligence,
    Wisdom,
    Charisma,
    NONE;

    public String value() {
        return name();
    }

    public static AbilityEnum fromValue(String v) {
        if (v == null || v.isBlank()) {
            return NONE;
        }
        return valueOf(v);
    }

    static boolean isAbility(String v) {
        try {
            Enum.valueOf(AbilityEnum.class, v);
            return true;
        } catch (IllegalArgumentException e) {
        }
        return false;
    }
}
