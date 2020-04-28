package org.molguin.flowers;

public class Flower implements Comparable<Flower> {
    public final FlowerConstants.Species species;
    public final FlowerConstants.Color color;
    public final FlowerConstants.Origin origin;
    public final HumanReadableProps props;
    final FlowerGenotype genotype;

    Flower(FlowerConstants.Species species, FlowerConstants.Color color,
           FlowerConstants.Origin origin, FlowerGenotype genotype) {
        this.species = species;
        this.color = color;
        this.origin = origin;
        this.genotype = genotype;

        this.props = new HumanReadableProps(species, color, origin, genotype);
    }

    @Override
    public int compareTo(Flower other) {
        return Integer.compare(this.hashCode(), other.hashCode());
    }

    @Override
    public int hashCode() {
        return (this.species.ordinal() * 10000) + this.getEncodedGenotype();
    }

    @Override
    public String toString() {
        return String.format("{%s | %s | %s | %s}",
                this.species.name(), this.color.name(),
                this.origin.name(), this.getEncodedGenotype());
    }

    public int getEncodedGenotype() {
        return this.genotype.encoded;
    }

    public static class HumanReadableProps {
        public final String species;
        public final String color;
        public final String origin;
        public final String genotype;
        public final String icon_name;

        private HumanReadableProps(FlowerConstants.Species species,
                                   FlowerConstants.Color color,
                                   FlowerConstants.Origin origin,
                                   FlowerGenotype genotype) {
            this.color = color.name().toLowerCase();
            this.species = species.name().toLowerCase();
            this.icon_name = String.format("%s_%s", this.species, this.color);
            this.origin = origin.name().toLowerCase();
            this.genotype = genotype.human_readable;
        }
    }
}
