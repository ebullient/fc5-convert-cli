package dev.ebullient.fc5.pojo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.ScalarStyle;
import org.yaml.snakeyaml.Yaml;

import com.github.slugify.Slugify;

import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.Templates;
import io.quarkus.qute.TemplateData;

public class MarkdownWriter {

    private static Slugify slugify;

    public static Slugify slugifier() {
        Slugify s = slugify;
        if (s == null) {
            slugify = s = Slugify.builder()
                    .customReplacement("\"", "")
                    .customReplacement("'", "")
                    .lowerCase(true)
                    .build();
        }
        return s;
    }

    private static Yaml yaml;

    public static Yaml yaml() {
        Yaml y = yaml;
        if (y == null) {
            DumperOptions options = new DumperOptions();
            options.setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED);
            options.setPrettyFlow(true);
            y = yaml = new Yaml(options);
        }
        return y;
    }

    final Templates templates;
    final Path output;

    public MarkdownWriter(Path output, Templates templates) {
        this.output = output;
        this.templates = templates;
    }

    public <T extends QuteSource> void writeFiles(List<T> elements, String typeName) throws IOException {
        if (elements.isEmpty()) {
            return;
        }
        Set<FileMap> fileMappings = new TreeSet<>((a, b) -> {
            if (a.dirName.equals(b.dirName)) {
                return a.fileName.compareTo(b.fileName);
            }
            return a.dirName.compareTo(b.dirName);
        });

        Log.outPrintln("⏱ Writing " + typeName);
        elements.forEach(x -> {
            String dirName = typeName.toLowerCase();
            FileMap fileMap = new FileMap(x.getName(), slugifier().slugify(x.getName()), dirName);
            try {
                switch (dirName) {
                    case "backgrounds":
                        writeFile(fileMap, dirName, templates.renderBackground((QuteBackground) x));
                        break;
                    case "classes":
                        writeFile(fileMap, dirName, templates.renderClass((QuteClass) x));
                        break;
                    case "feats":
                        writeFile(fileMap, dirName, templates.renderFeat((QuteFeat) x));
                        break;
                    case "items":
                        writeFile(fileMap, dirName, templates.renderItem((QuteItem) x));
                        break;
                    case "monsters":
                        QuteMonster m = (QuteMonster) x;
                        dirName = "bestiary/" + ((QuteMonster) x).type;
                        fileMap = new FileMap(x.getName(), slugifier().slugify(x.getName()), dirName);
                        writeFile(fileMap, dirName, templates.renderMonster(m));
                        break;
                    case "races":
                        writeFile(fileMap, dirName, templates.renderRace((QuteRace) x));
                        break;
                    case "spells":
                        writeFile(fileMap, dirName, templates.renderSpell((QuteSpell) x));
                        break;
                }
            } catch (IOException e) {
                throw new WrappedIOException(e);
            }
            fileMappings.add(fileMap);
        });
        fileMappings.stream()
                .collect(Collectors.groupingBy(fm -> fm.dirName))
                .forEach((dirName, value) -> {
                    int lastIndex = dirName.lastIndexOf("/");
                    String fileName = lastIndex > 0 ? dirName.substring(lastIndex + 1) : dirName;
                    String title = lastIndex > 0 ? fileName.substring(0, 1).toUpperCase() + fileName.substring(1) : typeName;
                    try {
                        writeFile(new FileMap(title, fileName, dirName), dirName, templates.renderIndex(title, value));
                    } catch (IOException ex) {
                        throw new WrappedIOException(ex);
                    }
                });
        Log.outPrintln("  ✅ " + (fileMappings.size() + 1) + " files.");
    }

    void writeFile(FileMap fileMap, String type, String content) throws IOException {
        Path targetDir = Paths.get(output.toString(), type);
        targetDir.toFile().mkdirs();

        Path target = targetDir.resolve(fileMap.fileName);

        Files.write(target, content.getBytes(StandardCharsets.UTF_8));
        Log.debugf("      %s", target);
    }

    @TemplateData
    public static class FileMap {
        final String title;
        final String fileName;
        final String dirName;

        FileMap(String title, String fileName, String dirName) {
            this.title = title;
            this.fileName = fileName + ".md";
            this.dirName = dirName;
        }

        public String getTitle() {
            return title;
        }

        public String getFileName() {
            return fileName;
        }

        public String getDirName() {
            return dirName;
        }
    }

    // IOException -> RuntimeException .. for working w/in stream/function
    public static class WrappedIOException extends RuntimeException {
        WrappedIOException(IOException cause) {
            super(cause);
        }
    }
}
