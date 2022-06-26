package dev.ebullient.fc5.json5e;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.Import5eTools;
import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.pojo.ItemEnum;
import dev.ebullient.fc5.pojo.Modifier;
import dev.ebullient.fc5.pojo.ModifierCategoryEnum;
import dev.ebullient.fc5.pojo.PropertyEnum;
import dev.ebullient.fc5.pojo.QuteItem;
import dev.ebullient.fc5.pojo.SkillOrAbility;

public interface JsonItem extends JsonBase {

    default String getItemName(JsonNode itemNode) {
        JsonNode srd = itemNode.get("srd");
        if (srd != null) {
            if (srd.isTextual()) {
                return srd.asText();
            }
        }
        return getSources().getName();
    }

    default boolean itemStealthPenalty(JsonNode jsonSource) {
        JsonNode stealth = jsonSource.get("stealth");
        if (stealth != null) {
            return stealth.asBoolean();
        }
        return false;
    }

    default String itemDetail(JsonNode jsonSource, List<PropertyEnum> propertyEnums) {
        String tier = getTextOrDefault(jsonSource, "tier", "");
        if (!tier.isEmpty()) {
            propertyEnums.add(PropertyEnum.fromValue(tier));
        }
        String rarity = jsonSource.has("rarity")
                ? jsonSource.get("rarity").asText()
                : "";
        if (!rarity.isEmpty() && !"none".equals(rarity)) {
            propertyEnums.add(PropertyEnum.fromValue(rarity));
        }
        String attunement = getTextOrDefault(jsonSource, "reqAttune", "");
        String detail = QuteItem.createDetail(attunement, null, propertyEnums);
        return replaceText(detail);
    }

    default List<String> itemTextAndRolls(JsonNode jsonSource) {
        return itemTextAndRolls(jsonSource, new ArrayList<>());
    }

    default List<String> itemTextAndRolls(JsonNode jsonSource, Collection<String> diceRolls) {
        List<String> text = new ArrayList<>();
        String sourceText = getSources().getSourceText();
        String altSource = getSources().alternateSource();

        try {
            getFluffDescription(jsonSource, JsonIndex.IndexType.itemfluff, text);
            jsonSource.withArray("entries").forEach(entry -> {
                if (entry.isTextual()) {
                    String input = entry.asText();
                    if (input.startsWith("{#itemEntry ")) {
                        insertItemRefText(text, jsonSource, input, diceRolls);
                    } else {
                        text.add(replaceText(entry.asText(), diceRolls));
                    }
                } else {
                    appendEntryToText(text, entry, diceRolls);
                }
            });
            addAdditionalEntries(jsonSource, text, diceRolls, altSource);
        } catch (Exception e) {
            Log.errorf(e, "Unable to parse text for item %s", getSources());
        }
        maybeAddBlankLine(text);

        if (jsonSource.has("lootTables")) {
            String txt = "Found on: " +
                    StreamSupport.stream(jsonSource.withArray("lootTables").spliterator(), false)
                            .map(JsonNode::asText)
                            .collect(Collectors.joining(", "));
            text.add(txt);
        }
        text.add("Source: " + sourceText);
        return text;
    }

    default void insertItemRefText(List<String> text, JsonNode source, String input, Collection<String> diceRolls) {
        JsonIndex index = getIndex();
        String finalKey = index.getRefKey(JsonIndex.IndexType.itementry, input.replaceAll("\\{#itemEntry (.*)\\}", "$1"));
        if (index.keyIsExcluded(finalKey)) {
            return;
        }
        JsonNode ref = index.getNode(finalKey);
        if (ref == null) {
            Log.errorf("Could not find %s from %s", finalKey, getSources());
            return;
        } else if (index.sourceIncluded(ref.get("source").asText())) {
            try {
                String entriesTemplate = Import5eTools.MAPPER.writeValueAsString(ref.get("entriesTemplate"));
                if (source.has("detail1")) {
                    entriesTemplate = entriesTemplate.replaceAll("\\{\\{item.detail1}}", source.get("detail1").asText());
                }
                if (source.has("resist")) {
                    entriesTemplate = entriesTemplate.replaceAll("\\{\\{item.resist}}",
                            joinAndReplace(source.withArray("resist")));
                }
                appendEntryToText(text, Import5eTools.MAPPER.readTree(entriesTemplate), diceRolls);
            } catch (JsonProcessingException e) {
                Log.errorf(e, "Unable to insert item element text for %s from %s", input, getSources());
            }
        }
    }

    default List<String> findProperties(JsonNode jsonSource, List<PropertyEnum> propertyEnums) {
        JsonNode property = jsonSource.get("property");
        if (property != null && property.isArray()) {
            List<String> properties = new ArrayList<>();
            property.forEach(x -> {
                properties.add(x.asText());
                propertyEnums.add(PropertyEnum.fromEncodedType(x.asText()));
            });
            return properties;
        }
        return List.of();
    }

    default List<Modifier> itemBonusModifers(JsonNode jsonSource) {
        return itemBonusModifers(jsonSource, new ArrayList<>());
    }

    default List<Modifier> itemBonusModifers(JsonNode jsonSource, Collection<String> diceRolls) {
        List<Modifier> bonuses = new ArrayList<>();
        for (String key : List.of("bonusAbilityCheck", "bonusAc", "bonusProficiencyBonus",
                "bonusSavingThrow", "bonusSpellAttack", "bonusWeapon",
                "bonusWeaponAttack", "bonusWeaponCritDamage", "bonusWeaponDamage")) {
            JsonNode bonusElement = jsonSource.get(key);
            if (bonusElement == null) {
                continue;
            }
            switch (key) {
                case "bonusAbilityCheck":
                    SkillOrAbility.allSkills
                            .stream()
                            .forEach(s -> bonuses.add(new Modifier(s + " " + bonusElement.asText(),
                                    ModifierCategoryEnum.SKILLS)));
                    break;
                case "bonusAC":
                    bonuses.add(new Modifier("ac " + bonusElement.asText(), ModifierCategoryEnum.BONUS));
                    break;
                case "bonusProficiencyBonus":
                    bonuses.add(new Modifier("Proficiency Bonus " + bonusElement.asText(), ModifierCategoryEnum.BONUS));
                    break;
                case "bonusSavingThrow":
                    bonuses.add(new Modifier("Saving Throws " + bonusElement.asText(), ModifierCategoryEnum.BONUS));
                    break;
                case "bonusSpellAttack":
                    bonuses.add(new Modifier("Spell Attack " + bonusElement.asText(), ModifierCategoryEnum.BONUS));
                    break;
                case "bonusSpellSaveDc":
                    bonuses.add(new Modifier("Spell DC " + bonusElement.asText(), ModifierCategoryEnum.BONUS));
                    break;
                case "bonusWeapon":
                    bonuses.add(new Modifier("Weapon Attacks " + bonusElement.asText(), ModifierCategoryEnum.BONUS));
                    bonuses.add(new Modifier("Weapon Damage " + bonusElement.asText(), ModifierCategoryEnum.BONUS));
                    break;
                case "bonusWeaponAttack":
                    bonuses.add(new Modifier("Weapon Attacks " + bonusElement.asText(), ModifierCategoryEnum.BONUS));
                    break;
                case "bonusWeaponCritDamage":
                    diceRolls.add(bonusElement.asText());
                    break;
                case "bonusWeaponDamage":
                    bonuses.add(new Modifier("Weapon Damage " + bonusElement.asText(), ModifierCategoryEnum.BONUS));
                    break;
            }

        }
        return bonuses;
    }

    default ItemEnum getType(JsonNode jsonSource) {
        try {
            String type = getTextOrDefault(jsonSource, "type", "");
            if (!type.isEmpty()) {
                return ItemEnum.fromEncodedValue(type);
            }
            if (booleanOrDefault(jsonSource, "staff", false)) {
                return ItemEnum.STAFF;
            }
            if (jsonSource.has("weaponCategory")) {
                return jsonSource.has("range")
                        ? ItemEnum.RANGED_WEAPON
                        : ItemEnum.MELEE_WEAPON;
            }
            if (booleanOrDefault(jsonSource, "poison", false)) {
                return ItemEnum.GEAR;
            }
            if (booleanOrDefault(jsonSource, "wondrous", false)
                    || booleanOrDefault(jsonSource, "sentient", false)) {
                return ItemEnum.WONDROUS;
            }
            if (suggestsMeleeWeapon(getSources().getName())) {
                return ItemEnum.MELEE_WEAPON;
            }
            if (suggestsRangedWeapon(getSources().getName())) {
                return ItemEnum.RANGED_WEAPON;
            }
            if (jsonSource.has("rarity")) {
                return ItemEnum.WONDROUS;
            }
            throw new IllegalArgumentException("Unknown type");
        } catch (IllegalArgumentException e) {
            Log.errorf(e, "Unable to parse text for item %s", getSources());
            throw e;
        }
    }

    default boolean suggestsMeleeWeapon(String name) {
        String lower = name.toLowerCase();
        return lower.matches(".*(dagger|sword|axe|mace|mornigstar|scimitar|axe|hammer).*");
    }

    default boolean suggestsRangedWeapon(String name) {
        String lower = name.toLowerCase();
        return lower.matches(".*(shortbow|longbow|crossbow).*");
    }
}
