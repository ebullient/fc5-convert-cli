package dev.ebullient.fc5.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class MonsterTypeTest extends ParsingTestBase {

    @Test
    public void testAnkheg() throws Exception {
        CompendiumType compendium = doParseInputResource("monsterAnkheg.xml");

        Assertions.assertNotNull(compendium);
        Assertions.assertFalse(compendium.monsters.isEmpty(),
                "Monsters should not be empty, found " + compendium);

        MonsterType monster = compendium.monsters.get(0);
        Assertions.assertAll(
                () -> assertEquals("Ankheg", monster.name),
                () -> assertEquals(SizeEnum.LARGE, monster.size),
                () -> assertEquals("monstrosity", monster.type),
                () -> assertEquals("Unaligned", monster.alignment),
                () -> assertEquals("14 (natural armor, 11 while prone)", monster.ac),
                () -> assertEquals("39 (6d10+6)", monster.hp),
                () -> assertEquals("walk 30 ft., burrow 10 ft.", monster.speed),

                () -> assertEquals(17, monster.scores.strength),
                () -> assertEquals(11, monster.scores.dexterity),
                () -> assertEquals(13, monster.scores.constitution),
                () -> assertEquals(1, monster.scores.intelligence),
                () -> assertEquals(13, monster.scores.wisdom),
                () -> assertEquals(6, monster.scores.charisma),
                () -> assertEquals(Collections.emptyList(), monster.save),
                () -> assertEquals(new ArrayList<>(Arrays.asList("Intimidation +2", "History +1")), monster.skill),
                () -> assertEquals(11, monster.passive),
                () -> assertEquals("", monster.languages),

                () -> assertEquals("2", monster.cr),
                () -> assertEquals("", monster.resist),
                () -> assertEquals("", monster.immune),
                () -> assertEquals("", monster.vulnerable),
                () -> assertEquals("", monster.conditionImmune),
                () -> assertEquals("darkvision 60 ft., tremorsense 60 ft.", monster.senses),

                () -> assertEquals(Collections.emptyList(), monster.trait),
                () -> assertEquals(2, monster.action.size()),
                () -> assertEquals(Collections.emptyList(), monster.legendary),
                () -> assertEquals(Collections.emptyList(), monster.reaction),

                () -> assertTrue(monster.getDescription().startsWith("An ankheg resembles ")),
                () -> assertEquals("grassland, forest", monster.environment));

        String content = templates.renderMonster(monster);
        Assertions.assertAll(
                () -> assertContains(content, "title: Ankheg"),
                () -> assertContains(content, "*Large monstrosity, Unaligned*"),
                () -> assertContains(content, "|17 (+3)|11 (+0)|13 (+1)|1 (-5)|13 (+1)|6 (-2)|"),
                () -> assertContains(content, "***Acid Spray (Recharge 6).*** The ankheg spits"),
                () -> assertContains(content, "aliases: [\"Ankheg\"]"),
                () -> assertContains(content, "**Lurkers in the Earth.**"),
                () -> assertContains(content, "**Bane of Field and Forest.**"),
                () -> assertContains(content, "**Earthen Tunnels.**"),
                () -> assertContains(content, "**Elemental Spirit in Material Form.**"));
    }
}
