package dev.ebullient.fc5.fc5data;

import java.util.Collections;
import java.util.List;

import dev.ebullient.fc5.pojo.BaseType;
import dev.ebullient.fc5.pojo.MdFeat;
import dev.ebullient.fc5.pojo.MdProficiency;
import dev.ebullient.fc5.pojo.Modifier;
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
public class FeatType extends MdFeat implements BaseType {

    final String prerequisite;
    final Text text;
    final MdProficiency proficiency;
    final List<Modifier> modifier;

    public FeatType(ParsingContext context) {
        super(context.getOrFail(context.owner, "name", String.class));

        prerequisite = context.getOrDefault("prerequisite", "");
        text = context.getOrDefault("text", Text.NONE);

        proficiency = context.getOrDefault("proficiency", MdProficiency.NONE);
        modifier = context.getOrDefault("modifier", Collections.emptyList());
    }

    @Override
    public String getText() {
        return String.join("\n", text.content);
    }

}
