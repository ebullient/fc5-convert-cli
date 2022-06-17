package dev.ebullient.fc5.json;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.json.JsonIndex.IndexType;
import dev.ebullient.fc5.xml.XmlMonsterType;
import dev.ebullient.fc5.xml.XmlObjectFactory;

public class CompendiumMonster extends CompendiumBase {
    String name;
    XmlMonsterType fc5Monster;
    List<JAXBElement<?>> attributes;
    CompendiumSources sources;

    public CompendiumMonster(String key, JsonIndex index, XmlObjectFactory factory) {
        super(key, index, factory);
    }

    public XmlMonsterType getXmlCompendiumObject() {
        return fc5Monster;
    }

    @Override
    public boolean convert(JsonNode jsonSource) {
        this.sources = new CompendiumSources(key, jsonSource);
        this.fc5Monster = factory.createMonsterType();
        this.attributes = fc5Monster.getNameOrSizeOrType();
        this.name = jsonSource.get("name").asText();

        if (index.excludeElement(key, jsonSource, sources)) {
            return false; // do not include
        }
        jsonSource = handleCopy(IndexType.monster, jsonSource);

        attributes.add(factory.createMonsterTypeName(decorateMonsterName(jsonSource)));
        attributes.add(factory.createMonsterTypeSize(getSize(name, jsonSource)));

        addMonsterType(jsonSource);
        addMonsterAlignment(jsonSource);
        addMonsterAc(jsonSource);
        addMonsterHp(jsonSource);
        addMonsterSpeed(jsonSource);
        addMonsterAbilities(jsonSource);
        // addMonsterAbilitySkillBonus(jsonSource);
        // addMonsterConditionsSensesLanguages(jsonSource);
        // addMonsterTraits(jsonSource);
        // addMonsterSpellcasting(jsonSource);
        // addMonsterDescription(jsonSource);
        // addMonsterEnvironment(jsonSource);

        return true; // do not include
    }

    private String decorateMonsterName(JsonNode source) {
        if (booleanOrDefault(source, "isNcp", false)) {
            return name + " (NPC)";
        }
        return name;
    }

    private void addMonsterType(JsonNode jsonSource) {
        JsonNode type = jsonSource.get("type");
        if (type.isTextual()) {
            attributes.add(factory.createMonsterTypeType(type.asText()));
        } else {
            StringBuilder result = new StringBuilder();
            result.append(type.get("type").asText());
            List<String> tags = new ArrayList<>();
            type.withArray("tags").forEach(tag -> {
                if (tag.isTextual()) {
                    tags.add(tag.asText());
                } else {
                    tags.add(String.format("%s %s",
                            tag.get("prefix").asText(),
                            tag.get("tag").asText()));
                }
            });
            if (!tags.isEmpty()) {
                result.append(" (")
                        .append(String.join(", ", tags))
                        .append(")");
            }
            attributes.add(factory.createMonsterTypeType(result.toString()));
        }
    }

    private void addMonsterAlignment(JsonNode jsonSource) {
        ArrayNode a1 = jsonSource.withArray("alignment");
        if (a1.size() == 0) {
            attributes.add(factory.createMonsterTypeAlignment("Unaligned"));
            return;
        }
        if (a1.size() == 1 && a1.get(0).has("special")) {
            attributes.add(factory.createMonsterTypeAlignment(a1.get(0).get("special").asText()));
            return;
        }

        String choices = a1.toString();
        if (choices.contains("note")) {
            List<String> notes = new ArrayList<>(List.of(choices.split("\\},\\{")));
            for (int i = 0; i < notes.size(); i++) {
                int pos = notes.get(i).indexOf("note");
                String alignment = mapAlignmentToString(toAlignmentCharacters(notes.get(i).substring(0, pos)));
                String note = notes.get(i).substring(pos + 4).replaceAll("[^A-Za-z ]+", "");
                notes.set(i, String.format("%s (%s)", alignment, note));
            }
            attributes.add(factory.createMonsterTypeAlignment(String.join(", ", notes)));
        } else {
            choices = toAlignmentCharacters(choices);
            attributes.add(factory.createMonsterTypeAlignment(mapAlignmentToString(choices)));
        }
    }

    String toAlignmentCharacters(String src) {
        return src.replaceAll("\"[A-Z]*[a-z ]+\"", "") // remove notes
                .replaceAll("[^LCNEGAUXY]", ""); // keep only alignment characters
    }

    private String mapAlignmentToString(String a) {
        switch (a) {
            case "A":
                return "Any alignment";
            case "C":
                return "Chaotic";
            case "CE":
                return "Chaotic Evil";
            case "CELENE":
                return "Any Evil Alignment";
            case "CG":
                return "Chaotic Good";
            case "CGNE":
                return "Chaotic Good or Neutral Evil";
            case "CGNYE":
                return "Any Chaotic alignment";
            case "CN":
                return "Chaotic Neutral";
            case "N":
                return "Neutral";
            case "NE":
                return "Neutral Evil";
            case "NG":
                return "Neutral Good";
            case "NGNE":
            case "NENG":
                return "Neutral Good or Neutral Evil";
            case "NNXNYN":
            case "NXCGNYE":
                return "Any Non-Lawful alignment";
            case "NX":
                return "Neutral";
            case "NY":
                return "Neutral";
            case "L":
                return "Lawful";
            case "LE":
                return "Lawful Evil";
            case "LG":
                return "Lawful Good";
            case "LN":
                return "Lawful Neutral";
            case "LNXCE":
                return "Any Evil Alignment";
            case "LNXCNYE":
                return "Any Non-Good alignment";
            case "E":
                return "Any Evil alignment";
            case "G":
                return "Any Good alignment";
            case "U":
                return "Unaligned";
        }
        Log.errorf("What alignment is this? %s (from %s)", a, key);
        return "Unknown";
    }

    private void addMonsterAc(JsonNode jsonSource) {
        StringBuilder result = new StringBuilder();
        List<String> details = new ArrayList<>();
        jsonSource.withArray("ac").forEach(ac -> {
            if (result.length() == 0) {
                if (ac.isNumber()) {
                    result.append(ac.asText());
                } else if (ac.has("special")) {
                    result.append(ac.get("special").asText());
                } else {
                    result.append(ac.get("ac").asText());
                    ac.withArray("from").forEach(f -> details.add(f.asText()));
                }
            } else {
                if (ac.isNumber()) {
                    details.add(ac.asText());
                } else {
                    StringBuilder value = new StringBuilder();
                    value.append(ac.get("ac").asText()).append(" from ");
                    ac.withArray("from").forEach(f -> value.append(f.asText()));
                    details.add(value.toString());
                }
            }
        });
        if (!details.isEmpty()) {
            result.append(" (")
                    .append(String.join(", ", details))
                    .append(")");
        }
        attributes.add(factory.createMonsterTypeAc(replaceText(result.toString())));
    }

    private void addMonsterSpeed(JsonNode jsonSource) {
        List<String> speed = new ArrayList<>();
        jsonSource.get("speed").fields().forEachRemaining(f -> {
            if (f.getValue().isNumber()) {
                speed.add(String.format("%s %s ft.", f.getKey(), f.getValue().asText()));
            } else if (f.getValue().has("number")) {
                speed.add(String.format("%s %s ft.%s",
                        f.getKey(),
                        f.getValue().get("number").asText(),
                        f.getValue().has("condition")
                                ? " " + f.getValue().get("condition").asText()
                                : ""));
            }
        });
        attributes.add(factory.createMonsterTypeSpeed(String.join(", ", speed)));
    }

    private void addMonsterHp(JsonNode jsonSource) {
        JsonNode health = jsonSource.get("hp");
        if (health.has("special")) {
            JsonNode special =  health.get("special");
            if ( special.isNumber() ) {
                attributes.add(factory.createMonsterTypeHp(special.asText()));
            } else {
                attributes.add(factory.createMonsterTypeHp(
                    special.asText().replaceAll("^(\\d+) .*", "$1")
                ));
            }
        } else if (health.has("formula")) {
            attributes.add(factory.createMonsterTypeHp(String.format("%s (%s)",
                    health.get("average").asText(),
                    health.get("formula").asText())));
        } else {
            Log.errorf("Unknown hp from %s: %s", name, health.toPrettyString());
        }
    }

    private void addMonsterAbilities(JsonNode jsonSource) {
    }

    private void addMonsterAbilitySkillBonus(JsonNode jsonSource) {
    }

    private void addMonsterConditionsSensesLanguages(JsonNode jsonSource) {
    }

    private void addMonsterTraits(JsonNode jsonSource) {
    }

    private void addMonsterSpellcasting(JsonNode jsonSource) {
    }

    private void addMonsterDescription(JsonNode jsonSource) {
    }

    private void addMonsterEnvironment(JsonNode jsonSource) {
    }

}
