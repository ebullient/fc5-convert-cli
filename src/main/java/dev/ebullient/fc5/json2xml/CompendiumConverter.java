package dev.ebullient.fc5.json2xml;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.json5e.CompendiumSources;
import dev.ebullient.fc5.json5e.JsonIndex;
import dev.ebullient.fc5.json5e.JsonIndex.IndexType;
import dev.ebullient.fc5.json2xml.jaxb.XmlCompendiumType;
import dev.ebullient.fc5.json2xml.jaxb.XmlObjectFactory;

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
        StreamSupport.stream(index.elements().spliterator(), false)
                .filter(e -> e.getValue().has("name"))
                .forEach(e -> {
                    CompendiumBase base = null;
                    IndexType type = IndexType.fromKey(e.getKey());
                    CompendiumSources sources = new CompendiumSources(type, e.getKey(), e.getValue());
                    switch (type) {
                        case background:
                            base = new CompendiumBackground(sources, index, factory);
                            break;
                        case classtype:
                            base = new CompendiumClass(sources, index, factory);
                            break;
                        case feat:
                            base = new CompendiumFeat(sources, index, factory);
                            break;
                        case item:
                            base = new CompendiumItem(sources, index, factory);
                            break;
                        case monster:
                            base = new CompendiumMonster(sources, index, factory);
                            break;
                        case race:
                            base = new CompendiumRace(sources, index, factory);
                            break;
                        case spell:
                            base = new CompendiumSpell(sources, index, factory);
                            break;
                        default:
                            // Fluff or other utility type
                            return;
                    }

                    // parse/convert, and filter to allowed sources
                    try {
                        base.convert(e.getValue()).forEach(x -> convertedElements.put(x.sources.getKey(), x));
                    } catch (Exception ex) {
                        Log.errorf(ex, "Error converting %s: %s", e.getKey(), ex.toString());
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
                .filter(v -> type == null || type.matches(v.sources.getType()))
                .forEach(v -> compendiumElements.add(v.getXmlCompendiumObject()));
        compendiumElements.sort(null);
        return compendium;
    }
}
