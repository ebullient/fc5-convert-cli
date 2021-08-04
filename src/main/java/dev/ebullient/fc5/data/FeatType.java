package dev.ebullient.fc5.data;

import java.util.Collections;
import java.util.List;

import io.quarkus.qute.TemplateData;

/**
 * <p>
 * Java class for featType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
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
@TemplateData
public class FeatType implements BaseType {

    final String name;
    final String prerequisite;
    final Text text;
    final Proficiency proficiency;
    final List<Modifier> modifier;

    public FeatType(ParsingContext context) {
        name = context.getOrFail(context.owner, "name", String.class);

        prerequisite = context.getOrDefault(name, "prerequisite", "");
        text = context.getOrDefault(name, "text", Text.NONE);

        proficiency = context.getOrDefault(name, "proficiency", Proficiency.NONE);
        modifier = context.getOrDefault(name, "modifier", Collections.emptyList());
    }

    public String getName() {
        return name;
    }

    public List<String> getTags() {
        return Collections.singletonList("feat/" + MarkdownWriter.slugifier().slugify(name));
    }

    public String getText() {
        return String.join("\n", text.content);
    }

    @Override
    public String toString() {
        return "FeatType [name=" + name + "]";
    }
}
