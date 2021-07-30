package dev.ebullient.fc5.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.quarkus.qute.TemplateData;

/**
 * <p>
 * Java class for itemType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="itemType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="type" type="{}itemEnum"/>
 *         &lt;element name="magic" type="{}boolean"/>
 *         &lt;element name="detail" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="weight" type="{}double"/>
 *         &lt;element name="text" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="roll" type="{}roll"/>
 *         &lt;element name="value" type="{}double"/>
 *         &lt;element name="modifier" type="{}modifierType"/>
 *         &lt;element name="ac" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="strength" type="{}integer"/>
 *         &lt;element name="stealth" type="{}boolean"/>
 *         &lt;element name="dmg1" type="{}roll"/>
 *         &lt;element name="dmg2" type="{}roll"/>
 *         &lt;element name="dmgType" type="{}damageEnum"/>
 *         &lt;element name="property" type="{}propertyList"/>
 *         &lt;element name="range" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@TemplateData
public class ItemType implements BaseType {

    final String name;
    final ItemEnum type;
    final boolean magic;
    final String detail;
    final double weight;
    final Text text;
    final List<Roll> roll;
    final double value;
    final List<Modifier> modifier;
    final int ac;
    final int strength;
    final boolean stealth;
    final Roll dmg1;
    final Roll dmg2;
    final DamageEnum dmgType;
    final String property;
    final String range;

    public ItemType(Map<String, Object> elements) {
        name = NodeParser.getOrDefault(elements, "name", "unknown");
        type = ItemEnum.fromValue(NodeParser.getOrDefault(elements, "type", "unknown"));
        magic = NodeParser.getOrDefault(elements, "magic", false);
        detail = NodeParser.getOrDefault(elements, "detail", "");
        weight = NodeParser.getOrDefault(elements, "weight", 0d);
        text = NodeParser.getOrDefault(elements, "text", Text.NONE);
        roll = NodeParser.getOrDefault(elements, "roll", Collections.emptyList());
        value = NodeParser.getOrDefault(elements, "value", 0d);
        modifier = NodeParser.getOrDefault(elements, "modifier", Collections.emptyList());
        ac = NodeParser.getOrDefault(elements, "ac", 0);
        strength = NodeParser.getOrDefault(elements, "strength", 0);
        stealth = NodeParser.getOrDefault(elements, "stealth", false);
        dmg1 = NodeParser.getOrDefault(elements, "dmg1", Roll.NONE);
        dmg2 = NodeParser.getOrDefault(elements, "dmg2", Roll.NONE);
        dmgType = NodeParser.getOrDefault(elements, "dmgType", DamageEnum.UNKNOWN);
        property = NodeParser.getOrDefault(elements, "property", "");
        range = NodeParser.getOrDefault(elements, "range", "");
    }

    public String getName() {
        return name;
    }

    public String getTag() {
        List<String> tags = new ArrayList<>();
        tags.add("item/" + type.getCategoryTag());
        if (isMagic()) {
            tags.add("item/magic/" + magicItemType());
        }
        return String.join(", ", tags);
    }

    public String getText() {
        return String.join("\n", text.content);
    }

    public boolean isWeapon() {
        return type.isWeapon();
    }

    public boolean isArmor() {
        return type.isArmor();
    }

    public String getDetail() {
        return detail;
    }

    public String getArmorClass() {
        return "TODO armor class";
    }

    public String getDamage() {
        return "TODO damaegs";
    }

    public String getProperty() {
        return "TODO property";
    }

    public double getCost() {
        return value;
    }

    public double getWeight() {
        return weight;
    }

    private String magicItemType() {
        String tag = "";
        if (detail.toLowerCase().contains("major")) {
            tag += "major/";
        } else if (detail.toLowerCase().contains("minor")) {
            tag += "minor/";
        }

        if (detail.toLowerCase().contains("very rare")) {
            tag += "very-rare";
        } else if (detail.toLowerCase().contains("rare")) {
            tag += "rare";
        } else if (detail.toLowerCase().contains("uncommon")) {
            tag += "uncommon";
        } else if (detail.toLowerCase().contains("common")) {
            tag += "common";
        }
        return tag;
    }

    @Override
    public String toString() {
        return "ItemType [name=" + name + "]";
    }
}
