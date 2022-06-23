package dev.ebullient.fc5.json2xml;

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
import dev.ebullient.fc5.json5e.CompendiumSources;
import dev.ebullient.fc5.json5e.JsonIndex;
import dev.ebullient.fc5.json5e.JsonIndex.IndexType;
import dev.ebullient.fc5.pojo.ItemEnum;
import dev.ebullient.fc5.pojo.MdItem;
import dev.ebullient.fc5.pojo.PropertyEnum;
import dev.ebullient.fc5.pojo.SkillOrAbility;
import dev.ebullient.fc5.json2xml.jaxb.XmlItemEnum;
import dev.ebullient.fc5.json2xml.jaxb.XmlItemType;
import dev.ebullient.fc5.json2xml.jaxb.XmlModifierType;
import dev.ebullient.fc5.json2xml.jaxb.XmlObjectFactory;

public class CompendiumItem extends CompendiumBase {
    XmlItemType fc5Item;
    List<JAXBElement<?>> attributes;

    String itemName;
    ItemEnum itemType;
    List<PropertyEnum> propertyEnums = new ArrayList<>();

    public CompendiumItem(CompendiumSources sources, JsonIndex index, XmlObjectFactory factory) {
        super(sources, index, factory);
    }

    public XmlItemType getXmlCompendiumObject() {
        return fc5Item;
    }

    @Override
    public List<CompendiumBase> convert(JsonNode jsonSource) {
        if (index.keyIsExcluded(sources.getKey())) {
            Log.debugf("Excluded %s", sources);
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
        try {
            this.itemType = ItemEnum.fromEncodedValue(getTextOrDefault(jsonSource, "type", ""));
        } catch (IllegalArgumentException e) {
            Log.errorf(e, "Unable to parse text for item %s", sources);
            throw e;
        }

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
        addItemTextAndRolls(jsonSource);
        addItemDetail(jsonSource);

        return List.of(this);
    }

    private String getItemName(JsonNode itemNode) {
        JsonNode srd = itemNode.get("srd");
        if (srd != null) {
            if (srd.isTextual()) {
                return srd.asText();
            }
        }
        return sources.getName();
    }

    private void addItemDetail(JsonNode jsonElement) {
        List<PropertyEnum> propertyEnums = new ArrayList<>();
        JsonNode property = jsonElement.get("property");
        if (property != null && property.isArray()) {
            List<String> properties = new ArrayList<>();
            property.forEach(x -> {
                properties.add(x.asText());
                propertyEnums.add(PropertyEnum.fromEncodedType(x.asText()));
            });
            attributes.add(factory.createItemTypeProperty(String.join(",", properties)));
        }

        String tier = getTextOrDefault(jsonElement, "tier", "");
        if (!tier.isEmpty()) {
            propertyEnums.add(PropertyEnum.fromValue(tier));
        }
        String rarity = jsonElement.has("rarity")
                ? jsonElement.get("rarity").asText()
                : "";
        if (!rarity.isEmpty()) {
            propertyEnums.add(PropertyEnum.fromValue(rarity));
        }
        String attunement = getTextOrDefault(jsonElement, "reqAttune", "");
        String detail = MdItem.createDetail(attunement, itemType, propertyEnums);
        attributes.add(factory.createItemTypeDetail(replaceText(detail)));
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
                    SkillOrAbility.allSkills
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

        XmlItemEnum xv = XmlItemEnum.mapValue(itemType);
        attributes.add(factory.createItemTypeType(xv));
    }

    private void addItemStealthAttribute(JsonNode jsonElement) {
        JsonNode stealth = jsonElement.get("stealth");
        if (stealth != null && stealth.asBoolean()) {
            attributes.add(factory.createItemTypeStealth("1"));
        }
    }
}
