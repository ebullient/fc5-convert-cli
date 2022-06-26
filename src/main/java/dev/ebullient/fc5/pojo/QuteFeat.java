package dev.ebullient.fc5.pojo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.quarkus.qute.TemplateData;

@TemplateData
public class QuteFeat implements QuteSource {

    final String name;
    final List<String> text;
    final Proficiency proficiency;
    final List<Modifier> modifier;
    final String prerequisite;

    protected QuteFeat(String name) {
        this.name = name;
        this.prerequisite = "none";
        this.proficiency = Proficiency.NONE;
        this.modifier = List.of();
        this.text = List.of();
    }

    protected QuteFeat(String name, List<String> text, Proficiency proficiency, List<Modifier> modifier, String prerequisite) {
        this.name = name;
        this.text = Objects.requireNonNull(text);
        this.proficiency = proficiency;
        this.modifier = modifier;
        this.prerequisite = prerequisite;
    }

    public String getName() {
        return name;
    }

    public List<String> getTags() {
        return Collections.singletonList("feat/" + MarkdownWriter.slugifier().slugify(name));
    }

    public String getText() {
        return String.join("\n", text).trim();
    }

    public Proficiency getProficiency() {
        return proficiency;
    }

    public List<Modifier> getModifier() {
        return modifier;
    }

    public String getPrerequisite() {
        return prerequisite;
    }

    @Override
    public String toString() {
        return "feat[name=" + name + "]";
    }

    public static class Builder {
        protected String name;
        protected List<String> text = new ArrayList<>();
        protected Proficiency proficiency;
        protected List<Modifier> modifier;
        protected String prerequisite;

        public Builder() {
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder addText(String t) {
            this.text.add(t);
            return this;
        }

        public Builder addText(Collection<String> t) {
            this.text.addAll(t);
            return this;
        }

        public Builder setProficiency(Proficiency proficiency) {
            this.proficiency = proficiency;
            return this;
        }

        public Builder setModifiers(List<Modifier> modifier) {
            this.modifier = modifier;
            return this;
        }

        public Builder setPrerequisite(String prerequisite) {
            this.prerequisite = prerequisite;
            return this;
        }

        public QuteFeat build() {
            return new QuteFeat(name, text, proficiency, modifier, prerequisite);
        }
    }
}
