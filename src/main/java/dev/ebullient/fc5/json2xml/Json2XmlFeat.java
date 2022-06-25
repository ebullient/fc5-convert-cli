package dev.ebullient.fc5.json2xml;

import java.util.List;

import javax.xml.bind.JAXBElement;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.json2xml.jaxb.XmlFeatType;
import dev.ebullient.fc5.json2xml.jaxb.XmlObjectFactory;
import dev.ebullient.fc5.json5e.CompendiumSources;
import dev.ebullient.fc5.json5e.JsonFeat;
import dev.ebullient.fc5.json5e.JsonIndex;

public class Json2XmlFeat extends Json2XmlBase implements JsonFeat {
    XmlFeatType fc5Feat;
    List<JAXBElement<?>> attributes;

    public Json2XmlFeat(CompendiumSources sources, JsonIndex index, XmlObjectFactory factory) {
        super(sources, index, factory);
    }

    public XmlFeatType getXmlCompendiumObject() {
        return fc5Feat;
    }

    @Override
    public List<Json2XmlBase> convert(JsonNode value) {
        if (index.keyIsExcluded(sources.getKey())) {
            Log.debugf("Excluded %s", sources);
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
        collectXmlModifierTypes(value).stream().forEach(m -> {
            attributes.add(factory.createFeatTypeModifier(m));
        });
        return List.of(this);
    }

    public void addFeatText(JsonNode value) {
        List<String> text = featText(value);
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
