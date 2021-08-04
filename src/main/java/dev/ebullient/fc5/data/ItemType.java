package dev.ebullient.fc5.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    final List<Modifier> modifiers;
    final int ac;
    final int strength;
    final boolean stealth;
    final Roll dmg1;
    final Roll dmg2;
    final DamageEnum dmgType;
    final List<PropertyEnum> properties;
    final String range;

    public ItemType(ParsingContext context) {
        name = context.getOrFail(context.owner, "name", String.class);

        type = ItemEnum.fromXmlValue(context.getOrDefault(name, "type", ""));
        properties = PropertyEnum.fromXmlValue(context.getOrDefault(name, "property", ""));

        weight = context.getOrDefault(name, "weight", 0d);
        cost = context.getOrDefault(name, "value", 0d);

        text = context.getOrDefault(name, "text", Text.NONE);

        dmg1 = context.getOrDefault(name, "dmg1", Roll.NONE);
        dmg2 = context.getOrDefault(name, "dmg2", Roll.NONE);
        dmgType = context.getOrDefault(name, "dmgType", DamageEnum.UNKNOWN);
        range = context.getOrDefault(name, "range", "");

        ac = context.getOrDefault(name, "ac", 0);
        strength = context.getOrDefault(name, "strength", 0);
        stealth = context.getOrDefault(name, "stealth", false);

        modifiers = context.getOrDefault(name, "modifier", Collections.emptyList());
        roll = context.getOrDefault(name, "roll", Collections.emptyList());

        // Figure out missing details
        String tmpDetail = context.getOrDefault(name, "detail", "");
        magicItem = new MagicItem(name, tmpDetail, text, type, context.getOrDefault(name, "magic", false));
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
        for (PropertyEnum p : properties) {
            tags.add("item/property/" + p.value().toLowerCase());
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
        return modifiers;
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
        if (type == ItemEnum.LIGHT_ARMOR) {
            result.append(" + DEX");
        } else if (type == ItemEnum.MEDIUM_ARMOR) {
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
        return properties.contains(PropertyEnum.VERSATILE);
    }

    public String getDamage() {
        return dmg1 + " " + dmgType.value();
    }

    public String getDamage2H() {
        return dmg2 + " " + dmgType.value();
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

            boolean hasRarity = tmpRarity != Rarity.NONE;
            boolean hasClassification = tmpClassification != Classification.NONE;

            if (type.canBeMagic() && (magic || hasRarity || hasClassification)) {
                if (name.startsWith("+") && !hasRarity) {
                    hasRarity = true;
                    if (name.startsWith("+1")) {
                        tmpRarity = Rarity.RARE;
                    } else if (name.startsWith("+2")) {
                        tmpRarity = Rarity.VERY_RARE;
                    } else if (name.startsWith("+3")) {
                        tmpRarity = Rarity.LEGENDARY;
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
                                tmpRarity = Rarity.UNCOMMON;
                                hasRarity = true;
                            }
                        case "C":
                            if (!hasRarity) {
                                tmpRarity = Rarity.RARE;
                                hasRarity = true;
                            }
                        case "D":
                            if (!hasRarity) {
                                tmpRarity = Rarity.VERY_RARE;
                                hasRarity = true;
                            }
                        case "E":
                            if (!hasRarity) {
                                tmpRarity = Rarity.LEGENDARY;
                                hasRarity = true;
                            }
                            if (!hasClassification) {
                                tmpClassification = Classification.MINOR;
                                hasClassification = true;
                            }
                            break;
                        case "F":
                            if (!hasRarity) {
                                tmpRarity = Rarity.UNCOMMON;
                                hasRarity = true;
                            }
                        case "G":
                            if (!hasRarity) {
                                tmpRarity = Rarity.RARE;
                                hasRarity = true;
                            }
                        case "H":
                            if (!hasRarity) {
                                tmpRarity = Rarity.VERY_RARE;
                                hasRarity = true;
                            }
                        case "I":
                            if (!hasRarity) {
                                tmpRarity = Rarity.LEGENDARY;
                                hasRarity = true;
                            }
                            if (!hasClassification) {
                                tmpClassification = Classification.MAJOR;
                                hasClassification = true;
                            }
                            break;
                    }
                }
            }

            if (!hasClassification && hasRarity) {
                if (tmpRarity == Rarity.LEGENDARY) {
                    tmpClassification = Classification.MAJOR;
                    hasClassification = true;
                } else if (tmpRarity == Rarity.RARE || tmpRarity == Rarity.VERY_RARE) { // rare or very rare
                    if (type.isConsumable()) {
                        tmpClassification = Classification.MINOR;
                        hasClassification = true;
                    } else {
                        tmpClassification = Classification.MAJOR;
                        hasClassification = true;
                    }
                } else if (tmpRarity == Rarity.UNCOMMON || tmpRarity == Rarity.common) {
                    tmpClassification = Classification.MINOR;
                    hasClassification = true;
                }
            }

            this.rarity = tmpRarity;
            this.classification = tmpClassification;
            this.isMagic = type.canBeMagic() && magic;
        }

        public boolean hasTag() {
            return rarity != Rarity.NONE || classification != Classification.NONE;
        }

        String updateDetails(String detail) {
            if (hasTag()) {
                detail = detail.replaceAll("(, )?(major|minor),?", "")
                        .replaceAll("(legendary|very rare|rare|uncommon|common),?", "").trim();

                StringBuilder builder = new StringBuilder();
                builder.append(classification);
                if (rarity != Rarity.NONE) {
                    builder.append(", ").append(rarity);
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
            if (classification != Classification.NONE) {
                sb.append("/").append(classification.toString());
            }
            if (rarity == Rarity.VERY_RARE) {
                sb.append("/very-rare");
            } else if (rarity != Rarity.NONE) {
                sb.append("/").append(rarity.toString());
            }

            return sb.toString();
        }
    }

    enum Rarity {
        NONE,
        common,
        UNCOMMON,
        RARE,
        VERY_RARE,
        LEGENDARY;

        public String toString() {
            return this == NONE ? "" : name().toLowerCase().replace("_", " ");
        }

        public static Rarity find(String content) {
            String detail = content.toLowerCase();
            if (detail.contains("legendary")) {
                return LEGENDARY;
            } else if (detail.contains("very rare")) {
                return VERY_RARE;
            } else if (detail.contains("rare")) {
                return RARE;
            } else if (detail.contains("uncommon")) {
                return UNCOMMON;
            } else if (detail.contains("common")) {
                return common;
            }
            return NONE;
        }
    }

    enum Classification {
        NONE,
        MAJOR,
        MINOR;

        public String toString() {
            return this == NONE ? "" : name().toLowerCase();
        }

        public static Classification find(String content) {
            String detail = content.toLowerCase();
            if (detail.contains("major")) {
                return MAJOR;
            } else if (detail.contains("minor")) {
                return MINOR;
            }
            return NONE;
        }
    }
}
