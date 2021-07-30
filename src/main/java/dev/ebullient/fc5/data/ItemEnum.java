package dev.ebullient.fc5.data;

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
public enum ItemEnum {

    lightArmor("light armor", "LA"),
    mediumArmor("medium armor", "MA"),
    heavyArmor("heavy armor", "HA"),
    shield("shield", "S"),
    melee("melee weapon", "M"),
    ranged("ranged weapon", "R"),
    ammunition("ammunition", "A"),
    rod("rod", "RD"),
    staff("staff", "ST"),
    wand("wand", "WD"),
    ring("ring", "RG"),
    potion("potion", "P"),
    scroll("scroll", "SC"),
    wondrous("wondrous item", "W"),
    gear("adventuring gear", "G"),
    wealth("coins and gemstones", "$"),
    unknown("unknown", "UNK");

    private final String longName;
    private final String xmlKey;

    private ItemEnum(String longName, String xmlKey) {
        this.longName = longName;
        this.xmlKey = xmlKey;
    }

    public static ItemEnum fromValue(String v) {
        if (v == null || v.isBlank()) {
            return unknown;
        }
        for (ItemEnum i : ItemEnum.values()) {
            if (i.xmlKey.equals(v)) {
                return i;
            }
        }
        return unknown;
    }

    public boolean isWeapon() {
        return this == ranged || this == melee || this == ammunition;
    }

    public boolean isArmor() {
        return this == lightArmor || this == mediumArmor || this == heavyArmor || this == shield;
    }

    public boolean isGear() {
        return this == gear;
    }

    public boolean isConsumable() {
        return this == potion || this == scroll;
    }

    public boolean isMoney() {
        return this == wealth;
    }

    public boolean canBeMagic() {
        return isWondrousItem() || isArmor() || isWeapon();
    }

    public boolean isWondrousItem() {
        return this == rod
                || this == staff
                || this == wand
                || this == ring
                || this == potion
                || this == scroll
                || this == wondrous;
    }

    public String getItemTag(String detail, Text text) {
        StringBuilder tag = new StringBuilder();
        tag.append("item");
        if (isArmor()) {
            tag.append("/armor/");
            tag.append(longName.replace(" armor", ""));
        } else if (isWeapon()) {
            tag.append("/weapon/");
            tag.append(detail.contains("martial") ? "martial/" : "simple/");
            tag.append(this == ranged ? "ranged" : "melee");
        } else if (isGear()) {
            tag.append("/gear");
            if (detail.contains("poison")) {
                tag.append("/poison");
            }
        } else if (isWondrousItem()) {
            tag.append("/wondrous" + (this == wondrous ? "" : "/" + longName));
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
            case melee:
            case ranged:
                replacement = new StringBuilder();
                replacement.append("weapon (");
                replacement.append(tmpDetail.contains("martial") ? "martial" : "simple");
                replacement.append(this == ranged ? " ranged" : " melee");
                replacement.append(")");

                tmpDetail = tmpDetail.replaceAll("(simple|martial) weapon(, )?", "")
                        .replaceAll("(melee|ranged) weapon", replacement.toString());
                break;
            case heavyArmor:
            case mediumArmor:
            case lightArmor:
                tmpDetail = tmpDetail.replaceAll("(heavy|medium|light) armor", "armor ($1)");
                break;
            default:
        }
        tmpDetail = tmpDetail.replaceAll("cursed item", "cursed");
        return tmpDetail;
    }
}
