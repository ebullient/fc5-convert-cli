package dev.ebullient.fc5.fc5data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

public class TextTest {
    @Test
    public void testParseSingleHeaderSentence() throws Exception {
        Text text = new Text(Arrays.asList("Summoning Air Elementals."));
        assertEquals(Arrays.asList("## Summoning Air Elementals", ""), text.content);
    }

    @Test
    public void testParseHeaderSentence() throws Exception {
        Text text = new Text(Arrays.asList(
                "Lurkers in the Earth. The ankheg uses its powerful mandibles ",
                "Source: Monster Manual p. 21"));
        assertEquals(Arrays.asList("**Lurkers in the Earth.** The ankheg uses its powerful mandibles",
                "",
                "Source: Monster Manual p. 21",
                ""), text.content);
    }

    @Test
    public void testHeaderAndDashedLines() throws Exception {
        Text text = new Text(Arrays.asList(
                "A Vampire's Lair. A vampire chooses a grand yet defensible location for \n" +
                        "Player Characters as Vampires. \n" +
                        "------\n" +
                        "The game statistics of a player character transformed into a\n" +
                        "------\n" +
                        "Source: Monster Manual p. 298, Rise of Tiamat"));
        assertEquals(Arrays.asList("**A Vampire's Lair.** A vampire chooses a grand yet defensible location for",
                "",
                "## Player Characters as Vampires",
                "",
                "The game statistics of a player character transformed into a",
                "",
                "Source: Monster Manual p. 298, Rise of Tiamat",
                ""), text.content);
    }
}
