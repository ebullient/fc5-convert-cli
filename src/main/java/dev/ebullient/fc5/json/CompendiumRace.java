package dev.ebullient.fc5.json;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import dev.ebullient.fc5.Import5eTools;
import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.xml.XmlObjectFactory;
import dev.ebullient.fc5.xml.XmlRaceType;
import dev.ebullient.fc5.xml.XmlSizeEnum;
import dev.ebullient.fc5.xml.XmlTraitType;

public class CompendiumRace extends CompendiumBase {

    String name;
    XmlRaceType fc5Race;
    List<JAXBElement<?>> attributes;
    CompendiumSources sources;

    public CompendiumRace(String key, JsonIndex index, XmlObjectFactory factory) {
        super(key, index, factory);
    }

    public XmlRaceType getXmlCompendiumObject() {
        return fc5Race;
    }

    @Override
    public boolean convert(JsonNode value) {
        this.sources = new CompendiumSources(key, value);
        this.fc5Race = factory.createRaceType();
        this.attributes = fc5Race.getNameOrSizeOrSpeed();
        getName(value);

        if (value.has("raceName")) {
            String baseNodeKey = String.format("race|%s|%s",
                value.get("raceName").asText(), value.get("raceSource").asText());
            JsonNode baseNode = index.nodeIndex.get(baseNodeKey);
            try {
                value = mergeRaceNode(baseNode, value);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Unable to resolve race " + name);
            }
        }

        if (index.excludeElement(sources.bookSources, value.has("srd"))) {
            return false; // do not include
        }
        if (value.has("reprintedAs")) {
            String ra = value.get("reprintedAs").asText();
            if (index.sourceIncluded(ra.substring(ra.lastIndexOf("|")+1))) {
                Log.debugf("Skipping %s in favor of %s", key, ra);
                return false; // the reprint will be used instead of this one.
            }
        }

        attributes.add(factory.createRaceTypeName(name));
        attributes.add(factory.createRaceTypeSize(getSize(value)));
        attributes.add(factory.createRaceTypeSpeed(getSpeed(value)));

        addRaceAbilities(value);
        addRaceSpellAbility(value);
        addRaceSkillProficiency(value);
        addRaceTraits(value);
        return true;
    }

	private String getName(JsonNode value) {
        JsonNode raceNameNode = value.get("raceName");
        String name = value.get("name").asText();

        if (raceNameNode != null) {
            return this.name = String.format("%s (%s)", raceNameNode.asText(), name);
        }
        return this.name = name;
    }

    private static XmlSizeEnum getSize(JsonNode value) {
        JsonNode size = value.get("size");
        if (size == null) {
            return XmlSizeEnum.M;
        } else if (size.isTextual()) {
            return XmlSizeEnum.fromValue(size.asText());
        }
        return XmlSizeEnum.fromValue(size.get(0).asText());
    }

    private static BigInteger getSpeed(JsonNode value) {
        JsonNode speed = value.get("speed");
        if (speed == null) {
            return BigInteger.valueOf(30);
        } else if (speed.isTextual()) {
            return BigInteger.valueOf(Long.valueOf(speed.asText()));
        }
        return BigInteger.valueOf(Long.valueOf(speed.get("walk").asText()));
    }

    public void addRaceTraits(JsonNode value) {
        StringBuilder text = new StringBuilder();
        Set<String> diceRolls = new HashSet<>();

        value.withArray("entries").forEach(entry -> {
            if (entry.isTextual()) {
                text.append(replaceText(entry.asText(), diceRolls)).append("\n");
            } else if (entry.isObject()) {
                XmlTraitType trait = factory.createTraitType();
                List<JAXBElement<String>> traitAttributes = trait.getNameOrTextOrAttack();
                traitAttributes.add(factory.createTraitTypeName(entry.get("name").asText()));

                StringBuilder traitText = new StringBuilder();
                appendEntryToText(traitText, entry.get("entries"), diceRolls);
                traitAttributes.add(factory.createTraitTypeText(traitText.toString()));

                attributes.add(factory.createRaceTypeTrait(trait));
            }
        });

        if (text.length() > 0) {
            XmlTraitType baseTrait = factory.createTraitType();
            List<JAXBElement<String>> baseTraitAttributes = baseTrait.getNameOrTextOrAttack();
            baseTraitAttributes.add(factory.createTraitTypeName(name + " Notes"));
            baseTraitAttributes.add(factory.createTraitTypeText(text.toString()));
            attributes.add(factory.createRaceTypeTrait(baseTrait));
        }
    }

    private void addRaceAbilities(JsonNode value) {
        JsonNode entry = value.withArray("ability");
        if (entry.size() == 1 && !entry.toString().contains("choose")) {
            List<String> list = new ArrayList<>();
            entry.fields().forEachRemaining(f -> {
                list.add(String.format("%s %s", f.getKey(), f.getValue().asText()));
            });
            attributes.add(factory.createRaceTypeAbility(String.join(", ", list)));
        }
    }

    private void addRaceSkillProficiency(JsonNode value) {
        JsonNode skills = value.withArray("skillProficiencies");
        if (skills.size() == 1 && !skills.toString().contains("choose")) {
            List<String> list = new ArrayList<>();
            skills.fieldNames().forEachRemaining(f -> list.add(f));
            attributes.add(factory.createRaceTypeProficiency(String.join(", ", list)));
        }
    }

    private void addRaceSpellAbility(JsonNode value) {
        JsonNode spells = value.withArray("additionalSpells");
        // if (skills.size() == 1 && !skills.toString().contains("choose")) {
        //     List<String> list = new ArrayList<>();
        //     skills.fieldNames().forEachRemaining(f -> list.add(f));
        //     attributes.add(factory.createRaceTypeProficiency(String.join(", ", list)));
        // }
    }

    JsonNode mergeRaceNode(JsonNode baseNode, JsonNode value) throws JsonMappingException, JsonProcessingException {

        ObjectNode mergedNode = (ObjectNode) Import5eTools.MAPPER.readTree(baseNode.toString());
        mergedNode.put("merged", true);

        value.fieldNames().forEachRemaining(f -> {
            JsonNode sourceNode = value.get(f);
            switch(f) {
                case "name":
                case "source":
                case "additionalSources":
                case "otherSources":
                case "raceName":
                case "raceSource":
                case "page":
                case "speed":
                case "srd":
                case "reprintedAs":
                    mergedNode.replace(f, sourceNode);
                    break;
                case "entries":
                    ArrayNode entries = (ArrayNode) mergedNode.withArray(f);
                    sourceNode.elements().forEachRemaining(e -> entries.add(e));
                    break;
                case "additionalSpells":
                    if ( sourceNode.isArray() ) {
                        ArrayNode list = (ArrayNode) mergedNode.withArray(f);
                        if ( list.isEmpty() ) {
                            list.add(value.get(f));
                        } else {
                            JsonNode source = value.withArray(f).get(0);
                            ObjectNode target = (ObjectNode) list.get(0);
                            source.fieldNames().forEachRemaining(i -> {
                                switch(i) {
                                    case "ability":
                                        target.replace(i, source.get(i));
                                        break;
                                    case "known":
                                    case "expanded":
                                    case "innate":
                                        source.with(i).fields().forEachRemaining(lvl -> {
                                            JsonNode sourceLevel = lvl.getValue();
                                            JsonNode targetLevel = target.with(i).get(lvl.getKey());
                                            if (targetLevel == null) {
                                                target.with(i).set(lvl.getKey(), sourceLevel);
                                            } else if (sourceLevel.isArray() && targetLevel.isArray()) {
                                                sourceLevel.elements().forEachRemaining(x -> ((ArrayNode) targetLevel).add(x));
                                            } else {
                                                throw new IllegalStateException("Conflict with "+name+" spellCasting: \n Source: " + sourceLevel.toPrettyString() + "\n Target: " + targetLevel.toPrettyString());
                                            }
                                        });
                                        break;
                                }
                            });
                        }
                    }
                    break;
                case "ability":
                case "weaponProficiencies":
                case "armorProficiencies":
                case "skillProficiencies":
                    ArrayNode list = (ArrayNode) mergedNode.withArray(f);
                    if ( list.isEmpty() ) {
                        list.add(value.get(f));
                    } else {
                        JsonNode source = value.withArray(f).get(0);
                        ObjectNode target = (ObjectNode) list.get(0);
                        source.fields().forEachRemaining(field -> target.replace(field.getKey(), field.getValue()));
                    }
                    break;
            }
        });

		return mergedNode;
	}
}
