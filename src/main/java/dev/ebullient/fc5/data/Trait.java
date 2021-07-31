package dev.ebullient.fc5.data;

import java.util.Collections;
import java.util.List;

import io.quarkus.qute.TemplateData;

/**
 * <p>
 * Java class for traitType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="traitType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="text" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="attack" type="{}attackType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="special" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@TemplateData
public class Trait {

    final String name;
    final Text text;
    final List<String> attack;
    final List<String> special;

    public Trait(ParsingContext context) {
        name = context.getOrDefault("name", "");

        text = context.getOrDefault("text", Text.NONE);
        attack = context.getOrDefault("attack", Collections.emptyList());
        special = context.getOrDefault("special", Collections.emptyList());
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return String.join("\n", text.content);
    }

    public List<String> getAttack() {
        return attack;
    }

    public List<String> getSpecial() {
        return special;
    }
}
