package dev.ebullient.fc5;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Command(name = "validate", mixinStandardHelpOptions = true, 
    header = "Validate XML files against a schema definition")
public class Validate implements Callable<Integer> {

    Path xsd;

    @ParentCommand
    private ConvertCli parent;

    @Option(names = "-s", description = "XSD file", required = true)
    void setXsdFile(File xsdFile) {
        xsd = xsdFile.toPath().toAbsolutePath().normalize();
    }

    @Override
    public Integer call() throws Exception {
        boolean allOk = true;

        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(xsd.toFile());
        Validator validator = schema.newValidator();

        for(Path source : parent.input ) {
            try {
                System.out.printf("Validate %80s ... ", source);
                validator.validate(new StreamSource(source.toFile()));
                System.out.println("✅ ");
            } catch (IOException | SAXException e) {
                System.out.println("⛔️ ");
                System.out.println("Exception: "+e.getMessage());
                allOk = false;
            }
        }

        return allOk ? CommandLine.ExitCode.OK : CommandLine.ExitCode.SOFTWARE;
    }
}
