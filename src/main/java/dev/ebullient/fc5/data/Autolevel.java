package dev.ebullient.fc5.data;

import java.util.Collections;
import java.util.List;

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
public class Autolevel {
    public static final Autolevel NONE = new Autolevel();

    final int level;
    final boolean scoreImprovement;
    final List<Feature> features;
    final SpellSlots slots;
    final List<String> counters;

    private Autolevel() {
        level = 0;
        scoreImprovement = false;
        features = Collections.emptyList();
        slots = SpellSlots.NONE;
        counters = Collections.emptyList();
    }

    public Autolevel(ParsingContext myElements) {
        String name = myElements.owner + " autolevel";
        this.level = myElements.getOrDefault(name, "level", 1);
        this.scoreImprovement = myElements.getOrDefault(name, "scoreImprovement", false);
        this.features = myElements.getOrDefault(name, "feature", Collections.emptyList());
        this.slots = myElements.getOrDefault(name, "slots", SpellSlots.NONE);
        this.counters = myElements.getOrDefault(name, "counter", Collections.emptyList());
    }

    public List<Feature> getFeatures() {
        return features;
    }
}
