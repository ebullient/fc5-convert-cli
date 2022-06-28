package dev.ebullient.fc5.fc5data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import dev.ebullient.fc5.pojo.QuteRace;
import dev.ebullient.fc5.pojo.SizeEnum;
import dev.ebullient.fc5.pojo.SkillOrAbility;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class RaceTypeTest extends ParsingTestBase {

    @Test
    public void testDragonbornRace() throws Exception {
        Fc5Compendium compendium = doParseInputResource("raceDragonborn.xml");

        Assertions.assertNotNull(compendium);
        Assertions.assertFalse(compendium.races.isEmpty(),
                "Races should not be empty, found " + compendium);

        QuteRace race = compendium.races.get(0);
        Assertions.assertAll(
                () -> assertEquals("Dragonborn", race.getName()),
                () -> assertEquals(SizeEnum.MEDIUM.value(), race.getSize()),
                () -> assertEquals(30, race.getSpeed()),
                () -> assertEquals("Str 2, Cha 1", race.getAbility()),
                () -> assertEquals("", race.getProficiency()),
                () -> assertEquals(SkillOrAbility.None.name(), race.getSpellAbility()),
                () -> assertEquals(8, race.getTrait().size()),
                () -> assertEquals("Description", race.getTrait().get(0).getName()),
                () -> assertEquals("Age", race.getTrait().get(1).getName()),
                () -> assertEquals("Alignment", race.getTrait().get(2).getName()),
                () -> assertEquals("Size", race.getTrait().get(3).getName()),
                () -> assertEquals("Draconic Ancestry", race.getTrait().get(4).getName()),
                () -> assertEquals("Breath Weapon", race.getTrait().get(5).getName()),
                () -> assertEquals("Damage Resistance", race.getTrait().get(6).getName()),
                () -> assertEquals("Languages", race.getTrait().get(7).getName()));

        String content = templates.renderRace(race);
        Assertions.assertAll(
                () -> assertContains(content, "# Dragonborn"),
                () -> assertContains(content, "- race/dragonborn"),
                () -> assertContains(content, "- **Ability Score Increase**: Str 2, Cha 1"),
                () -> assertContains(content, "|--------|-------------|--------------|"),
                () -> assertContains(content, "## Breath Weapon"));
    }
}
