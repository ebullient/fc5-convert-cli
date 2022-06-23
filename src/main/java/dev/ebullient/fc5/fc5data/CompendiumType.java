package dev.ebullient.fc5.fc5data;

import java.util.ArrayList;
import java.util.List;

import dev.ebullient.fc5.pojo.MdBackground;
import dev.ebullient.fc5.pojo.MdRace;

/**
 * <p>
 * Java class for compendiumType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="compendiumType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="item" type="{}itemType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="race" type="{}raceType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="class" type="{}classType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="feat" type="{}featType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="background" type="{}backgroundType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="spell" type="{}spellType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="monster" type="{}monsterType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/choice>
 *       &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}byte" />
 *       &lt;attribute name="auto_indent" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public class CompendiumType {

    final List<MdBackground> backgrounds = new ArrayList<>();
    final List<ClassType> classes = new ArrayList<>();
    final List<FeatType> feats = new ArrayList<>();
    final List<ItemType> items = new ArrayList<>();
    final List<MonsterType> monsters = new ArrayList<>();
    final List<MdRace> races = new ArrayList<>();
    final List<SpellType> spells = new ArrayList<>();

    public List<MdBackground> getBackgrounds() {
        return backgrounds;
    }

    public List<ClassType> getClasses() {
        return classes;
    }

    public List<FeatType> getFeats() {
        return feats;
    }

    public List<ItemType> getItems() {
        return items;
    }

    public List<MonsterType> getMonsters() {
        return monsters;
    }

    public List<MdRace> getRaces() {
        return races;
    }

    public List<SpellType> getSpells() {
        return spells;
    }

    @Override
    public String toString() {
        return "CompendiumType [backgrounds=" + backgrounds + ", classes=" + classes + ", feats=" + feats + ", items="
                + items + ", monsters=" + monsters + ", races=" + races + ", spells=" + spells + "]";
    }
}
