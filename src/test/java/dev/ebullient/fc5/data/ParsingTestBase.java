package dev.ebullient.fc5.data;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.inject.Inject;

import dev.ebullient.fc5.Templates;

public class ParsingTestBase {
    final static Path output = Paths.get(System.getProperty("user.dir")).toAbsolutePath().resolve("target");

    protected CompendiumXmlReader reader = new CompendiumXmlReader();

    @Inject
    protected Templates templates;

    protected CompendiumType doParse(String input) throws Exception {
        return reader.parseXMLInputStream(new ByteArrayInputStream(input.getBytes()));
    }

    protected CompendiumType doParseInputResource(String resourceName) throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            return reader.parseXMLInputStream(is);
        }
    }

    protected boolean textContains(Text textField, String content) {
        return String.join("", textField.content).contains(content);
    }

    protected boolean rollContains(List<Roll> rollList, String roll) {
        return rollList.stream().anyMatch(x -> x.textContent.equals(roll));
    }

    protected void assertContains(String content, String expected) {
        assertTrue(content.contains(expected), "content should contain " + expected + ". Found:\n" + content);
    }
}
