package dev.ebullient.fc5.fc5data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import dev.ebullient.fc5.pojo.ItemEnum;
import dev.ebullient.fc5.pojo.PropertyEnum;
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
                () -> assertEquals("Jug", item.getName()),
                () -> assertEquals(ItemEnum.GEAR, item.getType()),
                () -> assertEquals("Adventuring Gear", item.getDetail()),
                () -> assertEquals(4d, item.getWeight()),
                () -> assertEquals(0.02, item.getCost()),
                () -> assertTrue(textContains(item.text, "A jug holds")));

        String content = templates.renderItem(item);
        Assertions.assertAll(
                () -> assertContains(content, "# Jug"),
                () -> assertContains(content, "Adventuring Gear"),
                () -> assertContains(content, "item/gear"),
                () -> assertContains(content, "aliases: [\"Jug\"]"));

    }

    @Test
    public void testLanceItem() throws Exception {
        CompendiumType compendium = doParseInputResource("itemLance.xml");

        Assertions.assertNotNull(compendium);
        Assertions.assertFalse(compendium.items.isEmpty(),
                "Items should not be empty, found " + compendium);

        ItemType item = compendium.items.get(0);
        Assertions.assertAll(
                () -> assertEquals("Lance", item.getName()),
                () -> assertEquals(ItemEnum.MELEE_WEAPON, item.getType()),
                () -> assertEquals("Martial Melee Weapon", item.getDetail()),
                () -> assertEquals(6d, item.getWeight()),
                () -> assertEquals(10.0, item.getCost()),
                () -> assertEquals("1d12 piercing", item.getDamage()),
                () -> assertEquals("", item.getDamage2H()),
                () -> assertContainsProperties(item.getProperties(), "R,S,M"));

        String content = templates.renderItem(item);
        Assertions.assertAll(
                () -> assertContains(content, "# Lance"),
                () -> assertContains(content, "Martial Melee Weapon"),
                () -> assertContains(content, "item/weapon/martial/melee"),
                () -> assertContains(content, "Special: You have disadvantage"),
                () -> assertContains(content, "aliases: [\"Lance\"]"));

    }

    @Test
    public void testMoreItems() throws Exception {
        CompendiumType compendium = doParseInputResource("items.xml");

        Assertions.assertNotNull(compendium);
        Assertions.assertFalse(compendium.items.isEmpty(),
                "Items should not be empty, found " + compendium);

        boolean carpet = false;
        boolean crossbow = false;
        boolean longsword = false;
        boolean poison = false;
        boolean scimitar = false;
        boolean spikedarmor = false;
        for (ItemType item : compendium.items) {
            String content = templates.renderItem(item);
            if ("Light Crossbow".equals(item.getName())) {
                crossbow = true;
                validateCrossbow(item, content);
            } else if ("Longsword of Life Stealing".equals(item.getName())) {
                longsword = true;
                validateLongsword(item, content);
            } else if ("+3 Spiked Armor".equals(item.getName())) {
                spikedarmor = true;
                assertContains(content, "item/armor/medium");
                assertContains(content, "item/property/major");
                assertContains(content, "item/property/legendary");
                assertContains(content, "*Medium Armor, Major, Legendary*");
                assertContains(content, "**Base Armor Class**: 14 + DEX (max of +2)");
                assertContains(content, "- **Bonus**: AC +3");
            } else if ("Double-Bladed Scimitar of Vengeance".equals(item.getName())) {
                scimitar = true;
                assertContains(content, "item/weapon/martial/melee");
                assertContains(content, "item/property/major");
                assertContains(content, "item/property/uncommon");
                assertContains(content, "item/property/cursed");
                assertContains(content, "*Martial Melee Weapon, Major, Uncommon, Cursed Item*");
            } else if ("Carpet of Flying, 6 ft. × 9 ft.".equals(item.getName())) {
                carpet = true;
                assertContains(content, "item/wondrous");
                assertContains(content, "item/property/major");
                assertContains(content, "*Wondrous Item, Major*");
            } else if ("Carrion Crawler Mucus".equals(item.getName())) {
                poison = true;
                assertContains(content, "item/gear");
                assertContains(content, "item/property/major");
                assertContains(content, "item/property/poison");
                assertContains(content, "*Adventuring Gear, Major, Poison*");
            }
        }

        assertTrue(carpet, "Should have found a carpet");
        assertTrue(crossbow, "Should have found a crossbow");
        assertTrue(longsword, "Should have found a longsword");
        assertTrue(poison, "Should have found a poison");
        assertTrue(scimitar, "Should have found a scimitar");
        assertTrue(spikedarmor, "Should have found a spikedarmor");
    }

    private void validateLongsword(ItemType longsword, String content) {
        Assertions.assertAll(
                () -> assertEquals("Longsword of Life Stealing", longsword.getName()),
                () -> assertEquals(ItemEnum.MELEE_WEAPON, longsword.getType()),
                () -> assertEquals("Martial Melee Weapon, Major, Rare", longsword.getDetail()),
                () -> assertEquals(3d, longsword.getWeight()),
                () -> assertTrue(textContains(longsword.text, "Source:")),
                () -> assertTrue(rollContains(longsword.roll, "3d6")),
                () -> assertEquals("1d8 slashing", longsword.getDamage()),
                () -> assertEquals("1d10 slashing", longsword.getDamage2H()),
                () -> assertContainsProperties(longsword.getProperties(), "V,M"));

        Assertions.assertAll(
                () -> assertContains(content, "# Longsword of Life Stealing"),
                () -> assertContains(content, "Martial Melee Weapon, Major, Rare"),
                () -> assertContains(content, "item/weapon/martial/melee"),
                () -> assertContains(content, "aliases: [\"Longsword of Life Stealing\"]"));
    }

    private void validateCrossbow(ItemType crossbow, String content) {
        Assertions.assertAll(
                () -> assertEquals("Light Crossbow", crossbow.getName()),
                () -> assertEquals(ItemEnum.RANGED_WEAPON, crossbow.getType()),
                () -> assertEquals("Simple Ranged Weapon", crossbow.getDetail()),
                () -> assertEquals(5d, crossbow.getWeight()),
                () -> assertTrue(textContains(crossbow.text, "Source:")),
                () -> assertEquals(25.0, crossbow.getCost()),
                //                () -> assertEquals("1d8", crossbow.dmg1.textContent),
                //                () -> assertEquals(Roll.NONE, crossbow.dmg2),
                //                () -> assertEquals(DamageEnum.PIERCING, crossbow.dmgType),
                () -> assertContainsProperties(crossbow.getProperties(), "A,LD,2H"),
                () -> assertEquals("80/320", crossbow.getRange()));

        Assertions.assertAll(
                () -> assertContains(content, "# Light Crossbow"),
                () -> assertContains(content, "Simple Ranged Weapon"),
                () -> assertContains(content, "item/weapon/simple/ranged"),
                () -> assertContains(content, "aliases: [\"Light Crossbow\"]"));
    }

    void assertContainsProperties(List<PropertyEnum> properties, String origXml) {
        for (String key : origXml.split(",")) {
            assertTrue(properties.stream().anyMatch(x -> key.equals(x.getEncodedValue())),
                    "Expected to find " + key + " in list " + properties);
        }
    }
}
