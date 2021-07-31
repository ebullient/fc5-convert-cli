package dev.ebullient.fc5.data;

import io.quarkus.qute.TemplateData;

/**
 * <p>
 * Java class for modifierType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
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
@TemplateData
public class Modifier {
    public static final Modifier NONE = new Modifier();

    final String value;
    final CategoryEnum category;

    private Modifier() {
        value = "";
        category = CategoryEnum.UNKNOWN;
    }

    public Modifier(String value, CategoryEnum category) {
        if (category == CategoryEnum.UNKNOWN) {
            throw new IllegalArgumentException("Modifier " + value + " is missing required category");
        }
        this.value = value;
        this.category = category;
    }

    public String getCategory() {
        return category.longName();
    }

    public String getValue() {
        return value.replace("ac", "AC");
    }
}
