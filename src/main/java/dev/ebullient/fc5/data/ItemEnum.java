package dev.ebullient.fc5.data;

import java.util.Locale;

import io.quarkus.qute.TemplateData;

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
@TemplateData
public enum ItemEnum implements ConvertedEnumType {

    LIGHT_ARMOR("light armor", "LA"),
    MEDIUM_ARMOR("medium armor", "MA"),
    HEAVY_ARMOR("heavy armor", "HA"),
    SHIELD("shield", "S"),
    MELEE_WEAPON("melee weapon", "M"),
    RANGED_WEAPON("ranged weapon", "R"),
    AMMUNITION("ammunition", "A"),
    ROD("rod", "RD"),
    STAFF("staff", "ST"),
    WAND("wand", "WD"),
    RING("ring", "RG"),
    POTION("potion", "P"),
    SCROLL("scroll", "SC"),
    WONDROUS("wondrous item", "W"),
    GEAR("adventuring gear", "G"),
    WEALTH("coins and gemstones", "$"),
    UNKNOWN("unknown", "");

    private final String longName;
    private final String xmlValue;

    private ItemEnum(String longName, String xmlValue) {
        this.longName = longName;
        this.xmlValue = xmlValue;
    }

    public String getXmlValue() {
        return xmlValue;
    }

    public String value() {
        return longName;
    }

    public static ItemEnum fromXmlValue(String v) {
        if (v == null || v.isBlank()) {
            return UNKNOWN;
        }
        for (ItemEnum i : ItemEnum.values()) {
            if (i.xmlValue.equals(v)) {
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
        return this == GEAR;
    }

    public boolean isConsumable() {
        return this == POTION || this == SCROLL;
    }

    public boolean isMoney() {
        return this == WEALTH;
    }

    public boolean canBeMagic() {
        return isWondrousItem() || isArmor() || isWeapon();
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

    public String getItemTag(String detail, Text text) {
        StringBuilder tag = new StringBuilder();
        tag.append("item");
        if (isArmor()) {
            tag.append("/armor/");
            tag.append(longName.replace(" armor", ""));
        } else if (isWeapon()) {
            tag.append("/weapon/");
            tag.append(detail.toLowerCase(Locale.ROOT).contains("martial") ? "martial/" : "simple/");
            tag.append(this == RANGED_WEAPON ? "ranged" : "melee");
        } else if (isGear()) {
            tag.append("/gear");
            if (detail.toLowerCase(Locale.ROOT).contains("poison")) {
                tag.append("/poison");
            }
        } else if (isWondrousItem()) {
            tag.append("/wondrous" + (this == WONDROUS ? "" : "/" + longName));
        } else if (isMoney()) {
            if (text.contains("gemstone")) {
                tag.append("/gem");
            } else if (text.contains("Common coins")) {
                tag.append("/coins");
            } else {
                tag.append("/other");
            }
        }
        return tag.toString();
    }

    public String updateDetails(String tmpDetail) {
        tmpDetail = tmpDetail.toLowerCase();
        StringBuilder replacement;
        switch (this) {
            case MELEE_WEAPON:
            case RANGED_WEAPON:
                replacement = new StringBuilder();
                replacement.append("weapon (");
                replacement.append(tmpDetail.contains("martial") ? "martial" : "simple");
                replacement.append(this == RANGED_WEAPON ? " ranged" : " melee");
                replacement.append(")");

                tmpDetail = tmpDetail.replaceAll("(simple|martial) weapon(, )?", "")
                        .replaceAll("(melee|ranged) weapon", replacement.toString());
                break;
            case HEAVY_ARMOR:
            case MEDIUM_ARMOR:
            case LIGHT_ARMOR:
                tmpDetail = tmpDetail.replaceAll("(heavy|medium|light) armor", "armor ($1)");
                break;
            default:
        }
        tmpDetail = tmpDetail.replaceAll("cursed item", "cursed");
        return tmpDetail;
    }
}
