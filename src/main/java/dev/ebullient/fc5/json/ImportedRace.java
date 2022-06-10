package dev.ebullient.fc5.json;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.xml.bind.JAXBElement;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.xml.XmlObjectFactory;
import dev.ebullient.fc5.xml.XmlRaceType;
import dev.ebullient.fc5.xml.XmlSizeEnum;
import dev.ebullient.fc5.xml.XmlTraitType;

public class ImportedRace extends ImportedBase {

    final XmlRaceType fc5Race;
    final String key;
    final List<JAXBElement<?>> attributes;

    ImportedRace(JsonIndex index, XmlObjectFactory factory, JsonNode jsonItem) {
        super(factory, jsonItem, getRaceName(jsonItem));

        this.key = String.format("%s|%s", jsonItem.get("name").asText(), jsonItem.get("source"));
        if (jsonItem.has("raceSource")) {
            this.bookSources.add(jsonItem.get("raceSource").asText());
        }

        this.fc5Race = factory.createRaceType();
        this.attributes = fc5Race.getNameOrSizeOrSpeed();
    }

    private static String getRaceName(JsonNode jsonItem) {
        JsonNode raceNameNode = jsonItem.get("raceName");
        String name = jsonItem.get("name").asText();

        if (raceNameNode != null) {
            return String.format("%s (%s)", raceNameNode.asText(), name);
        }
        return name;
    }

    public void populateXmlAttributes(final Predicate<String> sourceIncluded,
            final Function<String, String> lookupName) {

        attributes.add(factory.createRaceTypeName(name));
        attributes.add(factory.createRaceTypeSize(toSize(jsonElement.get("size"))));
        attributes.add(factory.createRaceTypeSpeed(toWalkingSpeed(jsonElement.get("speed"))));

        addRaceAbilities();
        addRaceSpellAbility();
        addRaceSkillProficiency();
        //addRaceModifier();
        addRaceTraits();
    }

    private XmlSizeEnum toSize(JsonNode jsonNode) {
        if (jsonNode == null) {
            return XmlSizeEnum.M;
        } else if (jsonNode.isTextual()) {
            return XmlSizeEnum.fromValue(jsonNode.asText());
        }
        return XmlSizeEnum.fromValue(jsonNode.get(0).asText());
    }

    private BigInteger toWalkingSpeed(JsonNode jsonNode) {
        if (jsonNode == null) {
            return BigInteger.valueOf(30);
        } else if (jsonNode.isTextual()) {
            return BigInteger.valueOf(Long.valueOf(jsonNode.asText()));
        }
        return BigInteger.valueOf(Long.valueOf(jsonNode.get("walk").asText()));
    }

    private void addRaceAbilities() {
        JsonNode entry = jsonElement.withArray("ability");
        if (entry.size() == 1 && !entry.toString().contains("choose")) {
            List<String> list = new ArrayList<>();
            entry.fields().forEachRemaining(f -> {
                list.add(String.format("%s %s", f.getKey(), f.getValue().asText()));
            });
            attributes.add(factory.createRaceTypeAbility(String.join(", ", list)));
        }
    }

    private void addRaceSkillProficiency() {
    }

    private void addRaceSpellAbility() {

    }

    public void addRaceTraits() {
        StringBuilder text = new StringBuilder();
        Set<String> diceRolls = new HashSet<>();

        jsonElement.withArray("entries").forEach(entry -> {
            if (entry.isTextual()) {
                text.append(replaceText(entry.asText(), diceRolls)).append("\n");
            } else if (entry.isObject()) {
                XmlTraitType trait = factory.createTraitType();
                List<JAXBElement<String>> traitAttributes = trait.getNameOrTextOrAttack();
                traitAttributes.add(factory.createTraitTypeName(entry.get("name").asText()));

                StringBuilder traitText = new StringBuilder();
                appendEntry(traitText, entry.get("entries"), diceRolls);
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
}
