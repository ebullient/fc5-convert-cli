package dev.ebullient.fc5;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.function.Function;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
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
    Fc5ConvertCli parent;

    @Option(names = "-t", description = "XSLT file")
    void setXsltFile(File xsltFile) {
        xslt = xsltFile.toPath().toAbsolutePath().normalize();
    }

    @Option(names = "-o", description = "Output directory", required = true)
    void setOutputPath(File outputDir) {
        output = outputDir.toPath().toAbsolutePath().normalize();
        if (output.toFile().exists() && output.toFile().isFile()) {
            throw new ParameterException(spec.commandLine(),
                    "Specified output path exists and is a file: " + output.toString());
        }
    }

    @Option(names = "-x", description = "Suffix")
    String suffix;

    @Override
    public Integer call() throws Exception {
        final StreamSource xsltSource;
        if (xslt == null) {
            Log.outPrintln("💡 Using default XSLT filter");
            xsltSource = XmlStreamUtils.getSourceFor("filterMerge-2.0.xslt");
        } else {
            Log.outPrintln("💡 Using XLST " + xslt.toAbsolutePath());
            xsltSource = new StreamSource(xslt.toFile());
        }

        boolean allOk = run(xsltSource, path -> {
            String filename = path.getFileName().toString();
            if (suffix != null) {
                int pos = filename.lastIndexOf(".");
                if (suffix.indexOf('.') >= 0) {
                    filename = filename.substring(0, pos) + suffix;
                } else {
                    filename = filename.substring(0, pos) + suffix + filename.substring(pos);
                }
            }
            return output.resolve(filename);
        });

        return allOk ? CommandLine.ExitCode.OK : CommandLine.ExitCode.SOFTWARE;
    }

    public boolean run(StreamSource xsltSource, Function<Path, Path> targetPath)
            throws ParserConfigurationException, TransformerConfigurationException {
        DocumentBuilder db = XmlStreamUtils.getDocumentBuilder();

        TransformerFactory transformerFactory = new net.sf.saxon.BasicTransformerFactory();
        Transformer transformer = transformerFactory.newTransformer(xsltSource);

        Path targetDir = Paths.get(output.toString());
        targetDir.toFile().mkdirs();

        for (Path sourcePath : parent.input) {
            String systemId = sourcePath.toString();
            Log.debug("  System id: " + systemId);
            Log.outPrintf("⏱ Transforming %40s ... \n", sourcePath.getFileName());

            Path outputPath = targetPath.apply(sourcePath);
            if (sourcePath.equals(outputPath)) {
                Log.errorf("Source and target are the same file: %s", sourcePath);
                return false;
            }
            File targetFile = outputPath.toFile();

            try (InputStream is = new FileInputStream(sourcePath.toFile())) {
                Document doc = db.parse(is, systemId);
                DOMSource domSource = new DOMSource(doc, systemId);

                StreamResult result = new StreamResult(targetFile);
                transformer.transform(domSource, result);

                Log.outPrintf("✅ wrote %s\n", outputPath);
            } catch (IOException | SAXException | TransformerException e) {
                Log.errorf(e, "Exception processing %s: %s\n", sourcePath, e.getMessage());
                return false;
            }
            db.reset();
        }
        return true;
    }
}
