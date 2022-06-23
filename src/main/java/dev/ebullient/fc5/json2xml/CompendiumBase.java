package dev.ebullient.fc5.json2xml;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.xml.bind.JAXBElement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.json5e.CompendiumSources;
import dev.ebullient.fc5.json5e.JsonIndex;
import dev.ebullient.fc5.json5e.JsonIndex.IndexType;
import dev.ebullient.fc5.json2xml.jaxb.XmlModifierType;
import dev.ebullient.fc5.json2xml.jaxb.XmlObjectFactory;
import dev.ebullient.fc5.json2xml.jaxb.XmlSizeEnum;
import dev.ebullient.fc5.json2xml.jaxb.XmlTraitType;

public abstract class CompendiumBase {
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

    final CompendiumSources sources;
    final JsonIndex index;
    final XmlObjectFactory factory;

    public CompendiumBase(CompendiumSources sources, JsonIndex index, XmlObjectFactory factory) {
        this.sources = sources;
        this.index = index;
        this.factory = factory;
    }

    public CompendiumSources getSources() {
        return sources;
    }

    public abstract List<CompendiumBase> convert(JsonNode value);

    public abstract Object getXmlCompendiumObject();

    String getName() {
        return this.sources.getName();
    }

    XmlSizeEnum getSize(JsonNode value) {
        JsonNode size = value.get("size");
        try {
            if (size == null) {
                return XmlSizeEnum.M;
            } else if (size.isTextual()) {
                return XmlSizeEnum.fromValue(size.asText());
            } else if (size.isArray()) {
                return XmlSizeEnum.fromValue(size.get(0).asText());
            }
        } catch (IllegalArgumentException ignored) {
        }
        Log.errorf("Unable to parse size for %s from %s", sources, size.toPrettyString());
        return XmlSizeEnum.M;
    }

    BigInteger getSpeed(JsonNode value) {
        JsonNode speed = value.get("speed");
        try {
            if (speed == null) {
                return BigInteger.valueOf(30);
            } else if (speed.isIntegralNumber()) {
                return BigInteger.valueOf(speed.asLong());
            } else if (speed.has("walk")) {
                return BigInteger.valueOf(speed.get("walk").asLong());
            }
        } catch (IllegalArgumentException ignored) {
        }
        Log.errorf("Unable to parse size for %s from %s", sources, speed);
        return BigInteger.valueOf(30);
    }

    void appendEntryToText(List<String> text, JsonNode entry) {
        appendEntryToText(text, entry, new ArrayList<>());
    }

    void appendEntryToText(List<String> text, JsonNode entry, final Collection<String> diceRolls) {
        if (entry == null) {
            return;
        }
        if (entry.isTextual()) {
            text.add(replaceText(entry.asText(), diceRolls));
        } else if (entry.isArray()) {
            entry.elements().forEachRemaining(f -> appendEntryToText(text, f, diceRolls));
        } else if (entry.isObject()) {
            String objectType = entry.get("type").asText();
            switch (objectType) {
                case "entry":
                case "entries":
                case "itemSpell":
                case "item": {
                    List<String> inner = new ArrayList<>();
                    appendEntryToText(inner, entry.get("entry"), diceRolls);
                    appendEntryToText(inner, entry.get("entries"), diceRolls);
                    prependField(entry, "name", inner);
                    text.addAll(inner);
                    break;
                }
                case "link": {
                    text.add(entry.get("text").asText());
                }
                case "list": {
                    appendList(text, entry, diceRolls);
                    break;
                }
                case "table": {
                    appendTable(text, entry, diceRolls);
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
                    appendClassFeature(text, "classFeature", IndexType.classfeature, entry);
                    break;
                }
                case "refSubclassFeature": {
                    appendClassFeature(text, "subclassFeature", IndexType.subclassfeature, entry);
                    break;
                }
                case "refOptionalfeature":
                    appendOptionalFeature(text, entry);
                    break;
                case "options": {
                    maybeAddBlankLine(text);
                    appendOptions(text, entry);
                    break;
                }
                case "inline": {
                    List<String> inner = new ArrayList<>();
                    appendEntryToText(inner, entry.get("entries"), diceRolls);
                    text.add(String.join("", inner));
                }
                case "quote":
                case "section":
                case "insetReadaloud":
                case "inset":
                    // skip this type
                    break;
                default:
                    Log.errorf("Unknown entry object type %s from %s: %s", objectType, sources, entry.toPrettyString());
            }
        } else {
            Log.errorf("Unknown entry type in %s: %s", sources, entry.toPrettyString());
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

    private void appendList(List<String> text, JsonNode entry, final Collection<String> diceRolls) {
        appendList("items", text, entry, diceRolls);
        maybeAddBlankLine(text);
    }

    public void appendList(String fieldName, List<String> text, JsonNode entry) {
        appendList(fieldName, text, entry, new ArrayList<>());
    }

    public void appendList(String fieldName, List<String> text, JsonNode entry, final Collection<String> diceRolls) {
        entry.withArray(fieldName).forEach(e -> {
            List<String> listText = new ArrayList<>();
            appendEntryToText(listText, e, diceRolls);
            if (listText.size() > 0) {
                prependText(LI, listText);
                text.addAll(listText);
            }
        });
    }

    private void appendTable(List<String> text, JsonNode entry, final Collection<String> diceRolls) {
        StringBuilder table = new StringBuilder();
        if (entry.has("caption")) {
            table.append(entry.get("caption").asText()).append(":\n");
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

    private void appendOptions(List<String> text, JsonNode entry) {
        List<String> list = new ArrayList<>();
        entry.withArray("entries").forEach(e -> {
            List<String> item = new ArrayList<>();
            appendEntryToText(item, e);
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

    private void appendOptionalFeature(List<String> text, JsonNode entry) {
        String finalKey = index.getRefKey(IndexType.optionalfeature, entry.get("optionalfeature").asText());
        if (index.keyIsExcluded(finalKey)) {
            return;
        }

        JsonNode ref = index.getNode(finalKey);
        if (ref == null) {
            Log.errorf("Could not find %s from %s", finalKey, sources);
        } else if (index.sourceIncluded(ref.get("source").asText())) {
            CompendiumSources featureSources = index.constructSources(IndexType.optionalfeature, ref);
            text.add(decoratedFeatureTypeName(featureSources, "", ref));
        }
    }

    private void appendClassFeature(List<String> text, String field, IndexType type, JsonNode entry) {
        String finalKey = index.getRefKey(type, entry.get(field).asText());
        JsonNode ref = index.getNode(finalKey);
        if (ref == null) {
            Log.errorf("Could not find %s from %s", finalKey, sources);
            return;
        }
        CompendiumSources featureSources = index.constructSources(type, ref);
        if (index.keyIsExcluded(featureSources.getKey())) {
            Log.debugf("Excluded: ", sources);
            return;
        }
        String subclassShortName = getTextOrEmpty(ref, "subclassShortName");
        text.add(LI + decoratedFeatureTypeName(featureSources, subclassShortName, ref));
    }

    String replaceText(String input) {
        return replaceText(input, new ArrayList<>());
    }

    String replaceText(String input, final Collection<String> diceRolls) {
        String result = index.replaceDiceTxt(input, diceRolls);
        return index.replaceAttributes(result);
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

    public void getFluffDescription(JsonNode value, IndexType fluffType, List<String> text) {
        if (booleanOrDefault(value, "hasFluff", false)) {
            JsonNode fluffNode = index.getNode(fluffType, value);
            if (fluffNode != null) {
                fluffNode = index.handleCopy(fluffType, fluffNode);
                if (fluffNode.has("entries")) {
                    appendEntryToText(text, fluffNode.get("entries"));
                }
            }
        }
    }

    public List<XmlTraitType> collectTraits(JsonNode array) {
        List<XmlTraitType> traits = new ArrayList<>();
        if (array != null) {
            array.forEach(entry -> traits.add(jsonToTraitType(entry)));
        }

        return traits;
    }

    public List<XmlTraitType> collectTraitsFromEntries(String properName, JsonNode value) {
        List<XmlTraitType> traits = new ArrayList<>();
        List<String> text = new ArrayList<>();
        Set<String> diceRolls = new HashSet<>();

        value.withArray("entries").forEach(entry -> {
            if (entry.isTextual()) {
                text.add(replaceText(entry.asText(), diceRolls));
            } else if (entry.isObject()) {
                if (entry.has("type") && "list".equals(entry.get("type").asText())) {
                    traits.addAll(collectTraits(entry.withArray("items")));
                } else {
                    XmlTraitType trait = jsonToTraitType(entry);
                    traits.add(trait);
                }
            }
        });

        if (text.size() > 0) {
            XmlTraitType baseTrait = createTraitType(properName, text, diceRolls);
            traits.add(baseTrait);
        }
        return traits;
    }

    public XmlTraitType jsonToTraitType(JsonNode jsonElement) {
        XmlTraitType trait = factory.createTraitType();
        Set<String> diceRolls = new HashSet<>();

        String traitName = "";
        List<JAXBElement<String>> traitAttributes = trait.getNameOrTextOrAttack();
        if (jsonElement.has("name")) {

            final Pattern rechargePattern = Pattern.compile("\\{@recharge *(\\d*).*\\}");
            Matcher m = rechargePattern.matcher(jsonElement.get("name").asText());
            traitName = m.replaceAll((match) -> {
                if (match.group(1).length() > 0) {
                    traitAttributes.add(factory.createTraitTypeRecharge("D" + match.group(1)));
                    return "(Recharge " + match.group(1) + "-6)";
                }
                traitAttributes.add(factory.createTraitTypeRecharge("D6"));
                return "(Recharge 6)";
            });

            traitName = replaceText(traitName).replaceAll(":$", "");
            traitAttributes.add(factory.createTraitTypeName(traitName));
            if (SPECIAL.contains(traitName)) {
                traitAttributes.add(factory.createTraitTypeSpecial(traitName));
            }
        }

        List<String> traitText = new ArrayList<>();
        if (jsonElement.has("entries")) {
            appendEntryToText(traitText, jsonElement.get("entries"), diceRolls);
        } else if (jsonElement.has("entry")) {
            appendEntryToText(traitText, jsonElement.get("entry"), diceRolls);
        }
        traitText.forEach(t -> traitAttributes.add(factory.createTraitTypeText(t)));

        final String attackName = traitName;
        diceRolls.forEach(r -> {
            if (r.contains("|")) { // attack
                traitAttributes.add(factory.createTraitTypeAttack(attackName + r.trim()));
            } else {
                if (r.startsWith("d")) {
                    r = "1" + r;
                }
                traitAttributes.add(factory.createItemTypeRoll(r));
            }
        });

        return trait;
    }

    public XmlTraitType createTraitType(String traitName, List<String> text) {
        return createTraitType(traitName, text, List.of());
    }

    public XmlTraitType createTraitType(String traitName, List<String> text, Collection<String> diceRolls) {
        XmlTraitType trait = factory.createTraitType();
        List<JAXBElement<String>> traitAttributes = trait.getNameOrTextOrAttack();

        traitAttributes.add(factory.createTraitTypeName(traitName));
        text.forEach(t -> traitAttributes.add(factory.createTraitTypeText(t)));

        diceRolls.forEach(r -> {
            if (r.startsWith("d")) {
                r = "1" + r;
            }
            traitAttributes.add(factory.createItemTypeRoll(r));
        });
        return trait;
    }

    public void addTraitTypeText(XmlTraitType trait, String text) {
        trait.getNameOrTextOrAttack().add(factory.createTraitTypeText(text));
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
            listNode.fieldNames().forEachRemaining(list::add);
            return String.join(", ", list);
        } else if (listNode.isArray()) {
            List<String> list = new ArrayList<>();
            listNode.elements().forEachRemaining(f -> {
                String inner = jsonToSkillList(f);
                if (!inner.isEmpty()) {
                    list.add(inner);
                }
            });
            return String.join(", ", list);
        }
        return "";
    }

    public String jsonArrayObjectToSkillBonusList(JsonNode listNode) {
        if (listNode.size() == 1 && !listNode.toString().contains("choose")) {
            List<String> list = new ArrayList<>();
            listNode.forEach(
                    e -> e.fields().forEachRemaining(f -> list.add(String.format("%s %s", f.getKey(), f.getValue().asText()))));
            return String.join(", ", list);
        }
        return "";
    }

    public String jsonObjectToSkillBonusList(JsonNode listNode) {
        if (listNode != null) {
            List<String> list = new ArrayList<>();
            listNode.fields().forEachRemaining(f -> list.add(String.format("%s %s", f.getKey(), f.getValue().asText())));
            if (!list.isEmpty()) {
                return String.join(", ", list);
            }
        }
        return "";
    }

    public void addAdditionalEntries(JsonNode jsonElement, List<String> text, Collection<String> diceRolls, String altSource) {
        jsonElement.withArray("additionalEntries").forEach(entry -> {
            if (entry.has("source") && !index.sourceIncluded(entry.get("source").asText())) {
                return;
            } else if (!index.sourceIncluded(altSource)) {
                return;
            }
            appendEntryToText(text, entry, diceRolls);
        });
    }

    public List<String> listPrerequisites(JsonNode value) {
        List<String> prereqs = new ArrayList<>();

        value.withArray("prerequisite").forEach(entry -> {
            if (entry.has("level")) {
                prereqs.add(levelToText(entry.get("level")));
            }
            entry.withArray("race").forEach(r -> prereqs.add(index.lookupName(IndexType.race, raceToText(r))));

            Map<String, List<String>> abilityScores = new HashMap<>();
            entry.withArray("ability").forEach(a -> a.fields().forEachRemaining(score -> abilityScores.computeIfAbsent(
                    score.getValue().asText(),
                    k -> new ArrayList<>()).add(asAbilityEnum(score.getKey()))));
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
            entry.withArray("feat").forEach(f -> prereqs
                    .add(featPattern.matcher(f.asText()).replaceAll(m -> index.lookupName(IndexType.feat, m.group(1)))));
            entry.withArray("feature").forEach(f -> prereqs.add(featPattern.matcher(f.asText())
                    .replaceAll(m -> index.lookupName(IndexType.optionalfeature, m.group(1)))));
            entry.withArray("background")
                    .forEach(f -> prereqs.add(index.lookupName(IndexType.background, f.get("name").asText()) + " background"));
            entry.withArray("item").forEach(i -> prereqs.add(index.lookupName(IndexType.item, i.asText())));

            if (entry.has("psionics")) {
                prereqs.add("Psionics");
            }

            List<String> profs = new ArrayList<>();
            entry.withArray("proficiency").forEach(f -> f.fields().forEachRemaining(field -> {
                String key = field.getKey();
                if ("weapon".equals(key)) {
                    key += "s";
                }
                profs.add(String.format("%s %s", key, field.getValue().asText()));
            }));
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

    String getTextOrDefault(JsonNode x, String field, String value) {
        if (x.has(field)) {
            return x.get(field).asText();
        }
        return value;
    }

    boolean booleanOrDefault(JsonNode source, String key, boolean value) {
        JsonNode result = source.get(key);
        return result == null ? value : result.asBoolean(value);
    }

    BigInteger integerOrDefault(JsonNode source, String key, long value) {
        JsonNode result = source.get(key);
        return BigInteger.valueOf(result == null ? value : result.asLong());
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

    public String decoratedFeatureTypeName(CompendiumSources valueSources, String subclassTitle, JsonNode value) {
        String name = decoratedTypeName(value.get("name").asText(), valueSources);
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
