package dev.ebullient.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.platform.commons.JUnitException;

import dev.ebullient.test.QuarkusMainLauncher.LaunchPaths;
import dev.ebullient.test.QuarkusMainLauncher.LaunchResult;
import dev.ebullient.test.QuarkusMainTest.Launch;

public class QuarkusMainTestExtension implements ParameterResolver, InvocationInterceptor {
    private static final Namespace NAMESPACE = Namespace.create("io", "quarkus", "QuarkusMainTestExtension");

    class QuarkusMainLauncherImpl implements QuarkusMainLauncher {
        final ExtensionContext extensionContext;

        QuarkusMainLauncherImpl(ExtensionContext extensionContext) {
            this.extensionContext = extensionContext;
        }

        @Override
        public LaunchResult runQuarkusMain(Path startingDir, String... args) {
            QuarkusLaunchResult launchResult = new QuarkusLaunchResult();
            runQuarkusMain((QuarkusLaunchResult) launchResult, startingDir, args);
            return launchResult;
        }

        void runQuarkusMain(QuarkusLaunchResult launchResult, Path startingDir, String... args) {
            // See #readQuarkusArtifactProperties for artifactType validation
            String artifactType = readQuarkusArtifactProperties(extensionContext).getProperty("type");
            String pathStr = readQuarkusArtifactProperties(extensionContext).getProperty("path");
            Path buildOutputDirectory = determineBuildOutputDirectory(extensionContext);

            if (startingDir == null) {
                startingDir = buildOutputDirectory;
            }

            List<String> newArgs = new ArrayList<>();

            if (artifactType.contains("jar")) {
                // java -jar invocation of command
                newArgs.add("java");
                newArgs.add("-jar");
            }
            Path path = buildOutputDirectory.resolve(pathStr);
            newArgs.add(path.toAbsolutePath().toString());

            if (args != null && args.length > 0) {
                newArgs.addAll(Arrays.asList(args));
            }

            System.out.println("$ " + String.join(" ", newArgs));
            System.out.println(" -> " + startingDir);

            launchResult.outputStream = new ArrayList<>();
            launchResult.errorStream = new ArrayList<>();
            launchResult.processDirectory = startingDir;

            ProcessBuilder pb = new ProcessBuilder(newArgs)
                    .directory(startingDir.toFile())
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .redirectError(ProcessBuilder.Redirect.PIPE);

            try {
                Process p = pb.start();
                try {
                    ExecutorService io = Executors.newFixedThreadPool(2);
                    io.submit(new Streamer(p.getInputStream(), launchResult.outputStream));
                    io.submit(new Streamer(p.getErrorStream(), launchResult.errorStream));

                    p.waitFor(10, TimeUnit.MINUTES);
                    io.awaitTermination(3, TimeUnit.SECONDS);
                    launchResult.returnCode = p.exitValue();
                } catch (InterruptedException e) {
                    launchResult.returnCode = -1;
                    p.destroyForcibly();
                    Thread.currentThread().interrupt();
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {
        Launch parameters = invocationContext.getExecutable().getAnnotation(Launch.class);

        QuarkusLaunchResult launchResult = null;
        QuarkusMainLauncherImpl mainLauncher = null;

        for (Object argument : invocationContext.getArguments()) {
            if (argument instanceof LaunchResult) {
                launchResult = (QuarkusLaunchResult) argument;
            } else if (argument instanceof QuarkusMainLauncherImpl) {
                mainLauncher = (QuarkusMainLauncherImpl) argument;
            }
        }

        if (mainLauncher == null) {
            Path directory = determineBuildOutputDirectory(extensionContext);
            // They aren't asking for the launcher itself to programmatically run commands.
            // Create a launcher and run it.
            boolean checkReturnCode = parameters != null;
            if (launchResult == null) {
                launchResult = new QuarkusLaunchResult();
            }
            mainLauncher = new QuarkusMainLauncherImpl(extensionContext);
            mainLauncher.runQuarkusMain(launchResult, directory, parameters.args());

            // If a @Launch annotation was specified, validate the return code against the expected
            if (checkReturnCode) {
                Assertions.assertEquals(parameters.returnCode(), launchResult.returnCode(),
                        String.format("QuarkusMainTest %s failed due to unexpected return code. Launch result: %s\n",
                                invocationContext.getExecutable().getName(), launchResult));
            }
        }

        invocation.proceed();
    }

    @Override
    public <T> T interceptTestClassConstructor(Invocation<T> invocation,
            ReflectiveInvocationContext<Constructor<T>> invocationContext, ExtensionContext extensionContext)
            throws Throwable {

        ensureNoInjectAnnotationIsUsed(invocationContext.getClass());
        readQuarkusArtifactProperties(extensionContext);

        return invocation.proceed();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        Class<?> type = parameterContext.getParameter().getType();
        return (type == QuarkusMainLauncher.class || type == LaunchResult.class || type == LaunchPaths.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        Class<?> type = parameterContext.getParameter().getType();
        if (type == QuarkusMainLauncher.class) {
            return new QuarkusMainLauncherImpl(extensionContext);
        } else if (type == LaunchResult.class) {
            return new QuarkusLaunchResult();
        } else if (type == LaunchPaths.class) {
            return (LaunchPaths) () -> determineBuildOutputDirectory(extensionContext);
        }
        throw new IllegalArgumentException("Unknown parameter type");
    }

    class QuarkusLaunchResult implements LaunchResult {
        int returnCode = 0;
        Path processDirectory = null;
        List<String> outputStream = Collections.emptyList();
        List<String> errorStream = Collections.emptyList();

        @Override
        public Path getProcessDirectory() {
            return processDirectory;
        }

        @Override
        public List<String> getOutputStream() {
            return outputStream;
        }

        @Override
        public List<String> getErrorStream() {
            return errorStream;
        }

        @Override
        public int returnCode() {
            return returnCode;
        }

        @Override
        public String toString() {
            return "QuarkusLaunchResult [ returnCode=" + returnCode
                    + ", processDirectory=" + processDirectory
                    + "\n == OUTPUT STREAM ==\n" + outputStream.stream().collect(Collectors.joining("\n"))
                    + "\n == ERROR STREAM ==\n" + errorStream.stream().collect(Collectors.joining("\n"))
                    + "\n == ]";
        }
    }

    static void ensureNoInjectAnnotationIsUsed(Class<?> testClass) {
        Class<?> current = testClass;
        while (current.getSuperclass() != null) {
            for (Field field : current.getDeclaredFields()) {
                Inject injectAnnotation = field.getAnnotation(Inject.class);
                if (injectAnnotation != null) {
                    throw new JUnitException(
                            "@Inject is not supported by @QuarkusMainTest. Offending field is "
                                    + field.getDeclaringClass().getTypeName() + "."
                                    + field.getName());
                }
            }
            current = current.getSuperclass();
        }
    }

    static Properties readQuarkusArtifactProperties(ExtensionContext context) {
        Path buildOutputDirectory = determineBuildOutputDirectory(context);
        return (Properties) context.getStore(NAMESPACE).getOrComputeIfAbsent("quarkus-artifact.properties", k -> {
            Path artifactProperties = buildOutputDirectory.resolve("quarkus-artifact.properties");
            if (!Files.exists(artifactProperties)) {
                throw new IllegalStateException(
                        "Unable to locate the artifact metadata file. Make sure this is run after the jar or image has been packaged.\n"
                                + "For maven, the 'maven-failsafe-plugin' ensures this test is run after artifacts have been built.\n"
                                + "For gradle, make sure this test is run after the quarkusBuild gradle task.");
            }

            try {
                Properties properties = new Properties();
                properties.load(new FileInputStream(artifactProperties.toFile()));

                String artifactType = properties.getProperty("type");
                if (artifactType == null) {
                    throw new IllegalStateException("Unable to determine the type of artifact created by the Quarkus build");
                }
                if ("native".equals(artifactType) || "jar".equals(artifactType)) {
                    // All is well, we know how to run these
                } else {
                    throw new IllegalStateException("@QuarkusMainTest can't launch artifacts of type " + artifactType);
                }
                return properties;
            } catch (IOException e) {
                throw new UncheckedIOException(
                        "Unable to read artifact metadata file created that must be created by Quarkus in order to run integration tests.",
                        e);
            }
        });
    }

    private static Path determineBuildOutputDirectory(ExtensionContext context) {
        return (Path) context.getStore(NAMESPACE).getOrComputeIfAbsent("build.output.directory", k -> {
            String buildOutputDirStr = System.getProperty("build.output.directory");
            Path result = null;
            if (buildOutputDirStr != null) {
                result = Paths.get(buildOutputDirStr);
            } else {
                // we need to guess where the artifact properties file is based on the location of the test class
                Class<?> testClass = context.getRequiredTestClass();
                final CodeSource codeSource = testClass.getProtectionDomain().getCodeSource();
                if (codeSource != null) {
                    URL codeSourceLocation = codeSource.getLocation();
                    File artifactPropertiesDirectory = determineBuildOutputDirectory(codeSourceLocation);
                    if (artifactPropertiesDirectory == null) {
                        throw new IllegalStateException(
                                "Unable to determine the output of the Quarkus build. Consider setting the 'build.output.directory' system property.");
                    }
                    result = artifactPropertiesDirectory.toPath();
                }
            }
            if (result == null) {
                throw new IllegalStateException(
                        "Unable to locate the artifact metadata file created that must be created by Quarkus in order to run tests annotated with '@QuarkusMainTest'.");
            }
            if (!Files.isDirectory(result)) {
                throw new IllegalStateException(
                        "The determined Quarkus build output '" + result.toAbsolutePath().toString() + "' is not a directory");
            }
            return result;
        });
    }

    private static File determineBuildOutputDirectory(final URL url) {
        if (url == null) {
            return null;
        }
        if (url.getProtocol().equals("file") && url.getPath().endsWith("test-classes/")) {
            //we have the maven test classes dir
            return toPath(url).getParent().toFile();
        } else if (url.getProtocol().equals("file") && url.getPath().endsWith("test/")) {
            //we have the gradle test classes dir, build/classes/java/test
            return toPath(url).getParent().getParent().getParent().toFile();
        } else if (url.getProtocol().equals("file") && url.getPath().contains("/target/surefire/")) {
            //this will make mvn failsafe:integration-test work
            String path = url.getPath();
            int index = path.lastIndexOf("/target/");
            try {
                return Paths.get(new URI("file:" + (path.substring(0, index) + "/target/"))).toFile();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private static Path toPath(URL url) {
        try {
            return Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static final class Streamer implements Runnable {

        private final InputStream processStream;
        final List<String> consumer;

        private Streamer(final InputStream processStream, final List<String> consumer) {
            this.processStream = processStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            try (final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(processStream, StandardCharsets.UTF_8))) {
                String line = null;
                while ((line = reader.readLine()) != null) {
                    consumer.add(line);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
