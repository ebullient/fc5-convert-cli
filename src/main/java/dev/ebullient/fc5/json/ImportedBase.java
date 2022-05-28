package dev.ebullient.fc5.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.xml.bind.JAXBElement;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.xml.XmlModifierType;
import dev.ebullient.fc5.xml.XmlObjectFactory;

public abstract class ImportedBase {
    final static Pattern itemPattern = Pattern.compile("\\{@item ([^}|]+)\\|?[^}|]*\\|?([^}]*)\\}");
    final static Pattern dicePattern = Pattern.compile("\\{@(dice|damage) ([^}]+)\\}");
    final static Pattern chancePattern = Pattern.compile("\\{@chance ([^}]+)\\}");

    final XmlObjectFactory factory;

    final List<String> bookSources;
    final String sourceText;
    final String copyOf;
    final String name;
    final JsonNode jsonElement;

    ImportedBase(XmlObjectFactory factory, JsonNode jsonItem, String name) {
        Log.debugf("Reading %s", name);

        this.factory = factory;
        this.name = name;
        this.jsonElement = jsonItem;

        this.copyOf = jsonElement.has("_copy")
                ? jsonElement.get("_copy").get("name").asText()
                : null;
        this.bookSources = new ArrayList<>();
        this.sourceText = findSources();
    }

    public abstract void populateXmlAttributes(Predicate<String> sourceIncluded, Function<String, String> lookupName);

    private String findSources() {
        this.bookSources.add(jsonElement.get("source").asText());
        if (copyOf != null) {
            return "See: " + copyOf;
        }
        String srcText = String.format("%s p. %s", jsonElement.get("source").asText(), jsonElement.has("page")
                ? jsonElement.get("page").asText()
                : "unk");

        srcText += StreamSupport.stream(jsonElement.withArray("additionalSources").spliterator(), false)
                .peek(x -> this.bookSources.add(x.get("source").asText()))
                .map(x -> sourceAndPage(x))
                .collect(Collectors.joining(" | "));

        srcText += StreamSupport.stream(jsonElement.withArray("otherSources").spliterator(), false)
                .peek(x -> this.bookSources.add(x.get("source").asText()))
                .map(x -> sourceAndPage(x))
                .collect(Collectors.joining(" | "));

        return srcText;
    }

    private String sourceAndPage(JsonNode source) {
        if (source.has("page")) {
            return String.format("%s p. %s", source.get("source").asText(), source.get("page").asText());
        }
        return source.get("source").asText();
    }

    void appendEntry(StringBuilder text, JsonNode entry, final Set<String> diceRolls) {
        if (entry.isTextual()) {
            text.append(replaceText(entry.asText(), diceRolls)).append("\n");
        } else if (entry.isObject()) {
            String objectType = entry.get("type").asText();
            if ("table".equals(objectType)) {
                appendTableEntry(text, entry, diceRolls);
            } else if ("list".equals(objectType)) {
                appendListEntry(text, entry, diceRolls);
            } else if ("entries".equals(objectType)) {
                if (entry.has("name")) {
                    text.append("\n");
                    text.append(entry.get("name").asText()).append(": ");
                }
                entry.withArray("entries").forEach(e -> appendEntry(text, e, diceRolls));
            }
        } else {
            Log.errorf("Unknown entry type: %s", entry.toPrettyString());
        }
    }

    private void appendListEntry(StringBuilder text, JsonNode entry, final Set<String> diceRolls) {
        text.append("\n");
        entry.withArray("items").forEach(e -> {
            text.append("- ");
            appendEntry(text, e, diceRolls);
        });
    }

    private void appendTableEntry(StringBuilder text, JsonNode entry, final Set<String> diceRolls) {
        text.append("\n");
        if (entry.has("caption")) {
            text.append(entry.get("caption").asText()).append(": \n");
        }
        text.append(StreamSupport.stream(entry.withArray("colLabels").spliterator(), false)
                .map(x -> replaceText(x.asText(), diceRolls))
                .collect(Collectors.joining(" | ")))
                .append("\n");

        entry.withArray("rows").forEach(r -> text.append(StreamSupport.stream(r.spliterator(), false)
                .map(x -> replaceText(x.asText(), diceRolls))
                .collect(Collectors.joining(" | ")))
                .append("\n"));
    }

    private String replaceText(String input, final Set<String> diceRolls) {
        // {@item Ball Bearings (Bag of 1,000)|phb|bag of ball bearings}
        // {@item sphere of annihilation}
        // {@item spellbook|phb}
        Matcher m = itemPattern.matcher(input);
        String result = m.replaceAll((match) -> {
            if (match.groupCount() > 1) {
                return match.group(2);
            } else {
                return match.group(1);
            }
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
                .replaceAll("\\{@link ([^}]+)\\|([^}]+)\\}", "$1 ($2)") // this must come first
                .replaceAll("\\{@action ([^}]+)\\}", "$1")
                .replaceAll("\\{@creature ([^}]+)\\|[^}]*\\}", "$1")
                .replaceAll("\\{@condition ([^}]+)\\}", "$1")
                .replaceAll("\\{@dc ([^}]+)\\}", "DC $1")
                .replaceAll("\\{@filter ([^}]+)\\|[^}]*\\}", "$1")
                .replaceAll("\\{@book ([^}|]+)\\|[^}]*\\}", "\"$1\"")
                .replaceAll("\\{@hit ([^}]+)\\}", "+$1")
                .replaceAll("\\{@h\\}", "Hit: ")
                .replaceAll("\\{@i ([^}]+)\\}", "$1")
                .replaceAll("\\{@italic ([^}]+)\\}", "$1")
                .replaceAll("\\{@sense ([^}]+)\\}", "$1")
                .replaceAll("\\{@skill ([^}]+)\\}", "$1")
                .replaceAll("\\{@spell ([^}]+)\\|[^}]*\\}", "$1")
                .replaceAll("\\{@note (\\*|Note:)?\\s?([^}]+)\\}", "$1");
    }

    void addAbilityModifiers(List<JAXBElement<?>> attributes) {
        JsonNode abilityElement = jsonElement.get("ability");
        if (abilityElement == null) {
            return;
        }
        if (abilityElement.isObject()) {
            JsonNode staticValue = abilityElement.get("static");
            abilityObjectValue(attributes, staticValue == null
                    ? abilityElement
                    : staticValue, staticValue != null);
        } else if (abilityElement.isArray()) {

        } else if (abilityElement.isTextual()) {

        } else {
            Log.error("Unknown abilityElement: " + abilityElement.toPrettyString());
        }
    }

    private void abilityObjectValue(List<JAXBElement<?>> attributes, JsonNode abilityOrStaticElement, boolean isStatic) {
        if (abilityOrStaticElement.has("from") || abilityOrStaticElement.has("choose")) {
            return;
        }
        String type = isStatic ? "Score" : "Modifier";
        abilityOrStaticElement.fields().forEachRemaining(entry -> {
            XmlModifierType smt = new XmlModifierType(
                    String.format("%s %s %s", entry.getKey(), type, entry.getValue().asText()),
                    "Ability " + type);
            attributes.add(factory.createItemTypeModifier(smt));
        });
    }

    String abilityToString(String ability) {
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
        throw new IllegalStateException("Unknown ability: " + ability);
    }
}
