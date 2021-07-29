package dev.ebullient.fc5.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ItemTypeTest extends ParsingTestBase {
    
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
            () -> assertTrue(textContains(item.text, "A jug holds"))
        );
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
            () -> assertEquals("martial Weapon, Melee Weapon", item.detail),
            () -> assertEquals(6d, item.weight),
            () -> assertEquals(10.0, item.value),
            () -> assertEquals("1d12", item.dmg1.textContent),
            () -> assertEquals(Roll.NONE, item.dmg2),
            () -> assertEquals(DamageEnum.P, item.dmgType),
            () -> assertEquals("R,S,M", item.property)
        );
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
            () -> assertEquals("80/320", crossbow.range)
        );

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
            () -> assertEquals("V,M", longsword.property)
        );
    }
}
