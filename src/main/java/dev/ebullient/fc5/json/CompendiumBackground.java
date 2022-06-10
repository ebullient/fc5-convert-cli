package dev.ebullient.fc5.json;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.xml.XmlBackgroundType;
import dev.ebullient.fc5.xml.XmlObjectFactory;

public class CompendiumBackground extends CompendiumBase {

    public CompendiumBackground(String key, JsonIndex index, XmlObjectFactory factory) {
        super(key, index, factory);
    }

    public XmlBackgroundType getXmlCompendiumObject() {
        return null;
    }

    @Override
    public boolean convert(JsonNode value) {
        // TODO Auto-generated method stub

        return false; // do not include
    }

}
