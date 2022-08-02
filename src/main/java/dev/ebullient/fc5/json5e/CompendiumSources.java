package dev.ebullient.fc5.json5e;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.fc5.json5e.JsonIndex.IndexType;

public class CompendiumSources {
    final IndexType type;
    final String key;
    final String name;
    final Set<String> bookSources = new HashSet<>();
    final String sourceText;

    public CompendiumSources(IndexType type, String key, JsonNode jsonElement) {
        this.type = type;
        this.key = key;
        this.name = jsonElement.get("name").asText();
        this.sourceText = findSourceText(jsonElement);
    }

    public String getSourceText() {
        return sourceText;
    }

    private String findSourceText(JsonNode jsonElement) {
        this.bookSources.add(jsonElement.get("source").asText());

        List<String> srcText = new ArrayList<>();
        srcText.add(sourceAndPage(jsonElement));

        String copyOf = jsonElement.has("_copy")
                ? jsonElement.get("_copy").get("name").asText()
                : null;
        String copySrc = jsonElement.has("_copy")
                ? jsonElement.get("_copy").get("source").asText()
                : null;

        if (copyOf != null) {
            srcText.add(String.format("Derived from %s (%s)", copyOf, copySrc));
        }

        srcText.addAll(StreamSupport.stream(jsonElement.withArray("additionalSources").spliterator(), false)
                .filter(x -> !x.get("source").asText().equals(copySrc))
                .peek(x -> this.bookSources.add(x.get("source").asText()))
                .map(x -> sourceAndPage(x))
                .collect(Collectors.toList()));

        srcText.addAll(StreamSupport.stream(jsonElement.withArray("otherSources").spliterator(), false)
                .filter(x -> !x.get("source").asText().equals(copySrc))
                .peek(x -> this.bookSources.add(x.get("source").asText()))
                .map(x -> sourceAndPage(x))
                .collect(Collectors.toList()));

        return String.join(", ", srcText);
    }

    private String sourceAndPage(JsonNode source) {
        if (source.has("page")) {
            String src = source.get("source").asText();
            String book = abvToName.getOrDefault(src, src);
            return String.format("%s p. %s", book, source.get("page").asText());
        }
        return source.get("source").asText();
    }

    public boolean isPrimarySource(String source) {
        return bookSources.iterator().next().equals(source);
    }

    public boolean isFromUA() {
        return bookSources.stream().anyMatch(x -> x.contains("UA") && !x.equals("UAWGE"));
    }

    public String primarySource() {
        return bookSources.iterator().next();
    }

    public String alternateSource() {
        Iterator<String> i = bookSources.iterator();
        if (bookSources.size() > 1) {
            i.next();
        }
        return i.next();
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public IndexType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "sources[" + key + ']';
    }

    final static String AL_PREFIX = "Adventurers League: ";
    final static String AL_PREFIX_SHORT = "AL: ";
    final static String PS_PREFIX = "Plane Shift: ";
    final static String PS_PREFIX_SHORT = "PS: ";
    final static String UA_PREFIX = "Unearthed Arcana: ";
    final static String UA_PREFIX_SHORT = "UA: ";
    final static String TftYP_NAME = "Tales from the Yawning Portal";
    final static String AitFR_NAME = "Adventures in the Forgotten Realms";
    final static String NRH_NAME = "NERDS Restoring Harmony";
    final static String MCVX_PREFIX = "Monster Compendium Volume ";
    final static Map<String, String> abvToName = new HashMap<>();

    static {
        abvToName.put("CoS", "Curse of Strahd");
        abvToName.put("DMG", "Dungeon Master's Guide");
        abvToName.put("EEPC", "Elemental Evil Player's Companion");
        abvToName.put("EET", "Elemental Evil: Trinkets");
        abvToName.put("HotDQ", "Hoard of the Dragon Queen");
        abvToName.put("LMoP", "Lost Mine of Phandelver");
        abvToName.put("MM", "Monster Manual");
        abvToName.put("OotA", "Out of the Abyss");
        abvToName.put("PHB", "Player's Handbook");
        abvToName.put("PotA", "Princes of the Apocalypse");
        abvToName.put("RoT", "The Rise of Tiamat");
        abvToName.put("RoTOS", "The Rise of Tiamat Online Supplement");
        abvToName.put("SCAG", "Sword Coast Adventurer's Guide");
        abvToName.put("SKT", "Storm King's Thunder");
        abvToName.put("ToA", "Tomb of Annihilation");
        abvToName.put("TLK", "The Lost Kenku");
        abvToName.put("ToD", "Tyranny of Dragons");
        abvToName.put("TTP", "The Tortle Package");
        abvToName.put("TYP", TftYP_NAME);
        abvToName.put("TYP_AtG", TftYP_NAME + ": Against the Giants");
        abvToName.put("TYP_DiT", TftYP_NAME + ": Dead in Thay");
        abvToName.put("TYP_TFoF", TftYP_NAME + ": The Forge of Fury");
        abvToName.put("TYP_THSoT", TftYP_NAME + ": The Hidden Shrine of Tamoachan");
        abvToName.put("TYP_TSC", TftYP_NAME + ": The Sunless Citadel");
        abvToName.put("TYP_ToH", TftYP_NAME + ": Tomb of Horrors");
        abvToName.put("TYP_WPM", TftYP_NAME + ": White Plume Mountain");
        abvToName.put("VGM", "Volo's Guide to Monsters");
        abvToName.put("XGE", "Xanathar's Guide to Everything");
        abvToName.put("OGA", "One Grung Above");
        abvToName.put("MTF", "Mordenkainen's Tome of Foes");
        abvToName.put("WDH", "Waterdeep: Dragon Heist");
        abvToName.put("WDMM", "Waterdeep: Dungeon of the Mad Mage");
        abvToName.put("GGR", "Guildmasters' Guide to Ravnica");
        abvToName.put("KKW", "Krenko's Way");
        abvToName.put("LLK", "Lost Laboratory of Kwalish");
        abvToName.put("AZfyT", "A Zib for your Thoughts");
        abvToName.put("GoS", "Ghosts of Saltmarsh");
        abvToName.put("AI", "Acquisitions Incorporated");
        abvToName.put("OoW", "The Orrery of the Wanderer");
        abvToName.put("ESK", "Essentials Kit");
        abvToName.put("DIP", "Dragon of Icespire Peak");
        abvToName.put("HftT", "Hunt for the Thessalhydra");
        abvToName.put("DC", "Divine Contention");
        abvToName.put("SLW", "Storm Lord's Wrath");
        abvToName.put("SDW", "Sleeping Dragon's Wake");
        abvToName.put("BGDIA", "Baldur's Gate: Descent Into Avernus");
        abvToName.put("LR", "Locathah Rising");
        abvToName.put("AL", "Adventurers' League");
        abvToName.put("SAC", "Sage Advice Compendium");
        abvToName.put("ERLW", "Eberron: Rising from the Last War");
        abvToName.put("EFR", "Eberron: Forgotten Relics");
        abvToName.put("RMBRE", "The Lost Dungeon of Rickedness: Big Rick Energy");
        abvToName.put("RMR", "Dungeons & Dragons vs. Rick and Morty: Basic Rules");
        abvToName.put("MFF", "Mordenkainen's Fiendish Folio");
        abvToName.put("AWM", "Adventure with Muk");
        abvToName.put("IMR", "Infernal Machine Rebuild");
        abvToName.put("SADS", "Sapphire Anniversary Dice Set");
        abvToName.put("EGW", "Explorer's Guide to Wildemount");
        abvToName.put("EGW_ToR", "Tide of Retribution");
        abvToName.put("EGW_DD", "Dangerous Designs");
        abvToName.put("EGW_FS", "Frozen Sick");
        abvToName.put("EGW_US", "Unwelcome Spirits");
        abvToName.put("MOT", "Mythic Odysseys of Theros");
        abvToName.put("IDRotF", "Icewind Dale: Rime of the Frostmaiden");
        abvToName.put("TCE", "Tasha's Cauldron of Everything");
        abvToName.put("VRGR", "Van Richten's Guide to Ravenloft");
        abvToName.put("HoL", "The House of Lament");
        abvToName.put("RtG", "Return to Glory");
        abvToName.put("AitFR", AitFR_NAME);
        abvToName.put("AitFR_ISF", AitFR_NAME + ": In Scarlet Flames");
        abvToName.put("AitFR_THP", AitFR_NAME + ": The Hidden Page");
        abvToName.put("AitFR_AVT", AitFR_NAME + ": A Verdant Tomb");
        abvToName.put("AitFR_DN", AitFR_NAME + ": Deepest Night");
        abvToName.put("AitFR_FCD", AitFR_NAME + ": From Cyan Depths");
        abvToName.put("WBtW", "The Wild Beyond the Witchlight");
        abvToName.put("DoD", "Domains of Delight");
        abvToName.put("MaBJoV", "Minsc and Boo's Journal of Villainy");
        abvToName.put("FTD", "Fizban's Treasury of Dragons");
        abvToName.put("SCC", "Strixhaven: A Curriculum of Chaos");
        abvToName.put("SCC_CK", "Campus Kerfuffle");
        abvToName.put("SCC_HfMT", "Hunt for Mage Tower");
        abvToName.put("SCC_TMM", "The Magister's Masquerade");
        abvToName.put("SCC_ARiR", "A Reckoning in Ruins");
        abvToName.put("MPMM", "Mordenkainen Presents: Monsters of the Multiverse");
        abvToName.put("CRCotN", "Critical Role: Call of the Netherdeep");
        abvToName.put("SCREEN", "Dungeon Master's Screen");
        abvToName.put("SCREEN_WILDERNESS_KIT", "Dungeon Master's Screen: Wilderness Kit");
        abvToName.put("SCREEN_DUNGEON_KIT", "Dungeon Master's Screen: Dungeon Kit");
        abvToName.put("HEROES_FEAST", "Heroes' Feast");
        abvToName.put("CM", "Candlekeep Mysteries");
        abvToName.put("NRH", NRH_NAME);
        abvToName.put("NRH_TCMC", NRH_NAME + ": The Candy Mountain Caper");
        abvToName.put("NRH_AVitW", NRH_NAME + ": A Voice in the Wilderness");
        abvToName.put("NRH_ASS", NRH_NAME + ": A Sticky Situation");
        abvToName.put("NRH_CoI", NRH_NAME + ": Circus of Illusions");
        abvToName.put("NRH_TLT", NRH_NAME + ": The Lost Tomb");
        abvToName.put("NRH_AWoL", NRH_NAME + ": A Web of Lies");
        abvToName.put("NRH_AT", NRH_NAME + ": Adventure Together");
        abvToName.put("MGELFT", "Muk's Guide To Everything He Learned From Tasha");
        abvToName.put("VD", "Vecna Dossier");
        abvToName.put("ALCoS", AL_PREFIX + "Curse of Strahd");
        abvToName.put("ALEE", AL_PREFIX + "Elemental Evil");
        abvToName.put("ALRoD", AL_PREFIX + "Rage of Demons");
        abvToName.put("PSA", PS_PREFIX + "Amonkhet");
        abvToName.put("PSI", PS_PREFIX + "Innistrad");
        abvToName.put("PSK", PS_PREFIX + "Kaladesh");
        abvToName.put("PSZ", PS_PREFIX + "Zendikar");
        abvToName.put("PSX", PS_PREFIX + "Ixalan");
        abvToName.put("PSD", PS_PREFIX + "Dominaria");
        abvToName.put("XMtS", "X Marks the Spot");
        abvToName.put("UAA", UA_PREFIX + "Artificer");
        abvToName.put("UAEAG", UA_PREFIX + "Eladrin and Gith");
        abvToName.put("UAEBB", UA_PREFIX + "Eberron");
        abvToName.put("UAFFR", UA_PREFIX + "Feats for Races");
        abvToName.put("UAFFS", UA_PREFIX + "Feats for Skills");
        abvToName.put("UAFO", UA_PREFIX + "Fiendish Options");
        abvToName.put("UAFT", UA_PREFIX + "Feats");
        abvToName.put("UAGH", UA_PREFIX + "Gothic Heroes");
        abvToName.put("UAMDM", UA_PREFIX + "Modern Magic");
        abvToName.put("UASSP", UA_PREFIX + "Starter Spells");
        abvToName.put("UATMC", UA_PREFIX + "The Mystic Class");
        abvToName.put("UATOBM", UA_PREFIX + "That Old Black Magic");
        abvToName.put("UATRR", UA_PREFIX + "The Ranger, Revised");
        abvToName.put("UAWA", UA_PREFIX + "Waterborne Adventures");
        abvToName.put("UAVR", UA_PREFIX + "Variant Rules");
        abvToName.put("UALDR", UA_PREFIX + "Light, Dark, Underdark!");
        abvToName.put("UARAR", UA_PREFIX + "Ranger and Rogue");
        abvToName.put("UAATOSC", UA_PREFIX + "A Trio of Subclasses");
        abvToName.put("UABPP", UA_PREFIX + "Barbarian Primal Paths");
        abvToName.put("UARSC", UA_PREFIX + "Revised Subclasses");
        abvToName.put("UAKOO", UA_PREFIX + "Kits of Old");
        abvToName.put("UABBC", UA_PREFIX + "Bard: Bard Colleges");
        abvToName.put("UACDD", UA_PREFIX + "Cleric: Divine Domains");
        abvToName.put("UAD", UA_PREFIX + "Druid");
        abvToName.put("UARCO", UA_PREFIX + "Revised Class Options");
        abvToName.put("UAF", UA_PREFIX + "Fighter");
        abvToName.put("UAM", UA_PREFIX + "Monk");
        abvToName.put("UAP", UA_PREFIX + "Paladin");
        abvToName.put("UAMC", UA_PREFIX + "Modifying Classes");
        abvToName.put("UAS", UA_PREFIX + "Sorcerer");
        abvToName.put("UAWAW", UA_PREFIX + "Warlock and Wizard");
        abvToName.put("UATF", UA_PREFIX + "The Faithful");
        abvToName.put("UAWR", UA_PREFIX + "Wizard Revisited");
        abvToName.put("UAESR", UA_PREFIX + "Elf Subraces");
        abvToName.put("UAMAC", UA_PREFIX + "Mass Combat");
        abvToName.put("UA3PE", UA_PREFIX + "Three-Pillar Experience");
        abvToName.put("UAGHI", UA_PREFIX + "Greyhawk Initiative");
        abvToName.put("UATSC", UA_PREFIX + "Three Subclasses");
        abvToName.put("UAOD", UA_PREFIX + "Order Domain");
        abvToName.put("UACAM", UA_PREFIX + "Centaurs and Minotaurs");
        abvToName.put("UAGSS", UA_PREFIX + "Giant Soul Sorcerer");
        abvToName.put("UARoE", UA_PREFIX + "Races of Eberron");
        abvToName.put("UARoR", UA_PREFIX + "Races of Ravnica");
        abvToName.put("UAWGE", "Wayfinder's Guide to Eberron");
        abvToName.put("UAOSS", UA_PREFIX + "Of Ships and the Sea");
        abvToName.put("UASIK", UA_PREFIX + "Sidekicks");
        abvToName.put("UAAR", UA_PREFIX + "Artificer Revisited");
        abvToName.put("UABAM", UA_PREFIX + "Barbarian and Monk");
        abvToName.put("UASAW", UA_PREFIX + "Sorcerer and Warlock");
        abvToName.put("UABAP", UA_PREFIX + "Bard and Paladin");
        abvToName.put("UACDW", UA_PREFIX + "Cleric, Druid, and Wizard");
        abvToName.put("UAFRR", UA_PREFIX + "Fighter, Ranger, and Rogue");
        abvToName.put("UACFV", UA_PREFIX + "Class Feature Variants");
        abvToName.put("UAFRW", UA_PREFIX + "Fighter, Rogue, and Wizard");
        abvToName.put("UAPCRM", UA_PREFIX + "Prestige Classes and Rune Magic");
        abvToName.put("UAR", UA_PREFIX + "Ranger");
        abvToName.put("UA2020SC1", UA_PREFIX + "2020 Subclasses, Part 1");
        abvToName.put("UA2020SC2", UA_PREFIX + "2020 Subclasses, Part 2");
        abvToName.put("UA2020SC3", UA_PREFIX + "2020 Subclasses, Part 3");
        abvToName.put("UA2020SC4", UA_PREFIX + "2020 Subclasses, Part 4");
        abvToName.put("UA2020SC5", UA_PREFIX + "2020 Subclasses, Part 5");
        abvToName.put("UA2020SMT", UA_PREFIX + "2020 Spells and Magic Tattoos");
        abvToName.put("UA2020POR", UA_PREFIX + "2020 Psionic Options Revisited");
        abvToName.put("UA2020SCR", UA_PREFIX + "2020 Subclasses Revisited");
        abvToName.put("UA2020F", UA_PREFIX + "2020 Feats");
        abvToName.put("UA2021GL", UA_PREFIX + "2021 Gothic Lineages");
        abvToName.put("UA2021FF", UA_PREFIX + "2021 Folk of the Feywild");
        abvToName.put("UA2021DO", UA_PREFIX + "2021 Draconic Options");
        abvToName.put("UA2021MoS", UA_PREFIX + "2021 Mages of Strixhaven");
        abvToName.put("UA2021TotM", UA_PREFIX + "2021 Travelers of the Multiverse");
        abvToName.put("UA2022HoK", UA_PREFIX + "2022 Heroes of Krynn");
        abvToName.put("UA2022HoKR", UA_PREFIX + "2022 Heroes of Krynn Revisited");
        abvToName.put("UA2022GO", UA_PREFIX + "2022 Giant Options");
        abvToName.put("MCV1SC", MCVX_PREFIX + "1: Spelljammer Creatures");
    }
}
