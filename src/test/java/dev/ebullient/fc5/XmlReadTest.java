package dev.ebullient.fc5;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import picocli.CommandLine;

@QuarkusTest
public class XmlReadTest {

    @Inject
    protected Templates templates;

    ConvertCli cli;
    Convert convert;
    Validate validate;
    Transform transform;

    @BeforeEach
    void setUpCLi() {
        cli = new ConvertCli();

        convert = new Convert(templates);
        convert.parent = cli;

        validate = new Validate();
        validate.parent = cli;

        transform = new Transform();
        transform.parent = cli;
    }

    @Test
    @Disabled
    void testMergedXml() throws Exception {
        File output = new File("target/test-md");
        File input = new File("target/UVMS-GameMaster-merged.xml");
        deleteDir(output.toPath());

        cli.setInput(Collections.singletonList(input));
        convert.setOutputPath(output);

        Assertions.assertEquals(CommandLine.ExitCode.OK, convert.call());
    }

    @Test
    void testFc5Xml() throws Exception {
        File output = new File("target/test-fc5");
        File inputXml = new File("src/test/resources/FC5-Collection.xml");
        File mergedXml = new File("target/test-fc5/FC5-Collection-merged.xml");
        deleteDir(output.toPath());

        // start with input/collection xml
        cli.setInput(Collections.singletonList(inputXml));

        validate.validationSource = new Validate.ValidationSource(true);
        Assertions.assertEquals(CommandLine.ExitCode.OK, validate.call());

        transform.setOutputPath(output);
        transform.suffix = "-merged";
        Assertions.assertEquals(CommandLine.ExitCode.OK, transform.call());

        // Work with merged compendium xml
        cli.setInput(Collections.singletonList(mergedXml));

        convert.setOutputPath(output);
        Assertions.assertEquals(CommandLine.ExitCode.OK, convert.call());

        validate.validationSource = new Validate.ValidationSource(false);
        Assertions.assertEquals(CommandLine.ExitCode.OK, validate.call());
    }

    @Test
    void testExportedXml() throws Exception {
        File output = new File("target/test-exported");
        File input = new File("src/test/resources/testResources.xml");
        deleteDir(output.toPath());

        cli.setInput(Collections.singletonList(input));

        convert.setOutputPath(output);
        Assertions.assertEquals(CommandLine.ExitCode.OK, convert.call());
    }

    public static void deleteDir(Path path) throws Exception {
        if (!path.toFile().exists()) {
            return;
        }

        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);

        Assertions.assertFalse(path.toFile().exists());
    }
}
