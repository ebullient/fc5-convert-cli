package dev.ebullient.fc5.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class SpellTypeTest extends ParsingTestBase {

    @Test
    public void testAcidSplashSpell() throws Exception {
        CompendiumType compendium = doParseInputResource("spellAcidSplash.xml");

        Assertions.assertNotNull(compendium);
        Assertions.assertFalse(compendium.spells.isEmpty(),
                "Spells should not be empty, found " + compendium);

        List<String> tags = Arrays.asList(
                "spell/school/conjuration",
                "spell/class/artificer",
                "spell/class/sorcerer",
                "spell/class/wizard");

        SpellType spell = compendium.spells.get(0);
        Assertions.assertAll(
                () -> assertEquals("Acid Splash", spell.name),
                () -> assertEquals(0, spell.level),
                () -> assertEquals(tags, spell.getTags()),
                () -> assertEquals(SchoolEnum.conjuration, spell.school),
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

        String content = templates.renderSpell(spell);
        Assertions.assertAll(
                () -> assertContains(content, "spell/class/artificer"),
                () -> assertContains(content, "# Acid Splash"),
                () -> assertContains(content, "*conjuration cantrip*"),
                () -> assertContains(content, "- **Casting time:** 1 action"),
                () -> assertContains(content, "- **Components:** V, S"));
    }

    @Test
    public void testAlarmSpell() throws Exception {
        CompendiumType compendium = doParseInputResource("spellAlarm.xml");

        Assertions.assertNotNull(compendium);
        Assertions.assertFalse(compendium.spells.isEmpty(),
                "Spells should not be empty, found " + compendium);

        List<String> tags = Arrays.asList(
                "spell/school/abjuration",
                "spell/class/artificer",
                "spell/class/fighter-eldritch-knight",
                "spell/class/paladin-watchers",
                "spell/class/ranger",
                "spell/class/sorcerer-clockwork-soul",
                "spell/class/wizard",
                "spell/class/wizard-ritual-caster",
                "spell/ritual");

        SpellType spell = compendium.spells.get(0);
        Assertions.assertAll(
                () -> assertEquals("Alarm", spell.name),
                () -> assertEquals(1, spell.level),
                () -> assertEquals(tags, spell.getTags()),
                () -> assertEquals(SchoolEnum.abjuration, spell.school),
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

        String content = templates.renderSpell(spell);
        Assertions.assertAll(
                () -> assertContains(content, "# Alarm"),
                () -> assertContains(content, "*1st level abjuration (ritual)*"),
                () -> assertContains(content, "- **Casting time:** 1 minute unless cast as a ritual"),
                () -> assertContains(content,
                        "Artificer, Fighter (Eldritch Knight), Paladin (Watchers), Ranger, Sorcerer (Clockwork Soul), Wizard, Wizard (Ritual Caster)"),
                () -> assertContains(content, "### Classes"));
    }
}
