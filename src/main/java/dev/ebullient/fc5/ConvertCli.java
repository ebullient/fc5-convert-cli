package dev.ebullient.fc5;

import java.io.File;
import java.nio.file.Path;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "fc5-convert", mixinStandardHelpOptions = true)
public class ConvertCli implements Runnable {

    Path input;
    	
    @Parameters(description = "Source XML file")
    void setInput(File inputFile) {
        input = inputFile.toPath().toAbsolutePath().normalize();
    }

    @Option(names = "-o", description = "Output file")
    File output;

    @Override
    public void run() {
        System.out.printf("Input: %s\nOutput: %s\n", 
			input, 
			output == null ? "stdout" : output.getAbsolutePath());
    }

}
