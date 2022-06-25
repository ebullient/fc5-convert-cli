package dev.ebullient.fc5.pojo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    SILVERED("Silvered", "-"),
    POISON("Poison", "="),
    CURSED("Cursed Item", "*"),

    // Additional properties
    AMMUNITION_FUTURISTIC("Ammunition (Futuristic)", "AF"),
    BURST_FIRE("Burst Fire", "BF"),
    RELOAD("Reload", "RLD"),

    // Magic item attributes
    MAJOR("Major", "!"),
    MINOR("Minor", "@"),
    REQ_ATTUNEMENT("Requires Attunement", "#"),
    OPT_ATTUNEMENT("Optional Attunement", "$"),
    COMMON("Common", "1"),
    UNCOMMON("Uncommon", "2"),
    RARE("Rare", "3"),
    VERY_RARE("Very Rare", "4"),
    LEGENDARY("Legendary", "5"),
    ARTIFACT("Artifact", "6"),
    VARIES("varies", "7"),
    UNKNOWN("unknown", "8"),
    RARITY_UNK_MAGIC("unknown (magic)", "9");

    private final String longName;
    private final String encodedValue;
    private final boolean stdProperty; // used in json and xml
    private final boolean weapon; // can apply to weapons
    private final boolean rarity; // can apply to weapons

    PropertyEnum(String longName, String ev) {
        this.longName = longName;
        this.encodedValue = ev;
        this.stdProperty = ordinal() < 11;
        this.weapon = stdProperty || ev.equals("-") || ev.equals("*"); // std properties or silvered or cursed
        this.rarity = !weapon && ev.length() > 0 && (Character.isDigit(ev.charAt(0))); // exclude 2H
    }

    public static final List<PropertyEnum> weaponProperties = Stream.of(PropertyEnum.values())
            .filter(x -> x.weapon)
            .collect(Collectors.toList());

    public static final List<PropertyEnum> tierProperties = List.of(MAJOR, MINOR);

    public static final List<PropertyEnum> rarityProperties = Stream.of(PropertyEnum.values())
            .filter(x -> x.rarity)
            .collect(Collectors.toList());

    public String value() {
        return longName.toLowerCase();
    }

    public String getEncodedValue() {
        return encodedValue;
    }

    public String getMarkdownLink() {
        return weapon
                ? String.format("[%s](%s)", longName, "/rules/weapon-properties.md#" + longName)
                : String.format("[%s](%s)", longName, "/rules/gear-properties.md#" + longName);
    }

    public static PropertyEnum fromValue(String v) {
        if (v == null || v.isBlank()) {
            return null;
        }
        String key = v.toLowerCase();
        for (PropertyEnum p : PropertyEnum.values()) {
            if (p.longName.toLowerCase().equals(key)) {
                return p;
            }
        }
        throw new IllegalArgumentException("Invalid/Unknown property " + v);
    }

    public static PropertyEnum fromEncodedType(String v) {
        if (v == null || v.isBlank()) {
            return null;
        }
        for (PropertyEnum p : PropertyEnum.values()) {
            if (p.encodedValue.equals(v)) {
                return p;
            }
        }
        throw new IllegalArgumentException("Invalid/Unknown property " + v);
    }

    public static List<PropertyEnum> fromEncodedValue(String v) {
        if (v == null || v.isBlank()) {
            return Collections.emptyList();
        }
        List<PropertyEnum> result = new ArrayList<>();
        for (String s : v.split("\\s*,\\s*")) {
            result.add(fromEncodedType(s));
        }
        result.removeIf(Objects::isNull);
        return result;
    }

    public static void findAdditionalProperties(String name, ItemEnum type, List<PropertyEnum> properties,
            Predicate<String> matches) {
        if (type.isWeapon() && name.toLowerCase(Locale.ROOT).contains("silvered")) {
            List<PropertyEnum> result = new ArrayList<>(properties);
            result.add(SILVERED);
        }
        if (matches.test("^Curse: .*")) {
            properties.add(PropertyEnum.CURSED);
        }
        if (matches.test(
                "^(This poison is|This poison was|You can use the poison in|This poison must be harvested|A creature subjected to this poison|A creature that ingests this poison) .*")) {
            properties.add(PropertyEnum.POISON);
        }
        if (matches.test(".*it is actually poison.*")) {
            properties.add(PropertyEnum.POISON);
        }
    }
}
