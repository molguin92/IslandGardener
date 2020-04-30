package org.molguin.acbreedinghelper.flowers;

import android.content.Context;
import android.content.res.AssetManager;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import org.molguin.acbreedinghelper.R;
import org.molguin.acbreedinghelper.utils.Callback;
import org.molguin.acbreedinghelper.utils.ReversibleMap;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FlowerDatabase {
    private final Lock dbLock;
    private final Condition loadedCond;
    private final ReversibleMap<FlowerGenotype, FlowerConstants.Color> colorMap;
    private final ReversibleMap<FlowerGenotype, FlowerConstants.Species> speciesMap;
    private final ReversibleMap<FlowerGenotype, FlowerConstants.Origin> originMap;
    private final ExecutorService execServ;
    private boolean dataLoaded;

    public FlowerDatabase(final AssetManager am, final Context appContext) {
        this.dbLock = new ReentrantLock();
        this.loadedCond = dbLock.newCondition();
        this.dataLoaded = false;
        this.execServ = Executors.newFixedThreadPool(FlowerConstants.Species.values().length / 2);

        // on instantiation, read the JSON data and set up internal map of flowers
        this.speciesMap = new ReversibleMap<FlowerGenotype, FlowerConstants.Species>();
        this.originMap = new ReversibleMap<FlowerGenotype, FlowerConstants.Origin>();
        this.colorMap = new ReversibleMap<FlowerGenotype, FlowerConstants.Color>();

        // load everything in the background
        this.execServ.submit(new Runnable() {
            @Override
            public void run() {
                Reader reader = null;
                try {
                    reader = new InputStreamReader(am.open(appContext.getString(R.string.flower_json)));
                    JsonObject flower_json = Json.parse(reader).asObject();
                    for (JsonObject.Member species_mb : flower_json) {
                        // this for loop populates the species and origin maps with the flowers.
                        for (JsonObject.Member encoding_mb : species_mb.getValue().asObject()) {
                            JsonObject props = encoding_mb.getValue().asObject();

                            // get properties
                            String encoding_str = encoding_mb.getName();
                            String color_str = props.getString("color", null);
                            String origin_str = props.getString("origin", null);

                            FlowerConstants.Species species = FlowerConstants.Species.valueOf(species_mb.getName());
                            FlowerConstants.Color color = FlowerConstants.Color.valueOf(color_str);
                            FlowerConstants.Origin origin = FlowerConstants.Origin.valueOf(origin_str);
                            Genotype genes = new Genotype(Integer.parseInt(encoding_str));
                            FlowerGenotype flowerGenes = new FlowerGenotype(species, genes);

                            FlowerDatabase.this.speciesMap.put(flowerGenes, species);
                            FlowerDatabase.this.colorMap.put(flowerGenes, color);
                            FlowerDatabase.this.originMap.put(flowerGenes, origin);
                        }
                    }

                    // finally, notify that we are loaded and ready to go
                    FlowerDatabase.this.dbLock.lock();
                    try {
                        FlowerDatabase.this.dataLoaded = true;
                        FlowerDatabase.this.loadedCond.notifyAll();
                    } finally {
                        FlowerDatabase.this.dbLock.unlock();
                    }
                } catch (IOException e) {
                    return; // todo crash
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

    private void submit(Query q) {
        this.execServ.submit(new QueryRunnable(q));
    }

    public Query makeQuery() {
        return new Query();
    }

    public class Flower implements Comparable<Flower> {
        public final FlowerConstants.Color color;
        public final String icon_name;
        public final FlowerConstants.Origin origin;
        final FlowerConstants.Species species;
        final FlowerGenotype genes;

        Flower(FlowerGenotype genes) {
            this.species = genes.species;
            this.color = FlowerDatabase.this.colorMap.get(genes);
            this.origin = FlowerDatabase.this.originMap.get(genes);
            this.genes = genes;
            this.icon_name = String.format("%s_%s",
                    this.species.name().toLowerCase(),
                    this.color.name().toLowerCase());
        }

        public String humanReadableGenotype() {
            return this.genes.genes.human_readable;
        }

        @Override
        public int hashCode() {
            return this.genes.hashCode();
        }


        @Override
        public int compareTo(Flower other) {
            return this.genes.compareTo(other.genes);
        }
    }

    private class QueryRunnable implements Runnable {
        private final Query query;

        QueryRunnable(Query q) {
            this.query = q;
        }

        @Override
        public void run() {
            // queries need to wait until database is loaded
            try {
                FlowerDatabase.this.waitUntilLoaded();
            } catch (InterruptedException e) {
                return;
            }

            Set<FlowerGenotype> species_results = new HashSet<FlowerGenotype>();
            Set<FlowerGenotype> color_results = new HashSet<FlowerGenotype>();
            Set<FlowerGenotype> origin_results = new HashSet<FlowerGenotype>();

            for (FlowerConstants.Species s : this.query.species)
                species_results.addAll(FlowerDatabase.this.speciesMap.getKeysForValue(s));

            for (FlowerConstants.Color c : this.query.colors)
                color_results.addAll(FlowerDatabase.this.colorMap.getKeysForValue(c));

            for (FlowerConstants.Origin o : this.query.origins)
                origin_results.addAll(FlowerDatabase.this.originMap.getKeysForValue(o));


            species_results.retainAll(color_results);
            species_results.retainAll(origin_results);

            List<Flower> results = new ArrayList<Flower>(species_results.size());
            for (FlowerGenotype genes : species_results)
                results.add(new Flower(genes));

            for (Callback<Collection<Flower>, Void> f : this.query.callbacks)
                f.apply(results);
        }
    }

    public class Query {
        final Collection<FlowerConstants.Species> species;
        final Collection<FlowerConstants.Color> colors;
        final Collection<FlowerConstants.Origin> origins;
        final Collection<Callback<Collection<Flower>, Void>> callbacks;

        private Query() {
            this.species = new LinkedList<FlowerConstants.Species>();
            this.colors = new LinkedList<FlowerConstants.Color>();
            this.origins = new LinkedList<FlowerConstants.Origin>();
            this.callbacks = new LinkedList<Callback<Collection<Flower>, Void>>();
        }

        public void submit() {
            if (this.species.size() == 0)
                this.species.addAll(Arrays.asList(FlowerConstants.Species.values()));
            if (this.colors.size() == 0)
                this.colors.addAll(Arrays.asList(FlowerConstants.Color.values()));
            if (this.origins.size() == 0)
                this.origins.addAll(Arrays.asList(FlowerConstants.Origin.values()));

            FlowerDatabase.this.submit(this);
        }

        public Query add(FlowerConstants.Species s) {
            this.species.add(s);
            return this;
        }

        public Query add(FlowerConstants.Color c) {
            this.colors.add(c);
            return this;
        }

        public Query add(FlowerConstants.Origin o) {
            this.origins.add(o);
            return this;
        }

        public Query addCallback(Callback<Collection<Flower>, Void> c) {
            this.callbacks.add(c);
            return this;
        }
    }
}
