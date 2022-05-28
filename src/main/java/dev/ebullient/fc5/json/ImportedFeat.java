package dev.ebullient.fc5.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBElement;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.xml.XmlFeatType;
import dev.ebullient.fc5.xml.XmlObjectFactory;

public class ImportedFeat extends ImportedBase {
    final static Pattern featPattern = Pattern.compile("([^|]+)\\|?.*");

    final XmlFeatType fc5Feat;
    final List<JAXBElement<?>> attributes;

    ImportedFeat(XmlObjectFactory factory, JsonNode jsonElement, String name) {
        super(factory, jsonElement, name);

        this.fc5Feat = factory.createFeatType();
        this.attributes = fc5Feat.getNameOrPrerequisiteOrSpecial();

        attributes.add(factory.createFeatTypeName(name));
    }

    public void populateXmlAttributes(final Predicate<String> sourceIncluded, final Function<String, String> lookupName) {
        if (copyOf != null) {
            return;
        }
        addAbilityModifiers(attributes);
        addItemText(sourceIncluded);
        addProficiencies(sourceIncluded, lookupName);
    }

    private String levelToText(final Predicate<String> sourceIncluded, JsonNode levelNode) {
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
                boolean included = source == null || sourceIncluded.test(source.asText());
                if (visible && included) {
                    levelText.add(classNode.get("name").asText());
                }
            }
            return String.join(" ", levelText);
        } else {
            return levelToText(levelNode.asText());
        }
    }

    private String levelToText(String level) {
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

    private String raceToText(JsonNode race) {
        StringBuilder str = new StringBuilder();
        str.append(race.get("name").asText());
        if (race.has("subrace")) {
            str.append(" (").append(race.get("subrace").asText()).append(")");
        }
        return str.toString();
    }

    public void addItemText(final Predicate<String> sourceIncluded) {
        StringBuilder text = new StringBuilder();
        Set<String> diceRolls = new HashSet<>();

        jsonElement.withArray("entries").forEach(entry -> appendEntry(text, entry, diceRolls));

        text.append("\nSource: ").append(sourceText);
        attributes.add(factory.createItemTypeText(text.toString()));
    }

    private void addProficiencies(final Predicate<String> sourceIncluded, final Function<String, String> lookupName) {
        List<String> prereqs = new ArrayList<>();
        jsonElement.withArray("prerequisite").forEach(entry -> {
            if (entry.has("level")) {
                prereqs.add(levelToText(sourceIncluded, entry.get("level")));
            }
            entry.withArray("race").forEach(r -> {
                prereqs.add(lookupName.apply(raceToText(r)));
            });

            Map<String, List<String>> abilityScores = new HashMap<>();
            entry.withArray("ability").forEach(a -> {
                a.fields().forEachRemaining(score -> {
                    abilityScores.computeIfAbsent(
                            score.getValue().asText(),
                            k -> new ArrayList<>()).add(abilityToString(score.getKey()));
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
                prereqs.add(lookupName.apply(text));
            });
            entry.withArray("feat").forEach(f -> {
                prereqs.add(featPattern.matcher(f.asText()).replaceAll(m -> lookupName.apply(m.group(1))));
            });
            entry.withArray("feature").forEach(f -> {
                prereqs.add(featPattern.matcher(f.asText()).replaceAll(m -> lookupName.apply(m.group(1))));
            });
            entry.withArray("background").forEach(f -> {
                prereqs.add(lookupName.apply(f.get("name").asText()) + " background");
            });
            entry.withArray("item").forEach(i -> {
                prereqs.add(lookupName.apply(i.asText()));
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
            profs.add(String.format("Proficiency with %s", String.join(" or ", profs)));

            if (entry.has("other")) {
                profs.add(entry.get("other").asText());
            }
        });

        attributes.add(factory.createFeatTypePrerequisite(String.join(", ", prereqs)));
    }
}
