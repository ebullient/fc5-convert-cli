package dev.ebullient.fc5.data;

import io.quarkus.qute.TemplateData;

/**
 * <p>
 * Java class for categoryEnum.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * <p>
 * 
 * <pre>
 * &lt;simpleType name="categoryEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="bonus"/>
 *     &lt;enumeration value="ability score"/>
 *     &lt;enumeration value="ability modifier"/>
 *     &lt;enumeration value="saving throw"/>
 *     &lt;enumeration value="skills"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@TemplateData
public enum CategoryEnum {

    BONUS("Bonus"),
    ABILITY_SCORE("Ability Score"),
    ABILITY_MODIFIER("Ability Modifier"),
    SAVING_THROW("Saving Throw"),
    SKILLS("Skills"),
    UNKNOWN("Unknown");

    private final String value;

    CategoryEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CategoryEnum fromValue(String v) {
        for (CategoryEnum c : CategoryEnum.values()) {
            if (c.value.toLowerCase().equals(v)) {
                return c;
            }
        }
        return UNKNOWN;
    }

    public String longName() {
        return value;
    }
}
