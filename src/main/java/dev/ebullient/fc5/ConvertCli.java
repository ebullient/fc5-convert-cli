package dev.ebullient.fc5;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ScopeType;
import picocli.CommandLine.Spec;

@TopCommand
@Command(name = "fc5-convert", mixinStandardHelpOptions = true, subcommands = {
        Convert.class, Transform.class, Validate.class, Completion.class })
public class ConvertCli implements Callable<Integer> {

    List<Path> input;

    @Spec
    private CommandSpec spec;

    @Option(names = { "--verbose", "-v" }, description = "verbose output", scope = ScopeType.INHERIT)
    void setVerbose(boolean verbose) {
        Log.setVerbose(verbose);
    }

    @Parameters(description = "XML Source file(s)", scope = ScopeType.INHERIT)
    void setInput(List<File> inputFile) {
        input = new ArrayList<>(inputFile.size());
        for (File f : inputFile) {
            input.add(f.toPath().toAbsolutePath().normalize());
        }
    }

    @Override
    public Integer call() {
        return CommandLine.ExitCode.OK;
    }
}
