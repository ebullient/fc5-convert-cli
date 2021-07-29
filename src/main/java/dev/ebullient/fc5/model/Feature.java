package dev.ebullient.fc5.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

/**
 * <p>Java class for featureType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
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
public class Feature {

    final String name;
    final Text text;
    final List<String> special;
    final List<Modifier> modifier;
    final Proficiency proficiency;
    final boolean isOptional;

    public Feature(Node node) {
        Node optionalAttribute = node.getAttributes().getNamedItem("optional");
        this.isOptional = optionalAttribute == null ? false : NodeParser.parseBoolean(optionalAttribute.getTextContent());

        Map<String, Object> elements = NodeParser.parseNodeElements(node);
        name = NodeParser.getOrDefault(elements, "name", "unknown");
        text = NodeParser.getOrDefault(elements, "text", Text.NONE);
        special = NodeParser.getOrDefault(elements, "special", Collections.emptyList());
        modifier = NodeParser.getOrDefault(elements, "modifier", Collections.emptyList());
        proficiency = NodeParser.getOrDefault(elements, "proficiency", Proficiency.STRING);
        proficiency.setFlavor("string");
    }

    @Override
    public String toString() {
        return "FeatureType [nameOrTextOrSpecial=" + name + "]";
    }
}
