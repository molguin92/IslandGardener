package org.molguin.acbreedinghelper.flowers;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.TreeMultimap;

import org.molguin.acbreedinghelper.R;
import org.molguin.acbreedinghelper.utils.Callback;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FlowerCollection {
    private final Lock dbLock;
    private final Condition loadedCond;
    private final Map<FlowerConstants.Species, SpeciesCollection> speciesCollections;
    private final ExecutorService execServ;
    private boolean dataLoaded;

    public FlowerCollection(final AssetManager am, final Context appContext) {
        this.dbLock = new ReentrantLock();
        this.loadedCond = dbLock.newCondition();
        this.dataLoaded = false;
        this.execServ = Executors.newFixedThreadPool(FlowerConstants.Species.values().length / 2);

        // on instantiation, read the JSON data and set up internal map of flowers
        this.speciesCollections = new ConcurrentHashMap<FlowerConstants.Species, SpeciesCollection>();

        // load everything in the background
        this.execServ.submit(new LoggingRunnable() {
            @Override
            public void execute() throws Exception {
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

                Log.d("Data", "Finished loading.");
                // finally, notify that we are loaded and ready to go
                FlowerCollection.this.dbLock.lock();
                try {
                    FlowerCollection.this.dataLoaded = true;
                    FlowerCollection.this.loadedCond.signalAll();
                } finally {
                    FlowerCollection.this.dbLock.unlock();
                }
            }
        });
    }

    public void shutdown() throws InterruptedException {
        this.waitUntilLoaded();
        this.execServ.awaitTermination(1, TimeUnit.SECONDS);
        this.execServ.shutdown();
    }

    private void waitUntilLoaded() throws InterruptedException {
        this.dbLock.lock();
        try {
            while (!this.dataLoaded)
                this.loadedCond.await();
        } finally {
            this.dbLock.unlock();
        }
    }

    public void applyToSpecies(final FlowerConstants.Species species,
                               final Callback<Set<Flower>, Void> callback) {
        this.execServ.submit(new CallbackRunnable() {
            @Override
            void execute() {
                callback.apply(FlowerCollection.this.speciesCollections.get(species).getAllFlowers());
            }
        });
    }

    public void applyToColor(final FlowerConstants.Species species,
                             final FlowerConstants.Color color,
                             final Callback<Set<Flower>, Void> callback) {
        this.execServ.submit(new CallbackRunnable() {
            @Override
            void execute() {
                callback.apply(FlowerCollection.this.speciesCollections
                        .get(species)
                        .getFlowersForColor(color));
            }
        });
    }

    public void applyToOrigin(final FlowerConstants.Species species,
                              final FlowerConstants.Origin origin,
                              final Callback<Set<Flower>, Void> callback) {
        this.execServ.submit(new CallbackRunnable() {
            @Override
            void execute() {
                callback.apply(FlowerCollection.this.speciesCollections
                        .get(species)
                        .getFlowersForOrigin(origin));
            }
        });
    }

    public void applyToMating(final Flower parent1, final Flower parent2,
                              final Callback<Map<Flower, Double>, Void> callback) {
        if (parent1.species != parent2.species) throw new AssertionError();
        this.execServ.submit(new CallbackRunnable() {
            @Override
            void execute() {
                callback.apply(FlowerCollection.this.speciesCollections
                        .get(parent1.species)
                        .getOffspring(parent1, parent2));
            }
        });
    }

    private static class MatingKey implements Comparable<MatingKey> {
        Flower parent1;
        Flower parent2;

        MatingKey(Flower parent1_id, Flower parent2_id) {
            if (parent1_id.compareTo(parent2_id) >= 0) {
                this.parent1 = parent1_id;
                this.parent2 = parent2_id;
            } else {
                this.parent1 = parent2_id;
                this.parent2 = parent1_id;
            }
        }

        @Override
        public int compareTo(MatingKey other) {
            return Integer.compare(this.hashCode(), other.hashCode());
        }

        @Override
        public int hashCode() {
            return (this.parent1.hashCode() * 10000) + this.parent2.hashCode();
        }
    }

    private static class SpeciesCollection {
        private final FlowerConstants.Species species;
        private final Multimap<FlowerConstants.Color, Flower> colorFlowerMap;
        private final Multimap<FlowerConstants.Origin, Flower> originFlowerMap;
        private final BiMap<Integer, Flower> idFlowerBiMap;
        private final Map<MatingKey, Map<Flower, Double>> matingMap;

        SpeciesCollection(FlowerConstants.Species species) {
            this.species = species;
            this.colorFlowerMap =
                    Multimaps.synchronizedSortedSetMultimap(
                            TreeMultimap.<FlowerConstants.Color, Flower>create());
            this.originFlowerMap =
                    Multimaps.synchronizedSortedSetMultimap(
                            TreeMultimap.<FlowerConstants.Origin, Flower>create());


            this.matingMap = new ConcurrentHashMap<MatingKey, Map<Flower, Double>>();
            this.idFlowerBiMap = Maps.synchronizedBiMap(HashBiMap.<Integer, Flower>create());
        }

        void registerFlower(int id, FlowerConstants.Color color, FlowerConstants.Origin origin) {
            Flower flower = new Flower(this.species, color, origin, id);
            this.colorFlowerMap.put(color, flower);
            this.originFlowerMap.put(origin, flower);
            this.idFlowerBiMap.put(id, flower);
        }

        void registerMating(int parent1, int parent2, Map<Integer, Double> offspringProbMap) {
            Map<Flower, Double> offspringMap = new ConcurrentHashMap<Flower, Double>();
            for (Map.Entry<Integer, Double> e : offspringProbMap.entrySet())
                offspringMap.put(this.idFlowerBiMap.get(e.getKey()), e.getValue());

            this.matingMap.put(
                    new MatingKey(
                            this.idFlowerBiMap.get(parent1),
                            this.idFlowerBiMap.get(parent2)), offspringMap);
        }

        Set<Flower> getAllFlowers() {
            return this.idFlowerBiMap.values();
        }

        Set<Flower> getFlowersForColor(FlowerConstants.Color color) {
            return new HashSet<Flower>(this.colorFlowerMap.get(color));
        }

        Set<Flower> getFlowersForOrigin(FlowerConstants.Origin origin) {
            return new HashSet<Flower>(this.originFlowerMap.get(origin));
        }

        Map<Flower, Double> getOffspring(Flower parent1, Flower parent2) {
            Map<Flower, Double> offspring = this.matingMap.get(new MatingKey(parent1, parent2));
            return new HashMap<Flower, Double>(offspring);
        }
    }

    static private abstract class LoggingRunnable implements Runnable {
        @Override
        public void run() {
            try {
                this.execute();
            } catch (Exception e) {
                Log.e("ERROR", e.toString());
            }
        }

        abstract void execute() throws Exception;

    }

    private abstract class CallbackRunnable extends LoggingRunnable {
        @Override
        public void run() {
            try {
                FlowerCollection.this.waitUntilLoaded();
                super.run();
            } catch (InterruptedException e) {
                Log.e("INTERRUPTED", e.toString());
            }
        }
    }
}
