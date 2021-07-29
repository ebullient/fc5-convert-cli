package dev.ebullient.fc5.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClassTypeTest extends ParsingTestBase {
    @Test
    public void testBarbarianClass() throws Exception {
        CompendiumType compendium = doParseInputResource("classBarbarian.xml");

        Assertions.assertNotNull(compendium);
        Assertions.assertFalse(compendium.classes.isEmpty(),
            "Classes should not be empty, found " + compendium);
        
        ClassType barbarian = compendium.classes.get(0);
        Assertions.assertAll(
            () -> assertEquals("Barbarian", barbarian.name),
            () -> assertEquals(12, barbarian.hitDice),
            () -> assertEquals("Strength, Constitution, Animal Handling, Athletics, Intimidation, Nature, Perception, Survival", barbarian.proficiency.textContent),
            () -> assertEquals(AbilityEnum.NONE, barbarian.spellAbility),
            () -> assertEquals(2, barbarian.numSkills),
            () -> assertNotEquals(Autolevel.NONE, barbarian.autolevel),
            () -> assertEquals("light, medium, shields", barbarian.armor),
            () -> assertEquals("simple, martial", barbarian.weapons),
            () -> assertEquals("none", barbarian.tools),
            () -> assertEquals("2d4x10", barbarian.wealth.textContent)
        );
    }
}
