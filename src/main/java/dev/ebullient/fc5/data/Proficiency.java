package dev.ebullient.fc5.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.quarkus.qute.TemplateData;

@TemplateData
public class Proficiency {
    static final Proficiency NONE = new Proficiency("");

    final List<SkillOrAbility> skills;

    final String textContent;

    public Proficiency(String textContent) {
        this.textContent = textContent;
        if (textContent.trim().isBlank()) {
            this.skills = Collections.emptyList();
        } else {
            this.skills = new ArrayList<>();
            Arrays.asList(textContent.trim().split("\\s*,\\s*")).forEach(s -> {
                if (SkillEnum.isSkill(s)) {
                    skills.add(SkillEnum.fromXmlValue(s));
                } else {
                    skills.add(AbilityEnum.fromXmlValue(s));
                }
            });
        }
    }

    public String getSavingThrows() {
        return skills.stream()
                .filter(x -> x.isAbility())
                .map(x -> x.value() + " Saving Throws")
                .collect(Collectors.joining(", "));
    }

    public String getSkillNames() {
        return skills.stream()
                .filter(x -> x.isSkill())
                .map(x -> "*" + x.value() + "*")
                .collect(Collectors.joining(", "));
    }
}
