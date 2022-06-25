package dev.ebullient.fc5.json2xml;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
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
        this.name = jsonSource.get("name").asText();

        attributes.add(factory.createSpellTypeName(name));
        addSpellLevel(jsonSource);
        addSpellSchool(jsonSource);
        addRitualTag(jsonSource);
        addSpellTime(jsonSource);
        addSpellRange(jsonSource);
        addSpellComponents(jsonSource);
        addSpellDuration(jsonSource);
        addSpellClasses(jsonSource);
        addSpellTextAndRolls(name, jsonSource);

        return List.of(this);
    }

    void addSpellLevel(JsonNode jsonSource) {
        int level = jsonSource.get("level").asInt();
        attributes.add(factory.createSpellTypeLevel(BigInteger.valueOf(level)));
    }

    void addSpellSchool(JsonNode jsonSource) {
        String s = jsonSource.get("school").asText();
        XmlSchoolEnum school = XmlSchoolEnum.fromValue(s);
        attributes.add(factory.createSpellTypeSchool(school));
    }

    void addRitualTag(JsonNode source) {
        attributes.add(factory.createSpellTypeRitual(isRitual(source) ? "YES" : "NO"));
    }

    private void addSpellClasses(JsonNode jsonSource) {
        JsonNode node = jsonSource.get("classes");
        if (node == null || node.isNull()) {
            return;
        }
        Collection<String> classes = spellClasses(node, jsonSource.get("school"));
        attributes.add(factory.createSpellTypeClasses(String.join(", ", classes)));
    }

    private void addSpellComponents(JsonNode jsonSource) {
        List<String> list = spellComponents(jsonSource);
        attributes.add(factory.createSpellTypeComponents(String.join(", ", list)));
    }

    private void addSpellDuration(JsonNode jsonSource) {
        //<duration>8 hours</duration>
        attributes.add(factory.createSpellTypeDuration(spellDuration(jsonSource)));
    }

    private void addSpellRange(JsonNode jsonSource) {
        // <range>30 feet</range>
        attributes.add(factory.createSpellTypeRange(spellRange(jsonSource)));
    }

    private void addSpellTime(JsonNode jsonSource) {
        // <time>1 action</time>
        jsonSource.withArray("time").forEach(time -> {
            attributes.add(factory.createSpellTypeTime(castingTime(time)));
        });
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
