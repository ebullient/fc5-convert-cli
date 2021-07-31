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
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

@Command(name = "convert", mixinStandardHelpOptions = true, header = "Create Markdown references from XML file")
public class Convert implements Callable<Integer> {

    @Spec
    CommandSpec spec;

    @ParentCommand
    ConvertCli parent;

    final Templates tpl;
    final CompendiumXmlReader reader;

    Path output;

    public Convert(Templates tpl) {
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

    @Override
    public Integer call() throws Exception {
        Log.prepareStreams(spec);
        boolean allOk = true;

        MarkdownWriter writer = new MarkdownWriter(output, tpl);
        Log.out().println("💡 Writing files to " + output);

        for (Path sourcePath : parent.input) {
            Log.out().println("⏱ Reading " + sourcePath.getFileName());

            try (InputStream is = new BufferedInputStream(new FileInputStream(sourcePath.toFile()))) {
                CompendiumType compendium = reader.parseXMLInputStream(is);
                Log.out().println("✅ Done.");

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
                    Log.out().println("⛔️ Exception: " + e.getCause().getMessage());
                } else {
                    Log.out().println("⛔️ Exception: " + e.getMessage());
                }
            }
        }
        return allOk ? CommandLine.ExitCode.OK : CommandLine.ExitCode.SOFTWARE;
    }
}
