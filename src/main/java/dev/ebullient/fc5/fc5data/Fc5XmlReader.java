package dev.ebullient.fc5.fc5data;

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
import dev.ebullient.fc5.pojo.Modifier;
import dev.ebullient.fc5.pojo.ModifierCategoryEnum;
import dev.ebullient.fc5.pojo.Proficiency;
import dev.ebullient.fc5.pojo.QuteBackground;
import dev.ebullient.fc5.pojo.QuteClassAutoLevel;
import dev.ebullient.fc5.pojo.QuteRace;
import dev.ebullient.fc5.pojo.SchoolEnum;
import dev.ebullient.fc5.pojo.SizeEnum;
import dev.ebullient.fc5.pojo.SkillOrAbility;

public class Fc5XmlReader {

    static final List<String> TOP_LEVEL_ELEMENTS = Arrays.asList("background", "class", "feat", "item", "monster", "race",
            "spell");

    static final List<String> TEXT_CONTENT_ELEMENTS = Arrays.asList("dmg1", "dmg2", "dmgType", "proficiency", "magic",
            "ritual", "stealth", "roll", "school", "size", "spellAbility");

    static final List<String> TRAIT_CONTEXT = Arrays.asList("action", "legendary", "reaction", "trait");

    final XMLInputFactory factory;
    final List<String> ignoredItems = new ArrayList<>();

    public Fc5XmlReader() {
        factory = XMLInputFactory.newInstance();
    }

    public Fc5Compendium parseXMLInputStream(InputStream is) throws Exception {
        XMLStreamReader reader = factory.createXMLStreamReader(is);
        Fc5Compendium compendium = new Fc5Compendium();
        Deque<Fc5ParsingContext> contextStack = new ArrayDeque<>();
        Deque<Element> elementStack = new ArrayDeque<>();

        while (reader.hasNext()) {
            reader.next();
            switch (reader.getEventType()) {
                case XMLStreamReader.START_ELEMENT: {
                    String name = reader.getLocalName();
                    final Element element;
                    if ("autolevel".equals(name)) {
                        element = new AutolevelContextElement(reader, contextStack, compendium);
                    } else if ("feature".equals(name)) {
                        element = new FeatureContextElement(reader, contextStack, compendium);
                    } else if ("counter".equals(name)) {
                        element = new CounterContextElement(reader, contextStack, compendium);
                    } else if (TOP_LEVEL_ELEMENTS.contains(name) || TRAIT_CONTEXT.contains(name)) {
                        element = new ContextElement(reader, contextStack, compendium);
                    } else {
                        element = new Element(reader, contextStack);
                    }
                    elementStack.push(element);
                    break;
                }
                case XMLStreamReader.CHARACTERS: {
                    Element element = elementStack.peek();
                    assert element != null;
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
        return compendium;
    }

    class Element {
        Fc5ParsingContext context;
        String name;
        List<String> content;
        Map<String, String> attributes;

        Element(XMLStreamReader reader, Deque<Fc5ParsingContext> contextStack) {
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

        public void endElement(XMLStreamReader reader, Deque<Fc5ParsingContext> contextStack) {
            Fc5ParsingContext context = contextStack.peek();
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
        final Fc5ParsingContext myElements;
        final Fc5Compendium compendium;

        ContextElement(XMLStreamReader reader, Deque<Fc5ParsingContext> contextStack, Fc5Compendium compendiumType) {
            super(reader, contextStack);
            this.compendium = compendiumType;
            this.myElements = createContext();
            contextStack.push(myElements);
            if (TOP_LEVEL_ELEMENTS.contains(name)) {
                Log.debugf("--- beg %s", name);
            }
        }

        protected Fc5ParsingContext createContext() {
            return new Fc5ParsingContext(name);
        }

        @Override
        public void characters(XMLStreamReader reader) {
        }

        @Override
        public void endElement(XMLStreamReader reader, Deque<Fc5ParsingContext> contextStack) {
            contextStack.pop(); // pop this nested context
            Fc5ParsingContext parentContext = contextStack.peek();
            if (TOP_LEVEL_ELEMENTS.contains(name)) {
                createCompendiumEntry(compendium, this);
                Log.debugf("--- end %s: %s", name, myElements.elements.get("name"));
            } else if (parentContext != null) {
                parentContext.put(name, new Fc5Trait.TraitTypeBuilder(myElements).build());
            }
        }
    }

    class FeatureContextElement extends ContextElement {
        FeatureContextElement(XMLStreamReader reader, Deque<Fc5ParsingContext> contextStack,
                Fc5Compendium compendiumType) {
            super(reader, contextStack, compendiumType);
            myElements.put("optional", parseBoolean(attributes.get("optional")));
        }

        @Override
        protected Fc5ParsingContext createContext() {
            return new Fc5ParsingContext.FeatureParsingContext(name);
        }

        @Override
        public void endElement(XMLStreamReader reader, Deque<Fc5ParsingContext> contextStack) {
            contextStack.pop(); // pop this nested context
            Fc5ParsingContext parentContext = contextStack.peek();

            myElements.put("level", parentContext.elements.get("level"));
            parentContext.put(name, new Fc5ClassFeature.FeatureBuilder(myElements).build());
        }
    }

    class AutolevelContextElement extends ContextElement {
        AutolevelContextElement(XMLStreamReader reader, Deque<Fc5ParsingContext> contextStack,
                Fc5Compendium compendiumType) {
            super(reader, contextStack, compendiumType);
            myElements.put("level", attributes.get("level"));
            myElements.put("scoreImprovement", parseBoolean(attributes.get("scoreImprovement")));
        }

        @Override
        public void endElement(XMLStreamReader reader, Deque<Fc5ParsingContext> contextStack) {
            contextStack.pop(); // pop this nested context
            Fc5ParsingContext parentContext = contextStack.peek();
            parentContext.put(name, new Fc5Autolevel.Fc5Builder(myElements).build());
        }
    }

    class CounterContextElement extends ContextElement {
        CounterContextElement(XMLStreamReader reader, Deque<Fc5ParsingContext> contextStack,
                Fc5Compendium compendiumType) {
            super(reader, contextStack, compendiumType);
        }

        @Override
        public void endElement(XMLStreamReader reader, Deque<Fc5ParsingContext> contextStack) {
            contextStack.pop(); // pop this nested context
            Fc5ParsingContext parentContext = contextStack.peek();
            parentContext.put(name, Fc5Autolevel.buildCounter(myElements));
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
                return new Fc5Roll(content);
            case "dmgType":
                return Fc5DamageEnum.fromEncodedValue(content);
            case "modifier":
                ModifierCategoryEnum category = ModifierCategoryEnum.fromValue(element.attributes.get("category"));
                return new Modifier(content, category);
            case "proficiency":
                return new Proficiency.Builder().fromString(content).build();
            case "school":
                return SchoolEnum.fromEncodedValue(content);
            case "slots":
                boolean optional = parseBoolean(element.attributes.get("optional"));
                return new QuteClassAutoLevel.SpellSlots(content, optional);
            case "skill":
                if (content.contains(",")) {
                    return Arrays.asList(content.split("\\s*,\\s*"));
                }
                return content;
            case "size":
                return SizeEnum.fromValue(content);
            case "spellAbility":
                return SkillOrAbility.fromTextValue(content);
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

    void createCompendiumEntry(Fc5Compendium compendium, ContextElement element) {
        Fc5ParsingContext context = element.myElements;
        String name = context.getOrFail(context.owner, "name", String.class);
        switch (element.name) {
            case "background":
                QuteBackground background = new QuteBackground.Builder()
                        .setName(name)
                        .setProficiency(context.getOrDefault("proficiency", Proficiency.NONE))
                        .setTraits(context.getOrDefault("trait", Collections.emptyList()))
                        .build();
                compendium.getBackgrounds().add(background);
                break;
            case "class":
                compendium.getClasses().add(new Fc5Class.ClassBuilder(context).build());
                break;
            case "feat":
                compendium.getFeats().add(new Fc5Feat.Fc5FeatBuilder(context).build());
                break;
            case "item":
                compendium.getItems().add(new Fc5Item.ItemBuilder(context).build());
                break;
            case "monster":
                compendium.getMonsters().add(new Fc5Monster.MonsterTypeBuilder(context).build());
                break;
            case "race":
                QuteRace race = new QuteRace.Builder()
                        .setName(context.getOrFail(context.owner, "name", String.class))
                        .setSize(context.getOrDefault("size", SizeEnum.UNKNOWN))
                        .setSpeed(context.getOrDefault("speed", 0))
                        .setAbility(context.getOrDefault("ability", ""))
                        .setSpellAbility(context.getOrDefault("spellAbility", SkillOrAbility.None))
                        .setProficiency(context.getOrDefault("proficiency", Proficiency.NONE))
                        .setTraits(context.getOrDefault("trait", Collections.emptyList()))
                        .setModifiers(context.getOrDefault("modifier", Collections.emptyList()))
                        .build();
                compendium.getRaces().add(race);
                break;
            case "spell":
                compendium.getSpells().add(new Fc5Spell.SpellTypeBuilder(context).build());
                break;
        }
    }
}
