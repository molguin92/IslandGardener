package org.molguin.islandgardener.flowers;

import java.util.Map;

public interface FuzzyFlower extends Comparable<FuzzyFlower> {
    Map<SpecificFlower, Double> getVariantProbs();

    FlowerConstants.Color getColor();

    FlowerConstants.Species getSpecies();

    @Override
    int hashCode();

    @Override
    boolean equals(Object other);

    @Override
    int compareTo(FuzzyFlower other);

    String getIconName();

    double getTotalProbability();

    String humanReadableVariants();

    boolean isGroup();
    
}
