package dev.ebullient.fc5.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NodeParser {
    private static final List<String> EMPTY_TEXT = Collections.emptyList();

    private NodeParser() {
    }

    public static <T> T getOrDefault(Map<String, Object> elements, String key, T defaultValue) {
        if (defaultValue instanceof Text) {
            return (T) new Text(getOrDefault(elements, key, EMPTY_TEXT));
        }
        return convertObject(elements.get(key), defaultValue);
    }

    public static <T> T convertObject(Object o, T defaultValue) {
        if (o == null) {
            return defaultValue;
        }
        try {
            if (defaultValue instanceof Double) {
                if (o instanceof String && ((String) o).isBlank()) {
                    return defaultValue;
                }
                return (T) Double.valueOf((String) o);
            }
            if (defaultValue instanceof Integer) {
                if (o instanceof String && ((String) o).isBlank()) {
                    return defaultValue;
                }
                return (T) Integer.valueOf((String) o);
            }
            if (defaultValue instanceof List && !(o instanceof List)) {
                return (T) Collections.singletonList(o);
            }
            return (T) o;
        } catch (ClassCastException ex) {
            System.err.println("Error casting " + o + " with defaultValue " + defaultValue);
            throw ex;
        }
    }

    public static Map<String, Object> parseNodeElements(Node parent) {
        Map<String, Object> result = new HashMap<>();
        NodeList list = parent.getChildNodes();

        for (int i = 0; i < list.getLength(); i++) {
            Node child = list.item(i);
            String key = child.getNodeName();
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Object obj = result.get(key);
                Object newObj = parseNode(child);
                if (obj == null) {
                    result.put(key, newObj);
                } else {
                    List<Object> multiple = (List<Object>) result.computeIfPresent(key, NodeParser::convertToList);
                    multiple.add(newObj);
                }
            }
        }
        return result;
    }

    public static Object parseNode(Node node) {
        switch (node.getNodeName()) {
            case "autolevel":
                return new Autolevel(node);
            case "dmg1":
            case "dmg2":
                return new Roll(node.getTextContent());
            case "dmgType":
                return DamageEnum.fromValue(node.getTextContent());
            case "feature":
                return new Feature(node);
            case "modifier":
                return new Modifier(node);
            case "proficiency":
                return new Proficiency(node.getTextContent());
            case "magic":
            case "ritual":
            case "stealth":
                return parseBoolean(node.getTextContent());
            case "roll":
                return new Roll(node.getTextContent());
            case "school":
                return SchoolEnum.fromValue(node.getTextContent());
            case "size":
                return SizeEnum.fromValue(node.getTextContent());
            case "slots":
                return new SpellSlots(node);
            case "spellAbility":
                return AbilityEnum.fromValue(node.getTextContent());
            case "action":
            case "legendary":
            case "reaction":
            case "trait":
                return new Trait(parseNodeElements(node));
            case "ability":
            case "ac":
            case "alignment":
            case "armor":
            case "attack":
            case "cha":
            case "classes":
            case "con":
            case "components":
            case "conditionImmune":
            case "cr":
            case "description":
            case "detail":
            case "dex":
            case "duration":
            case "environment":
            case "hd":
            case "hp":
            case "immune":
            case "int":
            case "languages":
            case "level":
            case "name":
            case "numSkills":
            case "passive":
            case "prerequisite":
            case "property":
            case "range":
            case "resist":
            case "save":
            case "senses":
            case "skill":
            case "source":
            case "speed":
            case "spells":
            case "str":
            case "strength":
            case "text":
            case "time":
            case "tools":
            case "type":
            case "value":
            case "vulnerable":
            case "wealth":
            case "weapons":
            case "weight":
            case "wis":
                return node.getTextContent();
            default:
                System.out.println("Unknown type: " + node.getNodeName());
                return null;
        }
    }

    public static Boolean parseBoolean(String value) {
        if (value == null) {
            return false;
        }
        switch (value.toLowerCase()) {
            case "yes":
            case "true":
            case "1":
                return true;
            default:
                return false;
        }
    }

    private static List<Object> convertToList(String key, Object existing) {
        if (existing instanceof List) {
            return (List) existing;
        } else {
            List<Object> newList = new ArrayList<>();
            newList.add(existing);
            return newList;
        }
    }
}
