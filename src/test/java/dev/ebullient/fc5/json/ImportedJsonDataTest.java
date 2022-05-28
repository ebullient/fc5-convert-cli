package dev.ebullient.fc5.json;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.Import5eTools;
import dev.ebullient.fc5.xml.XmlCompendiumType;
import dev.ebullient.fc5.xml.XmlObjectFactory;

public class ImportedJsonDataTest {
    final static Path PROJECT_PATH = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
    final static Path OUTPUT_PATH = PROJECT_PATH.resolve("target");

    protected JsonNode doParse(String resourceName) throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            return Import5eTools.MAPPER.readTree(is);
        }
    }

    @Test
    public void testJsonItem() throws Exception {
        JsonNode data = doParse("items.json");
        ImportedJsonData importedData = new ImportedJsonData(List.of("PHB", "DMG", "XGE"));
        importedData.parseItemList(data.get("baseitem"));
        importedData.parseItemList(data.get("item"));
        assertThat(importedData.isEmpty()).isFalse();
        assertThat(importedData.size()).isGreaterThan(0);

        StringWriter sw = new StringWriter();

        JAXBContext jaxbContext = JAXBContext.newInstance(XmlCompendiumType.class, XmlObjectFactory.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        jaxbMarshaller.marshal(importedData.compendiumType, sw);

        Files.write(OUTPUT_PATH.resolve("items.xml"), sw.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void testSrdItems() throws Exception {
        JsonNode data = doParse("5etools_srd_items.json");
        ImportedJsonData importedData = new ImportedJsonData(List.of("PHB", "DMG", "XGE"));
        importedData.parseItemList(data.get("baseitem"));
        importedData.parseItemList(data.get("item"));
        assertThat(importedData.isEmpty()).isFalse();
        assertThat(importedData.size()).isGreaterThan(0);

        StringWriter sw = new StringWriter();

        JAXBContext jaxbContext = JAXBContext.newInstance(XmlCompendiumType.class, XmlObjectFactory.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        jaxbMarshaller.marshal(importedData.compendiumType, sw);

        Files.write(OUTPUT_PATH.resolve("5etools_srd_items.xml"), sw.toString().getBytes(StandardCharsets.UTF_8));
    }
}
