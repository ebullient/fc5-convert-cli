package dev.ebullient.fc5.json;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.Import5eTools;

public class JsonIndexTest {
    final static Path PROJECT_PATH = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
    final static Path OUTPUT_PATH = PROJECT_PATH.resolve("target");

    protected JsonNode doParse(String resourceName) throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            return Import5eTools.MAPPER.readTree(is);
        }
    }

    @Test
    public void testJsonItem() throws Exception {
        List<String> source = List.of("PHB", "DMG", "XGE");
        JsonNode data = doParse("items.json");
        JsonIndex index = new JsonIndex(source).importTree(data);
        CompendiumConverter converter = new CompendiumConverter(index).parseElements();

        converter.writeToXml(OUTPUT_PATH.resolve("items.xml"));
    }

    @Test
    public void testSrdItems() throws Exception {
        List<String> source = List.of("PHB", "DMG", "XGE");
        JsonNode data = doParse("5etools_srd_items.json");
        JsonIndex index = new JsonIndex(source).importTree(data);
        CompendiumConverter converter = new CompendiumConverter(index).parseElements();

        converter.writeToXml(OUTPUT_PATH.resolve("5etools_srd_items.xml"));
    }
}
