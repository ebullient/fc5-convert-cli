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

import dev.ebullient.fc5.model.CompendiumType;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

@Command(name = "convert", mixinStandardHelpOptions = true, 
    header = "Create Markdown references from XML file")
public class Convert implements Callable<Integer> {

    Path output;
    
    @Spec
    private CommandSpec spec;

    @ParentCommand
    private ConvertCli parent;

    @Option(names = "-o", description = "Output directory", required = true)
    void setOutputPath(File outputDir) {
        output = outputDir.toPath().toAbsolutePath().normalize();
        if ( !output.toFile().exists() ) {
            throw new ParameterException(spec.commandLine(), "Specified output path does not exist: " + output.toString());
        }
        if ( !output.toFile().isDirectory() ) {
            throw new ParameterException(spec.commandLine(), "Specified output path is not a directory: " + output.toString());
        }
    }

    @Override
    public Integer call() throws Exception {
        boolean allOk = true;
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
 
        for(Path sourcePath : parent.input ) {
            String systemId = sourcePath.toString();
            System.out.println("Reading " + sourcePath.getFileName());

            try (InputStream is = new FileInputStream(sourcePath.toFile())) {
                CompendiumType compendium = CompendiumType.readCompendium(db, is, systemId);
                System.out.println(compendium);
            } catch (IOException | SAXException e) {
                System.out.println("⛔️ ");
                System.out.println("Exception: "+e.getMessage());
                allOk = false;
            }
            db.reset();
        }
        return allOk ? CommandLine.ExitCode.OK : CommandLine.ExitCode.SOFTWARE;
    }
}
