package org.molguin.acbreedinghelper.model;

import org.molguin.acbreedinghelper.flowers.Flower;
import org.molguin.acbreedinghelper.flowers.FlowerConstants;

import java.util.Set;

public class FuzzyFlower implements Comparable<FuzzyFlower> {
    public final String icon_name;
    public final FlowerConstants.Species species;
    public final FlowerConstants.Color color;
    public final Set<Flower> variants;

    FuzzyFlower(FlowerConstants.Species species,
                FlowerConstants.Color color,
                Set<Flower> variants) {
        this.species = species;
        this.color = color;
        this.variants = variants;
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
        return (this.species.ordinal() * 10) + this.color.ordinal();
    }
}
