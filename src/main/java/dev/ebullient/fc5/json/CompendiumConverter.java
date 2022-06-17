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
import dev.ebullient.fc5.json.JsonIndex.IndexType;
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
            if (e.getKey().startsWith("background|")) {
                base = new CompendiumBackground(e.getKey(), index, factory);
            } else if (e.getKey().startsWith("classtype|")) {
                base = new CompendiumClass(e.getKey(), index, factory);
            } else if (e.getKey().startsWith("feat|")) {
                base = new CompendiumFeat(e.getKey(), index, factory);
            } else if (e.getKey().startsWith("item|")) {
                base = new CompendiumItem(e.getKey(), index, factory);
            } else if (e.getKey().startsWith("monster|")) {
                base = new CompendiumMonster(e.getKey(), index, factory);
            } else if (e.getKey().startsWith("race|")) {
                base = new CompendiumRace(e.getKey(), index, factory);
            } else if (e.getKey().startsWith("spell|")) {
                base = new CompendiumSpell(e.getKey(), index, factory);
            } else {
                return;
            }

            // parse/convert, and filter to allowed sources
            try {
                if (base.convert(e.getValue())) {
                    convertedElements.put(e.getKey(), base);
                }
            } catch (Exception ex) {
                Log.errorf(ex, "Error converting %s: ", e.getKey());
            }
        });
        Log.debugf("%s converted elements", convertedElements.size());
        return this;
    }

    public CompendiumConverter writeToXml(Path outputPath) throws JAXBException {
        XmlCompendiumType compendium = createCompendium(null);

        JAXBContext jaxbContext = JAXBContext.newInstance(XmlCompendiumType.class, XmlObjectFactory.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        jaxbMarshaller.marshal(compendium, outputPath.toFile());
        return this;
    }

    public CompendiumConverter writeTypeToXml(IndexType type, Path outputPath) throws JAXBException {
        XmlCompendiumType compendium = createCompendium(type);

        JAXBContext jaxbContext = JAXBContext.newInstance(XmlCompendiumType.class, XmlObjectFactory.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        jaxbMarshaller.marshal(compendium, outputPath.toFile());
        return this;
    }

    XmlCompendiumType createCompendium(IndexType type) {
        XmlCompendiumType compendium = this.factory.createCompendiumType();
        compendium.setVersion(Byte.valueOf("5"));
        //compendium.setAutoIndent("YES");

        List<Object> compendiumElements = compendium.getElements();
        convertedElements.values().stream()
                .filter(v -> type == null || v.key.startsWith(type.name()))
                .flatMap(v -> v.variants().stream())
                .forEach(v -> compendiumElements.add(v.getXmlCompendiumObject()));
        compendiumElements.sort(null);
        return compendium;
    }
}
