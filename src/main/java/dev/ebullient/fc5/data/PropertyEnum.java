package dev.ebullient.fc5.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.quarkus.qute.TemplateData;

/**
 * <pre>
 * &lt;xs:simpleType name="propertyList">
 *   &lt;xs:restriction base="xs:string">
 *     &lt;xs:pattern value="(A|F|H|L|LD|R|S|T|2H|V|M)*(, ?(A|F|H|L|LD|R|S|T|2H|V|M)*)*"/>
 *   &lt;/xs:restriction>
 * &lt;/xs:simpleType>
 * </pre>
 */
@TemplateData
public enum PropertyEnum {
    ammunition("Ammunition", "A"),
    finesse("Finesse", "F"),
    heavy("Heavy", "H"),
    light("Light", "L"),
    loading("Loading", "LD"),
    reach("Reach", "R"),
    special("Special", "S"),
    thrown("Thrown", "T"),
    twoHanded("Two-handed", "2H"),
    versatile("Versatile", "V"),
    m("m", "M");

    private final String longName;
    private final String xmlKey;

    private PropertyEnum(String longName, String xmlType) {
        this.longName = longName;
        this.xmlKey = xmlType;
    }

    public static PropertyEnum fromXmlType(String v) {
        if (v == null || v.isBlank()) {
            return null;
        }
        for (PropertyEnum p : PropertyEnum.values()) {
            if (p.xmlKey.equals(v)) {
                return p;
            }
        }
        return null;
    }

    public static List<PropertyEnum> fromPropertyString(String v) {
        if (v == null || v.isBlank()) {
            return Collections.emptyList();
        }
        List<PropertyEnum> result = new ArrayList<>();
        for (String s : v.split("\\s*,\\s*")) {
            result.add(fromXmlType(s));
        }
        result.removeIf(x -> x == null);
        return result;
    }

    public String getLongName() {
        return longName;
    }

    public String getXmlKey() {
        return xmlKey;
    }

    public String getMarkdownLink() {
        return String.format("[%s](%s)", longName, "/reference/item/weapon-properties.md#" + longName);
    }
}
