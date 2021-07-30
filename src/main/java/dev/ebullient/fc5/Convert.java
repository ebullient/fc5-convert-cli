package dev.ebullient.fc5;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.xml.sax.SAXException;

import dev.ebullient.fc5.data.CompendiumType;
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
    private CommandSpec spec;

    @ParentCommand
    private ConvertCli parent;

    final Templates tpl;
    Path output;

    public Convert(Templates tpl) {
        this.tpl = tpl;
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
        boolean allOk = true;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        MarkdownWriter writer = new MarkdownWriter(output, tpl);

        System.out.println("💡 Writing files to " + output);

        for (Path sourcePath : parent.input) {
            String systemId = sourcePath.toString();
            System.out.println("🧐 Reading " + sourcePath.getFileName());

            try (InputStream is = new FileInputStream(sourcePath.toFile())) {
                CompendiumType compendium = CompendiumType.readCompendium(db, is, systemId);
                writer.writeFiles(compendium.getBackgrounds(), "Backgrounds");
                writer.writeFiles(compendium.getClasses(), "Classes");
                writer.writeFiles(compendium.getFeats(), "Feats");
                writer.writeFiles(compendium.getItems(), "Items");
                writer.writeFiles(compendium.getMonsters(), "Monsters");
                writer.writeFiles(compendium.getRaces(), "Races");
                writer.writeFiles(compendium.getSpells(), "Spells");
            } catch (IOException | SAXException | WrappedIOException e) {
                allOk = false;
                if (e instanceof WrappedIOException) {
                    System.out.println("⛔️ Exception: " + e.getCause().getMessage());
                } else {
                    System.out.println("⛔️ Exception: " + e.getMessage());
                }
            }
            db.reset();
        }
        return allOk ? CommandLine.ExitCode.OK : CommandLine.ExitCode.SOFTWARE;
    }

}
