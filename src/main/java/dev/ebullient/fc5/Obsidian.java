package dev.ebullient.fc5;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import dev.ebullient.fc5.data.CompendiumType;
import dev.ebullient.fc5.data.CompendiumXmlReader;
import dev.ebullient.fc5.data.MarkdownWriter;
import dev.ebullient.fc5.data.MarkdownWriter.WrappedIOException;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

@Command(name = "obsidian", mixinStandardHelpOptions = true, header = "Create Obsidian.md Markdown references from XML file", sortOptions = false)
public class Obsidian implements Callable<Integer> {

    @Spec
    CommandSpec spec;

    @ParentCommand
    Fc5ConvertCli parent;

    final Templates tpl;
    final CompendiumXmlReader reader;

    Path output;

    Obsidian(Templates tpl) {
        this.tpl = tpl;
        reader = new CompendiumXmlReader();
    }

    @Option(names = "-o", description = "Output directory", required = true)
    void setOutputPath(File outputDir) {
        output = outputDir.toPath().toAbsolutePath().normalize();
        if (output.toFile().exists() && output.toFile().isFile()) {
            throw new ParameterException(spec.commandLine(),
                    "Specified output path exists and is a file: " + output.toString());
        }
    }

    @ArgGroup(exclusive = false)
    TemplatePaths paths = new TemplatePaths();

    @Override
    public Integer call() throws Exception {
        boolean allOk = true;

        Log.debugf("Defined templates: %s", paths.customTemplates.toString());
        tpl.setCustomTemplates(paths);
        Log.debugf("Defined templates: %s", tpl);

        MarkdownWriter writer = new MarkdownWriter(output, tpl);
        Log.outPrintln("💡 Writing files to " + output);

        for (Path sourcePath : parent.input) {
            Log.outPrintln("⏱ Reading " + sourcePath.getFileName());

            try (InputStream is = new BufferedInputStream(new FileInputStream(sourcePath.toFile()))) {
                CompendiumType compendium = reader.parseXMLInputStream(is);
                Log.outPrintln("  ✅ Done.");

                writer.writeFiles(compendium.getBackgrounds(), "Backgrounds");
                writer.writeFiles(compendium.getClasses(), "Classes");
                writer.writeFiles(compendium.getFeats(), "Feats");
                writer.writeFiles(compendium.getItems(), "Items");
                writer.writeFiles(compendium.getMonsters(), "Monsters");
                writer.writeFiles(compendium.getRaces(), "Races");
                writer.writeFiles(compendium.getSpells(), "Spells");
            } catch (IOException | WrappedIOException e) {
                allOk = false;
                if (e instanceof WrappedIOException) {
                    Log.outPrintln("⛔️ Exception: " + e.getCause().getMessage());
                } else {
                    Log.outPrintln("⛔️ Exception: " + e.getMessage());
                }
            }
        }
        return allOk ? CommandLine.ExitCode.OK : CommandLine.ExitCode.SOFTWARE;
    }
}
