package dev.ebullient.fc5.json5e;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.pojo.QuteTrait;

public interface JsonRace extends JsonBase {

    default JsonNode copyAndMergeRace(JsonNode jsonSource) {
        if (jsonSource.has("raceName") || jsonSource.has("_copy")) {
            jsonSource = getIndex().cloneOrCopy(getSources().getKey(),
                    jsonSource, JsonIndex.IndexType.race,
                    getTextOrDefault(jsonSource, "raceName", null),
                    getTextOrDefault(jsonSource, "raceSource", null));
        }
        return jsonSource;
    }

    default String decoratedRaceName(JsonNode jsonSource) {
        String raceName = getSources().getName();
        JsonNode raceNameNode = jsonSource.get("raceName");
        if (raceNameNode != null) {
            raceName = String.format("%s (%s)", raceNameNode.asText(), raceName);
        }
        return decoratedTypeName(raceName.replace("Variant; ", ""), getSources());
    }

    default List<QuteTrait> collectRacialTraits(JsonNode jsonSource) {
        // Create the description trait. Add that first.
        // Gather literal text entries into that single description entry
        List<QuteTrait> traits = new ArrayList<>();
        List<String> text = new ArrayList<>();
        Set<String> diceRolls = new HashSet<>();

        getFluffDescription(jsonSource, JsonIndex.IndexType.racefluff, text);
        jsonSource.withArray("entries").forEach(entry -> {
            if (entry.isTextual()) {
                text.add(replaceText(entry.asText().replaceAll(":$", "."), diceRolls));
            } else if (entry.isObject()) {
                if (entry.has("type") && "list".equals(entry.get("type").asText())) {
                    traits.addAll(collectTraits(entry.withArray("items")));
                } else {
                    traits.add(createTrait(entry));
                }
            }
        });
        maybeAddBlankLine(text);
        text.add("Source: " + getSources().getSourceText());

        // insert the description in the front
        traits.add(0, createTrait("Description", text, diceRolls));
        return traits;
    }

    default String getRacialSpellAbility(JsonNode jsonSource) {
        JsonNode additionalSpells = jsonSource.get("additionalSpells");
        if (additionalSpells == null || additionalSpells.isNull()) {
            return null;
        }
        JsonNode spells = additionalSpells.get(0);
        if (spells != null && spells.has("ability")) {
            JsonNode ability = spells.get("ability");
            if (ability.has("choose")) {
                // just pick the first (can be edited)
                return asAbilityEnum(ability.withArray("choose").get(0));
            } else {
                return asAbilityEnum(ability);
            }
        }
        return null;
    }

}
