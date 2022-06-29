package dev.ebullient.fc5.json5e;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.pojo.ItemEnum;
import dev.ebullient.fc5.pojo.MarkdownWriter;
import dev.ebullient.fc5.pojo.Proficiency;
import dev.ebullient.fc5.pojo.PropertyEnum;
import dev.ebullient.fc5.pojo.QuteBackground;
import dev.ebullient.fc5.pojo.QuteClass;
import dev.ebullient.fc5.pojo.QuteClassAutoLevel;
import dev.ebullient.fc5.pojo.QuteFeat;
import dev.ebullient.fc5.pojo.QuteItem;
import dev.ebullient.fc5.pojo.QuteMonster;
import dev.ebullient.fc5.pojo.QuteRace;
import dev.ebullient.fc5.pojo.QuteSource;
import dev.ebullient.fc5.pojo.QuteSpell;
import dev.ebullient.fc5.pojo.QuteTrait;
import dev.ebullient.fc5.pojo.SkillOrAbility;

public class Json2MarkdownConverter {
    final JsonIndex index;
    final MarkdownWriter writer;

    public Json2MarkdownConverter(JsonIndex index, MarkdownWriter writer) {
        this.index = index;
        this.writer = writer;
    }

    public Json2MarkdownConverter writeFiles(JsonIndex.IndexType type, String title) {
        String prefix = type + "|";
        Map<String, JsonNode> variants = StreamSupport.stream(index.elements().spliterator(), false)
                .filter(e -> e.getKey().startsWith(prefix))
                .flatMap(e -> findVariants(type, e.getKey(), e.getValue()))
                .filter(e -> index.keyIsIncluded(e.getKey()))
                .filter(e -> !JsonBase.isReprinted(index, e.getKey(), e.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        List<QuteSource> nodes = new ArrayList<>();
        for (JsonNode e : variants.values()) {
            QuteSource converted = json2qute(type, e);
            if (converted != null) {
                nodes.add(converted);
            }
        }

        try {
            writer.writeFiles(nodes, title);
        } catch (IOException e) {
            Log.outPrintln("⛔️ Exception: " + e.getCause().getMessage());
        }
        return this;
    }

    Stream<Map.Entry<String, JsonNode>> findVariants(JsonIndex.IndexType type, String key, JsonNode jsonSource) {
        if (type == JsonIndex.IndexType.race) {
            Map<String, JsonNode> variants = new HashMap<>();
            variants.put(key, jsonSource);
            CompendiumSources sources = index.constructSources(type, jsonSource);
            index.subraces(sources).forEach(sr -> {
                CompendiumSources srSources = index.constructSources(JsonIndex.IndexType.subrace, sr);
                variants.put(srSources.getKey(), sr);
            });
            return variants.entrySet().stream();
        }
        return Map.of(key, jsonSource).entrySet().stream();
    }

    private QuteSource json2qute(JsonIndex.IndexType type, JsonNode jsonNode) {
        switch (type) {
            case background:
                jsonNode = index.handleCopy(JsonIndex.IndexType.background, jsonNode);
                return new Json2QuteBackground(jsonNode).build();
            case classtype:
                return new Json2QuteClass(jsonNode).build();
            case feat:
                // no use of _copy
                return new Json2QuteFeat(jsonNode).build();
            case item:
                jsonNode = index.handleCopy(JsonIndex.IndexType.classtype, jsonNode);
                return new Json2QuteItem(jsonNode).build();
            case monster:
                jsonNode = index.handleCopy(JsonIndex.IndexType.monster, jsonNode);
                return new Json2QuteMonster(jsonNode).build();
            case race:
                // unique race copy/merge
                return new Json2QuteRace(jsonNode).build();
            case spell:
                // no use of _copy
                return new Json2QuteSpell(jsonNode).build();
        }
        throw new IllegalArgumentException("Unsupported type " + type);
    }

    // NOT STATIC CLASSES -- they share/reuse the index

    class Json2QuteBackground extends Json2QuteCommon implements JsonBackground {
        final QuteBackground.Builder builder;

        public Json2QuteBackground(JsonNode jsonSource) {
            super(JsonIndex.IndexType.background, jsonSource);
            String backgroundName = decoratedTypeName(decoratedBackgroundName(getName()), sources);
            this.builder = new QuteBackground.Builder()
                    .setName(backgroundName);

            List<QuteTrait> traits = collectTraitsFromEntries("Description", jsonSource,
                    () -> getDescription(jsonSource));

            builder.setTraits(traits);
        }

        QuteBackground build() {
            return builder.build();
        }
    }

    class Json2QuteClass extends Json2QuteCommon implements JsonClass {

        final QuteClass.Builder builder;
        final StartingClass scf;

        public Json2QuteClass(JsonNode jsonSource) {
            super(JsonIndex.IndexType.classtype, jsonSource);
            jsonSource = copyAndMergeClass(jsonSource);

            scf = new StartingClass(getIndex(), getSources(), getName(), isMarkdown());
            builder = new QuteClass.Builder()
                    .setName(decoratedTypeName(getName(), getSources()));

            if (!isSidekick()) {
                scf.classHitDice(jsonSource);
                scf.findClassProficiencies(jsonSource);
                scf.findStartingEquipment(jsonSource);
            }

            List<QuteClassAutoLevel> levels = classAutolevels(jsonSource);
            transferClassProficiencies(); // after autolevel for sidekick profs

            // Create autolevel w/ creation options & profs.
            builder.addAutoLevel(new QuteClassAutoLevel.Builder()
                    .setLevel(1)
                    .addFeatures(scf.buildStartingClassFeatures(jsonSource).stream())
                    .build());

            // Add other levels
            for (QuteClassAutoLevel level : levels) {
                if (!level.hasContent()) {
                    continue;
                }
                builder.addAutoLevel(new QuteClassAutoLevel.Builder()
                        .setLevel(level.getLevel())
                        .setScoreImprovement(level.isScoreImprovement())
                        .addFeatures(level.getFeatures())
                        .build());
            }
        }

        void transferClassProficiencies() {
            builder.setArmor(scf.getArmor())
                    .setWeapons(scf.getWeapons())
                    .setTools(scf.getTools())
                    .setNumSkills(Integer.parseInt(scf.getNumSkills()))
                    .setProficiency(scf.getProficiency())
                    .setHitDice(Integer.parseInt(scf.getHitDice()));
        }

        @Override
        public StartingClass getStartingClassAttributes() {
            return scf;
        }

        QuteClass build() {
            return builder.build();
        }
    }

    class Json2QuteFeat extends Json2QuteCommon implements JsonFeat {
        final QuteFeat.Builder builder;

        public Json2QuteFeat(JsonNode jsonSource) {
            super(JsonIndex.IndexType.feat, jsonSource);
            builder = new QuteFeat.Builder()
                    .setName(decoratedTypeName(getSources()))
                    .setModifiers(collectAbilityModifiers(jsonSource))
                    .setProficiency(new Proficiency.Builder()
                            .addSkills(jsonToSkillsList(jsonSource.withArray("skillProficiencies")))
                            .build())
                    .addText(featText(jsonSource));
        }

        QuteFeat build() {
            return builder.build();
        }
    }

    class Json2QuteItem extends Json2QuteCommon implements JsonItem {
        QuteItem.Builder builder;

        public Json2QuteItem(JsonNode jsonSource) {
            super(JsonIndex.IndexType.item, jsonSource);
            String name = getItemName(jsonSource);
            ItemEnum type = getType(jsonSource);
            List<PropertyEnum> propertyEnums = new ArrayList<>();
            findProperties(jsonSource, propertyEnums);

            builder = new QuteItem.Builder()
                    .setName(name)
                    .setType(type)
                    .setMagic(booleanOrDefault(jsonSource, "wondrous", false))
                    .setModifiers(collectAbilityModifiers(jsonSource))
                    .setModifiers(itemBonusModifers(jsonSource))
                    .setStealthPenalty(itemStealthPenalty(jsonSource))
                    .addText(itemTextAndRolls(jsonSource))
                    .addProperties(propertyEnums)
                    .setDetail(itemDetail(jsonSource, propertyEnums));

            if (jsonSource.has("value")) {
                builder.setCost(jsonSource.get("value").asDouble());
            }
            if (jsonSource.has("weight")) {
                builder.setWeight(jsonSource.get("weight").asDouble());
            }
            if (jsonSource.has("strength")) {
                builder.setStrengthRequirement(jsonSource.get("strength").asInt());
            }
            if (jsonSource.has("dmgType")) {
                builder.setDamage(
                        getTextOrDefault(jsonSource, "dmg1", null),
                        getTextOrDefault(jsonSource, "dmg2", null),
                        getTextOrDefault(jsonSource, "dmgType", null));
            }
            if (jsonSource.has("range")) {
                builder.setRange(jsonSource.get("range").asText());
            }
            if (jsonSource.has("ac")) {
                builder.setAc(jsonSource.get("ac").asInt());
            }
        }

        QuteItem build() {
            return builder.build();
        }
    }

    class Json2QuteMonster extends Json2QuteCommon implements JsonMonster {
        final QuteMonster.Builder builder;

        public Json2QuteMonster(JsonNode jsonSource) {
            super(JsonIndex.IndexType.monster, jsonSource);
            builder = new QuteMonster.Builder()
                    .setName(decorateMonsterName(jsonSource))
                    .setSize(getSize(jsonSource))
                    .setType(monsterType(jsonSource))
                    .setAlignment(monsterAlignment(jsonSource))
                    .setAc(monsterAc(jsonSource))
                    .setHpDice(monsterHp(jsonSource))
                    .setSpeed(monsterSpeed(jsonSource))
                    .setScores(intOrDefault(jsonSource, "str", 10),
                            intOrDefault(jsonSource, "dex", 10),
                            intOrDefault(jsonSource, "con", 10),
                            intOrDefault(jsonSource, "int", 10),
                            intOrDefault(jsonSource, "wis", 10),
                            intOrDefault(jsonSource, "cha", 10))
                    .setCr(getTextOrEmpty(jsonSource, "cr"))
                    .setDescription(monsterDescriptionList(jsonSource))
                    .setSave(jsonObjectToSkillBonusList(jsonSource.get("save")))
                    .setSkill(jsonObjectToSkillBonusList(jsonSource.get("skill")))
                    .setLanguages(joinAndReplace(jsonSource, "languages"))
                    .setSenses(joinAndReplace(jsonSource, "senses"),
                            intOrDefault(jsonSource, "passive", 10))
                    .setResist(joinAndReplace(jsonSource, "resist"))
                    .setVulnerable(joinAndReplace(jsonSource, "vulnerable"))
                    .setImmune(monsterImmunities(jsonSource))
                    .setConditionImmune(joinAndReplace(jsonSource, "conditionImmune"))
                    .setEnvironment(joinAndReplace(jsonSource, "environment"));

            builder.setTrait(collectTraits(jsonSource.get("trait")));
            builder.setAction(collectTraits(jsonSource.get("action")));
            builder.setReaction(collectTraits(jsonSource.get("reaction")));
            builder.setLegendary(collectTraits(jsonSource.get("legendary")));
            spellcastingTrait(jsonSource, this::noOp, this::noOp, builder::addAction, builder::addTrait);
        }

        QuteMonster build() {
            return builder.build();
        }
    }

    class Json2QuteRace extends Json2QuteCommon implements JsonRace {
        final QuteRace.Builder builder;

        public Json2QuteRace(JsonNode jsonSource) {
            super(JsonIndex.IndexType.race, jsonSource);
            jsonSource = copyAndMergeRace(jsonSource);
            String raceName = decoratedRaceName(jsonSource);

            builder = new QuteRace.Builder()
                    .setName(raceName)
                    .setModifiers(collectAbilityModifiers(jsonSource))
                    .setSize(getSize(jsonSource))
                    .setSpeed(getSpeed(jsonSource))
                    .setSpellAbility(SkillOrAbility.fromTextValue(getRacialSpellAbility(jsonSource)))
                    .setAbility(jsonArrayObjectToSkillBonusString(jsonSource, "ability"))
                    .setProficiency(new Proficiency.Builder()
                            .addSkills(jsonToSkillsList(jsonSource.withArray("skillProficiencies")))
                            .build())
                    .setTraits(collectRacialTraits(jsonSource));
        }

        QuteRace build() {
            return builder.build();
        }
    }

    class Json2QuteSpell extends Json2QuteCommon implements JsonSpell {
        final QuteSpell.Builder builder;

        public Json2QuteSpell(JsonNode jsonSource) {
            super(JsonIndex.IndexType.spell, jsonSource);
            builder = new QuteSpell.Builder()
                    .setName(getName())
                    .setLevel(spellLevel(jsonSource))
                    .setSchool(spellSchool(jsonSource))
                    .setRitual(spellIsRitual(jsonSource))
                    .setTime(spellCastingTime(jsonSource))
                    .setRange(spellRange(jsonSource))
                    .setComponents(spellComponentsString(jsonSource))
                    .setDuration(spellDuration(jsonSource))
                    .setClasses(spellClassesString(jsonSource));

            // text
            Set<String> diceRolls = new HashSet<>();
            List<String> text = new ArrayList<>();
            collectTextAndRolls(jsonSource, text, diceRolls);
            builder.addText(text);
        }

        QuteSpell build() {
            return builder.build();
        }

    }

    class Json2QuteCommon implements JsonBase {
        protected final CompendiumSources sources;

        Json2QuteCommon(JsonIndex.IndexType type, JsonNode jsonNode) {
            this.sources = index.constructSources(type, jsonNode);
        }

        String getName() {
            return this.sources.getName();
        }

        @Override
        public CompendiumSources getSources() {
            return sources;
        }

        @Override
        public JsonIndex getIndex() {
            return index;
        }

        @Override
        public boolean isMarkdown() {
            return true;
        }

        public void noOp(Object o) {
        }
    }
}
