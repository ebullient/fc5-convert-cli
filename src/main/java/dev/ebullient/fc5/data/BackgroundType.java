package dev.ebullient.fc5.data;

import java.util.Collections;
import java.util.List;

import io.quarkus.qute.TemplateData;

/**
 * <p>
 * Java class for backgroundType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
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
@TemplateData
public class BackgroundType implements BaseType {
    final String name;
    final List<Trait> traits;
    final Proficiency proficiency;

    public BackgroundType(ParsingContext context) {
        name = context.getOrFail(context.owner, "name", String.class);

        proficiency = context.getOrDefault(name, "proficiency", Proficiency.NONE);
        traits = context.getOrDefault(name, "trait", Collections.emptyList());
    }

    public String getName() {
        return name;
    }

    public List<Trait> getTrait() {
        return traits;
    }

    public List<String> getTags() {
        return Collections.singletonList("background/" + MarkdownWriter.slugifier().slugify(name));
    }

    public String getProficiency() {
        return proficiency.textContent;
    }

    @Override
    public String toString() {
        return "BackgroundType [name=" + name + "]";
    }
}
