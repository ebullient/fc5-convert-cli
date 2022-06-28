package dev.ebullient.fc5.pojo;

import static dev.ebullient.fc5.pojo.QuteSection.leveledAlphabeticalSort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.quarkus.qute.TemplateData;

@TemplateData
public class QuteClass implements QuteSource {
    public static final String NONE = "—";

    protected final String name;
    protected final int hitDice;
    protected final Proficiency proficiency;
    protected final int numSkills;
    protected final String armor;
    protected final String weapons;
    protected final String tools;

    protected final List<QuteClassAutoLevel> levels;

    public QuteClass(String name, int hitDice, Proficiency proficiency, int numSkills,
            String armor, String weapons, String tools, List<QuteClassAutoLevel> levels) {
        this.name = name;
        this.hitDice = hitDice;
        this.proficiency = proficiency;
        this.numSkills = numSkills;
        this.armor = armor;
        this.weapons = weapons;
        this.tools = tools;
        this.levels = levels;
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

    public Stream<? extends QuteClassFeature> getLevelFeatures() {
        return levels.stream().flatMap(QuteClassAutoLevel::getFeatures);
    }

    public List<QuteSection> getSections() {
        // Collect all of the autolevel features
        List<QuteSection> allSections = getLevelFeatures()
                .map(QuteSection::new)
                .sorted(leveledAlphabeticalSort)
                .collect(Collectors.toList());

        // Find named groups (to derive sections later), e.g.
        // Primal Path -> Primal Path: Path of the Berserker -> Path of the Berserker: Frenzy
        List<QuteSection> groups = new ArrayList<>();

        QuteSection classFeatures = new QuteSection("##", "Class Features");
        groups.add(classFeatures);

        List<QuteSection> remaining = new ArrayList<>(allSections);
        for (QuteSection index : allSections) {
            if (!index.isGrouped()) {
                if (index.findFeatureGroups(remaining, "##")) {
                    groups.add(index);
                }
            }
        }

        // Add unclaimed features
        classFeatures.addAll(remaining);

        return groups.stream()
                .flatMap(QuteSection::flatten)
                .collect(Collectors.toList());
    }

    public static class Builder {
        protected String name;
        protected int hitDice;
        protected Proficiency proficiency;
        protected int numSkills;
        protected String armor;
        protected String weapons;
        protected String tools;
        protected List<QuteClassAutoLevel> levels = new ArrayList<>();

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setHitDice(int hitDice) {
            this.hitDice = hitDice;
            return this;
        }

        public Builder setProficiency(Proficiency proficiency) {
            this.proficiency = proficiency;
            return this;
        }

        public Builder setNumSkills(int numSkills) {
            this.numSkills = numSkills;
            return this;
        }

        public Builder setArmor(String armor) {
            this.armor = armor;
            return this;
        }

        public Builder setWeapons(String weapons) {
            this.weapons = weapons;
            return this;
        }

        public Builder setTools(String tools) {
            this.tools = tools;
            return this;
        }

        public Builder addAutoLevel(QuteClassAutoLevel autoLevel) {
            levels.add(autoLevel);
            return this;
        }

        public QuteClass build() {
            return new QuteClass(name, hitDice, proficiency, numSkills,
                    armor, weapons, tools, levels);
        }
    }
}
