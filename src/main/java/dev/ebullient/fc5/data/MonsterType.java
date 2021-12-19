package dev.ebullient.fc5.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.quarkus.qute.TemplateData;

/**
 * <p>
 * Java class for monsterType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="monsterType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="size" type="{}sizeEnum"/>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="alignment" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ac" type="{}acType"/>
 *         &lt;element name="hp" type="{}hpType"/>
 *         &lt;element name="speed" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="str" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="dex" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="con" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="int" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="wis" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="cha" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="save" type="{}abilityBonusList"/>
 *         &lt;element name="skill" type="{}skillBonusList"/>
 *         &lt;element name="resist" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="vulnerable" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="immune" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="conditionImmune" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="senses" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="passive" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="languages" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="cr" type="{}crType"/>
 *         &lt;element name="trait" type="{}traitType"/>
 *         &lt;element name="action" type="{}traitType"/>
 *         &lt;element name="legendary" type="{}traitType"/>
 *         &lt;element name="reaction" type="{}traitType"/>
 *         &lt;element name="spells" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="slots" type="{}slotsType"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="environment" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@TemplateData
public class MonsterType implements BaseType {
    static final Pattern TYPE_DETAIL = Pattern.compile("(.+?) \\((.+?)\\)");

    final String name;
    final AbilityScores scores;
    final SizeEnum size;
    final String type;
    final String subtype;
    final String alignment;
    final String ac;
    final String acText;
    final String hp;
    final String hitDice;
    final String speed;
    final List<String> save;
    final List<String> skill;
    final String resist;
    final String vulnerable;
    final String immune;
    final String conditionImmune;
    final String senses;
    final int passive;
    final String languages;
    final String cr;
    final List<Trait> trait;
    final List<Trait> action;
    final List<Trait> legendary;
    final List<Trait> reaction;
    final String spells;
    final SpellSlots slots;
    final Text description;
    final String environment;

    public MonsterType(ParsingContext context) {
        name = context.getOrFail(context.owner, "name", String.class);
        size = context.getOrDefault(name, "size", SizeEnum.UNKNOWN);

        String fullType = context.getOrDefault(name, "type", "");
        Matcher m = TYPE_DETAIL.matcher(fullType);
        if (m.matches()) {
            type = m.group(1);
            subtype = m.group(2);
        } else {
            type = fullType;
            subtype = "";
        }

        alignment = context.getOrDefault(name, "alignment", "");

        String tmpAc = context.getOrDefault(name, "ac", "");
        m = TYPE_DETAIL.matcher(tmpAc);
        if (m.matches()) {
            ac = m.group(1);
            acText = m.group(2);
        } else {
            ac = tmpAc;
            acText = "";
        }

        String tmpHp = context.getOrDefault(name, "hp", "");
        m = TYPE_DETAIL.matcher(tmpHp);
        if (m.matches()) {
            hp = m.group(1);
            hitDice = m.group(2);
        } else {
            hp = tmpHp;
            hitDice = null;
        }

        speed = context.getOrDefault(name, "speed", "");
        scores = new AbilityScores(context, name);

        save = context.getOrDefault(name, "save", Collections.emptyList());
        skill = context.getOrDefault(name, "skill", Collections.emptyList());

        resist = context.getOrDefault(name, "resist", "");
        vulnerable = context.getOrDefault(name, "vulnerable", "");
        immune = context.getOrDefault(name, "immune", "");
        conditionImmune = context.getOrDefault(name, "conditionImmune", "");

        senses = context.getOrDefault(name, "senses", "");
        passive = context.getOrDefault(name, "passive", 10);
        languages = context.getOrDefault(name, "languages", "");
        cr = context.getOrDefault(name, "cr", "0");
        trait = context.getOrDefault(name, "trait", Collections.emptyList());
        action = context.getOrDefault(name, "action", Collections.emptyList());
        legendary = context.getOrDefault(name, "legendary", Collections.emptyList());
        reaction = context.getOrDefault(name, "reaction", Collections.emptyList());

        spells = context.getOrDefault(name, "spells", "");
        slots = context.getOrDefault(name, "slots", SpellSlots.NONE);
        environment = context.getOrDefault(name, "environment", "");

        description = new Text(Collections.singletonList(context.getOrDefault(name, "description", "")));
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slugify(name);
    }

    public List<String> getTags() {
        List<String> result = new ArrayList<>();
        result.add("monster/size/" + slugify(size.value()));

        if (subtype.isEmpty()) {
            result.add("monster/type/" + slugify(type));
        } else {
            for (String detail : subtype.split("\\s*,\\s*")) {
                result.add("monster/type/" + slugify(type) + "/" + slugify(detail));
            }
        }

        if (!environment.isBlank()) {
            for (String env : environment.split("\\s*,\\s*")) {
                result.add("monster/environment/" + slugify(env));
            }
        }
        return result;
    }

    public String getScoreString() {
        return scores.toString();
    }

    public AbilityScores getScores() {
        return scores;
    }

    public String getSize() {
        return size.value();
    }

    public String getType() {
        return type;
    }

    public String getSubtype() {
        return subtype;
    }

    public String getFullType() {
        return type + (subtype.isEmpty() ? "" : "(" + subtype + ")");
    }

    public String getAlignment() {
        return alignment;
    }

    public String getAc() {
        return ac;
    }

    public String getAcText() {
        return acText;
    }

    public String getHp() {
        return hp;
    }

    public String getHitDice() {
        return hitDice;
    }

    public String getSpeed() {
        return speed;
    }

    public String getSaveString() {
        return String.join(", ", save);
    }

    public Map<String, String> getSaves() {
        return save.stream()
                .collect(Collectors.toMap(
                        s -> s.substring(0, s.indexOf(' ')),
                        s -> s.substring(s.indexOf(' ') + 1)));
    }

    public List<String> getSkill() {
        return skill;
    }

    public Map<String, String> getSkills() {
        return skill.stream()
                .collect(Collectors.toMap(
                        s -> s.substring(0, s.indexOf(' ')),
                        s -> s.substring(s.indexOf(' ') + 1)));
    }

    public String getSkillString() {
        return String.join(", ", skill);
    }

    public String getResist() {
        return resist;
    }

    public String getVulnerable() {
        return vulnerable;
    }

    public String getImmune() {
        return immune;
    }

    public String getConditionImmune() {
        return conditionImmune;
    }

    public String getSenses() {
        return senses;
    }

    public int getPassive() {
        return passive;
    }

    public String getLanguages() {
        return languages;
    }

    public String getCr() {
        return cr;
    }

    public List<Trait> getTrait() {
        return trait;
    }

    public List<Trait> getAction() {
        return action;
    }

    public List<Trait> getLegendary() {
        return legendary;
    }

    public List<Trait> getReaction() {
        return reaction;
    }

    public String getTraitYaml() {
        return MarkdownWriter.yaml().dump(traitsToStrings(trait));
    }

    public String getActionYaml() {
        return MarkdownWriter.yaml().dump(traitsToStrings(action));
    }

    public String getLegendaryYaml() {
        return MarkdownWriter.yaml().dump(traitsToStrings(legendary));
    }

    public String getReactionYaml() {
        return MarkdownWriter.yaml().dump(traitsToStrings(reaction));
    }

    public String get5eStatblockYaml() {
        return MarkdownWriter.yaml().dump(toStatblock()).trim();
    }

    List<List<String>> traitsToStrings(List<Trait> things) {
        List<List<String>> list = new ArrayList<>();
        things.forEach(t -> {
            List<String> e = new ArrayList<>();
            if (t.name.length() > 0) {
                e.add(t.name);
            }
            e.add(t.getText());
            list.add(e);
        });
        return list;
    }

    public String getSpells() {
        return spells;
    }

    public SpellSlots getSlots() {
        return slots;
    }

    public String getDescription() {
        return String.join("\n", description.content).trim();
    }

    public String getEnvironment() {
        return environment;
    }

    String slugify(String text) {
        return MarkdownWriter.slugifier().slugify(text);
    }

    @Override
    public String toString() {
        return "MonsterType [name=" + name + "]";
    }

    /** Represent the structure used by the 5e statblock plugin */
    Map<String, Object> toStatblock() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("name", name);
        map.put("size", getSize());
        map.put("type", type);
        map.put("subtype", subtype);
        map.put("alignment", alignment);

        map.put("ac", Integer.parseInt(ac));
        map.put("hp", Integer.parseInt(hp));
        addUnlessEmpty(map, "hit_dice", hitDice);

        map.put("speed", speed);
        map.put("stats", scores.toArray());

        if (save.size() > 0) {
            map.put("saves", getSaves());
        }
        if (skill.size() > 0) {
            map.put("skillsaves", getSkills());
        }

        addUnlessEmpty(map, "damage_vulnerabilities", vulnerable);
        addUnlessEmpty(map, "damage_resistances", resist);
        addUnlessEmpty(map, "damage_immunities", immune);
        addUnlessEmpty(map, "condition_immunities", conditionImmune);

        map.put("senses", (senses.isBlank() ? "" : senses + ", ") + "passive Perception " + passive);
        map.put("languages", languages);
        map.put("cr", cr);

        Optional<Trait> spellcasting = trait.stream().filter(t -> t.getName().contains("Spellcasting")).findFirst();
        if (spellcasting.isPresent()) {
            String[] text = spellcasting.get().getText().split("\n"); // reflow text
            map.put("spells", Arrays.asList(text).stream()
                    .filter(x -> !x.isEmpty())
                    .map(x -> x.replaceAll("- \\*\\*(.*)\\*\\*", "$1"))
                    .collect(Collectors.toList()));
        }

        addUnlessEmpty(map, "traits", (spellcasting.isEmpty()
                ? trait
                : trait.stream().filter(t -> !t.getName().contains("Spellcasting"))
                        .collect(Collectors.toList())));

        addUnlessEmpty(map, "actions", action);
        addUnlessEmpty(map, "legendary_actions", legendary);
        addUnlessEmpty(map, "reactions", reaction);

        return map;
    }

    void addUnlessEmpty(Map<String, Object> map, String key, String value) {
        if (value != null && !value.isBlank()) {
            map.put(key, value);
        }
    }

    void addUnlessEmpty(Map<String, Object> map, String key, List<Trait> value) {
        if (!value.isEmpty()) {
            map.put(key, traitsToStrings(value));
        }
    }
}
