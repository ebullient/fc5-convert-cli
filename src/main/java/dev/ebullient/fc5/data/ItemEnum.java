package dev.ebullient.fc5.data;

import io.quarkus.qute.TemplateData;

/**
 * <p>
 * Java class for itemEnum.
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

    LA("light armor"),
    MA("medium armor"),
    HA("heavy armor"),
    S("shield"),
    M("melee weapon"),
    R("ranged weapon"),
    A("ammunition"),
    RD("rod"),
    ST("staff"),
    WD("wand"),
    RG("ring"),
    P("potion"),
    SC("scroll"),
    W("wonderous item"),
    G("adventuring gear"),
    $("coins and gemstones"),
    UNKNOWN("unknown");

    private final String longName;

    ItemEnum(String v) {
        longName = v;
    }

    public boolean isWeapon() {
        return this == ItemEnum.R || this == ItemEnum.M || this == ItemEnum.A;
    }

    public boolean isArmor() {
        return this == ItemEnum.LA || this == ItemEnum.MA || this == ItemEnum.HA || this == ItemEnum.S;
    }

    public boolean isGear() {
        return this == ItemEnum.G;
    }

    public boolean isMoney() {
        return this == ItemEnum.$;
    }

    public boolean isWondrousItem() {
        return this == ItemEnum.RD
                || this == ItemEnum.ST
                || this == ItemEnum.WD
                || this == ItemEnum.RG
                || this == ItemEnum.P
                || this == ItemEnum.SC
                || this == ItemEnum.W;
    }

    public static ItemEnum fromValue(String v) {
        return valueOf(v);
    }

    public String getCategoryTag() {
        if (isArmor()) {
            return "armor/" + MarkdownWriter.slugifier().slugify(longName.replace("armor", ""));
        } else if (isWeapon()) {
            return "weapon/" + MarkdownWriter.slugifier().slugify(longName.replace("weapon", ""));
        } else if (isGear()) {
            return "gear";
        } else if (isWondrousItem()) {
            return "wondrous" + (this == W ? "" : "/" + MarkdownWriter.slugifier().slugify(longName));
        } else if (isMoney()) {
            return "$";
        }
        return "unknown";
    }
}
