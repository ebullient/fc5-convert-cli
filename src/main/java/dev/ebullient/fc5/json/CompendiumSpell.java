package dev.ebullient.fc5.json;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.JAXBElement;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.xml.XmlObjectFactory;
import dev.ebullient.fc5.xml.XmlSchoolEnum;
import dev.ebullient.fc5.xml.XmlSpellType;

public class CompendiumSpell extends CompendiumBase {
    String name;
    XmlSpellType fc5Spell;
    List<JAXBElement<?>> attributes;

    public CompendiumSpell(CompendiumSources sources, JsonIndex index, XmlObjectFactory factory) {
        super(sources, index, factory);
    }

    public XmlSpellType getXmlCompendiumObject() {
        return fc5Spell;
    }

    @Override
    public List<CompendiumBase> convert(JsonNode jsonSource) {
        if (index.keyIsExcluded(sources.key)) {
            Log.debugf("Excluded %s", sources.key);
            return List.of(); // do not include
        }

        this.fc5Spell = factory.createSpellType();
        this.attributes = fc5Spell.getNameOrLevelOrSchool();
        this.name = jsonSource.get("name").asText();

        attributes.add(factory.createSpellTypeName(name));
        addSpellLevel(jsonSource);
        addSpellSchool(jsonSource);
        addRitualTag(jsonSource);
        addSpellTime(jsonSource);
        addSpellRange(jsonSource);
        addSpellComponents(jsonSource);
        addSpellDuration(jsonSource);
        addSpellClasses(jsonSource);
        addSpellTextAndRolls(name, jsonSource);

        return List.of(this);
    }

    void addSpellLevel(JsonNode jsonSource) {
        int level = jsonSource.get("level").asInt();
        attributes.add(factory.createSpellTypeLevel(BigInteger.valueOf(level)));
    }

    void addSpellSchool(JsonNode jsonSource) {
        String s = jsonSource.get("school").asText();
        XmlSchoolEnum school = XmlSchoolEnum.fromValue(s);
        attributes.add(factory.createSpellTypeSchool(school));
    }

    void addRitualTag(JsonNode source) {
        boolean ritual = false;
        JsonNode meta = source.get("meta");
        if (meta != null) {
            ritual = booleanOrDefault(meta, "ritual", false);
        }
        attributes.add(factory.createSpellTypeRitual(ritual ? "YES" : "NO"));
    }

    private void addSpellClasses(JsonNode jsonSource) {
        Set<String> classes = new TreeSet<>();
        JsonNode node = jsonSource.get("classes");
        if (node == null) {
            return;
        }
        node.withArray("fromClassList").forEach(c -> {
            String className = c.get("name").asText();
            String classSource = c.get("source").asText();
            if (includeClass(className, classSource)) {
                classes.add(className);
            }
        });
        node.withArray("fromSubclass").forEach(s -> {
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
        XmlSchoolEnum school = XmlSchoolEnum.fromValue(jsonSource.get("school").asText());
        if (school == XmlSchoolEnum.A || school == XmlSchoolEnum.EV) {
            if (classes.contains("Wizard")) {
                classes.add("Fighter (Eldritch Knight)");
            }
        }
        attributes.add(factory.createSpellTypeClasses(String.join(", ", classes)));
    }

    private boolean includeClass(String className, String classSource) {
        String finalKey = index.getClassKey(className, classSource);
        return index.keyIsIncluded(finalKey);
    }

    private boolean includeSubclass(String className, String classSource, String subclassName) {
        String finalKey = index.getSubclassKey(subclassName.trim(), className.trim(), classSource.trim());
        return index.keyIsIncluded(finalKey);
    }

    private void addSpellComponents(JsonNode jsonSource) {
        JsonNode components = jsonSource.get("components");
        List<String> list = new ArrayList<>();
        if (booleanOrDefault(components, "V", false)) {
            list.add("V");
        }
        if (booleanOrDefault(components, "S", false)) {
            list.add("S");
        }
        if (components.has("M")) {
            list.add(components.get("m").asText());
        }
        attributes.add(factory.createSpellTypeComponents(String.join(", ", list)));
    }

    private void addSpellDuration(JsonNode jsonSource) {
        //<duration>8 hours</duration>
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

        attributes.add(factory.createSpellTypeDuration(result.toString()));
    }

    private void addDuration(JsonNode element, StringBuilder result) {
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
                return;
        }
    }

    private void addSpellRange(JsonNode jsonSource) {
        // <range>30 feet</range>
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
        attributes.add(factory.createSpellTypeRange(result.toString()));
    }

    private void addSpellTime(JsonNode jsonSource) {
        // <time>1 action</time>
        jsonSource.withArray("time").forEach(time -> {
            attributes.add(factory.createSpellTypeTime(String.format("%s %s",
                    time.get("number").asText(),
                    time.get("unit").asText())));
        });
    }

    void addSpellTextAndRolls(String name, JsonNode jsonSource) {
        Set<String> diceRolls = new HashSet<>();
        List<String> text = new ArrayList<>();
        String sourceText = sources.getSourceText();

        try {
            jsonSource.withArray("entries").forEach(entry -> appendEntryToText(text, entry, diceRolls));
            maybeAddBlankLine(text); // before Source
        } catch (Exception e) {
            Log.errorf(e, "Unable to parse text for %s", name);
        }

        text.add("Source: " + sourceText);
        text.forEach(t -> attributes.add(factory.createSpellTypeText(t)));

        diceRolls.forEach(r -> {
            if (r.startsWith("d")) {
                r = "1" + r;
            }
            attributes.add(factory.createSpellTypeRoll(r));
        });
    }
}
