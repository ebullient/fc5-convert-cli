package dev.ebullient.fc5.pojo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.quarkus.qute.TemplateData;

@TemplateData
public class QuteTrait implements QuteSource {
    protected final String name;
    protected final List<String> text;
    protected final List<String> diceRolls;
    protected final List<String> attacks;

    protected final String recharge;
    protected final Proficiency proficiency;

    protected QuteTrait(String name, List<String> text, List<String> diceRolls, List<String> attacks,
            String recharge, Proficiency proficiency) {
        this.name = name;
        this.text = breathe(text);
        this.diceRolls = diceRolls;
        this.attacks = attacks;
        this.recharge = recharge;
        this.proficiency = proficiency;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getText() {
        return String.join("\n", text).trim();
    }

    public List<String> getRawText() {
        return text;
    }

    public List<String> getAttacks() {
        return attacks;
    }

    public List<String> getDiceRolls() {
        return diceRolls;
    }

    public String getRecharge() {
        return recharge;
    }

    public Proficiency getProficiency() {
        return proficiency;
    }

    public static class Builder {
        protected String name;
        protected List<String> text = new ArrayList<>();
        protected List<String> diceRolls = new ArrayList<>();
        protected List<String> attacks = new ArrayList<>();

        protected String recharge;
        protected Proficiency proficiency;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder addText(String t) {
            this.text.add(t);
            return this;
        }

        public Builder addText(Collection<String> t) {
            this.text.addAll(t);
            return this;
        }

        public Builder addDiceRoll(String d) {
            if (d.contains("|")) { // attack
                this.attacks.add(d);
            } else {
                if (d.startsWith("d")) {
                    d = "1" + d;
                }
                this.diceRolls.add(d);
            }
            return this;
        }

        public Builder addDiceRolls(Collection<String> d) {
            d.forEach(this::addDiceRoll);
            return this;
        }

        public Builder setRecharge(String recharge) {
            this.recharge = recharge;
            return this;
        }

        public Builder setProficiency(Proficiency proficiency) {
            this.proficiency = proficiency;
            return this;
        }

        public QuteTrait build() {
            return new QuteTrait(name, text, diceRolls, attacks, recharge, proficiency);
        }
    }
}
