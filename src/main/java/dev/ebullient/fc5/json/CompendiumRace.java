package dev.ebullient.fc5.json;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.json.JsonIndex.IndexType;
import dev.ebullient.fc5.xml.XmlObjectFactory;
import dev.ebullient.fc5.xml.XmlRaceType;
import dev.ebullient.fc5.xml.XmlTraitType;

public class CompendiumRace extends CompendiumBase {
    String raceName;
    XmlRaceType fc5Race;
    List<JAXBElement<?>> attributes;

    public CompendiumRace(CompendiumSources sources, JsonIndex index, XmlObjectFactory factory) {
        super(sources, index, factory);
    }

    public XmlRaceType getXmlCompendiumObject() {
        return fc5Race;
    }

    List<CompendiumBase> findVariants() {
        List<CompendiumBase> variants = new ArrayList<>();
        index.subraces(getName(), sources).forEach(r -> {
            CompendiumSources subraceSources = index.constructSources(IndexType.subrace, r);
            CompendiumRace subrace = new CompendiumRace(subraceSources, index, factory);
            variants.addAll(subrace.convert(r));
        });
        return variants;
    }

    @Override
    public List<CompendiumBase> convert(JsonNode jsonSource) {
        if (index.excludeElement(jsonSource, sources)) {
            Log.debugf("Excluded %s", sources.key);
            return List.of(); // do not include this one
        }

        List<CompendiumBase> variants = new ArrayList<>();
        if (sources.type == IndexType.race) {
            variants.addAll(findVariants());
        }

        if (jsonSource.has("raceName") || jsonSource.has("_copy")) {
            jsonSource = index.cloneOrCopy(sources.key, jsonSource, IndexType.race,
                    getTextOrDefault(jsonSource, "raceName", null),
                    getTextOrDefault(jsonSource, "raceSource", null));
        }
        if (jsonSource.has("reprintedAs")) {
            for (Iterator<JsonNode> i = jsonSource.withArray("reprintedAs").elements(); i.hasNext();) {
                String ra = i.next().asText();
                if (index.sourceIncluded(ra.substring(ra.lastIndexOf("|") + 1))) {
                    Log.debugf("Skipping %s in favor of %s", sources.key, ra);
                    return variants; // the reprint will be used instead (stop parsing this one)
                }
            }
        }

        this.fc5Race = factory.createRaceType();
        this.attributes = fc5Race.getNameOrSizeOrSpeed();
        this.raceName = decoratedRaceName(jsonSource);

        attributes.add(factory.createRaceTypeName(this.raceName));
        attributes.add(factory.createRaceTypeSize(getSize(jsonSource)));
        attributes.add(factory.createRaceTypeSpeed(getSpeed(jsonSource)));

        addRaceAbilities(jsonSource);
        addRaceSpellAbility(jsonSource);
        addRaceSkillProficiency(jsonSource);
        addRaceTraits(jsonSource);
        collectModifierTypes(jsonSource).forEach(m -> attributes.add(factory.createRaceTypeModifier(m)));

        variants.add(0, this);
        return variants;
    }

    private String decoratedRaceName(JsonNode jsonSource) {
        String raceName = getName();
        JsonNode raceNameNode = jsonSource.get("raceName");
        if (raceNameNode != null) {
            raceName = String.format("%s (%s)", raceNameNode.asText(), raceName);
        }
        return decoratedTypeName(raceName.replace("Variant; ", ""), sources);
    }

    public void addRaceTraits(JsonNode value) {
        try {
            Set<String> diceRolls = new HashSet<>();
            List<XmlTraitType> traits = new ArrayList<>();

            List<String> text = new ArrayList<>();
            getFluffDescription(value, IndexType.racefluff, text);
            XmlTraitType description = createTraitType("Description", text);
            attributes.add(factory.createBackgroundTypeTrait(description));

            value.withArray("entries").forEach(entry -> {
                if (entry.isTextual()) {
                    addTraitTypeText(description, replaceText(entry.asText().replaceAll(":$", "."), diceRolls));
                } else if (entry.isObject()) {
                    if (entry.has("type") && "list".equals(entry.get("type").asText())) {
                        traits.addAll(collectTraits(entry.withArray("items")));
                    } else {
                        XmlTraitType trait = jsonToTraitType(entry);
                        traits.add(trait);
                    }
                }
            });

            addTraitTypeText(description, "");
            addTraitTypeText(description, "Source: " + sources.getSourceText());

            traits.forEach(t -> attributes.add(factory.createRaceTypeTrait(t)));
        } catch (Exception e) {
            Log.errorf(e, "Unable to collect traits for %s", sources);
        }
    }

    private void addRaceAbilities(JsonNode value) {
        JsonNode ability = value.withArray("ability");
        String list = jsonArrayObjectToSkillBonusList(ability);
        if (!list.isEmpty()) {
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
        JsonNode additionalSpells = value.get("additionalSpells");
        if (additionalSpells == null || additionalSpells.isNull()) {
            return;
        }
        JsonNode spells = additionalSpells.get(0);
        if (spells != null && spells.has("ability")) {
            JsonNode ability = spells.get("ability");
            if (ability.has("choose")) {
                // just pick the first (can be edited)
                attributes.add(factory.createRaceTypeSpellAbility(asAbilityEnum(ability.withArray("choose").get(0))));
            } else {
                attributes.add(factory.createRaceTypeSpellAbility(asAbilityEnum(ability)));
            }
        }
    }
}
