package dev.ebullient.fc5.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.xml.XmlObjectFactory;

public abstract class CompendiumBase {
    final static Pattern itemPattern = Pattern.compile("\\{@item ([^}|]+)\\|?[^}|]*\\|?([^}]*)\\}");
    final static Pattern dicePattern = Pattern.compile("\\{@(dice|damage) ([^}]+)\\}");
    final static Pattern chancePattern = Pattern.compile("\\{@chance ([^}]+)\\}");

    final String key;
    final JsonIndex index;
    final XmlObjectFactory factory;
    final Set<String> bookSources;

    public CompendiumBase(String key, JsonIndex index, XmlObjectFactory factory) {
        this.key = key;
        this.index = index;
        this.factory = factory;
        this.bookSources = new HashSet<>();
    }

    public abstract boolean convert(JsonNode value);
    public abstract Object getXmlCompendiumObject();

    void appendEntryToText(StringBuilder text, JsonNode entry, final Collection<String> diceRolls) {
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
                entry.withArray("entries").forEach(e -> appendEntryToText(text, e, diceRolls));
            }
        } else {
            Log.errorf("Unknown entry type: %s", entry.toPrettyString());
        }
    }

    private void appendListEntry(StringBuilder text, JsonNode entry, final Collection<String> diceRolls) {
        text.append("\n");
        entry.withArray("items").forEach(e -> {
            text.append("- ");
            appendEntryToText(text, e, diceRolls);
        });
    }

    private void appendTableEntry(StringBuilder text, JsonNode entry, final Collection<String> diceRolls) {
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

    String replaceText(String input, final Collection<String> diceRolls) {
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

    String alternateSource() {
        Iterator<String> i = bookSources.iterator();
        if (bookSources.size() > 1) {
            i.next();
        }
        return i.next();
    }

    public String findSourceText(JsonNode jsonElement) {
        this.bookSources.add(jsonElement.get("source").asText());

        String copyOf = jsonElement.has("_copy")
            ? jsonElement.get("_copy").get("name").asText()
            : null;

        if (copyOf != null) {
            return "See: " + copyOf;
        }

        List<String> srcText = new ArrayList<>();
        srcText.add(sourceAndPage(jsonElement));

        if ( jsonElement.has("additionalSources")) {

        }
        srcText.addAll(StreamSupport.stream(jsonElement.withArray("additionalSources").spliterator(), false)
                .peek(x -> this.bookSources.add(x.get("source").asText()))
                .map(x -> sourceAndPage(x))
                .collect(Collectors.toList()));

                srcText.addAll(StreamSupport.stream(jsonElement.withArray("otherSources").spliterator(), false)
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
}
