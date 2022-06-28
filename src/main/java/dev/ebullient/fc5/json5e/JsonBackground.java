package dev.ebullient.fc5.json5e;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.Log;

public interface JsonBackground extends JsonBase {

    default List<String> getDescription(JsonNode jsonSource) {
        List<String> text = new ArrayList<>();
        try {
            getFluffDescription(jsonSource, JsonIndex.IndexType.backgroundfluff, text);
            maybeAddBlankLine(text);
        } catch (Exception e) {
            Log.errorf(e, "Unable to parse traits for %s", getSources());
        }
        text.add("Source: " + getSources().getSourceText());
        return text;
    }
}
