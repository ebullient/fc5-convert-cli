package dev.ebullient.fc5.json5e;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.pojo.MarkdownWriter;
import dev.ebullient.fc5.pojo.Modifier;
import dev.ebullient.fc5.pojo.ModifierCategoryEnum;
import dev.ebullient.fc5.pojo.QuteTrait;
import dev.ebullient.fc5.pojo.SizeEnum;
import dev.ebullient.fc5.pojo.SkillOrAbility;

public interface JsonBase {
    Pattern featPattern = Pattern.compile("([^|]+)\\|?.*");

    Pattern itemPattern = Pattern.compile("\\{@item ([^|}]+)\\|?([^|}]*)\\|?([^|}]*)?\\}");
    Pattern spellPattern = Pattern.compile("\\{@spell ([^|}]+)\\|?([^|}]*)\\|?([^|}]*)?\\}");
    Pattern dicePattern = Pattern.compile("\\{@(dice|damage) ([^|}]+)[^}]*\\}");
    Pattern chancePattern = Pattern.compile("\\{@chance ([^}]+)\\}");

    String damage = "\\(([0-9d+ -]+)\\)";
    String onlyDamage = ".*?" + damage + ".*";
    String additive = damage + "[a-z, ]+plus [0-9 ]+" + damage;
    Pattern alternate = Pattern.compile(damage + "[a-z, ]+or [0-9 ]+" + damage + " (.*)");
    String LI = "• ";
    String MD_LI = "- ";
    List<String> SPECIAL = List.of(
            "Fighting Style: Archery",
            "Fighting Style: Defense",
            "Fighting Style: Dueling",
            "Fighting Style: Two-Weapon Fighting",
            "Jack of all Trades",
            "Powerful Build",
            "Unarmored Defense: Charisma",
            "Unarmored Defense: Constitution",
            "Unarmored Defense: Dexterity",
            "Unarmored Defense: Intelligence",
            "Unarmored Defense: Strength",
            "Unarmored Defense: Wisdom");

    CompendiumSources getSources();

    JsonIndex getIndex();

    boolean isMarkdown();

    default String li() {
        return isMarkdown() ? MD_LI : LI;
    }

    static boolean isReprinted(JsonIndex index, String finalKey, JsonNode jsonSource) {
        if (jsonSource.has("reprintedAs")) {
            for (Iterator<JsonNode> i = jsonSource.withArray("reprintedAs").elements(); i.hasNext();) {
                String ra = i.next().asText();
                if (index.sourceIncluded(ra.substring(ra.lastIndexOf("|") + 1))) {
                    Log.debugf("Skipping %s in favor of %s", finalKey, ra);
                    return true; // the reprint will be used instead (stop parsing this one)
                }
            }
        }
        if (jsonSource.has("isReprinted")) {
            Log.debugf("Skipping %s (has been reprinted)", finalKey);
            return true; // the reprint will be used instead of this one.
        }
        return false;
    }

    default Stream<JsonNode> streamOf(ArrayNode array) {
        return StreamSupport.stream(array.spliterator(), false);
    }

    default String replaceText(String input) {
        return replaceText(input, new ArrayList<>());
    }

    default String replaceText(String input, final Collection<String> diceRolls) {
        return replaceAttributes(replaceDiceTxt(input, diceRolls));
    }

    default List<String> findAndReplace(JsonNode jsonSource, String field) {
        return findAndReplace(jsonSource, field, s -> s);
    }

    default List<String> findAndReplace(JsonNode jsonSource, String field, Function<String, String> replacement) {
        JsonNode node = jsonSource.get(field);
        if (node == null || node.isNull()) {
            return List.of();
        } else if (node.isTextual()) {
            return List.of(replaceText(node.asText()));
        } else if (node.isObject()) {
            throw new IllegalArgumentException(
                    String.format("Unexpected object node (expected array): %s (referenced from %s)", node, getSources()));
        }
        return streamOf(jsonSource.withArray(field))
                .map(x -> replaceText(x.asText()).trim())
                .map(x -> replacement.apply(x))
                .filter(x -> !x.isBlank())
                .collect(Collectors.toList());
    }

    default String joinAndReplace(JsonNode jsonSource, String field) {
        JsonNode node = jsonSource.get(field);
        if (node == null || node.isNull()) {
            return "";
        } else if (node.isTextual()) {
            return node.asText();
        } else if (node.isObject()) {
            throw new IllegalArgumentException(
                    String.format("Unexpected object node (expected array): %s (referenced from %s)", node, getSources()));
        }
        return joinAndReplace((ArrayNode) node);
    }

    default String joinAndReplace(ArrayNode array) {
        List<String> list = new ArrayList<>();
        array.forEach(v -> list.add(replaceText(v.asText())));
        return String.join(", ", list);
    }

    default String getTextOrEmpty(JsonNode x, String field) {
        if (x.has(field)) {
            return x.get(field).asText();
        }
        return "";
    }

    default String getTextOrDefault(JsonNode x, String field, String value) {
        if (x.has(field)) {
            return x.get(field).asText();
        }
        return value;
    }

    default String getOrEmptyIfEqual(JsonNode x, String field, String expected) {
        if (x.has(field)) {
            String value = x.get(field).asText().trim();
            return value.equalsIgnoreCase(expected) ? "" : value;
        }
        return "";
    }

    default boolean booleanOrDefault(JsonNode source, String key, boolean value) {
        JsonNode result = source.get(key);
        return result == null ? value : result.asBoolean(value);
    }

    default BigInteger bigIntegerOrDefault(JsonNode source, String key, Integer value) {
        JsonNode result = source.get(key);
        return result == null
                ? (value == null ? null : BigInteger.valueOf(value))
                : BigInteger.valueOf(result.asLong());
    }

    default int intOrDefault(JsonNode source, String key, int value) {
        JsonNode result = source.get(key);
        return result == null ? value : result.asInt();
    }

    default boolean isReprinted(JsonNode jsonSource) {
        return isReprinted(getIndex(), getSources().key, jsonSource);
    }

    default SizeEnum getSize(JsonNode value) {
        JsonNode size = value.get("size");
        if (size == null) {
            throw new IllegalArgumentException("Missing size attribute from " + getSources());
        }
        try {
            if (size.isTextual()) {
                return SizeEnum.fromValue(size.asText());
            } else if (size.isArray()) {
                String merged = streamOf((ArrayNode) size).map(JsonNode::asText).collect(Collectors.joining());
                return SizeEnum.fromValue(merged);
            }
        } catch (IllegalArgumentException ignored) {
        }
        Log.errorf("Unable to parse size for %s from %s", getSources(), size.toPrettyString());
        return SizeEnum.MEDIUM;
    }

    default int getSpeed(JsonNode value) {
        JsonNode speed = value.get("speed");
        try {
            if (speed == null || speed.isTextual()) {
                return 30;
            } else if (speed.isIntegralNumber()) {
                return speed.asInt();
            } else if (speed.has("walk")) {
                return speed.get("walk").asInt();
            }
        } catch (IllegalArgumentException ignored) {
        }
        Log.errorf("Unable to parse speed for %s from %s", getSources(), speed);
        return 30;
    }

    default void appendEntryToText(List<String> text, JsonNode entry) {
        appendEntryToText(text, entry, new ArrayList<>());
    }

    default void appendEntryToText(List<String> text, JsonNode entry, final Collection<String> diceRolls) {
        if (entry == null) {
            return;
        }
        if (entry.isTextual()) {
            text.add(replaceText(entry.asText(), diceRolls));
        } else if (entry.isArray()) {
            entry.elements().forEachRemaining(f -> {
                appendEntryToText(text, f, diceRolls);
                maybeAddBlankLine(text);
            });
        } else if (entry.isObject()) {
            String objectType = entry.get("type").asText();
            switch (objectType) {
                case "entry":
                case "entries":
                case "itemSpell":
                case "item": {
                    List<String> inner = new ArrayList<>();
                    appendEntryToText(inner, entry.get("entry"), diceRolls);
                    appendEntryToText(inner, entry.get("entries"), diceRolls);
                    if (prependField(entry, "name", inner)) {
                        maybeAddBlankLine(text);
                    }
                    text.addAll(inner);
                    break;
                }
                case "link": {
                    text.add(entry.get("text").asText());
                }
                case "list": {
                    maybeAddBlankLine(text);
                    appendList(text, entry, diceRolls);
                    break;
                }
                case "table": {
                    maybeAddBlankLine(text);
                    appendTable(text, entry, diceRolls);
                    break;
                }
                case "abilityDc":
                    text.add(String.format("Spell save DC: 8 + your proficiency bonus + your %s modifier",
                            asAbilityEnum(entry.withArray("attributes").get(0))));
                    break;
                case "abilityAttackMod":
                    text.add(String.format("Spell attack modifier: your proficiency bonus + your %s modifier",
                            asAbilityEnum(entry.withArray("attributes").get(0))));
                    break;
                case "refClassFeature": {
                    appendClassFeature(text, "classFeature", JsonIndex.IndexType.classfeature,
                            entry);
                    break;
                }
                case "refSubclassFeature": {
                    appendClassFeature(text, "subclassFeature", JsonIndex.IndexType.subclassfeature,
                            entry);
                    break;
                }
                case "refOptionalfeature":
                    appendOptionalFeature(text, entry);
                    break;
                case "options": {
                    appendOptions(text, entry);
                    break;
                }
                case "inline": {
                    List<String> inner = new ArrayList<>();
                    appendEntryToText(inner, entry.get("entries"), diceRolls);
                    text.add(String.join("", inner));
                }
                case "quote":
                case "section":
                case "insetReadaloud":
                case "inset":
                    // skip this type
                    break;
                default:
                    Log.errorf("Unknown entry object type %s from %s: %s", objectType, getSources(), entry.toPrettyString());
            }
        } else {
            Log.errorf("Unknown entry type in %s: %s", getSources(), entry.toPrettyString());
        }
    }

    default boolean prependField(JsonNode entry, String fieldName, List<String> inner) {
        if (entry.has(fieldName)) {
            String n = entry.get(fieldName).asText();
            if (inner.isEmpty()) {
                inner.add(n);
            } else {
                n = n.trim().replace(":", "");
                if (isMarkdown()) {
                    n = "**" + n + ".** ";
                } else {
                    n += ": ";
                }
                inner.set(0, n + inner.get(0));
                return true;
            }
        }
        return false;
    }

    default void prependText(String prefix, List<String> inner) {
        if (inner.isEmpty()) {
            inner.add(prefix);
        } else {
            if (inner.get(0).isEmpty() && inner.size() > 1) {
                inner.set(1, prependText(prefix, inner.get(1)));
            } else {
                inner.set(0, prependText(prefix, inner.get(0)));
            }
        }
    }

    default String prependText(String prefix, String text) {
        return text.startsWith(prefix) ? text : prefix + text;
    }

    default void appendList(List<String> text, JsonNode entry, final Collection<String> diceRolls) {
        appendList("items", text, entry, diceRolls);
    }

    default void appendList(String fieldName, List<String> text, JsonNode entry) {
        appendList(fieldName, text, entry, new ArrayList<>());
    }

    default void appendList(String fieldName, List<String> text, JsonNode entry, final Collection<String> diceRolls) {
        entry.withArray(fieldName).forEach(e -> {
            List<String> listText = new ArrayList<>();
            appendEntryToText(listText, e, diceRolls);
            if (listText.size() > 0) {
                prependText(li(), listText);
                text.addAll(listText);
            }
        });
    }

    default void appendTable(List<String> text, JsonNode entry, final Collection<String> diceRolls) {
        StringBuilder table = new StringBuilder();
        if (entry.has("caption")) {
            if (isMarkdown()) {
                table.append("**");
            }
            table.append(entry.get("caption").asText());
            if (isMarkdown()) {
                table.append("**");
            }
            table.append(":\n\n");
        }
        String header = StreamSupport.stream(entry.withArray("colLabels").spliterator(), false)
                .map(x -> replaceText(x.asText(), diceRolls))
                .collect(Collectors.joining(" | "));

        if (isMarkdown()) {
            header = ("| " + header + " |")
                    .replaceAll("^(d[0-9]+.*)", "dice: $1");
            table.append(header).append("\n");
            table.append(header.replaceAll("[^|]", "-")).append("\n");
        } else {
            table.append(header).append("\n");
        }

        entry.withArray("rows").forEach(r -> table
                .append(isMarkdown() ? "| " : "")
                .append(StreamSupport.stream(r.spliterator(), false)
                        .map(x -> replaceText(x.asText(), diceRolls))
                        .collect(Collectors.joining(" | ")))
                .append(isMarkdown() ? " |\n" : "\n"));

        if (isMarkdown()) {
            String heading = entry.get("colLabels").toString().toLowerCase();
            if (heading.contains("scam")) {
                table.append("^scam").append("\n");
            } else if (heading.contains("defining event")) {
                table.append("^defining-event").append("\n");
            } else if (heading.contains("guild business")) {
                table.append("^guild-business").append("\n");
            } else if (heading.contains("personality trait")) {
                table.append("^personality-trait").append("\n");
            } else if (heading.contains("ideal")) {
                table.append("^ideal").append("\n");
            } else if (heading.contains("bond")) {
                table.append("^bond").append("\n");
            } else if (heading.contains("flaw")) {
                table.append("^flaw").append("\n");
            } else if (entry.has("caption")) {
                table.append("^").append(MarkdownWriter.slugifier().slugify(entry.get("caption").asText())).append("\n");
            }
        }
        maybeAddBlankLine(text);
        text.add(table.toString());
    }

    default void appendOptions(List<String> text, JsonNode entry) {
        List<String> list = new ArrayList<>();
        entry.withArray("entries").forEach(e -> {
            List<String> item = new ArrayList<>();
            appendEntryToText(item, e);
            if (item.size() > 0) {
                prependText(li(), item);
                list.addAll(item);
            }
        });
        if (list.size() > 0) {
            maybeAddBlankLine(text);
            text.add("Options:");
            text.addAll(list);
            maybeAddBlankLine(text);
        }
    }

    default void maybeAddBlankLine(List<String> text) {
        if (text.size() > 0 && !text.get(text.size() - 1).isBlank()) {
            text.add("");
        }
    }

    default void blankBeforeList(List<String> text) {
        if (text.size() <= 1) {
            return;
        }
        if (!text.get(text.size() - 1).startsWith(li()) && !text.get(text.size() - 1).isBlank()) {
            text.add("");
        }
    }

    default void appendOptionalFeature(List<String> text, JsonNode entry) {
        JsonIndex index = getIndex();
        String finalKey = index.getRefKey(JsonIndex.IndexType.optionalfeature, entry.get("optionalfeature").asText());
        if (index.keyIsExcluded(finalKey)) {
            return;
        }

        JsonNode ref = index.getNode(finalKey);
        if (ref == null) {
            Log.errorf("Could not find %s from %s", finalKey, getSources());
        } else if (index.sourceIncluded(ref.get("source").asText())) {
            CompendiumSources featureSources = index.constructSources(JsonIndex.IndexType.optionalfeature, ref);
            text.add(decoratedFeatureTypeName(featureSources, "", ref));
        }
    }

    default void appendClassFeature(List<String> text, String field,
            JsonIndex.IndexType type, JsonNode entry) {
        JsonIndex index = getIndex();
        String finalKey = index.getRefKey(type, entry.get(field).asText());
        JsonNode ref = index.getNode(finalKey);
        if (ref == null) {
            Log.errorf("Could not find %s from %s", finalKey, getSources());
            return;
        }
        CompendiumSources featureSources = index.constructSources(type, ref);
        if (index.keyIsExcluded(featureSources.getKey())) {
            Log.debugf("Excluded: ", getSources());
            return;
        }
        String subclassShortName = getTextOrEmpty(ref, "subclassShortName");
        blankBeforeList(text);
        text.add(li() + decoratedFeatureTypeName(featureSources, subclassShortName, ref));
    }

    default void getFluffDescription(JsonNode value, JsonIndex.IndexType fluffType,
            final List<String> text) {
        JsonIndex index = getIndex();
        if (booleanOrDefault(value, "hasFluff", false)) {
            JsonNode fluffNode = index.getNode(fluffType, value);
            if (fluffNode != null) {
                fluffNode = index.handleCopy(fluffType, fluffNode);
                if (fluffNode.has("entries")) {
                    appendEntryToText(text, fluffNode.get("entries"));
                }
            }
        }
    }

    default void addAdditionalEntries(JsonNode jsonElement, List<String> text, Collection<String> diceRolls, String altSource) {
        jsonElement.withArray("additionalEntries").forEach(entry -> {
            if (entry.has("source") && !getIndex().sourceIncluded(entry.get("source").asText())) {
                return;
            } else if (!getIndex().sourceIncluded(altSource)) {
                return;
            }
            appendEntryToText(text, entry, diceRolls);
        });
    }

    default int chooseSkillListFrom(JsonNode choose, Collection<String> skillList) {
        int count = choose.has("count")
                ? choose.get("count").asInt()
                : 1;

        ArrayNode from = choose.withArray("from");
        from.forEach(s -> skillList.add(s.asText()));
        return count;
    }

    default List<String> jsonToSkillsList(JsonNode listNode) {
        if (listNode == null || listNode.isNull()) {
            return List.of();
        }
        if (listNode.isTextual()) {
            return List.of(listNode.asText());
        }
        List<String> list = new ArrayList<>();
        if (listNode.isObject() && !listNode.toString().contains("choose")) {
            listNode.fieldNames().forEachRemaining(x -> SkillOrAbility.fromTextValue(x).value());
        } else if (listNode.isArray()) {
            listNode.elements().forEachRemaining(f -> {
                List<String> inner = jsonToSkillsList(f);
                if (!inner.isEmpty()) {
                    list.addAll(inner);
                }
            });
        }
        return list;
    }

    default String jsonToSkillString(JsonNode listNode) {
        return String.join(", ", jsonToSkillsList(listNode));
    }

    default String jsonArrayObjectToSkillBonusString(JsonNode jsonSource, String field) {
        if (isMarkdown() && getTextOrEmpty(jsonSource, "lineage").equals("VRGR")) {
            if (getTextOrEmpty(jsonSource, "lineage").equals("VRGR")) {
                // Custom Lineage:
                return "Choose one of: (a) Choose any +2, choose any other +1; (b) Choose any +1, choose any other +1, choose any other +1";
            }
        }
        JsonNode ability = jsonSource.withArray(field);
        if (ability.isEmpty()) {
            return isMarkdown() ? "None" : "";
        }
        if (isMarkdown()) {
            List<String> list = new ArrayList<>();
            // We can describe more conditions on this path
            ability.elements().forEachRemaining(skillBonus -> {
                List<String> inner = new ArrayList<>();
                skillBonus.fields().forEachRemaining(f -> {
                    if (f.getKey().equals("choose")) {
                        inner.add(skillChoices(f.getValue()));
                    } else {
                        inner.add(String.format("%s %s", asAbilityEnum(f.getKey()), decoratedAmount(f.getValue().asInt())));
                    }
                });
                list.add(String.join(", ", inner));
            });
            return String.join("; or ", list);
        } else if (ability.size() >= 1 && !ability.get(0).toString().contains("choose")) {
            // XML syntax isn't built for variety. Just one flavor.
            List<String> list = new ArrayList<>();
            JsonNode skillBonus = ability.get(0);
            skillBonus.fields()
                    .forEachRemaining(f -> list.add(String.format("%s %s", f.getKey(), decoratedAmount(f.getValue().asInt()))));
            return String.join(", ", list);
        }
        return isMarkdown() ? "Unknown" : "";
    }

    default String skillChoices(JsonNode skillBonus) {
        int count = skillBonus.has("count")
                ? skillBonus.get("count").asInt()
                : 1;
        int amount = skillBonus.has("amount")
                ? skillBonus.get("amount").asInt()
                : 1;
        ArrayNode from = skillBonus.withArray("from");
        List<String> choices = new ArrayList<>();
        from.forEach(s -> choices.add(asAbilityEnum(s.asText())));
        return String.format("Apply %s to %s of %s",
                decoratedAmount(amount),
                count == 1 ? "one" : count + " (distinct)",
                String.join(", ", choices));
    }

    default String decoratedAmount(int amount) {
        if (amount >= 0) {
            return "+" + amount;
        }
        return amount + "";
    }

    default String jsonObjectToSkillBonusString(JsonNode listNode) {
        return String.join(", ", jsonObjectToSkillBonusList(listNode));
    }

    default List<String> jsonObjectToSkillBonusList(JsonNode listNode) {
        List<String> list = new ArrayList<>();
        if (listNode != null) {
            listNode.fields().forEachRemaining(f -> {
                if (f.getValue().isTextual()) {
                    list.add(String.format("%s %s",
                            SkillOrAbility.properValue(f.getKey()),
                            f.getValue().asText()));
                } else if (f.getKey().equals("other")) {
                    List<String> items = jsonObjectToSkillBonusList(f.getValue().get(0).get("oneOf"));
                    list.add("One of " + String.join(", ", items));
                } else {
                    throw new IllegalArgumentException(
                            "Unknown skill or ability field " + f + " referenced from " + getSources());
                }
            });
        }
        return list;
    }

    default List<QuteTrait> collectTraitsFromEntries(String properName, JsonNode value) {
        return collectTraitsFromEntries(properName, value, () -> List.of());
    }

    default List<QuteTrait> collectTraitsFromEntries(String properName, JsonNode value, Supplier<List<String>> fluff) {
        List<QuteTrait> traits = new ArrayList<>();
        List<String> text = new ArrayList<>();
        Set<String> diceRolls = new HashSet<>();

        value.withArray("entries").forEach(entry -> {
            if (entry.isTextual()) {
                text.add(replaceText(entry.asText(), diceRolls));
            } else if (entry.isObject()) {
                if (entry.has("type") && "list".equals(entry.get("type").asText())) {
                    appendList(text, entry, diceRolls);
                    maybeAddBlankLine(text);
                } else {
                    QuteTrait trait = createTrait(entry);
                    traits.add(trait);
                }
            }
        });

        text.addAll(fluff.get());

        if (text.size() > 0) {
            QuteTrait baseTrait = createTrait(properName, text, diceRolls);
            traits.add(0, baseTrait);
        }
        return traits;
    }

    default List<QuteTrait> collectTraits(JsonNode array) {
        if (array == null || array.isNull()) {
            return List.of();
        }
        if (array.isObject()) {
            throw new IllegalArgumentException("Lost our way: tried collecting traits from an object: " + getSources());
        }
        List<QuteTrait> traits = new ArrayList<>();
        array.forEach(entry -> traits.add(createTrait(entry)));
        return traits;
    }

    default QuteTrait createTrait(JsonNode jsonElement) {
        QuteTrait.Builder builder = new QuteTrait.Builder();
        List<String> traitText = new ArrayList<>();
        Set<String> diceRolls = new HashSet<>();
        String traitName = "";

        if (jsonElement.has("name")) {
            final Pattern rechargePattern = Pattern.compile("\\{@recharge *(\\d*).*\\}");
            Matcher m = rechargePattern.matcher(jsonElement.get("name").asText());
            traitName = m.replaceAll((match) -> {
                if (match.group(1).length() > 0) {
                    builder.setRecharge("D" + match.group(1));
                    return "(Recharge " + match.group(1) + "-6)";
                }
                builder.setRecharge("D6");
                return "(Recharge 6)";
            });

            traitName = replaceText(traitName).replaceAll(":$", "");
        }

        builder.setName(traitName);
        if (jsonElement.has("entries")) {
            appendEntryToText(traitText, jsonElement.get("entries"), diceRolls);
        } else if (jsonElement.has("entry")) {
            appendEntryToText(traitText, jsonElement.get("entry"), diceRolls);
        }
        builder.addText(traitText);
        builder.addDiceRolls(diceRolls);
        // TODO: Proficiency ?
        return builder.build();
    }

    default QuteTrait createTrait(String traitName, List<String> text, Collection<String> diceRolls) {
        return new QuteTrait.Builder()
                .setName(traitName)
                .addText(text)
                .addDiceRolls(diceRolls)
                .build();
    }

    default List<Modifier> collectAbilityModifiers(JsonNode value) {
        JsonNode abilityElement = value.get("ability");
        String type = value.toString().contains("score increases") ? "Score" : "Modifier";
        if (abilityElement == null) {
            return List.of();
        }
        if (abilityElement.has("static")) {
            return List.of();
        }
        if (abilityElement.has("choose")) {
            return collectChooseAbilityModifiers(type, abilityElement.get("choose"));
        }
        if (abilityElement.has("from")) {
            return collectChooseAbilityModifiers(type, abilityElement);
        }
        List<Modifier> modifiers = new ArrayList<>();
        abilityElement.fields().forEachRemaining(entry -> {
            String ability = asAbilityEnum(entry.getKey());
            String amount = entry.getValue().asText();
            if (!amount.startsWith("-") && !amount.startsWith("+")) {
                amount = "+" + amount;
            }
            modifiers.add(new Modifier(
                    String.format("%s %s", ability, amount),
                    ModifierCategoryEnum.fromValue("Ability " + type)));
        });
        return modifiers;
    }

    default List<Modifier> collectChooseAbilityModifiers(String type, JsonNode choose) {
        if (!choose.has("amount")) {
            return List.of();
        }
        String amount = choose.get("amount").asText();
        if (!amount.startsWith("-") && !amount.startsWith("+")) {
            amount = "+" + amount;
        }
        int count = choose.has("count")
                ? choose.get("count").asInt()
                : 1;

        ArrayNode from = choose.withArray("from");

        List<Modifier> modifiers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String ability = asAbilityEnum(from.get(i));
            modifiers.add(new Modifier(
                    String.format("%s %s", ability, amount),
                    ModifierCategoryEnum.fromValue("Ability " + type)));
        }
        return modifiers;
    }

    default String raceToText(JsonNode race) {
        StringBuilder str = new StringBuilder();
        str.append(race.get("name").asText());
        if (race.has("subrace")) {
            str.append(" (").append(race.get("subrace").asText()).append(")");
        }
        return str.toString();
    }

    default String levelToText(JsonNode levelNode) {
        if (levelNode.isObject()) {
            List<String> levelText = new ArrayList<>();
            levelText.add(levelToText(levelNode.get("level").asText()));
            if (levelNode.has("class") || levelNode.has("subclass")) {
                JsonNode classNode = levelNode.get("class");
                if (classNode == null) {
                    classNode = levelNode.get("subclass");
                }
                boolean visible = !classNode.has("visible") || classNode.get("visible").asBoolean();
                JsonNode source = classNode.get("source");
                boolean included = source == null || getIndex().sourceIncluded(source.asText());
                if (visible && included) {
                    levelText.add(classNode.get("name").asText());
                }
            }
            return String.join(" ", levelText);
        } else {
            return levelToText(levelNode.asText());
        }
    }

    default String levelToText(String level) {
        switch (level) {
            case "1":
                return "1st level";
            case "2":
                return "2nd level";
            case "3":
                return "3rd level";
            default:
                return level + "th level";
        }
    }

    default String decoratedTypeName(CompendiumSources sources) {
        return decoratedTypeName(sources.name, sources);
    }

    default String decoratedTypeName(String name, CompendiumSources sources) {
        if (sources.isPrimarySource("DMG") && !name.contains("(DMG)")) {
            return name + " (DMG)";
        }
        if (sources.isFromUA() && !name.contains("(UA)")) {
            return name + " (UA)";
        }
        return name;
    }

    default String decoratedFeatureTypeName(CompendiumSources valueSources, String subclassTitle, JsonNode value) {
        String name = decoratedTypeName(value.get("name").asText(), valueSources);
        String type = getTextOrEmpty(value, "featureType");

        if (!type.isEmpty()) {
            switch (type) {
                case "ED":
                    return "Elemental Discipline: " + name;
                case "EI":
                    return "Eldritch Invocation: " + name;
                case "MM":
                    return "Metamagic: " + name;
                case "MV":
                case "MV:B":
                case "MV:C2-UA":
                    return "Maneuver: " + name;
                case "FS:F":
                case "FS:B":
                case "FS:R":
                case "FS:P":
                    return "Fighting Style: " + name;
                case "AS":
                case "AS:V1-UA":
                case "AS:V2-UA":
                    return "Arcane Shot: " + name;
                case "PB":
                    return "Pact Boon: " + name;
                case "AI":
                    return "Artificer Infusion: " + name;
                case "SHP:H":
                case "SHP:M":
                case "SHP:W":
                case "SHP:F":
                case "SHP:O":
                    return "Ship Upgrade: " + name;
                case "IWM:W":
                    return "Infernal War Machine Variant: " + name;
                case "IWM:A":
                case "IWM:G":
                    return "Infernal War Machine Upgrade: " + name;
                case "OR":
                    return "Onomancy Resonant: " + name;
                case "RN":
                    return "Rune Knight Rune: " + name;
                case "AF":
                    return "Alchemical Formula: " + name;
                default:
                    Log.errorf("Unknown feature type %s for class feature %s", type, name);
            }
        }

        if (subclassTitle != null && !subclassTitle.isBlank()) {
            return subclassTitle + ": " + name;
        }
        return name;
    }

    default String asAbilityEnum(JsonNode textNode) {
        return asAbilityEnum(textNode.asText());
    }

    default String asAbilityEnum(String ability) {
        switch (ability) {
            case "str":
                return "Strength";
            case "dex":
                return "Dexterity";
            case "con":
                return "Constitution";
            case "int":
                return "Intelligence";
            case "wis":
                return "Wisdom";
            case "cha":
                return "Charisma";
        }
        return ability;
    }

    default String replaceDiceTxt(String input, Collection<String> diceRolls) {
        String result = input;
        Matcher m;

        if (result.contains("{@atk")) {
            String attack = result
                    .replaceAll("\\(\\{@(damage|dice) ([^}]+)\\}\\)", "($2)")
                    .replaceAll("\\{@(damage|dice) ([^}]+)\\}", "($2)");
            String toHit = attack.replaceAll(".*\\{@hit ([^}]+)\\}.*", "$1");
            if (toHit.matches("\\d+") && result.contains("{@damage")) {
                m = alternate.matcher(attack);
                if (m.find()) {
                    // alternate attacks
                    diceRolls.add(String.format("|+%s|%s%n", toHit, String.format("(%s) %s", m.group(1), m.group(3))
                            .replaceAll(additive, "($1 + $2)") // combine additive rolls
                            .replaceAll(onlyDamage, "$1")
                            .replaceAll(" ", "")));
                    diceRolls.add(String.format("|+%s|%s%n", toHit, String.format("(%s) %s", m.group(2), m.group(3))
                            .replaceAll(additive, "($1 + $2)") // combine additive rolls
                            .replaceAll(onlyDamage, "$1")
                            .replaceAll(" ", "")));
                } else {
                    diceRolls.add(String.format("|+%s|%s%n", toHit, attack
                            .replaceAll(additive, "($1 + $2)") // combine additive rolls
                            .replaceAll(onlyDamage, "$1")
                            .replaceAll(" ", "")));
                }
            }
            result = dicePattern.matcher(result).replaceAll(match -> match.group(2));
        } else {
            m = dicePattern.matcher(result);
            result = m.replaceAll((match) -> {
                diceRolls.add(match.group(2));
                return match.group(2);
            });
        }

        m = chancePattern.matcher(result);
        result = m.replaceAll((match) -> {
            diceRolls.add("1d100");
            return match.group(1) + "% chance";
        });

        return result;
    }

    default String replaceAttributes(String input) {
        String result;
        Matcher m;

        // {@item Ball Bearings (Bag of 1,000)|phb|bag of ball bearings}
        // {@item sphere of annihilation}
        // {@item spellbook|phb}
        m = itemPattern.matcher(input);
        result = m.replaceAll((match) -> match.group(1));

        m = spellPattern.matcher(result);
        result = m.replaceAll((match) -> match.group(1)
                + (match.groupCount() > 2 && match.group(2).length() > 0 ? '*' : ""));

        m = spellPattern.matcher(result);
        result = m.replaceAll((match) -> match.group(1)
                + (match.groupCount() > 2 && match.group(2).length() > 0 ? '*' : ""));

        result = result
                .replace("{@hitYourSpellAttack}", "the summoner's spell attack modifier")
                .replaceAll("\\{@link ([^}|]+)\\|([^}]+)}", "$1 ($2)") // this must come first
                .replaceAll("\\{@5etools ([^}|]+)\\|?[^}]*}", "$1")
                .replaceAll("\\{@area ([^|}]+)\\|?[^}]*}", "$1")
                .replaceAll("\\{@action ([^}]+)\\|?[^}]*}", "$1")
                .replaceAll("\\{@creature([^|}]+)\\|?[^}]*}", "$1")
                .replaceAll("\\{@condition ([^|}]+)\\|?[^}]*}", "$1")
                .replaceAll("\\{@disease ([^|}]+)\\|?[^}]*}", "$1")
                .replaceAll("\\{@hazard ([^|}]+)\\|?[^}]*}", "$1")
                .replaceAll("\\{@reward ([^|}]+)\\|?[^}]*}", "$1")
                .replaceAll("\\{@dc ([^}]+)}", "DC $1")
                .replaceAll("\\{@d20 ([^}]+?)}", "$1")
                .replaceAll("\\{@recharge ([^}]+?)}", "(Recharge $1-6)")
                .replaceAll("\\{@recharge}", "(Recharge 6)")
                .replaceAll("\\{@filter ([^|}]+)\\|?[^}]*}", "$1")
                .replaceAll("\\{@classFeature ([^|}]+)\\|?[^}]*}", "$1")
                .replaceAll("\\{@optfeature ([^|}]+)\\|?[^}]*}", "$1")
                .replaceAll("\\{@cult ([^|}]+)\\|([^|}]+)\\|[^|}]*}", "$2")
                .replaceAll("\\{@cult ([^|}]+)\\|[^}]*}", "$1")
                .replaceAll("\\{@deity ([^|}]+)\\|?[^}]*}", "$1")
                .replaceAll("\\{@language ([^|}]+)\\|?[^}]*}", "$1")
                .replaceAll("\\{@quickref ([^|}]+)\\|?[^}]*}", "$1")
                .replaceAll("\\{@table ([^|}]+)\\|?[^}]*}", "$1")
                .replaceAll("\\{@variantrule ([^|}]+)\\|?[^}]*}", "$1")
                .replaceAll("\\{@book ([^}|]+)\\|?[^}]*}", "\"$1\"")
                .replaceAll("\\{@hit ([^}]+)}", "+$1")
                .replaceAll("\\{@h}", "Hit: ")
                .replaceAll("\\{@atk m}", isMarkdown() ? "*Melee Attack:*" : "Melee Attack:")
                .replaceAll("\\{@atk mw}", isMarkdown() ? "*Melee Weapon Attack:*" : "Melee Weapon Attack:")
                .replaceAll("\\{@atk rw}", isMarkdown() ? "*Ranged Weapon Attack:*" : "Ranged Weapon Attack:")
                .replaceAll("\\{@atk mw,rw}",
                        isMarkdown() ? "*Melee or Ranged Weapon Attack:*" : "Melee or Ranged Weapon Attack:")
                .replaceAll("\\{@atk ms}", isMarkdown() ? "*Melee Spell Attack:*" : "Melee Spell Attack:")
                .replaceAll("\\{@atk rs}", isMarkdown() ? "*Ranged Spell Attack:*" : "Ranged Spell Attack:")
                .replaceAll("\\{@atk ms,rs}",
                        isMarkdown() ? "*Melee or Ranged Spell Attack:*" : "Melee or Ranged Spell Attack:")
                .replaceAll("\\{@skill ([^}]+)}", "$1")
                .replaceAll("\\{@note (\\*|Note:)?\\s?([^}]+)}", "✧ $2");

        if (isMarkdown()) {
            result = result
                    .replaceAll("\\{@b ([^}]+?)}", "**$1**")
                    .replaceAll("\\{@i ([^}]+?)}", "_$1_")
                    .replaceAll("\\{@italic ([^}]+)}", "_$1_")
                    .replaceAll("\\{@condition ([^|}]+)\\|?[^}]*}", "[$1](" + JsonIndex.rulesRoot() + "conditions.md#$1)")
                    .replaceAll("\\{@sense ([^}]+)}", "[$1](" + JsonIndex.rulesRoot() + "senses.md#$1))");

            m = Pattern.compile("\\{@background ([^|}]+)\\|?[^}]*}").matcher(result);
            result = m.replaceAll((match) -> String.format("[%s](%sbackgrounds/%s.md)",
                    match.group(1), JsonIndex.compendiumRoot(), MarkdownWriter.slugifier().slugify(match.group(1))));

            m = Pattern.compile("\\{@class ([^|}]+)\\|[^|]*\\|?([^|}]*)\\|?[^}]*}").matcher(result);
            result = m.replaceAll((match) -> String.format("[%s](%sclasses/%s.md)",
                    match.group(2), JsonIndex.compendiumRoot(), MarkdownWriter.slugifier().slugify(match.group(1))));

            m = Pattern.compile("\\{@class ([^|}]+)}").matcher(result);
            result = m.replaceAll((match) -> String.format("[%s](%sclasses/%s.md)",
                    match.group(1), JsonIndex.compendiumRoot(), MarkdownWriter.slugifier().slugify(match.group(1))));

            m = Pattern.compile("\\{@feat ([^|}]+)\\|?[^}]*}").matcher(result);
            result = m.replaceAll((match) -> String.format("[%s](%sfeats/%s.md)",
                    match.group(1), JsonIndex.compendiumRoot(), MarkdownWriter.slugifier().slugify(match.group(1))));

            m = Pattern.compile("\\{@item ([^|}]+)\\|?[^}]*}").matcher(result);
            result = m.replaceAll((match) -> String.format("[%s](%sitems/%s.md)",
                    match.group(1), JsonIndex.compendiumRoot(), MarkdownWriter.slugifier().slugify(match.group(1))));

            m = Pattern.compile("\\{@race ([^|}]+)\\|?[^}]*}").matcher(result);
            result = m.replaceAll((match) -> String.format("[%s](%sraces/%s.md)",
                    match.group(1), JsonIndex.compendiumRoot(), MarkdownWriter.slugifier().slugify(match.group(1))));
        } else {
            result = result
                    .replaceAll("\\{@b ([^}]+?)}", "$1")
                    .replaceAll("\\{@i ([^}]+?)}", "$1")
                    .replaceAll("\\{@italic ([^}]+)}", "$1")
                    .replaceAll("\\{@background ([^|}]+)\\|?[^}]*}", "$1")
                    .replaceAll("\\{@class ([^|}]+)\\|[^|]*\\|?([^|}]*)\\|?[^}]*}", "$2") // {@class Class||Usethis|...}
                    .replaceAll("\\{@class ([^|}]+)}", "$1") // {@class Bard}
                    .replaceAll("\\{@feat ([^|}]+)\\|?[^}]*}", "$1")
                    .replaceAll("\\{@item ([^|}]+)\\|?[^}]*}", "$1")
                    .replaceAll("\\{@race ([^|}]+)\\|?[^}]*}", "$1")
                    .replaceAll("\\{@condition ([^|}]+)\\|?[^}]*}", "$1")
                    .replaceAll("\\{@sense ([^}]+)}", "$1");
        }

        // after other replacements
        return result.replaceAll("\\{@adventure ([^|}]+)\\|[^}]*}", "$1");
    }
}
