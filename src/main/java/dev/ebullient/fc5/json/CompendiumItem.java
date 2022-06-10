package dev.ebullient.fc5.json;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.xml.bind.JAXBElement;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.data.SkillEnum;
import dev.ebullient.fc5.xml.XmlItemEnum;
import dev.ebullient.fc5.xml.XmlItemType;
import dev.ebullient.fc5.xml.XmlModifierType;
import dev.ebullient.fc5.xml.XmlObjectFactory;

public class CompendiumItem extends CompendiumBase {

    String name;
    XmlItemType fc5Item;
    List<JAXBElement<?>> attributes;
    CompendiumSources sources;

    public CompendiumItem(String key, JsonIndex index, XmlObjectFactory factory) {
        super(key, index, factory);
    }

    public XmlItemType getXmlCompendiumObject() {
        return fc5Item;
    }

    @Override
    public boolean convert(JsonNode value) {
        this.sources = new CompendiumSources(key, value);
        this.fc5Item = factory.createItemType();
        this.attributes = fc5Item.getNameOrTypeOrMagic();
        getItemName(value);

        if (index.excludeElement(sources.bookSources, value.has("srd"))) {
            return false; // do not include
        }
        if (value.has("reprintedAs")) {
            String ra = value.get("reprintedAs").asText();
            if (index.sourceIncluded(ra.substring(ra.lastIndexOf("|")+1))) {
                Log.debugf("Skipping %s in favor of %s", key, ra);
                return false; // the reprint will be used instead of this one.
            }
        }

        addItemTypeAttribute(value);
        addItemMagicAttribute(value);
        addAbilityModifiers(value);
        addItemBonusModifierAttribute(value, "bonusAbilityCheck");
        addItemBonusModifierAttribute(value, "bonusAc");
        addItemBonusModifierAttribute(value, "bonusProficiencyBonus");
        addItemBonusModifierAttribute(value, "bonusSavingThrow");
        addItemBonusModifierAttribute(value, "bonusSpellAttack");
        addItemBonusModifierAttribute(value, "bonusWeapon");
        addItemBonusModifierAttribute(value, "bonusWeaponAttack");
        addItemBonusModifierAttribute(value, "bonusWeaponCritDamage");
        addItemBonusModifierAttribute(value, "bonusWeaponDamage");
        addItemStealthAttribute(value);
        addItemProperty(value);
        addItemDetail(value);
        addItemTextAndRolls(value);

        return true;
    }

    private String getItemName(JsonNode value) {
        JsonNode srd = value.get("srd");
        if (srd != null) {
            if (srd.isTextual()) {
                return this.name = srd.asText();
            }
        }
        return this.name = value.get("name").asText();
    }


    private void addAbilityModifiers(JsonNode value) {
        JsonNode abilityElement = value.get("ability");
        if (abilityElement == null) {
            return;
        }
        if (abilityElement.isObject()) {
            JsonNode staticValue = abilityElement.get("static");
            abilityObjectValue(attributes, staticValue == null
                    ? abilityElement
                    : staticValue, staticValue != null);
        } else if (abilityElement.isArray()) {
            // TODO
        } else if (abilityElement.isTextual()) {
            // TODO
        } else {
            Log.error("Unknown abilityElement: " + abilityElement.toPrettyString());
        }
    }

    private void abilityObjectValue(List<JAXBElement<?>> attributes,
        JsonNode abilityOrStaticElement, boolean isStatic) {
        if (abilityOrStaticElement.has("from") || abilityOrStaticElement.has("choose")) {
            return;
        }
        String type = isStatic ? "Score" : "Modifier";
        abilityOrStaticElement.fields().forEachRemaining(entry -> {
            XmlModifierType smt = new XmlModifierType(
                    String.format("%s %s %s", entry.getKey(), type, entry.getValue().asText()),
                    "Ability " + type);
            attributes.add(factory.createItemTypeModifier(smt));
        });
    }


    private void addItemDetail(JsonNode jsonElement) {
        StringBuilder replacement = new StringBuilder();

        if (jsonElement.has("tier")) {
            replacement.append(jsonElement.get("tier").asText());
        }

        // rarity values: "rare", "none", "uncommon", "very rare", "legendary", "artifact", "unknown", "common", "unknown (magic)", "varies"
        String rarity = jsonElement.has("rarity")
            ? jsonElement.get("rarity").asText()
            : "none";
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

    private void addItemTextAndRolls(JsonNode jsonElement) {
        StringBuilder text = new StringBuilder();
        Set<String> diceRolls = new HashSet<>();

        String sourceText = findSourceText(jsonElement);
        String altSource = alternateSource();

        jsonElement.withArray("entries").forEach(entry -> appendEntryToText(text, entry, diceRolls));
        jsonElement.withArray("additionalEntries").forEach(entry -> {
            if (entry.has("source") && !index.sourceIncluded(entry.get("source").asText())) {
                return;
            } else if (!index.sourceIncluded(altSource)) {
                return;
            }

            appendEntryToText(text, entry, diceRolls);
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

    private void addItemMagicAttribute(JsonNode jsonElement) {
        JsonNode value = jsonElement.get("rarity");
        if (value != null) {
            String rarity = value.asText();
            attributes.add(factory.createItemTypeMagic("none".equals(rarity) ? "NO" : "YES"));
        }
    }

    private void addItemBonusModifierAttribute(JsonNode jsonElement, String string) {
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

    private void addItemTypeAttribute(JsonNode jsonElement) {
        attributes.add(factory.createItemTypeName(name));
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

        JsonNode value = jsonElement.get("type");
        if (value != null) {
            XmlItemEnum xv = XmlItemEnum.mapValue(value.asText());
            attributes.add(factory.createItemTypeType(xv));
        }
    }

    private void addItemStealthAttribute(JsonNode jsonElement) {
        JsonNode value = jsonElement.get("stealth");
        if (value != null && value.asBoolean()) {
            attributes.add(factory.createItemTypeStealth("1"));
        }
    }

    private void addItemProperty(JsonNode jsonElement) {
        JsonNode value = jsonElement.get("property");
        if (value != null && value.isArray()) {
            List<String> properties = new ArrayList<>();
            value.forEach(x -> properties.add(x.asText()));
            attributes.add(factory.createItemTypeProperty(String.join(",", properties)));
        }
    }
}
