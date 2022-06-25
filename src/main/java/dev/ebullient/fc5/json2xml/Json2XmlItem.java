package dev.ebullient.fc5.json2xml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.json2xml.jaxb.XmlItemEnum;
import dev.ebullient.fc5.json2xml.jaxb.XmlItemType;
import dev.ebullient.fc5.json2xml.jaxb.XmlObjectFactory;
import dev.ebullient.fc5.json5e.CompendiumSources;
import dev.ebullient.fc5.json5e.JsonIndex;
import dev.ebullient.fc5.json5e.JsonIndex.IndexType;
import dev.ebullient.fc5.json5e.JsonItem;
import dev.ebullient.fc5.pojo.ItemEnum;
import dev.ebullient.fc5.pojo.PropertyEnum;

public class Json2XmlItem extends Json2XmlBase implements JsonItem {
    XmlItemType fc5Item;
    List<JAXBElement<?>> attributes;

    String itemName;
    ItemEnum itemType;
    List<PropertyEnum> propertyEnums = new ArrayList<>();

    public Json2XmlItem(CompendiumSources sources, JsonIndex index, XmlObjectFactory factory) {
        super(sources, index, factory);
    }

    public XmlItemType getXmlCompendiumObject() {
        return fc5Item;
    }

    @Override
    public List<Json2XmlBase> convert(JsonNode jsonSource) {
        if (index.keyIsExcluded(sources.getKey())) {
            Log.debugf("Excluded %s", sources);
            return List.of(); // do not include
        }

        jsonSource = index.handleCopy(IndexType.item, jsonSource);
        if (isReprinted(jsonSource)) {
            return List.of();
        }

        this.fc5Item = factory.createItemType();
        this.attributes = fc5Item.getNameOrTypeOrMagic();
        this.itemName = getItemName(jsonSource);
        this.itemType = getType(jsonSource);

        attributes.add(factory.createItemTypeName(itemName));
        addItemTypeAttribute(jsonSource);
        addItemMagicAttribute(jsonSource);

        collectXmlModifierTypes(jsonSource).forEach(m -> attributes.add(factory.createItemTypeModifier(m)));
        addItemBonusModifierAttributes(jsonSource);

        addItemStealthAttribute(jsonSource);
        addItemTextAndRolls(jsonSource);
        addItemDetail(jsonSource);

        return List.of(this);
    }

    private void addItemDetail(JsonNode jsonSource) {
        List<PropertyEnum> propertyEnums = new ArrayList<>();
        List<String> properties = findProperties(jsonSource, propertyEnums);
        if (properties.size() > 0) {
            attributes.add(factory.createItemTypeProperty(String.join(",", properties)));
        }

        attributes.add(factory.createItemTypeDetail(itemDetail(jsonSource, propertyEnums)));
    }

    private void addItemTextAndRolls(JsonNode jsonElement) {
        Set<String> diceRolls = new HashSet<>();

        List<String> text = itemTextAndRolls(jsonElement, diceRolls);
        text.forEach(t -> attributes.add(factory.createItemTypeText(t)));
        addItemDiceRolls(diceRolls);
    }

    private void addItemMagicAttribute(JsonNode itemNode) {
        JsonNode value = itemNode.get("rarity");
        if (value != null) {
            String rarity = value.asText();
            attributes.add(factory.createItemTypeMagic("none".equals(rarity) ? "NO" : "YES"));
        }
    }

    private void addItemBonusModifierAttributes(JsonNode jsonElement) {
        Set<String> diceRolls = new HashSet<>();
        itemBonusModifers(jsonElement, diceRolls)
                .forEach(m -> attributes.add(factory.createItemTypeModifier(quteToXmlModifierType(m))));
        addItemDiceRolls(diceRolls);
    }

    private void addItemTypeAttribute(JsonNode jsonSource) {
        if (jsonSource.has("value")) {
            attributes.add(factory.createItemTypeValue(jsonSource.get("value").asText()));
        }
        if (jsonSource.has("weight")) {
            attributes.add(factory.createItemTypeWeight(jsonSource.get("weight").asText()));
        }
        if (jsonSource.has("strength")) {
            attributes.add(factory.createItemTypeStrength(jsonSource.get("strength").asText()));
        }
        if (jsonSource.has("dmg1")) {
            attributes.add(factory.createItemTypeDmg1(jsonSource.get("dmg1").asText()));
        }
        if (jsonSource.has("dmg2")) {
            attributes.add(factory.createItemTypeDmg2(jsonSource.get("dmg2").asText()));
        }
        if (jsonSource.has("dmgType")) {
            attributes.add(factory.createItemTypeDmgType(jsonSource.get("dmgType").asText()));
        }
        if (jsonSource.has("range")) {
            attributes.add(factory.createItemTypeRange(jsonSource.get("range").asText()));
        }
        if (jsonSource.has("ac")) {
            attributes.add(factory.createItemTypeAc(jsonSource.get("ac").bigIntegerValue()));
        }

        XmlItemEnum xv = XmlItemEnum.mapValue(itemType);
        attributes.add(factory.createItemTypeType(xv));
    }

    private void addItemStealthAttribute(JsonNode jsonElement) {
        JsonNode stealth = jsonElement.get("stealth");
        if (stealth != null && stealth.asBoolean()) {
            attributes.add(factory.createItemTypeStealth("1"));
        }
    }

    private void addItemDiceRolls(Set<String> diceRolls) {
        diceRolls.forEach(r -> {
            if (r.startsWith("d")) {
                r = "1" + r;
            }
            attributes.add(factory.createItemTypeRoll(r));
        });
    }
}
