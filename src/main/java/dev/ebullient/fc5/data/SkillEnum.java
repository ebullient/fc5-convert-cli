package dev.ebullient.fc5.data;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum SkillEnum implements SkillOrAbility {
    Athletics("Athletics"),
    Acrobatics("Acrobatics"),
    AnimalHandling("Animal Handling"),
    Arcana("Arcana"),
    Deception("Deception"),
    History("History"),
    Insight("Insight"),
    Intimidation("Intimidation"),
    Investigation("Investigation"),
    Medicine("Medicine"),
    Nature("Nature"),
    Perception("Perception"),
    Performance("Performance"),
    Persuasion("Persuasion"),
    Religion("Religion"),
    SleightOfHand("Sleight of Hand"),
    Stealth("Stealth"),
    Survival("Survival"),
    None("None");

    static final List<String> allXmlNames = Arrays.asList(SkillEnum.values()).stream()
            .map(x -> x.getXmlValue().toLowerCase())
            .collect(Collectors.toList());

    final String longValue;

    SkillEnum(String longValue) {
        this.longValue = longValue;
    }

    @Override
    public String getXmlValue() {
        return longValue;
    }

    @Override
    public String value() {
        return longValue;
    }

    @Override
    public boolean isSkill() {
        return true;
    }

    @Override
    public boolean isAbility() {
        return false;
    }

    public static SkillEnum fromXmlValue(String v) {
        if (v == null || v.isBlank()) {
            return None;
        }
        String lower = v.toLowerCase();
        for (SkillEnum s : SkillEnum.values()) {
            if (lower.equals(s.longValue.toLowerCase())) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown ability value " + v);
    }

    static boolean isSkill(String v) {
        if (v == null || v.isBlank()) {
            return false;
        }
        return allXmlNames.contains(v.toLowerCase());
    }
}
