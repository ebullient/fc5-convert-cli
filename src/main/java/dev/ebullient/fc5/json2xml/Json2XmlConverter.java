package dev.ebullient.fc5.json2xml;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.StreamSupport;

import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.json2xml.jaxb.XmlCompendiumType;
import dev.ebullient.fc5.json2xml.jaxb.XmlObjectFactory;
import dev.ebullient.fc5.json5e.CompendiumSources;
import dev.ebullient.fc5.json5e.JsonIndex;
import dev.ebullient.fc5.json5e.JsonIndex.IndexType;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

public class Json2XmlConverter {

    final JsonIndex index;
    final XmlObjectFactory factory;

    final Map<String, Json2XmlBase> convertedElements = new HashMap<>();
    final Map<String, Set<String>> reprints = new HashMap<>(); // TODO

    public Json2XmlConverter(JsonIndex index) {
        this.index = index;
        this.factory = new XmlObjectFactory();
    }

    public Json2XmlConverter parseElements() {
        StreamSupport.stream(index.elements().spliterator(), false)
                .filter(e -> e.getValue().has("name"))
                .forEach(e -> {
                    IndexType type = IndexType.fromKey(e.getKey());
                    CompendiumSources sources = new CompendiumSources(type, e.getKey(), e.getValue());
                    final Json2XmlBase base;
                    switch (Objects.requireNonNull(type)) {
                        case background:
                            base = new Json2XmlBackground(sources, index, factory);
                            break;
                        case classtype:
                            base = new Json2XmlClass(sources, index, factory);
                            break;
                        case feat:
                            base = new Json2XmlFeat(sources, index, factory);
                            break;
                        case item:
                        case itemvariant:
                            base = new Json2XmlItem(sources, index, factory);
                            break;
                        case monster:
                            base = new Json2XmlMonster(sources, index, factory);
                            break;
                        case race:
                            base = new Json2XmlRace(sources, index, factory);
                            break;
                        case spell:
                            base = new Json2XmlSpell(sources, index, factory);
                            break;
                        default:
                            // Fluff or other utility type
                            return;
                    }

                    // parse/convert, and filter to allowed sources
                    try {
                        base.convert(e.getValue()).stream()
                                .filter(Objects::nonNull)
                                .forEach(x -> convertedElements.put(x.sources.getKey(), x));
                    } catch (Exception ex) {
                        Log.errorf(ex, "Error converting %s: %s", e.getKey(), ex.toString());
                    }
                });
        Log.debugf("%s converted elements", convertedElements.size());
        return this;
    }

    public Json2XmlConverter writeToXml(Path outputPath) throws JAXBException {
        XmlCompendiumType compendium = createCompendium(null);

        JAXBContext jaxbContext = JAXBContext.newInstance(XmlCompendiumType.class, XmlObjectFactory.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        jaxbMarshaller.marshal(compendium, outputPath.toFile());
        return this;
    }

    public Json2XmlConverter writeTypeToXml(IndexType type, Path outputPath) throws JAXBException {
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
