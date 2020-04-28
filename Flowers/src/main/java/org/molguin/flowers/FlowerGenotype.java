package org.molguin.flowers;


public class FlowerGenotype implements Comparable<FlowerGenotype> {
    final int encoded;
    final FlowerConstants.Species species;
    final protected Genotype genes;

    public FlowerGenotype(FlowerConstants.Species species, Genotype base_genes) {
        this.species = species;
        this.genes = base_genes;
        this.encoded = (this.species.ordinal() * 10000) + this.genes.encoded;
    }

    public FlowerGenotype[] getAllOffSpring(FlowerGenotype mate) {
        if (this.species != mate.species) throw new AssertionError(); // todo: verbose
        Genotype[] offspring_genes = this.genes.getAllPossibleOffspring(mate.genes);
        FlowerGenotype[] offspring = new FlowerGenotype[offspring_genes.length];
        for (int i = 0; i < offspring_genes.length; i++)
            offspring[i] = new FlowerGenotype(this.species, offspring_genes[i]);
        return offspring;
    }

    @Override
    public int hashCode() {
        return this.encoded;
    }


    @Override
    public int compareTo(FlowerGenotype other) {
        return Integer.compare(this.encoded, other.encoded);
    }
}
