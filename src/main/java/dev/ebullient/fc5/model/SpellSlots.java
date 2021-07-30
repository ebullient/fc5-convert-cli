package dev.ebullient.fc5.model;

import org.w3c.dom.Node;

import io.quarkus.qute.TemplateData;

/**
 * <p>
 * Java class for slotsType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="slotsType">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;>integerList">
 *       &lt;attribute name="optional" type="{}boolean" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@TemplateData
public class SpellSlots {
    public static final SpellSlots NONE = new SpellSlots("");

    final String textContent;
    final boolean optional;

    private SpellSlots(String textContent) {
        this.textContent = textContent;
        optional = true;
    }

    public SpellSlots(Node node) {
        textContent = node.getTextContent();

        Node attribute = node.getAttributes().getNamedItem("optional");
        optional = attribute == null ? false : NodeParser.parseBoolean(attribute.getTextContent());
    }

}
