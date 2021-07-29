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
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ScopeType;

@QuarkusMain
@Command(name = "fc5-convert", mixinStandardHelpOptions = true, subcommands = {
        Convert.class, Transform.class, Validate.class, Completion.class })
public class ConvertCli implements QuarkusApplication, Callable<Integer> {

    List<Path> input;

    @Parameters(description = "XML Source file(s)", scope = ScopeType.INHERIT)
    void setInput(List<File> inputFile) {
        input = new ArrayList<>(inputFile.size());
        for (File f : inputFile) {
            input.add(f.toPath().toAbsolutePath().normalize());
        }
    }

    @Inject
    IFactory factory;

    @Override
    public int run(String... args) throws Exception {
        return new CommandLine(this, factory).execute(args);
    }

    @Override
    public Integer call() throws Exception {
        return CommandLine.ExitCode.OK;
    }

}
