package dev.ebullient.fc5.fc5data;

import java.util.Collections;
import java.util.List;

import dev.ebullient.fc5.pojo.Modifier;
import dev.ebullient.fc5.pojo.Proficiency;
import dev.ebullient.fc5.pojo.QuteClassFeature;
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
public class Fc5ClassFeature extends QuteClassFeature {

    final Fc5Text text;

    public Fc5ClassFeature(String name, int level, boolean optional, Fc5Text text,
            List<Modifier> modifiers, List<String> special, Proficiency proficiency, String sortingGroup) {
        super(name, level, optional, List.of(), modifiers, special, proficiency, sortingGroup);
        this.text = text;
    }

    @Override
    public String getText() {
        return String.join("\n", text.content);
    }

    static class FeatureBuilder extends Builder {
        Fc5Text text;

        public FeatureBuilder(Fc5ParsingContext context) {
            setName(context.getOrFail(context.owner, "name", String.class));
            setLevel(context.getOrDefault("level", 0));
            addModifiers(context.getOrDefault("modifier", Collections.emptyList()));
            setProficiency(context.getOrDefault("proficiency", Proficiency.NONE));
            setOptional(context.getOrDefault("optional", false));

            this.text = context.getOrDefault("text", Fc5Text.NONE);
            this.special = context.getOrDefault("special", Collections.emptyList());
        }

        public Fc5ClassFeature build() {
            return new Fc5ClassFeature(name, level, optional, this.text, modifiers, special, proficiency, sortingGroup);
        }
    }
}
