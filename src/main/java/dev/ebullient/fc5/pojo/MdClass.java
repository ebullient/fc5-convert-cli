package dev.ebullient.fc5.pojo;

import static dev.ebullient.fc5.pojo.Section.leveledAlphabeticalSort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.quarkus.qute.TemplateData;

@TemplateData
public class MdClass implements BaseType {
    public static final String NONE = "—";

    protected final String name;
    protected final int hitDice;
    protected final MdProficiency proficiency;
    protected final int numSkills;
    protected final String armor;
    protected final String weapons;
    protected final String tools;

    protected final List<MdFeature> features;

    public MdClass(String name, int hitDice, MdProficiency proficiency, int numSkills,
            String armor, String weapons, String tools, List<MdFeature> features) {
        this.name = name;
        this.hitDice = hitDice;
        this.proficiency = proficiency;
        this.numSkills = numSkills;
        this.armor = armor;
        this.weapons = weapons;
        this.tools = tools;
        this.features = features;
    }

    @Override
    public String getName() {
        return name;
    }

    public List<String> getTags() {
        return Collections.singletonList("class/" + MarkdownWriter.slugifier().slugify(name));
    }

    public int getHitDice() {
        return hitDice;
    }

    public int getHitRollAverage() {
        return hitDice / 2 + 1;
    }

    public String getSavingThrows() {
        return proficiency.getSavingThrows();
    }

    public int getNumSkills() {
        return numSkills;
    }

    public String getSkills() {
        return proficiency.getSkillNames();
    }

    public String getProficiency() {
        return proficiency.toText();
    }

    public String getArmor() {
        return armor;
    }

    public String getWeapons() {
        return weapons;
    }

    public String getTools() {
        return tools;
    }

    public Collection<? extends MdFeature> getLevelFeatures() {
        return features;
    }

    public List<Section> getSections() {
        // Collect all of the autolevel features
        List<Section> allSections = getLevelFeatures().stream()
                .map(Section::new)
                .sorted(leveledAlphabeticalSort)
                .collect(Collectors.toList());

        // Find named groups (to derive sections later), e.g.
        // Primal Path -> Primal Path: Path of the Berserker -> Path of the Berserker: Frenzy
        List<Section> groups = new ArrayList<>();

        Section classFeatures = new Section("##", "Class Features");
        groups.add(classFeatures);

        List<Section> remaining = new ArrayList<>(allSections);
        for (Section index : allSections) {
            if (!index.isGrouped()) {
                if (index.findFeatureGroups(remaining, "##")) {
                    groups.add(index);
                }
            }
        }

        // Add unclaimed features
        classFeatures.addAll(remaining);

        return groups.stream()
                .flatMap(x -> x.flatten())
                .collect(Collectors.toList());
    }

    public static class Builder {
        protected String name;
        protected int hitDice;
        protected MdProficiency proficiency;
        protected int numSkills;
        protected String armor;
        protected String weapons;
        protected String tools;
        protected List<MdFeature> features = new ArrayList<>();

        public void setName(String name) {
            this.name = name;
        }

        public void setHitDice(int hitDice) {
            this.hitDice = hitDice;
        }

        public void setProficiency(MdProficiency proficiency) {
            this.proficiency = proficiency;
        }

        public void setNumSkills(int numSkills) {
            this.numSkills = numSkills;
        }

        public void setArmor(String armor) {
            this.armor = armor;
        }

        public void setWeapons(String weapons) {
            this.weapons = weapons;
        }

        public void setTools(String tools) {
            this.tools = tools;
        }

        public void addFeature(String name, int level, List<String> text) {
            this.features.add(new MdFeature(name, level, text));
        }

        public MdClass build() {
            return new MdClass(name, hitDice, proficiency, numSkills,
                    armor, weapons, tools, features);
        }
    }
}
