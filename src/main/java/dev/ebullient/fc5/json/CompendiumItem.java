package dev.ebullient.fc5.json;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.xml.bind.JAXBElement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.Import5eTools;
import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.data.SkillEnum;
import dev.ebullient.fc5.json.JsonIndex.IndexType;
import dev.ebullient.fc5.xml.XmlItemEnum;
import dev.ebullient.fc5.xml.XmlItemType;
import dev.ebullient.fc5.xml.XmlModifierType;
import dev.ebullient.fc5.xml.XmlObjectFactory;

public class CompendiumItem extends CompendiumBase {
    XmlItemType fc5Item;
    List<JAXBElement<?>> attributes;

    String itemName;

    public CompendiumItem(CompendiumSources sources, JsonIndex index, XmlObjectFactory factory) {
        super(sources, index, factory);
    }

    public XmlItemType getXmlCompendiumObject() {
        return fc5Item;
    }

    @Override
    public List<CompendiumBase> convert(JsonNode jsonSource) {
        if (index.keyIsExcluded(sources.key)) {
            Log.debugf("Excluded %s", sources.key);
            return List.of(); // do not include
        }

        jsonSource = index.handleCopy(IndexType.item, jsonSource);
        if (jsonSource.has("reprintedAs")) {
            String ra = jsonSource.get("reprintedAs").asText();
            if (index.sourceIncluded(ra.substring(ra.lastIndexOf("|") + 1))) {
                Log.debugf("Skipping %s in favor of %s", sources, ra);
                return List.of(); // the reprint will be used instead of this one.
            }
        }
        this.fc5Item = factory.createItemType();
        this.attributes = fc5Item.getNameOrTypeOrMagic();
        this.itemName = getItemName(jsonSource);

        attributes.add(factory.createItemTypeName(itemName));
        addItemTypeAttribute(jsonSource);
        addItemMagicAttribute(jsonSource);
        collectModifierTypes(jsonSource).forEach(m -> attributes.add(factory.createItemTypeModifier(m)));

        addItemBonusModifierAttribute(jsonSource, "bonusAbilityCheck");
        addItemBonusModifierAttribute(jsonSource, "bonusAc");
        addItemBonusModifierAttribute(jsonSource, "bonusProficiencyBonus");
        addItemBonusModifierAttribute(jsonSource, "bonusSavingThrow");
        addItemBonusModifierAttribute(jsonSource, "bonusSpellAttack");
        addItemBonusModifierAttribute(jsonSource, "bonusWeapon");
        addItemBonusModifierAttribute(jsonSource, "bonusWeaponAttack");
        addItemBonusModifierAttribute(jsonSource, "bonusWeaponCritDamage");
        addItemBonusModifierAttribute(jsonSource, "bonusWeaponDamage");
        addItemStealthAttribute(jsonSource);
        addItemProperty(jsonSource);
        addItemDetail(jsonSource);
        addItemTextAndRolls(jsonSource);

        return List.of(this);
    }

    private String getItemName(JsonNode itemNode) {
        JsonNode srd = itemNode.get("srd");
        if (srd != null) {
            if (srd.isTextual()) {
                return srd.asText();
            }
        }
        return sources.name;
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
        JsonNode attunementNode = jsonElement.get("reqAttune");
        if (attunementNode != null) {
            String attunement = attunementNode.asText();
            if (attunementNode.isBoolean()) {
                replacement.append(" (requires attunement)");
            } else if ("optional".equals(attunement)) {
                replacement.append(" (attunement optional)");
            } else {
                replacement.append(" (requires attunement ")
                        .append(attunement).append(")");
            }
        }

        attributes.add(factory.createItemTypeDetail(replaceText(replacement.toString())));
    }

    private void addItemTextAndRolls(JsonNode jsonElement) {
        Set<String> diceRolls = new HashSet<>();

        String sourceText = sources.getSourceText();
        String altSource = sources.alternateSource();

        List<String> text = new ArrayList<>();
        try {
            getFluffDescription(jsonElement, IndexType.itemfluff, text);
            jsonElement.withArray("entries").forEach(entry -> {
                if (entry.isTextual()) {
                    String input = entry.asText();
                    if (input.startsWith("{#itemEntry ")) {
                        insertItemRefText(text, jsonElement, input, diceRolls);
                    } else {
                        text.add(replaceText(entry.asText(), diceRolls));
                    }
                } else {
                    appendEntryToText(text, entry, diceRolls);
                }
            });
            addAdditionalEntries(jsonElement, text, diceRolls, altSource);
        } catch (Exception e) {
            Log.errorf(e, "Unable to parse text for item %s", sources);
        }
        maybeAddBlankLine(text);

        if (jsonElement.has("lootTables")) {
            StringBuilder txt = new StringBuilder();
            txt.append("Found on: ");
            txt.append(StreamSupport.stream(jsonElement.withArray("lootTables").spliterator(), false)
                    .map(x -> x.asText())
                    .collect(Collectors.joining(", ")));
            text.add(txt.toString());
        }
        text.add("Source: " + sourceText);
        text.forEach(t -> attributes.add(factory.createItemTypeText(t)));

        diceRolls.forEach(r -> {
            if (r.startsWith("d")) {
                r = "1" + r;
            }
            attributes.add(factory.createItemTypeRoll(r));
        });
    }

    private void insertItemRefText(List<String> text, JsonNode source, String input, Set<String> diceRolls) {
        String finalKey = index.getRefKey(IndexType.itementry, input.replaceAll("\\{#itemEntry (.*)\\}", "$1"));
        if (index.keyIsExcluded(finalKey)) {
            return;
        }
        JsonNode ref = index.getNode(finalKey);
        if (ref == null) {
            Log.errorf("Could not find %s from %s", finalKey, sources);
            return;
        } else if (index.sourceIncluded(ref.get("source").asText())) {
            try {
                String entriesTemplate = Import5eTools.MAPPER.writeValueAsString(ref.get("entriesTemplate"));
                if (source.has("detail1")) {
                    entriesTemplate = entriesTemplate.replaceAll("\\{\\{item.detail1\\}\\}", source.get("detail1").asText());
                }
                if (source.has("resist")) {
                    entriesTemplate = entriesTemplate.replaceAll("\\{\\{item.resist\\}\\}",
                            joinAndReplace(source.withArray("resist")));
                }
                appendEntryToText(text, Import5eTools.MAPPER.readTree(entriesTemplate), diceRolls);
            } catch (JsonProcessingException e) {
                Log.errorf(e, "Unable to insert item element text for %s from %s", input, sources);
            }
        }
    }

    private void addItemMagicAttribute(JsonNode itemNode) {
        JsonNode value = itemNode.get("rarity");
        if (value != null) {
            String rarity = value.asText();
            attributes.add(factory.createItemTypeMagic("none".equals(rarity) ? "NO" : "YES"));
        }
    }

    private void addItemBonusModifierAttribute(JsonNode jsonElement, String key) {
        JsonNode bonusElement = jsonElement.get(key);
        if (bonusElement != null) {
            XmlModifierType mt;
            switch (key) {
                case "bonusAbilityCheck":
                    SkillEnum.allSkills
                            .stream()
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

        JsonNode type = jsonElement.get("type");
        if (type != null) {
            try {
                XmlItemEnum xv = XmlItemEnum.mapValue(type.asText());
                attributes.add(factory.createItemTypeType(xv));
            } catch (Exception e) {
                Log.errorf(e, "Unable to determine type of %s from %s", sources, type.toPrettyString());
            }
        }
    }

    private void addItemStealthAttribute(JsonNode jsonElement) {
        JsonNode stealth = jsonElement.get("stealth");
        if (stealth != null && stealth.asBoolean()) {
            attributes.add(factory.createItemTypeStealth("1"));
        }
    }

    private void addItemProperty(JsonNode jsonElement) {
        JsonNode property = jsonElement.get("property");
        if (property != null && property.isArray()) {
            List<String> properties = new ArrayList<>();
            property.forEach(x -> properties.add(x.asText()));
            attributes.add(factory.createItemTypeProperty(String.join(",", properties)));
        }
    }
}
