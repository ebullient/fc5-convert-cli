package dev.ebullient.fc5.json2xml;

import java.math.BigInteger;
import java.util.List;

import javax.xml.bind.JAXBElement;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.Log;
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
    StartingClass scf;

    public Json2XmlClass(CompendiumSources sources, JsonIndex index, XmlObjectFactory factory) {
        super(sources, index, factory);
    }

    public XmlClassType getXmlCompendiumObject() {
        return fc5Class;
    }

    @Override
    public List<Json2XmlBase> convert(JsonNode classNode) {
        if (index.keyIsExcluded(sources.getKey())) {
            Log.debugf("Excluded %s", sources);
            return List.of(); // skip this
        }
        classNode = copyAndMergeClass(classNode);
        if (isReprinted(classNode)) {
            return List.of(); // the reprint will be used instead of this one.
        }

        this.fc5Class = factory.createClassType();
        this.attributes = fc5Class.getNameOrHdOrProficiency();

        this.scf = new StartingClass(index, sources, getName(), isMarkdown());
        attributes.add(factory.createClassTypeName(decoratedTypeName(getName(), sources)));

        if (isSidekick()) {
            addClassSpellAbility(classNode);
        } else {
            scf.findClassProficiencies(classNode);
            addClassHitDice(classNode);
            addClassSpellAbility(classNode);
            addClassWealth(classNode);
        }
        // Find all autolevels (includes resolving subclass features & sidekick profs)
        List<QuteClassAutoLevel> levels = classAutolevels(classNode);
        transferClassProficiencies(classNode); // after autolevels for sidekicks
        addClassAutoLevels(classNode, levels);
        return List.of(this);
    }

    private void addClassWealth(JsonNode classNode) {
        String wealth = scf.getStartingEquipment(classNode);
        if (wealth != null) {
            attributes.add(factory.createClassTypeWealth(wealth.replaceAll(" ", "")));
        }
    }

    private void addClassHitDice(JsonNode classNode) {
        int hd = scf.classHitDice(classNode);
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

    private void transferClassProficiencies(JsonNode value) {
        attributes.add(factory.createClassTypeArmor(scf.getArmor()));
        attributes.add(factory.createClassTypeTools(scf.getTools()));
        attributes.add(factory.createClassTypeWeapons(scf.getWeapons()));
        attributes.add(factory.createClassTypeNumSkills(scf.getNumSkills()));
        String profs = scf.getSkills();
        if (!profs.isEmpty()) {
            attributes.add(factory.createClassTypeProficiency(profs));
        }
    }

    void addClassAutoLevels(JsonNode classNode, List<QuteClassAutoLevel> levels) {
        // Create autolevel w/ creation options & profs.
        attributes.add(factory.createClassTypeAutolevel(getStartingLevel(classNode)));
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

    XmlAutolevelType getStartingLevel(JsonNode classNode) {
        XmlAutolevelType autoLevel = factory.createAutolevelType();
        autoLevel.setLevel(BigInteger.ONE);
        List<JAXBElement<?>> content = autoLevel.getContent();

        if (scf.hasSubclassSpellcasting()) {
            attributes.add(factory.createClassTypeSpellAbility(asAbilityEnum(scf.spellAbility())));
        }

        scf.buildStartingClassFeatures(classNode)
                .forEach(f -> content.add(factory.createAutolevelTypeFeature(quteToXmlFeatureType(f))));
        return autoLevel;
    }

    @Override
    public StartingClass getStartingClassAttributes() {
        return scf;
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
