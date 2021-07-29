package dev.ebullient.fc5;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.xml.sax.SAXException;

import com.github.slugify.Slugify;

import dev.ebullient.fc5.model.BackgroundType;
import dev.ebullient.fc5.model.BaseType;
import dev.ebullient.fc5.model.ClassType;
import dev.ebullient.fc5.model.CompendiumType;
import dev.ebullient.fc5.model.FeatType;
import dev.ebullient.fc5.model.ItemType;
import dev.ebullient.fc5.model.MonsterType;
import dev.ebullient.fc5.model.RaceType;
import dev.ebullient.fc5.model.SpellType;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

@Command(name = "convert", mixinStandardHelpOptions = true, header = "Create Markdown references from XML file")
public class Convert implements Callable<Integer> {

    Path output;
    final Slugify slg = new Slugify();

    @Spec
    private CommandSpec spec;

    @ParentCommand
    private ConvertCli parent;

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

        slg.withLowerCase(true);

        System.out.println("💡 Writing files to " + output);

        for (Path sourcePath : parent.input) {
            String systemId = sourcePath.toString();
            System.out.println("🧐 Reading " + sourcePath.getFileName());

            try (InputStream is = new FileInputStream(sourcePath.toFile())) {
                CompendiumType compendium = CompendiumType.readCompendium(db, is, systemId);
                writeFiles(compendium.getBackgrounds(), "Backgrounds");
                writeFiles(compendium.getClasses(), "Classes");
                writeFiles(compendium.getFeats(), "Feats");
                writeFiles(compendium.getItems(), "Items");
                writeFiles(compendium.getMonsters(), "Monsters");
                writeFiles(compendium.getRaces(), "Races");
                writeFiles(compendium.getSpells(), "Spells");
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

    <T extends BaseType> void writeFiles(List<T> elements, String typeName) throws IOException {
        List<FileMap> fileMappings = new ArrayList<>();
        String dirName = typeName.toLowerCase();

        elements.forEach(x -> {
            FileMap fileMap = new FileMap(x.getName(), slg.slugify(x.getName()));
            try {
                switch (dirName) {
                    case "backgrounds":
                        writeFile(fileMap, dirName, Templates.background2md((BackgroundType) x).render());
                        break;
                    case "classes":
                        writeFile(fileMap, dirName, Templates.class2md((ClassType) x).render());
                        break;
                    case "feats":
                        writeFile(fileMap, dirName, Templates.feat2md((FeatType) x).render());
                        break;
                    case "items":
                        writeFile(fileMap, dirName, Templates.item2md((ItemType) x).render());
                        break;
                    case "monsters":
                        writeFile(fileMap, dirName, Templates.monster2md((MonsterType) x).render());
                        break;
                    case "races":
                        writeFile(fileMap, dirName, Templates.race2md((RaceType) x).render());
                        break;
                    case "spells":
                        writeFile(fileMap, dirName, Templates.spell2md((SpellType) x).render());
                        break;
                }
            } catch (IOException e) {
                throw new WrappedIOException(e);
            }
            fileMappings.add(fileMap);
        });
        writeFile(new FileMap(typeName, dirName), dirName, Templates.index("Index of " + dirName, fileMappings).render());
    }

    void writeFile(FileMap fileMap, String type, String content) throws IOException {
        Path targetDir = Paths.get(output.toString(), type);
        Path target = targetDir.resolve(fileMap.fileName);

        //Files.write(target, content.getBytes(StandardCharsets.UTF_8));
        System.out.println("✅ Generated " + target);
    }

    static class FileMap {
        final String title;
        final String fileName;

        FileMap(String title, String fileName) {
            this.title = title;
            this.fileName = fileName + ".md";
        }
    }

    static class WrappedIOException extends RuntimeException {
        WrappedIOException(IOException cause) {
            super(cause);
        }
    }
}
