package dev.ebullient.fc5.data;

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import dev.ebullient.fc5.Log;

public class CompendiumXmlReader {

    static final List<String> TOP_LEVEL_ELEMENTS = Arrays.asList("background", "class", "feat", "item", "monster", "race",
            "spell");

    static final List<String> TEXT_CONTENT_ELEMENTS = Arrays.asList("dmg1", "dmg2", "dmgType", "proficiency", "magic",
            "ritual", "stealth", "roll", "school", "size", "spellAbility");

    static final List<String> TRAIT_CONTEXT = Arrays.asList("action", "legendary", "reaction", "trait");

    final XMLInputFactory factory;
    final List<String> ignoredItems = new ArrayList<>();

    public CompendiumXmlReader() {
        factory = XMLInputFactory.newInstance();
    }

    public CompendiumType parseXMLInputStream(InputStream is) throws Exception {
        XMLStreamReader reader = factory.createXMLStreamReader(is);
        CompendiumType compendiumType = new CompendiumType();
        Deque<ParsingContext> contextStack = new ArrayDeque<>();
        Deque<Element> elementStack = new ArrayDeque<>();

        while (reader.hasNext()) {
            reader.next();
            switch (reader.getEventType()) {
                case XMLStreamReader.START_ELEMENT: {
                    String name = reader.getLocalName();
                    final Element element;
                    if ("autolevel".equals(name)) {
                        element = new AutolevelContextElement(reader, contextStack, compendiumType);
                    } else if ("feature".equals(name)) {
                        element = new FeatureContextElement(reader, contextStack, compendiumType);
                    } else if (TOP_LEVEL_ELEMENTS.contains(name) || TRAIT_CONTEXT.contains(name)) {
                        element = new ContextElement(reader, contextStack, compendiumType);
                    } else {
                        element = new Element(reader, contextStack);
                    }
                    elementStack.push(element);
                    break;
                }
                case XMLStreamReader.CHARACTERS: {
                    Element element = elementStack.peek();
                    element.characters(reader);
                    break;
                }
                case XMLStreamReader.END_ELEMENT: {
                    Element element = elementStack.pop();
                    element.endElement(reader, contextStack);
                    break;
                }
            }
        }
        return compendiumType;
    }

    class Element {
        ParsingContext context;
        String name;
        List<String> content;
        Map<String, String> attributes;

        Element(XMLStreamReader reader, Deque<ParsingContext> contextStack) {
            name = reader.getLocalName();
            content = new ArrayList<>();
            if (reader.getAttributeCount() > 0) {
                attributes = new HashMap<>();
                for (int i = 0; i < reader.getAttributeCount(); i++) {
                    attributes.put(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
                }
            } else {
                attributes = Collections.emptyMap();
            }
        }

        public void characters(XMLStreamReader reader) {
            content.add(reader.getText());
        }

        public void endElement(XMLStreamReader reader, Deque<ParsingContext> contextStack) {
            ParsingContext context = contextStack.peek();
            if (context == null) {
                ignoredItems.add(name); // e.g. compendium element itself
            } else {
                context.put(name, wrapContent(this));
            }
        }

        @Override
        public String toString() {
            return "element=" + name;
        }
    }

    class ContextElement extends Element {
        final ParsingContext myElements;
        final CompendiumType compendium;

        ContextElement(XMLStreamReader reader, Deque<ParsingContext> contextStack, CompendiumType compendiumType) {
            super(reader, contextStack);
            this.compendium = compendiumType;
            this.myElements = createContext();
            contextStack.push(myElements);
            if (TOP_LEVEL_ELEMENTS.contains(name)) {
                Log.debugf("--- beg %s", name);
            }
        }

        protected ParsingContext createContext() {
            return new ParsingContext(name);
        }

        @Override
        public void characters(XMLStreamReader reader) {
        }

        @Override
        public void endElement(XMLStreamReader reader, Deque<ParsingContext> contextStack) {
            contextStack.pop(); // pop this nested context
            ParsingContext parentContext = contextStack.peek();
            if (TOP_LEVEL_ELEMENTS.contains(name)) {
                createCompendiumEntry(compendium, this);
                Log.debugf("--- end %s: %s", name, myElements.elements.get("name"));
            } else if (parentContext != null) {
                parentContext.put(name, new Trait(myElements));
            }
        }
    }

    class FeatureContextElement extends ContextElement {
        FeatureContextElement(XMLStreamReader reader, Deque<ParsingContext> contextStack,
                CompendiumType compendiumType) {
            super(reader, contextStack, compendiumType);
            myElements.put("optional", parseBoolean(attributes.get("optional")));
        }

        @Override
        protected ParsingContext createContext() {
            return new ParsingContext.FeatureParsingContext(name);
        }

        @Override
        public void endElement(XMLStreamReader reader, Deque<ParsingContext> contextStack) {
            contextStack.pop(); // pop this nested context
            ParsingContext parentContext = contextStack.peek();

            myElements.put("level", parentContext.elements.get("level"));
            parentContext.put(name, new Feature(myElements));
        }
    }

    class AutolevelContextElement extends ContextElement {
        AutolevelContextElement(XMLStreamReader reader, Deque<ParsingContext> contextStack,
                CompendiumType compendiumType) {
            super(reader, contextStack, compendiumType);
            myElements.put("level", attributes.get("level"));
            myElements.put("scoreImprovement", parseBoolean(attributes.get("scoreImprovement")));
        }

        @Override
        public void endElement(XMLStreamReader reader, Deque<ParsingContext> contextStack) {
            contextStack.pop(); // pop this nested context
            ParsingContext parentContext = contextStack.peek();
            parentContext.put(name, new Autolevel(myElements));
        }
    }

    private Object wrapContent(Element element) {
        String content = flatten(element.content);
        switch (element.name) {
            case "magic":
            case "ritual":
            case "stealth":
                return parseBoolean(content);
            case "dmg1":
            case "dmg2":
            case "roll":
                return new Roll(content);
            case "dmgType":
                return DamageEnum.fromXmlValue(content);
            case "modifier":
                CategoryEnum category = CategoryEnum.fromValue(element.attributes.get("category"));
                return new Modifier(content, category);
            case "proficiency":
                return new Proficiency(content);
            case "school":
                return SchoolEnum.fromXmlValue(content);
            case "slots":
                boolean optional = parseBoolean(element.attributes.get("optional"));
                return new SpellSlots(content, optional);
            case "size":
                return SizeEnum.fromXmlValue(content);
            case "spellAbility":
                return AbilityEnum.fromXmlValue(content);
            default:
                return content;
        }
    }

    String flatten(List<String> content) {
        return String.join("", content);
    }

    Boolean parseBoolean(String value) {
        if (value == null) {
            return false;
        }
        switch (value.toLowerCase()) {
            case "yes":
            case "true":
            case "1":
                return true;
            default:
                return false;
        }
    }

    void createCompendiumEntry(CompendiumType compendium, ContextElement element) {
        ParsingContext context = element.myElements;
        switch (element.name) {
            case "background":
                compendium.getBackgrounds().add(new BackgroundType(context));
                break;
            case "class":
                compendium.getClasses().add(new ClassType(context));
                break;
            case "feat":
                compendium.getFeats().add(new FeatType(context));
                break;
            case "item":
                compendium.getItems().add(new ItemType(context));
                break;
            case "monster":
                compendium.getMonsters().add(new MonsterType(context));
                break;
            case "race":
                compendium.getRaces().add(new RaceType(context));
                break;
            case "spell":
                compendium.getSpells().add(new SpellType(context));
                break;
        }
    }
}
