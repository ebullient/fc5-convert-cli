package dev.ebullient.fc5.json;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.xml.bind.JAXBElement;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.xml.XmlObjectFactory;
import dev.ebullient.fc5.xml.XmlRaceType;

public class ImportedRace extends ImportedBase {

    final XmlRaceType fc5Race;
    final List<JAXBElement<?>> attributes;

    ImportedRace(XmlObjectFactory factory, JsonNode jsonItem, String name) {
        super(factory, jsonItem, name);

        this.fc5Race = factory.createRaceType();
        this.attributes = fc5Race.getNameOrSizeOrSpeed();

        attributes.add(factory.createRaceTypeName(name));
    }

    public void populateXmlAttributes(Predicate<String> sourceIncluded, Function<String, String> lookupName) {
        if (copyOf != null) {
            return;
        }
    }
}
