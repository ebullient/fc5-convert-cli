package dev.ebullient.fc5.pojo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import io.quarkus.qute.TemplateData;

@TemplateData
public class QuteItem implements QuteSource {
    protected final String name;
    protected final ItemEnum type;
    protected final boolean magic;
    protected final String detail;
    protected final double weight;
    protected final List<String> text;

    protected final double cost;
    protected final List<Modifier> modifiers;
    protected final int ac;
    protected final int strengthRequirement;
    protected final boolean stealthPenalty;
    protected final String damage;
    protected final String damage2H;
    protected final List<PropertyEnum> properties;
    protected final String range;
    protected final List<String> tags;

    protected QuteItem(String name, boolean magic, String detail, ItemEnum type, int ac,
            double cost, double weight, int strength, boolean stealth,
            String damage, String damage2H, String range, List<String> tags,
            List<Modifier> modifiers, List<PropertyEnum> properties,
            List<String> text) {
        this.name = name;
        this.magic = magic;
        this.detail = detail;
        this.type = type;
        this.ac = ac;
        this.cost = cost;
        this.weight = weight;
        this.stealthPenalty = stealth;
        this.strengthRequirement = strength;
        this.damage = damage;
        this.damage2H = damage2H;
        this.range = range;
        this.modifiers = modifiers;
        this.properties = properties;
        this.tags = tags;
        this.text = breathe(text);
    }

    public String getName() {
        return name;
    }

    public ItemEnum getType() {
        return type;
    }

    public String getDetail() {
        return detail;
    }

    public boolean isArmor() {
        return type.isArmor();
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

    public boolean isWeapon() {
        return type.isWeapon();
    }

    public boolean isVersatile() {
        return properties.contains(PropertyEnum.VERSATILE);
    }

    public boolean isRanged() {
        return type == ItemEnum.RANGED_WEAPON;
    }

    public String getDamage() {
        return damage;
    }

    public String getDamage2H() {
        return damage2H == null ? "" : damage2H;
    }

    public String getRange() {
        return range;
    }

    public List<Modifier> getModifiers() {
        return modifiers;
    }

    public List<PropertyEnum> getProperties() {
        return properties;
    }

    public String getPropertiesString() {
        return String.join(", ", properties.stream().map(x -> x.getMarkdownLink()).collect(Collectors.toList()));
    }

    public int getStrengthRequirement() {
        return strengthRequirement;
    }

    public boolean getStealthPenalty() {
        return stealthPenalty;
    }

    public double getCost() {
        return cost;
    }

    public double getWeight() {
        return weight;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getText() {
        return String.join("\n", text).trim();
    }

    @Override
    public String toString() {
        return "item[name=" + name + "]";
    }

    public static class Builder {
        protected String name;
        protected ItemEnum type;
        protected String detail;
        protected boolean magic;
        protected int ac;
        protected double cost;
        protected double weight;
        protected int strengthRequirement;
        protected boolean stealthPenalty;
        protected String damage;
        protected String damage2H;
        protected String range;
        protected List<PropertyEnum> properties = new ArrayList<>();
        protected List<Modifier> modifiers = new ArrayList<>();
        protected List<String> text = new ArrayList<>();
        protected List<String> tags;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setType(String v) {
            this.type = ItemEnum.fromEncodedValue(v);
            return this;
        }

        public Builder setType(ItemEnum type) {
            this.type = type;
            return this;
        }

        public Builder setDetail(String detail) {
            this.detail = detail;
            return this;
        }

        public Builder setMagic(boolean magic) {
            this.magic = magic;
            return this;
        }

        public Builder setAc(Integer v) {
            if (v != null) {
                this.ac = v;
            }
            return this;
        }

        public Builder setCost(Double v) {
            if (v != null) {
                this.cost = v;
            }
            return this;
        }

        public Builder setWeight(Double v) {
            if (v != null) {
                this.weight = v;
            }
            return this;
        }

        public Builder setStrengthRequirement(Integer v) {
            if (v != null) {
                this.strengthRequirement = v;
            }
            return this;
        }

        public Builder setStealthPenalty(boolean stealthPenalty) {
            this.stealthPenalty = stealthPenalty;
            return this;
        }

        public Builder setDamage(String dmg1, String dmg2, String dmgType) {
            this.damage = dmg1 + " " + dmgType;
            if (dmg2 != null && !dmg2.isBlank()) {
                this.damage2H = dmg2 + " " + dmgType;
            }
            return this;
        }

        public Builder setRange(String range) {
            this.range = range;
            return this;
        }

        public Builder addProperty(PropertyEnum propertyEnum) {
            this.properties.add(propertyEnum);
            return this;
        }

        public Builder addProperties(List<PropertyEnum> list) {
            this.properties.addAll(list);
            return this;
        }

        public Builder setModifiers(List<Modifier> modifiers) {
            this.modifiers = modifiers;
            return this;
        }

        public Builder addText(String t) {
            this.text.add(t);
            return this;
        }

        public Builder addText(Collection<String> t) {
            if (t != null) {
                this.text.addAll(t);
            }
            return this;
        }

        public QuteItem build() {
            PropertyEnum.findAdditionalProperties(name, type, properties, s -> text.stream().anyMatch(l -> l.matches(s)));
            tags = getTags(type, properties);

            return new QuteItem(name, magic, detail, type, ac, cost, weight,
                    strengthRequirement, stealthPenalty, damage, damage2H, range, tags,
                    modifiers, properties, text);
        }
    }

    public static List<String> getTags(ItemEnum type, List<PropertyEnum> properties) {
        List<String> tags = new ArrayList<>();
        tags.add(type.getItemTag(properties));
        for (PropertyEnum p : properties) {
            tags.add("item/property/" + p.value().toLowerCase());
        }
        return tags;
    }

    /**
     * @param attunement blank if false, "true" for default string, "optional" if attunement is optional, or some other specific
     *        string
     * @param type Item type
     * @param properties Item properties -- ensure non-null & modifiable: side-effect, will set magic properties
     * @return
     */
    public static String createDetail(String attunement, ItemEnum type, List<PropertyEnum> properties) {
        StringBuilder replacement = new StringBuilder();

        if (type != null) {
            if (type.isWeapon()) {
                replacement.append(properties.contains(PropertyEnum.MARTIAL) ? "martial " : "simple ");
            }
            replacement.append(type.getSpecializedLower());
        }
        PropertyEnum.tierProperties.forEach(p -> {
            if (properties.contains(p)) {
                if (replacement.length() > 0) {
                    replacement.append(", ");
                }
                replacement.append(p.value());
            }
        });
        PropertyEnum.rarityProperties.forEach(p -> {
            if (properties.contains(p)) {
                if (replacement.length() > 0) {
                    replacement.append(", ");
                }
                replacement.append(p.value());
            }
        });
        if (properties.contains(PropertyEnum.POISON)) {
            if (replacement.length() > 0) {
                replacement.append(", ");
            }
            replacement.append(PropertyEnum.POISON.value());
        }
        if (properties.contains(PropertyEnum.CURSED)) {
            if (replacement.length() > 0) {
                replacement.append(", ");
            }
            replacement.append(PropertyEnum.CURSED.value());
        }

        switch (attunement) {
            case "":
            case "false":
                break;
            case "true":
                properties.add(PropertyEnum.REQ_ATTUNEMENT);
                replacement.append(" (requires attunement)");
                break;
            case "optional":
                properties.add(PropertyEnum.OPT_ATTUNEMENT);
                replacement.append(" (attunement optional)");
                break;
            default:
                properties.add(PropertyEnum.REQ_ATTUNEMENT);
                replacement.append(" (requires attunement ")
                        .append(attunement).append(")");
                break;
        }
        return replacement.toString();
    }
}
