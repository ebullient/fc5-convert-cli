package dev.ebullient.fc5.pojo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import io.quarkus.qute.TemplateData;

@TemplateData
public class QuteClassFeature implements QuteSource {
    protected final String name;
    protected final int autolevel;
    protected final boolean optional;
    protected final List<String> featureText;
    protected final List<Modifier> modifiers;
    protected final List<String> special;
    protected final Proficiency proficiency;
    protected final String sortingGroup;

    public QuteClassFeature(String name, int autolevel, boolean optional,
            List<String> featureText, List<Modifier> modifiers, List<String> special,
            Proficiency proficiency, String sortingGroup) {
        this.name = name;
        this.autolevel = autolevel;
        this.optional = optional;
        this.featureText = Objects.requireNonNull(featureText);
        this.modifiers = modifiers;
        this.special = special;
        this.proficiency = proficiency;
        this.sortingGroup = sortingGroup;
    }

    public String getText() {
        return String.join(", ", featureText);
    }

    public List<String> getRawText() {
        return featureText;
    }

    public String getName() {
        return name;
    }

    public boolean isOptional() {
        return optional;
    }

    public int getLevel() {
        return autolevel;
    }

    public String getSortingGroup() {
        return sortingGroup;
    }

    @Override
    public String toString() {
        return "feature[name=" + name + ", level=" + autolevel + "]";
    }

    public static class Builder {
        protected boolean optional = false;
        protected String name;
        protected List<String> featureText = new ArrayList<>();
        protected List<String> special = new ArrayList<>();
        protected List<Modifier> modifiers = new ArrayList<>();
        protected Proficiency proficiency;

        protected int level;
        protected String subclassTitle;
        protected String sortingGroup = "";

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setSubclassTitle(String subclassTitle) {
            this.subclassTitle = subclassTitle;
            return this;
        }

        public Builder addText(List<String> t) {
            this.featureText.addAll(t);
            return this;
        }

        public Builder addText(String t) {
            this.featureText.add(t);
            return this;
        }

        public Builder setLevel(int level) {
            this.level = level;
            return this;
        }

        public Builder setOptional(boolean optional) {
            this.optional = optional;
            return this;
        }

        public Builder addModifiers(List<Modifier> m) {
            this.modifiers.addAll(m);
            return this;
        }

        public Builder addModifier(Modifier m) {
            this.modifiers.add(m);
            return this;
        }

        public Builder addSpecial(String s) {
            this.special.add(s);
            return this;
        }

        public Builder setProficiency(Proficiency proficiency) {
            this.proficiency = proficiency;
            return this;
        }

        public Builder setGroup(String sortingGroup) {
            this.sortingGroup = sortingGroup;
            return this;
        }

        public QuteClassFeature build() {
            return new QuteClassFeature(name, level, optional, featureText, modifiers, special, proficiency, sortingGroup);
        }

    }

    public static Comparator<QuteClassFeature> alphabeticalFeatureSort = new Comparator<>() {
        @Override
        public int compare(QuteClassFeature o1, QuteClassFeature o2) {
            return o1.name.compareTo(o2.name);
        }
    };
}
