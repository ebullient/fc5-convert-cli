package dev.ebullient.fc5.json;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.json.JsonIndex.IndexType;
import dev.ebullient.fc5.xml.XmlFeatureType;
import dev.ebullient.fc5.xml.XmlObjectFactory;

public class CompendiumClassFeature extends CompendiumBase {
    String name;
    XmlFeatureType fc5ClassFeature;
    List<JAXBElement<?>> attributes;
    CompendiumSources sources;

    final IndexType type;
    String subclassTitle; // only set for subclasses

    public CompendiumClassFeature(String key, JsonIndex index, XmlObjectFactory factory, IndexType type, String subclassName) {
        super(key, index, factory);
        this.type = type;
        this.subclassTitle = subclassName;
    }

    @Override
    public XmlFeatureType getXmlCompendiumObject() {
        return fc5ClassFeature;
    }

    @Override
    public boolean convert(JsonNode value) {
        this.sources = new CompendiumSources(key, value);
        this.fc5ClassFeature = factory.createFeatureType();
        this.attributes = fc5ClassFeature.getNameOrTextOrSpecial();
        this.name = value.get("name").asText();

        if (index.excludeElement(key, value, sources)) {
            return false; // do not include
        }
        if (value.has("isReprinted")) {
            Log.debugf("Skipping %s, has been reprinted", key);
            return false; // the reprint will be used instead of this one.
        }

        attributes.add(factory.createFeatureTypeName(decoratedFeatureTypeName(sources, subclassTitle, name, value)));
        if (type.isOptional() || sources.isFromUA()) {
            fc5ClassFeature.setOptional("YES");
        }

        addClassFeatureText(value);
        addClassFeatureSpecial(value);
        collectModifierTypes(value).stream().forEach(m -> {
            attributes.add(factory.createFeatureTypeModifier(m));
        });
        // proficiency
        return true;
    }

    public void addClassFeatureSpecial(JsonNode value) {
        if (name.startsWith("Unarmored Defense")) {
            String content = value.get("entries").toString();
            if (content.contains("Constitution modifier")) {
                attributes.add(factory.createFeatureTypeSpecial("Unarmored Defense: Constitution"));
            } else if (content.contains("Wisdom modifier")) {
                attributes.add(factory.createFeatureTypeSpecial("Unarmored Defense: Wisdom"));
            } else if (content.contains("Charisma modifier")) {
                attributes.add(factory.createFeatureTypeSpecial("Unarmored Defense: Charisma"));
            } else {
                Log.errorf("Unhandled Unarmored Defense for %s: %s", name, content);
            }
        } else if (SPECIAL.contains(name)) {
            attributes.add(factory.createFeatureTypeSpecial(name));
        }
    }

    public void addClassFeatureText(JsonNode value) {
        List<String> text = new ArrayList<>();
        try {
            value.withArray("entries").forEach(entry -> appendEntryToText(name, text, entry));
            maybeAddBlankLine(text);
        } catch (Exception e) {
            Log.errorf(e, "Unable to parse text for %s", name);
        }
        text.add("Source: " + sources.getSourceText());
        text.forEach(t -> attributes.add(factory.createFeatureTypeText(t)));
    }
}
