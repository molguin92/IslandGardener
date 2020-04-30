package org.molguin.flowers;

import com.eclipsesource.json.JsonObject;

import org.molguin.utils.Callback;
import org.molguin.utils.ReversibleMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FlowerDatabase {
    private final ReversibleMap<FlowerGenotype, FlowerConstants.Color> colorMap;
    private final ReversibleMap<FlowerGenotype, FlowerConstants.Species> speciesMap;
    private final ReversibleMap<FlowerGenotype, FlowerConstants.Origin> originMap;
    private final ExecutorService execServ;
    private final Dispatcher dispatcher;

    public FlowerDatabase(JsonObject flower_json) {
        this.execServ = Executors.newFixedThreadPool(FlowerConstants.Species.values().length / 2);
        this.dispatcher = new Dispatcher();
        this.execServ.submit(this.dispatcher);

        // on instantiation, read the JSON data and set up internal map of flowers
        this.speciesMap = new ReversibleMap<>();
        this.originMap = new ReversibleMap<>();
        this.colorMap = new ReversibleMap<>();


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

                this.speciesMap.put(flowerGenes, species);
                this.colorMap.put(flowerGenes, color);
                this.originMap.put(flowerGenes, origin);
            }
        }
    }

    public void shutdown() throws InterruptedException {
        this.dispatcher.shutdown();
        this.execServ.awaitTermination(1, TimeUnit.SECONDS);
        this.execServ.shutdown();
    }

    public void submit(Query q) {
        this.dispatcher.submit(q);
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

    private class Dispatcher implements Runnable {
        private final Lock runLock;
        private final Queue<Query> queryQueue;
        private boolean running;

        Dispatcher() {
            this.running = false;
            this.runLock = new ReentrantLock();
            this.queryQueue = new LinkedBlockingQueue<>();
        }

        void submit(Query q) {
            this.queryQueue.add(q);
        }

        @Override
        public void run() {
            try {
                this.runLock.lock();
                this.running = true;
            } finally {
                this.runLock.unlock();
            }

            while (this.isRunning()) {
                try {
                    Query q = this.queryQueue.remove();
                    FlowerDatabase.this.execServ.submit(new QueryRunnable(q));
                } catch (NoSuchElementException e) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        continue;
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        }

        private boolean isRunning() {
            try {
                this.runLock.lock();
                return this.running;
            } finally {
                this.runLock.unlock();
            }
        }

        private void shutdown() {
            try {
                this.runLock.lock();
                this.running = false;
            } finally {
                this.runLock.unlock();
            }
        }
    }

    private class QueryRunnable implements Runnable {
        private final Query query;

        QueryRunnable(Query q) {
            this.query = q;
        }

        @Override
        public void run() {
            Set<FlowerGenotype> species_results = new HashSet<>();
            Set<FlowerGenotype> color_results = new HashSet<>();
            Set<FlowerGenotype> origin_results = new HashSet<>();

            for (FlowerConstants.Species s : this.query.species)
                species_results.addAll(FlowerDatabase.this.speciesMap.getKeysForValue(s));

            for (FlowerConstants.Color c : this.query.colors)
                color_results.addAll(FlowerDatabase.this.colorMap.getKeysForValue(c));

            for (FlowerConstants.Origin o : this.query.origins)
                origin_results.addAll(FlowerDatabase.this.originMap.getKeysForValue(o));


            species_results.retainAll(color_results);
            species_results.retainAll(origin_results);

            List<Flower> results = new ArrayList<>(species_results.size());
            for (FlowerGenotype genes : species_results)
                results.add(new Flower(genes));

            for (Callback<Collection<Flower>, Void> f : this.query.callbacks)
                f.apply(results);
        }
    }

    public class Query {
        protected final Collection<FlowerConstants.Species> species;
        protected final Collection<FlowerConstants.Color> colors;
        protected final Collection<FlowerConstants.Origin> origins;
        protected final Collection<Callback<Collection<Flower>, Void>> callbacks;

        private Query() {
            this.species = new LinkedList<>();
            this.colors = new LinkedList<>();
            this.origins = new LinkedList<>();
            this.callbacks = new LinkedList<>();
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
