package org.molguin.acbreedinghelper.flowers;

import java.util.HashSet;
import java.util.Set;

public class SpecificFlower extends AbstractFuzzyFlower {
    private final static String[] SYMBOLS = new String[]{"r", "y", "w", "s"};
    public final FlowerConstants.Origin origin;
    public final String human_readable_genotype;
    final int genotype_id;


    SpecificFlower(FlowerConstants.Species species,
                   FlowerConstants.Color color,
                   FlowerConstants.Origin origin,
                   int genotype_id) {
        super(species, color);
        this.origin = origin;
        this.genotype_id = genotype_id;
        this.human_readable_genotype = genotypeIDToSymbolic(genotype_id);
    }

    private static String genotypeIDToSymbolic(int genotype_id) {
        StringBuilder sb = new StringBuilder(8);
        int iter = genotype_id;
        String[] gene_symbols = new String[4];
        for (int i = 3; i >= 0; i--) {
            // loop goes back to front
            int enc = iter % 10;
            String symbol = SYMBOLS[i];
            switch (enc) {
                case 0:
                    gene_symbols[i] = symbol + symbol;
                    break;
                case 1:
                    gene_symbols[i] = symbol.toUpperCase() + symbol;
                    break;
                case 2:
                    String symb = symbol.toUpperCase();
                    gene_symbols[i] = symb + symb;
                    break;
            }

            iter = iter / 10;
        }

        for (String s : gene_symbols)
            sb.append(s);

        return sb.toString();
    }

    @Override
    public int hashCode() {
        return (this.species.ordinal() * 10000) + this.genotype_id;
    }

    @Override
    public Set<SpecificFlower> getVariants() {
        Set<SpecificFlower> set = new HashSet<SpecificFlower>();
        set.add(this);
        return set;
    }
}
