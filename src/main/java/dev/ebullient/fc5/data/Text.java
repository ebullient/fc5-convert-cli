package dev.ebullient.fc5.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.ebullient.fc5.Log;
import io.quarkus.qute.TemplateData;

@TemplateData
public class Text {
    public static final Text NONE = new Text(Collections.emptyList());
    final static Pattern SKILLS = Pattern.compile(" Skill Proficiencies:");
    final static Pattern LANGUAGES = Pattern.compile(" Languages:");
    final static Pattern EQUIPMENT = Pattern.compile(" Equipment:");
    final static Pattern TOOLS = Pattern.compile(" Tool Proficiencies:");

    final List<String> content;

    public Text(List<String> text) {
        if (text.isEmpty()) {
            this.content = Collections.emptyList();
        } else {
            this.content = new ArrayList<>();
            for (String x : text) {
                content.addAll(convertToMarkdown(x));
            }
        }
    }

    private List<String> convertToMarkdown(String textContent) {
        try {
            // Create mutable list
            List<String> result = new ArrayList<>(Arrays.asList(textContent.split("\n")));
            ListIterator<String> i = result.listIterator();

            boolean listMode = false;
            boolean tableMode = false;

            while (i.hasNext()) {
                String line = i.next().trim();
                if (!line.isBlank()) {
                    line = line.replaceAll("•", "-");
                    line = SKILLS.matcher(line).replaceAll(" **Skill Proficiencies:**");
                    line = LANGUAGES.matcher(line).replaceAll(" **Languages:**");
                    line = EQUIPMENT.matcher(line).replaceAll(" **Equipment:**");
                    line = TOOLS.matcher(line).replaceAll(" **Tool Proficiencies:**");
                    i.set(line);
                }

                // handle lines before/after tables and lists
                listMode = handleList(i, line, listMode);
                tableMode = handleTable(i, line, tableMode);

                // Add lines between paragraphs (if not already handled)
                if (!listMode && !tableMode) {
                    insertBlankLine(i, line);
                }
                
                // make sure there is a blank line in front of headings
                if (line.startsWith("#") || line.startsWith("Source: ")) {
                    insertBlankLineAbove(i, line);
                }

            }
            return result;
        } catch (Exception e) {
            Log.err().println("Unable to convert entry to markdown: " + e.getMessage());
            Log.err().println("Source text: ");
            Log.err().println(e);
            Log.err().println("Details: ");
            e.printStackTrace(Log.err());
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

    boolean handleTable(ListIterator<String> i, String line, boolean oldTableMode) {
        boolean newTableMode = line.indexOf('|') >= 0;
        if (newTableMode) {
            if (oldTableMode != newTableMode) {
                // Make sure there is a blank line above the table
                insertBlankLineAbove(i, line);
            }
            // add pipes on the outside of the row (XML only has pipes between columns)
            line = "|" + line + "|";
            i.set(line);
            if (oldTableMode != newTableMode) {
                // add a |---|----| header row
                insertLine(i, String.join("|", line.replaceAll("[^|]", "-")));
            }
        } else if (oldTableMode && oldTableMode != newTableMode) {
                // Make sure there is a blank line following the table
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
            String previous = i.previous();
            assert (previous.equals(line)); // this line
            previous = i.previous();
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
