package dev.ebullient.fc5.fc5data;

import java.util.Collections;
import java.util.List;

import dev.ebullient.fc5.pojo.Modifier;
import dev.ebullient.fc5.pojo.Proficiency;
import dev.ebullient.fc5.pojo.QuteFeat;
import dev.ebullient.fc5.pojo.QuteSource;
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
public class Fc5Feat extends QuteFeat implements QuteSource {

    final Fc5Text text;

    public Fc5Feat(String name, Fc5Text text, Proficiency proficiency, List<Modifier> modifier, String prerequisite) {
        super(name, List.of(), proficiency, modifier, prerequisite);
        this.text = text;
    }

    @Override
    public String getText() {
        return String.join("\n", text.content);
    }

    static class Fc5FeatBuilder extends Builder {
        Fc5Text text;

        public Fc5FeatBuilder(Fc5ParsingContext context) {
            setName(context.getOrFail(context.owner, "name", String.class));
            setPrerequisite(context.getOrDefault("prerequisite", ""));
            setProficiency(context.getOrDefault("proficiency", Proficiency.NONE));
            setModifiers(context.getOrDefault("modifier", Collections.emptyList()));
            text = context.getOrDefault("text", Fc5Text.NONE);
        }

        public Fc5FeatBuilder setText(Fc5Text text) {
            this.text = text;
            return this;
        }

        public Fc5Feat build() {
            return new Fc5Feat(name, text, proficiency, modifier, prerequisite);
        }
    }
}
