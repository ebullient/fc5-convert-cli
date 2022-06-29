package dev.ebullient.fc5.pojo;

import java.util.List;
import java.util.ListIterator;

public interface QuteSource {
    String getName();

    default List<String> breathe(List<String> text) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }
        if (text.size() >= 1) {
            ListIterator<String> li = text.listIterator(0);
            while (li.hasNext()) {
                String next = li.next();
                int ni = li.nextIndex();
                if (next.isBlank() || ni == text.size() || text.get(ni).isBlank()) {
                    continue;
                }
                if ((next.contains("|") && text.get(ni).contains("|")) // table
                        || (next.startsWith("|") && text.get(ni).startsWith("^")) // block-id
                        || (next.charAt(0) == text.get(ni).charAt(0) && next.charAt(1) == text.get(ni).charAt(1))) { // list items
                    continue;
                }
                li.add("");
            }
        }
        return text;
    }
}
