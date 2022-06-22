package dev.ebullient.fc5.json;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.json.JsonIndex.IndexType;

public class CompendiumSources {
    final IndexType type;
    final String key;
    final String name;
    final Set<String> bookSources = new HashSet<>();
    final String sourceText;

    CompendiumSources(IndexType type, String key, JsonNode jsonElement) {
        this.type = type;
        this.key = key;
        this.name = jsonElement.get("name").asText();
        this.sourceText = findSourceText(jsonElement);
    }

    public String getSourceText() {
        return sourceText;
    }

    private String findSourceText(JsonNode jsonElement) {
        this.bookSources.add(jsonElement.get("source").asText());

        List<String> srcText = new ArrayList<>();
        srcText.add(sourceAndPage(jsonElement));

        String copyOf = jsonElement.has("_copy")
                ? jsonElement.get("_copy").get("name").asText()
                : null;
        String copySrc = jsonElement.has("_copy")
                ? jsonElement.get("_copy").get("source").asText()
                : null;

        if (copyOf != null) {
            srcText.add(String.format("Derived from %s (%s)", copyOf, copySrc));
        }

        srcText.addAll(StreamSupport.stream(jsonElement.withArray("additionalSources").spliterator(), false)
                .filter(x -> !x.get("source").asText().equals(copySrc))
                .peek(x -> this.bookSources.add(x.get("source").asText()))
                .map(x -> sourceAndPage(x))
                .collect(Collectors.toList()));

        srcText.addAll(StreamSupport.stream(jsonElement.withArray("otherSources").spliterator(), false)
                .filter(x -> !x.get("source").asText().equals(copySrc))
                .peek(x -> this.bookSources.add(x.get("source").asText()))
                .map(x -> sourceAndPage(x))
                .collect(Collectors.toList()));

        return String.join(", ", srcText);
    }

    private String sourceAndPage(JsonNode source) {
        if (source.has("page")) {
            return String.format("%s p. %s", source.get("source").asText(), source.get("page").asText());
        }
        return source.get("source").asText();
    }

    boolean isPrimarySource(String source) {
        return bookSources.iterator().next().equals(source);
    }

    public boolean isFromUA() {
        return bookSources.iterator().next().contains("UA");
    }

    String alternateSource() {
        Iterator<String> i = bookSources.iterator();
        if (bookSources.size() > 1) {
            i.next();
        }
        return i.next();
    }

    @Override
    public String toString() {
        return "sources[" + key + ']';
    }
}
