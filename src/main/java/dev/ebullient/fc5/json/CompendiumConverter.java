package dev.ebullient.fc5.json;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.xml.XmlCompendiumType;
import dev.ebullient.fc5.xml.XmlObjectFactory;

public class CompendiumConverter {

    final JsonIndex index;
    final XmlObjectFactory factory;

    final Map<String, CompendiumBase> convertedElements = new HashMap<>();
    final Map<String, Set<String>> reprints = new HashMap<>();

    public CompendiumConverter(JsonIndex index) {
        this.index = index;
        this.factory = new XmlObjectFactory();
    }

    public CompendiumConverter parseElements() {
        index.elements().forEach(e -> {
            CompendiumBase base = null;
            if (e.getKey().startsWith("background")) {
                base = new CompendiumBackground(e.getKey(), index, factory);
            } else if (e.getKey().startsWith("item")) {
                base = new CompendiumItem(e.getKey(), index, factory);
            } else if (e.getKey().startsWith("race")) {
                base = new CompendiumRace(e.getKey(), index, factory);
            } else {
                return;
            }

            // parse/convert, and filter to allowed sources
            if ( base.convert(e.getValue()) ) {
                convertedElements.put(e.getKey(), base);
            }
        });
        Log.debugf("%s converted elements", convertedElements.size());
        return this;
    }

    public void writeToXml(Path outputPath) throws JAXBException {
        XmlCompendiumType compendium = createCompendium();
        JAXBContext jaxbContext = JAXBContext.newInstance(XmlCompendiumType.class, XmlObjectFactory.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        jaxbMarshaller.marshal(compendium, outputPath.toFile());
    }

    XmlCompendiumType createCompendium() {
        XmlCompendiumType compendium = this.factory.createCompendiumType();
        compendium.setVersion(Byte.valueOf("5"));
        List<Object> compendiumElements = compendium.getElements();
        convertedElements.values().forEach(v -> compendiumElements.add(v.getXmlCompendiumObject()));
        return compendium;
    }
}
