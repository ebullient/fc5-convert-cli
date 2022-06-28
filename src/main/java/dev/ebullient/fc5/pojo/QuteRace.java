package dev.ebullient.fc5.pojo;

import java.util.Collections;
import java.util.List;

import io.quarkus.qute.TemplateData;

@TemplateData
public class QuteRace implements QuteSource {
    protected final String name;
    protected final SizeEnum size;
    protected final int speed;
    protected final String ability;
    protected final SkillOrAbility spellAbility;
    protected final Proficiency proficiency;
    protected final List<QuteTrait> trait;
    protected final List<Modifier> modifiers;

    protected QuteRace(String name, SizeEnum size, int speed, String ability,
            SkillOrAbility spellAbility, Proficiency proficiency, List<QuteTrait> traits,
            List<Modifier> modifiers) {
        this.name = name;
        this.size = size;
        this.speed = speed;
        this.ability = ability;
        this.spellAbility = spellAbility;
        this.proficiency = proficiency;
        this.trait = traits;
        this.modifiers = modifiers;
    }

    public String getName() {
        return name;
    }

    public List<String> getTags() {
        String[] split = name.split("\\(");
        for (int i = 0; i < split.length; i++) {
            split[i] = MarkdownWriter.slugifier().slugify(split[i].trim());
        }
        return Collections.singletonList("race/" + String.join("/", split));
    }

    public String getSize() {
        return size.value();
    }

    public int getSpeed() {
        return speed;
    }

    public String getAbility() {
        return ability;
    }

    public String getSpellAbility() {
        return spellAbility.value();
    }

    public String getProficiency() {
        return proficiency.toText();
    }

    public String getSkills() {
        return proficiency.getSkillNames();
    }

    public List<QuteTrait> getTrait() {
        return trait;
    }

    public List<Modifier> getModifiers() {
        return modifiers;
    }

    @Override
    public String toString() {
        return "race[name=" + name + "]";
    }

    public static class Builder {
        String name;
        SizeEnum size;
        int speed;
        String ability;
        SkillOrAbility spellAbility;
        Proficiency proficiency;
        List<QuteTrait> traits;
        List<Modifier> modifiers;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setSize(SizeEnum size) {
            this.size = size;
            return this;
        }

        public Builder setSpeed(int speed) {
            this.speed = speed;
            return this;
        }

        public Builder setAbility(String ability) {
            this.ability = ability;
            return this;
        }

        public Builder setSpellAbility(SkillOrAbility spellAbility) {
            this.spellAbility = spellAbility;
            return this;
        }

        public Builder setProficiency(Proficiency proficiency) {
            this.proficiency = proficiency;
            return this;
        }

        public Builder setTraits(List<QuteTrait> traits) {
            this.traits = traits;
            return this;
        }

        public Builder setModifiers(List<Modifier> modifiers) {
            this.modifiers = modifiers;
            return this;
        }

        public QuteRace build() {
            return new QuteRace(name, size, speed, ability, spellAbility, proficiency, traits, modifiers);
        }
    }
}
