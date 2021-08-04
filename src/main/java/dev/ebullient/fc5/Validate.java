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
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

@Command(name = "validate", mixinStandardHelpOptions = true, header = "Validate XML files against a schema definition")
public class Validate implements Callable<Integer> {

    Path xsd;

    @Spec
    private CommandSpec spec;

    @ParentCommand
    private ConvertCli parent;

    @Option(names = "-s", description = "XSD file", required = true)
    void setXsdFile(File xsdFile) {
        xsd = xsdFile.toPath().toAbsolutePath().normalize();
    }

    @Override
    public Integer call() throws Exception {
        Log.prepareStreams(spec);
        boolean allOk = true;

        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(xsd.toFile());
        Validator validator = schema.newValidator();

        if (parent.input == null || parent.input.isEmpty()) {
            throw new CommandLine.MissingParameterException(spec.commandLine(), spec.args(),
                    "Must specify an input file to validate");
        }

        for (Path source : parent.input) {
            try {
                Log.outPrintln("⏱ Validate " + source);
                validator.validate(new StreamSource(source.toFile()));
                Log.outPrintln("  ✅ OK"); // end line
            } catch (IOException | SAXException e) {
                Log.error(e, "  Exception: " + e.getMessage());
                allOk = false;
            }
        }

        return allOk ? CommandLine.ExitCode.OK : CommandLine.ExitCode.SOFTWARE;
    }
}
