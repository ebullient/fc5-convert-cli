package dev.ebullient.fc5.fc5data;

import io.quarkus.qute.TemplateData;

@TemplateData
public class Fc5Roll {
    public static final Fc5Roll NONE = new Fc5Roll("");

    final String textContent;

    public Fc5Roll(String textContent) {
        this.textContent = textContent;
    }

    @Override
    public String toString() {
        return textContent;
    }
}
