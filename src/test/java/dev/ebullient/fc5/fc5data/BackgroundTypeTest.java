package dev.ebullient.fc5.fc5data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import dev.ebullient.fc5.pojo.QuteBackground;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class BackgroundTypeTest extends ParsingTestBase {

    @Test
    public void testSimpleBackground() throws Exception {
        Fc5Compendium compendium = doParse(String.join("",
                "<compendium>",
                "<background>",
                "<name>Entertainer</name>",
                "<proficiency>Acrobatics, Performance</proficiency>",
                "</background>",
                "</compendium>"));

        Assertions.assertNotNull(compendium);
        Assertions.assertFalse(compendium.backgrounds.isEmpty(),
                "Backgrounds should not be empty, found " + compendium);

        QuteBackground background = compendium.backgrounds.get(0);
        Assertions.assertEquals("Entertainer", background.getName());

        Assertions.assertAll(
                () -> assertNotNull(background.getProficiency()),
                () -> assertNotNull(background.getAbilitySkills().getSkillNames()),
                () -> assertEquals(Collections.emptyList(), background.getTrait()));
    }

    @Test
    public void testAcolyteBackground() throws Exception {
        Fc5Compendium compendium = doParseInputResource("backgroundAcolyte.xml");

        Assertions.assertNotNull(compendium);
        Assertions.assertFalse(compendium.backgrounds.isEmpty(),
                "Backgrounds should not be empty, found " + compendium);

        QuteBackground background = compendium.backgrounds.get(0);
        Assertions.assertEquals("Acolyte", background.getName());
        Assertions.assertNotNull(background.getProficiency());
        Assertions.assertEquals(3, background.getTrait().size());

        Assertions.assertAll(
                () -> assertNotNull(background.getAbilitySkills()),
                () -> assertNotNull(background.getAbilitySkills().getSkillNames()),
                () -> assertEquals("Description", background.getTrait().get(0).getName()),
                () -> assertEquals("Feature: Shelter of the Faithful", background.getTrait().get(1).getName()),
                () -> assertEquals("Suggested Characteristics", background.getTrait().get(2).getName()));

        String content = templates.renderBackground(background);
        Assertions.assertAll(
                () -> assertContains(content, "# Acolyte"),
                () -> assertContains(content, "**Skill Proficiencies**"),
                () -> assertContains(content, "## Description"),
                () -> assertContains(content, "|----------|------------------|"),
                () -> assertContains(content, "\n\n| dice: d8 | Personality Trait|"),
                () -> assertContains(content, "\n\n| dice: d6 | Ideal|"),
                () -> assertContains(content, "\n\n| dice: d6 | Bond|"),
                () -> assertContains(content, "\n\n| dice: d6 | Flaw|"));
    }
}
