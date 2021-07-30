package dev.ebullient.fc5.data;

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.quarkus.qute.TemplateData;

@TemplateData
public class Text {
    public static final Text NONE = new Text(Collections.emptyList());
    final Pattern linebreaks = Pattern.compile("[\n\r\u0085\u2028\u2029]");

    final List<String> content;

    public Text(List<String> text) {
        this.content = text.stream()
                .map(x -> convertToMarkdown(x))
                .flatMap(x -> x.stream())
                .collect(Collectors.toList());
    }

    private List<String> convertToMarkdown(String textContent) {
        try {
            String[] text = linebreaks.matcher(textContent).replaceAll("\n").split("\n");

            List<String> lines = Stream.of(text)
                    .map(x -> x.replaceAll("•", "-"))
                    .map(x -> x.trim())
                    .collect(Collectors.toList());

            ListIterator<String> i = lines.listIterator();

            boolean listMode = false;
            boolean tableMode = false;

            while (i.hasNext()) {
                String line = i.next().replaceAll("Skill Proficiencies:", "**Skill Proficiencies:**")
                        .replaceAll("Languages:", "**Languages:**").replaceAll("Equipment:", "**Equipment:**")
                        .trim();
                i.set(line);

                listMode = handleList(i, line, listMode);
                tableMode = handleTable(i, line, tableMode);

                if (line.startsWith("##") || line.startsWith("Source:")) {
                    insertBlankLineAbove(i, line);
                }

                if (!listMode && !tableMode) {
                    insertBlankLine(i, line);
                }
            }

            return lines;
        } catch (Exception e) {
            System.err.println("Unable to convert entry to markdown: " + e.getMessage());
            System.err.println("Source text: ");
            System.err.println(e);
            System.err.println("Details: ");
            e.printStackTrace();
            throw e;
        }
    }

    boolean handleList(ListIterator<String> i, String line, boolean listMode) {
        boolean newListMode = line.startsWith("-");
        if (listMode != newListMode) {
            insertBlankLineAbove(i, line);
        }
        return newListMode;
    }

    boolean handleTable(ListIterator<String> i, String line, boolean tableMode) {
        boolean newTableMode = line.indexOf('|') >= 0;
        if (newTableMode) {
            if (tableMode != newTableMode) {
                insertBlankLineAbove(i, line);
            }
            line = "|" + line + "|";
            i.set(line);
            if (tableMode != newTableMode) {
                insertLine(i, String.join("|", line.replaceAll("[^|]", "-")));
            }
        } else if (tableMode && tableMode != newTableMode) {
            insertBlankLine(i, line);
        }
        return newTableMode;
    }

    void insertLine(ListIterator<String> i, String newLine) {
        i.add(newLine);
        assert (i.previous().equals(newLine));
        assert (i.next().equals(newLine));
    }

    void insertBlankLine(ListIterator<String> i, String line) {
        if (!line.isBlank()) {
            insertLine(i, "");
        }
    }

    void insertBlankLineAbove(ListIterator<String> i, String line) {
        if (i.previousIndex() > 1) {
            assert (i.previous().equals(line)); // this line
            String previous = i.previous();
            assert (i.next().equals(previous)); // move cursor
            if (!previous.trim().isBlank()) {
                i.add("");
            }
            assert (i.next().equals(line)); // back to current line
        }
    }

    public MatchResult matches(Pattern pattern) {
        Matcher matcher = pattern.matcher(String.join("\n", content));
        if (matcher.find()) {
            return matcher.toMatchResult();
        }
        return null;
    }

    public boolean contains(String string) {
        return content.stream().anyMatch(x -> x.contains(string));
    }
}
