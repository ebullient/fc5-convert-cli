package dev.ebullient.fc5.fc5data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import dev.ebullient.fc5.pojo.SkillOrAbility;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ClassTypeTest extends ParsingTestBase {
    @Test
    public void testBarbarianClass() throws Exception {
        Fc5Compendium compendium = doParseInputResource("classBarbarian.xml");

        Assertions.assertNotNull(compendium);
        Assertions.assertFalse(compendium.classes.isEmpty(),
                "Classes should not be empty, found " + compendium);

        Fc5Class barbarian = compendium.classes.get(0);
        Assertions.assertAll(
                () -> assertEquals("Barbarian", barbarian.getName()),
                () -> assertEquals(12, barbarian.getHitDice()),
                () -> assertEquals(
                        "Strength, Constitution, Animal Handling, Athletics, Intimidation, Nature, Perception, Survival",
                        barbarian.getProficiency()),
                () -> assertEquals(SkillOrAbility.None, barbarian.spellAbility),
                () -> assertEquals(2, barbarian.getNumSkills()),
                () -> assertEquals("light, medium, shields", barbarian.getArmor()),
                () -> assertEquals("simple, martial", barbarian.getWeapons()),
                () -> assertEquals("none", barbarian.getTools()),
                () -> assertEquals("2d4x10", barbarian.wealth));

        List<Fc5Autolevel> autolevels = barbarian.autolevel;
        Assertions.assertAll(
                () -> assertEquals(52, autolevels.size()),
                () -> assertEquals(1, autolevels.get(1).getLevel()),
                () -> assertFalse(autolevels.stream().anyMatch(x -> x.features.size() == 0)));

        autolevels.stream()
                .map(x -> x.features.get(0))
                .filter(y -> y.getName().startsWith("Artillerist"))
                .forEach(y -> Assertions.assertTrue(textContains(y.text, "## "),
                        "Parse should handle interleaved names"));

        String content = templates.renderClass(barbarian);
        Assertions.assertAll(
                () -> assertContains(content, "# Barbarian"),
                () -> assertContains(content, "- **Hit Points at Higher Levels:** add 7 OR 1d12 + CON  (minimum of 1)"),
                () -> assertContains(content, "- **Saving Throws**: Strength Saving Throws, Constitution Saving Throws"),
                () -> assertContains(content, "#### Tiger (Level 14)"));
    }
}
