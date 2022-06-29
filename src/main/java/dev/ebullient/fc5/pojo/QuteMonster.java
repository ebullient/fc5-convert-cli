package dev.ebullient.fc5.pojo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.quarkus.qute.TemplateData;

@TemplateData
public class QuteMonster implements QuteSource {
    static final Pattern TYPE_DETAIL = Pattern.compile("(.+?) \\((.+?)\\)");

    protected final String name;
    protected final List<String> description;

    protected final SizeEnum size;
    protected final String type;
    protected final String subtype;
    protected final String alignment;

    protected final String ac;
    protected final String acText;
    protected final String hp;
    protected final String hitDice;
    protected final String speed;

    protected final AbilityScores scores;

    protected final List<String> save;
    protected final List<String> skill;
    protected final String senses;
    protected final int passive;
    protected final String vulnerable;
    protected final String resist;
    protected final String immune;
    protected final String conditionImmune;
    protected final String languages;
    protected final String cr;

    protected final List<QuteTrait> trait;
    protected final List<QuteTrait> action;
    protected final List<QuteTrait> reaction;
    protected final List<QuteTrait> legendary;
    protected final String environment;

    public QuteMonster(String name, List<String> description, SizeEnum size,
            String type, String subtype, String alignment, String ac, String acText,
            String hp, String hitDice, String speed, AbilityScores scores,
            List<String> save, List<String> skill, String senses, int passive,
            String vulnerable, String resist, String immune, String conditionImmune,
            String languages, String cr,
            List<QuteTrait> trait, List<QuteTrait> action, List<QuteTrait> reaction, List<QuteTrait> legendary,
            String environment) {
        this.name = name;
        this.description = breathe(description);
        this.size = size;
        this.type = type;
        this.subtype = subtype;
        this.alignment = alignment;
        this.ac = ac;
        this.acText = acText;
        this.hp = hp;
        this.hitDice = hitDice;
        this.speed = speed;
        this.scores = scores;
        this.save = save;
        this.skill = skill;
        this.senses = senses;
        this.passive = passive;
        this.vulnerable = vulnerable;
        this.resist = resist;
        this.immune = immune;
        this.conditionImmune = conditionImmune;
        this.languages = languages;
        this.cr = cr;
        this.trait = trait;
        this.action = action;
        this.reaction = reaction;
        this.legendary = legendary;
        this.environment = environment;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slugify(name);
    }

    public String getDescription() {
        return String.join("\n", description).trim();
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

    public String getEnvironment() {
        return environment;
    }

    public List<QuteTrait> getTrait() {
        return trait;
    }

    public List<QuteTrait> getAction() {
        return action;
    }

    public List<QuteTrait> getLegendary() {
        return legendary;
    }

    public List<QuteTrait> getReaction() {
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

        Optional<QuteTrait> spellcasting = trait.stream().filter(t -> t.getName().contains("Spellcasting")).findFirst();
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

    protected void addUnlessEmpty(Map<String, Object> map, String key, List<QuteTrait> value) {
        if (!value.isEmpty()) {
            map.put(key, traitsToStrings(value));
        }
    }

    protected List<List<String>> traitsToStrings(List<QuteTrait> things) {
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

    protected String slugify(String text) {
        return MarkdownWriter.slugifier().slugify(text);
    }

    @Override
    public String toString() {
        return "monster[name=" + name + "]";
    }

    public static class Builder {
        protected String name;
        protected List<String> description;

        protected SizeEnum size;
        protected String type;
        protected String subtype;
        protected String alignment;

        protected String ac;
        protected String acText;
        protected String hp;
        protected String hitDice;
        protected String speed;

        protected AbilityScores scores;

        protected List<String> save;
        protected List<String> skill;
        protected String senses;
        protected int passive;
        protected String vulnerable;
        protected String resist;
        protected String immune;
        protected String conditionImmune;
        protected String languages;
        protected String cr;

        protected List<QuteTrait> trait = new ArrayList<>();
        protected List<QuteTrait> action = new ArrayList<>();
        protected List<QuteTrait> reaction = new ArrayList<>();
        protected List<QuteTrait> legendary = new ArrayList<>();
        protected String environment;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setDescription(List<String> description) {
            this.description = description;
            return this;
        }

        public Builder setSize(SizeEnum size) {
            this.size = size;
            return this;
        }

        public Builder setType(String fullType) {
            Matcher m = TYPE_DETAIL.matcher(fullType);
            if (m.matches()) {
                type = m.group(1);
                subtype = m.group(2);
            } else {
                type = fullType;
                subtype = "";
            }
            return this;
        }

        public Builder setAlignment(String alignment) {
            this.alignment = alignment;
            return this;
        }

        public Builder setAc(String tmpAc) {
            Matcher m = TYPE_DETAIL.matcher(tmpAc);
            if (m.matches()) {
                ac = m.group(1);
                acText = m.group(2);
            } else {
                ac = tmpAc;
                acText = "";
            }
            return this;
        }

        public Builder setHpDice(String tmpHp) {
            Matcher m = TYPE_DETAIL.matcher(tmpHp);
            if (m.matches()) {
                hp = m.group(1);
                hitDice = m.group(2);
            } else {
                hp = tmpHp;
                hitDice = null;
            }
            return this;
        }

        public Builder setSpeed(String speed) {
            this.speed = speed;
            return this;
        }

        public Builder setScores(int s, int d, int co, int i, int w, int ch) {
            this.scores = new AbilityScores.Builder()
                    .setStrength(s)
                    .setDexterity(d)
                    .setConstitution(co)
                    .setIntelligence(i)
                    .setWisdom(w)
                    .setCharisma(ch)
                    .build();
            return this;
        }

        public Builder setSave(List<String> save) {
            this.save = save;
            return this;
        }

        public Builder setSkill(List<String> skill) {
            this.skill = skill;
            return this;
        }

        public Builder setSenses(String senses, int passive) {
            this.senses = senses;
            this.passive = passive;
            return this;
        }

        public Builder setVulnerable(String vulnerable) {
            this.vulnerable = vulnerable;
            return this;
        }

        public Builder setResist(String resist) {
            this.resist = resist;
            return this;
        }

        public Builder setImmune(String immune) {
            this.immune = immune;
            return this;
        }

        public Builder setConditionImmune(String conditionImmune) {
            this.conditionImmune = conditionImmune;
            return this;
        }

        public Builder setLanguages(String languages) {
            this.languages = languages;
            return this;
        }

        public Builder setCr(String cr) {
            this.cr = cr;
            return this;
        }

        public Builder setTrait(List<QuteTrait> trait) {
            this.trait.addAll(trait);
            return this;
        }

        public Builder addTrait(QuteTrait trait) {
            this.trait.add(trait);
            return this;
        }

        public Builder setAction(List<QuteTrait> action) {
            this.action.addAll(action);
            return this;
        }

        public Builder addAction(QuteTrait action) {
            this.action.add(action);
            return this;
        }

        public Builder setReaction(List<QuteTrait> reaction) {
            this.reaction = reaction;
            return this;
        }

        public Builder setLegendary(List<QuteTrait> legendary) {
            this.legendary = legendary;
            return this;
        }

        public Builder setEnvironment(String environment) {
            this.environment = environment;
            return this;
        }

        public QuteMonster build() {
            return new QuteMonster(name, description, size, type, subtype,
                    alignment, ac, acText, hp, hitDice, speed, scores,
                    save, skill, senses, passive, vulnerable, resist, immune, conditionImmune,
                    languages, cr, trait, action, reaction, legendary, environment);
        }
    }
}
