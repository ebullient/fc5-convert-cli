package dev.ebullient.fc5.fc5data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import dev.ebullient.fc5.pojo.SchoolEnum;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class SpellTypeTest extends ParsingTestBase {

    @Test
    public void testAcidSplashSpell() throws Exception {
        Fc5Compendium compendium = doParseInputResource("spellAcidSplash.xml");

        Assertions.assertNotNull(compendium);
        Assertions.assertFalse(compendium.spells.isEmpty(),
                "Spells should not be empty, found " + compendium);

        List<String> tags = Arrays.asList(
                "spell/school/conjuration",
                "spell/level/cantrip",
                "spell/class/artificer",
                "spell/class/sorcerer",
                "spell/class/wizard");

        Fc5Spell spell = compendium.spells.get(0);
        Assertions.assertAll(
                () -> assertEquals("Acid Splash", spell.getName()),
                () -> assertEquals("cantrip", spell.getLevel()),
                () -> assertEquals(tags, spell.getTags()),
                () -> assertEquals(SchoolEnum.Conjuration.value(), spell.getSchool()),
                () -> assertFalse(spell.getRitual()),
                () -> assertEquals("1 action", spell.getTime()),
                () -> assertEquals("60 feet", spell.getRange()),
                () -> assertEquals("V, S", spell.getComponents()),
                () -> assertEquals("Instantaneous", spell.getDuration()),
                () -> assertEquals("Artificer, Sorcerer, Wizard", spell.getClasses()),
                () -> assertTrue(textContains(spell.getText(), "You hurl a bubble of acid.")),
                () -> assertTrue(rollContains(spell.roll, "1d6")),
                () -> assertTrue(rollContains(spell.roll, "2d6")),
                () -> assertTrue(rollContains(spell.roll, "3d6")),
                () -> assertTrue(rollContains(spell.roll, "4d6")));

        String content = templates.renderSpell(spell);
        Assertions.assertAll(
                () -> assertContains(content, "spell/class/artificer"),
                () -> assertContains(content, "# Acid Splash"),
                () -> assertContains(content, "*cantrip, conjuration*"),
                () -> assertContains(content, "- **Casting time:** 1 action"),
                () -> assertContains(content, "- **Components:** V, S"));
    }

    @Test
    public void testAlarmSpell() throws Exception {
        Fc5Compendium compendium = doParseInputResource("spellAlarm.xml");

        Assertions.assertNotNull(compendium);
        Assertions.assertFalse(compendium.spells.isEmpty(),
                "Spells should not be empty, found " + compendium);

        List<String> tags = Arrays.asList(
                "spell/school/abjuration",
                "spell/level/1",
                "spell/class/artificer",
                "spell/class/fighter-eldritch-knight",
                "spell/class/paladin-watchers",
                "spell/class/ranger",
                "spell/class/sorcerer-clockwork-soul",
                "spell/class/wizard",
                "spell/class/wizard-ritual-caster",
                "spell/ritual");

        Fc5Spell spell = compendium.spells.get(0);
        Assertions.assertAll(
                () -> assertEquals("Alarm", spell.getName()),
                () -> assertEquals("1st-level", spell.getLevel()),
                () -> assertEquals(tags, spell.getTags()),
                () -> assertEquals(SchoolEnum.Abjuration.value(), spell.getSchool()),
                () -> assertTrue(spell.getRitual()),
                () -> assertEquals("1 minute", spell.getTime()),
                () -> assertEquals("30 feet", spell.getRange()),
                () -> assertEquals("V, S, M (a tiny bell and a piece of fine silver wire)", spell.getComponents()),
                () -> assertEquals("8 hours", spell.getDuration()),
                () -> assertEquals(
                        "Artificer, Fighter (Eldritch Knight), Paladin (Watchers), Ranger, Sorcerer (Clockwork Soul), Wizard, Wizard (Ritual Caster)",
                        spell.getClasses()),
                () -> assertTrue(textContains(spell.getText(), "You set an alarm against unwanted intrusion.")),
                () -> assertEquals(Collections.emptyList(), spell.roll));

        String content = templates.renderSpell(spell);
        Assertions.assertAll(
                () -> assertContains(content, "# Alarm"),
                () -> assertContains(content, "*1st-level, abjuration (ritual)*"),
                () -> assertContains(content, "- **Casting time:** 1 minute unless cast as a ritual"),
                () -> assertContains(content,
                        "Artificer, Fighter (Eldritch Knight), Paladin (Watchers), Ranger, Sorcerer (Clockwork Soul), Wizard, Wizard (Ritual Caster)"),
                () -> assertContains(content, "### Classes"));
    }
}
