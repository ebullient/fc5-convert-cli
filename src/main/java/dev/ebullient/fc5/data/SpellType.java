package dev.ebullient.fc5.data;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.quarkus.qute.TemplateData;

/**
 * <p>
 * Java class for spellType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="spellType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="level" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="school" type="{}schoolEnum" minOccurs="0"/>
 *         &lt;element name="ritual" type="{}boolean"/>
 *         &lt;element name="time" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="range" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="components" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="duration" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="classes" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="source" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="text" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="roll" type="{}roll" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@TemplateData
public class SpellType implements BaseType {

    final String name;
    final int level;
    final SchoolEnum school;
    final boolean ritual;
    final String time;
    final String range;
    final String components;
    final String duration;
    final String classes;
    final String source;
    final Text text;
    final List<Roll> roll;

    public SpellType(Map<String, Object> elements) {
        name = NodeParser.getOrDefault(elements, "name", "unknown");
        level = NodeParser.getOrDefault(elements, "level", 0);
        school = NodeParser.getOrDefault(elements, "school", SchoolEnum.UNKNOWN);
        ritual = NodeParser.getOrDefault(elements, "ritual", false);
        time = NodeParser.getOrDefault(elements, "time", "");
        range = NodeParser.getOrDefault(elements, "range", "");
        components = NodeParser.getOrDefault(elements, "components", "");
        duration = NodeParser.getOrDefault(elements, "duration", "");
        classes = NodeParser.getOrDefault(elements, "classes", "");
        source = NodeParser.getOrDefault(elements, "source", "");
        text = NodeParser.getOrDefault(elements, "text", Text.NONE);
        roll = NodeParser.getOrDefault(elements, "roll", Collections.emptyList());
    }

    public String getName() {
        return name;
    }

    public String getTag() {
        return "spell/" + MarkdownWriter.slugifier().slugify(name);
    }

    public int getLevel() {
        return level;
    }

    public SchoolEnum getSchool() {
        return school;
    }

    public boolean isRitual() {
        return ritual;
    }

    public String getTime() {
        return time;
    }

    public String getRange() {
        return range;
    }

    public String getComponents() {
        return components;
    }

    public String getDuration() {
        return duration;
    }

    public String getClasses() {
        return classes;
    }

    public String getSource() {
        return source;
    }

    public String getText() {
        return String.join("\n", text.content);
    }

    public List<Roll> getRoll() {
        return roll;
    }

    @Override
    public String toString() {
        return "SpellType [name=" + name + "]";
    }
}
