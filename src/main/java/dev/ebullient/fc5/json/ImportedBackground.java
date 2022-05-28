package dev.ebullient.fc5.json;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.xml.bind.JAXBElement;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.xml.XmlBackgroundType;
import dev.ebullient.fc5.xml.XmlObjectFactory;

public class ImportedBackground extends ImportedBase {

    final XmlBackgroundType fc5Background;
    final List<JAXBElement<?>> attributes;

    ImportedBackground(XmlObjectFactory factory, JsonNode jsonItem, String name) {
        super(factory, jsonItem, name);

        this.fc5Background = factory.createBackgroundType();
        this.attributes = fc5Background.getNameOrProficiencyOrTrait();

        attributes.add(factory.createBackgroundTypeName(name));
    }

    public void populateXmlAttributes(Predicate<String> sourceIncluded, Function<String, String> lookupName) {
        if (copyOf != null) {
            return;
        }
    }

}
