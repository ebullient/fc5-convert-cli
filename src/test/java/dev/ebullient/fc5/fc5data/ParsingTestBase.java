package dev.ebullient.fc5.fc5data;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.inject.Inject;

import dev.ebullient.fc5.Templates;

public class ParsingTestBase {
    static final Path output = Paths.get(System.getProperty("user.dir")).toAbsolutePath().resolve("target");

    protected Fc5XmlReader reader = new Fc5XmlReader();

    @Inject
    protected Templates templates;

    protected Fc5Compendium doParse(String input) throws Exception {
        return reader.parseXMLInputStream(new ByteArrayInputStream(input.getBytes()));
    }

    protected Fc5Compendium doParseInputResource(String resourceName) throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            return reader.parseXMLInputStream(is);
        }
    }

    protected boolean textContains(String text, String content) {
        return text.contains(content);
    }

    protected boolean textContains(Fc5Text textField, String content) {
        return String.join("", textField.content).contains(content);
    }

    protected boolean rollContains(List<Fc5Roll> rollList, String roll) {
        return rollList.stream().anyMatch(x -> x.textContent.equals(roll));
    }

    protected void assertContains(String content, String expected) {
        assertTrue(content.contains(expected), "content should contain " + expected + ". Found:\n" + content);
    }
}
