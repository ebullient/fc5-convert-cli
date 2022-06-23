package dev.ebullient.fc5.json2xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import dev.ebullient.fc5.json5e.JsonIndex;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.Import5eTools;
import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.json5e.JsonIndex.IndexType;

public class JsonDataTest {
    final static Path PROJECT_PATH = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
    final static Path OUTPUT_PATH = PROJECT_PATH.resolve("target/5etools-import/jsondata");

    // for compile/test purposes. Must clone/sync separately.
    final static Path TOOLS_PATH = PROJECT_PATH.resolve("5etools-mirror-1.github.io/data");

    protected JsonNode doParse(String resourceName) throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            return Import5eTools.MAPPER.readTree(is);
        }
    }

    protected JsonNode doParse(Path resourcePath) throws Exception {
        try (InputStream is = Files.newInputStream(resourcePath, StandardOpenOption.READ)) {
            return Import5eTools.MAPPER.readTree(is);
        }
    }

    protected void fullIndex(JsonIndex index, Path resourcePath) throws Exception {
        try (Stream<Path> stream = Files.list(resourcePath)) {
            stream.forEach(p -> {
                File f = p.toFile();
                if (f.isDirectory()) {
                    try {
                        fullIndex(index, p);
                    } catch (Exception e) {
                        Log.errorf(e, "Error parsing %s", p.toString());
                    }
                } else if (f.getName().endsWith(".json")) {
                    try {
                        index.importTree(doParse(p));
                    } catch (Exception e) {
                        Log.errorf(e, "Error parsing %s", p.toString());
                    }
                }
            });
        }
    }

    @BeforeAll
    public static void setupDir() {
        Log.setVerbose(true);
        OUTPUT_PATH.toFile().mkdirs();
    }

    @Test
    public void testFullKeyIndex() throws Exception {
        if (TOOLS_PATH.toFile().exists()) {
            List<String> source = List.of("*");

            JsonIndex index = new JsonIndex(source);
            fullIndex(index, TOOLS_PATH);

            Path p = OUTPUT_PATH.resolve("allIndex.json");
            index.writeIndex(p);
        }
    }

    @Test
    public void testFilteredKeyIndex() throws Exception {
        if (TOOLS_PATH.toFile().exists()) {
            List<String> source = List.of("PHB", "DMG", "XGE");

            JsonIndex index = new JsonIndex(source);
            fullIndex(index, TOOLS_PATH);

            Path p = OUTPUT_PATH.resolve("filteredIndex.json");
            index.writeSourceIndex(p);
        }
    }

    @Test
    public void testBackground() throws Exception {
        List<String> source = List.of("PHB", "DMG", "XGE");
        JsonNode data = doParse("backgrounds.json");
        JsonIndex index = new JsonIndex(source).importTree(data);
        CompendiumConverter converter = new CompendiumConverter(index).parseElements();

        Path p = OUTPUT_PATH.resolve("backgrounds.xml");
        converter.writeToXml(p);
        assertFileContent(p);
    }

    @Test
    public void testAllBackgrounds() throws Exception {
        if (TOOLS_PATH.toFile().exists()) {
            List<String> source = List.of("*");
            JsonIndex index = new JsonIndex(source)
                    .importTree(doParse(TOOLS_PATH.resolve("backgrounds.json")))
                    .importTree(doParse(TOOLS_PATH.resolve("fluff-backgrounds.json")));

            CompendiumConverter converter = new CompendiumConverter(index).parseElements();

            Path p1 = OUTPUT_PATH.resolve("5etools_all_backgrounds.xml");
            Path p2 = OUTPUT_PATH.resolve("5etools_backgrounds.xml");
            converter.writeToXml(p1)
                    .writeTypeToXml(IndexType.background, p2);
            assertFileContent(p1);
            assertSameContents(p1, p2);
        }
    }

    @Test
    public void testClass() throws Exception {
        List<String> source = List.of("PHB", "DMG", "XGE");
        JsonNode data = doParse("class-cleric.json");
        JsonIndex index = new JsonIndex(source).importTree(data);
        CompendiumConverter converter = new CompendiumConverter(index).parseElements();

        Path p = OUTPUT_PATH.resolve("class.xml");
        converter.writeToXml(p);
        assertFileContent(p);
    }

    @Test
    public void testAllClasses() throws Exception {
        if (TOOLS_PATH.toFile().exists()) {
            Path classes = TOOLS_PATH.resolve("class");
            List<String> source = List.of("*");

            JsonIndex index = new JsonIndex(source);
            try (Stream<Path> stream = Files.list(classes)) {
                stream
                        .filter(p -> p.toFile().getName().startsWith("class-"))
                        .filter(p -> p.toFile().getName().endsWith(".json"))
                        .forEach(p -> {
                            try {
                                index.importTree(doParse(p));
                            } catch (Exception e) {
                                Log.errorf(e, "Unable to read/parse %s", p.toString());
                            }
                        });
            }
            index.importTree(doParse(TOOLS_PATH.resolve("optionalfeatures.json")));

            CompendiumConverter converter = new CompendiumConverter(index).parseElements();

            Path p1 = OUTPUT_PATH.resolve("5etools_all_classes.xml");
            Path p2 = OUTPUT_PATH.resolve("5etools_classes.xml");
            converter.writeToXml(p1)
                    .writeTypeToXml(IndexType.classtype, p2);
            assertFileContent(p1);
            assertSameContents(p1, p2);
        }
    }

    @Test
    public void testFeat() throws Exception {
        List<String> source = List.of("PHB", "DMG", "XGE");
        JsonNode data = doParse("feats.json");
        JsonIndex index = new JsonIndex(source).importTree(data);
        CompendiumConverter converter = new CompendiumConverter(index).parseElements();

        Path p = OUTPUT_PATH.resolve("feats.xml");
        converter.writeToXml(p);
        assertFileContent(p);
    }

    @Test
    public void testAllFeats() throws Exception {
        if (TOOLS_PATH.toFile().exists()) {
            List<String> source = List.of("*");
            JsonIndex index = new JsonIndex(source)
                    .importTree(doParse(TOOLS_PATH.resolve("feats.json")));

            CompendiumConverter converter = new CompendiumConverter(index).parseElements();

            Path p1 = OUTPUT_PATH.resolve("5etools_all_feats.xml");
            Path p2 = OUTPUT_PATH.resolve("5etools_feats.xml");
            converter.writeToXml(p1)
                    .writeTypeToXml(IndexType.feat, p2);
            assertFileContent(p1);
            assertSameContents(p1, p2);
        }
    }

    @Test
    public void testItem() throws Exception {
        List<String> source = List.of("PHB", "DMG", "XGE");
        JsonNode data = doParse("items.json");
        JsonIndex index = new JsonIndex(source).importTree(data);
        CompendiumConverter converter = new CompendiumConverter(index).parseElements();

        Path p = OUTPUT_PATH.resolve("items.xml");
        converter.writeToXml(p);
        assertFileContent(p);
    }

    @Test
    public void testAllItems() throws Exception {
        if (TOOLS_PATH.toFile().exists()) {
            List<String> source = List.of("*");
            JsonIndex index = new JsonIndex(source)
                    .importTree(doParse(TOOLS_PATH.resolve("items-base.json")))
                    .importTree(doParse(TOOLS_PATH.resolve("items.json")))
                    .importTree(doParse(TOOLS_PATH.resolve("fluff-items.json")));

            CompendiumConverter converter = new CompendiumConverter(index).parseElements();

            Path p1 = OUTPUT_PATH.resolve("5etools_all_items.xml");
            Path p2 = OUTPUT_PATH.resolve("5etools_items.xml");
            converter.writeToXml(p1)
                    .writeTypeToXml(IndexType.item, p2);
            assertFileContent(p1);
            assertSameContents(p1, p2);
        }
    }

    @Test
    public void testAllMonsters() throws Exception {
        if (TOOLS_PATH.toFile().exists()) {
            Path classes = TOOLS_PATH.resolve("bestiary");
            List<String> source = List.of("*");

            JsonIndex index = new JsonIndex(source);
            try (Stream<Path> stream = Files.list(classes)) {
                stream
                        .filter(p -> p.toFile().getName().contains("bestiary-"))
                        .filter(p -> p.toFile().getName().endsWith(".json"))
                        .forEach(p -> {
                            try {
                                index.importTree(doParse(p));
                            } catch (Exception e) {
                                Log.errorf(e, "Unable to read/parse %s", p.toString());
                            }
                        });
            }

            CompendiumConverter converter = new CompendiumConverter(index).parseElements();

            Path p1 = OUTPUT_PATH.resolve("5etools_all_monsters.xml");
            Path p2 = OUTPUT_PATH.resolve("5etools_monsters.xml");
            converter.writeToXml(p1)
                    .writeTypeToXml(IndexType.monster, p2);
            assertFileContent(p1);
            assertSameContents(p1, p2);
        }
    }

    @Test
    public void testRaces() throws Exception {
        List<String> source = List.of("PHB", "DMG", "XGE");
        JsonNode data = doParse("races.json");
        JsonIndex index = new JsonIndex(source).importTree(data);
        CompendiumConverter converter = new CompendiumConverter(index).parseElements();

        Path p = OUTPUT_PATH.resolve("races.xml");
        converter.writeToXml(p);
        assertFileContent(p);
    }

    @Test
    public void testAllRaces() throws Exception {
        if (TOOLS_PATH.toFile().exists()) {
            List<String> source = List.of("*");
            JsonIndex index = new JsonIndex(source)
                    .importTree(doParse(TOOLS_PATH.resolve("races.json")))
                    .importTree(doParse(TOOLS_PATH.resolve("fluff-races.json")));

            CompendiumConverter converter = new CompendiumConverter(index).parseElements();

            Path p1 = OUTPUT_PATH.resolve("5etools_all_races.xml");
            Path p2 = OUTPUT_PATH.resolve("5etools_races.xml");
            converter.writeToXml(p1)
                    .writeTypeToXml(IndexType.race, p2);
            assertFileContent(p1);
            assertSameContents(p1, p2);
        }
    }

    @Test
    public void testAllSpells() throws Exception {
        if (TOOLS_PATH.toFile().exists()) {
            Path classes = TOOLS_PATH.resolve("spells");
            List<String> source = List.of("*");
            JsonIndex index = new JsonIndex(source);
            try (Stream<Path> stream = Files.list(classes)) {
                stream
                        .filter(p -> p.toFile().getName().startsWith("spells-"))
                        .filter(p -> p.toFile().getName().endsWith(".json"))
                        .forEach(p -> {
                            try {
                                index.importTree(doParse(p));
                            } catch (Exception e) {
                                Log.errorf(e, "Unable to read/parse %s", p.toString());
                            }
                        });
            }
            CompendiumConverter converter = new CompendiumConverter(index).parseElements();

            Path p1 = OUTPUT_PATH.resolve("5etools_all_spells.xml");
            Path p2 = OUTPUT_PATH.resolve("5etools_spells.xml");
            converter.writeToXml(p1)
                    .writeTypeToXml(IndexType.spell, p2);
            assertFileContent(p1);
            assertSameContents(p1, p2);
        }
    }

    void assertFileContent(Path p) throws IOException {
        List<String> errors = new ArrayList<>();
        Files.readAllLines(p).stream()
                .forEach(l -> {
                    if (l.contains("{@")) {
                        errors.add(String.format("Found {@ in %s: %s", p, l));
                    }
                    if (l.contains("{#")) {
                        errors.add(String.format("Found {# in %s: %s", p, l));
                    }
                });
        assertThat(errors).isEmpty();
    }

    void assertSameContents(Path path1, Path path2) throws IOException {
        try (RandomAccessFile randomAccessFile1 = new RandomAccessFile(path1.toFile(), "r");
                RandomAccessFile randomAccessFile2 = new RandomAccessFile(path2.toFile(), "r")) {

            FileChannel ch1 = randomAccessFile1.getChannel();
            FileChannel ch2 = randomAccessFile2.getChannel();
            assertThat(ch1.size()).isEqualTo(ch2.size());

            long size = ch1.size();
            MappedByteBuffer m1 = ch1.map(FileChannel.MapMode.READ_ONLY, 0L, size);
            MappedByteBuffer m2 = ch2.map(FileChannel.MapMode.READ_ONLY, 0L, size);
            assertThat(m1).isEqualTo(m2);
        }
    }
}
