package dev.ebullient.fc5.json2xml;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.json2xml.jaxb.XmlObjectFactory;
import dev.ebullient.fc5.json2xml.jaxb.XmlRaceType;
import dev.ebullient.fc5.json5e.CompendiumSources;
import dev.ebullient.fc5.json5e.JsonIndex;
import dev.ebullient.fc5.json5e.JsonIndex.IndexType;
import dev.ebullient.fc5.json5e.JsonRace;
import dev.ebullient.fc5.pojo.QuteTrait;

public class Json2XmlRace extends Json2XmlBase implements JsonRace {
    String raceName;
    XmlRaceType fc5Race;
    List<JAXBElement<?>> attributes;

    public Json2XmlRace(CompendiumSources sources, JsonIndex index, XmlObjectFactory factory) {
        super(sources, index, factory);
    }

    public XmlRaceType getXmlCompendiumObject() {
        return fc5Race;
    }

    List<Json2XmlBase> findVariants() {
        List<Json2XmlBase> variants = new ArrayList<>();
        index.subraces(sources).forEach(r -> {
            CompendiumSources subraceSources = index.constructSources(IndexType.subrace, r);
            if (index.sourceIncluded(subraceSources)) {
                Json2XmlRace subrace = new Json2XmlRace(subraceSources, index, factory);
                variants.addAll(subrace.convert(r));
            }
        });
        return variants;
    }

    @Override
    public List<Json2XmlBase> convert(JsonNode jsonSource) {
        if (index.keyIsExcluded(sources.getKey())) {
            Log.debugf("Excluded %s", sources);
            return List.of(); // do not include this one
        }
        List<Json2XmlBase> variants = new ArrayList<>();
        if (sources.getType() == IndexType.race) {
            variants.addAll(findVariants());
        }
        jsonSource = copyAndMergeRace(jsonSource);

        // this must be checked _after_ finding variants
        if (isReprinted(jsonSource)) {
            return variants; // the reprint will be used instead (stop parsing this one)
        }

        this.fc5Race = factory.createRaceType();
        this.attributes = fc5Race.getNameOrSizeOrSpeed();
        this.raceName = decoratedRaceName(jsonSource);

        attributes.add(factory.createRaceTypeName(this.raceName));
        attributes.add(factory.createRaceTypeSize(getSizeEnum(jsonSource)));
        attributes.add(factory.createRaceTypeSpeed(getXmlSpeed(jsonSource)));

        addRaceAbilities(jsonSource);
        addRaceSpellAbility(jsonSource);
        addRaceSkillProficiency(jsonSource);
        addRaceTraits(jsonSource);
        collectXmlModifierTypes(jsonSource).forEach(m -> attributes.add(factory.createRaceTypeModifier(m)));

        variants.add(0, this);
        return variants;
    }

    public void addRaceTraits(JsonNode value) {
        try {
            List<QuteTrait> traits = collectRacialTraits(value);
            traits.forEach(t -> attributes.add(factory.createRaceTypeTrait(quteToXmlTraitType(t))));
        } catch (Exception e) {
            Log.errorf(e, "Unable to collect traits for %s", sources);
        }
    }

    private void addRaceAbilities(JsonNode value) {
        String list = jsonArrayObjectToSkillBonusString(value, "ability");
        if (!list.isEmpty()) {
            attributes.add(factory.createRaceTypeAbility(list));
        }
    }

    private void addRaceSkillProficiency(JsonNode value) {
        JsonNode skills = value.withArray("skillProficiencies");
        String list = jsonToSkillString(skills);
        if (!list.isEmpty()) {
            attributes.add(factory.createRaceTypeProficiency(list));
        }
    }

    private void addRaceSpellAbility(JsonNode value) {
        String ability = getRacialSpellAbility(value);
        if (ability != null) {
            attributes.add(factory.createRaceTypeSpellAbility(ability));
        }
    }

    private BigInteger getXmlSpeed(JsonNode value) {
        int speed = getSpeed(value);
        return BigInteger.valueOf(speed);
    }
}
