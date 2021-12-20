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

@Command(name = "validate", mixinStandardHelpOptions = true, header = "Validate XML files against a schema definition", requiredOptionMarker = '*')
public class Validate implements Callable<Integer> {

    @Spec
    private CommandSpec spec;

    @ParentCommand
    Fc5ConvertCli parent;

    @CommandLine.ArgGroup(exclusive = true)
    ValidationSource validationSource = new ValidationSource();

    static class ValidationSource {

        Path xsd;

        ValidationSource() {
        }

        ValidationSource(boolean collection) {
            this.collection = collection;
            this.compendium = !collection;
            this.xsd = null;
        }

        ValidationSource(File xsdFile) {
            this.collection = false;
            this.compendium = false;
            setXsdFile(xsdFile);
        }

        @Option(names = "-s", description = "XSD file", required = true)
        void setXsdFile(File xsdFile) {
            xsd = xsdFile.toPath().toAbsolutePath().normalize();
        }

        @Option(names = "--collection", description = "Validate a collection of compendium files (first element in the file is <collection>")
        boolean collection;

        @Option(names = "--compendium", description = "Validate a compendium file (first element is <collection>", defaultValue = "true")
        boolean compendium = true;
    }

    @Override
    public Integer call() throws Exception {
        boolean allOk = true;

        final StreamSource xsdSource;
        if (validationSource.xsd == null) {
            if (validationSource.collection) {
                Log.outPrintln("💡 Using Collection XSD file");
                xsdSource = new StreamSource(this.getClass().getResourceAsStream("/collection.xsd"));
            } else {
                Log.outPrintln("💡 Using Compendium XSD file");
                xsdSource = new StreamSource(this.getClass().getResourceAsStream("/compendium.xsd"));
            }
        } else {
            Log.outPrintln("💡 Using XSD " + validationSource.xsd.toAbsolutePath());
            xsdSource = new StreamSource(validationSource.xsd.toFile());
        }

        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(xsdSource);
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
