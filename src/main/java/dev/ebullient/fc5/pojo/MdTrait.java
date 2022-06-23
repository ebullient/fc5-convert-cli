package dev.ebullient.fc5.pojo;

import java.util.List;

import io.quarkus.qute.TemplateData;

@TemplateData
public class MdTrait implements BaseType {
    final String name;

    final List<String> text;

    protected MdTrait(String name) {
        this.name = name;
        this.text = List.of();
    }

    protected MdTrait(String name, List<String> text) {
        this.name = name;
        this.text = text;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getText() {
        return String.join("\n", text).trim();
    }

    public static class Builder {
        String name;
        List<String> text = List.of();

        public Builder() {
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setText(List<String> text) {
            this.text = text;
        }

        public MdTrait build() {
            return new MdTrait(name, text);
        }
    }
}
