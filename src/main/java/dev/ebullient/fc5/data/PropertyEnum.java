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
public enum PropertyEnum implements ConvertedEnumType {
    AMMUNITION("Ammunition", "A"),
    FINESSE("Finesse", "F"),
    HEAVY("Heavy", "H"),
    LIGHT("Light", "L"),
    LOADING("Loading", "LD"),
    REACH("Reach", "R"),
    SPECIAL("Special", "S"),
    THROWN("Thrown", "T"),
    TWO_HANDED("Two-handed", "2H"),
    VERSATILE("Versatile", "V"),
    MARTIAL("Martial", "M"),
    UNKNOWN("Unknown", "");

    private final String longName;
    private final String xmlValue;

    private PropertyEnum(String longName, String xmlType) {
        this.longName = longName;
        this.xmlValue = xmlType;
    }

    @Override
    public String value() {
        return longName;
    }

    @Override
    public String getXmlValue() {
        return xmlValue;
    }

    public String getMarkdownLink() {
        return String.format("[%s](%s)", longName, "/reference/item/weapon-properties.md#" + longName);
    }

    public static PropertyEnum fromXmlType(String v) {
        if (v == null || v.isBlank()) {
            return null;
        }
        for (PropertyEnum p : PropertyEnum.values()) {
            if (p.xmlValue.equals(v)) {
                return p;
            }
        }
        throw new IllegalArgumentException("Invalid/Unknown property " + v);
    }

    public static List<PropertyEnum> fromXmlValue(String v) {
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
}
