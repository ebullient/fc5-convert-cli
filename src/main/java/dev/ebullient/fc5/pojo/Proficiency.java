package dev.ebullient.fc5.pojo;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Proficiency {
    public static final Proficiency NONE = new Builder().fromString("").build();

    final List<SkillOrAbility> skills;

    public Proficiency(List<SkillOrAbility> skills) {
        this.skills = skills;
    }

    public String getSavingThrows() {
        return skills.stream()
                .filter(x -> x.isAbility())
                .sorted(Comparator.comparingInt(Enum::ordinal))
                .map(x -> x.value() + " Saving Throws")
                .collect(Collectors.joining(", "));
    }

    public String getSkillNames() {
        return skills.stream()
                .filter(x -> x.isSkill())
                .sorted(Comparator.comparingInt(Enum::ordinal))
                .map(x -> "*" + x.value() + "*")
                .collect(Collectors.joining(", "));
    }

    public String toText() {
        return skills.stream()
                .sorted(Comparator.comparingInt(Enum::ordinal))
                .map(s -> s.value())
                .collect(Collectors.joining(", "));
    }

    public static class Builder {
        final Set<SkillOrAbility> skills = new HashSet<>();

        public Builder addSkills(Collection<SkillOrAbility> skills) {
            this.skills.addAll(skills);
            return this;
        }

        public Builder addSkill(SkillOrAbility skill) {
            skills.add(skill);
            return this;
        }

        public Builder fromString(String textContent) {
            if (textContent == null || textContent.trim().isEmpty()) {
                return this;
            }
            Arrays.asList(textContent.trim().split("\\s*,\\s*")).forEach(s -> {
                skills.add(SkillOrAbility.fromTextValue(s));
            });
            return this;
        }

        public Proficiency build() {
            return new Proficiency(skills.stream().collect(Collectors.toList()));
        }
    }
}
