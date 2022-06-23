package dev.ebullient.fc5.json2xml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.JAXBElement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.json5e.CompendiumSources;
import dev.ebullient.fc5.json5e.JsonIndex;
import dev.ebullient.fc5.json5e.JsonIndex.IndexType;
import dev.ebullient.fc5.json2xml.jaxb.XmlMonsterType;
import dev.ebullient.fc5.json2xml.jaxb.XmlObjectFactory;
import dev.ebullient.fc5.json2xml.jaxb.XmlSlotsType;
import dev.ebullient.fc5.json2xml.jaxb.XmlTraitType;

public class CompendiumMonster extends CompendiumBase {

    XmlMonsterType fc5Monster;
    List<JAXBElement<?>> attributes;

    public CompendiumMonster(CompendiumSources sources, JsonIndex index, XmlObjectFactory factory) {
        super(sources, index, factory);
    }

    public XmlMonsterType getXmlCompendiumObject() {
        return fc5Monster;
    }

    @Override
    public List<CompendiumBase> convert(JsonNode jsonSource) {
        if (index.keyIsExcluded(sources.getKey())) {
            Log.debugf("Excluded %s", sources);
            return List.of(); // do not include
        }
        jsonSource = index.handleCopy(IndexType.monster, jsonSource);

        this.fc5Monster = factory.createMonsterType();
        this.attributes = fc5Monster.getNameOrSizeOrType();

        attributes.add(factory.createMonsterTypeName(decorateMonsterName(jsonSource)));
        attributes.add(factory.createMonsterTypeSize(getSize(jsonSource)));

        addMonsterType(jsonSource);
        addMonsterAlignment(jsonSource);
        addMonsterAc(jsonSource);
        addMonsterHp(jsonSource);
        addMonsterSpeed(jsonSource);
        addMonsterAbilities(jsonSource);
        addMonsterAbilitySkillBonus(jsonSource);
        addMonsterConditionsSensesLanguages(jsonSource);
        addMonsterCrProficiencyBonus(jsonSource); // profBonus needed for traits
        addMonsterSpellcasting(jsonSource);
        addMonsterTraits(jsonSource);
        addMonsterDescription(jsonSource);
        addMonsterEnvironment(jsonSource);

        return List.of(this); // do not include
    }

    private String decorateMonsterName(JsonNode source) {
        String revised = getName().replace("\"", "");
        if (booleanOrDefault(source, "isNpc", false)) {
            return revised + " (NPC)";
        }
        return revised;
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
        Log.errorf("What alignment is this? %s (from %s)", a, sources);
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
            JsonNode special = health.get("special");
            if (special.isNumber()) {
                attributes.add(factory.createMonsterTypeHp(special.asText()));
            } else {
                attributes.add(factory.createMonsterTypeHp(
                        special.asText().replaceAll("^(\\d+) .*", "$1")));
            }
        } else if (health.has("formula")) {
            attributes.add(factory.createMonsterTypeHp(String.format("%s (%s)",
                    health.get("average").asText(),
                    health.get("formula").asText())));
        } else {
            Log.errorf("Unknown hp from %s: %s", sources, health.toPrettyString());
        }
    }

    private void addMonsterAbilities(JsonNode jsonSource) {
        attributes.add(factory.createMonsterTypeStr(integerOrDefault(jsonSource, "str", 10)));
        attributes.add(factory.createMonsterTypeDex(integerOrDefault(jsonSource, "dex", 10)));
        attributes.add(factory.createMonsterTypeCon(integerOrDefault(jsonSource, "con", 10)));
        attributes.add(factory.createMonsterTypeInt(integerOrDefault(jsonSource, "int", 10)));
        attributes.add(factory.createMonsterTypeWis(integerOrDefault(jsonSource, "wis", 10)));
        attributes.add(factory.createMonsterTypeDex(integerOrDefault(jsonSource, "cha", 10)));
    }

    private void addMonsterAbilitySkillBonus(JsonNode jsonSource) {
        JsonNode savingThrows = jsonSource.get("save");
        if (savingThrows != null) {
            String list = jsonObjectToSkillBonusList(savingThrows);
            if (!list.isEmpty()) {
                attributes.add(factory.createMonsterTypeSave(list));
            }
        }

        JsonNode skills = jsonSource.get("skill");
        if (skills != null) {
            String list = jsonObjectToSkillBonusList(skills);
            if (!list.isEmpty()) {
                attributes.add(factory.createMonsterTypeSkill(list));
            }
        }

    }

    private void addMonsterConditionsSensesLanguages(JsonNode jsonSource) {
        if (jsonSource.has("conditionImmune")) {
            attributes.add(factory.createMonsterTypeConditionImmune(joinAndReplace(jsonSource.withArray("conditionImmune"))));
        }
        if (jsonSource.has("passive")) {
            attributes.add(factory.createMonsterTypePassive(integerOrDefault(jsonSource, "passive", 10)));
        }
        if (jsonSource.has("resist")) {
            attributes.add(factory.createMonsterTypeResist(joinAndReplace(jsonSource.withArray("resist"))));
        }
        if (jsonSource.has("immune") && !jsonSource.get("immune").isNull()) {
            List<String> immunities = new ArrayList<>();
            StringBuilder separator = new StringBuilder();
            jsonSource.withArray("immune").forEach(immunity -> {
                if (immunity.isTextual()) {
                    immunities.add(immunity.asText());
                } else {
                    StringBuilder str = new StringBuilder();
                    str.append(joinAndReplace(immunity.withArray("immune")));
                    if (immunity.has("note")) {
                        str.append(" ")
                                .append(immunity.get("note").asText());
                    }

                    if (separator.length() == 0) {
                        separator.append(";");
                    }
                    immunities.add(str.toString());
                }
            });
            if (separator.length() == 0) {
                separator.append(",");
            }
            attributes.add(factory.createMonsterTypeResist(String.join(separator.toString(), immunities)));
        }
        if (jsonSource.has("languages") && !jsonSource.get("languages").isNull()) {
            attributes.add(factory.createMonsterTypeLanguages(joinAndReplace(jsonSource.withArray("languages"))));
        }
        if (jsonSource.has("senses") && !jsonSource.get("senses").isNull()) {
            attributes.add(factory.createMonsterTypeSenses(joinAndReplace(jsonSource.withArray("senses"))));
        }
        if (jsonSource.has("vulnerable") && !jsonSource.get("vulnerable").isNull()) {
            attributes.add(factory.createMonsterTypeVulnerable(joinAndReplace(jsonSource.withArray("vulnerable"))));
        }
    }

    private void addMonsterCrProficiencyBonus(JsonNode jsonSource) {
        attributes.add(factory.createMonsterTypeCr(getTextOrEmpty(jsonSource, "cr")));
    }

    private void addMonsterSpellcasting(JsonNode jsonSource) {
        JsonNode node = jsonSource.get("spellcasting");
        if (node != null && node.isArray()) {
            Set<String> spells = new TreeSet<>();
            Set<String> diceRolls = new HashSet<>();

            node.forEach(spellcasting -> {
                List<String> text = new ArrayList<>();
                String traitName = getTextOrEmpty(spellcasting, "name");
                appendEntryToText(text, spellcasting.get("headerEntries"), diceRolls);

                if (spellcasting.has("will")) {
                    List<String> atWill = getSpells(spellcasting, "will");
                    text.add(LI + "At will: " + String.join(", ", atWill));
                    spells.addAll(atWill);
                }
                JsonNode daily = spellcasting.get("daily");
                if (daily != null) {
                    daily.fieldNames().forEachRemaining(field -> {
                        List<String> things = getSpells(daily, field);
                        spells.addAll(things);
                        switch (field) {
                            case "1":
                            case "2":
                            case "3":
                                text.add(String.format("%s%s/day: %s", LI,
                                        field.substring(0, 1),
                                        String.join(", ", things)));
                                break;
                            case "1e":
                            case "2e":
                            case "3e":
                                text.add(String.format("%s%s/day each: %s", LI,
                                        field.substring(0, 1),
                                        String.join(", ", things)));
                                break;
                            default:
                                Log.debugf("What is this: %s", spellcasting.toPrettyString());
                        }
                    });
                }
                JsonNode knownSpells = spellcasting.get("spells");
                if (knownSpells != null) {
                    String[] slots = new String[] { "0", "0", "0", "0", "0", "0", "0", "0", "0", "0" };
                    knownSpells.fields().forEachRemaining(spellLevelEntry -> {
                        JsonNode spellLevel = spellLevelEntry.getValue();
                        int level = Integer.parseInt(spellLevelEntry.getKey());
                        List<String> things = getSpells(spellLevel, "spells");
                        spells.addAll(things);

                        switch (level) {
                            case 0:
                                text.add(String.format("%sCantrips: %s", LI,
                                        String.join(", ", things)));
                                slots[0] = "" + things.size();
                                break;
                            case 1:
                                text.add(String.format("%s1st-level spells: %s", LI,
                                        String.join(", ", things)));

                                slots[1] = getTextOrDefault(spellLevel, "slots", "0");
                                break;
                            case 2:
                                text.add(String.format("%s2nd-level spells: %s", LI,
                                        String.join(", ", things)));
                                slots[2] = getTextOrDefault(spellLevel, "slots", "0");
                                break;
                            case 3:
                                text.add(String.format("%s3rd-level spells: %s", LI,
                                        String.join(", ", things)));
                                slots[3] = getTextOrDefault(spellLevel, "slots", "0");
                                break;
                            case 4:
                            case 5:
                            case 6:
                            case 7:
                            case 8:
                            case 9:
                                text.add(String.format("%s%sth-level spells: %s", LI,
                                        spellLevelEntry.getKey(),
                                        String.join(", ", things)));
                                slots[level] = getTextOrDefault(spellLevel, "slots", "0");
                                break;
                            default:
                                Log.debugf("What is this: %s", spellcasting.toPrettyString());
                        }
                    });

                    XmlSlotsType spellslots = factory.createSlotsType();
                    spellslots.setValue(String.join(", ", List.of(slots)));
                    attributes.add(factory.createMonsterTypeSlots(spellslots));
                }

                appendEntryToText(text, spellcasting.get("footerEntries"), diceRolls);

                XmlTraitType trait = createTraitType(traitName, text, diceRolls);
                if ("action".equals(getTextOrEmpty(spellcasting, "displayAs"))) {
                    attributes.add(factory.createMonsterTypeAction(trait));
                } else {
                    attributes.add(factory.createMonsterTypeTrait(trait));
                }
                attributes.add(factory.createMonsterTypeSpells(String.join(", ", spells).replace("*", "")));
            });
        }
    }

    private List<String> getSpells(JsonNode source, String fieldName) {
        List<String> spells = new ArrayList<>();
        JsonNode spellNode = null;
        if (source.isArray()) {
            spellNode = source;
        } else if (source.isObject() && source.has(fieldName)) {
            spellNode = source.get(fieldName);
            if (spellNode.isObject()) {
                spellNode = spellNode.get("spells");
            }
        }
        if (spellNode != null) {
            spellNode.forEach(s -> {
                String spell = replaceText(s.asText());
                spells.add(spell);
            });
        } else {
            Log.debugf("Asked for spells w/ not exist field: %s", fieldName);
        }
        return spells;
    }

    private void addMonsterTraits(JsonNode jsonSource) {
        collectTraits(jsonSource.get("trait")).forEach(t -> {
            attributes.add(factory.createMonsterTypeTrait(t));
        });
        collectTraits(jsonSource.get("action")).forEach(t -> {
            attributes.add(factory.createMonsterTypeAction(t));
        });
        collectTraits(jsonSource.get("reaction")).forEach(t -> {
            attributes.add(factory.createMonsterTypeReaction(t));
        });
        collectTraits(jsonSource.get("legendary")).forEach(t -> {
            attributes.add(factory.createMonsterTypeLegendary(t));
        });
    }

    private void addMonsterDescription(JsonNode jsonSource) {
        List<String> text = new ArrayList<>();
        try {
            getFluffDescription(jsonSource, IndexType.monsterfluff, text);
            maybeAddBlankLine(text);
        } catch (Exception e) {
            Log.errorf(e, "Unable to parse description for %s", sources);
        }
        text.add("Source: " + sources.getSourceText());
        attributes.add(factory.createMonsterTypeDescription(String.join("\n", text)));
    }

    private void addMonsterEnvironment(JsonNode jsonSource) {
        String value = joinAndReplace(jsonSource.withArray("environment"));
        if (!value.isEmpty()) {
            attributes.add(factory.createMonsterTypeEnvironment(value));
        }
    }

}
