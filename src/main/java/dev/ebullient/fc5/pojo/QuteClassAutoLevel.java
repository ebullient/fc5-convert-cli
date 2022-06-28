package dev.ebullient.fc5.pojo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class QuteClassAutoLevel implements QuteSource {
    public static final QuteClassAutoLevel NONE = new QuteClassAutoLevel();

    protected final String name;
    protected final int level;
    protected final boolean scoreImprovement;
    protected final List<? extends QuteClassFeature> features;
    protected final List<Counter> counters;
    protected final SpellSlots slots;

    protected QuteClassAutoLevel() {
        name = "autolevel - none";
        level = 0;
        scoreImprovement = false;
        features = Collections.emptyList();
        counters = Collections.emptyList();
        slots = null;
    }

    public QuteClassAutoLevel(int level, boolean scoreImprovement,
            List<? extends QuteClassFeature> features, List<Counter> counters, SpellSlots slots) {
        this.name = "autolevel-" + level;
        this.level = level;
        this.scoreImprovement = scoreImprovement;
        this.features = features;
        this.counters = counters;
        this.slots = slots;
    }

    @Override
    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public boolean isScoreImprovement() {
        return scoreImprovement;
    }

    public List<Counter> getCounters() {
        return counters;
    }

    public SpellSlots getSlots() {
        return slots;
    }

    public Stream<? extends QuteClassFeature> getFeatures() {
        return features.stream();
    }

    public boolean hasContent() {
        return slots != null || !counters.isEmpty() || !features.isEmpty();
    }

    public static class Builder {
        protected int level;
        protected boolean scoreImprovement;
        protected List<QuteClassFeature> features = new ArrayList<>();
        protected List<Counter> counters = new ArrayList<>();
        protected SpellSlots slots;

        public Builder setLevel(int level) {
            this.level = level;
            return this;
        }

        public Builder setScoreImprovement(boolean scoreImprovement) {
            this.scoreImprovement = scoreImprovement;
            return this;
        }

        public Builder addFeature(QuteClassFeature f) {
            this.features.add(f);
            return this;
        }

        public Builder addFeatures(Stream<? extends QuteClassFeature> f) {
            f.forEach(x -> addFeature(x));
            return this;
        }

        public Builder addCounters(List<Counter> c) {
            this.counters.addAll(c);
            return this;
        }

        public Builder addCounter(Counter c) {
            this.counters.add(c);
            return this;
        }

        public Builder setSlots(SpellSlots slots) {
            this.slots = slots;
            return this;
        }

        public QuteClassAutoLevel build() {
            return new QuteClassAutoLevel(level, scoreImprovement, features, counters, slots);
        }
    }

    public enum Reset {
        L,
        S
    }

    public static class Counter {
        public final Optional<String> name;
        public final Optional<Integer> count;
        public final Optional<Reset> reset;

        public Counter(String name, Integer count, Reset reset) {
            this.name = name == null ? Optional.empty() : Optional.of(name);
            this.count = count == null ? Optional.empty() : Optional.of(count);
            this.reset = reset == null ? Optional.empty() : Optional.of(reset);
        }

        public Optional<String> getName() {
            return name;
        }

        public Optional<Integer> getCount() {
            return count;
        }

        public Optional<Reset> getReset() {
            return reset;
        }
    }

    /**
     * <p>
     * Java class for slotsType complex type.
     *
     * <p>
     * The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType name="slotsType">
     *   &lt;simpleContent>
     *     &lt;extension base="&lt;>integerList">
     *       &lt;attribute name="optional" type="{}boolean" />
     *     &lt;/extension>
     *   &lt;/simpleContent>
     * &lt;/complexType>
     * </pre>
     *
     *
     */
    public static class SpellSlots {
        public static final SpellSlots NONE = new SpellSlots("", true);

        final String value;
        final boolean optional;

        public SpellSlots(String value) {
            this.value = value;
            this.optional = false;
        }

        public SpellSlots(String value, boolean optional) {
            this.value = value;
            this.optional = optional;
        }

        public String getValue() {
            return value;
        }

        public boolean isOptional() {
            return optional;
        }
    }
}
