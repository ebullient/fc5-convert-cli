package dev.ebullient.test;

import java.nio.file.Path;
import java.util.List;

public interface QuarkusMainLauncher {

    LaunchResult runQuarkusMain(Path startingDir, String... args);

    public interface LaunchResult {
        Path getProcessDirectory();

        List<String> getOutputStream();

        List<String> getErrorStream();

        int returnCode();
    }

    public interface LaunchPaths {
        Path getProjectBuildPath();
    }
}
