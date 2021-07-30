package dev.ebullient.fc5.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import io.quarkus.qute.TemplateData;

/**
 * <p>
 * Java class for featureType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
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
@TemplateData
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

        String nameText = null;
        List<String> textStrings = new ArrayList<>();
        List<String> specialList = null;
        List<Modifier> modifierList = null;
        Proficiency profContent = null;

        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                switch (child.getNodeName()) {
                    case "name": {
                        if (nameText == null) {
                            nameText = child.getTextContent();
                        } else {
                            textStrings.add("## " + child.getTextContent());
                        }
                        break;
                    }
                    case "modifier": {
                        if (modifierList == null) {
                            modifierList = new ArrayList<>();
                        }
                        modifierList.add(NodeParser.convertObject(NodeParser.parseNode(child), Modifier.NONE));
                        break;
                    }
                    case "proficiency": {
                        if (profContent != null) {
                            throw new IllegalArgumentException("Multiple proficiency definitions");
                        }
                        profContent = NodeParser.convertObject(NodeParser.parseNode(child), Proficiency.STRING);
                        profContent.setFlavor("string");
                        break;
                    }
                    case "special": {
                        if (specialList == null) {
                            specialList = new ArrayList<>();
                        }
                        specialList.add(child.getTextContent());
                        break;
                    }
                    case "text": {
                        textStrings.add(child.getTextContent());
                        break;
                    }
                }
            }
        }

        this.name = nameText;
        this.modifier = modifierList == null ? Collections.emptyList() : modifierList;
        this.proficiency = profContent;
        this.special = specialList == null ? Collections.emptyList() : specialList;
        this.text = new Text(textStrings);
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return String.join("\n", text.content);
    }

    @Override
    public String toString() {
        return "FeatureType [name=" + name + "]";
    }
}
