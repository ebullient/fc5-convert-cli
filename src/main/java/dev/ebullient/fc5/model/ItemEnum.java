package dev.ebullient.fc5.model;


/**
 * <p>Java class for itemEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="itemEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="LA"/>
 *     &lt;enumeration value="MA"/>
 *     &lt;enumeration value="HA"/>
 *     &lt;enumeration value="S"/>
 *     &lt;enumeration value="M"/>
 *     &lt;enumeration value="R"/>
 *     &lt;enumeration value="A"/>
 *     &lt;enumeration value="RD"/>
 *     &lt;enumeration value="ST"/>
 *     &lt;enumeration value="WD"/>
 *     &lt;enumeration value="RG"/>
 *     &lt;enumeration value="P"/>
 *     &lt;enumeration value="SC"/>
 *     &lt;enumeration value="W"/>
 *     &lt;enumeration value="G"/>
 *     &lt;enumeration value="$"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
public enum ItemEnum {

    LA,
    MA,
    HA,
    S,
    M,
    R,
    A,
    RD,
    ST,
    WD,
    RG,
    P,
    SC,
    W,
    G,
    $, 
    UNKNOWN;

    public String value() {
        return name();
    }

    public static ItemEnum fromValue(String v) {
        return valueOf(v);
    }
    

}
