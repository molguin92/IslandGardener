/*
 *     Copyright © 2020 Manuel Olguín Muñoz <manuel@olguin.se>
 *
 *     FlowerCollection.java is part of Island Gardener
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

import org.molguin.islandgardener.R;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
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

    public SortedSet<FlowerColorGroup> getAllColorGroupsForSpecies(FlowerConstants.Species s) {
        return this.speciesCollections.get(s).getAllColorGroups();
    }

    public SortedSet<SpecificFlower> getAllFlowersForSpecies(FlowerConstants.Species s) {
        return this.speciesCollections.get(s).getAllFlowers();
    }

    public SortedSet<FuzzyFlower> getAllOffspring(FuzzyFlower parent1, FuzzyFlower parent2) {
        if (parent1.getSpecies() != parent2.getSpecies()) throw new AssertionError();
        return this.speciesCollections.get(parent1.getSpecies()).getOffspring(parent1, parent2);
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

        SortedSet<SpecificFlower> getAllFlowers() {
            return new TreeSet<SpecificFlower>(this.idFlowerBiMap.values());
        }

        SortedSet<FlowerColorGroup> getAllColorGroups() {
            SortedSet<FlowerColorGroup> containers = new TreeSet<FlowerColorGroup>();

            for (FlowerConstants.Color c : this.colorFlowerMap.keySet())
                containers.add(new FlowerColorGroup(species, c, this.colorFlowerMap.get(c)));

            return containers;
        }

        SortedSet<FuzzyFlower> getOffspring(FuzzyFlower parent1, FuzzyFlower parent2) {
            BiMap<FlowerConstants.Color, FuzzyFlower> results = HashBiMap.create();
            for (Map.Entry<SpecificFlower, Double> p1 : parent1.getVariantProbs().entrySet()) {
                for (Map.Entry<SpecificFlower, Double> p2 : parent2.getVariantProbs().entrySet()) {
                    for (Map.Entry<SpecificFlower, Double> c : this.matings.get(p1.getKey(), p2.getKey()).entrySet()) {
                        SpecificFlower child = c.getKey();
                        FlowerColorGroup cgroup = (FlowerColorGroup) results.get(child.color);
                        if (cgroup == null)
                            cgroup = new FlowerColorGroup(this.species, child.color, new TreeMap<SpecificFlower, Double>());

                        double prevProb = cgroup.getVariantProbability(child);
                        double newProb = (c.getValue() * p1.getValue() * p2.getValue()) + prevProb;
                        cgroup.putVariant(child, newProb);
                        results.put(cgroup.color, cgroup);
                    }
                }
            }

            // return offspring ordered by descending probability
            SortedSet<FuzzyFlower> resultSet = new TreeSet<FuzzyFlower>(new Comparator<FuzzyFlower>() {
                @Override
                public int compare(FuzzyFlower f1, FuzzyFlower f2) {
                    // if F1 and F2 have exactly the same probability, we need to compare them based on their hashcodes
                    int prob_comp = Double.compare(f2.getTotalProbability(), f1.getTotalProbability());
                    if (prob_comp == 0)
                        return Integer.compare(f1.hashCode(), f2.hashCode());
                    else return prob_comp;
                }
            });

            resultSet.addAll(results.values());
            return resultSet;
        }
    }
}
