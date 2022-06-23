package dev.ebullient.fc5.json2xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.json5e.CompendiumSources;
import dev.ebullient.fc5.json5e.JsonIndex;
import dev.ebullient.fc5.json5e.JsonIndex.IndexType;
import dev.ebullient.fc5.json2xml.jaxb.XmlBackgroundType;
import dev.ebullient.fc5.json2xml.jaxb.XmlObjectFactory;
import dev.ebullient.fc5.json2xml.jaxb.XmlTraitType;

public class CompendiumBackground extends CompendiumBase {
    final static String NODE_TYPE = "background";

    XmlBackgroundType fc5Background;
    List<JAXBElement<?>> attributes;
    String backgroundName;

    public CompendiumBackground(CompendiumSources sources, JsonIndex index, XmlObjectFactory factory) {
        super(sources, index, factory);
    }

    public XmlBackgroundType getXmlCompendiumObject() {
        return fc5Background;
    }

    @Override
    public List<CompendiumBase> convert(JsonNode jsonSource) {
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
        String list = jsonToSkillList(skills);
        if (!list.isEmpty()) {
            attributes.add(factory.createBackgroundTypeProficiency(list));
        }
    }

    public void addBackgroundTraits(JsonNode jsonSource) {
        List<String> text = new ArrayList<>();
        try {
            getFluffDescription(jsonSource, IndexType.backgroundfluff, text);
            maybeAddBlankLine(text);
        } catch (Exception e) {
            Log.errorf(e, "Unable to parse traits for %s", sources);
        }
        text.add("Source: " + sources.getSourceText());
        XmlTraitType description = createTraitType("Description", text);
        attributes.add(factory.createBackgroundTypeTrait(description));

        List<XmlTraitType> traits = collectTraitsFromEntries(backgroundName, jsonSource);
        traits.forEach(t -> attributes.add(factory.createBackgroundTypeTrait(t)));
    }
}
