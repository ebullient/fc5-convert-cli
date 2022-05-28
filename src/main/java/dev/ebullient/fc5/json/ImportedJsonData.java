package dev.ebullient.fc5.json;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

    public ImportedJsonData(List<String> allowedSources) {
        this.allowedSources = allowedSources;
        this.factory = new XmlObjectFactory();
        this.importedItems = new HashMap<>();

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
    public void parseItemList(JsonNode jsonNode) {
        for (Iterator<JsonNode> i = jsonNode.elements(); i.hasNext();) {
            JsonNode element = i.next();
            boolean isSRD = element.has("srd");

            ImportedItem itemHolder = new ImportedItem(factory, element, getName(element));
            if (excludeItem(itemHolder.bookSources, isSRD)) {
                // skip this item: not from a specified source
                Log.debugf("Skipped %s from %s (%s)", getName(element), itemHolder.bookSources, isSRD);
                continue;
            }
            if (importedItems.get(itemHolder.name.toLowerCase()) != null) {
                Log.error("Duplicate entry for " + itemHolder.name);
                continue;
            }
            importedItems.put(itemHolder.name.toLowerCase(), itemHolder);
            compendiumElements.add(itemHolder.fc5Item);
        }
    }

    /** Read item or baseitem from 5etools.json, and create XML compatible object types */
    public void parseFeatList(JsonNode jsonNode) {
        for (Iterator<JsonNode> i = jsonNode.elements(); i.hasNext();) {
            JsonNode element = i.next();
            boolean isSRD = element.has("srd");

            ImportedFeat featHolder = new ImportedFeat(factory, element, getName(element));
            if (excludeItem(featHolder.bookSources, isSRD)) {
                // skip this item: not from a specified source
                Log.debugf("Skipped %s from %s (%s)", getName(element), featHolder.bookSources, isSRD);
                continue;
            }
            if (importedItems.get(featHolder.name.toLowerCase()) != null) {
                Log.error("Duplicate entry for " + featHolder.name);
                continue;
            }

            importedItems.put(featHolder.name.toLowerCase(), featHolder);
            compendiumElements.add(featHolder.fc5Feat);
        }
    }

    public boolean excludeItem(List<String> bookSources, boolean isSRD) {
        if (allowedSources.isEmpty()) {
            return !isSRD; // exclude non-SRD sources when no filter is specified.
        }
        return bookSources.stream().noneMatch(x -> allowedSources.contains(x));
    }

    public boolean excludeItem(JsonNode itemSource, boolean isSRD) {
        if (allowedSources.isEmpty()) {
            return !isSRD; // exclude non-SRD sources when no filter is specified.
        }
        if (itemSource == null || !itemSource.isTextual()) {
            return true; // unlikely, but skip items if we can't check their source
        }
        return !allowedSources.contains(itemSource.asText());
    }

    /**
     * From 5eTools, if the item is in the SRD, the SRD name is in the
     * "srd" field (rather than just true/false).
     *
     * @param element
     * @return String to use as the name
     */
    public String getName(JsonNode element) {
        JsonNode srd = element.get("srd");
        if (srd != null) {
            if (srd.isTextual()) {
                return srd.asText();
            }
        }
        return element.get("name").asText();
    }

    public void writeToXml(Path outputPath) throws JAXBException {
        // fill out the rest of XML values
        importedItems.forEach((k, v) -> v.populateXmlAttributes(
                bookSource -> allowedSources.contains(bookSource),
                lowerName -> {
                    ImportedBase base = importedItems.get(lowerName);
                    if (base == null) {
                        Log.debugf("Did not find element for %s", lowerName);
                    }
                    return base == null ? lowerName : base.name;
                }));

        JAXBContext jaxbContext = JAXBContext.newInstance(XmlCompendiumType.class, XmlObjectFactory.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        jaxbMarshaller.marshal(compendiumType, outputPath.toFile());
    }
}
