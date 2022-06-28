package dev.ebullient.fc5.json2xml;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBElement;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.json2xml.jaxb.XmlFeatureType;
import dev.ebullient.fc5.json2xml.jaxb.XmlModifierType;
import dev.ebullient.fc5.json2xml.jaxb.XmlObjectFactory;
import dev.ebullient.fc5.json2xml.jaxb.XmlSizeEnum;
import dev.ebullient.fc5.json2xml.jaxb.XmlTraitType;
import dev.ebullient.fc5.json5e.CompendiumSources;
import dev.ebullient.fc5.json5e.JsonBase;
import dev.ebullient.fc5.json5e.JsonIndex;
import dev.ebullient.fc5.pojo.Modifier;
import dev.ebullient.fc5.pojo.QuteClassFeature;
import dev.ebullient.fc5.pojo.QuteTrait;

public abstract class Json2XmlBase implements JsonBase {

    final CompendiumSources sources;
    final JsonIndex index;
    final XmlObjectFactory factory;

    public Json2XmlBase(CompendiumSources sources, JsonIndex index, XmlObjectFactory factory) {
        this.sources = sources;
        this.index = index;
        this.factory = factory;
    }

    @Override
    public JsonIndex getIndex() {
        return index;
    }

    @Override
    public CompendiumSources getSources() {
        return sources;
    }

    @Override
    public boolean isMarkdown() {
        return false;
    }

    String getName() {
        return this.sources.getName();
    }

    public abstract List<Json2XmlBase> convert(JsonNode value);

    public abstract Object getXmlCompendiumObject();

    XmlSizeEnum getSizeEnum(JsonNode value) {
        return XmlSizeEnum.fromValue(getSize(value));
    }

    public List<XmlTraitType> collectXmlTraits(JsonNode array) {
        List<QuteTrait> traits = collectTraits(array);
        return traits.stream().map(this::quteToXmlTraitType).collect(Collectors.toList());
    }

    public List<XmlTraitType> collectXmlTraitsFromEntries(String properName, JsonNode value) {
        List<QuteTrait> traits = collectTraitsFromEntries(properName, value);
        return traits.stream().map(this::quteToXmlTraitType).collect(Collectors.toList());
    }

    public XmlTraitType quteToXmlTraitType(QuteTrait qute) {
        XmlTraitType trait = factory.createTraitType();
        String traitName = qute.getName();

        List<JAXBElement<String>> traitAttributes = trait.getNameOrTextOrAttack();
        traitAttributes.add(factory.createTraitTypeName(qute.getName()));

        if (SPECIAL.contains(traitName)) {
            traitAttributes.add(factory.createTraitTypeSpecial(traitName));
        }
        if (qute.getRecharge() != null) {
            traitAttributes.add(factory.createTraitTypeRecharge(qute.getRecharge()));
        }
        if (qute.getProficiency() != null) {
            traitAttributes.add(factory.createTraitTypeProficiency(qute.getProficiency().toText()));
        }
        qute.getRawText().forEach(t -> traitAttributes.add(factory.createTraitTypeText(t)));
        qute.getDiceRolls().forEach(r -> traitAttributes.add(factory.createItemTypeRoll(r)));
        qute.getAttacks().forEach(r -> traitAttributes.add(factory.createTraitTypeAttack(traitName + r.trim())));
        return trait;
    }

    public XmlTraitType createXmlTraitType(String traitName, List<String> text) {
        return quteToXmlTraitType(createTrait(traitName, text, List.of()));
    }

    public XmlTraitType createXmlTraitType(String traitName, List<String> text, Collection<String> diceRolls) {
        return quteToXmlTraitType(createTrait(traitName, text, diceRolls));
    }

    public List<XmlModifierType> collectXmlModifierTypes(JsonNode value) {
        List<Modifier> modifiers = collectAbilityModifiers(value);
        return modifiers.stream()
                .map(m -> new XmlModifierType(m.getValue(), m.getCategory()))
                .collect(Collectors.toList());
    }

    public XmlFeatureType quteToXmlFeatureType(QuteClassFeature qute) {
        XmlFeatureType feature = factory.createFeatureType();
        if (qute.isOptional()) {
            feature.setOptional("YES");
        }

        List<JAXBElement<?>> fAttr = feature.getNameOrTextOrSpecial();
        fAttr.add(factory.createFeatureTypeName(qute.getName()));
        qute.getRawText().forEach(line -> fAttr.add(factory.createFeatureTypeText(line)));

        return feature;
    }

    public XmlModifierType quteToXmlModifierType(Modifier m) {
        XmlModifierType modifier = factory.createModifierType();
        modifier.setCategory(m.getCategory());
        modifier.setValue(m.getValue());
        return modifier;
    }
}
