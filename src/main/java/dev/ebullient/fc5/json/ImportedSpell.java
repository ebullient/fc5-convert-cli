package dev.ebullient.fc5.json;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.xml.bind.JAXBElement;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.xml.XmlObjectFactory;
import dev.ebullient.fc5.xml.XmlSpellType;

public class ImportedSpell extends ImportedBase {

    final XmlSpellType fc5Spell;
    final List<JAXBElement<?>> attributes;

    ImportedSpell(XmlObjectFactory factory, JsonNode jsonItem) {
        super(factory, jsonItem, getName(jsonItem));

        this.fc5Spell = factory.createSpellType();
        this.attributes = fc5Spell.getNameOrLevelOrSchool();

        attributes.add(factory.createSpellTypeName(name));
    }

    public void populateXmlAttributes(final Predicate<String> sourceIncluded,
            final Function<String, String> lookupName) {
        if (copyOf != null) {
            return;
        }
    }

}
