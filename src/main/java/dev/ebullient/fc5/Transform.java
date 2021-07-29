package dev.ebullient.fc5;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

@Command(name = "transform", mixinStandardHelpOptions = true, header = "Transform XML files", requiredOptionMarker = '*')
public class Transform implements Callable<Integer> {

    Path xslt;
    Path output;

    @Spec
    private CommandSpec spec;

    @ParentCommand
    private ConvertCli parent;

    @Option(names = "-t", description = "XSLT file")
    void setXsltFile(File xsltFile) {
        xslt = xsltFile.toPath().toAbsolutePath().normalize();
    }

    @Option(names = "-o", description = "Output directory", required = true)
    void setOutputPath(File outputDir) {
        output = outputDir.toPath().toAbsolutePath().normalize();
        if (!output.toFile().exists()) {
            throw new ParameterException(spec.commandLine(), "Specified output path does not exist: " + output.toString());
        }
        if (!output.toFile().isDirectory()) {
            throw new ParameterException(spec.commandLine(), "Specified output path is not a directory: " + output.toString());
        }
    }

    @Option(names = "-x", description = "Suffix")
    String suffix;

    @Override
    public Integer call() throws Exception {
        boolean allOk = true;

        final StreamSource xsltSource;
        if (xslt == null) {
            System.out.println("💡 Using default XSLT filter");
            xsltSource = new StreamSource(this.getClass().getResourceAsStream("/filterMerge-2.0.xslt"));
        } else {
            System.out.println("💡 Using XLST " + xslt.toAbsolutePath());
            xsltSource = new StreamSource(xslt.toFile());
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();

        TransformerFactory transformerFactory = new net.sf.saxon.BasicTransformerFactory();
        Transformer transformer = transformerFactory.newTransformer(xsltSource);

        for (Path sourcePath : parent.input) {
            String systemId = sourcePath.toString();
            System.out.printf("Transform %40s ... ", sourcePath.getFileName());

            String filename = sourcePath.getFileName().toString();
            if (suffix != null) {
                int pos = filename.lastIndexOf(".");
                filename = filename.substring(0, pos) + suffix + filename.substring(pos);
            }
            File targetFile = output.resolve(filename).toFile();

            try (InputStream is = new FileInputStream(sourcePath.toFile())) {
                Document doc = db.parse(is, systemId);

                // transform xml to html via a xslt file
                try (FileOutputStream target = new FileOutputStream(targetFile, false)) {
                    transformer.transform(new DOMSource(doc, systemId), new StreamResult(target));
                }

                System.out.printf("✅ wrote %s\n", targetFile.getAbsolutePath());
            } catch (IOException | SAXException | TransformerException e) {
                System.out.println("⛔️ ");
                System.out.println("Exception: " + e.getMessage());
                allOk = false;
            }
            db.reset();
        }

        return allOk ? CommandLine.ExitCode.OK : CommandLine.ExitCode.SOFTWARE;
    }
}
