package dev.ebullient.fc5.json5e;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import dev.ebullient.fc5.Import5eTools;
import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.json2xml.Json2XmlBase;

public class JsonIndex implements JsonBase {

    final static int CR_UNKNOWN = 100001;
    final static int CR_CUSTOM = 100000;

    // classfeature|ability score improvement|monk|phb|12
    static final String classFeature_1 = "classfeature\\|[^|]+\\|[^|]+\\|";
    static final String classFeature_2 = "\\|\\d+\\|?";
    // subclassfeature|blessed strikes|cleric|phb|death|dmg|8|uaclassfeaturevariants
    static final String subclassFeature_1 = "subclassfeature\\|[^|]+\\|[^|]+\\|";
    static final String subclassFeature_2 = "\\|[^|]+\\|";
    static final String subclassFeature_3 = "\\|\\d+\\|?";

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
        subrace,
        optionalfeature,
        other,
        trait;

        public boolean isOptional() {
            return this == subclass || this == subclassfeature || this == optionalfeature;
        }

        public static IndexType fromKey(String key) {
            for (IndexType t : values()) {
                if (key.startsWith(t.name() + "|")) {
                    return t;
                }
            }
            return null;
        }

        /**
         * When used as a filter, does the given type "match"?
         * (specifically, a subrace counts as a race)
         *
         * @param type IndexType to compare
         * @return true if specified type "matches" this type
         */
        public boolean matches(IndexType type) {
            if (this == race && type == subrace) {
                return true;
            }
            return type == this;
        }
    }

    private final boolean allSources;
    private final Set<String> allowedSources = new HashSet<>();
    private final Set<String> excludedKeys = new HashSet<>();
    private final Set<Pattern> excludedPatterns = new HashSet<>();
    private final Map<String, JsonNode> nodeIndex = new HashMap<>();
    private final Map<String, String> aliases = new HashMap<>();

    private final Set<String> srdKeys = new HashSet<>();
    private final Set<String> familiarKeys = new HashSet<>();
    private final Set<String> includeGroups = new HashSet<>();

    Pattern classFeaturePattern;
    Pattern subclassFeaturePattern;

    public JsonIndex(List<String> sources) {
        this.allowedSources.addAll(sources.stream().map(String::toLowerCase).collect(Collectors.toList()));
        allSources = allowedSources.contains("*");
        setClassFeaturePatterns();
    }

    public JsonIndex importTree(JsonNode node) {
        if (!node.isObject()) {
            return this;
        }
        node.withArray("background").forEach(x -> addToIndex(IndexType.background, x));
        node.withArray("backgroundFluff").forEach(x -> addToIndex(IndexType.backgroundfluff, x));
        node.withArray("class").forEach(x -> addToIndex(IndexType.classtype, x));
        node.withArray("subclass").forEach(x -> addToIndex(IndexType.subclass, x));
        node.withArray("classFeature").forEach(x -> addToIndex(IndexType.classfeature, x));
        node.withArray("optionalfeature").forEach(x -> addToIndex(IndexType.optionalfeature, x));
        node.withArray("subclassFeature").forEach(x -> addToIndex(IndexType.subclassfeature, x));
        node.withArray("feat").forEach(x -> addToIndex(IndexType.feat, x));
        node.withArray("baseitem").forEach(x -> addToIndex(IndexType.item, x));
        node.withArray("item").forEach(x -> addToIndex(IndexType.item, x));
        node.withArray("itemEntry").forEach(x -> addToIndex(IndexType.itementry, x));
        node.withArray("itemFluff").forEach(x -> addToIndex(IndexType.itemfluff, x));
        node.withArray("monster").forEach(x -> addToIndex(IndexType.monster, x));
        node.withArray("monsterFluff").forEach(x -> addToIndex(IndexType.monsterfluff, x));
        node.withArray("race").forEach(x -> addToIndex(IndexType.race, x));
        node.withArray("subrace").forEach(x -> addToIndex(IndexType.subrace, x));
        node.withArray("raceFluff").forEach(x -> addToIndex(IndexType.racefluff, x));
        node.withArray("spell").forEach(x -> addToIndex(IndexType.spell, x));
        node.withArray("spellFluff").forEach(x -> addToIndex(IndexType.spellfluff, x));
        node.withArray("trait").forEach(x -> addToIndex(IndexType.trait, x));
        node.withArray("from").forEach(x -> updateSources(x.asText().toLowerCase()));
        node.withArray("includeGroups").forEach(x -> includeGroups.add(x.asText()));
        node.withArray("exclude").forEach(x -> excludedKeys.add(x.asText().toLowerCase()));
        node.withArray("excludePattern").forEach(x -> addExcludePattern(x.asText().toLowerCase()));
        return this;
    }

    void addToIndex(IndexType type, JsonNode node) {
        String key = getKey(type, node);
        nodeIndex.put(key, node);
        if (type == IndexType.subclass) {
            String aliasKey = getSubclassKey(node.get("shortName").asText().trim(),
                    node.get("className").asText(), node.get("classSource").asText());
            // add subclass to alias. Referenced from spells
            aliases.put(aliasKey, key);
        }
        if (node.has("srd")) {
            srdKeys.add(key);
        }
        if (node.has("familiar")) {
            familiarKeys.add(key);
        }
    }

    void addExcludePattern(String value) {
        String[] split = value.split("\\|");
        if (split.length > 1) {
            for (int i = 0; i < split.length - 1; i++) {
                if (!split[i].endsWith("\\")) {
                    split[i] += "\\";
                }
            }
        }
        excludedPatterns.add(Pattern.compile(String.join("|", split)));
    }

    void updateSources(String x) {
        allowedSources.add(x);
        setClassFeaturePatterns();
    }

    void setClassFeaturePatterns() {
        String allowed = allowedSources.contains("*") ? "([^|]+)" : "(" + String.join("|", allowedSources) + ")";
        classFeaturePattern = Pattern.compile(classFeature_1 + allowed + classFeature_2 + allowed + "?");
        subclassFeaturePattern = Pattern
                .compile(subclassFeature_1 + allowed + subclassFeature_2 + allowed + subclassFeature_3 + allowed + "?");
    }

    public Iterable<Entry<String, JsonNode>> elements() {
        return nodeIndex.entrySet();
    }

    public Stream<JsonNode> subraces(String raceName, CompendiumSources sources) {
        String raceSource = String.join("|", sources.bookSources);
        String pattern = String.format("%s\\|[^|]+\\|%s\\|(%s)", IndexType.subrace, raceName, raceSource)
                .toLowerCase();
        return nodeIndex.entrySet().stream()
                .filter(e -> e.getKey().matches(pattern))
                .map(Entry::getValue);
    }

    public Stream<JsonNode> classElementsMatching(IndexType type, String className, String classSource) {
        String pattern = String.format("%s\\|[^|]+\\|%s\\|.*", type, className)
                .toLowerCase();
        return nodeIndex.entrySet().stream()
                .filter(e -> e.getKey().matches(pattern))
                .map(Entry::getValue);
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

    String sanitize(String string) {
        return string.replace("\"", "").replace("the ", "");
    }

    public CompendiumSources constructSources(IndexType type, JsonNode x) {
        return new CompendiumSources(type, getKey(type, x), x);
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
            case subrace:
                return String.format("%s|%s|%s|%s",
                        type,
                        getTextOrEmpty(x, "name"),
                        getTextOrEmpty(x, "raceName"),
                        getTextOrEmpty(x, "raceSource"))
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
     * @param finalKey Pre-created cross reference string (including type)
     * @return referenced JsonNode or null
     */
    public JsonNode getNode(String finalKey) {
        if (finalKey == null) {
            return null;
        }
        return nodeIndex.get(finalKey);
    }

    public JsonNode getNode(Json2XmlBase compendiumBase) {
        return nodeIndex.get(compendiumBase.getSources().key);
    }

    /**
     * Construct a simple key (for most elements) using the
     * type, name, and source.
     *
     * @param type Type of object
     * @param name name of the object
     * @param source Class sources
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
        if (x == null) {
            return null;
        }
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
        return allSources || allowedSources.contains(source.toLowerCase());
    }

    public boolean excludeElement(JsonNode element, CompendiumSources sources) {
        return keyIsExcluded(sources.key);
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
        return !allowedSources.contains(itemSource.asText().toLowerCase());
    }

    public boolean keyIsIncluded(String key) {
        if (excludedKeys.contains(key) ||
                excludedPatterns.stream().anyMatch(x -> x.matcher(key).matches())) {
            return false;
        }
        if (allSources) {
            return true;
        }
        if (allowedSources.isEmpty()) {
            return srdKeys.contains(key);
        }
        if (key.contains("classfeature|")) {
            // class features squish phb
            String featureKey = key.replace("||", "|phb|");
            return classFeaturePattern.matcher(featureKey).matches() || subclassFeaturePattern.matcher(featureKey).matches();
        }
        if (key.startsWith("monster|") && key.endsWith("mm")
                && includeGroups.contains("familiars") && familiarKeys.contains(key)) {
            return true;
        }
        if (key.startsWith("subrace|") || key.startsWith("subclass")) {
            // The key isn't enough on its own.. it doesn't contain the subrace/subclass source.
            JsonNode node = getSubresourceNode(key);
            if (node == null) {
                Log.debugf("Unable to find subclass for " + key);
                return false;
            }
            String rs = node.get("source").asText().toLowerCase();
            return allowedSources.contains(rs) && allowedSources.stream().anyMatch(source -> key.contains("|" + source));
        }

        return allowedSources.stream().anyMatch(source -> key.contains("|" + source));
    }

    JsonNode getSubresourceNode(String key) {
        JsonNode node = getNode(key);
        if (node == null) {
            node = getNode(aliases.get(key));
        }
        return node;
    }

    public boolean keyIsExcluded(String key) {
        return !keyIsIncluded(key);
    }

    public void writeIndex(Path outputFile) throws IOException {
        List<String> keys = new ArrayList<>(getKeys());
        Collections.sort(keys);
        writeFilterIndex(outputFile, keys);
    }

    public void writeSourceIndex(Path outputFile) throws IOException {
        if (allowedSources.contains("*")) {
            writeIndex(outputFile);
            return;
        }
        List<String> keys = getKeys().stream()
                .filter(this::keyIsIncluded)
                .collect(Collectors.toList());
        Collections.sort(keys);
        writeFilterIndex(outputFile, keys);
    }

    private void writeFilterIndex(Path outputFile, List<String> keys) throws IOException {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
        Import5eTools.MAPPER.writer()
                .with(pp)
                .writeValue(outputFile.toFile(), Map.of("keys", keys));
    }

    public JsonNode handleCopy(IndexType type, JsonNode jsonSource) {
        JsonNode _copy = jsonSource.get("_copy");
        if (_copy != null) {
            // Fix infinite loop: self-referencing copy
            if (type == IndexType.monsterfluff
                    && jsonSource.get("name").asText().equalsIgnoreCase("Derro Savant")
                    && _copy.get("name").asText().equalsIgnoreCase("Derro Savant")) {
                ((ObjectNode) _copy).set("name", new TextNode("Derro"));
            }
            JsonNode baseNode = getNode(type, _copy);
            if (baseNode != null) {
                // is the copy a copy?
                baseNode = handleCopy(type, baseNode);
                try {
                    String originKey = getKey(type, jsonSource);
                    return mergeNodes(originKey, baseNode, jsonSource);
                } catch (IllegalStateException | StackOverflowError e) {
                    throw new IllegalStateException("Unable to resolve copy " + _copy.toPrettyString());
                }
            }

        }
        return jsonSource;
    }

    public JsonNode cloneOrCopy(String originKey, JsonNode value, IndexType parentType, String parentName,
            String parentSource) {
        JsonNode parentNode = parentName == null ? null : getNode(parentType, parentName, parentSource);
        JsonNode copyNode = getNode(parentType, value.get("_copy"));
        if (parentNode == null && copyNode == null) {
            Log.errorf("both parent and requested copy are null? (from %s with _copy=%s)", originKey,
                    value.get("_copy").toPrettyString());
        } else if (parentNode == null) {
            value = mergeNodes(originKey, copyNode, value);
        } else if (copyNode == null) {
            value = mergeNodes(originKey, parentNode, value);
        } else {
            // base type first
            JsonNode mergeNode = mergeNodes(originKey, parentNode, copyNode);
            value = mergeNodes(originKey, mergeNode, value);
            Log.errorf("Check my work: %s is a copy of %s based on %s", originKey,
                    getTextOrEmpty(copyNode, "name"), getTextOrEmpty(parentNode, "name"));
        }
        return value;
    }

    JsonNode mergeNodes(String originKey, JsonNode baseNode, JsonNode childNode) {
        ObjectNode target = (ObjectNode) copyNode(baseNode);
        target.put("merged", true);
        target.remove("srd");
        target.remove("basicRules");
        target.remove("reprintedAs");
        target.remove("_versions");
        target.remove("_copy");

        JsonNode _copy = childNode.get("_copy");
        JsonNode _mod = _copy == null ? null : _copy.get("_mod");
        JsonNode _preserve = _copy == null ? null : _copy.get("_preserve");
        JsonNode overwrite = _copy == null ? null : _copy.get("overwrite");

        for (Iterator<String> it = childNode.fieldNames(); it.hasNext();) {
            String f = it.next();
            JsonNode childField = childNode.get(f);
            switch (f) {
                case "_copy":
                case "_mod":
                case "_versions":
                    // skip -- do not copy
                    break;
                case "ability":
                    if ((overwrite != null && overwrite.has("ability")) || !baseNode.has("ability")) {
                        target.set("ability", copyNode(childField));
                    } else {
                        ArrayNode cpyAbility = target.withArray("ability");
                        if (cpyAbility.size() != childField.size()) {
                            Log.errorf("Copy/Merge: Ability array lengths did not match (from %s)", originKey);
                            continue;
                        }
                        for (int i = 0; i < childField.size(); i++) {
                            mergeFields(originKey, childField.get(i), (ObjectNode) cpyAbility.get(i));
                        }
                    }
                    break;
                case "skillProficiencies":
                case "weaponProficiencies":
                case "armorProficiencies":
                case "languageProficiencies": {
                    ArrayNode cpyArray = target.withArray(f);
                    if ((overwrite != null && overwrite.has(f)) || cpyArray.isEmpty()) {
                        target.set(f, copyNode(childField));
                    } else {
                        // usually size of one... so just append fields
                        for (int i = 0; i < childField.size(); i++) {
                            mergeFields(originKey, childField.get(i), (ObjectNode) cpyArray.get(i));
                        }
                    }
                    break;
                }
                case "entries":
                    if (_mod == null) {
                        ArrayNode targetEntries = target.withArray("entries");
                        appendToArray(targetEntries, childField);
                    }
                    break;
                default:
                    if (childField == null) {
                        target.remove(f);
                    } else if (_preserve == null || !_preserve.has(f)) {
                        target.replace(f, copyNode(childField));
                    } else {
                        Log.debugf("Copy/Merge: Skip field %s (from %s)", f, originKey);
                    }
                    break;
            }
        }

        if (_mod != null) {
            _mod.fields().forEachRemaining(field -> {
                if (field.getKey().equals("*")) {
                    List.of("action", "bonus", "reaction", "trait", "legendary", "mythic", "variant", "spellcasting",
                            "legendaryHeader").forEach(x -> {
                                if (target.has(x)) {
                                    handleModifications(originKey, x, field.getValue(), target);
                                }
                            });
                } else if (field.getKey().equals("_")) {
                    handleModifications(originKey, null, field.getValue(), target);
                } else {
                    handleModifications(originKey, field.getKey(), field.getValue(), target);
                }
            });
        }

        return target;
    }

    JsonNode copyNode(JsonNode sourceNode) {
        try {
            return Import5eTools.MAPPER.readTree(sourceNode.toString());
        } catch (JsonProcessingException ex) {
            Log.errorf(ex, "Unable to copy %s", sourceNode.toString());
            throw new IllegalStateException("JsonProcessingException processing " + sourceNode);
        }
    }

    private ArrayNode sortArrayNode(ArrayNode array) {
        if (array == null || array.size() <= 1) {
            return array;
        }
        Set<JsonNode> elements = new TreeSet<>(Comparator.comparing(a -> a.asText().toLowerCase()));
        array.forEach(elements::add);
        ArrayNode sorted = Import5eTools.MAPPER.createArrayNode();
        elements.forEach(sorted::add);
        return sorted;
    }

    JsonNode copyReplaceNode(JsonNode sourceNode, Pattern replace, String with) {
        try {
            String modified = replace.matcher(sourceNode.toString()).replaceAll(with);
            return Import5eTools.MAPPER.readTree(modified);
        } catch (JsonProcessingException ex) {
            Log.errorf(ex, "Unable to copy %s", sourceNode.toString());
            throw new IllegalStateException("JsonProcessingException processing " + sourceNode);
        }
    }

    void mergeFields(String originKey, JsonNode sourceNode, ObjectNode targetNode) {
        sourceNode.fields().forEachRemaining(f -> {
            if ("choose".equals(f.getKey()) && targetNode.has("choose")) {
                Log.errorf("Merging choose is not supported (target %s from %s)", targetNode.toPrettyString(), originKey);
            } else {
                targetNode.set(f.getKey(), copyNode(f.getValue()));
            }
        });
    }

    void handleModifications(String originKey, String prop, JsonNode modInfo, JsonNode target) {
        if (modInfo.isTextual()) {
            if ("remove".equals(modInfo.asText())) {
                ((ObjectNode) target).remove(prop);
            } else {
                Log.errorf("Unknown modification mode: %s (from %s)", modInfo.toPrettyString(), originKey);
            }
        } else if (modInfo.isArray()) {
            modInfo.forEach(modItem -> doMod(originKey, modItem, prop, target));
        } else {
            doMod(originKey, modInfo, prop, target);
        }
    }

    void doMod(String originKey, JsonNode modItem, String modFieldName, JsonNode target) {
        switch (modItem.get("mode").asText()) {
            case "prependArr":
            case "appendArr":
            case "insertArr":
            case "removeArr":
            case "replaceArr":
            case "replaceOrAppendArr":
            case "appendIfNotExistsArr":
                doModArray(originKey, modItem, modFieldName, target);
                break;
            case "replaceTxt":
                doReplaceText(originKey, modItem, modFieldName, target);
                break;
            case "addSkills":
                doAddSkills(originKey, modItem, modFieldName, target);
                break;
            case "addSpells":
                doAddSpells(originKey, modItem, modFieldName, target);
                break;
            case "replaceSpells":
                doReplaceSpells(originKey, modItem, modFieldName, target);
                break;
            default:
                Log.errorf("Unknown modification mode: %s (from %s)", modItem.toPrettyString(), originKey);
                break;
        }
    }

    private void doReplaceSpells(String originKey, JsonNode modItem, String modFieldName, JsonNode target) {
        if (!target.has("spellcasting")) {
            throw new IllegalStateException("Can't add spells to a monster without spellcasting: " + originKey);
        }

        JsonNode targetSpellcasting = target.get("spellcasting").get(0);
        if (modItem.has("spells")) {
            JsonNode spells = modItem.get("spells");
            ObjectNode targetSpells = targetSpellcasting.with("spells");
            spells.fields().forEachRemaining(ss -> {
                if (targetSpells.has(ss.getKey())) {
                    JsonNode levelMetas = ss.getValue();
                    ObjectNode targetLevel = targetSpells.with(ss.getKey());
                    ArrayNode targetLevelSpells = targetLevel.withArray("spells");
                    levelMetas.forEach(x -> replaceArray(originKey, x, targetLevelSpells,
                            x.get("replace"), x.get("with")));
                    targetSpells.set(ss.getKey(), sortArrayNode(targetLevelSpells));
                }
            });

        }
        List.of("rest", "daily", "weekly", "yearly").forEach(prop -> {
            if (!modItem.has(prop)) {
                return;
            }
            ObjectNode targetGroup = targetSpellcasting.with(prop);
            for (int i = 1; i <= 9; ++i) {
                String key = i + "";
                if (modItem.get(prop).has(key)) {
                    modItem.get(prop).get(key).forEach(
                            sp -> replaceArray(originKey, sp, targetGroup.withArray(key),
                                    sp.get("replace"), sp.get("with")));
                    targetGroup.set(key, sortArrayNode(targetGroup.withArray(key)));
                }

                String e = i + "e";
                if (modItem.get(prop).has(e)) {
                    modItem.get(prop).get(e).forEach(
                            sp -> replaceArray(originKey, sp, targetGroup.withArray(e),
                                    sp.get("replace"), sp.get("with")));
                    targetGroup.set(e, sortArrayNode(targetGroup.withArray(e)));
                }
            }

        });

        Log.debugf("Replace spells %s", originKey);
    }

    private void doAddSpells(String originKey, JsonNode modItem, String modFieldName, JsonNode target) {
        if (!target.has("spellcasting")) {
            throw new IllegalStateException("Can't add spells to a monster without spellcasting: " + originKey);
        }
        Log.debugf("Add spells %s", originKey);
        ObjectNode targetSpellcasting = (ObjectNode) target.get("spellcasting").get(0);
        if (modItem.has("spells")) {
            JsonNode spells = modItem.get("spells");
            ObjectNode targetSpells = targetSpellcasting.with("spells");
            spells.fields().forEachRemaining(s -> {
                if (!targetSpells.has(s.getKey())) {
                    targetSpells.set(s.getKey(), sortArrayNode((ArrayNode) s.getValue()));
                } else {
                    JsonNode spellsNew = spells.get(s.getKey());
                    ObjectNode spellsTgt = targetSpells.with(s.getKey());
                    spellsNew.fields().forEachRemaining(ss -> {
                        if (!spellsTgt.has(ss.getKey())) {
                            spellsTgt.set(ss.getKey(), sortArrayNode((ArrayNode) ss.getValue()));
                        } else if (spellsTgt.get(ss.getKey()).isArray()) {
                            ArrayNode spellsArray = spellsTgt.withArray(ss.getKey());
                            appendToArray(spellsArray, copyNode(ss.getValue()));
                            targetSpells.set(s.getKey(), sortArrayNode(spellsArray));
                        } else if (spellsTgt.get(ss.getKey()).isObject()) {
                            throw new IllegalArgumentException(
                                    String.format("Object %s is not an array (referenced from %s)", ss.getKey(), originKey));
                        }
                    });
                }
            });
        }

        List.of("constant", "will", "ritual").forEach(prop -> {
            if (!modItem.has(prop)) {
                return;
            }
            ArrayNode targetGroup = targetSpellcasting.withArray(prop);
            modItem.get(prop).forEach(targetGroup::add);
            targetSpellcasting.set(prop, sortArrayNode(targetGroup));
        });

        List.of("rest", "daily", "weekly", "yearly").forEach(prop -> {
            if (!modItem.has(prop)) {
                return;
            }

            ObjectNode targetGroup = targetSpellcasting.with(prop);
            for (int i = 1; i <= 9; ++i) {
                String key = i + "";
                if (modItem.get(prop).has(key)) {
                    modItem.get(prop).get(key).forEach(
                            sp -> targetGroup.withArray(key).add(sp));
                    targetGroup.set(key, sortArrayNode(targetGroup.withArray(key)));
                }

                String e = i + "e";
                if (modItem.get(prop).has(e)) {
                    modItem.get(prop).get(e).forEach(
                            sp -> targetGroup.withArray(e).add(sp));
                    targetGroup.set(e, sortArrayNode(targetGroup.withArray(e)));
                }
            }
        });

    }

    private void doAddSkills(String originKey, JsonNode modItem, String modFieldName, JsonNode target) {
        Log.debugf("Add skills %s", originKey);
        ObjectNode targetSkills = target.with("skill");
        modItem.get("skills").fields().forEachRemaining(e -> {
            // mode: 1 = proficient; 2 = expert
            int mode = e.getValue().asInt();
            String skill = e.getKey();
            String ability = getAbilityForSkill(skill);
            int score = target.get(ability).asInt();
            double total = mode * crToPb(target.get("cr")) + getAbilityModNumber(score);
            String totalAsText = (total >= 0 ? "+" : "") + ((int) total);

            if (targetSkills.has(skill)) {
                if (targetSkills.get(skill).asDouble() < total) {
                    targetSkills.set(skill, new TextNode(totalAsText));
                }
            } else {
                targetSkills.set(skill, new TextNode(totalAsText));
            }
        });
    }

    private void doReplaceText(String originKey, JsonNode modItem, String modFieldName, JsonNode target) {
        String replace = modItem.get("replace").asText();
        String with = modItem.get("with").asText();
        JsonNode flags = modItem.get("flags");

        final Pattern pattern;
        if (flags != null) {
            int pFlags = 0;
            if (flags.asText().contains("i")) {
                pFlags |= Pattern.CASE_INSENSITIVE;
            }
            pattern = Pattern.compile(replace, pFlags);
        } else {
            pattern = Pattern.compile(replace);
        }

        JsonNode targetField = target.get(modFieldName);
        List<String> properties;
        final boolean findPlainText;
        if (modItem.has("props")) {
            properties = new ArrayList<>();
            modItem.withArray("props").forEach(x -> properties.add(x.isNull() ? "null" : x.asText()));
            properties.remove("null");
            findPlainText = properties.contains("null");
        } else {
            properties = List.of("entries", "headerEntries", "footerEntries");
            findPlainText = true;
        }

        if (findPlainText && targetField.isTextual()) {
            ((ObjectNode) target).set(modFieldName, copyReplaceNode(targetField, pattern, with));
            return;
        }

        targetField.forEach(x -> {
            if (!x.isObject()) {
                return;
            }
            properties.forEach(prop -> {
                if (x.has(prop)) {
                    ((ObjectNode) x).set(prop, copyReplaceNode(x.get(prop), pattern, with));
                }
            });
        });
    }

    void doModArray(String originKey, JsonNode modItem, String fieldName, JsonNode target) {
        JsonNode items = modItem.has("items") ? copyNode(modItem.get("items")) : null;
        ArrayNode tgtArray = target.withArray(fieldName);
        switch (modItem.get("mode").asText()) {
            case "prependArr":
                insertIntoArray(tgtArray, 0, items);
                break;
            case "appendArr":
                appendToArray(tgtArray, items);
                break;
            case "appendIfNotExistsArr":
                if (tgtArray.size() == 0) {
                    appendToArray(tgtArray, items);
                } else {
                    assert items != null;
                    if (items.isArray()) {
                        List<JsonNode> filtered = streamOf((ArrayNode) items)
                                .filter(it -> streamOf(tgtArray).noneMatch(it::equals))
                                .collect(Collectors.toList());
                        tgtArray.addAll(filtered);
                    } else {
                        if (streamOf(tgtArray).noneMatch(items::equals)) {
                            tgtArray.add(items);
                        }
                    }
                }
                break;
            case "insertArr": {
                int index = modItem.get("index").asInt();
                insertIntoArray(tgtArray, index, items);
                break;
            }
            case "removeArr": {
                removeFromArray(originKey, modItem, tgtArray, items);
                break;
            }
            case "replaceArr": {
                JsonNode replace = modItem.get("replace");
                replaceArray(originKey, modItem, tgtArray, replace, items);
                break;
            }
            case "replaceOrAppendArr": {
                JsonNode replace = modItem.get("replace");
                if (!replaceArray(originKey, modItem, tgtArray, replace, items)) {
                    appendToArray(tgtArray, items);
                }
                break;
            }
            default:
                Log.errorf("Unknown modification mode: %s (from %s)", modItem.toPrettyString(), originKey);
                break;
        }
    }

    void appendToArray(ArrayNode tgtArray, JsonNode items) {
        if (items == null) {
            return;
        }
        if (items.isArray()) {
            tgtArray.addAll((ArrayNode) items);
        } else {
            tgtArray.add(items);
        }
    }

    void insertIntoArray(ArrayNode tgtArray, int index, JsonNode items) {
        if (items == null) {
            return;
        }
        if (items.isArray()) {
            // iterate backwards so that items end up in the right order @ desired index
            for (int i = items.size() - 1; i >= 0; i--) {
                tgtArray.insert(index, items.get(i));
            }
        } else {
            tgtArray.insert(index, items);
        }
    }

    void removeFromArray(String originKey, JsonNode modItem, ArrayNode tgtArray, JsonNode items) {
        JsonNode names = modItem.get("names");
        if (modItem.has("names")) {
            if (names.isTextual()) {
                int index = findIndexByName(originKey, tgtArray, names.asText());
                if (index >= 0) {
                    tgtArray.remove(index);
                }
            } else if (names.isArray()) {
                modItem.withArray("names").forEach(name -> {
                    int index = findIndexByName(originKey, tgtArray, name.asText());
                    if (index >= 0) {
                        tgtArray.remove(index);
                    }
                });
            }
        } else if (items != null && items.isArray()) {
            items.forEach(x -> {
                int index = findIndex(tgtArray, x);
                if (index >= 0) {
                    tgtArray.remove(index);
                }
            });
        }
    }

    boolean replaceArray(String originKey, JsonNode modItem, ArrayNode tgtArray, JsonNode replace, JsonNode items) {
        if (items == null) {
            return false;
        }
        int index = -1;
        if (replace.isTextual()) {
            index = findIndexByName(originKey, tgtArray, replace.asText());
        } else if (replace.isObject() && replace.has("index")) {
            index = replace.get("index").asInt();
        } else {
            Log.errorf("Unknown modification mode: %s (from %s)", modItem.toPrettyString(), originKey);
            return false;
        }

        if (index >= 0) {
            tgtArray.remove(index);
            insertIntoArray(tgtArray, index, items);
            return true;
        }
        return false;
    }

    int findIndexByName(String originKey, ArrayNode haystack, String needle) {
        int index = -1;
        for (int i = 0; i < haystack.size(); i++) {
            if (haystack.get(i).isObject()) {
                if (haystack.get(i).has("name") && haystack.get(i).get("name").asText().equals(needle)) {
                    index = i;
                    break;
                }
            } else if (haystack.get(i).isTextual()) {
                if (haystack.get(i).asText().equals(needle)) {
                    index = i;
                    break;
                }
            } else {
                Log.errorf("Unknown entry type: %s (from %s)", haystack.get(i), originKey);
            }
        }
        return index;
    }

    int findIndex(ArrayNode haystack, JsonNode needle) {
        for (int i = 0; i < haystack.size(); i++) {
            if (haystack.get(i).equals(needle)) {
                return i;
            }
        }
        return -1;
    }

    int crToPb(JsonNode cr) {
        if (cr == null || cr.isTextual() && cr.asText().equals("Unknown")) {
            return 0;
        }
        String crValue = (cr.isTextual() ? cr.asText() : cr.get("cr").asText());
        double crDouble = crToNumber(crValue);
        if (crDouble < 5)
            return 2;
        return (int) Math.ceil(crDouble / 4) + 1;
    }

    private double crToNumber(String crValue) {
        if (crValue.equals("Unknown") || crValue.equals("\u2014") || crValue == null) {
            return CR_UNKNOWN;
        }
        String[] parts = crValue.trim().split("/");
        try {
            if (parts.length == 1) {
                return Double.valueOf(parts[0]);
            } else if (parts.length == 2) {
                return Double.valueOf(parts[0]) / Double.valueOf(parts[1]);
            }
        } catch (NumberFormatException nfe) {
            return CR_CUSTOM;
        }
        return 0;
    }

    int getAbilityModNumber(int abilityScore) {
        return (int) Math.floor((abilityScore - 10) / 2);
    };

    String getAbilityForSkill(String skill) {
        switch (skill) {
            case "athletics":
                return "str";
            case "acrobatics":
                return "dex";
            case "sleight of hand":
                return "dex";
            case "stealth":
                return "dex";
            case "arcana":
                return "int";
            case "history":
                return "int";
            case "investigation":
                return "int";
            case "nature":
                return "int";
            case "religion":
                return "int";
            case "animal handling":
                return "wis";
            case "insight":
                return "wis";
            case "medicine":
                return "wis";
            case "perception":
                return "wis";
            case "survival":
                return "wis";
            case "deception":
                return "cha";
            case "intimidation":
                return "cha";
            case "performance":
                return "cha";
            case "persuasion":
                return "cha";
        }
        throw new IllegalArgumentException("Unknown skill: " + skill);
    }

    @Override
    public JsonIndex getIndex() {
        return this;
    }

    @Override
    public CompendiumSources getSources() {
        return null;
    }
}
