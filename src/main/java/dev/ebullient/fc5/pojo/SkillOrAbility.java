package dev.ebullient.fc5.pojo;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum SkillOrAbility {
    STR("Strength", false),
    DEX("Dexterity", false),
    CON("Constitution", false),
    INT("Intelligence", false),
    WIS("Wisdom", false),
    CHA("Charisma", false),
    Acrobatics("Acrobatics", true),
    AnimalHandling("Animal Handling", true),
    Arcana("Arcana", true),
    Athletics("Athletics", true),
    Deception("Deception", true),
    History("History", true),
    Insight("Insight", true),
    Intimidation("Intimidation", true),
    Investigation("Investigation", true),
    Medicine("Medicine", true),
    Nature("Nature", true),
    Perception("Perception", true),
    Performance("Performance", true),
    Persuasion("Persuasion", true),
    Religion("Religion", true),
    SleightOfHand("Sleight of Hand", true),
    Stealth("Stealth", true),
    Survival("Survival", true),
    Any("Any", false),
    Varies("Varies", false),
    None("None", false);

    private final String longValue;
    private final String lowerValue;
    private final boolean isSkill;
    private final boolean isAbility;

    SkillOrAbility(String longValue, boolean isSkill) {
        this.longValue = longValue;
        this.lowerValue = longValue.toLowerCase();
        this.isSkill = isSkill;
        this.isAbility = !isSkill && !longValue.equals("None") && !longValue.equals("Any") && !longValue.equals("Varies");
    }

    public boolean isAbility() {
        return isAbility;
    }

    public boolean isSkill() {
        return isSkill;
    }

    public String value() {
        return longValue;
    }

    public static String properValue(String v) {
        return fromTextValue(v).value();
    }

    public static SkillOrAbility fromTextValue(String v) {
        if (v == null || v.isBlank()) {
            return None;
        }
        String lower = v.toLowerCase().replace(" saving throws", "");
        for (SkillOrAbility s : SkillOrAbility.values()) {
            if (s.lowerValue.equals(lower) || s.name().toLowerCase().equals(lower)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown skill or ability value " + v + " (compared using " + lower + ")");
    }

    public static final List<String> allSkills = Stream.of(SkillOrAbility.values())
            .filter(x -> x.isSkill)
            .map(x -> x.longValue)
            .collect(Collectors.toList());

    public static final List<String> allAbilities = Stream.of(SkillOrAbility.values())
            .filter(x -> x.isAbility)
            .map(x -> x.longValue)
            .collect(Collectors.toList());

    public static boolean isSkill(String v) {
        if (v == null || v.isBlank()) {
            return false;
        }
        String lower = v.toLowerCase();
        for (SkillOrAbility s : SkillOrAbility.values()) {
            if (s.isSkill && s.lowerValue.equals(lower)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAbility(String v) {
        if (v == null || v.isBlank()) {
            return false;
        }
        String lower = v.toLowerCase();
        for (SkillOrAbility s : SkillOrAbility.values()) {
            if (s.isAbility && s.lowerValue.equals(lower)) {
                return true;
            }
        }
        return false;
    }
}
