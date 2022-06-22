package dev.ebullient.fc5.json;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.xml.XmlFeatType;
import dev.ebullient.fc5.xml.XmlObjectFactory;

public class CompendiumFeat extends CompendiumBase {
    XmlFeatType fc5Feat;
    List<JAXBElement<?>> attributes;

    public CompendiumFeat(CompendiumSources sources, JsonIndex index, XmlObjectFactory factory) {
        super(sources, index, factory);
    }

    public XmlFeatType getXmlCompendiumObject() {
        return fc5Feat;
    }

    @Override
    public List<CompendiumBase> convert(JsonNode value) {
        if (index.keyIsExcluded(sources.key)) {
            Log.debugf("Excluded %s", sources.key);
            return List.of(); // do not include
        }

        this.fc5Feat = factory.createFeatType();
        this.attributes = fc5Feat.getNameOrPrerequisiteOrSpecial();

        String name = getName();
        attributes.add(factory.createFeatTypeName(name));
        addFeatPrerequisite(value);
        if (SPECIAL.contains(name)) {
            attributes.add(factory.createFeatureTypeSpecial(name));
        }
        addFeatText(value);
        addFeatProficiency(value);
        collectModifierTypes(value).stream().forEach(m -> {
            attributes.add(factory.createFeatTypeModifier(m));
        });
        return List.of(this);
    }

    public void addFeatText(JsonNode value) {
        List<String> text = new ArrayList<>();
        Set<String> diceRolls = new HashSet<>();

        String sourceText = sources.getSourceText();
        String altSource = sources.alternateSource();

        try {
            value.withArray("entries").forEach(entry -> appendEntryToText(text, entry, diceRolls));
            addAdditionalEntries(value, text, diceRolls, altSource);
            maybeAddBlankLine(text); // before Source
        } catch (Exception e) {
            Log.errorf(e, "Unable to parse text for %s", sources);
        }
        text.add("Source: " + sourceText);
        text.forEach(t -> attributes.add(factory.createFeatTypeText(t)));
    }

    private void addFeatProficiency(JsonNode value) {
        JsonNode skills = value.withArray("skillProficiencies");
        String list = jsonToSkillList(skills);
        if (list != null && !list.isEmpty()) {
            attributes.add(factory.createBackgroundTypeProficiency(list));
        }
    }

    private void addFeatPrerequisite(JsonNode value) {
        List<String> prereqs = listPrerequisites(value);
        attributes.add(factory.createFeatTypePrerequisite(String.join(", ", prereqs)));
    }
}
