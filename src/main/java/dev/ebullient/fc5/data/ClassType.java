package dev.ebullient.fc5.data;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.quarkus.qute.TemplateData;

/**
 * <p>
 * Java class for classType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="classType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="hd" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="proficiency" type="{}abilityAndSkillList"/>
 *         &lt;element name="spellAbility" type="{}abilityEnum"/>
 *         &lt;element name="numSkills" type="{}integer"/>
 *         &lt;element name="autolevel" type="{}autolevelType"/>
 *         &lt;element name="armor" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="weapons" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="tools">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               ...
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="wealth" type="{}rollFormula"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@TemplateData
public class ClassType implements BaseType {
    public static final String NONE = "none";

    final String name;
    final int hitDice;
    final Proficiency proficiency;
    final AbilityEnum spellAbility;
    final int numSkills;
    final List<Autolevel> autolevel;
    final String armor;
    final String weapons;
    final String tools;
    final String wealth;

    public ClassType(Map<String, Object> elements) {
        name = NodeParser.getOrDefault(elements, "name", "unknown");
        hitDice = NodeParser.getOrDefault(elements, "hd", 8);

        proficiency = NodeParser.getOrDefault(elements, "proficiency", Proficiency.ABILITY_AND_SKILL_LIST);
        proficiency.setFlavor("abilityAndSkillList");

        spellAbility = NodeParser.getOrDefault(elements, "spellAbility", AbilityEnum.NONE);
        numSkills = NodeParser.getOrDefault(elements, "numSkills", 0);
        autolevel = NodeParser.getOrDefault(elements, "autolevel", Collections.emptyList());
        armor = NodeParser.getOrDefault(elements, "armor", NONE);
        weapons = NodeParser.getOrDefault(elements, "weapons", NONE);
        tools = NodeParser.getOrDefault(elements, "tools", NONE);
        wealth = NodeParser.getOrDefault(elements, "wealth", "");
    }

    public List<Autolevel> getLevelFeatures() {
        return autolevel;
    }

    public String getName() {
        return name;
    }

    public String getTag() {
        return "class/" + MarkdownWriter.slugifier().slugify(name);
    }

    public int getHitDice() {
        return hitDice;
    }

    public int getHitRollAverage() {
        return hitDice / 2 + 1;
    }

    public String getArmor() {
        return armor;
    }

    public String getSavingThrows() {
        return proficiency.getSavingThrows();
    }

    public int getNumSkills() {
        return numSkills;
    }

    public String getSkills() {
        return proficiency.getSkillNames();
    }

    public String getTools() {
        return tools;
    }

    public String getWeapons() {
        return weapons;
    }

    @Override
    public String toString() {
        return "ClassType [name=" + name + "]";
    }
}
