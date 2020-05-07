/*
 *     Copyright © 2020 Manuel Olguín Muñoz <manuel@olguin.se>
 *
 *     FuzzyFlower.java is part of Island Gardener
 *
 *     Island Gardener is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Island Gardener is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Island Gardener.  If not, see <https://www.gnu.org/licenses/>.
 */

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
