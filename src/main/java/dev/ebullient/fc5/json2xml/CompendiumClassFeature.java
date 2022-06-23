package dev.ebullient.fc5.json2xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.json5e.CompendiumSources;
import dev.ebullient.fc5.json5e.JsonIndex;
import dev.ebullient.fc5.json5e.JsonIndex.IndexType;
import dev.ebullient.fc5.json2xml.jaxb.XmlFeatureType;
import dev.ebullient.fc5.json2xml.jaxb.XmlObjectFactory;

public class CompendiumClassFeature extends CompendiumBase {

    XmlFeatureType fc5ClassFeature;
    List<JAXBElement<?>> attributes;

    final IndexType type;
    String subclassTitle; // only set for subclasses

    public CompendiumClassFeature(CompendiumSources sources, JsonIndex index, XmlObjectFactory factory, String subclassName) {
        super(sources, index, factory);
        this.type = sources.getType();
        this.subclassTitle = subclassName;
    }

    @Override
    public XmlFeatureType getXmlCompendiumObject() {
        return fc5ClassFeature;
    }

    @Override
    public List<CompendiumBase> convert(JsonNode value) {
        if (index.keyIsExcluded(sources.getKey())) {
            Log.debugf("Excluded %s", sources);
            return List.of(); // do not include
        }

        this.fc5ClassFeature = factory.createFeatureType();
        this.attributes = fc5ClassFeature.getNameOrTextOrSpecial();

        attributes.add(factory.createFeatureTypeName(decoratedFeatureTypeName(sources, subclassTitle, value)));
        if (type.isOptional() || sources.isFromUA()) {
            fc5ClassFeature.setOptional("YES");
        }

        addClassFeatureText(value);
        addClassFeatureSpecial(value);
        collectModifierTypes(value).stream().forEach(m -> {
            attributes.add(factory.createFeatureTypeModifier(m));
        });
        // proficiency
        return List.of(this);
    }

    public void addClassFeatureSpecial(JsonNode value) {
        if (sources.getName().startsWith("Unarmored Defense")) {
            String content = value.get("entries").toString();
            if (content.contains("Constitution modifier")) {
                attributes.add(factory.createFeatureTypeSpecial("Unarmored Defense: Constitution"));
            } else if (content.contains("Wisdom modifier")) {
                attributes.add(factory.createFeatureTypeSpecial("Unarmored Defense: Wisdom"));
            } else if (content.contains("Charisma modifier")) {
                attributes.add(factory.createFeatureTypeSpecial("Unarmored Defense: Charisma"));
            } else {
                Log.errorf("Unhandled Unarmored Defense for %s: %s", sources, content);
            }
        } else if (SPECIAL.contains(sources.getName())) {
            attributes.add(factory.createFeatureTypeSpecial(sources.getName()));
        }
    }

    public void addClassFeatureText(JsonNode value) {
        List<String> text = new ArrayList<>();
        try {
            value.withArray("entries").forEach(entry -> appendEntryToText(text, entry));
            maybeAddBlankLine(text);
        } catch (Exception e) {
            Log.errorf(e, "Unable to parse text for %s", sources);
        }
        text.add("Source: " + sources.getSourceText());
        text.forEach(t -> attributes.add(factory.createFeatureTypeText(t)));
    }
}
