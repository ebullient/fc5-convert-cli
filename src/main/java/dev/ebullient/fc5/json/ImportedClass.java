package dev.ebullient.fc5.json;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.xml.bind.JAXBElement;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.xml.XmlClassType;
import dev.ebullient.fc5.xml.XmlObjectFactory;

public class ImportedClass extends ImportedBase {

    final XmlClassType fc5Class;
    final List<JAXBElement<?>> attributes;

    ImportedClass(XmlObjectFactory factory, JsonNode jsonItem, String name) {
        super(factory, jsonItem, name);

        this.fc5Class = factory.createClassType();
        this.attributes = fc5Class.getNameOrHdOrProficiency();

        attributes.add(factory.createClassTypeName(name));
    }

    public void populateXmlAttributes(Predicate<String> sourceIncluded, Function<String, String> lookupName) {
        if (copyOf != null) {
            return;
        }
    }

}
