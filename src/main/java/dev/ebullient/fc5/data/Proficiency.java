package dev.ebullient.fc5.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.quarkus.qute.TemplateData;

@TemplateData
public class Proficiency {
    public static final Proficiency ABILITY_AND_SKILL_LIST = new Proficiency("", "abilityAndSkillList") {
        @Override
        public void setFlavor(String string) {
        }
    };
    public static final Proficiency SKILL_LIST = new Proficiency("", "skillList") {
        @Override
        public void setFlavor(String string) {
        }
    };
    public static final Proficiency STRING = new Proficiency("", "string") {
        @Override
        public void setFlavor(String string) {
        }
    };

    String flavor;
    final List<String> skills;

    final String textContent;

    private Proficiency(String textContent, String flavor) {
        this.flavor = flavor;
        this.textContent = textContent;
        this.skills = Collections.emptyList();
    }

    public Proficiency(String textContent) {
        this.textContent = textContent;
        this.skills = Arrays.asList(textContent.split("\\s*,\\s*"));
    }

    public void setFlavor(String flavor) {
        this.flavor = flavor;
    }

    public String getSavingThrows() {
        if (ABILITY_AND_SKILL_LIST.flavor.equals(this.flavor)) {
            return skills.stream()
                    .filter(x -> AbilityEnum.isAbility(x))
                    .collect(Collectors.joining(", "));
        }
        return "";
    }

    public String getSkillNames() {
        if (ABILITY_AND_SKILL_LIST.flavor.equals(this.flavor)) {
            return skills.stream()
                    .filter(x -> !AbilityEnum.isAbility(x))
                    .map(x -> "*" + x + "*")
                    .collect(Collectors.joining(", "));
        }
        return skills.stream()
                .map(x -> "*" + x + "*")
                .collect(Collectors.joining(", "));
    }
}
