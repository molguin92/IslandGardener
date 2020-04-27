package org.molguin.flowers;

import com.eclipsesource.json.JsonObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FlowerDatabase {
    private final Map<FlowerConstants.Species, Map<String, Flower>> speciesMap;
    private final Map<FlowerConstants.Origin, Map<String, Flower>> originMap;
    private final Map<SpeciesColorKey, Set<Flower>> speciesColorMap;

    public FlowerDatabase(JsonObject flower_json) {
        // on instantiation, read the JSON data and set up internal map of flowers
        this.speciesMap = new HashMap<>();
        this.originMap = new HashMap<>();
        this.speciesColorMap = new HashMap<>();

        for (FlowerConstants.Species species : FlowerConstants.Species.values()) {
            // prepare mapping species -> set of flowers
            this.speciesMap.put(species, new HashMap<>());
            for (FlowerConstants.Color color : FlowerConstants.Color.values())
                speciesColorMap.put(new SpeciesColorKey(species, color), new HashSet<>());
        }

        for (FlowerConstants.Origin origin : FlowerConstants.Origin.values())
            // prepare mapping origin -> set of flowers
            this.originMap.put(origin, new HashMap<>());


        for (JsonObject.Member species_mb : flower_json) {
            // this for loop populates the species and origin maps with the flowers.
            for (JsonObject.Member encoding_mb : species_mb.getValue().asObject()) {
                String encoding = encoding_mb.getName();
                JsonObject props = encoding_mb.getValue().asObject();

                // get properties
                String color = props.getString("color", null);
                String origin = props.getString("origin", null);

                // build the flower and store the reference both in the species map and in the origin map
                Flower flower = new Flower(FlowerConstants.Species.valueOf(species_mb.getName()),
                        FlowerConstants.Color.valueOf(color),
                        FlowerConstants.Origin.valueOf(origin),
                        new FlowerGenotype(encoding));

                Map<String, Flower> map_for_species = this.speciesMap.get(flower.species);
                Map<String, Flower> map_for_origins = this.originMap.get(flower.origin);

                map_for_species.put(flower.getEncodedGenotype(), flower);
                map_for_origins.put(flower.getEncodedGenotype(), flower);

                this.speciesColorMap
                        .get(new SpeciesColorKey(flower.species, flower.color))
                        .add(flower);
            }
        }
    }

    public Collection<Flower> getAllFlowersForSpecies(FlowerConstants.Species species) {
        return this.speciesMap.get(species).values();
    }

    private static class SpeciesColorKey {
        final FlowerConstants.Species species;
        final FlowerConstants.Color color;

        SpeciesColorKey(FlowerConstants.Species species, FlowerConstants.Color color) {
            this.species = species;
            this.color = color;
        }

        @Override
        public boolean equals(Object other) {
            return other != null && other.hashCode() == this.hashCode();

        }

        @Override
        public int hashCode() {
            return (this.species.ordinal() * 10) + this.color.ordinal();
        }
    }
}
