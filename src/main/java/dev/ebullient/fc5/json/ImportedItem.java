package dev.ebullient.fc5.json;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.xml.bind.JAXBElement;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.data.SkillEnum;
import dev.ebullient.fc5.xml.XmlItemEnum;
import dev.ebullient.fc5.xml.XmlItemType;
import dev.ebullient.fc5.xml.XmlModifierType;
import dev.ebullient.fc5.xml.XmlObjectFactory;

public class ImportedItem extends ImportedBase {

    final String type;
    final String rarity;
    final XmlItemType fc5Item;
    final List<JAXBElement<?>> attributes;

    ImportedItem(XmlObjectFactory factory, JsonNode jsonItem, String name) {
        super(factory, jsonItem, name);

        this.fc5Item = factory.createItemType();
        this.attributes = fc5Item.getNameOrTypeOrMagic();

        attributes.add(factory.createItemTypeName(name));
        this.type = addItemTypeAttribute();
        this.rarity = addItemMagicAttribute();
    }

    public void populateXmlAttributes(Predicate<String> sourceIncluded, Function<String, String> lookupName) {
        if (copyOf != null) {
            return;
        }
        if (jsonElement.has("value")) {
            attributes.add(factory.createItemTypeValue(jsonElement.get("value").asText()));
        }
        if (jsonElement.has("weight")) {
            attributes.add(factory.createItemTypeWeight(jsonElement.get("weight").asText()));
        }
        if (jsonElement.has("strength")) {
            attributes.add(factory.createItemTypeStrength(jsonElement.get("strength").asText()));
        }
        if (jsonElement.has("dmg1")) {
            attributes.add(factory.createItemTypeDmg1(jsonElement.get("dmg1").asText()));
        }
        if (jsonElement.has("dmg2")) {
            attributes.add(factory.createItemTypeDmg2(jsonElement.get("dmg2").asText()));
        }
        if (jsonElement.has("dmgType")) {
            attributes.add(factory.createItemTypeDmgType(jsonElement.get("dmgType").asText()));
        }
        if (jsonElement.has("range")) {
            attributes.add(factory.createItemTypeRange(jsonElement.get("range").asText()));
        }
        if (jsonElement.has("ac")) {
            attributes.add(factory.createItemTypeAc(jsonElement.get("ac").bigIntegerValue()));
        }
        addAbilityModifiers(attributes);
        addItemBonusModifierAttribute("bonusAbilityCheck");
        addItemBonusModifierAttribute("bonusAc");
        addItemBonusModifierAttribute("bonusProficiencyBonus");
        addItemBonusModifierAttribute("bonusSavingThrow");
        addItemBonusModifierAttribute("bonusSpellAttack");
        addItemBonusModifierAttribute("bonusWeapon");
        addItemBonusModifierAttribute("bonusWeaponAttack");
        addItemBonusModifierAttribute("bonusWeaponCritDamage");
        addItemBonusModifierAttribute("bonusWeaponDamage");
        addItemStealthAttribute();
        addItemProperty();
        addItemDetail();
        addItemTextAndRolls(sourceIncluded);
    }

    void addItemDetail() {
        StringBuilder replacement = new StringBuilder();

        if (jsonElement.has("tier")) {
            replacement.append(jsonElement.get("tier").asText());
        }

        // rarity values: "rare", "none", "uncommon", "very rare", "legendary", "artifact", "unknown", "common", "unknown (magic)", "varies"
        switch (rarity) {
            case "none":
                break;
            case "unknown":
            case "unknown (magic)":
            case "varies":
                if (replacement.length() > 0) {
                    replacement.append(", ");
                }
                replacement.append("rarity ").append(rarity);
                break;
            default:
                if (replacement.length() > 0) {
                    replacement.append(", ");
                }
                replacement.append(rarity);
                break;
        }

        // attunement
        JsonNode attument = jsonElement.get("reqAttune");
        if (attument != null) {
            String value = attument.asText();
            if (attument.isBoolean()) {
                replacement.append(" (requires attunement)");
            } else if ("optional".equals(value)) {
                replacement.append(" (attunement optional)");
            } else {
                replacement.append(" (requires attunement ")
                        .append(value).append(")");
            }
        }

        attributes.add(factory.createItemTypeDetail(replacement.toString()));
    }

    public void addItemTextAndRolls(final Predicate<String> sourceIncluded) {
        StringBuilder text = new StringBuilder();
        Set<String> diceRolls = new HashSet<>();
        String altSource = bookSources.size() > 1 ? bookSources.get(1) : bookSources.get(0);

        jsonElement.withArray("entries").forEach(entry -> appendEntry(text, entry, diceRolls));

        jsonElement.withArray("additionalEntries").forEach(entry -> {
            if (entry.has("source") && !sourceIncluded.test(entry.get("source").asText())) {
                return;
            } else if (!sourceIncluded.test(altSource)) {
                return;
            }
            appendEntry(text, entry, diceRolls);
        });

        text.append("\n");

        if (jsonElement.has("lootTables")) {
            text.append("Found on: ");
            text.append(StreamSupport.stream(jsonElement.withArray("lootTables").spliterator(), false)
                    .map(x -> x.asText())
                    .collect(Collectors.joining(", ")))
                    .append("\n");
        }

        text.append("Source: ").append(sourceText);
        attributes.add(factory.createItemTypeText(text.toString()));

        diceRolls.forEach(r -> {
            if (r.startsWith("d")) {
                r = "1" + r;
            }
            attributes.add(factory.createItemTypeRoll(r));
        });
    }

    String addItemMagicAttribute() {
        JsonNode value = jsonElement.get("rarity");
        if (value != null) {
            String rarity = value.asText();
            attributes.add(factory.createItemTypeMagic("none".equals(rarity) ? "NO" : "YES"));
            return rarity;
        }
        return null;
    }

    void addItemBonusModifierAttribute(String key) {
        JsonNode bonusElement = jsonElement.get(key);
        if (bonusElement != null) {
            XmlModifierType mt;
            switch (key) {
                case "bonusAbilityCheck":
                    SkillEnum.allXmlNames
                            .stream()
                            .filter(s -> !"None".equals(s))
                            .forEach(s -> {
                                XmlModifierType smt = new XmlModifierType("Skill", s + " " + bonusElement.asText());
                                attributes.add(factory.createItemTypeModifier(smt));
                            });
                    break;
                case "bonusAC":
                    mt = new XmlModifierType("ac " + bonusElement.asText(), "bonus");
                    attributes.add(factory.createItemTypeModifier(mt));
                    break;
                case "bonusProficiencyBonus":
                    mt = new XmlModifierType("Proficiency Bonus " + bonusElement.asText(), "bonus");
                    attributes.add(factory.createItemTypeModifier(mt));
                    break;
                case "bonusSavingThrow":
                    mt = new XmlModifierType("Saving Throws " + bonusElement.asText(), "bonus");
                    attributes.add(factory.createItemTypeModifier(mt));
                    break;
                case "bonusSpellAttack":
                    mt = new XmlModifierType("Spell Attack " + bonusElement.asText(), "bonus");
                    attributes.add(factory.createItemTypeModifier(mt));
                    break;
                case "bonusSpellSaveDc":
                    mt = new XmlModifierType("Spell DC " + bonusElement.asText(), "bonus");
                    attributes.add(factory.createItemTypeModifier(mt));
                    break;
                case "bonusWeapon":
                    mt = new XmlModifierType("Weapon Attacks " + bonusElement.asText(), "bonus");
                    attributes.add(factory.createItemTypeModifier(mt));
                    mt = new XmlModifierType("Weapon Damage " + bonusElement.asText(), "bonus");
                    attributes.add(factory.createItemTypeModifier(mt));
                    break;
                case "bonusWeaponAttack":
                    mt = new XmlModifierType("Weapon Attacks " + bonusElement.asText(), "bonus");
                    attributes.add(factory.createItemTypeModifier(mt));
                    break;
                case "bonusWeaponCritDamage":
                    attributes.add(factory.createItemTypeRoll(bonusElement.asText()));
                    break;
                case "bonusWeaponDamage":
                    mt = new XmlModifierType("Weapon Damage " + bonusElement.asText(), "bonus");
                    attributes.add(factory.createItemTypeModifier(mt));
                    break;
            }
        }
    }

    String addItemTypeAttribute() {
        JsonNode value = jsonElement.get("type");
        if (value != null) {
            XmlItemEnum xv = XmlItemEnum.mapValue(value.asText());
            attributes.add(factory.createItemTypeType(xv));
            return value.asText();
        }
        return null;
    }

    void addItemStealthAttribute() {
        JsonNode value = jsonElement.get("stealth");
        if (value != null && value.asBoolean()) {
            attributes.add(factory.createItemTypeStealth("1"));
        }
    }

    void addItemProperty() {
        JsonNode value = jsonElement.get("property");
        if (value != null && value.isArray()) {
            List<String> properties = new ArrayList<>();
            value.forEach(x -> properties.add(x.asText()));
            attributes.add(factory.createItemTypeProperty(String.join(",", properties)));
        }
    }
}
