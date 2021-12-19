package dev.ebullient.fc5.data;

public class AbilityScores {
    int strength;
    int dexterity;
    int constitution;
    int intelligence;
    int wisdom;
    int charisma;

    public AbilityScores(ParsingContext context, String name) {
        strength = context.getOrDefault(name, "str", 10);
        dexterity = context.getOrDefault(name, "dex", 10);
        constitution = context.getOrDefault(name, "con", 10);
        intelligence = context.getOrDefault(name, "int", 10);
        wisdom = context.getOrDefault(name, "wis", 10);
        charisma = context.getOrDefault(name, "cha", 10);
    }

    private String toAbilityModifier(int value) {
        int mod = value - 10;
        if (mod % 2 != 0) {
            mod -= 1; // round down
        }
        int modifier = mod / 2;
        return String.format("%s (%s%s)", value,
                modifier >= 0 ? "+" : "",
                modifier);
    }

    public int[] toArray() {
        int array[] = {
                strength,
                dexterity,
                constitution,
                intelligence,
                wisdom,
                charisma
        };
        return array;
    }

    public int getStr() {
        return strength;
    }

    public int getDex() {
        return dexterity;
    }

    public int getCon() {
        return constitution;
    }

    public int getInte() {
        return intelligence;
    }

    public int getWis() {
        return wisdom;
    }

    public int getCha() {
        return charisma;
    }

    @Override
    public String toString() {
        return toAbilityModifier(strength)
                + "|" + toAbilityModifier(dexterity)
                + "|" + toAbilityModifier(constitution)
                + "|" + toAbilityModifier(intelligence)
                + "|" + toAbilityModifier(wisdom)
                + "|" + toAbilityModifier(charisma);
    }
}
