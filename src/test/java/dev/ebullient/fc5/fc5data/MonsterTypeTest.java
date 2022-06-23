package dev.ebullient.fc5.fc5data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import dev.ebullient.fc5.pojo.SizeEnum;
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
                () -> assertEquals("Ankheg", monster.getName()),
                () -> assertEquals(SizeEnum.LARGE.value(), monster.getSize()),
                () -> assertEquals("monstrosity", monster.getType()),
                () -> assertEquals("Unaligned", monster.getAlignment()),
                () -> assertEquals("14", monster.getAc()),
                () -> assertEquals("39", monster.getHp()),
                () -> assertEquals("walk 30 ft., burrow 10 ft.", monster.getSpeed()),

                () -> assertEquals(17, monster.getScores().getStr()),
                () -> assertEquals(11, monster.getScores().getDex()),
                () -> assertEquals(13, monster.getScores().getCon()),
                () -> assertEquals(1, monster.getScores().getInt()),
                () -> assertEquals(13, monster.getScores().getWis()),
                () -> assertEquals(6, monster.getScores().getCha()),
                () -> assertEquals("", monster.getSaveString()),
                () -> assertEquals(new ArrayList<>(Arrays.asList("Intimidation +2", "History +1")), monster.getSkill()),
                () -> assertEquals(11, monster.getPassive()),
                () -> assertEquals("", monster.getLanguages()),

                () -> assertEquals("2", monster.getCr()),
                () -> assertEquals("", monster.getResist()),
                () -> assertEquals("", monster.getImmune()),
                () -> assertEquals("", monster.getVulnerable()),
                () -> assertEquals("", monster.getConditionImmune()),
                () -> assertEquals("darkvision 60 ft., tremorsense 60 ft.", monster.getSenses()),

                () -> assertEquals(Collections.emptyList(), monster.getTrait()),
                () -> assertEquals(2, monster.getAction().size()),
                () -> assertEquals(Collections.emptyList(), monster.getLegendary()),
                () -> assertEquals(Collections.emptyList(), monster.getReaction()),

                () -> assertTrue(monster.getDescription().startsWith("An ankheg resembles ")),
                () -> assertEquals("grassland, forest", monster.getEnvironment()));

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

    @Test
    public void testAboleth() throws Exception {
        CompendiumType compendium = doParseInputResource("monsterAboleth.xml");

        Assertions.assertNotNull(compendium);
        Assertions.assertFalse(compendium.monsters.isEmpty(),
                "Monsters should not be empty, found " + compendium);

        MonsterType monster = compendium.monsters.get(0);
        Assertions.assertAll(
                () -> assertEquals("Aboleth", monster.getName()),
                () -> assertEquals(SizeEnum.LARGE.value(), monster.getSize()),
                () -> assertEquals(2, monster.getSkill().size()));
    }
}
