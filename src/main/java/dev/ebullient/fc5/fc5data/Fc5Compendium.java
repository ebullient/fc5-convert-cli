package dev.ebullient.fc5.fc5data;

import java.util.ArrayList;
import java.util.List;

import dev.ebullient.fc5.pojo.QuteBackground;
import dev.ebullient.fc5.pojo.QuteRace;

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
public class Fc5Compendium {

    final List<QuteBackground> backgrounds = new ArrayList<>();
    final List<Fc5Class> classes = new ArrayList<>();
    final List<Fc5Feat> feats = new ArrayList<>();
    final List<Fc5Item> items = new ArrayList<>();
    final List<Fc5Monster> monsters = new ArrayList<>();
    final List<QuteRace> races = new ArrayList<>();
    final List<Fc5Spell> spells = new ArrayList<>();

    public List<QuteBackground> getBackgrounds() {
        return backgrounds;
    }

    public List<Fc5Class> getClasses() {
        return classes;
    }

    public List<Fc5Feat> getFeats() {
        return feats;
    }

    public List<Fc5Item> getItems() {
        return items;
    }

    public List<Fc5Monster> getMonsters() {
        return monsters;
    }

    public List<QuteRace> getRaces() {
        return races;
    }

    public List<Fc5Spell> getSpells() {
        return spells;
    }

    @Override
    public String toString() {
        return "CompendiumType [backgrounds=" + backgrounds + ", classes=" + classes + ", feats=" + feats + ", items="
                + items + ", monsters=" + monsters + ", races=" + races + ", spells=" + spells + "]";
    }
}
