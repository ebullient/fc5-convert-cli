package dev.ebullient.fc5.fc5data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class FeatTypeTest extends ParsingTestBase {

    @Test
    public void testActorFeat() throws Exception {
        CompendiumType compendium = doParseInputResource("featActor.xml");

        Assertions.assertNotNull(compendium);
        Assertions.assertFalse(compendium.feats.isEmpty(),
                "Feats should not be empty, found " + compendium);

        FeatType feat = compendium.feats.get(0);
        Assertions.assertAll(
                () -> assertEquals("Actor", feat.getName()),
                () -> assertEquals("", feat.prerequisite),
                () -> assertTrue(textContains(feat.text, "Skilled at mimicry")),
                () -> assertEquals(1, feat.modifier.size(), "Should have found a modifier"));
    }

    @Test
    public void testDefensiveDuelistFeat() throws Exception {
        CompendiumType compendium = doParseInputResource("featDefensiveDuelist.xml");

        Assertions.assertNotNull(compendium);
        Assertions.assertFalse(compendium.feats.isEmpty(),
                "Feats should not be empty, found " + compendium);

        FeatType feat = compendium.feats.get(0);
        Assertions.assertAll(
                () -> assertEquals("Defensive Duelist", feat.getName()),
                () -> assertEquals("Dexterity 13 or higher", feat.prerequisite),
                () -> assertTrue(textContains(feat.text, "When you are wielding")),
                () -> assertEquals(0, feat.modifier.size(), "Should not have found a modifier"));

        String content = templates.renderFeat(feat);
        Assertions.assertAll(
                () -> assertContains(content, "# Defensive Duelist"),
                () -> assertContains(content, "When you are wielding a finesse weapo"),
                () -> assertContains(content, "aliases: [\"Defensive Duelist\"]"));
    }
}
