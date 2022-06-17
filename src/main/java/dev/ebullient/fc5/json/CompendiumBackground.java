package dev.ebullient.fc5.json;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.json.JsonIndex.IndexType;
import dev.ebullient.fc5.xml.XmlBackgroundType;
import dev.ebullient.fc5.xml.XmlObjectFactory;
import dev.ebullient.fc5.xml.XmlTraitType;

public class CompendiumBackground extends CompendiumBase {
    final static String NODE_TYPE = "background";

    String name;
    XmlBackgroundType fc5Background;
    List<JAXBElement<?>> attributes;
    CompendiumSources sources;

    public CompendiumBackground(String key, JsonIndex index, XmlObjectFactory factory) {
        super(key, index, factory);
    }

    public XmlBackgroundType getXmlCompendiumObject() {
        return fc5Background;
    }

    @Override
    public boolean convert(JsonNode jsonSource) {
        this.sources = new CompendiumSources(key, jsonSource);
        this.fc5Background = factory.createBackgroundType();
        this.attributes = fc5Background.getNameOrProficiencyOrTrait();
        this.name = jsonSource.get("name").asText();

        if (index.excludeElement(key, jsonSource, sources)) {
            return false; // do not include
        }
        jsonSource = handleCopy(IndexType.background, jsonSource);

        attributes.add(factory.createBackgroundTypeName(name));

        addBackgroundSkillProficiency(jsonSource);
        addBackgroundTraits(jsonSource);
        return true; // do not include
    }

    private void addBackgroundSkillProficiency(JsonNode value) {
        JsonNode skills = value.withArray("skillProficiencies");
        String list = jsonToSkillList(skills);
        if (list != null && !list.isEmpty()) {
            attributes.add(factory.createBackgroundTypeProficiency(list));
        }
    }

    public void addBackgroundTraits(JsonNode value) {
        List<String> text = new ArrayList<>();
        try {
            getFluffDescription(name, value, IndexType.backgroundfluff, text);
            maybeAddBlankLine(text);
        } catch (Exception e) {
            Log.errorf(e, "Unable to parse traits for %s", name);
        }
        text.add("Source: " + sources.getSourceText());
        XmlTraitType description = createTraitType("Description", text);
        attributes.add(factory.createBackgroundTypeTrait(description));

        List<XmlTraitType> traits = collectTraits(name, value);
        traits.forEach(t -> attributes.add(factory.createBackgroundTypeTrait(t)));
    }
}
