package org.molguin.acbreedinghelper.flowers;

import java.util.Random;

public class Genotype {
    final int encoded;
    final String human_readable;
    private final Gene red;
    private final Gene ylw;
    private final Gene wht;
    private final Gene shd;

    Genotype(Gene red, Gene ylw, Gene wht, Gene shd) {
        this.red = red;
        this.ylw = ylw;
        this.wht = wht;
        this.shd = shd;

        this.encoded = (red.value * 1000) + (ylw.value * 100) + (wht.value * 10) + shd.value;
        this.human_readable = Genotype.genHumanReadableRep(
                new Gene[]{this.red, this.ylw, this.wht, this.shd}, new char[]{'r', 'y', 'w', 's'});

    }

    Genotype(int encoded) {
        this.encoded = encoded % 2223;
        this.red = new Gene((this.encoded / 1000) % 10);
        this.ylw = new Gene((this.encoded / 100) % 10);
        this.wht = new Gene((this.encoded / 10) % 10);
        this.shd = new Gene(this.encoded % 10);
        this.human_readable = Genotype.genHumanReadableRep(
                new Gene[]{this.red, this.ylw, this.wht, this.shd}, new char[]{'r', 'y', 'w', 's'});

    }

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

    @Override
    public int hashCode() {
        return this.encoded;
    }

    Genotype getRandomOffspring(Genotype other) {
        Genotype[] offspring = this.getAllPossibleOffspring(other);
        return offspring[new Random().nextInt(offspring.length)];
    }

    Genotype[] getAllPossibleOffspring(Genotype other) {
        Gene[] red_offspring = this.red.getAllPossibleOffspring(other.red);
        Gene[] ylw_offspring = this.ylw.getAllPossibleOffspring(other.ylw);
        Gene[] wht_offspring = this.wht.getAllPossibleOffspring(other.wht);
        Gene[] shd_offspring = this.shd.getAllPossibleOffspring(other.shd);

        int total = red_offspring.length * ylw_offspring.length
                * wht_offspring.length * shd_offspring.length;

        Genotype[] offspring = new Genotype[total];

        int idx = 0;
        for (Gene red : red_offspring)
            for (Gene ylw : ylw_offspring)
                for (Gene wht : wht_offspring)
                    for (Gene shd : shd_offspring)
                        offspring[idx++] = new Genotype(red, ylw, wht, shd);

        return offspring;
    }

    static class Gene {
        final int value;
        final byte[] alleles;

        private Gene(byte left, byte right) {
            if (left != right) {
                this.alleles = new byte[]{1, 0};
            } else {
                this.alleles = right > 0 ? new byte[]{1, 1} : new byte[]{0, 0};
            }
            this.value = this.alleles[0] + this.alleles[1];
        }

        private Gene(int value) {
            this.value = value % 3;
            switch (this.value) {
                case 0:
                    this.alleles = new byte[]{0, 0};
                    break;
                case 1:
                    this.alleles = new byte[]{1, 0};
                    break;
                case 2:
                    this.alleles = new byte[]{1, 1};
                    break;
                default:
                    throw new RuntimeException();
            }
        }

        Gene[] getAllPossibleOffspring(Gene other) {
            byte[] p1 = this.getAlleles();
            byte[] p2 = other.getAlleles();

            Gene[] results = new Gene[p1.length * p2.length];
            int idx = 0;
            for (byte a1 : p1)
                for (byte a2 : p2)
                    results[idx++] = new Gene(a1, a2);

            return results;
        }

        byte[] getAlleles() {
            return this.alleles;
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


    }
}
