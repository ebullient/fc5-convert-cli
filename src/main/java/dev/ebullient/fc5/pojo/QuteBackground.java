package dev.ebullient.fc5.pojo;

import java.util.Collections;
import java.util.List;

import io.quarkus.qute.TemplateData;

@TemplateData
public class QuteBackground implements QuteSource {
    final String name;
    final Proficiency proficiency;
    final List<QuteTrait> trait;

    QuteBackground(String name, Proficiency proficiency, List<QuteTrait> traits) {
        this.name = name;
        this.proficiency = proficiency;
        this.trait = traits;
    }

    public String getName() {
        return name;
    }

    public List<String> getTags() {
        return Collections.singletonList("background/" + MarkdownWriter.slugifier().slugify(name));
    }

    public List<QuteTrait> getTrait() {
        return trait;
    }

    public Proficiency getAbilitySkills() {
        return proficiency;
    }

    public String getProficiency() {
        return proficiency.toText();
    }

    @Override
    public String toString() {
        return "Background [name=" + name + "]";
    }

    public static class Builder {
        String name;
        Proficiency proficiency;
        List<QuteTrait> traits;

        public Builder setName(String name) {
            this.name = name;
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

        public QuteBackground build() {
            return new QuteBackground(name, proficiency, traits);
        }
    }
}
