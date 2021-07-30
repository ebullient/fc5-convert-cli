package dev.ebullient.fc5.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.quarkus.qute.TemplateData;

/**
 * <p>
 * Java class for itemType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
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
    static final Pattern FOUND_ON = Pattern.compile("Found On: .*Magic Item Table ([A-I])");

    final String name;
    final ItemEnum type;
    final MagicItem magicItem;
    final String detail;
    final double weight;
    final Text text;
    final List<Roll> roll;
    final double cost;
    final List<Modifier> modifier;
    final int ac;
    final int strength;
    final boolean stealth;
    final Roll dmg1;
    final Roll dmg2;
    final DamageEnum dmgType;
    final List<PropertyEnum> properties;
    final String range;

    public ItemType(Map<String, Object> elements) {
        name = NodeParser.getOrDefault(elements, "name", "unknown");

        type = ItemEnum.fromValue(NodeParser.getOrDefault(elements, "type", ""));
        properties = PropertyEnum.fromPropertyString(NodeParser.getOrDefault(elements, "property", ""));

        weight = NodeParser.getOrDefault(elements, "weight", 0d);
        cost = NodeParser.getOrDefault(elements, "value", 0d);

        text = NodeParser.getOrDefault(elements, "text", Text.NONE);

        dmg1 = NodeParser.getOrDefault(elements, "dmg1", Roll.NONE);
        dmg2 = NodeParser.getOrDefault(elements, "dmg2", Roll.NONE);
        dmgType = NodeParser.getOrDefault(elements, "dmgType", DamageEnum.unknown);
        range = NodeParser.getOrDefault(elements, "range", "");

        ac = NodeParser.getOrDefault(elements, "ac", 0);
        strength = NodeParser.getOrDefault(elements, "strength", 0);
        stealth = NodeParser.getOrDefault(elements, "stealth", false);

        modifier = NodeParser.getOrDefault(elements, "modifier", Collections.emptyList());
        roll = NodeParser.getOrDefault(elements, "roll", Collections.emptyList());

        // Figure out missing details
        String tmpDetail = NodeParser.getOrDefault(elements, "detail", "");
        magicItem = new MagicItem(name, tmpDetail, text, type, NodeParser.getOrDefault(elements, "magic", false));
        tmpDetail = magicItem.updateDetails(tmpDetail);
        detail = type.updateDetails(tmpDetail);
    }

    public String getName() {
        return name;
    }

    public List<String> getTags() {
        List<String> tags = new ArrayList<>();
        tags.add(type.getItemTag(detail, text));
        if (magicItem.hasTag()) {
            tags.add(magicItem.classificationTag());
        }
        return tags;
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

    public List<Modifier> getModifiers() {
        return modifier;
    }

    public String getDetail() {
        return detail;
    }

    public String getArmorClass() {
        StringBuilder result = new StringBuilder();
        result.append(ac);
        // - If you wear light armor, you add your Dexterity modifier to the base number from your armor type to determine your Armor Class.
        // - If you wear medium armor, you add your Dexterity modifier, to a maximum of +2, to the base number from your armor type to determine your Armor Class.
        // - Heavy armor doesn’t let you add your Dexterity modifier to your Armor Class, but it also doesn’t penalize you if your Dexterity modifier is negative.
        if (type == ItemEnum.lightArmor) {
            result.append(" + DEX");
        } else if (type == ItemEnum.mediumArmor) {
            result.append(" + DEX (max of +2)");
        }
        return result.toString();
    }

    public int getStrengthRequirement() {
        return strength;
    }

    public boolean getStealthPenalty() {
        return stealth;
    }

    public boolean isVersatile() {
        return properties.contains(PropertyEnum.versatile);
    }

    public String getDamage() {
        return dmg1 + " " + dmgType.getLongName();
    }

    public String getDamage2H() {
        return dmg2 + " " + dmgType.getLongName();
    }

    public List<PropertyEnum> getProperties() {
        return properties;
    }

    public String getPropertiesString() {
        return String.join(", ", properties.stream().map(x -> x.getMarkdownLink()).collect(Collectors.toList()));
    }

    public double getCost() {
        return cost;
    }

    public double getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return "ItemType [name=" + name + "]";
    }

    static class MagicItem {
        final Rarity rarity;
        final Classification classification;
        final boolean isMagic;

        MagicItem(String name, String detail, Text text, ItemEnum type, boolean magic) {
            Rarity tmpRarity = Rarity.find(detail);
            Classification tmpClassification = Classification.find(detail);

            boolean hasRarity = tmpRarity != Rarity.none;
            boolean hasClassification = tmpClassification != Classification.none;

            if (type.canBeMagic() && (magic || hasRarity || hasClassification)) {
                if (name.startsWith("+")) {
                    hasRarity = true;
                    if (name.startsWith("+1")) {
                        tmpRarity = Rarity.rare;
                    } else if (name.startsWith("+2")) {
                        tmpRarity = Rarity.veryRare;
                    } else if (name.startsWith("+3")) {
                        tmpRarity = Rarity.legendary;
                    }
                }

                MatchResult result = text.matches(FOUND_ON);
                if (result != null) {
                    // Note these fall through: Tables A-E are minor, G-I are major
                    switch (result.group(1)) {
                        case "A":
                            if (!hasRarity) {
                                tmpRarity = Rarity.common;
                                hasRarity = true;
                            }
                        case "B":
                            if (!hasRarity) {
                                tmpRarity = Rarity.uncommon;
                                hasRarity = true;
                            }
                        case "C":
                            if (!hasRarity) {
                                tmpRarity = Rarity.rare;
                                hasRarity = true;
                            }
                        case "D":
                            if (!hasRarity) {
                                tmpRarity = Rarity.veryRare;
                                hasRarity = true;
                            }
                        case "E":
                            if (!hasRarity) {
                                tmpRarity = Rarity.legendary;
                                hasRarity = true;
                            }
                            if (!hasClassification) {
                                tmpClassification = Classification.minor;
                                hasClassification = true;
                            }
                            break;
                        case "F":
                            if (!hasRarity) {
                                tmpRarity = Rarity.uncommon;
                                hasRarity = true;
                            }
                        case "G":
                            if (!hasRarity) {
                                tmpRarity = Rarity.rare;
                                hasRarity = true;
                            }
                        case "H":
                            if (!hasRarity) {
                                tmpRarity = Rarity.veryRare;
                                hasRarity = true;
                            }
                        case "I":
                            if (!hasRarity) {
                                tmpRarity = Rarity.legendary;
                                hasRarity = true;
                            }
                            if (!hasClassification) {
                                tmpClassification = Classification.major;
                                hasClassification = true;
                            }
                            break;
                    }
                }
            }

            if (!hasClassification && hasRarity) {
                if (tmpRarity == Rarity.legendary) {
                    tmpClassification = Classification.major;
                    hasClassification = true;
                } else if (tmpRarity == Rarity.rare || tmpRarity == Rarity.veryRare) { // rare or very rare
                    if (type.isConsumable()) {
                        tmpClassification = Classification.minor;
                        hasClassification = true;
                    } else {
                        tmpClassification = Classification.major;
                        hasClassification = true;
                    }
                } else if (tmpRarity == Rarity.uncommon || tmpRarity == Rarity.common) {
                    tmpClassification = Classification.minor;
                    hasClassification = true;
                }
            }

            this.rarity = tmpRarity;
            this.classification = tmpClassification;
            this.isMagic = type.canBeMagic() && magic;
        }

        public boolean hasTag() {
            return rarity != Rarity.none || classification != Classification.none;
        }

        String updateDetails(String detail) {
            if (hasTag()) {
                detail = detail.replaceAll("(, )?(major|minor),?", "")
                        .replaceAll("(legendary|very rare|rare|uncommon|common),?", "").trim();

                StringBuilder builder = new StringBuilder();
                builder.append(classification);
                if (rarity != Rarity.none) {
                    builder.append(" ").append(rarity);
                }
                if (!detail.isBlank()) {
                    builder.append(", ").append(detail);
                }
                detail = builder.toString();
            }
            return detail;
        }

        private String classificationTag() {
            StringBuilder sb = new StringBuilder();
            sb.append("item");
            if (classification != Classification.none) {
                sb.append("/").append(classification.toString());
            }
            if (rarity == Rarity.veryRare) {
                sb.append("/very-rare");
            } else if (rarity != Rarity.none) {
                sb.append("/").append(rarity.toString());
            }

            return sb.toString();
        }
    }

    enum Rarity {
        none,
        common,
        uncommon,
        rare,
        veryRare,
        legendary;

        public String toString() {
            switch (this) {
                case veryRare:
                    return "very rare";
                case none:
                    return "";
                default:
                    return name();
            }
        }

        public static Rarity find(String content) {
            String detail = content.toLowerCase();
            if (detail.contains("legendary")) {
                return legendary;
            } else if (detail.contains("very rare")) {
                return veryRare;
            } else if (detail.contains("rare")) {
                return rare;
            } else if (detail.contains("uncommon")) {
                return uncommon;
            } else if (detail.contains("common")) {
                return common;
            }
            return none;
        }
    }

    enum Classification {
        none,
        major,
        minor;

        public String toString() {
            return this == none ? "" : name();
        }

        public static Classification find(String content) {
            String detail = content.toLowerCase();
            if (detail.contains("major")) {
                return major;
            } else if (detail.contains("minor")) {
                return minor;
            }
            return none;
        }
    }
}
