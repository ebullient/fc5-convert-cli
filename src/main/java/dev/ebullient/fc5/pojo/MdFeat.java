package dev.ebullient.fc5.pojo;

import java.util.Collections;
import java.util.List;

import io.quarkus.qute.TemplateData;

@TemplateData
public class MdFeat implements BaseType {

    final String name;
    final List<String> text;

    protected MdFeat(String name) {
        this.name = name;
        this.text = List.of();
    }

    protected MdFeat(String name, List<String> text) {
        this.name = name;
        this.text = text;
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

    @Override
    public String toString() {
        return "feat[name=" + name + "]";
    }

    public static class Builder {
        String name;
        List<String> text;

        public Builder() {
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setText(List<String> text) {
            this.text = text;
            return this;
        }

        public MdFeat build() {
            return new MdFeat(name, text);
        }
    }
}
