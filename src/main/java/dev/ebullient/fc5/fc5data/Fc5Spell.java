package dev.ebullient.fc5.fc5data;

import java.util.Collections;
import java.util.List;

import dev.ebullient.fc5.pojo.QuteSpell;
import dev.ebullient.fc5.pojo.SchoolEnum;
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
public class Fc5Spell extends QuteSpell {

    final Fc5Text textData;
    final List<Fc5Roll> roll;

    Fc5Spell(String name, int level, SchoolEnum school, boolean ritual, String time, String range,
            String components, String duration, String classes,
            List<String> text, Fc5Text textData, List<Fc5Roll> roll) {
        super(name, level, school, ritual, time, range, components, duration, classes, text);
        this.textData = textData;
        this.roll = roll;
    }

    @Override
    public String getText() {
        return String.join("\n", textData.content);
    }

    public static class SpellTypeBuilder extends Builder {
        final Fc5Text textData;
        final List<Fc5Roll> roll;

        public SpellTypeBuilder(Fc5ParsingContext context) {
            setName(context.getOrFail(context.owner, "name", String.class));
            setLevel(context.getOrDefault("level", 0));
            setSchool(context.getOrDefault("school", SchoolEnum.None));
            setRitual(context.getOrDefault("ritual", false));
            setTime(context.getOrDefault("time", ""));
            setRange(context.getOrDefault("range", ""));
            setComponents(context.getOrDefault("components", ""));
            setDuration(context.getOrDefault("duration", ""));
            setClasses(context.getOrDefault("classes", ""));

            textData = context.getOrDefault("text", Fc5Text.NONE);
            roll = context.getOrDefault("roll", Collections.emptyList());
        }

        public Fc5Spell build() {

            return new Fc5Spell(name, level, school, ritual, time, range,
                    components, duration, classes, text, textData, roll);
        }
    }
}
