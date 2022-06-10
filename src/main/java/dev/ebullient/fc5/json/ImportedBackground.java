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

    ImportedBackground(XmlObjectFactory factory, JsonNode jsonItem) {
        super(factory, jsonItem, getName(jsonItem));

        this.fc5Background = factory.createBackgroundType();
        this.attributes = fc5Background.getNameOrProficiencyOrTrait();
    }

    public void populateXmlAttributes(final Predicate<String> sourceIncluded,
            final Function<String, String> lookupName) {
        if (copyOf != null) {
            return;
        }
        attributes.add(factory.createBackgroundTypeName(name));
    }

}
