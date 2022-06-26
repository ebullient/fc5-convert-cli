package dev.ebullient.fc5.json2xml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.json2xml.jaxb.XmlMonsterType;
import dev.ebullient.fc5.json2xml.jaxb.XmlObjectFactory;
import dev.ebullient.fc5.json2xml.jaxb.XmlSlotsType;
import dev.ebullient.fc5.json2xml.jaxb.XmlTraitType;
import dev.ebullient.fc5.json5e.CompendiumSources;
import dev.ebullient.fc5.json5e.JsonIndex;
import dev.ebullient.fc5.json5e.JsonIndex.IndexType;
import dev.ebullient.fc5.json5e.JsonMonster;

public class Json2XmlMonster extends Json2XmlBase implements JsonMonster {

    XmlMonsterType fc5Monster;
    List<JAXBElement<?>> attributes;

    public Json2XmlMonster(CompendiumSources sources, JsonIndex index, XmlObjectFactory factory) {
        super(sources, index, factory);
    }

    public XmlMonsterType getXmlCompendiumObject() {
        return fc5Monster;
    }

    @Override
    public List<Json2XmlBase> convert(JsonNode jsonSource) {
        if (index.keyIsExcluded(sources.getKey())) {
            Log.debugf("Excluded %s", sources);
            return List.of(); // do not include
        }
        jsonSource = index.handleCopy(IndexType.monster, jsonSource);

        this.fc5Monster = factory.createMonsterType();
        this.attributes = fc5Monster.getNameOrSizeOrType();

        attributes.add(factory.createMonsterTypeName(decorateMonsterName(jsonSource)));
        attributes.add(factory.createMonsterTypeSize(getSizeEnum(jsonSource)));

        attributes.add(factory.createMonsterTypeType(monsterType(jsonSource)));
        attributes.add(factory.createMonsterTypeAlignment(monsterAlignment(jsonSource)));
        attributes.add(factory.createMonsterTypeAc(monsterAc(jsonSource)));
        attributes.add(factory.createMonsterTypeHp(monsterHp(jsonSource)));
        attributes.add(factory.createMonsterTypeSpeed(monsterSpeed(jsonSource)));
        attributes.add(factory.createMonsterTypeStr(bigIntegerOrDefault(jsonSource, "str", 10)));
        attributes.add(factory.createMonsterTypeDex(bigIntegerOrDefault(jsonSource, "dex", 10)));
        attributes.add(factory.createMonsterTypeCon(bigIntegerOrDefault(jsonSource, "con", 10)));
        attributes.add(factory.createMonsterTypeInt(bigIntegerOrDefault(jsonSource, "int", 10)));
        attributes.add(factory.createMonsterTypeWis(bigIntegerOrDefault(jsonSource, "wis", 10)));
        attributes.add(factory.createMonsterTypeDex(bigIntegerOrDefault(jsonSource, "cha", 10)));
        attributes.add(factory.createMonsterTypeCr(getTextOrEmpty(jsonSource, "cr")));
        attributes.add(factory.createMonsterTypeDescription(monsterDescription(jsonSource)));
        addMonsterAbilitySkillBonus(jsonSource);
        addMonsterConditionsSensesLanguages(jsonSource);
        addMonsterSpellcasting(jsonSource);
        addMonsterEnvironment(jsonSource);

        collectXmlTraits(jsonSource.get("trait")).forEach(t -> attributes.add(factory.createMonsterTypeTrait(t)));
        collectXmlTraits(jsonSource.get("action")).forEach(t -> attributes.add(factory.createMonsterTypeAction(t)));
        collectXmlTraits(jsonSource.get("reaction")).forEach(t -> attributes.add(factory.createMonsterTypeReaction(t)));
        collectXmlTraits(jsonSource.get("legendary")).forEach(t -> attributes.add(factory.createMonsterTypeLegendary(t)));

        return List.of(this); // do not include
    }

    private void addMonsterAbilitySkillBonus(JsonNode jsonSource) {
        JsonNode savingThrows = jsonSource.get("save");
        if (savingThrows != null) {
            String list = jsonObjectToSkillBonusString(savingThrows);
            if (!list.isEmpty()) {
                attributes.add(factory.createMonsterTypeSave(list));
            }
        }

        JsonNode skills = jsonSource.get("skill");
        if (skills != null) {
            String list = jsonObjectToSkillBonusString(skills);
            if (!list.isEmpty()) {
                attributes.add(factory.createMonsterTypeSkill(list));
            }
        }
    }

    private void addMonsterConditionsSensesLanguages(JsonNode jsonSource) {
        if (jsonSource.has("languages") && !jsonSource.get("languages").isNull()) {
            attributes.add(factory.createMonsterTypeLanguages(joinAndReplace(jsonSource.withArray("languages"))));
        }
        if (jsonSource.has("senses") && !jsonSource.get("senses").isNull()) {
            attributes.add(factory.createMonsterTypeSenses(joinAndReplace(jsonSource.withArray("senses"))));
        }
        if (jsonSource.has("passive")) {
            attributes.add(factory.createMonsterTypePassive(bigIntegerOrDefault(jsonSource, "passive", 10)));
        }
        if (jsonSource.has("resist")) {
            attributes.add(factory.createMonsterTypeResist(joinAndReplace(jsonSource.withArray("resist"))));
        }
        if (jsonSource.has("vulnerable") && !jsonSource.get("vulnerable").isNull()) {
            attributes.add(factory.createMonsterTypeVulnerable(joinAndReplace(jsonSource.withArray("vulnerable"))));
        }
        String immunities = monsterImmunities(jsonSource);
        if (immunities != null) {
            attributes.add(factory.createMonsterTypeImmune(immunities));
        }
        if (jsonSource.has("conditionImmune")) {
            attributes.add(factory.createMonsterTypeConditionImmune(joinAndReplace(jsonSource.withArray("conditionImmune"))));
        }
    }

    private void addMonsterSpellcasting(JsonNode jsonSource) {
        JsonNode node = jsonSource.get("spellcasting");
        if (node == null || node.isNull()) {
            return;
        } else if (node.isObject()) {
            throw new IllegalArgumentException("Unknown spellcasting: " + getSources());
        }
        JsonNode spellcasting = node.get(0);
        String traitName = getTextOrEmpty(spellcasting, "name");

        List<String> text = new ArrayList<>();
        Set<String> diceRolls = new HashSet<>();
        Set<String> spells = monsterSpellcasting(jsonSource, text, diceRolls,
                s -> {
                    XmlSlotsType spellslots = factory.createSlotsType();
                    spellslots.setValue(String.join(", ", List.of(s)));
                    attributes.add(factory.createMonsterTypeSlots(spellslots));
                });

        XmlTraitType trait = createXmlTraitType(traitName, text, diceRolls);
        if ("action".equals(getTextOrEmpty(spellcasting, "displayAs"))) {
            attributes.add(factory.createMonsterTypeAction(trait));
        } else {
            attributes.add(factory.createMonsterTypeTrait(trait));
        }
        attributes.add(factory.createMonsterTypeSpells(String.join(", ", spells).replace("*", "")));
    }

    private void addMonsterEnvironment(JsonNode jsonSource) {
        String value = joinAndReplace(jsonSource.withArray("environment"));
        if (!value.isEmpty()) {
            attributes.add(factory.createMonsterTypeEnvironment(value));
        }
    }
}
