package dev.ebullient.fc5;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import dev.ebullient.fc5.data.ParsingTestBase;
import io.quarkus.test.junit.QuarkusTest;
import picocli.CommandLine;

@QuarkusTest
//@Disabled
public class XmlReadTest extends ParsingTestBase {

    @Test
    void testMergedXml() throws Exception {
        File output = new File("target/test-md");
        File input = new File("target/UVMS-GameMaster-merged.xml");
        deleteDir(output.toPath());

        ConvertCli cli = new ConvertCli();
        cli.setInput(Collections.singletonList(input));

        Convert convert = new Convert(templates);
        convert.parent = cli;
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
