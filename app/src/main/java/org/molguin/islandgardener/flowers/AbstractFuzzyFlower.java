/*
 *     Copyright © 2020 Manuel Olguín Muñoz <manuel@olguin.se>
 *
 *     AbstractFuzzyFlower.java is part of Island Gardener
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

abstract class AbstractFuzzyFlower implements FuzzyFlower {
    protected final FlowerConstants.Color color;
    protected final FlowerConstants.Species species;
    private final String icon_name;

    AbstractFuzzyFlower(FlowerConstants.Species species, FlowerConstants.Color color) {
        this.species = species;
        this.color = color;
        this.icon_name = genIconName(species, color);
    }

    private static String genIconName(FlowerConstants.Species species, FlowerConstants.Color color) {
        return String.format("%s_%s", species.name().toLowerCase(), color.name().toLowerCase());
    }

    @Override
    public FlowerConstants.Color getColor() {
        return this.color;
    }

    @Override
    public FlowerConstants.Species getSpecies() {
        return this.species;
    }

    @Override
    public int compareTo(FuzzyFlower other) {
        if (this.isGroup() == other.isGroup())
            return Integer.compare(this.hashCode(), other.hashCode());
        else if (this.isGroup()) // this one is group, other is not
            return -1;
        else
            return 1; // this one is not group, other is
    }

    @Override
    public String getIconName() {
        return this.icon_name;
    }
}
