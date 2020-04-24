package org.molguin.flowers;

public class Flower {
    public final FlowerConstants.Species species;
    public final FlowerConstants.Color color;
    public final FlowerConstants.Origin origin;
    final FlowerGenotype genotype;

    Flower(FlowerConstants.Species species, FlowerConstants.Color color, FlowerConstants.Origin origin, FlowerGenotype genotype) {
        this.species = species;
        this.color = color;
        this.origin = origin;
        this.genotype = genotype;
    }

    public String getEncodedGenotype() {
        return this.genotype.encoded;
    }

    @Override
    public String toString() {
        return String.format("{%s | %s | %s | %s}",
                this.species.name(), this.color.name(),
                this.origin.name(), this.getEncodedGenotype());
    }
}
