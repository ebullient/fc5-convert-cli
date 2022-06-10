package dev.ebullient.fc5.json;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;

public class CompendiumSources {
    final String key;
    final Set<String> bookSources = new HashSet<>();
    final String sourceText;

    CompendiumSources(String key, JsonNode jsonElement) {
        this.key = key;
        this.sourceText = getSourceText(jsonElement);
    }

    private String getSourceText(JsonNode jsonElement) {
        List<String> source = new ArrayList<>();

        this.bookSources.add(jsonElement.get("source").asText());
        source.add(sourceAndPage(jsonElement));

        source.addAll(StreamSupport.stream(jsonElement.withArray("additionalSources").spliterator(), false)
                .peek(x -> this.bookSources.add(x.get("source").asText()))
                .map(x -> sourceAndPage(x))
                .collect(Collectors.toList()));

        source.addAll(StreamSupport.stream(jsonElement.withArray("otherSources").spliterator(), false)
                .peek(x -> this.bookSources.add(x.get("source").asText()))
                .map(x -> sourceAndPage(x))
                .collect(Collectors.toList()));

        return String.join(", ", source);
    }

    private String sourceAndPage(JsonNode source) {
        if (source.has("page")) {
            return String.format("%s p. %s", source.get("source").asText(), source.get("page").asText());
        }
        return source.get("source").asText();
    }
}
