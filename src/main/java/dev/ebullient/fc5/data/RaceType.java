package dev.ebullient.fc5.data;

import java.util.Collections;
import java.util.List;

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

    public RaceType(ParsingContext context) {
        name = context.getOrFail(context.owner, "name", String.class);

        size = context.getOrDefault(name, "size", SizeEnum.UNKNOWN);
        speed = context.getOrDefault(name, "speed", 0);
        ability = context.getOrDefault(name, "ability", "");
        spellAbility = context.getOrDefault(name, "spellAbility", AbilityEnum.NONE);
        proficiency = context.getOrDefault(name, "proficiency", Proficiency.NONE);
        traits = context.getOrDefault(name, "trait", Collections.emptyList());
        modifiers = context.getOrDefault(name, "modifier", Collections.emptyList());
    }

    public String getName() {
        return name;
    }

    public List<String> getTags() {
        return Collections.singletonList("race/" + MarkdownWriter.slugifier().slugify(name));
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

    public String getSpellAbility() {
        return spellAbility == AbilityEnum.NONE ? "" : spellAbility.toString();
    }

    public String getSkills() {
        return proficiency.getSkillNames();
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
