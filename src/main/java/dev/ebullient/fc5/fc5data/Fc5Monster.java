package dev.ebullient.fc5.fc5data;

import java.util.Collections;
import java.util.regex.Pattern;

import dev.ebullient.fc5.pojo.QuteMonster;
import dev.ebullient.fc5.pojo.SizeEnum;
import io.quarkus.qute.TemplateData;

/**
 * <p>
 * Java class for monsterType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="monsterType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="size" type="{}sizeEnum"/>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="alignment" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ac" type="{}acType"/>
 *         &lt;element name="hp" type="{}hpType"/>
 *         &lt;element name="speed" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="str" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="dex" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="con" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="int" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="wis" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="cha" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="save" type="{}abilityBonusList"/>
 *         &lt;element name="skill" type="{}skillBonusList"/>
 *         &lt;element name="resist" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="vulnerable" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="immune" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="conditionImmune" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="senses" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="passive" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="languages" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="cr" type="{}crType"/>
 *         &lt;element name="trait" type="{}traitType"/>
 *         &lt;element name="action" type="{}traitType"/>
 *         &lt;element name="legendary" type="{}traitType"/>
 *         &lt;element name="reaction" type="{}traitType"/>
 *         &lt;element name="spells" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="slots" type="{}slotsType"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="environment" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@TemplateData
public class Fc5Monster extends QuteMonster {
    static final Pattern TYPE_DETAIL = Pattern.compile("(.+?) \\((.+?)\\)");

    final Fc5Text descriptionText;

    public Fc5Monster(MonsterTypeBuilder builder) {
        super(builder);
        this.descriptionText = builder.descriptionText;
    }

    @Override
    public String getDescription() {
        return String.join("\n", descriptionText.content).trim();
    }

    static class MonsterTypeBuilder extends Builder {
        Fc5Text descriptionText;

        public MonsterTypeBuilder(Fc5ParsingContext context) {
            setName(context.getOrFail(context.owner, "name", String.class));
            setSize(context.getOrDefault("size", SizeEnum.MEDIUM));
            setType(context.getOrDefault("type", ""));
            setAlignment(context.getOrDefault("alignment", ""));
            setAc(context.getOrDefault("ac", ""));
            setHpDice(context.getOrDefault("hp", ""));
            setSpeed(context.getOrDefault("speed", ""));

            setScores(context.getOrDefault("str", 10),
                    context.getOrDefault("dex", 10),
                    context.getOrDefault("con", 10),
                    context.getOrDefault("int", 10),
                    context.getOrDefault("wis", 10),
                    context.getOrDefault("cha", 10));

            setSave(context.getOrDefault("save", Collections.emptyList()));
            setSkill(context.getOrDefault("skill", Collections.emptyList()));

            setResist(context.getOrDefault("resist", ""));
            setVulnerable(context.getOrDefault("vulnerable", ""));
            setImmune(context.getOrDefault("immune", ""));
            setConditionImmune(context.getOrDefault("conditionImmune", ""));

            setSenses(context.getOrDefault("senses", ""),
                    context.getOrDefault("passive", 10));

            setLanguages(context.getOrDefault("languages", ""));
            setCr(context.getOrDefault("cr", "0"));
            setTrait(context.getOrDefault("trait", Collections.emptyList()));
            setAction(context.getOrDefault("action", Collections.emptyList()));
            setLegendary(context.getOrDefault("legendary", Collections.emptyList()));
            setReaction(context.getOrDefault("reaction", Collections.emptyList()));
            setEnvironment(context.getOrDefault("environment", ""));

            descriptionText = new Fc5Text(Collections.singletonList(context.getOrDefault("description", "")));
        }

        @Override
        public Fc5Monster build() {
            return new Fc5Monster(this);
        }
    }
}
