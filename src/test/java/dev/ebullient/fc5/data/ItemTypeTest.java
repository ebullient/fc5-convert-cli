package dev.ebullient.fc5.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class ItemTypeTest extends ParsingTestBase {

    @Test
    public void testJugItem() throws Exception {
        CompendiumType compendium = doParseInputResource("itemJug.xml");

        Assertions.assertNotNull(compendium);
        Assertions.assertFalse(compendium.items.isEmpty(),
                "Items should not be empty, found " + compendium);

        ItemType item = compendium.items.get(0);
        Assertions.assertAll(
                () -> assertEquals("Jug", item.name),
                () -> assertEquals(ItemEnum.G, item.type),
                () -> assertEquals("Adventuring Gear", item.detail),
                () -> assertEquals(4d, item.weight),
                () -> assertEquals(0.02, item.value),
                () -> assertTrue(textContains(item.text, "A jug holds")));

        String content = templates.renderItem(item);
        System.out.println(content);
        Assertions.assertAll(
                () -> assertTrue(content.contains("# Jug")),
                () -> assertTrue(content.contains("Adventuring Gear")),
                () -> assertTrue(content.contains("item/gear")),
                () -> assertTrue(content.contains("aliases: ['Jug']")));

    }

    @Test
    public void testLanceItem() throws Exception {
        CompendiumType compendium = doParseInputResource("itemLance.xml");

        Assertions.assertNotNull(compendium);
        Assertions.assertFalse(compendium.items.isEmpty(),
                "Items should not be empty, found " + compendium);

        ItemType item = compendium.items.get(0);
        Assertions.assertAll(
                () -> assertEquals("Lance", item.name),
                () -> assertEquals(ItemEnum.M, item.type),
                () -> assertEquals(true, item.magic),
                () -> assertEquals("martial Weapon, Melee Weapon", item.detail),
                () -> assertEquals(6d, item.weight),
                () -> assertEquals(10.0, item.value),
                () -> assertEquals("1d12", item.dmg1.textContent),
                () -> assertEquals(Roll.NONE, item.dmg2),
                () -> assertEquals(DamageEnum.P, item.dmgType),
                () -> assertEquals("R,S,M", item.property));

        String content = templates.renderItem(item);
        System.out.println(content);
        Assertions.assertAll(
                () -> assertTrue(content.contains("# Lance")),
                () -> assertTrue(content.contains("martial Weapon, Melee Weapon")),
                () -> assertTrue(content.contains("item/weapon/melee")),
                () -> assertTrue(content.contains("Special: You have disadvantage")),
                () -> assertTrue(content.contains("aliases: ['Lance']")));

    }

    @Test
    public void testMoreItems() throws Exception {
        CompendiumType compendium = doParseInputResource("items.xml");

        Assertions.assertNotNull(compendium);
        Assertions.assertFalse(compendium.items.isEmpty(),
                "Items should not be empty, found " + compendium);

        ItemType crossbow = compendium.items.get(0);
        Assertions.assertAll(
                () -> assertEquals("Light Crossbow", crossbow.name),
                () -> assertEquals(ItemEnum.R, crossbow.type),
                () -> assertEquals("simple Weapon, Ranged Weapon", crossbow.detail),
                () -> assertEquals(5d, crossbow.weight),
                () -> assertTrue(textContains(crossbow.text, "Source:")),
                () -> assertEquals(25.0, crossbow.value),
                () -> assertEquals("1d8", crossbow.dmg1.textContent),
                () -> assertEquals(Roll.NONE, crossbow.dmg2),
                () -> assertEquals(DamageEnum.P, crossbow.dmgType),
                () -> assertEquals("A,LD,2H", crossbow.property),
                () -> assertEquals("80/320", crossbow.range));

        String content = templates.renderItem(crossbow);
        System.out.println(content);
        Assertions.assertAll(
                () -> assertTrue(content.contains("# Light Crossbow")),
                () -> assertTrue(content.contains("simple Weapon, Ranged Weapon")),
                () -> assertTrue(content.contains("item/weapon/ranged")),
                () -> assertTrue(content.contains("aliases: ['Light Crossbow']")));

        ItemType longsword = compendium.items.get(1);
        Assertions.assertAll(
                () -> assertEquals("Longsword of Life Stealing", longsword.name),
                () -> assertEquals(ItemEnum.M, longsword.type),
                () -> assertEquals("major, martial Weapon, Melee Weapon", longsword.detail),
                () -> assertEquals(3d, longsword.weight),
                () -> assertTrue(textContains(longsword.text, "Source:")),
                () -> assertTrue(rollContains(longsword.roll, "3d6")),
                () -> assertEquals("1d8", longsword.dmg1.textContent),
                () -> assertEquals("1d10", longsword.dmg2.textContent),
                () -> assertEquals(DamageEnum.S, longsword.dmgType),
                () -> assertEquals("V,M", longsword.property));

        String content2 = templates.renderItem(longsword);
        System.out.println(content2);
        Assertions.assertAll(
                () -> assertTrue(content2.contains("# Longsword of Life Stealing")),
                () -> assertTrue(content2.contains("major, martial Weapon, Melee Weapon")),
                () -> assertTrue(content2.contains("item/weapon/melee")),
                () -> assertTrue(content2.contains("aliases: ['Longsword of Life Stealing']")));

    }
}
