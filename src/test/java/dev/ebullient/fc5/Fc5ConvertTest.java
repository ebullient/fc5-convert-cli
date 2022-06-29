package dev.ebullient.fc5;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
public class Fc5ConvertTest {
    final static Path PROJECT_PATH = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
    final static Path OUTPUT_PATH = PROJECT_PATH.resolve("target/fc5-convert");
    final static Path CUSTOM_OUTPUT_PATH = PROJECT_PATH.resolve("target/fc5-convert-custom");
    final static Path MIXED_OUTPUT_PATH = PROJECT_PATH.resolve("target/fc5-convert-mixed");

    @Test
    @Launch({})
    void testThings() {
        System.out.println("Life is good");
    }

    @Test
    @Launch({ "--help" })
    void testCommandHelp(LaunchResult result) {
        result.echoSystemOut();
        Assertions.assertTrue(result.getOutput().contains("Usage: fc5-convert [-hvV] [<input>...] [COMMAND]"),
                "Result should contain the CLI help message. Found: " + dump(result));
    }

    @Test
    @Launch({ "obsidian", "--help" })
    void testObsidianCommandHelp(LaunchResult result) {
        result.echoSystemOut();
        Assertions.assertTrue(result.getOutput().contains("Usage: fc5-convert obsidian [-hvV] -o=<outputPath>"),
                "Result should contain the CLI help message. Found: " + dump(result));
    }

    @Test
    @Launch({ "transform", "--help" })
    void testTransformCommandHelp(LaunchResult result) {
        result.echoSystemOut();
        Assertions.assertTrue(
                result.getOutput().contains("Usage: fc5-convert transform [-hvV] -o=<outputPath> [-t=<xsltFile>]"),
                "Result should contain the CLI help message. Found: " + dump(result));
    }

    @Test
    @Launch({ "validate", "--help" })
    void testValidateCommandHelp(LaunchResult result) {
        result.echoSystemOut();
        Assertions.assertTrue(
                result.getOutput().contains("Usage: fc5-convert validate [-hvV] [--collection | [--compendium] |"),
                "Result should contain the CLI help message. Found: " + dump(result));
    }

    @Test
    @Launch({ "validate", "--compendium", "src/test/resources/FC5-Compendium.xml" })
    void testValidateXmlHelp(LaunchResult result) {
        result.echoSystemOut();
        // Assertions.assertTrue(
        //         result.getOutput().contains("Usage: fc5-convert validate [-hvV] [--collection | [--compendium] |"),
        //         "Result should contain the CLI help message. Found: " + dump(result));
    }

    @Test
    void testFc5Xml(TestInfo info, QuarkusMainLauncher launcher) {
        LaunchResult result;

        result = launcher.launch("validate", "--collection", "src/test/resources/FC5-Collection.xml");
        Assertions.assertEquals(0, result.exitCode(), "An error occurred. " + dump(result));

        result = launcher.launch("validate", "--compendium", "src/test/resources/FC5-Compendium.xml");
        Assertions.assertEquals(0, result.exitCode(), "An error occurred. " + dump(result));

        result = launcher.launch("transform", "-o", OUTPUT_PATH.toString(), "-x", "-merged",
                "src/test/resources/FC5-Collection.xml");
        Assertions.assertEquals(0, result.exitCode(), "An error occurred. " + dump(result));

        result = launcher.launch("validate", "--compendium", OUTPUT_PATH.resolve("FC5-Collection-merged.xml").toString());
        Assertions.assertEquals(0, result.exitCode(), "An error occurred. " + dump(result));

        result = launcher.launch("obsidian", "-o", OUTPUT_PATH.toString(),
                OUTPUT_PATH.resolve("FC5-Collection-merged.xml").toString());
        Assertions.assertEquals(0, result.exitCode(), "An error occurred. " + dump(result));

        result = launcher.launch("transform", "--verbose", "-o", OUTPUT_PATH.toString(),
                "-t", "src/main/resources/itemExport.xslt",
                "-x", ".csv",
                OUTPUT_PATH.resolve("FC5-Collection-merged.xml").toString());
        Assertions.assertEquals(0, result.exitCode(), "An error occurred. " + dump(result));
    }

    @Test
    void testCustomTemplates(TestInfo info, QuarkusMainLauncher launcher) throws IOException {
        LaunchResult result = launcher.launch("obsidian", "-o", CUSTOM_OUTPUT_PATH.toString(),
                "--background", "src/test/resources/customTemplates/background2md.txt",
                "--class", "src/test/resources/customTemplates/class2md.txt",
                "--feat", "src/test/resources/customTemplates/feat2md.txt",
                "--item", "src/test/resources/customTemplates/item2md.txt",
                "--monster", "src/test/resources/customTemplates/monster2-5estatblock-md.txt",
                "--race", "src/test/resources/customTemplates/race2md.txt",
                "--spell", "src/test/resources/customTemplates/spell2md.txt",
                "src/test/resources/FC5-Compendium.xml");

        Assertions.assertEquals(0, result.exitCode(), "An error occurred. " + dump(result));
        Assertions.assertTrue(Files.readString(CUSTOM_OUTPUT_PATH.resolve("backgrounds/acolyte.md")).contains("CUSTOM"),
                "File should contain CUSTOM");
        Assertions.assertTrue(Files.readString(CUSTOM_OUTPUT_PATH.resolve("classes/barbarian.md")).contains("CUSTOM"),
                "File should contain CUSTOM");
        Assertions.assertTrue(Files.readString(CUSTOM_OUTPUT_PATH.resolve("feats/grappler.md")).contains("CUSTOM"),
                "File should contain CUSTOM");
        Assertions.assertTrue(Files.readString(CUSTOM_OUTPUT_PATH.resolve("items/abacus.md")).contains("CUSTOM"),
                "File should contain CUSTOM");

        String monster = Files.readString(CUSTOM_OUTPUT_PATH.resolve("bestiary/aberration/aboleth.md"));
        Assertions.assertTrue(monster.contains("```statblock"),
                "File should contain ```statblock");
        Assertions.assertFalse(monster.contains("{resource."),
                "File should not contain unresolved resource strings");

        Assertions.assertTrue(Files.readString(CUSTOM_OUTPUT_PATH.resolve("races/dragonborn.md")).contains("CUSTOM"),
                "File should contain CUSTOM");
        Assertions.assertTrue(Files.readString(CUSTOM_OUTPUT_PATH.resolve("spells/acid-arrow.md")).contains("CUSTOM"),
                "File should contain CUSTOM");
    }

    @Test
    void testMixedMonsterTemplate(TestInfo info, QuarkusMainLauncher launcher) throws IOException {
        LaunchResult result = launcher.launch("obsidian", "-o", MIXED_OUTPUT_PATH.toString(),
                "--monster", "src/test/resources/customTemplates/monster2-pieces.txt",
                "src/test/resources/FC5-Compendium.xml");
        Assertions.assertEquals(0, result.exitCode(), "An error occurred. " + dump(result));

        String monster = Files.readString(MIXED_OUTPUT_PATH.resolve("bestiary/aberration/aboleth.md"));
        Assertions.assertTrue(monster.contains("```ad-statblock"),
                "File should contain ```ad-statblock");
        Assertions.assertFalse(monster.contains("{resource."),
                "File should not contain unresolved resource strings");
        Assertions.assertTrue(monster.contains("\"legendary_actions\":"),
                "File should contain statblock elements in the header metadata");
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

    public static String dump(LaunchResult result) {
        return "\n" + result.getOutput() + "\nSystem err:\n" + result.getErrorOutput();
    }
}
