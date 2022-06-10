package dev.ebullient.fc5.json;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonIndex {

    final List<String> allowedSources;
    final Map<String, JsonNode> nodeIndex = new HashMap<>();

    public JsonIndex(List<String> sources) {
        this.allowedSources = sources;
    }

    public JsonIndex importTree(JsonNode node) {
        node.withArray("background").forEach(x -> {
            nodeIndex.put(getKey("background", x).toLowerCase(), x);
        });
        node.withArray("class").forEach(x -> {
            nodeIndex.put(getKey("class", x).toLowerCase(), x);
        });
        node.withArray("feat").forEach(x -> {
            nodeIndex.put(getKey("feat", x).toLowerCase(), x);
        });
        node.withArray("baseitem").forEach(x -> {
            nodeIndex.put(getKey("item", x).toLowerCase(), x);
        });
        node.withArray("item").forEach(x -> {
            nodeIndex.put(getKey("item", x).toLowerCase(), x);
        });
        node.withArray("monster").forEach(x -> {
            nodeIndex.put(getKey("monster", x).toLowerCase(), x);
        });
        node.withArray("race").forEach(x -> {
            nodeIndex.put(getKey("race", x).toLowerCase(), x);
        });
        node.withArray("spell").forEach(x -> {
            nodeIndex.put(getKey("spell", x).toLowerCase(), x);
        });
        return this;
    }

    public Iterable<Entry<String, JsonNode>> elements() {
        return nodeIndex.entrySet();
    }

    private String getKey(String type, JsonNode x) {
        return String.format("%s|%s|%s", type, x.get("name").asText(), x.get("source").asText());
    }

    public boolean sourceIncluded(String source) {
        return allowedSources.contains(source);
    }

    public boolean includeElement(Collection<String> bookSources, boolean isSrd) {
        if (allowedSources.isEmpty()) {
            return isSrd; // include SRD sources when no filter is specified.
        }
        return bookSources.stream().anyMatch(x -> allowedSources.contains(x));
    }

    public boolean excludeElement(Collection<String> bookSources, boolean isSRD) {
        if (allowedSources.isEmpty()) {
            return !isSRD; // exclude non-SRD sources when no filter is specified.
        }
        return bookSources.stream().noneMatch(x -> allowedSources.contains(x));
    }

    public boolean excludeItem(JsonNode itemSource, boolean isSRD) {
        if (allowedSources.isEmpty()) {
            return !isSRD; // exclude non-SRD sources when no filter is specified.
        }
        if (itemSource == null || !itemSource.isTextual()) {
            return true; // unlikely, but skip items if we can't check their source
        }
        return !allowedSources.contains(itemSource.asText());
    }
}
