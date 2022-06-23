package dev.ebullient.fc5.fc5data;

import io.quarkus.qute.TemplateData;

@TemplateData
public class Roll {
    public static final Roll NONE = new Roll("");

    final String textContent;

    public Roll(String textContent) {
        this.textContent = textContent;
    }

    @Override
    public String toString() {
        return textContent;
    }
}
