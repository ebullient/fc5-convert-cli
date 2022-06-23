package dev.ebullient.fc5.pojo;

import java.util.List;

import io.quarkus.qute.TemplateData;

@TemplateData
public class MdFeature implements BaseType {
    protected final String name;
    protected final List<String> featureText;
    protected final int level;

    public MdFeature(String name, int level, List<String> featureText) {
        this.name = name;
        this.featureText = featureText;
        this.level = level;
    }

    public String getText() {
        return String.join(", ", featureText);
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return "feature[name=" + name + ", level=" + level + "]";
    }

    public static class Builder {
        protected String name;
        protected List<String> featureText;
        protected int level;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setFeatureText(List<String> featureText) {
            this.featureText = featureText;
            return this;
        }

        public Builder setLevel(int level) {
            this.level = level;
            return this;
        }

        public MdFeature build() {
            return new MdFeature(name, level, featureText);
        }
    }
}
