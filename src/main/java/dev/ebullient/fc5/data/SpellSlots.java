package dev.ebullient.fc5.data;

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
    public static final SpellSlots NONE = new SpellSlots("", true);

    final String textContent;
    final boolean optional;

    public SpellSlots(String textContent, boolean optional) {
        this.textContent = textContent;
        this.optional = optional;
    }
}
