package dev.ebullient.fc5.json;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.xml.bind.JAXBElement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import dev.ebullient.fc5.Import5eTools;
import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.json.JsonIndex.IndexType;
import dev.ebullient.fc5.xml.XmlModifierType;
import dev.ebullient.fc5.xml.XmlObjectFactory;
import dev.ebullient.fc5.xml.XmlSizeEnum;
import dev.ebullient.fc5.xml.XmlTraitType;

public abstract class CompendiumBase {
    final static Pattern itemPattern = Pattern.compile("\\{@item ([^|}]+)\\|?([^|}]*)\\|?([^|}]*)?\\}");
    final static Pattern spellPattern = Pattern.compile("\\{@spell ([^|}]+)\\|?([^|}]*)\\|?([^|}]*)?\\}");
    final static Pattern dicePattern = Pattern.compile("\\{@(dice|damage) ([^|}]+)[^}]*\\}");
    final static Pattern chancePattern = Pattern.compile("\\{@chance ([^}]+)\\}");
    final static Pattern featPattern = Pattern.compile("([^|]+)\\|?.*");

    public static final String LI = "• ";

    final static List<String> SPECIAL = List.of(
            "Fighting Style: Archery",
            "Fighting Style: Defense",
            "Fighting Style: Dueling",
            "Fighting Style: Two-Weapon Fighting",
            "Jack of all Trades",
            "Powerful Build",
            "Unarmored Defense: Charisma",
            "Unarmored Defense: Constitution",
            "Unarmored Defense: Dexterity",
            "Unarmored Defense: Intelligence",
            "Unarmored Defense: Strength",
            "Unarmored Defense: Wisdom");

    final String key;
    final JsonIndex index;
    final XmlObjectFactory factory;

    public CompendiumBase(String key, JsonIndex index, XmlObjectFactory factory) {
        this.key = key;
        this.index = index;
        this.factory = factory;
    }

    public abstract boolean convert(JsonNode value);

    public abstract Object getXmlCompendiumObject();

    List<CompendiumBase> variants() {
        return List.of(this);
    }

    XmlSizeEnum getSize(String name, JsonNode value) {
        JsonNode size = value.get("size");
        try {
            if (size == null) {
                return XmlSizeEnum.M;
            } else if (size.isTextual()) {
                return XmlSizeEnum.fromValue(size.asText());
            } else if (size.isArray()) {
                return XmlSizeEnum.fromValue(size.get(0).asText());
            }
        } catch (IllegalArgumentException ex) {
        }
        Log.errorf("Unable to parse size for %s from %s", name, size.toPrettyString());
        return XmlSizeEnum.M;
    }

    BigInteger getSpeed(String name, JsonNode value) {
        JsonNode speed = value.get("speed");
        try {
            if (speed == null) {
                return BigInteger.valueOf(30);
            } else if (speed.isIntegralNumber()) {
                return BigInteger.valueOf(speed.asLong());
            } else if (speed.has("walk")) {
                return BigInteger.valueOf(speed.get("walk").asLong());
            }
        } catch (IllegalArgumentException ex) {
        }
        Log.errorf("Unable to parse size for %s from %s", name, speed.toPrettyString());
        return BigInteger.valueOf(30);
    }

    void appendEntryToText(String name, List<String> text, JsonNode entry) {
        appendEntryToText(name, text, entry, new ArrayList<>());
    }

    void appendEntryToText(String name, List<String> text, JsonNode entry, final Collection<String> diceRolls) {
        if (entry == null) {
            return;
        }
        if (entry.isTextual()) {
            text.add(replaceText(entry.asText(), diceRolls));
        } else if (entry.isArray()) {
            entry.elements().forEachRemaining(f -> appendEntryToText(name, text, f, diceRolls));
        } else if (entry.isObject()) {
            String objectType = entry.get("type").asText();
            switch (objectType) {
                case "entries": {
                    List<String> inner = new ArrayList<>();
                    if (entry.has("entries")) {
                        appendEntryToText(name, inner, entry.get("entries"), diceRolls);
                    } else if (entry.has("entry")) {
                        appendEntryToText(name, inner, entry.get("entry"), diceRolls);
                    }
                    prependField(entry, "name", inner);
                    text.addAll(inner);
                    break;
                }
                case "itemSpell":
                case "item": {
                    List<String> inner = new ArrayList<>();
                    appendEntryToText(name, inner, entry.get("entry"), diceRolls);
                    prependField(entry, "name", inner);
                    text.addAll(inner);
                    break;
                }
                case "list": {
                    appendList(name, text, entry, diceRolls);
                    break;
                }
                case "table": {
                    appendTable(name, text, entry, diceRolls);
                    break;
                }
                case "abilityDc":
                    text.add(String.format("Spell save DC: 8 + your proficiency bonus + your %s modifier",
                            asAbilityEnum(entry.withArray("attributes").get(0))));
                    break;
                case "abilityAttackMod":
                    text.add(String.format("Spell attack modifier: your proficiency bonus + your %s modifier",
                            asAbilityEnum(entry.withArray("attributes").get(0))));
                    break;
                case "refClassFeature": {
                    appendClassFeature(name, text, "classFeature", IndexType.classfeature, entry);
                    break;
                }
                case "refSubclassFeature": {
                    appendClassFeature(name, text, "subclassFeature", IndexType.subclassfeature, entry);
                    break;
                }
                case "refOptionalfeature":
                    appendOptionalFeature(name, text, entry);
                    break;
                case "options": {
                    maybeAddBlankLine(text);
                    appendOptions(name, text, entry);
                    break;
                }
                case "quote":
                case "section":
                case "insetReadaloud":
                case "inset":
                    // skip this type
                    break;
                default:
                    Log.errorf("Unknown entry object type %s from %s: %s", objectType, name, entry.toPrettyString());
            }
        } else {
            Log.errorf("Unknown entry type in %s: %s", name, entry.toPrettyString());
        }
    }

    private void prependField(JsonNode entry, String fieldName, List<String> inner) {
        if (entry.has(fieldName)) {
            String n = entry.get(fieldName).asText();
            if (inner.isEmpty()) {
                inner.add(n);
            } else {
                inner.set(0, n + ": " + inner.get(0));
                inner.add(0, "");
            }
        }
    }

    private void prependText(String prefix, List<String> inner) {
        if (inner.isEmpty()) {
            inner.add(prefix);
        } else {
            if (inner.get(0).isEmpty() && inner.size() > 1) {
                inner.set(1, prependText(prefix, inner.get(1)));
            } else {
                inner.set(0, prependText(prefix, inner.get(0)));
            }
        }
    }

    private String prependText(String prefix, String text) {
        return text.startsWith(prefix) ? text : prefix + text;
    }

    private void appendList(String name, List<String> text, JsonNode entry, final Collection<String> diceRolls) {
        appendList(name, "items", text, entry, diceRolls);
    }

    public void appendList(String name, String fieldName, List<String> text, JsonNode entry) {
        appendList(name, fieldName, text, entry, new ArrayList<>());
    }

    public void appendList(String name, String fieldName, List<String> text, JsonNode entry,
            final Collection<String> diceRolls) {
        maybeAddBlankLine(text);
        entry.withArray(fieldName).forEach(e -> {
            List<String> listText = new ArrayList<>();
            appendEntryToText(name, listText, e, diceRolls);
            if (listText.size() > 0) {
                prependText(LI, listText);
                text.addAll(listText);
            }
        });
        maybeAddBlankLine(text);
    }

    private void appendTable(String name, List<String> text, JsonNode entry, final Collection<String> diceRolls) {
        StringBuilder table = new StringBuilder();
        if (entry.has("caption")) {
            table.append(entry.get("caption").asText() + ":\n");
        }
        table.append(StreamSupport.stream(entry.withArray("colLabels").spliterator(), false)
                .map(x -> replaceText(x.asText(), diceRolls))
                .collect(Collectors.joining(" | ")))
                .append("\n");

        entry.withArray("rows").forEach(r -> table.append(StreamSupport.stream(r.spliterator(), false)
                .map(x -> replaceText(x.asText(), diceRolls))
                .collect(Collectors.joining(" | ")))
                .append("\n"));

        maybeAddBlankLine(text);
        text.add(table.toString());
    }

    private void appendOptions(String name, List<String> text, JsonNode entry) {
        List<String> list = new ArrayList<>();
        entry.withArray("entries").forEach(e -> {
            List<String> item = new ArrayList<>();
            appendEntryToText(name, item, e);
            prependText(LI, item);
            list.addAll(item);
        });
        if (list.size() > 0) {
            maybeAddBlankLine(text);
            text.add("Options:");
            text.addAll(list);
            maybeAddBlankLine(text);
        }
    }

    public void maybeAddBlankLine(List<String> text) {
        if (text.size() > 0 && !text.get(text.size() - 1).isBlank()) {
            text.add("");
        }
    }

    private void appendOptionalFeature(String name, List<String> text, JsonNode entry) {
        String finalKey = index.getRefKey(IndexType.optionalfeature, entry.get("optionalfeature").asText());
        if (index.keyIsExcluded(finalKey)) {
            return;
        }
        JsonNode ref = index.getNode(finalKey);
        if (ref == null) {
            Log.errorf("Could not find %s from %s", finalKey, name);
        } else if (index.sourceIncluded(ref.get("source").asText())) {
            String featureKey = index.getKey(IndexType.optionalfeature, ref);
            String featureName = ref.get("name").asText();
            CompendiumSources sources = new CompendiumSources(featureKey, ref);
            text.add(decoratedFeatureTypeName(sources, "", featureName, ref));
        }
    }

    private void appendClassFeature(String name, List<String> text, String field, IndexType type, JsonNode entry) {
        String finalKey = index.getRefKey(type, entry.get(field).asText());
        if (index.keyIsExcluded(finalKey)) {
            return;
        }
        JsonNode ref = index.getNode(finalKey);
        if (ref == null) {
            Log.errorf("Could not find %s from %s", finalKey, name);
        } else if (index.sourceIncluded(ref.get("source").asText())) {
            String classKey = index.getKey(type, ref);
            String featureName = ref.get("name").asText();
            String subclassShortName = getTextOrEmpty(ref, "subclassShortName");
            CompendiumSources sources = new CompendiumSources(classKey, ref);
            text.add(LI + decoratedFeatureTypeName(sources, subclassShortName, featureName, ref));
        }
    }

    String replaceText(String input) {
        return replaceText(input, new ArrayList<>());
    }

    String replaceText(String input, final Collection<String> diceRolls) {
        // {@item Ball Bearings (Bag of 1,000)|phb|bag of ball bearings}
        // {@item sphere of annihilation}
        // {@item spellbook|phb}
        Matcher m = itemPattern.matcher(input);
        String result = m.replaceAll((match) -> {
            return match.group(1);
        });

        m = spellPattern.matcher(result);
        result = m.replaceAll((match) -> {
            return match.group(1)
                    + (match.groupCount() > 2 ? '*' : "");
        });

        m = dicePattern.matcher(result);
        result = m.replaceAll((match) -> {
            diceRolls.add(match.group(2));
            return match.group(2);
        });

        m = chancePattern.matcher(result);
        result = m.replaceAll((match) -> {
            diceRolls.add("1d100");
            return match.group(1) + "% chance";
        });

        return result
                .replaceAll("\\{@link ([^}|]+)\\|([^}]+)\\}", "$1 ($2)") // this must come first
                .replaceAll("\\{@5etools ([^}|]+)\\|?[^}]*\\}", "$1")
                .replaceAll("\\{@action ([^}]+)\\|?[^}]*\\}", "$1")
                .replaceAll("\\{@creature([^|}]+)\\|?[^}]*\\}", "$1")
                .replaceAll("\\{@condition ([^|}]+)\\|?[^}]*\\}", "$1")
                .replaceAll("\\{@dc ([^}]+)\\}", "DC $1")
                .replaceAll("\\{@d20 ([^}]+?)\\}", "$1")
                .replaceAll("\\{@filter ([^|}]+)\\|?[^}]*\\}", "$1")
                .replaceAll("\\{@background ([^|}]+)\\|?[^}]*\\}", "$1")
                .replaceAll("\\{@classFeature ([^|}]+)\\|?[^}]*\\}", "$1")
                .replaceAll("\\{@item ([^|}]+)\\|?[^}]*\\}", "$1")
                .replaceAll("\\{@race ([^|}]+)\\|?[^}]*\\}", "$1")
                .replaceAll("\\{@adventure ([^|}]+)\\|([^|}]+)\\|[^|}]*\\}", "$1 of $2")
                .replaceAll("\\{@adventure ([^|}]+)\\|[^|}]*\\}", "$1")
                .replaceAll("\\{@deity ([^|}]+)\\|?[^}]*\\}", "$1")
                .replaceAll("\\{@language ([^|}]+)\\|?[^}]*\\}", "$1")
                .replaceAll("\\{@quickref ([^|}]+)\\|?[^}]*\\}", "$1")
                .replaceAll("\\{@table ([^|}]+)\\|?[^}]*\\}", "$1")
                .replaceAll("\\{@variantrule ([^|}]+)\\|?[^}]*\\}", "$1")
                .replaceAll("\\{@optfeature ([^|}]+)\\|?[^}]*\\}", "$1")
                .replaceAll("\\{@feat ([^|}]+)\\|?[^}]*\\}", "$1")
                .replaceAll("\\{@book ([^}|]+)\\|?[^}]*\\}", "\"$1\"")
                .replaceAll("\\{@class ([^|}]+)\\|[^|]*\\|?([^|}]*)\\|?[^}]*\\}", "$2") // {@class Class||Usethis|...}
                .replaceAll("\\{@class ([^|}]+)\\}", "$1") // {@class Bard}
                .replaceAll("\\{@hit ([^}]+)\\}", "+$1")
                .replaceAll("\\{@h\\}", "Hit: ")
                .replaceAll("\\{@b ([^}]+?)\\}", "$1")
                .replaceAll("\\{@i ([^}]+?)\\}", "$1")
                .replaceAll("\\{@italic ([^}]+)\\}", "$1")
                .replaceAll("\\{@sense ([^}]+)\\}", "$1")
                .replaceAll("\\{@skill ([^}]+)\\}", "$1")
                .replaceAll("\\{@note (\\*|Note:)?\\s?([^}]+)\\}", "$1");
    }

    String asAbilityEnum(JsonNode textNode) {
        return asAbilityEnum(textNode.asText());
    }

    String asAbilityEnum(String ability) {
        switch (ability) {
            case "str":
                return "Strength";
            case "dex":
                return "Dexterity";
            case "con":
                return "Constitution";
            case "int":
                return "Intelligence";
            case "wis":
                return "Wisdom";
            case "cha":
                return "Charisma";
        }
        return ability;
    }

    public String getFluffDescription(String name, JsonNode value, IndexType fluffType, List<String> text) {
        if (booleanOrDefault(value, "hasFluff", false)) {
            JsonNode fluffNode = index.getNode(fluffType, value);
            if (fluffNode != null && fluffNode.has("entries")) {
                appendEntryToText(name, text, fluffNode.get("entries"));
            }
        }
        return text.toString();
    }

    public List<XmlTraitType> collectTraits(String name, JsonNode value) {
        List<XmlTraitType> traits = new ArrayList<>();
        List<String> text = new ArrayList<>();
        Set<String> diceRolls = new HashSet<>();

        value.withArray("entries").forEach(entry -> {
            if (entry.isTextual()) {
                text.add(replaceText(entry.asText(), diceRolls));
            } else if (entry.isObject()) {
                if (entry.has("type") && "list".equals(entry.get("type").asText())) {
                    entry.withArray("items").forEach(item -> {
                        XmlTraitType trait = jsonToTraitType(name, item, diceRolls);
                        traits.add(trait);
                    });
                } else {
                    XmlTraitType trait = jsonToTraitType(name, entry, diceRolls);
                    traits.add(trait);
                }
            }
        });

        if (text.size() > 0) {
            XmlTraitType baseTrait = createTraitType(name, text);
            traits.add(baseTrait);
        }
        return traits;
    }

    public XmlTraitType jsonToTraitType(String name, JsonNode jsonElement, Collection<String> diceRolls) {
        XmlTraitType trait = factory.createTraitType();

        List<JAXBElement<String>> traitAttributes = trait.getNameOrTextOrAttack();
        if (jsonElement.has("name")) {
            String traitName = jsonElement.get("name").asText().replaceAll(":$", "");
            traitAttributes.add(factory.createTraitTypeName(traitName));
            if (SPECIAL.contains(name)) {
                traitAttributes.add(factory.createTraitTypeSpecial(name));
            }
        }

        List<String> traitText = new ArrayList<>();
        if (jsonElement.has("entries")) {
            appendEntryToText(name, traitText, jsonElement.get("entries"), diceRolls);
        } else if (jsonElement.has("entry")) {
            appendEntryToText(name, traitText, jsonElement.get("entry"), diceRolls);
        }
        traitText.forEach(t -> traitAttributes.add(factory.createTraitTypeText(t)));

        return trait;
    }

    public XmlTraitType createTraitType(String name, List<String> text) {
        XmlTraitType baseTrait = factory.createTraitType();
        List<JAXBElement<String>> baseTraitAttributes = baseTrait.getNameOrTextOrAttack();
        baseTraitAttributes.add(factory.createTraitTypeName(name));
        text.forEach(t -> baseTraitAttributes.add(factory.createTraitTypeText(t)));
        return baseTrait;
    }

    public XmlTraitType addTraitTypeText(XmlTraitType trait, String text) {
        trait.getNameOrTextOrAttack().add(factory.createTraitTypeText(text));
        return trait;
    }

    List<XmlModifierType> collectModifierTypes(JsonNode value) {
        JsonNode abilityElement = value.get("ability");
        String type = value.toString().contains("score increases") ? "Score" : "Modifier";
        if (abilityElement == null) {
            return List.of();
        }
        if (abilityElement.has("static")) {
            return List.of();
        }
        if (abilityElement.has("choose")) {
            return chooseAbilityModifierFrom(type, abilityElement.get("choose"));
        }
        if (abilityElement.has("from")) {
            return chooseAbilityModifierFrom(type, abilityElement);
        }
        List<XmlModifierType> modifiers = new ArrayList<>();
        abilityElement.fields().forEachRemaining(entry -> {
            String ability = asAbilityEnum(entry.getKey());
            String amount = entry.getValue().asText();
            if (!amount.startsWith("-") && !amount.startsWith("+")) {
                amount = "+" + amount;
            }
            modifiers.add(new XmlModifierType(
                    String.format("%s %s", ability, amount), "Ability " + type));
        });
        return modifiers;
    }

    private List<XmlModifierType> chooseAbilityModifierFrom(String type, JsonNode choose) {
        if (!choose.has("amount")) {
            return List.of();
        }
        String amount = choose.get("amount").asText();
        if (!amount.startsWith("-") && !amount.startsWith("+")) {
            amount = "+" + amount;
        }
        int count = choose.has("count")
                ? choose.get("count").asInt()
                : 1;

        ArrayNode from = choose.withArray("from");
        List<XmlModifierType> modifiers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String ability = asAbilityEnum(from.get(i));
            modifiers.add(new XmlModifierType(
                    String.format("%s %s", ability, amount), "Ability " + type));
        }
        return modifiers;
    }

    public int chooseSkillListFrom(JsonNode choose, List<String> skillList) {
        int count = choose.has("count")
                ? choose.get("count").asInt()
                : 1;

        ArrayNode from = choose.withArray("from");
        from.forEach(s -> skillList.add(s.asText()));
        return count;
    }

    public String jsonToSkillList(JsonNode listNode) {
        if (listNode.isObject()
                && !listNode.toString().contains("choose")) {
            List<String> list = new ArrayList<>();
            listNode.fieldNames().forEachRemaining(f -> list.add(f));
            return String.join(", ", list);
        } else if (listNode.isArray()) {
            List<String> list = new ArrayList<>();
            listNode.elements().forEachRemaining(f -> list.add(jsonToSkillList(f)));
            return String.join(", ", list);
        }
        return null;
    }

    public String jsonToSkillBonusList(JsonNode listNode) {
        if (listNode.size() == 1 && !listNode.toString().contains("choose")) {
            List<String> list = new ArrayList<>();
            listNode.fields().forEachRemaining(f -> {
                list.add(String.format("%s %s", f.getKey(), f.getValue().asText()));
            });
            return String.join(", ", list);
        }
        return null;
    }

    JsonNode handleCopy(IndexType type, JsonNode jsonSource) {
        if (jsonSource.has("_copy")) {
            JsonNode copy = jsonSource.get("_copy");
            // is the copy a copy?
            JsonNode baseNode = handleCopy(type, index.getNode(type, copy));
            try {
                jsonSource = mergeNodes(type, baseNode, jsonSource);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Unable to find copy " + copy.toPrettyString());
            }
        }
        return jsonSource;
    }

    JsonNode mergeNodes(IndexType type, JsonNode baseNode, JsonNode value)
            throws JsonMappingException, JsonProcessingException {
        ObjectNode mergedNode = (ObjectNode) Import5eTools.MAPPER.readTree(baseNode.toString());
        mergedNode.put("merged", true);
        mergedNode.remove("srd");
        mergedNode.remove("source");
        mergedNode.remove("page");
        mergedNode.remove("basicRules");
        mergedNode.remove("reprintedAs");
        mergedNode.remove("hasFluff");

        value.fieldNames().forEachRemaining(f -> {
            JsonNode sourceNode = value.get(f);
            switch (f) {
                default:
                    mergedNode.replace(f, sourceNode);
                    break;
                case "_mod":
                    JsonNode modEntries = sourceNode.get("entries");
                    if (modEntries.isObject()) {
                        updateEntries(mergedNode, modEntries);
                    } else if (modEntries.isArray()) {
                        modEntries.elements().forEachRemaining(m -> updateEntries(mergedNode, m));
                    } else {
                        throw new IllegalStateException("Unknown modification: " + sourceNode.toPrettyString());
                    }
                    break;
                case "_copy":
                    Log.debugf("Merge: Skipping field %s", f);
            }
        });

        return mergedNode;
    }

    void updateEntries(JsonNode mergedNode, JsonNode modification) {
        ArrayNode entries = mergedNode.withArray("entries");
        switch (modification.get("mode").asText()) {
            case "insertArr":
                int index = modification.get("index").asInt();
                JsonNode items = modification.get("items");
                if (items.isArray()) {
                    // iterate backwards so they end up in the right order @ desired index
                    for (int i = items.size() - 1; i >= 0; i--) {
                        entries.insert(index, items.get(i));
                    }
                } else {
                    entries.insert(index, items);
                }
                break;
            case "replaceArr":
                break;
            default:
                throw new IllegalArgumentException("Unknown modification mode: " + modification.toPrettyString());
        }
    }

    public List<String> listPrerequisites(JsonNode value) {
        List<String> prereqs = new ArrayList<>();

        value.withArray("prerequisite").forEach(entry -> {
            if (entry.has("level")) {
                prereqs.add(levelToText(entry.get("level")));
            }
            entry.withArray("race").forEach(r -> {
                prereqs.add(index.lookupName(IndexType.race, raceToText(r)));
            });

            Map<String, List<String>> abilityScores = new HashMap<>();
            entry.withArray("ability").forEach(a -> {
                a.fields().forEachRemaining(score -> {
                    abilityScores.computeIfAbsent(
                            score.getValue().asText(),
                            k -> new ArrayList<>()).add(asAbilityEnum(score.getKey()));
                });
            });
            abilityScores.forEach(
                    (k, v) -> prereqs.add(String.format("%s %s or higher", String.join(" or ", v), k)));

            if (entry.has("spellcasting") && entry.get("spellcasting").asBoolean()) {
                prereqs.add("The ability to cast at least one spell");
            }
            if (entry.has("pact")) {
                prereqs.add("Pact of the " + entry.get("pact").asText());
            }
            if (entry.has("patron")) {
                prereqs.add(entry.get("patron").asText() + " Patron");
            }
            entry.withArray("spell").forEach(s -> {
                String text = s.asText().replaceAll("#c", "");
                prereqs.add(index.lookupName(IndexType.spell, text));
            });
            entry.withArray("feat").forEach(f -> {
                prereqs.add(featPattern.matcher(f.asText()).replaceAll(m -> index.lookupName(IndexType.feat, m.group(1))));
            });
            entry.withArray("feature").forEach(f -> {
                prereqs.add(featPattern.matcher(f.asText())
                        .replaceAll(m -> index.lookupName(IndexType.optionalfeature, m.group(1))));
            });
            entry.withArray("background").forEach(f -> {
                prereqs.add(index.lookupName(IndexType.background, f.get("name").asText()) + " background");
            });
            entry.withArray("item").forEach(i -> {
                prereqs.add(index.lookupName(IndexType.item, i.asText()));
            });
            if (entry.has("psionics")) {
                prereqs.add("Psionics");
            }

            List<String> profs = new ArrayList<>();
            entry.withArray("proficiency").forEach(f -> {
                f.fields().forEachRemaining(field -> {
                    String key = field.getKey();
                    if ("weapon".equals(key)) {
                        key += "s";
                    }
                    profs.add(String.format("%s %s", key, field.getValue().asText()));
                });
            });
            prereqs.add(String.format("Proficiency with %s", String.join(" or ", profs)));

            if (entry.has("other")) {
                prereqs.add(entry.get("other").asText());
            }
        });
        return prereqs;
    }

    String raceToText(JsonNode race) {
        StringBuilder str = new StringBuilder();
        str.append(race.get("name").asText());
        if (race.has("subrace")) {
            str.append(" (").append(race.get("subrace").asText()).append(")");
        }
        return str.toString();
    }

    String levelToText(JsonNode levelNode) {
        if (levelNode.isObject()) {
            List<String> levelText = new ArrayList<>();
            levelText.add(levelToText(levelNode.get("level").asText()));
            if (levelNode.has("class") || levelNode.has("subclass")) {
                JsonNode classNode = levelNode.get("class");
                if (classNode == null) {
                    classNode = levelNode.get("subclass");
                }
                boolean visible = !classNode.has("visible") || classNode.get("visible").asBoolean();
                JsonNode source = classNode.get("source");
                boolean included = source == null || index.sourceIncluded(source.asText());
                if (visible && included) {
                    levelText.add(classNode.get("name").asText());
                }
            }
            return String.join(" ", levelText);
        } else {
            return levelToText(levelNode.asText());
        }
    }

    String levelToText(String level) {
        switch (level) {
            case "1":
                return "1st level";
            case "2":
                return "2nd level";
            case "3":
                return "3rd level";
            default:
                return level + "th level";
        }
    }


    String joinAndReplace(ArrayNode array) {
        List<String> list = new ArrayList<>();
        array.forEach(v -> list.add(replaceText(v.asText())));
        return String.join(", ", list);
    }

    String getTextOrEmpty(JsonNode x, String field) {
        if (x.has(field)) {
            return x.get(field).asText();
        }
        return "";
    }

    boolean booleanOrDefault(JsonNode source, String key, boolean value) {
        JsonNode result = source.get(key);
        return result == null ? value : result.asBoolean(value);
    }

    String decoratedTypeName(String name, CompendiumSources sources) {
        if (sources.isPrimarySource("DMG") && !name.contains("(DMG)")) {
            return name + " (DMG)";
        }
        if (sources.isFromUA() && !name.contains("(UA)")) {
            return name + " (UA)";
        }
        return name;
    }

    public String decoratedFeatureTypeName(CompendiumSources sources, String subclassTitle, String name, JsonNode value) {
        name = decoratedTypeName(name, sources);

        String type = getTextOrEmpty(value, "featureType");
        if (!type.isEmpty()) {
            switch (type) {
                case "ED":
                    return "Elemental Discipline: " + name;
                case "EI":
                    return "Eldritch Invocation: " + name;
                case "MM":
                    return "Metamagic: " + name;
                case "MV":
                case "MV:B":
                case "MV:C2-UA":
                    return "Maneuver: " + name;
                case "FS:F":
                case "FS:B":
                case "FS:R":
                case "FS:P":
                    return "Fighting Style: " + name;
                case "AS":
                case "AS:V1-UA":
                case "AS:V2-UA":
                    return "Arcane Shot: " + name;
                case "PB":
                    return "Pact Boon: " + name;
                case "AI":
                    return "Artificer Infusion: " + name;
                case "SHP:H":
                case "SHP:M":
                case "SHP:W":
                case "SHP:F":
                case "SHP:O":
                    return "Ship Upgrade: " + name;
                case "IWM:W":
                    return "Infernal War Machine Variant: " + name;
                case "IWM:A":
                case "IWM:G":
                    return "Infernal War Machine Upgrade: " + name;
                case "OR":
                    return "Onomancy Resonant: " + name;
                case "RN":
                    return "Rune Knight Rune: " + name;
                case "AF":
                    return "Alchemical Formula: " + name;
                default:
                    Log.errorf("Unknown feature type %s for class feature %s", type, name);
            }
        }

        if (subclassTitle != null && !subclassTitle.isBlank()) {
            return subclassTitle + ": " + name;
        }

        return name;
    }
}
