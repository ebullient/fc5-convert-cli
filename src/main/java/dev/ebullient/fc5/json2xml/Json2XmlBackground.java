package dev.ebullient.fc5.json2xml;

import java.util.List;

import javax.xml.bind.JAXBElement;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.json2xml.jaxb.XmlBackgroundType;
import dev.ebullient.fc5.json2xml.jaxb.XmlObjectFactory;
import dev.ebullient.fc5.json2xml.jaxb.XmlTraitType;
import dev.ebullient.fc5.json5e.CompendiumSources;
import dev.ebullient.fc5.json5e.JsonBackground;
import dev.ebullient.fc5.json5e.JsonIndex;
import dev.ebullient.fc5.json5e.JsonIndex.IndexType;

public class Json2XmlBackground extends Json2XmlBase implements JsonBackground {
    XmlBackgroundType fc5Background;
    List<JAXBElement<?>> attributes;
    String backgroundName;

    public Json2XmlBackground(CompendiumSources sources, JsonIndex index, XmlObjectFactory factory) {
        super(sources, index, factory);
    }

    public XmlBackgroundType getXmlCompendiumObject() {
        return fc5Background;
    }

    @Override
    public List<Json2XmlBase> convert(JsonNode jsonSource) {
        if (index.keyIsExcluded(sources.getKey())) {
            Log.debugf("Excluded %s", sources);
            return List.of(); // do not include
        }
        jsonSource = index.handleCopy(IndexType.background, jsonSource);

        this.fc5Background = factory.createBackgroundType();
        this.attributes = fc5Background.getNameOrProficiencyOrTrait();

        this.backgroundName = decoratedTypeName(getName(), sources);
        attributes.add(factory.createBackgroundTypeName(backgroundName));

        addBackgroundSkillProficiency(jsonSource);
        addBackgroundTraits(jsonSource);
        return List.of(this); // do not include
    }

    private void addBackgroundSkillProficiency(JsonNode value) {
        JsonNode skills = value.withArray("skillProficiencies");
        String list = jsonToSkillString(skills);
        if (!list.isEmpty()) {
            attributes.add(factory.createBackgroundTypeProficiency(list));
        }
    }

    public void addBackgroundTraits(JsonNode jsonSource) {
        List<String> text = getDescription(jsonSource);

        XmlTraitType description = createXmlTraitType("Description", text);
        attributes.add(factory.createBackgroundTypeTrait(description));

        List<XmlTraitType> traits = collectXmlTraitsFromEntries(backgroundName, jsonSource);
        traits.forEach(t -> attributes.add(factory.createBackgroundTypeTrait(t)));
    }
}
