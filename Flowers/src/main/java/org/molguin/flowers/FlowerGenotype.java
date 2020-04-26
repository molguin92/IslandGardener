package org.molguin.flowers;

import java.util.Locale;
import java.util.Random;

public class FlowerGenotype {
    private static class Gene {
        final int value;

        private Gene(boolean left, boolean right) {
            if (left != right) {
                this.value = 1;
            } else {
                this.value = right ? 2 : 0;
            }
        }

        private Gene(int value) {
            this.value = value % 3;
        }

        @Override
        public boolean equals(Object obj) {
            return (obj != null)
                    && (obj.getClass() == this.getClass())
                    && (obj.hashCode() == this.hashCode());
        }

        @Override
        public int hashCode() {
            return this.value;
        }

        boolean[] getAlleles() {
            boolean[] result = new boolean[2];
            switch (this.value) {
                case 0:
                    break;
                case 1:
                    result[0] = true;
                    break;
                case 2:
                    result[0] = result[1] = true;
                    break;
                default:
                    throw new RuntimeException(String.valueOf(this.value));

            }
            return result;
        }

        Gene[] getAllPossibleOffspring(Gene other) {
            boolean[] p1 = this.getAlleles();
            boolean[] p2 = other.getAlleles();

            Gene[] results = new Gene[p1.length * p2.length];
            int idx = 0;
            for (boolean a1 : p1)
                for (boolean a2 : p2)
                    results[idx++] = new Gene(a1, a2);

            return results;
        }
    }

    private final Gene red;
    private final Gene ylw;
    private final Gene wht;
    private final Gene shd;
    final String encoded;
    final String human_readable;

    private static String genHumanReadableRep(Gene[] genes, char[] symbols) {
        if (genes.length != symbols.length) throw new AssertionError();
        StringBuilder rep = new StringBuilder();
        for (int i = 0; i < genes.length; i++) {
            String symbol = String.valueOf(symbols[i]);
            switch (genes[i].value) {
                case 0:
                    symbol = symbol.toLowerCase();
                    rep.append(symbol).append(symbol);
                    break;
                case 1:
                    rep.append(symbol.toUpperCase()).append(symbol.toLowerCase());
                    break;
                case 2:
                    symbol = symbol.toUpperCase();
                    rep.append(symbol).append(symbol);
                    break;
            }
        }
        return rep.toString();
    }

    FlowerGenotype(Gene red, Gene ylw, Gene wht, Gene shd) {
        this.red = red;
        this.ylw = ylw;
        this.wht = wht;
        this.shd = shd;

        this.encoded = String.format(Locale.getDefault(), "%04d",
                (red.value * 1000) + (ylw.value * 100) +
                        (wht.value * 10) + shd.value);
        this.human_readable = FlowerGenotype.genHumanReadableRep(
                new Gene[]{this.red, this.ylw, this.wht, this.shd}, new char[]{'r', 'y', 'w', 's'});

    }

    FlowerGenotype(String encoded) {
        this.encoded = encoded.substring(0, 4);
        char[] values = this.encoded.toCharArray();

        this.red = new Gene(Integer.parseInt(String.valueOf(values[0])));
        this.ylw = new Gene(Integer.parseInt(String.valueOf(values[1])));
        this.wht = new Gene(Integer.parseInt(String.valueOf(values[2])));
        this.shd = new Gene(Integer.parseInt(String.valueOf(values[3])));
        this.human_readable = FlowerGenotype.genHumanReadableRep(
                new Gene[]{this.red, this.ylw, this.wht, this.shd}, new char[]{'r', 'y', 'w', 's'});

    }

    FlowerGenotype[] getAllPossibleOffspring(FlowerGenotype other) {
        Gene[] red_offspring = this.red.getAllPossibleOffspring(other.red);
        Gene[] ylw_offspring = this.ylw.getAllPossibleOffspring(other.ylw);
        Gene[] wht_offspring = this.wht.getAllPossibleOffspring(other.wht);
        Gene[] shd_offspring = this.shd.getAllPossibleOffspring(other.shd);

        int total = red_offspring.length * ylw_offspring.length
                * wht_offspring.length * shd_offspring.length;

        FlowerGenotype[] offspring = new FlowerGenotype[total];

        int idx = 0;
        for (Gene red : red_offspring)
            for (Gene ylw : ylw_offspring)
                for (Gene wht : wht_offspring)
                    for (Gene shd : shd_offspring)
                        offspring[idx++] = new FlowerGenotype(red, ylw, wht, shd);

        return offspring;
    }

    FlowerGenotype getRandomOffspring(FlowerGenotype other) {
        FlowerGenotype[] offspring = this.getAllPossibleOffspring(other);
        return offspring[new Random().nextInt(offspring.length)];
    }
}
