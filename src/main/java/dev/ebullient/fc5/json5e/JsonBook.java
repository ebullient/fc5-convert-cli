package dev.ebullient.fc5.json5e;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class JsonBook implements JsonBase {

    public JsonBook() {

    }

    @Override
    public CompendiumSources getSources() {
        return null;
    }

    @Override
    public JsonIndex getIndex() {
        return null;
    }

    @Override
    public boolean isMarkdown() {
        return true;
    }

    public void processSections(Path inputPath, Path target, JsonNode jsonNode, JsonIndex index) throws IOException {
        String filename = inputPath.getFileName().toString();
        try (PrintWriter writer = new PrintWriter(new FileWriter(target.toFile()))) {
            writer.println("---");
            writer.println("aliases: []");
            writer.println("---");
            writer.printf("# %s%n", filename.replace(".md", ""));

            for (Iterator<JsonNode> i = jsonNode.elements(); i.hasNext();) {
                JsonNode element = i.next();
                String name = element.get("name").asText();
                writer.println();
                writer.println("## " + name);
                writeEntries(element.withArray("entries"), writer, "### ");
            }
        }
    }

    private void writeEntries(ArrayNode jsonNode, PrintWriter writer, String heading) {
        jsonNode.forEach(x -> {
            if (x.isTextual()) {
                writer.println();
                writer.println(replaceAttributes(x.asText()));
            } else if (x.isObject()) {
                String type = x.get("type").asText();
                switch (type) {
                    case "section": {
                        writer.println();
                        writer.println(heading + replaceAttributes(x.get("name").asText()));
                        writeEntries(x.withArray("entries"), writer, "#" + heading);
                        break;
                    }
                    case "entries": {
                        writer.println();
                        if (x.has("name")) {
                            writer.println(heading + replaceAttributes(x.get("name").asText()));
                        }
                        writeEntries(x.withArray("entries"), writer, "#" + heading);
                        break;
                    }
                    case "list": {
                        if (x.has("name")) {
                            writer.println();
                            writer.println("#" + heading + replaceAttributes(x.get("name").asText()));
                        }
                        writer.println();
                        x.withArray("items").forEach(i -> {
                            if (i.isTextual()) {
                                writer.println("- " + i.asText());
                            } else if (i.isObject() && i.has("name")) {
                                writer.printf("- **%s**. %s%n",
                                        replaceAttributes(i.get("name").asText()),
                                        replaceAttributes(i.get("entry").asText()));
                            }
                        });
                        break;
                    }
                    case "table": {
                        if (x.has("caption")) {
                            writer.println();
                            writer.println("#" + heading + replaceAttributes(x.get("caption").asText()));
                        }

                        StringBuilder tableHeader = new StringBuilder();
                        x.withArray("colLabels").forEach(c -> {
                            tableHeader.append("| ")
                                    .append(c.asText())
                                    .append(" ");
                        });
                        tableHeader.append("|");

                        writer.println();
                        writer.println(tableHeader);
                        writer.println(tableHeader.toString().replaceAll("[^|]", "-"));

                        x.withArray("rows").forEach(r -> {
                            r.forEach(c -> {
                                writer.append("| ")
                                        .append(replaceAttributes(c.asText()))
                                        .append(" ");
                            });
                            writer.println("|");
                        });

                        break;
                    }
                    default:
                        break;
                }
            }
        });
    }

}
