package org.molguin.acbreedinghelper.flowers;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.google.common.collect.TreeMultimap;

import org.molguin.acbreedinghelper.R;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FlowerCollection {
    private final Map<FlowerConstants.Species, SpeciesCollection> speciesCollections;

    public FlowerCollection(final AssetManager am,
                            final Context appContext) throws IOException {

        // on instantiation, read the JSON data and set up internal map of flowers
        this.speciesCollections = new ConcurrentHashMap<FlowerConstants.Species, SpeciesCollection>();

        Reader reader = new InputStreamReader(am.open(appContext.getString(R.string.flower_json)));
        JsonObject flower_json = Json.parse(reader).asObject();
        // JSON structure:
        // { species:
        //      variants: { id : { color: color, origin: origin}, ...}
        //      matings: { idp1: {idp2: { child1: prob, child2: prob}, ...}}
        // }


        for (JsonObject.Member species_data : flower_json) {
            FlowerConstants.Species species =
                    FlowerConstants.Species.valueOf(species_data.getName());

            Log.d("Data", "Loading flower data for " + species.namePlural());

            SpeciesCollection collection = new SpeciesCollection(species);

            JsonObject spec_obj = species_data.getValue().asObject();
            JsonObject variants = spec_obj.get("variants").asObject();
            JsonObject matings = spec_obj.get("matings").asObject();

            for (JsonObject.Member v : variants) {
                JsonObject values = v.getValue().asObject();
                int variant_id = Integer.parseInt(v.getName());
                FlowerConstants.Color color =
                        FlowerConstants.Color.valueOf(values.get("color").asString());
                FlowerConstants.Origin origin =
                        FlowerConstants.Origin.valueOf(values.get("origin").asString());

                collection.registerFlower(variant_id, color, origin);
            }

            for (JsonObject.Member mate1 : matings) {
                int parent1 = Integer.parseInt(mate1.getName());
                for (JsonObject.Member mate2 : mate1.getValue().asObject()) {
                    int parent2 = Integer.parseInt(mate2.getName());
                    Map<Integer, Double> offspring_probs = new HashMap<Integer, Double>();

                    for (JsonObject.Member offspring : mate2.getValue().asObject()) {
                        int offspring_id = Integer.parseInt(offspring.getName());
                        double prob = offspring.getValue().asDouble();
                        offspring_probs.put(offspring_id, prob);
                    }

                    collection.registerMating(parent1, parent2, offspring_probs);
                }
            }

            FlowerCollection.this.speciesCollections.put(species, collection);
        }
    }

    public Set<SpecificFlower> getAllFlowersForSpecies(FlowerConstants.Species s) {
        return this.speciesCollections.get(s).getAllFlowers();
    }

    public Map<SpecificFlower, Double> getAllOffspring(SpecificFlower parent1, SpecificFlower parent2) {
        if (parent1.species != parent2.species) throw new AssertionError();
        return this.speciesCollections.get(parent1.species).getOffspring(parent1, parent2);
    }

    private static class SpeciesCollection {
        private final FlowerConstants.Species species;
        private final Multimap<FlowerConstants.Color, SpecificFlower> colorFlowerMap;
        private final Multimap<FlowerConstants.Origin, SpecificFlower> originFlowerMap;
        private final BiMap<Integer, SpecificFlower> idFlowerBiMap;
        private final Table<SpecificFlower, SpecificFlower, Map<SpecificFlower, Double>> matings;

        SpeciesCollection(FlowerConstants.Species species) {
            this.species = species;
            this.colorFlowerMap =
                    Multimaps.synchronizedSortedSetMultimap(
                            TreeMultimap.<FlowerConstants.Color, SpecificFlower>create());
            this.originFlowerMap =
                    Multimaps.synchronizedSortedSetMultimap(
                            TreeMultimap.<FlowerConstants.Origin, SpecificFlower>create());

            this.matings = Tables.synchronizedTable(HashBasedTable.<SpecificFlower, SpecificFlower, Map<SpecificFlower, Double>>create());
            this.idFlowerBiMap = Maps.synchronizedBiMap(HashBiMap.<Integer, SpecificFlower>create());
        }

        void registerFlower(int id, FlowerConstants.Color color, FlowerConstants.Origin origin) {
            SpecificFlower specificFlower = new SpecificFlower(this.species, color, origin, id);
            this.colorFlowerMap.put(color, specificFlower);
            this.originFlowerMap.put(origin, specificFlower);
            this.idFlowerBiMap.put(id, specificFlower);
        }

        void registerMating(int parent1, int parent2, Map<Integer, Double> offspringProbMap) {
            Map<SpecificFlower, Double> offspringMap = new ConcurrentHashMap<SpecificFlower, Double>();
            for (Map.Entry<Integer, Double> e : offspringProbMap.entrySet())
                offspringMap.put(this.idFlowerBiMap.get(e.getKey()), e.getValue());

            SpecificFlower specificFlower1 = this.idFlowerBiMap.get(parent1);
            SpecificFlower specificFlower2 = this.idFlowerBiMap.get(parent2);

            this.matings.put(specificFlower1, specificFlower2, offspringMap);
            this.matings.put(specificFlower2, specificFlower1, offspringMap);
        }

        Set<SpecificFlower> getAllFlowers() {
            return this.idFlowerBiMap.values();
        }

        Set<SpecificFlower> getFlowersForColor(FlowerConstants.Color color) {
            return new HashSet<SpecificFlower>(this.colorFlowerMap.get(color));
        }

        Set<SpecificFlower> getFlowersForOrigin(FlowerConstants.Origin origin) {
            return new HashSet<SpecificFlower>(this.originFlowerMap.get(origin));
        }

        Map<SpecificFlower, Double> getOffspring(SpecificFlower parent1, SpecificFlower parent2) {
            Map<SpecificFlower, Double> offspring = this.matings.get(parent1, parent2);
            return new HashMap<SpecificFlower, Double>(offspring);
        }
    }
}
