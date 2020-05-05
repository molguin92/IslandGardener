package org.molguin.acbreedinghelper.flowers;

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
    public String getIconName() {
        return this.icon_name;
    }

    @Override
    public int compareTo(FuzzyFlower other) {
        return Integer.compare(this.hashCode(), other.hashCode());
    }
}