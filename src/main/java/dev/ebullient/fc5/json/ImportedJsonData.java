package dev.ebullient.fc5.json;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.xml.XmlCompendiumType;
import dev.ebullient.fc5.xml.XmlObjectFactory;

public class ImportedJsonData {

    final List<String> allowedSources;
    final XmlObjectFactory factory;
    final XmlCompendiumType compendiumType;
    final Map<String, ImportedBase> importedItems;
    final List<Object> compendiumElements;
    final Map<String, Set<String>> reprints;
    final JsonIndex index;

    public ImportedJsonData(List<String> allowedSources, JsonIndex index) {
        this.index = index;
        this.allowedSources = allowedSources;

        this.factory = new XmlObjectFactory();
        this.importedItems = new HashMap<>();
        this.reprints = new HashMap<>();

        this.compendiumType = this.factory.createCompendiumType();
        this.compendiumType.setVersion(Byte.valueOf("5"));
        this.compendiumElements = this.compendiumType.getElements();
    }

    public boolean isEmpty() {
        return importedItems.isEmpty();
    }

    public int size() {
        return importedItems.size();
    }

    /** Read item or baseitem from 5etools.json, and create XML compatible object types */
    public void parseItem(JsonNode element) {
        ImportedItem itemHolder = new ImportedItem(index, factory, element);
        importedItems.put(itemHolder.name.toLowerCase(), itemHolder);
}

    /** Read item or baseitem from 5etools.json, and create XML compatible object types */
    public void parseFeat(JsonNode element) {
        ImportedFeat featHolder = new ImportedFeat(index, factory, element);
        importedItems.put(featHolder.name.toLowerCase(), featHolder);
    }

    public void parseRace(JsonNode element) {
        ImportedRace raceHolder = new ImportedRace(index, factory, element);

        Set<String> moreSources = reprints.get(raceHolder.key);
        if (element.has("reprintedAs")) {
            reprints.put(raceHolder.key, raceHolder.bookSources);
        } else if (moreSources.size() > 0) {
            raceHolder.bookSources.addAll(moreSources);
        }
        importedItems.put(raceHolder.name.toLowerCase(), raceHolder);
    }

    public void writeToXml(Path outputPath) throws JAXBException {
        Log.debugf("%s items known", importedItems.size());

        Log.debugf("%s items in compendium", compendiumElements.size());

        JAXBContext jaxbContext = JAXBContext.newInstance(XmlCompendiumType.class, XmlObjectFactory.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        jaxbMarshaller.marshal(compendiumType, outputPath.toFile());
    }
}
