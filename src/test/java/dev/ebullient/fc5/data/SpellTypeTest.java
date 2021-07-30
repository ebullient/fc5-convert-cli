package dev.ebullient.fc5.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SpellTypeTest extends ParsingTestBase {

    @Test
    public void testAcidSplashSpell() throws Exception {
        CompendiumType compendium = doParseInputResource("spellAcidSplash.xml");

        Assertions.assertNotNull(compendium);
        Assertions.assertFalse(compendium.spells.isEmpty(),
                "Spells should not be empty, found " + compendium);

        SpellType spell = compendium.spells.get(0);
        Assertions.assertAll(
                () -> assertEquals("Acid Splash", spell.name),
                () -> assertEquals(0, spell.level),
                () -> assertEquals(SchoolEnum.C, spell.school),
                () -> assertEquals(false, spell.ritual),
                () -> assertEquals("1 action", spell.time),
                () -> assertEquals("60 feet", spell.range),
                () -> assertEquals("V, S", spell.components),
                () -> assertEquals("Instantaneous", spell.duration),
                () -> assertEquals("Artificer, Sorcerer, Wizard", spell.classes),
                () -> assertTrue(textContains(spell.text, "You hurl a bubble of acid.")),
                () -> assertTrue(rollContains(spell.roll, "1d6")),
                () -> assertTrue(rollContains(spell.roll, "2d6")),
                () -> assertTrue(rollContains(spell.roll, "3d6")),
                () -> assertTrue(rollContains(spell.roll, "4d6")));
    }

    @Test
    public void testAlarmSpell() throws Exception {
        CompendiumType compendium = doParseInputResource("spellAlarm.xml");

        Assertions.assertNotNull(compendium);
        Assertions.assertFalse(compendium.spells.isEmpty(),
                "Spells should not be empty, found " + compendium);

        SpellType spell = compendium.spells.get(0);
        Assertions.assertAll(
                () -> assertEquals("Alarm", spell.name),
                () -> assertEquals(1, spell.level),
                () -> assertEquals(SchoolEnum.A, spell.school),
                () -> assertEquals(true, spell.ritual),
                () -> assertEquals("1 minute", spell.time),
                () -> assertEquals("30 feet", spell.range),
                () -> assertEquals("V, S, M (a tiny bell and a piece of fine silver wire)", spell.components),
                () -> assertEquals("8 hours", spell.duration),
                () -> assertEquals(
                        "Artificer, Fighter (Eldritch Knight), Paladin (Watchers), Ranger, Sorcerer (Clockwork Soul), Wizard, Wizard (Ritual Caster)",
                        spell.classes),
                () -> assertTrue(textContains(spell.text, "You set an alarm against unwanted intrusion.")),
                () -> assertEquals(Collections.emptyList(), spell.roll));
    }
}
