package dev.ebullient.fc5.fc5data;

import java.util.Collections;
import java.util.List;

import dev.ebullient.fc5.pojo.Proficiency;
import dev.ebullient.fc5.pojo.QuteTrait;
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
public class Fc5Trait extends QuteTrait {

    final Fc5Text fc5Text;

    public Fc5Trait(String name, Fc5Text fc5Text, List<String> diceRolls, List<String> attacks,
            String recharge, Proficiency proficiency) {
        super(name, List.of(), diceRolls, attacks, recharge, proficiency);
        this.fc5Text = fc5Text;
    }

    @Override
    public String getText() {
        return String.join("\n", fc5Text.content).trim();
    }

    static class TraitTypeBuilder extends Builder {
        Fc5Text fc5Text;

        public TraitTypeBuilder(Fc5ParsingContext context) {
            setName(context.getOrDefault("name", ""));

            List<String> rolls = context.getOrDefault("attack", Collections.emptyList());
            this.attacks.addAll(rolls);

            rolls = context.getOrDefault("roll", Collections.emptyList());
            this.diceRolls.addAll(rolls);

            // TODO: Proficiency?
            // TODO: Recharge?

            fc5Text = context.getOrDefault("text", Fc5Text.NONE);
        }

        @Override
        public Fc5Trait build() {
            return new Fc5Trait(name, fc5Text, diceRolls, attacks, recharge, proficiency);
        }
    }
}
