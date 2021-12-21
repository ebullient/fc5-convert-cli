package dev.ebullient.fc5;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import javax.xml.transform.stream.StreamSource;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

@Command(name = "csv", mixinStandardHelpOptions = true, header = "Export Items to CSV file", sortOptions = false)
public class Csv implements Callable<Integer> {

    Path output;

    @Spec
    private CommandSpec spec;

    @ParentCommand
    Fc5ConvertCli parent;

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
        Log.outPrintln("💡 Exporting Items to CSV");
        try (InputStream resource = ClassLoader.getSystemResourceAsStream("itemExport.xslt")) {
            final StreamSource xsltSource = new StreamSource(resource);

            Transform transform = new Transform();
            transform.output = output;
            transform.parent = parent;

            boolean allOk = transform.run(xsltSource, path -> {
                String filename = path.getFileName().toString();
                int pos = filename.lastIndexOf(".");
                filename = filename.substring(0, pos) + "-item.csv";
                return output.resolve(filename);
            });

            return allOk ? CommandLine.ExitCode.OK : CommandLine.ExitCode.SOFTWARE;
        }
    }
}
