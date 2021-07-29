package dev.ebullient.fc5.model;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>Java class for compendiumType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="compendiumType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="item" type="{}itemType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="race" type="{}raceType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="class" type="{}classType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="feat" type="{}featType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="background" type="{}backgroundType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="spell" type="{}spellType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="monster" type="{}monsterType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/choice>
 *       &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}byte" />
 *       &lt;attribute name="auto_indent" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class CompendiumType {

    final List<BackgroundType> backgrounds = new ArrayList<>();
    final List<ClassType> classes = new ArrayList<>();
    final List<FeatType> feats = new ArrayList<>();
    final List<ItemType> items = new ArrayList<>();
    final List<MonsterType> monsters = new ArrayList<>();
    final List<RaceType> races = new ArrayList<>();
    final List<SpellType> spells = new ArrayList<>();

    public CompendiumType(Node compendiumRoot) {
        if ( compendiumRoot.hasChildNodes() ) {
            NodeList nodeList = compendiumRoot.getChildNodes();
            for(int i = 0; i < nodeList.getLength(); i++) {
                Node child = nodeList.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    Map<String, Object> elements = NodeParser.parseNodeElements(child);
                    switch(child.getNodeName()) {
                        case "background" : 
                            backgrounds.add(new BackgroundType(elements));
                            break;
                        case "class" : 
                            classes.add(new ClassType(elements));
                            break;
                        case "feat" : 
                            feats.add(new FeatType(elements));
                            break;
                        case "item" : 
                            items.add(new ItemType(elements));
                            break;
                        case "monster" : 
                            monsters.add(new MonsterType(elements));
                            break;
                        case "race" : 
                            races.add(new RaceType(elements));
                            break;
                        case "spell" : 
                            spells.add(new SpellType(elements));
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown element in compendium: " + child.getNodeName());
                    }
                }
            }            
        }
    }

    @Override
    public String toString() {
        return "CompendiumType [backgrounds=" + backgrounds + ", classes=" + classes + ", feats=" + feats + ", items="
                + items + ", monsters=" + monsters + ", races=" + races + ", spells=" + spells + "]";
    }

    public static final CompendiumType readCompendium(DocumentBuilder db, InputStream is, String systemId) throws Exception {
        Document doc = db.parse(is, systemId);
        Node compendiumNode = doc.getFirstChild();
        assert("compendium".equals(compendiumNode.getNodeName()));
        return new CompendiumType(compendiumNode);
    }
}
