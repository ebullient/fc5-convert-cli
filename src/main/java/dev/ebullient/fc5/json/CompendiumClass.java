package dev.ebullient.fc5.json;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.StreamSupport;

import javax.xml.bind.JAXBElement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import dev.ebullient.fc5.Import5eTools;
import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.data.SkillEnum;
import dev.ebullient.fc5.json.JsonIndex.IndexType;
import dev.ebullient.fc5.xml.XmlAutolevelType;
import dev.ebullient.fc5.xml.XmlClassType;
import dev.ebullient.fc5.xml.XmlCounterType;
import dev.ebullient.fc5.xml.XmlFeatureType;
import dev.ebullient.fc5.xml.XmlModifierType;
import dev.ebullient.fc5.xml.XmlObjectFactory;
import dev.ebullient.fc5.xml.XmlResetEnum;
import dev.ebullient.fc5.xml.XmlSlotsType;

public class CompendiumClass extends CompendiumBase {
    final static String INFUSIONS_KNOWN_FEAT = "classfeature|infusions known|artificer|tce|2";

    String name;
    XmlClassType fc5Class;
    List<JAXBElement<?>> attributes;
    CompendiumSources sources;
    Map<String, List<String>> startingText = new HashMap<>();
    boolean additionalFromBackground;

    public CompendiumClass(String key, JsonIndex index, XmlObjectFactory factory) {
        super(key, index, factory);
    }

    public XmlClassType getXmlCompendiumObject() {
        return fc5Class;
    }

    @Override
    public boolean convert(JsonNode classNode) {
        this.sources = new CompendiumSources(key, classNode);
        this.fc5Class = factory.createClassType();
        this.attributes = fc5Class.getNameOrHdOrProficiency();
        this.name = classNode.get("name").asText();

        if (index.excludeElement(key, classNode, sources)) {
            return false; // do not include
        }
        if (classNode.has("isReprinted")) {
            Log.debugf("Skipping %s (has been reprinted)", name);
            return false; // the reprint will be used instead of this one.
        }

        if (classNode.has("className") || classNode.has("_copy")) {
            classNode = cloneOrCopy(classNode);
        }

        attributes.add(factory.createClassTypeName(decoratedTypeName(name, sources)));
        addClassHitDice(classNode);
        addClassProficiencies(classNode);
        addClassSpellAbility(classNode);
        addClassWealth(classNode);
        addStartingLevel(classNode);
        addClassAutoLevels(classNode);
        return true;
    }

    private void addClassWealth(JsonNode classNode) {
        JsonNode equipment = classNode.get("startingEquipment");
        if (equipment != null) {
            String wealth = replaceText(getTextOrEmpty(equipment, "goldAlternative"))
                    .replaceAll("×", "x");
            attributes.add(factory.createClassTypeWealth(wealth.replaceAll(" ", "")));
            startingText.put("wealth", List.of(wealth));

            additionalFromBackground = booleanOrDefault(equipment, "additionalFromBackground", true);
            List<String> text = new ArrayList<>();
            appendList(name, "default", text, equipment);
            startingText.put("equipment", text);
        }
    }

    private void addClassHitDice(JsonNode classNode) {
        JsonNode hd = classNode.get("hd");
        if (hd != null) {
            attributes.add(factory.createClassTypeHd(BigInteger.valueOf(hd.get("faces").asInt())));
            startingText.put("hd", List.of(hd.get("faces").asText()));
        }
    }

    private void addClassSpellAbility(JsonNode classNode) {
        JsonNode ability = classNode.get("spellcastingAbility");
        if (ability != null) {
            attributes.add(factory.createClassTypeSpellAbility(asAbilityEnum(ability)));
            if (classNode.toString().contains("all expended spell slots when you finish a short")) {
                attributes.add(factory.createClassTypeSlotsReset(XmlResetEnum.S));
            } else {
                attributes.add(factory.createClassTypeSlotsReset(XmlResetEnum.L));
            }
        }
    }

    private void addClassProficiencies(JsonNode value) {
        List<String> abilitySkills = new ArrayList<>();
        if (value.has("proficiency")) {
            value.withArray("proficiency").forEach(n -> abilitySkills.add(asAbilityEnum(n)));
        }

        JsonNode startingProf = value.get("startingProficiencies");
        if (startingProf == null) {
            Log.errorf("%s has no starting proficiencies", name);
        } else {
            if (startingProf.has("armor")) {
                String armor = getArmor(startingProf);
                attributes.add(factory.createClassTypeArmor(armor));
                startingText.put("armor", List.of(armor));
            }
            if (startingProf.has("weapons")) {
                String weapons = getWeapons(startingProf);
                attributes.add(factory.createClassTypeWeapons(weapons));
                startingText.put("weapons", List.of(weapons));
            }
            if (startingProf.has("tools")) {
                String tools = getTools(startingProf);
                attributes.add(factory.createClassTypeTools(tools));
                startingText.put("tools", List.of(tools));
            }
            if (startingProf.has("skills")) {
                List<String> list = new ArrayList<>();
                int count = getSkills(startingProf, list);
                attributes.add(factory.createClassTypeNumSkills(count + ""));

                if (count == SkillEnum.allSkills.size()) { // any
                    startingText.put("skills", List.of("Choose any " + count));
                } else {
                    startingText.put("skills",
                            List.of(String.format("Choose %s from %s", count, String.join(", ", list))));
                }

                abilitySkills.addAll(list);
            }
        }

        if (!abilitySkills.isEmpty()) {
            attributes.add(factory.createClassTypeProficiency(String.join(", ", abilitySkills)));
        }
    }

    void addClassAutoLevels(JsonNode classNode) {
        ArrayNode cantrips = classNode.withArray("cantripProgression");
        List<XmlAutolevelType> levels = new ArrayList<>(20);
        for (int lvl = 0; lvl < 20; lvl++) {
            XmlAutolevelType autoLevel = factory.createAutolevelType();
            autoLevel.setLevel(BigInteger.valueOf(lvl + 1));
            levels.add(autoLevel);
        }

        classNode.withArray("classTableGroups").forEach(x -> {
            ArrayNode spellProgression = x.withArray("rowsSpellProgression");
            ArrayNode cols = x.withArray("colLabels");
            ArrayNode rows = x.withArray("rows");

            String[] labels = new String[cols.size()];
            for (int c = 0; c < cols.size(); c++) {
                labels[c] = replaceText(cols.get(c).asText());
            }

            for (int r = 0; r < spellProgression.size(); r++) {
                StringBuilder sb = new StringBuilder();
                sb.append(cantrips.size() > 0
                        ? cantrips.get(r).asText()
                        : "0").append(", ");
                sb.append(joinAndReplace((ArrayNode) spellProgression.get(r)));

                XmlSlotsType slots = factory.createSlotsType();
                slots.setValue(sb.toString());

                List<JAXBElement<?>> content = levels.get(r).getContent();
                content.add(factory.createAutolevelTypeSlots(slots));
            }

            for (int r = 0; r < rows.size(); r++) {
                ArrayNode row = (ArrayNode) rows.get(r);
                List<JAXBElement<?>> content = levels.get(r).getContent();

                for (int c = 0; c < cols.size(); c++) {
                    switch (labels[c]) {
                        case "Infusions Known": {
                            if (index.keyIsExcluded(INFUSIONS_KNOWN_FEAT)) {
                                break;
                            }
                        }
                        case "Cantrips Known":
                        case "Disciplines Known":
                        case "Invocations Known":
                        case "Maneuvers Known":
                        case "Psi Limit":
                        case "Spells Known":
                        case "Talents Known": {
                            // No reset, just a number
                            int count = row.get(c).asInt();
                            if (count > 0) {
                                XmlCounterType counter = createCounter(labels[c], count, null);
                                content.add(factory.createAutolevelTypeCounter(counter));
                            }
                            break;
                        }
                        case "Rages":
                        case "Infused Items":
                        case "Psi Points":
                        case "Sorcery Points": {
                            // Reset on Long Rest
                            int count = row.get(c).asInt();
                            if (count > 0) {
                                XmlCounterType counter = createCounter(labels[c], count, XmlResetEnum.L);
                                content.add(factory.createAutolevelTypeCounter(counter));
                            }
                            break;
                        }
                        case "Ki Points": {
                            // Reset on Short Rest
                            int count = row.get(c).asInt();
                            if (count > 0) {
                                XmlCounterType counter = createCounter(labels[c], count, XmlResetEnum.S);
                                content.add(factory.createAutolevelTypeCounter(counter));
                            }
                            break;
                        }
                        case "Unarmed (MA) Damage": {
                            // parse die face
                            JsonNode toRoll = row.get(c).get("toRoll");
                            if (toRoll != null) {
                                int value = toRoll.get("faces").asInt();
                                if (value > 0) {
                                    XmlCounterType counter = createCounter(labels[c], value, null);
                                    content.add(factory.createAutolevelTypeCounter(counter));
                                }
                            }
                            break;
                        }
                        case "Rage Damage": {
                            // attack damage bonus die
                            int value = row.get(c).get("value").asInt();
                            if (value > 0) {
                                XmlCounterType counter = createCounter("Rage Damage Bonus", value, null);
                                content.add(factory.createAutolevelTypeCounter(counter));
                            }
                            break;
                        }
                        case "Unarmored Movement": {
                            // speed bonus
                            int value = row.get(c).get("value").asInt();
                            if (value > 0) {
                                int prev = rows.get(r - 1).get(c).get("value").asInt();
                                if (value != prev) {
                                    int delta = (value - prev);
                                    XmlModifierType speed = new XmlModifierType("Speed +" + delta, "bonus");

                                    XmlFeatureType feature = factory.createFeatureType();
                                    feature.getNameOrTextOrSpecial()
                                            .add(factory.createFeatTypeName(labels[c] + " (" + (r + 1) + ")"));
                                    feature.getNameOrTextOrSpecial().add(factory.createFeatureTypeModifier(speed));
                                    feature.getNameOrTextOrSpecial().add(factory.createFeatureTypeText(String.format(
                                            "Your speed increases by %s feet %swhile you are not wearing armor or wielding a shield.",
                                            (prev == 0 ? delta + "" : delta + " additional"),
                                            (prev == 0 ? "" : "(" + value + " total) "))));

                                    // add the feature to autolevel content
                                    content.add(factory.createAutolevelTypeFeature(feature));
                                }
                            }
                            break;
                        }
                        case "Spell Slots": {
                            // Warlock spell slots act as cantrips kind of
                            XmlSlotsType slots = factory.createSlotsType();
                            slots.setValue(row.get(c).asText());
                            content.add(factory.createAutolevelTypeSlots(slots));
                            break;
                        }
                    }
                }

                if (name.equals("Bard")) {
                    final XmlCounterType counter;
                    if (r < 5) {
                        counter = createCounter("Bardic die (BI)", 6, null);
                    } else if (r < 10) {
                        counter = createCounter("Bardic die (BI)", 8, null);
                    } else if (r < 15) {
                        counter = createCounter("Bardic die (BI)", 10, null);
                    } else {
                        counter = createCounter("Bardic die (BI)", 12, null);
                    }
                    content.add(factory.createAutolevelTypeCounter(counter));
                }
            }

        });

        Map<String, Set<CompendiumClassFeature>> classFeatures = getClassFeaturesByLevel(classNode);

        for (int r = 0; r < 20; r++) {
            final XmlAutolevelType autolevel = levels.get(r);
            List<JAXBElement<?>> content = autolevel.getContent();

            Set<CompendiumClassFeature> levelFeatures = classFeatures.get((r + 1) + "");
            if (levelFeatures != null) {
                levelFeatures.forEach(f -> {
                    content.add(factory.createAutolevelTypeFeature(f.getXmlCompendiumObject()));
                    if (f.name.equals("Ability Score Improvement")) {
                        autolevel.setScoreImprovement("YES");
                    }
                });
            }

            if (content.size() > 0) {
                // add autolevel to the class
                attributes.add(factory.createClassTypeAutolevel(autolevel));
            }
        }
    }

    private Map<String, Set<CompendiumClassFeature>> getClassFeaturesByLevel(JsonNode classNode) {
        List<String> hasSubclasses = new ArrayList<>();
        String classSource = classNode.get("source").asText();
        String subclassTitle = getTextOrEmpty(classNode, "subclassTitle");
        Map<String, Set<CompendiumClassFeature>> featuresByLevel = new HashMap<>();

        classNode.withArray("classFeatures").forEach(f -> {
            if (f.isTextual()) {
                addNodeToMap(featuresByLevel, IndexType.classfeature, f.asText(), null);
            } else {
                String level = addNodeToMap(featuresByLevel, IndexType.classfeature,
                        f.get("classFeature").asText(), null);

                if (booleanOrDefault(f, "gainSubclassFeature", false)) {
                    if (level != null && hasSubclasses.isEmpty()) {
                        // subclasses introduced.
                        // Find index elements for the relevant subclasses and add them to the map
                        index.classElementsMatching(IndexType.subclass, name, classSource)
                                .forEach(x -> addSubclassToMap(featuresByLevel, level, x, subclassTitle));
                    }
                    hasSubclasses.add(level);
                }
            }
        });
        return featuresByLevel;
    }

    private void addSubclassToMap(Map<String, Set<CompendiumClassFeature>> featuresByLevel, String level,
            JsonNode subclassNode, String subclassTitle) {
        String scKey = index.getKey(IndexType.subclass, subclassNode);
        CompendiumClassFeature feature = new CompendiumClassFeature(scKey, index, factory, IndexType.subclass, subclassTitle);

        if (feature.convert(subclassNode)) {
            // does the subclass have subclass features? add those to the map...
            subclassNode.withArray("subclassFeatures").forEach(f -> {
                if (f.isTextual()) {
                    String scf = f.asText();
                    addNodeToMap(featuresByLevel, IndexType.subclassfeature, scf, getPrefix(subclassTitle, feature.name, scf));
                } else {
                    Log.errorf("Unexpected subclass feature type for name %s: %s", name, f.toPrettyString());
                }
            });
        }
    }

    private String getPrefix(String subclassTitle, String featureName, String lookupKey) {
        String revised = featureName.replace("(UA)", "").trim();
        if (lookupKey.startsWith(revised)) {
            return subclassTitle;
        }
        return revised;
    }

    private String addNodeToMap(Map<String, Set<CompendiumClassFeature>> featureByLevel, IndexType type, String lookup,
            String title) {
        // "Ability Score Improvement|Paladin||4",
        String level = lookup.replaceAll(".*\\|(\\d+)\\|?.*", "$1");
        String finalKey = index.getRefKey(type, lookup);
        JsonNode value = index.getNode(finalKey);
        if (value != null) {
            CompendiumClassFeature feature = new CompendiumClassFeature(finalKey, index, factory, type, title);
            if (feature.convert(value)) {
                featureByLevel
                        .computeIfAbsent(level, k -> new TreeSet<>((a, b) -> a.fc5ClassFeature.compareTo(b.fc5ClassFeature)))
                        .add(feature);

                StreamSupport.stream(value.withArray("entries").spliterator(), false)
                        .filter(x -> x.isObject())
                        .filter(x -> x.has("type"))
                        .forEach(node -> {
                            switch (node.get("type").asText()) {
                                case "refClassFeature": {
                                    String cf = node.get("classFeature").asText();
                                    addNodeToMap(featureByLevel, IndexType.classfeature, cf, null);
                                    break;
                                }
                                case "refSubclassFeature": {
                                    String scf = node.get("subclassFeature").asText();
                                    addNodeToMap(featureByLevel, IndexType.subclassfeature, scf,
                                            getPrefix(title, feature.name, scf));
                                    break;
                                }
                                case "refOptionalfeature": {
                                    String of = node.get("optionalfeature").asText();
                                    addNodeToMap(featureByLevel, IndexType.optionalfeature, of, null);
                                    break;
                                }
                            }
                        });

                return level;
            }
            return null; // skip this
        } else {
            Log.errorf("Unable to find %s in Index (referenced from %s)", finalKey, name);
            return null;
        }
    }

    XmlCounterType createCounter(String counterName, int value, XmlResetEnum reset) {
        XmlCounterType counter = factory.createCounterType();
        List<Object> attrs = counter.getNameOrValueOrReset();
        attrs.add(counterName);
        attrs.add(BigInteger.valueOf(value));
        if (reset != null) {
            attrs.add(reset);
        }
        return counter;
    }

    JsonNode cloneOrCopy(JsonNode value) {
        JsonNode classNode = index.getNode(IndexType.classtype,
                value.get("className").asText(),
                value.get("classSource").asText());

        JsonNode copyNode = index.getNode(IndexType.classtype, value.get("_copy"));
        try {
            if (classNode == null) {
                value = mergeClassNodes(copyNode, value);
            } else if (copyNode == null) {
                value = mergeClassNodes(classNode, value);
            } else {
                // base type first
                JsonNode mergeNode = mergeClassNodes(classNode, copyNode);
                value = mergeClassNodes(mergeNode, value);
                Log.errorf("Check my work: %s is a copy of %s based on %s",
                        name, getTextOrEmpty(copyNode, "name"), getTextOrEmpty(classNode, "name"));
            }
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to resolve class " + name);
        }
        return value;
    }

    JsonNode mergeClassNodes(JsonNode baseNode, JsonNode value) throws JsonMappingException, JsonProcessingException {
        ObjectNode mergedNode = (ObjectNode) Import5eTools.MAPPER.readTree(baseNode.toString());
        mergedNode.put("merged", true);
        mergedNode.remove("srd");
        return null;
    }

    void addStartingLevel(JsonNode classNode) {
        XmlAutolevelType autoLevel = factory.createAutolevelType();
        autoLevel.setLevel(BigInteger.ONE);
        List<JAXBElement<?>> content = autoLevel.getContent();

        List<String> text = new ArrayList<>();
        text.add(String.format("You are proficient with the following items%s.",
                additionalFromBackground ? ", in addition to any proficiencies provided by your race or background"
                        : ""));
        text.add(String.format("%sArmor: %s", LI, textOrDefault("armor", "none")));
        text.add(String.format("%sWeapons: %s", LI, textOrDefault("weapons", "none")));
        text.add(String.format("%sTools: %s", LI, textOrDefault("tools", "none")));
        text.add(String.format("%sSkills: %s", LI, textOrDefault("skills", "none")));
        text.add("");
        text.add(String.format("You begin play with the following equipment%s.",
                additionalFromBackground ? ", in addition to any equipment provided by your background" : ""));
        List<String> equipment = startingText.get("equipment");
        if (equipment == null) {
            text.add(LI + "None");
        } else {
            text.addAll(equipment);
        }
        maybeAddBlankLine(text);
        text.add(String.format("Alternatively, you may start with %s gp and choose your own equipment.",
                textOrDefault("wealth", "3d4 x 10"))); // middle/sorcerer
        maybeAddBlankLine(text);
        text.add("Source: " + sources.getSourceText());

        XmlFeatureType feature = factory.createFeatureType();
        feature.setOptional("YES");

        List<JAXBElement<?>> fAttr = feature.getNameOrTextOrSpecial();
        fAttr.add(factory.createFeatureTypeName(String.format("Starting %s", name)));
        text.stream().forEach(line -> fAttr.add(factory.createFeatureTypeText(line)));

        content.add(factory.createAutolevelTypeFeature(feature));

        JsonNode multiclassing = classNode.get("multiclassing");
        if (multiclassing != null) {
            addMulticlassing(multiclassing, content);
        }
        attributes.add(factory.createClassTypeAutolevel(autoLevel));
    }

    void addMulticlassing(JsonNode multiclassing, List<JAXBElement<?>> content) {
        final List<String> t2 = new ArrayList<>();

        t2.add(String.format("To multiclass as a %s, you must meet the following prerequisites:", name));
        multiclassing.with("requirements").fields().forEachRemaining(ability -> {
            t2.add(String.format("%s%s %s", LI, asAbilityEnum(ability.getKey()), ability.getValue().asText()));
        });

        JsonNode gained = multiclassing.get("proficienciesGained");
        if (gained != null) {
            t2.add("");
            t2.add("You gain the following proficiencies:");
            if (gained.has("armor")) {
                t2.add(LI + "Armor: " + getArmor(gained));
            }
            if (gained.has("weapons")) {
                t2.add(LI + "Weapons: " + getWeapons(gained));
            }
            if (gained.has("tools")) {
                t2.add(LI + "Tools: " + getTools(gained));
            }
            if (gained.has("skills")) {
                List<String> list = new ArrayList<>();
                int count = getSkills(gained, list);
                t2.add(String.format("%sSkills: Choose %s from %s",
                        LI, count, String.join(", ", list)));
            }
        }

        XmlFeatureType f2 = factory.createFeatureType();
        f2.setOptional("YES");

        List<JAXBElement<?>> f2Attr = f2.getNameOrTextOrSpecial();
        f2Attr.add(factory.createFeatureTypeName(String.format("Multiclassing %s", name)));
        t2.stream().forEach(line -> f2Attr.add(factory.createFeatureTypeText(line)));
        f2Attr.add(factory.createFeatureTypeText("Source: " + sources.getSourceText()));

        content.add(factory.createAutolevelTypeFeature(f2));
    }

    String textOrDefault(String field, String value) {
        List<String> text = startingText.get(field);
        return text == null ? value : String.join("\n", text);
    }

    String getArmor(JsonNode source) {
        return joinAndReplace(source.withArray("armor"))
                .replace("shield", "shields");
    }

    String getWeapons(JsonNode source) {
        return joinAndReplace(source.withArray("weapons"));
    }

    String getTools(JsonNode source) {
        return joinAndReplace(source.withArray("tools"));
    }

    int getSkills(JsonNode source, List<String> list) {
        ArrayNode skillNode = source.withArray("skills");
        if (skillNode.size() > 1) {
            Log.errorf("Multivalue skill array in %s: %s", name, source.toPrettyString());
        }
        JsonNode skills = skillNode.get(0);

        int count = 2;

        if (skills.has("choose")) {
            count = chooseSkillListFrom(skills.get("choose"), list);
        } else if (skills.has("from")) {
            count = chooseSkillListFrom(skills, list);
        } else if (skills.has("any")) {
            count = skills.get("any").asInt();
            list.addAll(SkillEnum.allSkills);
        } else {
            Log.errorf("Unexpected skills in starting proficiencies for %s: %s",
                    name, source.toPrettyString());
        }

        return count;
    }
}
