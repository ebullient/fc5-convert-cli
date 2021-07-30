package dev.ebullient.fc5.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.quarkus.qute.TemplateData;

/**
 * <p>
 * Java class for raceType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="raceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="size" type="{}sizeEnum"/>
 *         &lt;element name="speed" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="ability" type="{}abilityBonusList"/>
 *         &lt;element name="spellAbility" type="{}abilityEnum"/>
 *         &lt;element name="proficiency" type="{}skillList"/>
 *         &lt;element name="trait" type="{}traitType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="modifier" type="{}modifierType" minOccurs="0"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@TemplateData
public class RaceType implements BaseType {

    final String name;
    final SizeEnum size;
    final int speed;
    final String ability;
    final AbilityEnum spellAbility;
    final Proficiency proficiency;
    final List<Trait> traits;
    final List<Modifier> modifiers;

    public RaceType(Map<String, Object> elements) {
        name = NodeParser.getOrDefault(elements, "name", "unknown");
        size = NodeParser.getOrDefault(elements, "size", SizeEnum.UNKNOWN);
        speed = NodeParser.getOrDefault(elements, "speed", 0);
        ability = NodeParser.getOrDefault(elements, "ability", "");
        spellAbility = NodeParser.getOrDefault(elements, "spellAbility", AbilityEnum.NONE);
        proficiency = NodeParser.getOrDefault(elements, "proficiency", Proficiency.SKILL_LIST);
        traits = NodeParser.getOrDefault(elements, "trait", Collections.emptyList());
        modifiers = NodeParser.getOrDefault(elements, "modifier", Collections.emptyList());
    }

    public String getName() {
        return name;
    }

    public String getTag() {
        return "race/" + MarkdownWriter.slugifier().slugify(name);
    }

    public SizeEnum getSize() {
        return size;
    }

    public int getSpeed() {
        return speed;
    }

    public String getAbility() {
        return ability;
    }

    public AbilityEnum getSpellAbility() {
        return spellAbility;
    }

    public Proficiency getProficiency() {
        return proficiency;
    }

    public List<Trait> getTraits() {
        return traits;
    }

    public List<Modifier> getModifiers() {
        return modifiers;
    }

    @Override
    public String toString() {
        return "RaceType [name=" + name + "]";
    }
}
