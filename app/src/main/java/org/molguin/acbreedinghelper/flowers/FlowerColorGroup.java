package org.molguin.acbreedinghelper.flowers;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class FlowerColorGroup extends AbstractFuzzyFlower {
    public final Map<SpecificFlower, Double> variants;

    public FlowerColorGroup(FlowerConstants.Species species,
                            FlowerConstants.Color color,
                            Collection<SpecificFlower> variants) {
        this(species, color, new TreeMap<SpecificFlower, Double>());
        for (SpecificFlower v : variants)
            this.variants.put(v, 1.0 / variants.size());

    }

    public FlowerColorGroup(FlowerConstants.Species species,
                            FlowerConstants.Color color,
                            Map<SpecificFlower, Double> variantProbs) {
        super(species, color);
        this.variants = new TreeMap<SpecificFlower, Double>(variantProbs);
    }

    @Override
    public int compareTo(FuzzyFlower other) {
        return Integer.compare(this.hashCode(), other.hashCode());
    }

    @Override
    public int hashCode() {
        return (this.species.ordinal() * 100) + (this.color.ordinal() * 10) + this.variants.size();
    }

    @Override
    public Map<SpecificFlower, Double> getVariantProbs() {
        return this.variants;
    }

    @Override
    public double getTotalProbability() {
        double total = 0;
        for (double prob : this.variants.values())
            total += prob;

        return total;
    }

    void putVariant(SpecificFlower flower, double prob) {
        // only allowed inside package!
        this.variants.put(flower, prob);
    }

    double getVariantProbability(SpecificFlower flower) {
        // only allowed inside package!
        Double prob = this.variants.get(flower);
        if (prob == null)
            return 0.0;
        else return prob;
    }
}
