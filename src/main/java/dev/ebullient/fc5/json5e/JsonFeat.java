package dev.ebullient.fc5.json5e;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.Log;

public interface JsonFeat extends JsonBase {

    default List<String> featText(JsonNode jsonSource) {
        List<String> text = new ArrayList<>();
        Set<String> diceRolls = new HashSet<>();

        String sourceText = getSources().getSourceText();
        String altSource = getSources().alternateSource();

        try {
            jsonSource.withArray("entries").forEach(entry -> appendEntryToText(text, entry, diceRolls));
            addAdditionalEntries(jsonSource, text, diceRolls, altSource);
        } catch (Exception e) {
            Log.errorf(e, "Unable to parse text for %s", getSources());
        }
        maybeAddBlankLine(text); // before Source
        text.add("Source: " + sourceText);
        return text;
    }

    default List<String> listPrerequisites(JsonNode value) {
        List<String> prereqs = new ArrayList<>();
        JsonIndex index = getIndex();
        value.withArray("prerequisite").forEach(entry -> {
            if (entry.has("level")) {
                prereqs.add(levelToText(entry.get("level")));
            }
            entry.withArray("race").forEach(r -> prereqs.add(index.lookupName(JsonIndex.IndexType.race, raceToText(r))));

            Map<String, List<String>> abilityScores = new HashMap<>();
            entry.withArray("ability").forEach(a -> a.fields().forEachRemaining(score -> abilityScores.computeIfAbsent(
                    score.getValue().asText(),
                    k -> new ArrayList<>()).add(asAbilityEnum(score.getKey()))));
            abilityScores.forEach(
                    (k, v) -> prereqs.add(String.format("%s %s or higher", String.join(" or ", v), k)));

            if (entry.has("spellcasting") && entry.get("spellcasting").asBoolean()) {
                prereqs.add("The ability to cast at least one spell");
            }
            if (entry.has("pact")) {
                prereqs.add("Pact of the " + entry.get("pact").asText());
            }
            if (entry.has("patron")) {
                prereqs.add(entry.get("patron").asText() + " Patron");
            }
            entry.withArray("spell").forEach(s -> {
                String text = s.asText().replaceAll("#c", "");
                prereqs.add(index.lookupName(JsonIndex.IndexType.spell, text));
            });
            entry.withArray("feat").forEach(f -> prereqs
                    .add(featPattern.matcher(f.asText())
                            .replaceAll(m -> index.lookupName(JsonIndex.IndexType.feat, m.group(1)))));
            entry.withArray("feature").forEach(f -> prereqs.add(featPattern.matcher(f.asText())
                    .replaceAll(m -> index.lookupName(JsonIndex.IndexType.optionalfeature, m.group(1)))));
            entry.withArray("background")
                    .forEach(f -> prereqs
                            .add(index.lookupName(JsonIndex.IndexType.background, f.get("name").asText()) + " background"));
            entry.withArray("item").forEach(i -> prereqs.add(index.lookupName(JsonIndex.IndexType.item, i.asText())));

            if (entry.has("psionics")) {
                prereqs.add("Psionics");
            }

            List<String> profs = new ArrayList<>();
            entry.withArray("proficiency").forEach(f -> f.fields().forEachRemaining(field -> {
                String key = field.getKey();
                if ("weapon".equals(key)) {
                    key += "s";
                }
                profs.add(String.format("%s %s", key, field.getValue().asText()));
            }));
            prereqs.add(String.format("Proficiency with %s", String.join(" or ", profs)));

            if (entry.has("other")) {
                prereqs.add(entry.get("other").asText());
            }
        });
        return prereqs;
    }
}
