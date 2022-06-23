package dev.ebullient.fc5.fc5data;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dev.ebullient.fc5.pojo.MdFeature;
import dev.ebullient.fc5.pojo.MdProficiency;
import dev.ebullient.fc5.pojo.Modifier;
import io.quarkus.qute.TemplateData;

/**
 * <p>
 * Java class for featureType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="featureType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="text" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="special" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="modifier" type="{}modifierType"/>
 *         &lt;element name="proficiency" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/choice>
 *       &lt;attribute name="optional" type="{}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@TemplateData
public class Feature extends MdFeature {

    public static Comparator<Feature> alphabeticalFeatureSort = new Comparator<>() {
        @Override
        public int compare(Feature o1, Feature o2) {
            return o1.name.compareTo(o2.name);
        }
    };

    final Text text;
    final List<String> special;
    final List<Modifier> modifier;
    final MdProficiency proficiency;
    final boolean isOptional;

    public Feature(String name, int level, Text text, List<String> special, List<Modifier> modifier, MdProficiency proficiency,
            boolean isOptional) {
        super(name, level, List.of());
        this.text = text;
        this.special = special;
        this.modifier = modifier;
        this.proficiency = proficiency;
        this.isOptional = isOptional;
    }

    @Override
    public String getText() {
        return String.join("\n", text.content);
    }

    static class FeatureBuilder extends Builder {
        Text text;
        List<String> special;
        List<Modifier> modifier;
        MdProficiency proficiency;
        boolean isOptional;

        public FeatureBuilder(ParsingContext context) {
            setName(context.getOrFail(context.owner, "name", String.class));
            setLevel(context.getOrDefault("level", 0));

            this.text = context.getOrDefault("text", Text.NONE);
            this.special = context.getOrDefault("special", Collections.emptyList());
            this.modifier = context.getOrDefault("modifier", Collections.emptyList());
            this.proficiency = context.getOrDefault("proficiency", MdProficiency.NONE);
            this.isOptional = context.getOrDefault("optional", false);
        }

        public Feature build() {
            return new Feature(name, level, text, special, modifier, proficiency, isOptional);
        }
    }
}
