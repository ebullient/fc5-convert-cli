package dev.ebullient.fc5.fc5data;

import java.util.Collections;
import java.util.List;

import dev.ebullient.fc5.pojo.ItemEnum;
import dev.ebullient.fc5.pojo.Modifier;
import dev.ebullient.fc5.pojo.PropertyEnum;
import dev.ebullient.fc5.pojo.QuteItem;
import dev.ebullient.fc5.pojo.QuteSource;
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
public class Fc5Item extends QuteItem implements QuteSource {
    final Fc5Text text;
    final List<Fc5Roll> roll;

    public Fc5Item(String name, String detail, ItemEnum type, int ac,
            double cost, double weight, int strength, boolean stealth,
            String damage, String damage2H, String range, List<String> tags,
            List<Modifier> modifiers, List<PropertyEnum> properties,
            Fc5Text text, List<Fc5Roll> roll) {
        super(name, detail, type, ac, cost, weight, strength, stealth,
                damage, damage2H, range, tags, modifiers, properties, List.of());
        this.text = text;
        this.roll = roll;
    }

    public String getText() {
        return String.join("\n", text.content);
    }

    static class ItemBuilder extends Builder {
        Fc5ParsingContext context;

        public ItemBuilder(Fc5ParsingContext context) {
            this.context = context;
            setName(context.getOrFail(context.owner, "name", String.class));
            setType(ItemEnum.fromEncodedValue(context.getOrDefault("type", "")));

            setWeight(context.getOrDefault("weight", 0d));
            setCost(context.getOrDefault("value", 0d));

            setDamage(context.getOrDefault("dmg1", Fc5Roll.NONE).toString(),
                    context.getOrDefault("dmg2", Fc5Roll.NONE).toString(),
                    context.getOrDefault("dmgType", Fc5DamageEnum.UNKNOWN).value());
            setRange(context.getOrDefault("range", ""));

            setAc(context.getOrDefault("ac", 0));
            setStrengthRequirement(context.getOrDefault("strength", 0));
            setStealthPenalty(context.getOrDefault("stealth", false));

            setModifiers(context.getOrDefault("modifier", Collections.emptyList()));

            addProperties(PropertyEnum.fromEncodedValue(context.getOrDefault("property", "")));
        }

        public Fc5Item build() {
            Fc5Text text = context.getOrDefault("text", Fc5Text.NONE);
            List<Fc5Roll> roll = context.getOrDefault("roll", Collections.emptyList());

            String tmpDetail = context.getOrDefault("detail", "").toLowerCase();
            if (tmpDetail.contains("major")) {
                properties.add(PropertyEnum.MAJOR);
            } else if (tmpDetail.contains("minor")) {
                properties.add(PropertyEnum.MINOR);
            }
            if (tmpDetail.contains("legendary")) {
                properties.add(PropertyEnum.LEGENDARY);
            } else if (tmpDetail.contains("very rare")) {
                properties.add(PropertyEnum.VERY_RARE);
            } else if (tmpDetail.contains("rare")) {
                properties.add(PropertyEnum.RARE);
            } else if (tmpDetail.contains("uncommon")) {
                properties.add(PropertyEnum.UNCOMMON);
            } else if (tmpDetail.contains("common")) {
                properties.add(PropertyEnum.COMMON);
            } else if (tmpDetail.contains("artifact")) {
                properties.add(PropertyEnum.ARTIFACT);
            }
            String attunement = tmpDetail.contains("attunement") ? "true" : "";

            PropertyEnum.findAdditionalProperties(name, type, properties, s -> text.matchLine(s));
            setDetail(createDetail(attunement, type, properties));

            tags = getTags(type, properties);
            return new Fc5Item(name, detail, type, ac, cost, weight, strengthRequirement, stealthPenalty,
                    damage, damage2H, range, tags, modifiers, properties, text, roll);
        }
    }
}
