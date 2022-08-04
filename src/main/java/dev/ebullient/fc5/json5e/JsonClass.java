package dev.ebullient.fc5.json5e;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.pojo.Modifier;
import dev.ebullient.fc5.pojo.ModifierCategoryEnum;
import dev.ebullient.fc5.pojo.Proficiency;
import dev.ebullient.fc5.pojo.QuteClassAutoLevel;
import dev.ebullient.fc5.pojo.QuteClassFeature;
import dev.ebullient.fc5.pojo.SkillOrAbility;

public interface JsonClass extends JsonBase {
    String INFUSIONS_KNOWN_FEAT = "classfeature|infusions known|artificer|tce|2";

    default JsonNode copyAndMergeClass(JsonNode jsonSource) {
        if (jsonSource.has("className") || jsonSource.has("_copy")) {
            jsonSource = getIndex().cloneOrCopy(getSources().getKey(),
                    jsonSource, JsonIndex.IndexType.classtype,
                    getTextOrDefault(jsonSource, "className", null),
                    getTextOrDefault(jsonSource, "classSource", null));
        }
        return jsonSource;
    }

    default boolean isSidekick() {
        return getSources().getName().toLowerCase().contains("sidekick");
    }

    default String getWealth(JsonNode equipment) {
        return replaceText(getTextOrEmpty(equipment, "goldAlternative"))
                .replaceAll("×", "x");
    }

    default List<String> defaultEquipment(JsonNode equipment) {
        List<String> text = new ArrayList<>();
        appendList("default", text, equipment);
        return text;
    }

    default int classSkills(JsonNode source, Collection<String> list, CompendiumSources sources) {
        ArrayNode skillNode = source.withArray("skills");
        if (skillNode.size() > 1) {
            Log.errorf("Multivalue skill array in %s: %s", sources, source.toPrettyString());
        }
        JsonNode skills = skillNode.get(0);
        int count = 2;

        if (skills.has("choose")) {
            count = chooseSkillListFrom(skills.get("choose"), list);
        } else if (skills.has("from")) {
            count = chooseSkillListFrom(skills, list);
        } else if (skills.has("any")) {
            count = skills.get("any").asInt();
            list.addAll(SkillOrAbility.allSkills);
        } else {
            Log.errorf("Unexpected skills in starting proficiencies for %s: %s",
                    sources, source.toPrettyString());
        }
        return count;
    }

    default List<QuteClassAutoLevel> classAutolevels(JsonNode classNode) {
        List<QuteClassAutoLevel.Builder> levels = new ArrayList<>(20);

        for (int lvl = 0; lvl < 20; lvl++) {
            levels.add(new QuteClassAutoLevel.Builder().setLevel(lvl + 1));
        }

        // Primary class spellcasting
        spellcastingSkillProgression(levels, classNode, "classTableGroups", false);

        GroupedClassFeatures classFeatures = getClassFeaturesByLevel(classNode);

        // Warp through starting class information to find _subclass_ spellcasting
        if (getStartingClassAttributes().subclassSpellcasting != null) {
            spellcastingSkillProgression(levels, getStartingClassAttributes().subclassSpellcasting,
                    "subclassTableGroups", true);
        }

        List<QuteClassAutoLevel> finalLevels = new ArrayList<>();
        for (int r = 0; r < 20; r++) {
            final QuteClassAutoLevel.Builder builder = levels.get(r); // group subclasses

            int featureR = r + 1;
            classFeatures.getFeatures(featureR).forEach(f -> {
                builder.addFeature(f);
                if (f.getName().equals("Ability Score Improvement")) {
                    builder.setScoreImprovement(true);
                }
            });
            finalLevels.add(builder.build());
        }

        return finalLevels;
    }

    default void spellcastingSkillProgression(List<QuteClassAutoLevel.Builder> levels, JsonNode classNode,
            String tableGroup, boolean optional) {
        ArrayNode cantrips = classNode.withArray("cantripProgression");
        for (Iterator<JsonNode> i = classNode.withArray(tableGroup).elements(); i.hasNext();) {
            JsonNode x = i.next();
            ArrayNode cols = x.withArray("colLabels");
            ArrayNode rows = x.withArray("rows");

            String[] labels = new String[cols.size()];
            for (int c = 0; c < cols.size(); c++) {
                labels[c] = replaceText(cols.get(c).asText());
            }

            if (x.has("title") && x.get("title").asText().equals("Spell Slots per Spell Level")) {
                constructSpellSlots(rows, cantrips, levels, optional);
            }
            if (x.has("rowsSpellProgression")) {
                ArrayNode spellProgression = x.withArray("rowsSpellProgression");
                constructSpellSlots(spellProgression, cantrips, levels, optional);
            }

            for (int r = 0; r < rows.size(); r++) {
                addCounters(r, rows, cols.size(), labels, levels.get(r));
            }
        }
    }

    default void addCounters(int r, ArrayNode rows, int numCols, String[] labels,
            QuteClassAutoLevel.Builder builder) {
        List<String> knownThings = new ArrayList<>();
        ArrayNode row = (ArrayNode) rows.get(r);
        for (int c = 0; c < numCols; c++) {
            switch (labels[c]) {
                case "Infusions Known": {
                    if (getIndex().keyIsExcluded(INFUSIONS_KNOWN_FEAT)) {
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
                        knownThings.add(String.format("%s: %s", labels[c], count));
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
                        knownThings.add(String.format("%s: %s, resets on long rest", labels[c], count));
                        builder.addCounter(
                                new QuteClassAutoLevel.Counter(labels[c], count, QuteClassAutoLevel.Reset.L));
                    }
                    break;
                }
                case "Ki Points": {
                    // Reset on Short Rest
                    int count = row.get(c).asInt();
                    if (count > 0) {
                        knownThings.add(String.format("%s: %s, resets on short rest", labels[c], count));
                        builder.addCounter(
                                new QuteClassAutoLevel.Counter(labels[c], count, QuteClassAutoLevel.Reset.S));
                    }
                    break;
                }
                case "Martial Arts": {
                    // parse die face
                    JsonNode toRoll = row.get(c).get("toRoll");
                    if (toRoll != null) {
                        int value = toRoll.get(0).get("faces").asInt();
                        if (value > 0) {
                            knownThings.add(String.format("Martial Arts Damage die: %s", value));
                        }
                    }
                    break;
                }
                case "Rage Damage": {
                    // attack damage bonus die
                    int value = row.get(c).get("value").asInt();
                    if (value > 0) {
                        knownThings.add(String.format("Rage Damage Bonus die: %s", value));
                    }
                    break;
                }
                case "Unarmored Movement": {
                    // speed bonus
                    int value = row.get(c).get("value").asInt();
                    if (value > 0 && !isMarkdown()) {
                        int prev = rows.get(r - 1).get(c).get("value").asInt();
                        if (value != prev) {
                            int delta = (value - prev);
                            Modifier speed = new Modifier("Speed +" + delta, ModifierCategoryEnum.BONUS);
                            builder.addFeature(new QuteClassFeature.Builder()
                                    .setName(labels[c] + " (" + (r + 1) + ")")
                                    .addModifier(speed)
                                    .addText(String.format(
                                            "Your speed increases by %s feet %swhile you are not wearing armor or wielding a shield.",
                                            (prev == 0 ? delta + "" : delta + " additional"),
                                            (prev == 0 ? "" : "(" + value + " total) ")))
                                    .build());
                        }
                    }
                    break;
                }
                case "Spell Slots": {
                    // Warlock spell slots act as cantrips kind of
                    builder.setSlots(new QuteClassAutoLevel.SpellSlots(row.get(c).asText()));
                    break;
                }
            }
        }

        if (getSources().getSourceText().equals("Bard")) {
            if (r < 5) {
                knownThings.add(String.format("Bardic Inspiration die: %s", 6));
                // builder.addCounter(new QuteClassAutoLevel.Counter("Bardic die (BI)", 6, null));
            } else if (r < 10) {
                knownThings.add(String.format("Bardic Inspiration die: %s", 8));
                // builder.addCounter(new QuteClassAutoLevel.Counter("Bardic die (BI)", 8, null));
            } else if (r < 15) {
                knownThings.add(String.format("Bardic Inspiration die: %s", 10));
                // builder.addCounter(new QuteClassAutoLevel.Counter("Bardic die (BI)", 10, null));
            } else {
                knownThings.add(String.format("Bardic Inspiration die: %s", 12));
                // builder.addCounter(new QuteClassAutoLevel.Counter("Bardic die (BI)", 12, null));
            }
        }

        if (knownThings.size() > 0 && !isMarkdown()) {
            Collections.sort(knownThings);
            builder.addFeature(new QuteClassFeature.Builder()
                    .setName("Class Features")
                    .setLevel(0)
                    .setOptional(false)
                    .addText(String.join("\n", knownThings))
                    .build());
        }
    }

    private void constructSpellSlots(ArrayNode spellProgression, ArrayNode cantrips, List<QuteClassAutoLevel.Builder> levels,
            boolean optional) {
        for (int r = 0; r < spellProgression.size(); r++) {
            String sb = (cantrips.size() > 0 ? cantrips.get(r).asText() : "0")
                    + ", " + joinAndReplace((ArrayNode) spellProgression.get(r));
            levels.get(r).setSlots(new QuteClassAutoLevel.SpellSlots(sb, optional));
        }
    }

    default GroupedClassFeatures getClassFeaturesByLevel(JsonNode classNode) {
        List<String> hasSubclasses = new ArrayList<>();
        String classSource = classNode.get("source").asText();
        String subclassTitle = getTextOrEmpty(classNode, "subclassTitle");
        GroupedClassFeatures featuresByLevel = new GroupedClassFeatures();

        for (Iterator<JsonNode> i = classNode.withArray("classFeatures").elements(); i.hasNext();) {
            JsonNode f = i.next();
            if (f.isTextual()) {
                addNodeToMap(featuresByLevel, JsonIndex.IndexType.classfeature, f.asText(), null, "");
            } else {
                String level = addNodeToMap(featuresByLevel, JsonIndex.IndexType.classfeature,
                        f.get("classFeature").asText(), null, "");

                if (booleanOrDefault(f, "gainSubclassFeature", false)) {
                    if (level != null && hasSubclasses.isEmpty()) {
                        // subclasses introduced.
                        // Find index elements for the relevant subclasses and add them to the map
                        getIndex().classElementsMatching(JsonIndex.IndexType.subclass,
                                getSources().getName(), classSource)
                                .forEach(x -> addSubclassFeaturesToMap(featuresByLevel, x, subclassTitle));
                    }
                    hasSubclasses.add(level);
                }
            }
        }
        return featuresByLevel;
    }

    default void addSubclassFeaturesToMap(GroupedClassFeatures featuresByLevel,
            JsonNode subclassNode, String subclassTitle) {
        JsonIndex index = getIndex();
        String scKey = index.getKey(JsonIndex.IndexType.subclass, subclassNode);
        subclassNode = index.resolveClassFeatureNode(scKey, subclassNode);
        if (subclassNode == null) {
            return; // skipped or not found
        }
        String grouping = subclassTitle.isEmpty()
                ? subclassNode.get("shortName").asText()
                : String.format("%s: %s", subclassTitle, subclassNode.get("shortName").asText());
        CompendiumSources subclassSources = new CompendiumSources(JsonIndex.IndexType.subclass, scKey, subclassNode);
        // does the subclass have subclass features? add those to the map...
        subclassNode.withArray("subclassFeatures").forEach(f -> {
            if (f.isTextual()) {
                String scf = f.asText();
                addNodeToMap(featuresByLevel, JsonIndex.IndexType.subclassfeature, scf,
                        getPrefix(subclassTitle, subclassSources.getName(), scf), grouping);
            } else {
                Log.errorf("Unexpected subclass feature type for name %s: %s", getSources(), f.toPrettyString());
            }
        });
        if (subclassNode.has("spellcastingAbility")) {
            getStartingClassAttributes().subclassSpellcasting = subclassNode;
        }
    }

    default String addNodeToMap(GroupedClassFeatures featureByLevel, JsonIndex.IndexType type,
            String lookup, String t, String sortingGroup) {
        JsonIndex index = getIndex();

        // "Ability Score Improvement|Paladin||4",
        String level = type == JsonIndex.IndexType.optionalfeature
                ? t.replaceAll("(\\d+)\\|?.*", "$1")
                : lookup.replaceAll(".*\\|(\\d+)\\|?.*", "$1");

        String title = type == JsonIndex.IndexType.optionalfeature
                ? t.replaceAll("\\d+\\|?(.*)", "$1")
                : t;

        String finalKey = index.getRefKey(type, lookup);
        JsonNode featureJson = index.resolveClassFeatureNode(finalKey);
        if (featureJson == null) {
            return null; // skipped or not found
        }
        CompendiumSources featureSources = new CompendiumSources(type, finalKey, featureJson);
        StreamSupport.stream(featureJson.withArray("entries").spliterator(), false)
                .filter(JsonNode::isObject)
                .filter(x -> x.has("type"))
                .flatMap(x -> x.get("type").asText().equals("entries")
                        ? StreamSupport.stream(x.withArray("entries").spliterator(), false)
                        : Stream.of(x))
                .filter(x -> x.has("type"))
                .forEach(node -> {
                    switch (node.get("type").asText()) {
                        case "refClassFeature": {
                            String cf = node.get("classFeature").asText();
                            addNodeToMap(featureByLevel, JsonIndex.IndexType.classfeature, cf, null, sortingGroup);
                            break;
                        }
                        case "refSubclassFeature": {
                            String scf = node.get("subclassFeature").asText();
                            addNodeToMap(featureByLevel, JsonIndex.IndexType.subclassfeature, scf,
                                    getPrefix(title, featureSources.getName(), scf), sortingGroup);
                            break;
                        }
                        case "refOptionalfeature": {
                            String of = node.get("optionalfeature").asText();
                            addNodeToMap(featureByLevel, JsonIndex.IndexType.optionalfeature, of,
                                    level + "|" + getPrefix(title, featureSources.getName(), of), sortingGroup);
                            break;
                        }
                    }
                });

        featureByLevel.add(level, buildClassFeature(featureSources, featureJson, title, level, sortingGroup));
        return level;
    }

    default QuteClassFeature buildClassFeature(CompendiumSources sources, JsonNode featureJson, String title, String level,
            String sortingGroup) {
        QuteClassFeature.Builder builder = new QuteClassFeature.Builder()
                .setLevel(Integer.parseInt(level))
                .setGroup(sortingGroup)
                .setName(decoratedFeatureTypeName(sources, title, featureJson))
                .addText(classFeatureText(sources, featureJson))
                .setSubclassTitle(title);

        if (sources.getName().startsWith("Unarmored Defense")) {
            String content = featureJson.get("entries").toString();
            if (content.contains("Constitution modifier")) {
                builder.addSpecial("Unarmored Defense: Constitution");
            } else if (content.contains("Wisdom modifier")) {
                builder.addSpecial("Unarmored Defense: Wisdom");
            } else if (content.contains("Charisma modifier")) {
                builder.addSpecial("Unarmored Defense: Charisma");
            } else {
                Log.errorf("Unhandled Unarmored Defense for %s: %s", sources, content);
            }
        } else if (SPECIAL.contains(sources.getName())) {
            builder.addSpecial(sources.getName());
        }

        if (sources.type.isOptional() || sources.isFromUA()) {
            builder.setOptional(true);
        }

        if (isSidekick() && "1".equals(level) && sources.getName().equals("Bonus Proficiencies")) {
            getStartingClassAttributes().sidekickProficiencies(featureJson);
        }

        return builder.build();
    }

    default List<String> classFeatureText(CompendiumSources sources, JsonNode value) {
        List<String> text = new ArrayList<>();
        try {
            value.withArray("entries").forEach(entry -> appendEntryToText(text, entry));
            maybeAddBlankLine(text);
        } catch (Exception e) {
            Log.errorf(e, "Unable to parse text for %s", sources);
        }
        text.add("Source: " + sources.getSourceText());
        return text;
    }

    default String getPrefix(String subclassTitle, String featureName, String lookupKey) {
        String revised = featureName.replace("(UA)", "").trim();
        if (lookupKey.startsWith(revised)) {
            return subclassTitle;
        }
        return revised;
    }

    default StartingClass getStartingClassAttributes() {
        return null;
    }

    class StartingClass implements JsonClass {
        final CompendiumSources sources;
        final JsonIndex index;
        final String name;
        Map<String, List<String>> startingText = new HashMap<>();
        boolean additionalFromBackground;
        Proficiency proficiency;

        boolean isMarkdown;

        JsonNode subclassSpellcasting;

        public StartingClass(JsonIndex index, CompendiumSources sources, String name, boolean isMarkdown) {
            this.index = index;
            this.sources = sources;
            this.name = name;
            this.isMarkdown = isMarkdown;
        }

        @Override
        public JsonIndex getIndex() {
            return index;
        }

        @Override
        public CompendiumSources getSources() {
            return sources;
        }

        @Override
        public boolean isMarkdown() {
            return isMarkdown;
        }

        public String findStartingEquipment(JsonNode classNode) {
            String wealth = null;
            JsonNode equipment = classNode.get("startingEquipment");
            if (equipment != null) {
                wealth = getWealth(equipment);
                put("wealth", List.of(wealth));
                put("equipment", defaultEquipment(equipment));
                additionalFromBackground = booleanOrDefault(equipment, "additionalFromBackground", true);
            }
            return wealth;
        }

        public void sidekickProficiencies(JsonNode sidekickClassFeature) {
            Proficiency.Builder pb = new Proficiency.Builder();
            sidekickClassFeature.withArray("entries").forEach(e -> {
                String line = e.asText();
                if (line.contains("saving throw")) {
                    //"The sidekick gains proficiency in one saving throw of your choice: Dexterity, Intelligence, or Charisma.",
                    //"The sidekick gains proficiency in one saving throw of your choice: Wisdom, Intelligence, or Charisma.",
                    //"The sidekick gains proficiency in one saving throw of your choice: Strength, Dexterity, or Constitution.",
                    String text = line.replaceAll(".*in one saving throw of your choice: (.*)", "$1")
                            .replaceAll("or ", "").replace(".", "");
                    put("saves", List.of(text));
                    pb.addSkills(List.of(text.split("\\s*,\\s*")));
                }
                if (line.contains("skills")) {
                    // "In addition, the sidekick gains proficiency in five skills of your choice, and it gains proficiency with light armor. If it is a humanoid or has a simple or martial weapon in its stat block, it also gains proficiency with all simple weapons and with two tools of your choice."
                    // "In addition, the sidekick gains proficiency in two skills of your choice from the following list: {@skill Arcana}, {@skill History}, {@skill Insight}, {@skill Investigation}, {@skill Medicine}, {@skill Performance}, {@skill Persuasion}, and {@skill Religion}.",
                    // "In addition, the sidekick gains proficiency in two skills of your choice from the following list: {@skill Acrobatics}, {@skill Animal Handling}, {@skill Athletics}, {@skill Intimidation}, {@skill Nature}, {@skill Perception}, and {@skill Survival}.",
                    String numSkills = line.replaceAll(".* proficiency in (.*) skills .*", "$1");
                    int count = Integer.parseInt(textToInt(numSkills));
                    put("numSkills", List.of(count + ""));

                    Collection<String> skills;
                    int start = line.indexOf("list:");
                    if (start >= 0) {
                        int end = line.indexOf('.');
                        String text = line.substring(start + 5, end).trim()
                                .replaceAll("\\{@skill ([^}]+)}", "$1")
                                .replace(".", "")
                                .replace("and ", "");
                        skills = Set.of(text.split("\\s*,\\s*"));
                    } else {
                        skills = SkillOrAbility.allSkills;
                    }
                    put("skills", List.of(skillChoices(skills, count)));
                    pb.addSkills(skills);
                }
                if (line.contains("armor")) {
                    // "In addition, the sidekick gains proficiency in five skills of your choice, and it gains proficiency with light armor. If it is a humanoid or has a simple or martial weapon in its stat block, it also gains proficiency with all simple weapons and with two tools of your choice."
                    // "The sidekick gains proficiency with light armor, and if it is a humanoid or has a simple or martial weapon in its stat block, it also gains proficiency with all simple weapons."
                    // "The sidekick gains proficiency with all armor, and if it is a humanoid or has a simple or martial weapon in its stat block, it gains proficiency with shields and all simple and martial weapons."
                    if (line.contains("all armor")) { // Warrior Sidekick
                        put("armor", List.of("light, medium, heavy, shields"));
                        put("weapons", List.of("martial"));
                    } else {
                        put("armor", List.of("light"));
                        put("weapons", List.of("simple"));
                    }
                }
                if (line.contains("tools")) {
                    put("tools", List.of("two tools of your choice"));
                }
            });
            this.proficiency = pb.build();
        }

        public int classHitDice(JsonNode classNode) {
            JsonNode hd = classNode.get("hd");
            if (hd != null) {
                put("hd", List.of(hd.get("faces").asText()));
                return hd.get("faces").asInt();
            }
            return 0;
        }

        public void findClassProficiencies(JsonNode classNode) {
            Proficiency.Builder pb = new Proficiency.Builder();
            if (classNode.has("proficiency")) {
                classNode.withArray("proficiency").forEach(n -> pb.addSkill(asAbilityEnum(n)));
            }

            JsonNode startingProf = classNode.get("startingProficiencies");
            if (startingProf == null) {
                Log.errorf("%s has no starting proficiencies", sources);
            } else {
                if (startingProf.has("armor")) {
                    put("armor", findAndReplace(startingProf, "armor", s -> s.replace("shield", "shields")));
                }
                if (startingProf.has("weapons")) {
                    put("weapons", findAndReplace(startingProf, "weapons"));
                }
                if (startingProf.has("tools")) {
                    put("tools", findAndReplace(startingProf, "tools"));
                }
                if (startingProf.has("skills")) {
                    Set<String> set = new HashSet<>();
                    int count = classSkills(startingProf, set, sources);
                    put("numSkills", List.of(count + ""));

                    if (count == SkillOrAbility.allSkills.size()) { // any
                        pb.addSkills(SkillOrAbility.allSkills);
                        set.addAll(SkillOrAbility.allSkills);
                    }
                    pb.addSkills(set);
                    put("skills", List.of(skillChoices(set, count)));
                }
            }
            this.proficiency = pb.build();
        }

        public Proficiency getProficiency() {
            return proficiency;
        }

        public String getArmor() {
            return joinOrDefault("armor", "none");
        }

        public String getWeapons() {
            return joinOrDefault("weapons", "none");
        }

        public String getTools() {
            return joinOrDefault("tools", "none");
        }

        public String getNumSkills() {
            return joinOrDefault("numSkills", "0");
        }

        public String getSkills() {
            return proficiency.toText();
        }

        public String getHitDice() {
            return joinOrDefault("hd", "0");
        }

        public void put(String key, List<String> value) {
            startingText.put(key, value);
        }

        public List<QuteClassFeature> buildStartingClassFeatures(JsonNode classNode) {
            List<String> text = new ArrayList<>();
            text.add(String.format("You are proficient with the following items%s.",
                    additionalFromBackground
                            ? ", in addition to any proficiencies provided by your race or background"
                            : ""));
            maybeAddBlankLine(text);
            if (startingText.containsKey("saves")) {
                text.add(String.format(isMarkdown() ? "%s**Saving Throws:** %s" : "%sSaving Throws: %s",
                        li(), joinOrDefault("saves", "none")));
            }
            text.add(String.format(isMarkdown() ? "%s**Armor:** %s" : "%sArmor: %s",
                    li(), joinOrDefault("armor", "none")));
            text.add(String.format(isMarkdown() ? "%s**Weapons:** %s" : "%sWeapons: %s",
                    li(), joinOrDefault("weapons", "none")));
            text.add(String.format(isMarkdown() ? "%s**Tools:** %s" : "%sTools: %s",
                    li(), joinOrDefault("tools", "none")));
            text.add(li() + joinOrDefault("skills", String.format(isMarkdown() ? "**Skills:** %s" : "Skills: %s", "none")));

            if (!isSidekick()) {
                maybeAddBlankLine(text);
                text.add(String.format("You begin play with the following equipment%s.",
                        additionalFromBackground ? ", in addition to any equipment provided by your background" : ""));
                maybeAddBlankLine(text);
                List<String> equipment = startingText.get("equipment");
                if (equipment == null) {
                    text.add(li() + "None");
                } else {
                    text.addAll(equipment);
                }
                maybeAddBlankLine(text);
                text.add(String.format("Alternatively, you may start with %s gp and choose your own equipment.",
                        joinOrDefault("wealth", "3d4 x 10"))); // middle/sorcerer
            }

            maybeAddBlankLine(text);
            text.add("Source: " + sources.getSourceText());

            QuteClassFeature starting = new QuteClassFeature.Builder()
                    .setName(String.format("Starting %s", name))
                    .setOptional(true)
                    .addText(text)
                    .build();

            JsonNode multiclassing = classNode.get("multiclassing");
            if (multiclassing != null) {
                return List.of(starting, getMulticlassingFeature(multiclassing));
            }
            return List.of(starting);
        }

        QuteClassFeature getMulticlassingFeature(JsonNode multiclassing) {
            final List<String> text = new ArrayList<>();

            text.add(String.format("To multiclass as a %s, you must meet the following prerequisites:", name));
            maybeAddBlankLine(text);
            JsonNode requirements = multiclassing.with("requirements");
            if (requirements.has("or")) {
                List<String> options = new ArrayList<>();
                requirements.get("or").get(0).fields().forEachRemaining(
                        ability -> options.add(String.format("%s %s", li(),
                                asAbilityEnum(ability.getKey()), ability.getValue().asText())));
                text.add(String.format("%s%s", li(), String.join(", or ", options)));
            } else {
                requirements.fields().forEachRemaining(
                        ability -> text.add(String.format("%s%s %s", li(),
                                asAbilityEnum(ability.getKey()), ability.getValue().asText())));
            }

            JsonNode gained = multiclassing.get("proficienciesGained");
            if (gained != null) {
                maybeAddBlankLine(text);
                text.add("You gain the following proficiencies:");
                maybeAddBlankLine(text);

                text.add(String.format(isMarkdown() ? "%s**Armor:** %s" : "%sArmor: %s",
                        li(), joinOrDefault(gained, "armor", "none", s -> s.replace("shield", "shields"))));
                text.add(String.format(isMarkdown() ? "%s**Weapons:** %s" : "%sWeapons: %s",
                        li(), joinOrDefault(gained, "weapons", "none")));
                text.add(String.format(isMarkdown() ? "%s**Tools:** %s" : "%sTools: %s",
                        li(), joinOrDefault(gained, "tools", "none")));

                if (gained.has("skills")) {
                    List<String> list = new ArrayList<>();
                    int count = classSkills(gained, list, sources);
                    text.add(li() + skillChoices(list, count));
                }
            }
            maybeAddBlankLine(text);
            text.add("Source: " + sources.getSourceText());
            return new QuteClassFeature.Builder()
                    .setName(String.format("Multiclassing %s", name))
                    .setOptional(true)
                    .addText(text)
                    .build();
        }

        String skillChoices(Collection<String> skills, int numSkills) {
            return String.format(isMarkdown() ? "**Skills:** Choose %s from %s" : "Skills: Choose %s from %s",
                    numSkills,
                    skills.stream().map(SkillOrAbility::fromTextValue)
                            .sorted(Comparator.comparingInt(Enum::ordinal))
                            .map(x -> isMarkdown() ? "*" + x.value() + "*" : x.value())
                            .collect(Collectors.joining(", ")));
        }

        String joinOrDefault(String field, String value) {
            List<String> text = startingText.get(field);
            return text == null ? value : String.join(", ", text);
        }

        String joinOrDefault(JsonNode source, String field, String value) {
            return joinOrDefault(source, field, value, s -> s);
        }

        String joinOrDefault(JsonNode source, String field, String value, Function<String, String> replacements) {
            List<String> text = findAndReplace(source, field, replacements);
            return text == null || text.isEmpty() ? value : String.join(", ", text);
        }

        private String textToInt(String text) {
            switch (text) {
                case "two":
                    return "2";
                case "three":
                    return "3";
                case "five":
                    return "5";
                default:
                    Log.errorf("Unknown number of skills (%s) listed in sidekick class features (%s)", text, sources);
                    return "1";
            }
        }

        public boolean hasSubclassSpellcasting() {
            return subclassSpellcasting != null;
        }

        public JsonNode spellAbility() {
            return subclassSpellcasting.get("spellcastingAbility");
        }
    }

    class GroupedClassFeatures {
        Map<String, Set<QuteClassFeature>> features = new HashMap<>();

        public void add(String level, QuteClassFeature feature) {
            features.computeIfAbsent(level,
                    k -> new TreeSet<>(QuteClassFeature.alphabeticalFeatureSort)).add(feature);
        }

        public void add(int level, QuteClassFeature feature) {
            add(level + "", feature);
        }

        public Set<QuteClassFeature> getFeatures(int level) {
            return getFeatures(level + "");
        }

        public Set<QuteClassFeature> getFeatures(String level) {
            Set<QuteClassFeature> result = features.get(level);
            return result == null ? Set.of() : result;
        }
    }
}
