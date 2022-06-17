package dev.ebullient.fc5.json;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import dev.ebullient.fc5.Import5eTools;
import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.json.JsonIndex.IndexType;
import dev.ebullient.fc5.xml.XmlObjectFactory;
import dev.ebullient.fc5.xml.XmlRaceType;
import dev.ebullient.fc5.xml.XmlTraitType;

public class CompendiumRace extends CompendiumBase {
    final static String NODE_TYPE = "race";

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

    List<CompendiumBase> variants() {
        return List.of(this);
    }

    @Override
    public boolean convert(JsonNode value) {
        this.sources = new CompendiumSources(key, value);
        this.fc5Race = factory.createRaceType();
        this.attributes = fc5Race.getNameOrSizeOrSpeed();
        getName(value);

        if (value.has("raceName")) {
            JsonNode baseNode = index.getNode(IndexType.race,
                    value.get("raceName").asText(),
                    value.get("raceSource").asText());
            try {
                value = mergeRaceNode(baseNode, value);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Unable to resolve race " + name);
            }
        }
        if (index.excludeElement(key, value, sources)) {
            return false; // do not include
        }
        if (value.has("reprintedAs")) {
            String ra = value.get("reprintedAs").asText();
            if (index.sourceIncluded(ra.substring(ra.lastIndexOf("|") + 1))) {
                Log.debugf("Skipping %s in favor of %s", key, ra);
                return false; // the reprint will be used instead of this one.
            }
        }

        attributes.add(factory.createRaceTypeName(decoratedTypeName(name, sources)));
        attributes.add(factory.createRaceTypeSize(getSize(name, value)));
        attributes.add(factory.createRaceTypeSpeed(getSpeed(name, value)));

        addRaceAbilities(value);
        addRaceSpellAbility(value);
        addRaceSkillProficiency(value);
        addRaceTraits(value);
        collectModifierTypes(value).stream().forEach(m -> {
            attributes.add(factory.createRaceTypeModifier(m));
        });
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

    public void addRaceTraits(JsonNode value) {
        try {
            List<String> text = new ArrayList<>();
            getFluffDescription(name, value, IndexType.racefluff, text);
            text.add("");
            XmlTraitType description = createTraitType("Description", text);
            addTraitTypeText(description, "Source: " + sources.getSourceText());
            attributes.add(factory.createBackgroundTypeTrait(description));

            List<XmlTraitType> traits = collectTraits(name, value);
            traits.forEach(t -> attributes.add(factory.createRaceTypeTrait(t)));
        } catch (Exception e) {
            Log.errorf(e, "Unable to collect traits for %s", name);
        }
    }

    private void addRaceAbilities(JsonNode value) {
        JsonNode ability = value.withArray("ability");
        String list = jsonToSkillBonusList(ability);
        if (list != null && !list.isEmpty()) {
            attributes.add(factory.createRaceTypeAbility(list));
        }
    }

    private void addRaceSkillProficiency(JsonNode value) {
        JsonNode skills = value.withArray("skillProficiencies");
        String list = jsonToSkillList(skills);
        if (list != null && !list.isEmpty()) {
            attributes.add(factory.createRaceTypeProficiency(list));
        }
    }

    private void addRaceSpellAbility(JsonNode value) {
        JsonNode spells = value.withArray("additionalSpells").get(0);
        if (spells != null && spells.has("ability")) {
            JsonNode ability = spells.get("ability");
            if (ability.has("choose")) {
                // just pick the first
                attributes.add(factory.createRaceTypeSpellAbility(asAbilityEnum(ability.withArray("choose").get(0))));
            } else {
                attributes.add(factory.createRaceTypeSpellAbility(asAbilityEnum(ability)));
            }
        }
    }

    JsonNode mergeRaceNode(JsonNode baseNode, JsonNode value) throws JsonMappingException, JsonProcessingException {

        ObjectNode mergedNode = (ObjectNode) Import5eTools.MAPPER.readTree(baseNode.toString());
        mergedNode.put("merged", true);
        mergedNode.remove("srd");

        value.fieldNames().forEachRemaining(f -> {
            JsonNode sourceNode = value.get(f);
            switch (f) {
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
                    if (sourceNode.isArray()) {
                        ArrayNode list = (ArrayNode) mergedNode.withArray(f);
                        if (list.isEmpty()) {
                            list.add(value.get(f));
                        } else {
                            JsonNode source = value.withArray(f).get(0);
                            ObjectNode target = (ObjectNode) list.get(0);
                            source.fieldNames().forEachRemaining(i -> {
                                switch (i) {
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
                                                throw new IllegalStateException("Conflict with " + name
                                                        + " spellCasting: \n Source: " + sourceLevel.toPrettyString()
                                                        + "\n Target: " + targetLevel.toPrettyString());
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
                    if (list.isEmpty()) {
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
