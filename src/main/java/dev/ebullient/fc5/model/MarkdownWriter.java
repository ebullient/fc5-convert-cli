package dev.ebullient.fc5.model;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.github.slugify.Slugify;

import dev.ebullient.fc5.Templates;
import io.quarkus.qute.TemplateInstance;

public class MarkdownWriter {

    private static Slugify slugify;

    public static Slugify slugifier() {
        Slugify s = slugify;
        if (s == null) {
            s = new Slugify();
            s.withLowerCase(true);
            slugify = s;
        }
        return s;
    }

    final Templates tpl;
    final Path output;

    public MarkdownWriter(Path output, Templates tpl) {
        this.output = output;
        this.tpl = tpl;
    }

    public <T extends BaseType> void writeFiles(List<T> elements, String typeName) throws IOException {
        List<FileMap> fileMappings = new ArrayList<>();
        String dirName = typeName.toLowerCase();

        elements.forEach(x -> {
            FileMap fileMap = new FileMap(x.getName(), slugifier().slugify(x.getName()));
            try {
                switch (dirName) {
                    case "backgrounds":
                        writeFile(fileMap, dirName, tpl.background2md.instance());
                        break;
                    case "classes":
                        writeFile(fileMap, dirName, tpl.class2md.instance());
                        break;
                    case "feats":
                        writeFile(fileMap, dirName, tpl.feat2md.instance());
                        break;
                    case "items":
                        writeFile(fileMap, dirName, tpl.item2md.instance());
                        break;
                    case "monsters":
                        writeFile(fileMap, dirName, tpl.monster2md.instance());
                        break;
                    case "races":
                        writeFile(fileMap, dirName, tpl.race2md.instance());
                        break;
                    case "spells":
                        writeFile(fileMap, dirName, tpl.spell2md.instance());
                        break;
                }
            } catch (IOException e) {
                throw new WrappedIOException(e);
            }
            fileMappings.add(fileMap);
        });
        writeFile(new FileMap(typeName, dirName), dirName, tpl.index.instance());
    }

    void writeFile(FileMap fileMap, String type, TemplateInstance templateInstance) throws IOException {
        Path targetDir = Paths.get(output.toString(), type);
        Path target = targetDir.resolve(fileMap.fileName);

        //Files.write(target, content.getBytes(StandardCharsets.UTF_8));
        System.out.println("✅ Generated " + target);
    }

    public static class FileMap {
        final String title;
        final String fileName;

        FileMap(String title, String fileName) {
            this.title = title;
            this.fileName = fileName + ".md";
        }
    }

    public static class WrappedIOException extends RuntimeException {
        WrappedIOException(IOException cause) {
            super(cause);
        }
    }
}
