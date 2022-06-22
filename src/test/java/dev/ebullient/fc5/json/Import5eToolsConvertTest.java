package dev.ebullient.fc5.json;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import dev.ebullient.fc5.Fc5ConvertTest;
import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
public class Import5eToolsConvertTest {
    final static Path PROJECT_PATH = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
    final static Path OUTPUT_PATH = PROJECT_PATH.resolve("target/5etools-import");
    final static Path TOOLS_PATH = PROJECT_PATH.resolve("5etools-mirror-1.github.io/data");

    @Test
    @Launch({ "5etools", "--help" })
    void testCommandHelp(LaunchResult result) {
        result.echoSystemOut();
        assertThat(result.getOutput()).contains("Usage: fc5-convert 5etools")
                .overridingErrorMessage("Result should contain the CLI help message. Found: %s", Fc5ConvertTest.dump(result));
    }

    @Test
    void testImportData(TestInfo info, QuarkusMainLauncher launcher) throws Exception {
        if (TOOLS_PATH.toFile().exists()) {
            LaunchResult result;
            result = launcher.launch("5etools", "--index", "--xml",
                    "-o", OUTPUT_PATH.toString(),
                    "-s", "PHB,DMG,SCAG",
                    TOOLS_PATH.toString(),
                    PROJECT_PATH.resolve("src/test/resources/sources.json").toString());

            assertThat(result.exitCode()).isZero().overridingErrorMessage("An error occurred. %s", Fc5ConvertTest.dump(result));

            String fullIndex = Files.readString(OUTPUT_PATH.resolve("all-index.json"));
            String filteredIndex = Files.readString(OUTPUT_PATH.resolve("src-index.json"));

            assertThat(fullIndex).contains("xge");
            assertThat(fullIndex).contains("race|aarakocra|dmg");
            assertThat(fullIndex).contains("classfeature|bardic versatility|bard||4|tce");
            assertThat(fullIndex).contains("classfeature|bonus proficiencies|warrior sidekick|tce|1");

            assertThat(filteredIndex).doesNotContain("xge").overridingErrorMessage("Did not expect any ids containing xge");
            assertThat(filteredIndex).doesNotContain("race|aarakocra|dmg");
            assertThat(filteredIndex).contains("classfeature|bardic versatility|bard||4|tce"); // tce added in sources.json
            assertThat(filteredIndex).contains("classfeature|bonus proficiencies|warrior sidekick|tce|1"); // tce added in sources.json
        }
    }
}
