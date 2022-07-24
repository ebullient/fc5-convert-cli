package dev.ebullient.fc5.fc5data;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import dev.ebullient.fc5.pojo.Proficiency;
import dev.ebullient.fc5.pojo.QuteClass;
import dev.ebullient.fc5.pojo.QuteClassFeature;
import dev.ebullient.fc5.pojo.SkillOrAbility;
import io.quarkus.qute.TemplateData;

/**
 * <p>
 * Java class for classType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="classType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="hd" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="proficiency" type="{}abilityAndSkillList"/>
 *         &lt;element name="spellAbility" type="{}abilityEnum"/>
 *         &lt;element name="numSkills" type="{}integer"/>
 *         &lt;element name="autolevel" type="{}autolevelType"/>
 *         &lt;element name="armor" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="weapons" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="tools">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               ...
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="wealth" type="{}rollFormula"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@TemplateData
public class Fc5Class extends QuteClass {
    final String wealth;
    final SkillOrAbility spellAbility;
    final List<Fc5Autolevel> autolevel;

    public Fc5Class(String name, int hitDice, Proficiency proficiency, int numSkills,
            String armor, String weapons, String tools,
            String wealth, SkillOrAbility spellAbility, List<Fc5Autolevel> autolevel) {
        super(name, hitDice, proficiency, numSkills, armor, weapons, tools, List.of());

        this.wealth = wealth;
        this.spellAbility = spellAbility;
        this.autolevel = autolevel;
    }

    @Override
    public Stream<? extends QuteClassFeature> getLevelFeatures() {
        return autolevel.stream().flatMap(x -> x.features.stream());
    }

    @Override
    public String toString() {
        return "ClassType [name=" + name + "]";
    }

    public static class ClassBuilder extends Builder {
        final String wealth;
        final SkillOrAbility spellAbility;
        final List<Fc5Autolevel> autolevel;

        public ClassBuilder(Fc5ParsingContext context) {
            setName(context.getOrFail(context.owner, "name", String.class));
            setHitDice(context.getOrDefault("hd", 8));
            setProficiency(context.getOrDefault("proficiency", Proficiency.NONE));
            setNumSkills(context.getOrDefault("numSkills", 0));
            setArmor(context.getOrDefault("armor", NONE));
            setWeapons(context.getOrDefault("weapons", NONE));
            setTools(context.getOrDefault("tools", NONE));

            wealth = context.getOrDefault("wealth", "");
            spellAbility = context.getOrDefault("spellAbility", SkillOrAbility.None);
            autolevel = context.getOrDefault("autolevel", Collections.emptyList());
        }

        @Override
        public Fc5Class build() {
            return new Fc5Class(name, hitDice, proficiency, numSkills,
                    armor, weapons, tools, wealth, spellAbility, autolevel);
        }
    }
}
