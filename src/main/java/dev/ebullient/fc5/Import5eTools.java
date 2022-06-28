package dev.ebullient.fc5;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;

import dev.ebullient.fc5.json2xml.Json2XmlConverter;
import dev.ebullient.fc5.json5e.Json2MarkdownConverter;
import dev.ebullient.fc5.json5e.JsonIndex;
import dev.ebullient.fc5.json5e.JsonIndex.IndexType;
import dev.ebullient.fc5.pojo.MarkdownWriter;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

@Command(name = "5etools", mixinStandardHelpOptions = true, header = "Convert 5etools data to markdown", description = {
        "This will read from a 5etools json file (or the 5etools data directory) and will produce xml or markdown documents (based on options).",
}, footer = {
        "Use the sources option to filter converted items by source. If no sources",
        "are specified, only items from the SRD will be included.",
        "Specify values as they appear in the exported json, e.g. -s PHB -s DMG.",
        "Only include items from sources you own.",
        "",
        "You can describe sources and specify specific identifiers to be excluded in a json file, e.g.",
        "{",
        "  \"from\" : [",
        "    \"PHB\",",
        "    \"DMG\",",
        "    \"SCAG\",",
        "  ]",
        "  \"exclude\" : [",
        "    \"background|sage|phb\",",
        "  ]",
        "  \"excludePattern\" : [",
        "    \"race|.*|dmg\",",
        "  ]",
        "}",
        "",
        "Pass this file in as another input source. Use the identifiers from the generated index files in the list of excluded rules."
})
public class Import5eTools implements Callable<Integer> {
    public final static ObjectMapper MAPPER = new ObjectMapper()
            .setVisibility(VisibilityChecker.Std.defaultInstance().with(JsonAutoDetect.Visibility.ANY));

    final Templates tpl;

    Import5eTools(Templates tpl) {
        this.tpl = tpl;
    }

    @Spec
    CommandSpec spec;

    @ParentCommand
    Fc5ConvertCli parent;

    @Option(names = "-s", description = "Source Books%n  Comma-separated list or multiple declarations (PHB,DMG,...)")
    List<String> source = Collections.emptyList();

    @Option(names = "--xml", description = "Generate FightClub 5 XML Compendium files")
    boolean createXml;

    @Option(names = "--md", description = "Generate Markdown files")
    boolean createMarkdown;

    @Option(names = "--index", description = "Create index of keys that can be used to exclude entries")
    boolean filterIndex;

    @ArgGroup(exclusive = false)
    TemplatePaths paths = new TemplatePaths();

    Path output;

    @Option(names = "-o", description = "Output directory", required = true)
    void setOutputPath(File outputDir) {
        output = outputDir.toPath().toAbsolutePath().normalize();
        if (output.toFile().exists() && output.toFile().isFile()) {
            throw new ParameterException(spec.commandLine(),
                    "Specified output path exists and is a file: " + output.toString());
        }
    }

    @Override
    public Integer call() throws Exception {
        if (parent.input == null || parent.input.isEmpty()) {
            throw new CommandLine.MissingParameterException(spec.commandLine(), spec.args(),
                    "Must specify an input file");
        }
        if (!output.toFile().exists() && !output.toFile().mkdirs()) {
            Log.errorf("Unable to create output directory: %s", output);
            return ExitCode.USAGE;
        }

        if (source.size() == 1 && source.get(0).contains(",")) {
            String tmp = source.remove(0);
            source = List.of(tmp.split(","));
        }

        Log.outPrintf("Importing/Converting items from 5e tools %s to %s.\n",
                parent.input, output);

        boolean allOk = true;
        JsonIndex index = new JsonIndex(source);

        for (Path inputPath : parent.input) {
            Log.outPrintf("⏱  Reading 5eTools data from %s%n", inputPath);
            try {
                if (inputPath.toFile().isDirectory()) {
                    List<String> inputs = List.of("backgrounds.json", "fluff-backgrounds.json", "bestiary", "class",
                            "feats.json", "items.json", "items-base.json", "fluff-items.json", "optionalfeatures.json",
                            "races.json", "fluff-races.json",
                            "spells", "variantrules.json");
                    for (String input : inputs) {
                        Path p = inputPath.resolve(input);
                        if (p.toFile().isFile()) {
                            readFile(index, p);
                        } else {
                            fullIndex(index, p);
                        }
                    }
                } else {
                    readFile(index, inputPath);
                }
            } catch (IOException e) {
                Log.error(e, "  Exception: " + e.getMessage());
                allOk = false;
            }
            Log.outPrintln("✅ finished reading 5etools data.");
        }

        if (filterIndex) {
            index.writeIndex(output.resolve("all-index.json"));
            index.writeSourceIndex(output.resolve("src-index.json"));
        }

        if (createXml) {
            new Json2XmlConverter(index)
                    .parseElements()
                    .writeToXml(output.resolve("compendium.xml"))
                    .writeTypeToXml(IndexType.background, output.resolve("backgrounds.xml"))
                    .writeTypeToXml(IndexType.classtype, output.resolve("classes.xml"))
                    .writeTypeToXml(IndexType.feat, output.resolve("feats.xml"))
                    .writeTypeToXml(IndexType.item, output.resolve("items.xml"))
                    .writeTypeToXml(IndexType.monster, output.resolve("monsters.xml"))
                    .writeTypeToXml(IndexType.race, output.resolve("races.xml"))
                    .writeTypeToXml(IndexType.spell, output.resolve("spells.xml"));
        }

        if (createMarkdown) {
            Log.debugf("Custom templates: %s", paths.customTemplates.toString());
            tpl.setCustomTemplates(paths);
            Log.debugf("Defined templates: %s", tpl);

            MarkdownWriter writer = new MarkdownWriter(output, tpl);
            Log.outPrintln("💡 Writing files to " + output);
            new Json2MarkdownConverter(index, writer)
                    .writeFiles(IndexType.background, "Backgrounds")
                    .writeFiles(IndexType.classtype, "Classes")
                    .writeFiles(IndexType.feat, "Feats")
                    .writeFiles(IndexType.item, "Items")
                    .writeFiles(IndexType.monster, "Monsters")
                    .writeFiles(IndexType.race, "Races")
                    .writeFiles(IndexType.spell, "Spells");
        }
        return allOk ? ExitCode.OK : ExitCode.SOFTWARE;
    }

    protected void fullIndex(JsonIndex index, Path resourcePath) throws IOException {
        String basename = resourcePath.getFileName().toString();

        Log.debugf("📁 %s\n", resourcePath);
        try (Stream<Path> stream = Files.list(resourcePath)) {
            stream.forEach(p -> {
                File f = p.toFile();
                String name = p.getFileName().toString();
                if (f.isDirectory()) {
                    try {
                        fullIndex(index, p);
                    } catch (Exception e) {
                        Log.errorf(e, "Error parsing %s", p.toString());
                    }
                } else if ((name.startsWith("fluff") || name.startsWith(basename)) && name.endsWith(".json")) {
                    try {
                        readFile(index, p);
                    } catch (Exception e) {
                        Log.errorf(e, "Error parsing %s", resourcePath.toString());
                    }
                }
            });
        } catch (Exception e) {
            Log.errorf(e, "Error parsing %s", resourcePath.toString());
        }
    }

    void readFile(JsonIndex index, Path inputPath) throws IOException {
        File f = inputPath.toFile();
        JsonNode node = MAPPER.readTree(f);
        if (node.has("name")) {
            processNameList(node.get("name"), index);
            Log.outPrintln("✅ Finished processing names from " + inputPath);
        } else {
            index.importTree(node);
            Log.debugf("🔖 Finished reading %s\n", inputPath);
        }
    }

    void processNameList(JsonNode listSource, JsonIndex index) throws IOException {
        for (Iterator<JsonNode> i = listSource.elements(); i.hasNext();) {
            JsonNode element = i.next();
            String name = element.get("name").asText();

            JsonNode tables = element.get("tables");
            if (tables == null || tables.isEmpty()) {
                Log.debugf("Skipped %s; no tables", name);
                continue;
            }

            boolean isSRD = element.has("srd");
            JsonNode itemSource = element.get("source");
            if (index.excludeItem(itemSource, isSRD)) {
                // skip this item: not from a specified source
                Log.debugf("Skipped %s from %s (%s)", name, itemSource, isSRD);
                continue;
            }

            Path target = output.resolve("names-" + MarkdownWriter.slugifier().slugify(name) + ".md");
            try (PrintWriter writer = new PrintWriter(new FileWriter(target.toFile()))) {
                writer.println("---");
                writer.println("tags: [ table ]");
                writer.println("---");
                writer.printf("# %s%n", name);

                for (Iterator<JsonNode> j = tables.elements(); j.hasNext();) {
                    JsonNode t_element = j.next();
                    writer.printf("%n## %s%n%n", t_element.get("option").asText());
                    writer.printf("| dice: d%s | Name |%n", t_element.get("diceType").asText());
                    writer.println("|------------|----------------------|");

                    JsonNode table = t_element.get("table");
                    for (Iterator<JsonNode> k = table.elements(); k.hasNext();) {
                        JsonNode row = k.next();
                        String min = row.get("min").asText();
                        String max = row.get("max").asText();
                        if ("0".equals(max)) {
                            max = "100";
                        }
                        writer.printf("| %s-%s | %s |%n", min, max, row.get("result").asText());
                    }
                    String blockId = MarkdownWriter.slugifier().slugify(t_element.get("option").asText());
                    writer.printf("^%s%n", blockId);
                }
            }
        }
    }
}
