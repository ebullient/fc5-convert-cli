package dev.ebullient.fc5;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import dev.ebullient.test.QuarkusMainLauncher;
import dev.ebullient.test.QuarkusMainLauncher.LaunchPaths;
import dev.ebullient.test.QuarkusMainLauncher.LaunchResult;
import dev.ebullient.test.QuarkusMainTest;
import dev.ebullient.test.QuarkusMainTest.Launch;

@QuarkusMainTest
public class Fc5ConvertIT {
    final static Path PROJECT_PATH = Paths.get(System.getProperty("user.dir")).toAbsolutePath();

    @BeforeEach
    void cleanUp(TestInfo info, LaunchPaths paths) {
        Path targetDir = paths.getProjectBuildPath().resolve(info.getDisplayName());
        deleteDir(targetDir);
    }

    @Test
    @Launch(args = {})
    void testThings() {
        System.out.println("Life is good");
    }

    @Test
    @Launch(args = { "--help" })
    void testCommandHelp(LaunchResult result) {
        System.out.println(result.getOutputStream().stream().collect(Collectors.joining("\n")));
        Assertions.assertTrue(result.getOutputStream().stream()
                .anyMatch(x -> x.startsWith("Usage: fc5-convert [-hvV] [<input>...] [COMMAND]")),
                "Result should contain the CLI help message. Found: " + result);
    }

    @Test
    @Launch(args = { "obsidian", "--help" })
    void testObsidianCommandHelp(LaunchResult result) {
        System.out.println(result.getOutputStream().stream().collect(Collectors.joining("\n")));
        Assertions.assertTrue(result.getOutputStream().stream()
                .anyMatch(x -> x.startsWith("Usage: fc5-convert obsidian [-hvV] -o=<outputPath> [<input>...]")),
                "Result should contain the CLI help message. Found: " + result);
    }

    @Test
    @Launch(args = { "transform", "--help" })
    void testTransformCommandHelp(LaunchResult result) {
        System.out.println(result.getOutputStream().stream().collect(Collectors.joining("\n")));
        Assertions.assertTrue(result.getOutputStream().stream()
                .anyMatch(x -> x.startsWith("Usage: fc5-convert transform [-hvV] -o=<outputPath> [-t=<xsltFile>]")),
                "Result should contain the CLI help message. Found: " + result);
    }

    @Test
    @Launch(args = { "validate", "--help" })
    void testValidateCommandHelp(LaunchResult result) {
        System.out.println(result.getOutputStream().stream().collect(Collectors.joining("\n")));
        Assertions.assertTrue(result.getOutputStream().stream()
                .anyMatch(x -> x.startsWith("Usage: fc5-convert validate [-hvV] [--collection | [--compendium] |")),
                "Result should contain the CLI help message. Found: " + result);
    }

    @Test
    void testFc5Xml(TestInfo info, QuarkusMainLauncher launcher, LaunchPaths paths) {
        final Path targetDir = paths.getProjectBuildPath().resolve(info.getDisplayName());
        LaunchResult result;

        result = launcher.runQuarkusMain(PROJECT_PATH,
                "validate", "--collection", "./src/test/resources/FC5-Collection.xml");
        Assertions.assertEquals(0, result.returnCode(), "An error occurred. " + result);

        result = launcher.runQuarkusMain(PROJECT_PATH,
                "validate", "--compendium", "src/test/resources/FC5-Compendium.xml");
        Assertions.assertEquals(0, result.returnCode(), "An error occurred. " + result);

        result = launcher.runQuarkusMain(PROJECT_PATH,
                "transform", "-o", targetDir.toString(), "-x", "-merged", "src/test/resources/FC5-Collection.xml");
        Assertions.assertEquals(0, result.returnCode(), "An error occurred. " + result);

        result = launcher.runQuarkusMain(PROJECT_PATH,
                "validate", "--compendium", targetDir.resolve("FC5-Collection-merged.xml").toString());
        Assertions.assertEquals(0, result.returnCode(), "An error occurred. " + result);

        result = launcher.runQuarkusMain(PROJECT_PATH,
                "validate", "--compendium", targetDir.resolve("FC5-Collection-merged.xml").toString());
        Assertions.assertEquals(0, result.returnCode(), "An error occurred. " + result);

        result = launcher.runQuarkusMain(PROJECT_PATH,
                "obsidian", "-o", targetDir.toString(), targetDir.resolve("FC5-Collection-merged.xml").toString());
        Assertions.assertEquals(0, result.returnCode(), "An error occurred. " + result);
    }

    @Test
    @Launch(args = { "obsidian", "-o", "testExportedXml", "../src/test/resources/testResources.xml" })
    void testExportedXml(LaunchResult result) throws Exception {
        System.out.println(String.join("\n", result.getOutputStream()));
    }

    public static void deleteDir(Path path) {
        if (!path.toFile().exists()) {
            return;
        }

        try {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        Assertions.assertFalse(path.toFile().exists());
    }
}
