package dev.ebullient.fc5.json2xml;

import java.math.BigInteger;
import java.util.List;

import javax.xml.bind.JAXBElement;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.json2xml.jaxb.XmlAutolevelType;
import dev.ebullient.fc5.json2xml.jaxb.XmlClassType;
import dev.ebullient.fc5.json2xml.jaxb.XmlCounterType;
import dev.ebullient.fc5.json2xml.jaxb.XmlObjectFactory;
import dev.ebullient.fc5.json2xml.jaxb.XmlResetEnum;
import dev.ebullient.fc5.json2xml.jaxb.XmlSlotsType;
import dev.ebullient.fc5.json5e.CompendiumSources;
import dev.ebullient.fc5.json5e.JsonClass;
import dev.ebullient.fc5.json5e.JsonIndex;
import dev.ebullient.fc5.pojo.QuteClassAutoLevel;

public class Json2XmlClass extends Json2XmlBase implements JsonClass {

    XmlClassType fc5Class;
    List<JAXBElement<?>> attributes;
    StartingClass startingClassFeature;

    public Json2XmlClass(CompendiumSources sources, JsonIndex index, XmlObjectFactory factory) {
        super(sources, index, factory);
    }

    public XmlClassType getXmlCompendiumObject() {
        return fc5Class;
    }

    @Override
    public List<Json2XmlBase> convert(JsonNode classNode) {
        classNode = resolveClassNode(classNode);
        if (classNode == null) {
            return List.of();
        }

        this.fc5Class = factory.createClassType();
        this.attributes = fc5Class.getNameOrHdOrProficiency();
        this.startingClassFeature = new StartingClass(index, sources, getName());
        attributes.add(factory.createClassTypeName(decoratedTypeName(getName(), sources)));
        if (getName().toLowerCase().contains("sidekick")) {
            addClassSpellAbility(classNode);
        } else {
            addClassHitDice(classNode);
            addClassProficiencies(classNode);
            addClassSpellAbility(classNode);
            addClassWealth(classNode);
        }
        addClassAutoLevels(classNode);
        return List.of(this);
    }

    private void addClassWealth(JsonNode classNode) {
        String wealth = startingClassFeature.getStartingEquipment(classNode);
        if (wealth != null) {
            attributes.add(factory.createClassTypeWealth(wealth.replaceAll(" ", "")));
        }
    }

    private void addClassHitDice(JsonNode classNode) {
        int hd = startingClassFeature.classHitDice(classNode);
        if (hd > 0) {
            attributes.add(factory.createClassTypeHd(BigInteger.valueOf(hd)));
        }
    }

    private void addClassSpellAbility(JsonNode classNode) {
        JsonNode ability = classNode.get("spellcastingAbility");
        if (ability != null) {
            attributes.add(factory.createClassTypeSpellAbility(asAbilityEnum(ability)));
            if (classNode.toString().contains("all expended spell slots when you finish a short")) {
                attributes.add(factory.createClassTypeSlotsReset(XmlResetEnum.S));
            } else {
                attributes.add(factory.createClassTypeSlotsReset(XmlResetEnum.L));
            }
        }
    }

    private void addClassProficiencies(JsonNode value) {
        List<String> abilitySkills = startingClassFeature.classProficiencies(value);
        attributes.add(factory.createClassTypeArmor(startingClassFeature.getArmor()));
        attributes.add(factory.createClassTypeTools(startingClassFeature.getTools()));
        attributes.add(factory.createClassTypeWeapons(startingClassFeature.getWeapons()));
        attributes.add(factory.createClassTypeNumSkills(startingClassFeature.getNumSkills()));
        if (!abilitySkills.isEmpty()) {
            attributes.add(factory.createClassTypeProficiency(String.join(", ", abilitySkills)));
        }
    }

    void addClassAutoLevels(JsonNode classNode) {
        // Find all autolevels (includes resolving subclass features & sidekick profs)
        List<QuteClassAutoLevel> levels = classAutolevels(classNode);
        // add the starting level first
        addStartingLevel(classNode);
        // Now add the other levels
        for (QuteClassAutoLevel level : levels) {
            if (!level.hasContent()) {
                continue;
            }
            XmlAutolevelType autolevel = factory.createAutolevelType();
            autolevel.setLevel(BigInteger.valueOf(level.getLevel()));
            if (level.isScoreImprovement()) {
                autolevel.setScoreImprovement("YES");
            }

            List<JAXBElement<?>> content = autolevel.getContent();
            level.getCounters().forEach(c -> content.add(factory.createAutolevelTypeCounter(quteToXmlCounterType(c))));
            level.getFeatures().forEach(f -> content.add(factory.createAutolevelTypeFeature(quteToXmlFeatureType(f))));
            if (level.getSlots() != null) {
                content.add(factory.createAutolevelTypeSlots(quteToXmlSlots(level.getSlots())));
            }
            attributes.add(factory.createClassTypeAutolevel(autolevel));
        }
    }

    void addStartingLevel(JsonNode classNode) {
        XmlAutolevelType autoLevel = factory.createAutolevelType();
        autoLevel.setLevel(BigInteger.ONE);
        List<JAXBElement<?>> content = autoLevel.getContent();

        if (isSidekick()) { // this has to be done late -- read from class feature
            attributes.add(factory.createClassTypeArmor(startingClassFeature.getArmor()));
            attributes.add(factory.createClassTypeWeapons(startingClassFeature.getWeapons()));
            attributes.add(factory.createClassTypeTools(startingClassFeature.getTools()));
            attributes.add(factory.createClassTypeNumSkills(startingClassFeature.getNumSkills()));
            attributes.add(factory.createClassTypeProficiency(startingClassFeature.getSkills()));
        }

        if (startingClassFeature.hasSubclassSpellcasting()) {
            attributes.add(factory.createClassTypeSpellAbility(asAbilityEnum(startingClassFeature.spellAbility())));
        }

        startingClassFeature.getStartingFeatures(classNode)
                .forEach(f -> content.add(factory.createAutolevelTypeFeature(quteToXmlFeatureType(f))));
    }

    @Override
    public StartingClass getStartingClassAttributes() {
        return startingClassFeature;
    }

    public XmlCounterType quteToXmlCounterType(QuteClassAutoLevel.Counter c) {
        XmlCounterType xmlCounter = factory.createCounterType();
        c.name.ifPresent(s -> xmlCounter.getNameOrValueOrReset().add(s));
        c.count.ifPresent(integer -> xmlCounter.getNameOrValueOrReset().add(BigInteger.valueOf(integer)));
        c.reset.ifPresent(reset -> xmlCounter.getNameOrValueOrReset().add(XmlResetEnum.fromValue(reset.toString())));
        return xmlCounter;
    }

    private XmlSlotsType quteToXmlSlots(QuteClassAutoLevel.SpellSlots slots) {
        XmlSlotsType xmlSlots = factory.createSlotsType();
        xmlSlots.setValue(slots.getValue());
        if (slots.isOptional()) { // TODO: Arcane Trickster
            xmlSlots.setOptional("YES");
        }
        return xmlSlots;
    }

}
