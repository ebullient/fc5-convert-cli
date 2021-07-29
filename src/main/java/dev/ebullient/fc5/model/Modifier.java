package dev.ebullient.fc5.model;

import org.w3c.dom.Node;


/**
 * <p>Java class for modifierType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="modifierType">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;>modifierValue">
 *       &lt;attribute name="category" type="{}categoryEnum" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 */
public class Modifier {
    public static final Modifier NONE = new Modifier();
    
    final String value;
    final CategoryEnum category;

    private Modifier() {
        value = "";
        category = CategoryEnum.UNKNOWN;
    }

    public Modifier(Node node) {
        value = node.getTextContent();
        Node attribute = node.getAttributes().getNamedItem("category");
        if ( attribute == null ) {
            throw new IllegalArgumentException("Modifier "+ value +" is missing required category");
        }
        category = CategoryEnum.fromValue(attribute.getTextContent());
    }
}
