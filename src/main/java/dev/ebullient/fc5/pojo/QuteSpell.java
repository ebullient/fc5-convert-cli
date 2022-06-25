package dev.ebullient.fc5.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.quarkus.qute.TemplateData;

@TemplateData
public class QuteSpell implements QuteSource {

    final String name;
    final int level;
    final SchoolEnum school;
    final boolean ritual;
    final String time;
    final String range;
    final String components;
    final String duration;
    final String classes;
    final List<String> classSlugs;
    final List<String> text;

    public QuteSpell(String name, int level, SchoolEnum school, boolean ritual, String time, String range,
            String components, String duration, String classes, List<String> text) {
        this.name = name;
        this.level = level;
        this.school = school;
        this.ritual = ritual;
        this.time = time;
        this.range = range;
        this.components = components;
        this.duration = duration;
        this.classes = classes;
        this.classSlugs = Stream.of(classes.split("\\s*,\\s*"))
                .map(x -> MarkdownWriter.slugifier().slugify(x)).collect(Collectors.toList());
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public List<String> getTags() {
        List<String> tags = new ArrayList<>();
        tags.add("spell/school/" + school.value());
        tags.add("spell/level/" + (level == 0 ? "cantrip" : level));
        classSlugs.forEach(x -> tags.add("spell/class/" + x));
        if (ritual) {
            tags.add("spell/ritual");
        }
        return tags;
    }

    public String getClasses() {
        return classes;
    }

    public String getComponents() {
        return components;
    }

    public String getDuration() {
        return duration;
    }

    public String getLevel() {
        switch (level) {
            case 0:
                return "cantrip";
            case 1:
                return "1st-level";
            case 2:
                return "2nd-level";
            case 3:
                return "3rd-level";
            default:
                return level + "th-level";
        }
    }

    public String getRange() {
        return range;
    }

    public boolean getRitual() {
        return ritual;
    }

    public String getSchool() {
        return school.value();
    }

    public String getTime() {
        return time;
    }

    public String getText() {
        return String.join("\n", text).trim();
    }

    @Override
    public String toString() {
        return "spell[name=" + name + "]";
    }

    public static class Builder {
        protected String name;
        protected int level;
        protected SchoolEnum school;
        protected boolean ritual;
        protected String time;
        protected String range;
        protected String components;
        protected String duration;
        protected String classes;
        protected String source;
        protected List<String> text;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setLevel(int level) {
            this.level = level;
            return this;
        }

        public Builder setSchool(SchoolEnum school) {
            this.school = school;
            return this;
        }

        public Builder setRitual(boolean ritual) {
            this.ritual = ritual;
            return this;
        }

        public Builder setTime(String time) {
            this.time = time;
            return this;
        }

        public Builder setRange(String range) {
            this.range = range;
            return this;
        }

        public Builder setComponents(String components) {
            this.components = components;
            return this;
        }

        public Builder setDuration(String duration) {
            this.duration = duration;
            return this;
        }

        public Builder setClasses(String classes) {
            this.classes = classes;
            return this;
        }

        public Builder setSource(String source) {
            this.source = source;
            return this;
        }

        public Builder setText(List<String> text) {
            this.text = text;
            return this;
        }

        public QuteSpell build() {
            return new QuteSpell(name, level, school, ritual, time, range, components, duration, classes, text);
        }
    }
}
