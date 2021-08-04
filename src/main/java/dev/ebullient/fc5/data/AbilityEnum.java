package dev.ebullient.fc5.data;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
public enum AbilityEnum implements SkillOrAbility {
    STR("Strength"),
    DEX("Dexterity"),
    CON("Constitution"),
    INT("Intelligence"),
    WIS("Wisdom"),
    CHA("Charisma"),
    NONE("");

    final static List<String> allShortNames = Arrays.asList(AbilityEnum.values()).stream()
            .map(x -> x.name())
            .collect(Collectors.toList());
    final String longName;

    private AbilityEnum(String longName) {
        this.longName = longName;
    }

    public String getXmlValue() {
        return longName;
    }

    public String value() {
        return longName;
    }

    @Override
    public boolean isSkill() {
        return false;
    }

    @Override
    public boolean isAbility() {
        return true;
    }

    public static AbilityEnum fromXmlValue(String v) {
        if (v == null || v.isBlank() || v.length() < 3) {
            return NONE;
        }
        String compare = v.substring(0, 3).toUpperCase();
        for (AbilityEnum a : AbilityEnum.values()) {
            if (compare.equals(a.name())) {
                return a;
            }
        }
        throw new IllegalArgumentException("Unknown ability value " + v);
    }

    static boolean isAbility(String v) {
        if (v == null || v.isBlank() || v.length() < 3) {
            return false;
        }
        return allShortNames.contains(v.substring(0, 3).toUpperCase());
    }
}
