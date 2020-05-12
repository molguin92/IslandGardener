/*
 *     Copyright © 2020 Manuel Olguín Muñoz <manuel@olguin.se>
 *
 *     SpecificFlower.java is part of Island Gardener
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
import java.util.TreeMap;

public class SpecificFlower extends AbstractFuzzyFlower {
    private final static String[] SYMBOLS = new String[]{"r", "y", "w", "s"};
    public final FlowerConstants.Origin origin;
    final int genotype_id;


    SpecificFlower(FlowerConstants.Species species,
                   FlowerConstants.Color color,
                   FlowerConstants.Origin origin,
                   int genotype_id) {
        super(species, color);
        this.origin = origin;
        this.genotype_id = genotype_id;
    }

    @Override
    public int hashCode() {
        return (this.species.ordinal() * 100000) + (this.color.ordinal() * 10000) + this.genotype_id;
    }

    @Override
    public Map<SpecificFlower, Double> getVariantProbs() {
        Map<SpecificFlower, Double> set = new TreeMap<SpecificFlower, Double>();
        set.put(this, 1.0);
        return set;
    }

    @Override
    public double getTotalProbability() {
        return 1.0;
    }

    @Override
    public String humanReadableVariants(boolean invWGene) {
        return this.genotypeIDToSymbolic(invWGene);
    }

    public String genotypeIDToSymbolic(boolean invWGene) {
        StringBuilder sb = new StringBuilder(8);
        int iter = this.genotype_id;
        String[] gene_symbols = new String[4];
        for (int i = 3; i >= 0; i--) {
            // loop goes back to front
            int enc = iter % 10;
            String symbol = SYMBOLS[i];
            switch (enc) {
                case 0:
                    if (invWGene && symbol.equals("w")) {
                        String symb = symbol.toUpperCase();
                        gene_symbols[i] = symb + symb;
                    } else
                        gene_symbols[i] = symbol + symbol;
                    break;
                case 1:
                    gene_symbols[i] = symbol.toUpperCase() + symbol;
                    break;
                case 2:
                    if (invWGene && symbol.equals("w"))
                        gene_symbols[i] = symbol + symbol;
                    else {
                        String symb = symbol.toUpperCase();
                        gene_symbols[i] = symb + symb;
                    }
                    break;
            }

            iter = iter / 10;
        }

        for (String s : gene_symbols)
            sb.append(s);

        return sb.toString();
    }

    @Override
    public boolean isGroup() {
        return false;
    }
}
