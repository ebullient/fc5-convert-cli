package dev.ebullient.fc5.fc5data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import dev.ebullient.fc5.pojo.MdRace;
import dev.ebullient.fc5.pojo.SkillOrAbility;
import dev.ebullient.fc5.pojo.SizeEnum;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class RaceTypeTest extends ParsingTestBase {

    @Test
    public void testDragonbornRace() throws Exception {
        CompendiumType compendium = doParseInputResource("raceDragonborn.xml");

        Assertions.assertNotNull(compendium);
        Assertions.assertFalse(compendium.races.isEmpty(),
                "Races should not be empty, found " + compendium);

        MdRace race = compendium.races.get(0);
        Assertions.assertAll(
                () -> assertEquals("Dragonborn", race.getName()),
                () -> assertEquals(SizeEnum.MEDIUM, race.getSize()),
                () -> assertEquals(30, race.getSpeed()),
                () -> assertEquals("Str 2, Cha 1", race.getAbility()),
                () -> assertEquals("", race.getProficiency()),
                () -> assertEquals(SkillOrAbility.None.name(), race.getSpellAbility()),
                () -> assertEquals(8, race.getTraits().size()),
                () -> assertEquals("Description", race.getTraits().get(0).getName()),
                () -> assertEquals("Age", race.getTraits().get(1).getName()),
                () -> assertEquals("Alignment", race.getTraits().get(2).getName()),
                () -> assertEquals("Size", race.getTraits().get(3).getName()),
                () -> assertEquals("Draconic Ancestry", race.getTraits().get(4).getName()),
                () -> assertEquals("Breath Weapon", race.getTraits().get(5).getName()),
                () -> assertEquals("Damage Resistance", race.getTraits().get(6).getName()),
                () -> assertEquals("Languages", race.getTraits().get(7).getName()));

        String content = templates.renderRace(race);
        Assertions.assertAll(
                () -> assertContains(content, "# Dragonborn"),
                () -> assertContains(content, "- race/dragonborn"),
                () -> assertContains(content, "- **Ability Score Increase**: Str 2, Cha 1"),
                () -> assertContains(content, "|--------|-------------|--------------|"),
                () -> assertContains(content, "## Breath Weapon"));
    }
}
