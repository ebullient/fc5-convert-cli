package dev.ebullient.fc5.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

}
