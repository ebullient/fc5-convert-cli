package dev.ebullient.fc5.data;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

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

    public Autolevel(Node node) {
        Node levelAttribute = node.getAttributes().getNamedItem("level");
        this.level = Integer.valueOf(levelAttribute.getTextContent());

        Node siAttribute = node.getAttributes().getNamedItem("scoreImprovement");
        this.scoreImprovement = siAttribute == null ? false : NodeParser.parseBoolean(siAttribute.getTextContent());

        Map<String, Object> elements = NodeParser.parseNodeElements(node);
        features = NodeParser.getOrDefault(elements, "feature", Collections.emptyList());
        slots = NodeParser.getOrDefault(elements, "slots", SpellSlots.NONE);
        counters = NodeParser.getOrDefault(elements, "counter", Collections.emptyList());
    }

    public List<Feature> getFeatures() {
        return features;
    }
}
