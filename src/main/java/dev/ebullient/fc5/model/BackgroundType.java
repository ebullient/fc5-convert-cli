package dev.ebullient.fc5.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>Java class for backgroundType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="backgroundType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="proficiency" type="{}skillList"/>
 *         &lt;element name="trait" type="{}traitType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class BackgroundType {
    final String name;
    final List<Trait> traits;
    final Proficiency proficiency;

    public BackgroundType(Map<String, Object> elements) {
        name = NodeParser.getOrDefault(elements, "name", "unknown");
        
        proficiency = NodeParser.getOrDefault(elements, "proficiency", Proficiency.SKILL_LIST);
        proficiency.setFlavor("skillList");

        traits = NodeParser.getOrDefault(elements, "trait", Collections.emptyList());
    }

    public String getName() {
        return name;
    }

    public List<Trait> getTrait() {
        return traits;
    }

    public Proficiency getProficiency() {
        return proficiency;
    }

    @Override
    public String toString() {
        return "BackgroundType [name=" + name + "]";
    }
}
