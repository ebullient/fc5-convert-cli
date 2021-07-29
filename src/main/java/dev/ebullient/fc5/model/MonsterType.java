package dev.ebullient.fc5.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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
public class MonsterType implements BaseType {
    class AbilityScores {
        int strength;
        int dexterity;
        int constitution;
        int intelligence;
        int wisdom;
        int charisma;
    }

    final String name;
    final AbilityScores scores = new AbilityScores();
    final SizeEnum size;
    final String type;
    final String alignment;
    final String ac;
    final String hp;
    final String speed;
    final String save;
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
    final String description;
    final String environment;

    public MonsterType(Map<String, Object> elements) {
        try {
            name = NodeParser.getOrDefault(elements, "name", "unknown");
            size = NodeParser.getOrDefault(elements, "size", SizeEnum.UNKNOWN);
            type = NodeParser.getOrDefault(elements, "type", "");
            alignment = NodeParser.getOrDefault(elements, "alignment", "");
            ac = NodeParser.getOrDefault(elements, "ac", "");
            hp = NodeParser.getOrDefault(elements, "hp", "");
            speed = NodeParser.getOrDefault(elements, "speed", "");

            scores.strength = NodeParser.getOrDefault(elements, "str", 10);
            scores.dexterity = NodeParser.getOrDefault(elements, "dex", 10);
            scores.constitution = NodeParser.getOrDefault(elements, "con", 10);
            scores.intelligence = NodeParser.getOrDefault(elements, "int", 10);
            scores.wisdom = NodeParser.getOrDefault(elements, "wis", 10);
            scores.charisma = NodeParser.getOrDefault(elements, "cha", 10);

            save = NodeParser.getOrDefault(elements, "save", "");
            skill = NodeParser.getOrDefault(elements, "skill", Collections.emptyList());
            resist = NodeParser.getOrDefault(elements, "resist", "");
            vulnerable = NodeParser.getOrDefault(elements, "vulnerable", "");
            immune = NodeParser.getOrDefault(elements, "immune", "");
            conditionImmune = NodeParser.getOrDefault(elements, "conditionImmune", "");
            senses = NodeParser.getOrDefault(elements, "senses", "");
            passive = NodeParser.getOrDefault(elements, "passive", 10);
            languages = NodeParser.getOrDefault(elements, "languages", "Common");
            cr = NodeParser.getOrDefault(elements, "cr", "0");
            trait = NodeParser.getOrDefault(elements, "trait", Collections.emptyList());
            action = NodeParser.getOrDefault(elements, "action", Collections.emptyList());
            legendary = NodeParser.getOrDefault(elements, "legendary", Collections.emptyList());
            reaction = NodeParser.getOrDefault(elements, "reaction", Collections.emptyList());

            spells = NodeParser.getOrDefault(elements, "spells", "");
            slots = NodeParser.getOrDefault(elements, "slots", SpellSlots.NONE);
            description = NodeParser.getOrDefault(elements, "description", "");
            environment = NodeParser.getOrDefault(elements, "environment", "");
        } catch (ClassCastException ex) {
            System.err.println("Error parsing monster " + NodeParser.getOrDefault(elements, "name", "unknown"));
            throw ex;
        }
    }

    public String getName() {
        return name;
    }

    public AbilityScores getScores() {
        return scores;
    }

    public SizeEnum getSize() {
        return size;
    }

    public String getType() {
        return type;
    }

    public String getAlignment() {
        return alignment;
    }

    public String getAc() {
        return ac;
    }

    public String getHp() {
        return hp;
    }

    public String getSpeed() {
        return speed;
    }

    public String getSave() {
        return save;
    }

    public List<String> getSkill() {
        return skill;
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

    public String getSpells() {
        return spells;
    }

    public SpellSlots getSlots() {
        return slots;
    }

    public String getDescription() {
        return description;
    }

    public String getEnvironment() {
        return environment;
    }

    @Override
    public String toString() {
        return "MonsterType [name=" + name + "]";
    }
}
