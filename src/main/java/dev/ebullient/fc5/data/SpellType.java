package dev.ebullient.fc5.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    final List<String> classSlugs;
    final String source;
    final Text text;
    final List<Roll> roll;

    public SpellType(ParsingContext context) {
        name = context.getOrFail(context.owner, "name", String.class);
        school = context.getOrFail(context.owner, "school", SchoolEnum.class);

        level = context.getOrDefault("level", 0);
        ritual = context.getOrDefault("ritual", false);
        time = context.getOrDefault("time", "");
        range = context.getOrDefault("range", "");
        components = context.getOrDefault("components", "");
        duration = context.getOrDefault("duration", "");
        source = context.getOrDefault("source", "");
        text = context.getOrDefault("text", Text.NONE);
        roll = context.getOrDefault("roll", Collections.emptyList());

        classes = context.getOrDefault("classes", "");
        classSlugs = Stream.of(classes.split("\\s*,\\s*"))
                .map(x -> slugify(x)).collect(Collectors.toList());
    }

    public String getName() {
        return name;
    }

    public List<String> getTags() {
        List<String> tags = new ArrayList<>();
        tags.add("spell/school/" + school.longName());
        classSlugs.forEach(x -> tags.add("spell/class/" + x));
        if (ritual) {
            tags.add("spell/ritual");
        }
        return tags;
    }

    public String getClasses() {
        return classes;
    }

    public String getComponents() {
        return components;
    }

    public String getDuration() {
        return duration;
    }

    public String getLevel() {
        switch (level) {
            case 0:
                return "cantrip";
            case 1:
                return "1st level";
            case 2:
                return "2nd level";
            case 3:
                return "3rd level";
            default:
                return level + "st level";
        }
    }

    public String getRange() {
        return range;
    }

    public boolean getRitual() {
        return ritual;
    }

    public String getSchool() {
        return school.longName();
    }

    public String getTime() {
        return time;
    }

    public String getText() {
        return String.join("\n", text.content);
    }

    String slugify(String text) {
        return MarkdownWriter.slugifier().slugify(text);
    }

    @Override
    public String toString() {
        return "SpellType [name=" + name + "]";
    }
}
