package dev.ebullient.fc5;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IFactory;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.ScopeType;
import picocli.CommandLine.Spec;

@QuarkusMain
@Command(name = "fc5-convert", mixinStandardHelpOptions = true, subcommands = {
        Validate.class, Transform.class, Obsidian.class, Import5eTools.class, Completion.class })
public class Fc5ConvertCli implements Callable<Integer>, QuarkusApplication {

    List<Path> input;

    @Inject
    IFactory factory;

    @Spec
    private CommandSpec spec;

    @Option(names = { "--verbose", "-v" }, description = "verbose output", scope = ScopeType.INHERIT)
    void setVerbose(boolean verbose) {
        Log.setVerbose(verbose);
    }

    @Parameters(description = "Source file(s)", scope = ScopeType.INHERIT)
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

    private int executionStrategy(ParseResult parseResult) {
        // Initialize log streams, carry on with the rest of the show
        Log.prepareStreams(parseResult.commandSpec());
        return new CommandLine.RunLast().execute(parseResult);
    }

    @Override
    public int run(String... args) throws Exception {
        return new CommandLine(this, factory)
                .setExecutionStrategy(this::executionStrategy)
                .execute(args);
    }
}
