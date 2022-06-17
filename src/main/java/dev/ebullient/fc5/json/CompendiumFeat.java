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
    String name;
    XmlFeatType fc5Feat;
    List<JAXBElement<?>> attributes;
    CompendiumSources sources;

    public CompendiumFeat(String key, JsonIndex index, XmlObjectFactory factory) {
        super(key, index, factory);
    }

    public XmlFeatType getXmlCompendiumObject() {
        return fc5Feat;
    }

    @Override
    public boolean convert(JsonNode value) {
        this.sources = new CompendiumSources(key, value);
        this.fc5Feat = factory.createFeatType();
        this.attributes = fc5Feat.getNameOrPrerequisiteOrSpecial();
        this.name = value.get("name").asText();

        if (index.excludeElement(key, value, sources)) {
            return false; // do not include
        }

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
        return true; // do not include
    }

    public void addFeatText(JsonNode value) {
        List<String> text = new ArrayList<>();
        Set<String> diceRolls = new HashSet<>();

        String sourceText = sources.getSourceText();
        String altSource = sources.alternateSource();

        try {
            value.withArray("entries").forEach(entry -> appendEntryToText(name, text, entry, diceRolls));
            value.withArray("additionalEntries").forEach(entry -> {
                if (entry.has("source") && !index.sourceIncluded(entry.get("source").asText())) {
                    return;
                } else if (!index.sourceIncluded(altSource)) {
                    return;
                }
                appendEntryToText(name, text, entry, diceRolls);
            });
            maybeAddBlankLine(text); // before Source
        } catch (Exception e) {
            Log.errorf(e, "Unable to parse text for %s", name);
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
