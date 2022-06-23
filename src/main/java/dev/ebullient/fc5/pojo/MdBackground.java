package dev.ebullient.fc5.pojo;

import java.util.Collections;
import java.util.List;

import io.quarkus.qute.TemplateData;

@TemplateData
public class MdBackground implements BaseType {
    final String name;
    final MdProficiency proficiency;
    final List<MdTrait> traits;

    MdBackground(String name, MdProficiency proficiency, List<MdTrait> traits) {
        this.name = name;
        this.proficiency = proficiency;
        this.traits = traits;
    }

    public String getName() {
        return name;
    }

    public List<String> getTags() {
        return Collections.singletonList("background/" + MarkdownWriter.slugifier().slugify(name));
    }

    public List<MdTrait> getTraits() {
        return traits;
    }

    public MdProficiency getAbilitySkills() {
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
        MdProficiency proficiency;
        List<MdTrait> traits;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setProficiency(MdProficiency proficiency) {
            this.proficiency = proficiency;
            return this;
        }

        public Builder setTraits(List<MdTrait> traits) {
            this.traits = traits;
            return this;
        }

        public MdBackground build() {
            return new MdBackground(name, proficiency, traits);
        }
    }
}
