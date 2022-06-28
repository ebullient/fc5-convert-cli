package dev.ebullient.fc5.json2xml;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.json2xml.jaxb.XmlObjectFactory;
import dev.ebullient.fc5.json2xml.jaxb.XmlSchoolEnum;
import dev.ebullient.fc5.json2xml.jaxb.XmlSpellType;
import dev.ebullient.fc5.json5e.CompendiumSources;
import dev.ebullient.fc5.json5e.JsonIndex;
import dev.ebullient.fc5.json5e.JsonSpell;

public class Json2XmlSpell extends Json2XmlBase implements JsonSpell {
    String name;
    XmlSpellType fc5Spell;
    List<JAXBElement<?>> attributes;

    public Json2XmlSpell(CompendiumSources sources, JsonIndex index, XmlObjectFactory factory) {
        super(sources, index, factory);
    }

    public XmlSpellType getXmlCompendiumObject() {
        return fc5Spell;
    }

    @Override
    public List<Json2XmlBase> convert(JsonNode jsonSource) {
        if (index.keyIsExcluded(sources.getKey())) {
            Log.debugf("Excluded %s", sources);
            return List.of(); // do not include
        }

        this.fc5Spell = factory.createSpellType();
        this.attributes = fc5Spell.getNameOrLevelOrSchool();
        this.name = getName();

        attributes.add(factory.createSpellTypeName(name));
        attributes.add(factory.createSpellTypeLevel(BigInteger.valueOf(spellLevel(jsonSource))));

        XmlSchoolEnum school = XmlSchoolEnum.fromValue(spellSchool(jsonSource));
        attributes.add(factory.createSpellTypeSchool(school));

        attributes.add(factory.createSpellTypeRitual(spellIsRitual(jsonSource) ? "YES" : "NO"));
        attributes.add(factory.createSpellTypeTime(spellCastingTime(jsonSource)));
        attributes.add(factory.createSpellTypeRange(spellRange(jsonSource)));
        attributes.add(factory.createSpellTypeComponents(spellComponentsString(jsonSource)));
        attributes.add(factory.createSpellTypeDuration(spellDuration(jsonSource)));
        attributes.add(factory.createSpellTypeClasses(spellClassesString(jsonSource)));
        addSpellTextAndRolls(name, jsonSource);

        return List.of(this);
    }

    void addSpellTextAndRolls(String name, JsonNode jsonSource) {
        Set<String> diceRolls = new HashSet<>();
        List<String> text = new ArrayList<>();
        collectTextAndRolls(jsonSource, text, diceRolls);

        text.forEach(t -> attributes.add(factory.createSpellTypeText(t)));
        diceRolls.forEach(r -> {
            if (r.startsWith("d")) {
                r = "1" + r;
            }
            attributes.add(factory.createSpellTypeRoll(r));
        });
    }
}
