package dev.ebullient.fc5.model;

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
public enum CategoryEnum {

    BONUS("bonus"),
    ABILITY_SCORE("ability score"),
    ABILITY_MODIFIER("ability modifier"),
    SAVING_THROW("saving throw"),
    SKILLS("skills"),
    UNKNOWN("unknown");

    private final String value;

    CategoryEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CategoryEnum fromValue(String v) {
        for (CategoryEnum c : CategoryEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        return UNKNOWN;
    }

}
