package org.molguin.acbreedinghelper.flowers;

import androidx.annotation.NonNull;

import java.util.Set;

public interface FuzzyFlower extends Comparable<FuzzyFlower> {
    Set<SpecificFlower> getVariants();

    FlowerConstants.Color getColor();

    FlowerConstants.Species getSpecies();

    @Override
    int hashCode();

    @Override
    boolean equals(Object other);

    @Override
    int compareTo(FuzzyFlower other);

    String getIconName();
}
