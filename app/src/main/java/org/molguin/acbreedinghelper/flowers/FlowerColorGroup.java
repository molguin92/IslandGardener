package org.molguin.acbreedinghelper.flowers;

import java.util.Set;
import java.util.TreeSet;

public class FlowerColorGroup extends AbstractFuzzyFlower {
    public final String icon_name;
    public final Set<SpecificFlower> variants;

    public FlowerColorGroup(FlowerConstants.Species species,
                            FlowerConstants.Color color,
                            Set<SpecificFlower> variants) {
        super(species, color);
        this.variants = new TreeSet<SpecificFlower>(variants);
        this.icon_name = String.format("%s_%s",
                species.name().toLowerCase(),
                color.name().toLowerCase());
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
    public Set<SpecificFlower> getVariants() {
        return this.variants;
    }
}
