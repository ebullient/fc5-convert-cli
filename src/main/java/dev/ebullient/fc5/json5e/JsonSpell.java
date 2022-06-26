package dev.ebullient.fc5.json5e;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.pojo.SchoolEnum;

public interface JsonSpell extends JsonBase {

    default boolean spellIsRitual(JsonNode jsonSource) {
        boolean ritual = false;
        JsonNode meta = jsonSource.get("meta");
        if (meta != null) {
            ritual = booleanOrDefault(meta, "ritual", false);
        }
        return ritual;
    }

    default int spellLevel(JsonNode jsonSource) {
        return jsonSource.get("level").asInt();
    }

    default SchoolEnum spellSchool(JsonNode jsonSource) {
        return SchoolEnum.fromEncodedValue(jsonSource.get("school").asText());
    }

    default String spellClassesString(JsonNode jsonSource) {
        JsonNode node = jsonSource.get("classes");
        if (node == null || node.isNull()) {
            return "";
        }
        Collection<String> classes = spellClasses(node, jsonSource.get("school"));
        return String.join(", ", classes);
    }

    default Collection<String> spellClasses(JsonNode jsonSource, JsonNode jsonSchool) {
        Set<String> classes = new TreeSet<>();
        jsonSource.withArray("fromClassList").forEach(c -> {
            String className = c.get("name").asText();
            String classSource = c.get("source").asText();
            if (includeClass(className, classSource)) {
                classes.add(className);
            }
        });
        jsonSource.withArray("fromSubclass").forEach(s -> {
            String className = s.get("class").get("name").asText().trim();
            if (classes.contains(className)) {
                return;
            }
            String classSource = s.get("class").get("source").asText();
            String subclassName = s.get("subclass").get("name").asText();
            if (includeSubclass(className, classSource, subclassName)) {
                classes.add(String.format("%s (%s)", className, subclassName));
            }
        });
        SchoolEnum school = SchoolEnum.fromEncodedValue(jsonSchool.asText());
        if (school == SchoolEnum.Abjuration || school == SchoolEnum.Evocation) {
            if (classes.contains("Wizard")) {
                classes.add("Fighter (Eldritch Knight)");
            }
        }
        return classes;
    }

    private boolean includeClass(String className, String classSource) {
        String finalKey = getIndex().getClassKey(className, classSource);
        return getIndex().keyIsIncluded(finalKey);
    }

    private boolean includeSubclass(String className, String classSource, String subclassName) {
        String finalKey = getIndex().getSubclassKey(subclassName.trim(), className.trim(), classSource.trim());
        return getIndex().keyIsIncluded(finalKey);
    }

    default List<String> spellComponents(JsonNode jsonSource) {
        JsonNode components = jsonSource.get("components");

        List<String> list = new ArrayList<>();
        components.fields().forEachRemaining(f -> {
            switch (f.getKey().toLowerCase()) {
                case "v":
                    list.add("V");
                    break;
                case "s":
                    list.add("S");
                    break;
                case "m":
                    if (f.getValue().isObject()) {
                        list.add(f.getValue().get("text").asText());
                    } else {
                        list.add(f.getValue().asText());
                    }
                    break;
            }
        });
        return list;
    }

    default String spellComponentsString(JsonNode jsonSource) {
        return String.join(", ", spellComponents(jsonSource));
    }

    default String spellDuration(JsonNode jsonSource) {
        StringBuilder result = new StringBuilder();
        JsonNode durations = jsonSource.withArray("duration");
        if (durations.size() > 0) {
            addDuration(durations.get(0), result);
        }
        if (durations.size() > 1) {
            result.append(", ");
            String type = getTextOrEmpty(durations.get(1), "type");
            if ("timed".equals(type)) {
                result.append(" up to ");
            }
            addDuration(durations.get(1), result);
        }
        return result.toString();
    }

    default void addDuration(JsonNode element, StringBuilder result) {
        String type = getTextOrEmpty(element, "type");
        switch (type) {
            case "instant":
                result.append("Instantaneous");
                break;
            case "permanent":
                result.append("Until dispelled");
                if (element.withArray("ends").size() > 1) {
                    result.append(" or triggered");
                }
                break;
            case "special":
                result.append("Special");
                break;
            case "timed": {
                if (booleanOrDefault(element, "concentration", false)) {
                    result.append("Concentration, up to ");
                }
                JsonNode duration = element.get("duration");
                result.append(duration.get("amount").asText())
                        .append(" ")
                        .append(duration.get("type").asText());
                break;
            }
            default:
                Log.errorf("What is this? %s", element.toPrettyString());
        }
    }

    default String spellRange(JsonNode jsonSource) {
        StringBuilder result = new StringBuilder();
        JsonNode range = jsonSource.get("range");
        if (range != null) {
            String type = getTextOrEmpty(range, "type");
            JsonNode distance = range.get("distance");
            switch (type) {
                case "cube":
                case "cone":
                case "hemisphere":
                case "line":
                case "radius":
                case "sphere": {
                    // Self (xx-foot yy)
                    result.append("Self (")
                            .append(distance.get("amount").asText())
                            .append("-")
                            .append(distance.get("type").asText())
                            .append(" ")
                            .append(type)
                            .append(")");
                    break;
                }
                case "point": {
                    String distanceType = distance.get("type").asText();
                    switch (distanceType) {
                        case "self":
                        case "sight":
                        case "touch":
                        case "unlimited":
                            result.append(distanceType.substring(0, 1).toUpperCase())
                                    .append(distanceType.substring(1));
                            break;
                        default:
                            result.append(distance.get("amount").asText())
                                    .append(" ")
                                    .append(distanceType);
                            break;
                    }
                    break;
                }
                case "special": {
                    result.append("Special");
                    break;
                }
            }
        }
        return result.toString();
    }

    default String spellCastingTime(JsonNode jsonSource) {
        JsonNode time = jsonSource.withArray("time").get(0);
        return String.format("%s %s",
                time.get("number").asText(),
                time.get("unit").asText());
    }

    default void collectTextAndRolls(JsonNode jsonSource, List<String> text, Set<String> diceRolls) {
        try {
            jsonSource.withArray("entries").forEach(entry -> appendEntryToText(text, entry, diceRolls));
            maybeAddBlankLine(text); // before Source
        } catch (Exception e) {
            Log.errorf(e, "Unable to parse text for %s", getSources());
        }
        text.add("Source: " + getSources().getSourceText());
    }
}
