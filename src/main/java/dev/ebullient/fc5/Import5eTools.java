package dev.ebullient.fc5;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;

import dev.ebullient.fc5.data.MarkdownWriter;
import dev.ebullient.fc5.json.CompendiumConverter;
import dev.ebullient.fc5.json.JsonIndex;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

@Command(name = "5etools", mixinStandardHelpOptions = true, header = "Convert 5etools data to markdown", description = {
        "This will read from a 5etools json file and will produce xml or markdown documents (based on options)."
}, footer = {
        "Use the sources option to filter converted items by source. If no sources",
        "are specified, only items from the SRD will be included.",
        "Specify values as they appear in the exported json, e.g. -s PHB -s DMG.",
        "Only include items from sources you own."
})
public class Import5eTools implements Callable<Integer> {
    public final static ObjectMapper MAPPER = new ObjectMapper()
            .setVisibility(VisibilityChecker.Std.defaultInstance().with(JsonAutoDetect.Visibility.ANY));

    @Spec
    CommandSpec spec;

    @ParentCommand
    Fc5ConvertCli parent;

    @Option(names = "-s", description = "Source Books%n  Comma-separated list or multiple declarations (PHB,DMG,...)")
    List<String> source = Collections.emptyList();

    @Option(names = "-x", description = "Suffix (xml for FightClub 5 xml files of json for source-filtered json)")
    String suffix;

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

        if (source.size() == 1 && source.contains(",")) {
            String tmp = source.remove(0);
            source = List.of(tmp.split(","));
        }

        Log.outPrintf("Importing/Converting items from 5e tools %s to %s. %s\n",
                parent.input, output, source.isEmpty()
                    ? "Including only SRD items."
                    : "Including items from " + source);

        output.toFile().mkdirs();

        boolean allOk = true;
        JsonIndex index = new JsonIndex(source);

        for (Path inputPath : parent.input) {
            try {
                Log.debugf("⏱  Reading %s; will filter ", inputPath);
                File f = inputPath.toFile();
                JsonNode node = MAPPER.readTree(f);
                if (node.has("name")) {
                    processNameList(node.get("name"), index);
                    Log.outPrintln("✅ Finished processing names form " + inputPath);
                    continue;
                }
                index.importTree(node);
                Log.outPrintf("🔖 Finished reading %s\n", inputPath);
            } catch (IOException e) {
                Log.error(e, "  Exception: " + e.getMessage());
                allOk = false;
            }
        }

        if (suffix == null || suffix.endsWith("xml")) {
            new CompendiumConverter(index)
                .parseElements()
                .writeToXml(output.resolve("compendium.xml"));
        }

        return allOk ? ExitCode.OK : ExitCode.SOFTWARE;
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
