package dev.ebullient.fc5.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class BackgroundTypeTest extends ParsingTestBase {

    @Test
    public void testSimpleBackground() throws Exception {
        CompendiumType compendium = doParse(String.join("",
                "<compendium>",
                "<background>",
                "<name>Entertainer</name>",
                "<proficiency>Acrobatics, Performance</proficiency>",
                "</background>",
                "</compendium>"));

        Assertions.assertNotNull(compendium);
        Assertions.assertFalse(compendium.backgrounds.isEmpty(),
                "Backgrounds should not be empty, found " + compendium);

        BackgroundType background = compendium.backgrounds.get(0);
        Assertions.assertEquals("Entertainer", background.name);
        Assertions.assertNotNull(background.proficiency);

        Assertions.assertAll(
                () -> assertNotNull(background.proficiency.textContent),
                () -> assertNotNull(background.proficiency.skills),
                () -> assertEquals(Collections.emptyList(), background.traits));
    }

    @Test
    public void testAcolyteBackground() throws Exception {
        CompendiumType compendium = doParseInputResource("backgroundAcolyte.xml");

        Assertions.assertNotNull(compendium);
        Assertions.assertFalse(compendium.backgrounds.isEmpty(),
                "Backgrounds should not be empty, found " + compendium);

        BackgroundType background = compendium.backgrounds.get(0);
        Assertions.assertEquals("Acolyte", background.name);
        Assertions.assertNotNull(background.proficiency);
        Assertions.assertEquals(3, background.traits.size());

        Assertions.assertAll(
                () -> assertNotNull(background.proficiency.textContent),
                () -> assertNotNull(background.proficiency.skills),
                () -> assertEquals("Description", background.traits.get(0).name),
                () -> assertEquals("Feature: Shelter of the Faithful", background.traits.get(1).name),
                () -> assertEquals("Suggested Characteristics", background.traits.get(2).name));

        String content = templates.renderBackground(background);
        System.out.println(content);
        Assertions.assertAll(
                () -> assertTrue(content.contains("# Background: Acolyte")),
                () -> assertTrue(content.contains("**Skill Proficiencies**")),
                () -> assertTrue(content.contains("## Description")),
                () -> assertTrue(content.contains("|---|------------------|")));
    }
}
