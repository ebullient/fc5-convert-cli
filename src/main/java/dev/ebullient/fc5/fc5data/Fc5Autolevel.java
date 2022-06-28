package dev.ebullient.fc5.fc5data;

import java.util.Collections;
import java.util.List;

import dev.ebullient.fc5.pojo.QuteClassAutoLevel;
import io.quarkus.qute.TemplateData;

/**
 * <p>
 * Java class for autolevelType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="autolevelType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="feature" type="{}featureType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="slots" type="{}slotsType" minOccurs="0"/>
 *         &lt;element name="counter" type="{}counterType" minOccurs="0"/>
 *       &lt;/choice>
 *       &lt;attribute name="level" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="scoreImprovement" type="{}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@TemplateData
public class Fc5Autolevel extends QuteClassAutoLevel {
    public static final Fc5Autolevel NONE = new Fc5Autolevel();

    final List<Fc5ClassFeature> features;

    private Fc5Autolevel() {
        super();
        features = List.of();
    }

    Fc5Autolevel(int level, boolean scoreImprovement,
            List<Fc5ClassFeature> features, List<Counter> counters,
            SpellSlots slots) {
        super(level, scoreImprovement, List.of(), counters, slots);
        this.features = features;
    }

    @Override
    public boolean hasContent() {
        return super.hasContent() || !features.isEmpty();
    }

    public static Object buildCounter(Fc5ParsingContext myElements) {
        String value = myElements.getOrDefault("value", null);
        return new Counter(
                myElements.getOrDefault("name", null),
                value == null ? null : Integer.valueOf(value),
                getResetFrom(myElements));
    }

    static Reset getResetFrom(Fc5ParsingContext myElements) {
        String value = myElements.getOrDefault("reset", null);
        return value == null ? null : Reset.valueOf(value);
    }

    public static class Fc5Builder extends Builder {
        List<Fc5ClassFeature> features;

        public Fc5Builder(Fc5ParsingContext myElements) {
            setLevel(myElements.getOrDefault("level", 1));
            setScoreImprovement(myElements.getOrDefault("scoreImprovement", false));
            addCounters(myElements.getOrDefault("counter", Collections.emptyList()));

            this.features = myElements.getOrDefault("feature", Collections.emptyList());
            this.slots = myElements.getOrDefault("slots", SpellSlots.NONE);
        }

        public Fc5Autolevel build() {
            return new Fc5Autolevel(level, scoreImprovement, features, counters, slots);
        }
    }
}
