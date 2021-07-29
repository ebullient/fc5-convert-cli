package dev.ebullient.fc5.model;

import java.util.Collections;
import java.util.List;

public class Text {
    public static final Text NONE = new Text(Collections.emptyList());

    final List<String> content;

    public Text(List<String> text) {
        this.content = text;
    }
}
