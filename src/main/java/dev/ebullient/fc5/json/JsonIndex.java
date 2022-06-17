package dev.ebullient.fc5.json;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.Import5eTools;
import dev.ebullient.fc5.Log;

public class JsonIndex {
    public enum IndexType {
        background,
        backgroundfluff,
        classtype,
        classfeature,
        feat,
        item,
        itementry,
        itemfluff,
        monster,
        monsterfluff,
        race,
        racefluff,
        spell,
        spellfluff,
        subclass,
        subclassfeature,
        optionalfeature,
        other;

        public boolean isOptional() {
            return this == subclass || this == subclassfeature || this == optionalfeature;
        }
    }

    private final boolean allSources;
    private final List<String> allowedSources;
    private final Set<String> excludedKeys = new HashSet<>();
    private final Map<String, JsonNode> nodeIndex = new HashMap<>();

    public JsonIndex(List<String> sources) {
        this.allowedSources = sources;
        allSources = allowedSources.contains("*");
    }

    public JsonIndex importTree(JsonNode node) {
        if (!node.isObject()) {
            return this;
        }
        node.withArray("background").forEach(x -> {
            nodeIndex.put(getKey(IndexType.background, x), x);
        });
        node.withArray("backgroundFluff").forEach(x -> {
            nodeIndex.put(getKey(IndexType.backgroundfluff, x), x);
        });
        node.withArray("class").forEach(x -> {
            nodeIndex.put(getKey(IndexType.classtype, x), x);
        });
        node.withArray("subclass").forEach(x -> {
            nodeIndex.put(getKey(IndexType.subclass, x), x);
        });
        node.withArray("classFeature").forEach(x -> {
            nodeIndex.put(getKey(IndexType.classfeature, x), x);
        });
        node.withArray("optionalfeature").forEach(x -> {
            nodeIndex.put(getKey(IndexType.optionalfeature, x), x);
        });
        node.withArray("subclassFeature").forEach(x -> {
            nodeIndex.put(getKey(IndexType.subclassfeature, x), x);
        });
        node.withArray("feat").forEach(x -> {
            nodeIndex.put(getKey(IndexType.feat, x), x);
        });
        node.withArray("baseitem").forEach(x -> {
            nodeIndex.put(getKey(IndexType.item, x), x);
        });
        node.withArray("item").forEach(x -> {
            nodeIndex.put(getKey(IndexType.item, x), x);
        });
        node.withArray("itemEntry").forEach(x -> {
            nodeIndex.put(getKey(IndexType.itementry, x), x);
        });
        node.withArray("itemFluff").forEach(x -> {
            nodeIndex.put(getKey(IndexType.itemfluff, x), x);
        });
        node.withArray("monster").forEach(x -> {
            nodeIndex.put(getKey(IndexType.monster, x), x);
        });
        node.withArray("monsterFluff").forEach(x -> {
            nodeIndex.put(getKey(IndexType.monsterfluff, x), x);
        });
        node.withArray("race").forEach(x -> {
            nodeIndex.put(getKey(IndexType.race, x), x);
        });
        node.withArray("raceFluff").forEach(x -> {
            nodeIndex.put(getKey(IndexType.racefluff, x), x);
        });
        node.withArray("spell").forEach(x -> {
            nodeIndex.put(getKey(IndexType.spell, x), x);
        });
        node.withArray("spellFluff").forEach(x -> {
            nodeIndex.put(getKey(IndexType.spellfluff, x), x);
        });
        node.withArray("exclude").forEach(x -> {
            excludedKeys.add(x.asText());
        });
        return this;
    }

    public Iterable<Entry<String, JsonNode>> elements() {
        return nodeIndex.entrySet();
    }

    public Stream<JsonNode> classElementsMatching(IndexType type, String className, String classSource) {
        String pattern = String.format("%s\\|[^|]+\\|%s\\|.*", type, className)
                .toLowerCase();
        return nodeIndex.entrySet().stream()
                .filter(e -> e.getKey().matches(pattern))
                .map(e -> e.getValue());
    }

    public Collection<String> getKeys() {
        return nodeIndex.keySet();
    }

    public String getClassKey(String className, String classSource) {
        return String.format("%s|%s|%s",
                IndexType.classtype, className, classSource).toLowerCase();
    }

    public String getSubclassKey(String name, String className, String classSource) {
        return String.format("%s|%s|%s|%s|",
                IndexType.subclass, name, className, classSource).toLowerCase();
    }

    public String getKey(IndexType type, JsonNode x) {
        switch (type) {
            case subclass:
                return String.format("%s|%s|%s|%s|",
                        type,
                        getTextOrEmpty(x, "name"),
                        getTextOrEmpty(x, "className"),
                        getTextOrEmpty(x, "classSource"))
                        .toLowerCase();
            case classfeature: {
                String featureSource = getOrEmptyIfEqual(x, "source",
                        getTextOrDefault(x, "classSource", "PHB"));
                return String.format("%s|%s|%s|%s|%s%s",
                        type,
                        getTextOrEmpty(x, "name"),
                        getTextOrEmpty(x, "className"),
                        getOrEmptyIfEqual(x, "classSource", "PHB"),
                        getTextOrEmpty(x, "level"),
                        featureSource.isBlank() ? "" : "|" + featureSource)
                        .toLowerCase();
            }
            case subclassfeature: {
                String scSource = getOrEmptyIfEqual(x, "subclassSource", "PHB");
                String scFeatureSource = getOrEmptyIfEqual(x, "source", "PHB");
                return String.format("%s|%s|%s|%s|%s|%s|%s%s",
                        type,
                        getTextOrEmpty(x, "name"),
                        getTextOrEmpty(x, "className"),
                        getOrEmptyIfEqual(x, "classSource", "PHB"),
                        getTextOrEmpty(x, "subclassShortName"),
                        scSource,
                        getTextOrEmpty(x, "level"),
                        scFeatureSource.equals(scSource) ? "" : "|" + scFeatureSource)
                        .toLowerCase();
            }
            case itementry: {
                String itEntrySource = getOrEmptyIfEqual(x, "source", "DMG");
                return String.format("%s|%s%s",
                        type,
                        getTextOrEmpty(x, "name"),
                        itEntrySource.isBlank() ? "" : "|" + itEntrySource)
                        .toLowerCase();
            }
            case optionalfeature: {
                String opFeatureSource = getOrEmptyIfEqual(x, "source", "PHB");
                return String.format("%s|%s%s",
                        type,
                        getTextOrEmpty(x, "name"),
                        opFeatureSource.isBlank() ? "" : "|" + opFeatureSource)
                        .toLowerCase();
            }
            default:
                return String.format("%s|%s|%s",
                        type,
                        getTextOrEmpty(x, "name"),
                        getTextOrEmpty(x, "source"))
                        .toLowerCase();
        }
    }

    public String getRefKey(IndexType type, String crossRef) {
        return String.format("%s|%s", type, crossRef).toLowerCase()
                // NOTE: correct reference inconsistencies in the original data
                .replaceAll("\\|phb\\|", "||")
                .replaceAll("\\|tce\\|8\\|tce", "|tce|8");
    }

    /**
     * For subclasses, class features, and subclass features,
     * cross references come directly from the class definition
     * (as a lookup for additional json sources).
     *
     * @param type Type of object to find
     * @param crossRef Pre-created cross reference string.
     * @return referenced JsonNode or null
     */
    public JsonNode getNode(String finalKey) {
        return nodeIndex.get(finalKey);
    }

    /**
     * Construct a simple key (for most elements) using the
     * type, name, and source.
     *
     * @param type Type of object
     * @param x JsonNode providing lookup elements (name, source)
     * @return JsonNode or null
     */
    public JsonNode getNode(IndexType type, String name, String source) {
        String key = String.format("%s|%s|%s", type, name, source)
                .toLowerCase();
        return nodeIndex.get(key);
    }

    /**
     * Find the full JsonNode based on information from the node
     * passed in. Used for fluff nodes, and to find the original node
     * for a copy.
     *
     * @param type Type of object
     * @param x JsonNode providing lookup elements (name, source)
     * @return JsonNode or null
     */
    public JsonNode getNode(IndexType type, JsonNode x) {
        return nodeIndex.get(getKey(type, x));
    }

    public String lookupName(IndexType type, String name) {
        String prefix = String.format("%s|%s|", type, name).toLowerCase();
        List<String> target = nodeIndex.keySet().stream()
                .filter(k -> k.startsWith(prefix))
                .collect(Collectors.toList());

        if (target.isEmpty()) {
            Log.debugf("Did not find element for %s", name);
            return name;
        } else if (target.size() > 1) {
            Log.debugf("Found several elements for %s: %s", name, target);
        }
        return nodeIndex.get(target.get(0)).get("name").asText();
    }

    public boolean sourceIncluded(String source) {
        return allSources || allowedSources.contains(source);
    }

    public boolean excludeElement(String key, JsonNode element, CompendiumSources sources) {
        if (excludedKeys.contains(key)) {
            return true;
        }
        if (allSources) {
            return false;
        }
        if (allowedSources.isEmpty()) {
            return !element.has("srd"); // exclude non-SRD sources when no filter is specified.
        }
        return sources.bookSources.stream().noneMatch(x -> allowedSources.contains(x));
    }

    public boolean excludeItem(JsonNode itemSource, boolean isSRD) {
        if (allSources) {
            return false;
        }
        if (allowedSources.isEmpty()) {
            return !isSRD; // exclude non-SRD sources when no filter is specified.
        }
        if (itemSource == null || !itemSource.isTextual()) {
            return true; // unlikely, but skip items if we can't check their source
        }
        return !allowedSources.contains(itemSource.asText());
    }

    private String getTextOrEmpty(JsonNode x, String field) {
        return getTextOrDefault(x, field, "").trim();
    }

    private String getOrEmptyIfEqual(JsonNode x, String field, String expected) {
        if (x.has(field)) {
            String value = x.get(field).asText().trim();
            return value.equalsIgnoreCase(expected) ? "" : value;
        }
        return "";
    }

    private String getTextOrDefault(JsonNode x, String field, String defaultValue) {
        if (x.has(field)) {
            return x.get(field).asText();
        }
        return defaultValue;
    }

    public boolean keyIsExcluded(String key) {
        if (excludedKeys.contains(key)) {
            return true;
        }
        if (allSources) {
            return false;
        }
        return allowedSources.stream().noneMatch(s -> key.contains(s)) && !sourceIncluded("PHB");
    }

    public void writeFilterIndex(Path output) throws StreamWriteException, DatabindException, IOException {
        List<String> keys = new ArrayList<>(getKeys());
        Collections.sort(keys);

        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);

        Import5eTools.MAPPER.writer()
                .with(pp)
                .writeValue(output.toFile(), Map.of("keys", keys));
    }
}
