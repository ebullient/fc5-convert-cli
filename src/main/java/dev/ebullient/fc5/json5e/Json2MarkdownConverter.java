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
            nodes.add(json2qute(type, e));
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
                jsonNode = index.handleCopy(JsonIndex.IndexType.classtype, jsonNode);
                return new Json2QuteClass(jsonNode).build();
            case feat:
                // no use of _copy
                return new Json2QuteFeat(jsonNode).build();
            case item:
                jsonNode = index.handleCopy(JsonIndex.IndexType.classtype, jsonNode);
                return new Json2QuteItem(jsonNode).build();
            case monster:
                jsonNode = index.handleCopy(JsonIndex.IndexType.classtype, jsonNode);
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

            String backgroundName = decoratedTypeName(sources);
            this.builder = new QuteBackground.Builder()
                    .setName(backgroundName)
                    .setProficiency(new Proficiency.Builder()
                            .addSkills(jsonToSkillsList(jsonSource.withArray("skillProficiencies")))
                            .build());

            List<QuteTrait> traits = collectTraitsFromEntries(backgroundName, jsonSource);
            traits.add(0, new QuteTrait.Builder()
                    .setName("Description")
                    .addText(getDescription(jsonSource))
                    .build());

            builder.setTraits(traits);
        }

        QuteBackground build() {
            return builder.build();
        }

    }

    class Json2QuteClass extends Json2QuteCommon implements JsonClass {

        public Json2QuteClass(JsonNode jsonNode) {
            super(JsonIndex.IndexType.classtype, jsonNode);
        }

        QuteClass build() {
            return new QuteClass.Builder().build();
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

        public Json2QuteMonster(JsonNode jsonNode) {
            super(JsonIndex.IndexType.monster, jsonNode);
        }

        QuteMonster build() {
            return new QuteMonster.Builder().build();
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
                    .setAbility(jsonArrayObjectToSkillBonusString(jsonSource.withArray("ability")))
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
    }
}
