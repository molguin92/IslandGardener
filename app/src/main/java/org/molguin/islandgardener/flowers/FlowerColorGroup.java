package org.molguin.islandgardener.flowers;

import android.util.Log;

import java.util.Collection;
import java.util.Iterator;
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
    public int hashCode() {
        return (this.species.ordinal()) * 100 + this.color.ordinal();
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

    @Override
    public String humanReadableVariants() {
        Iterator<SpecificFlower> iter = this.variants.keySet().iterator();
        if (!iter.hasNext()) return "";

        StringBuilder sb = new StringBuilder();
        while (true) {
            sb.append(iter.next().human_readable_genotype);
            if (iter.hasNext()) {
                sb.append(", ");
            } else break;
        }
        return sb.toString();
    }

    @Override
    public boolean isGroup() {
        return true;
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
