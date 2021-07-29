package dev.ebullient.fc5.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * <p>Java class for featType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="featType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="prerequisite" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="text" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="proficiency" type="{}abilityAndSkillList" minOccurs="0"/>
 *         &lt;element name="modifier" type="{}modifierType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class FeatType {

    final String name;
    final String prerequisite;
    final Text text;
    final Proficiency proficiency;
    final List<Modifier> modifier;

    public FeatType(Map<String, Object> elements) {
        name = NodeParser.getOrDefault(elements, "name", "unknown");
        prerequisite = NodeParser.getOrDefault(elements, "prerequisite", "");
        text = NodeParser.getOrDefault(elements, "text", Text.NONE);

        proficiency = NodeParser.getOrDefault(elements, "proficiency", Proficiency.ABILITY_AND_SKILL_LIST);
        proficiency.setFlavor("abilityAndSkillList");

        modifier = NodeParser.getOrDefault(elements, "modifier", Collections.emptyList());
    }

    @Override
    public String toString() {
        return "FeatType [name=" + name + "]";
    }
}
