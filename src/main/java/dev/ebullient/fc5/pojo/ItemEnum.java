package dev.ebullient.fc5.pojo;

import java.util.List;

/**
 * <p>
 * Java class for
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * <p>
 *
 * <pre>
 * &lt;simplethis name="itemEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="LA"/>
 *     &lt;enumeration value="MA"/>
 *     &lt;enumeration value="HA"/>
 *     &lt;enumeration value="S"/>
 *     &lt;enumeration value="M"/>
 *     &lt;enumeration value="R"/>
 *     &lt;enumeration value="A"/>
 *     &lt;enumeration value="RD"/>
 *     &lt;enumeration value="ST"/>
 *     &lt;enumeration value="WD"/>
 *     &lt;enumeration value="RG"/>
 *     &lt;enumeration value="P"/>
 *     &lt;enumeration value="SC"/>
 *     &lt;enumeration value="W"/>
 *     &lt;enumeration value="G"/>
 *     &lt;enumeration value="$"/>
 *   &lt;/restriction>
 * &lt;/simplethis>
 * </pre>
 *
 */
public enum ItemEnum {

    LIGHT_ARMOR("Light Armor", "LA", ""),
    MEDIUM_ARMOR("Medium Armor", "MA", ""),
    HEAVY_ARMOR("Heavy Armor", "HA", ""),
    SHIELD("Shield", "S", ""),
    MELEE_WEAPON("Melee Weapon", "M", ""),
    EXPLOSIVE("Ranged Weapon", "EXP", "explosive"),
    RANGED_WEAPON("Ranged Weapon", "R", ""),
    AMMUNITION("Ammunition", "A", ""),
    AMMUNITION_FUTURISTIC("Ammunition", "AF", "ammunition (futuristic)"),
    ROD("Rod", "RD", ""),
    STAFF("Staff", "ST", ""),
    WAND("Wand", "WD", ""),
    RING("Ring", "RG", ""),
    POTION("Potion", "P", ""),
    SCROLL("Scroll", "SC", ""),
    ELDRITCH_MACHINE("Wondrous Item", "EM", "Eldritch Machine"),
    GENERIC_VARIANT("Wondrous Item", "GV", "Generic Variant"),
    MASTER_RUNE("Wondrous Item", "MR", "Master Rune"),
    OTHER("Wondrous Item", "OTH", "Other"),
    WONDROUS("Wondrous Item", "W", ""),
    ARTISANS_TOOLS("Adventuring Gear", "AT", "Artisan's Tools"),
    FOOD("Adventuring Gear", "FD", "Food and Drink"),
    GAMING_SET("Adventuring Gear", "GS", "Gaming Set"),
    INSTRUMENT("Adventuring Gear", "INS", "Instrument"),
    MOUNT("Adventuring Gear", "MNT", "Mount"),
    SPELLCASTING_FOCUS("Adventuring Gear", "SCF", "Spellcasting Focus"),
    TOOLS("Adventuring Gear", "T", "Tools"),
    TACK("Adventuring Gear", "TAH", "Tack and Harness"),
    TRADE_GOOD("Adventuring Gear", "TG", "Trade Good"),
    AIRSHIP("Adventuring Gear", "AIR", "Airship, Vehicle (air)"),
    SHIP("Adventuring Gear", "SHP", "Ship, Vehicle (water)"),
    VEHICLE("Adventuring Gear", "VEH", "Vehicle (land)"),
    GEAR("Adventuring Gear", "G", ""),
    WEALTH("Treasure", "$", ""),
    UNKNOWN("Unknown", "", "");

    private final String longName;
    private final String lower;
    private final String encodedValue;
    private final String additionalType;

    private ItemEnum(String longName, String encodedValue, String additionalType) {
        this.longName = longName;
        this.lower = longName.toLowerCase();
        this.encodedValue = encodedValue;
        this.additionalType = additionalType;
    }

    public String getEncodedValue() {
        if (lower.equals("adventuring gear")) {
            return "G";
        }
        if (lower.equals("ammunition")) {
            return "A";
        }
        if (lower.equals("ranged weapon")) {
            return "R";
        }
        if (lower.equals("wondrous item")) {
            return "W";
        }
        return encodedValue;
    }

    public String getSpecializedType() {
        return additionalType.isBlank() ? lower : additionalType.toLowerCase();
    }

    public String value() {
        return lower;
    }

    public static ItemEnum fromEncodedValue(String v) {
        if (v == null || v.isBlank()) {
            throw new IllegalArgumentException("Invalid/Empty item type");
        }
        for (ItemEnum i : ItemEnum.values()) {
            if (i.encodedValue.equals(v)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Invalid/Unknown item type " + v);
    }

    public boolean isWeapon() {
        return this == RANGED_WEAPON || this == MELEE_WEAPON || this == AMMUNITION;
    }

    public boolean isArmor() {
        return this == LIGHT_ARMOR || this == MEDIUM_ARMOR || this == HEAVY_ARMOR || this == SHIELD;
    }

    public boolean isGear() {
        return this.lower.equals("adventuring gear");
    }

    public boolean isConsumable() {
        return this == POTION || this == SCROLL;
    }

    public boolean isMoney() {
        return this == WEALTH;
    }

    public boolean isWondrousItem() {
        return this == ROD
                || this == STAFF
                || this == WAND
                || this == RING
                || this == POTION
                || this == SCROLL
                || this == WONDROUS;
    }

    public String getItemTag(List<PropertyEnum> properties) {
        StringBuilder tag = new StringBuilder();
        tag.append("item");
        if (isArmor()) {
            tag.append("/armor/");
            tag.append(lower.replace(" armor", ""));
        } else if (isWeapon()) {
            tag.append("/weapon/");
            tag.append(properties.contains(PropertyEnum.MARTIAL) ? "martial/" : "simple/");
            tag.append(this == RANGED_WEAPON ? "ranged" : "melee");
        } else if (isGear()) {
            tag.append("/gear");
            if (properties.contains(PropertyEnum.POISON)) {
                tag.append("/poison");
            } else if (properties.contains(PropertyEnum.CURSED)) {
                tag.append("/cursed");
            }
        } else if (isWondrousItem()) {
            tag.append("/wondrous" + (this == WONDROUS ? "" : "/" + longName));
        } else if (isMoney()) {
            tag.append("/wealth");
        }
        return tag.toString();
    }
}
