package dev.ebullient.fc5.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.BeforeEach;

import dev.ebullient.fc5.Templates;

public class ParsingTestBase {
    final static Path output = Paths.get(System.getProperty("user.dir")).toAbsolutePath().resolve("target");

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db;

    @Inject
    Templates templates;

    @BeforeEach
    void getDocumentBuilder() throws Exception {
        db = dbf.newDocumentBuilder();
    }

    CompendiumType doParse(String input) throws Exception {
        return CompendiumType.readCompendium(db,
                new ByteArrayInputStream(input.getBytes()), null);
    }

    CompendiumType doParseInputResource(String resourceName) throws Exception {
        File file = new File("src/test/resources/" + resourceName);
        try (InputStream is = new FileInputStream(file)) {
            return CompendiumType.readCompendium(db, is, null);
        }
    }

    boolean textContains(Text textField, String content) {
        return String.join("", textField.content).contains(content);
    }

    boolean rollContains(List<Roll> rollList, String roll) {
        return rollList.stream().anyMatch(x -> x.textContent.equals(roll));
    }
}
