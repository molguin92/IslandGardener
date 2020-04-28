package org.molguin.flowers;

import com.eclipsesource.json.JsonObject;

import org.molguin.Callback;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FlowerDatabase {
    private final ConcurrentMap<FlowerConstants.Species, Set<Flower>> speciesMap;
    private final ConcurrentMap<FlowerConstants.Origin, Set<Flower>> originMap;
    private final ConcurrentMap<FlowerConstants.Color, Set<Flower>> colorMap;
    private final ExecutorService execServ;
    private final Dispatcher dispatcher;

    public FlowerDatabase(JsonObject flower_json) {
        this.execServ = Executors.newFixedThreadPool(3);
        this.dispatcher = new Dispatcher();
        this.execServ.submit(this.dispatcher);

        // on instantiation, read the JSON data and set up internal map of flowers
        this.speciesMap = new ConcurrentHashMap<>();
        this.originMap = new ConcurrentHashMap<>();
        this.colorMap = new ConcurrentHashMap<>();

        for (FlowerConstants.Species species : FlowerConstants.Species.values())
            // prepare mapping species -> set of flowers
            this.speciesMap.put(species, new ConcurrentSkipListSet<>());

        for (FlowerConstants.Origin origin : FlowerConstants.Origin.values())
            // prepare mapping origin -> set of flowers
            this.originMap.put(origin, new ConcurrentSkipListSet<>());

        for (FlowerConstants.Color color : FlowerConstants.Color.values())
            // prepare mapping color -> set of flowers
            this.colorMap.put(color, new ConcurrentSkipListSet<>());


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
                        new FlowerGenotype(Integer.parseInt(encoding)));

                Set<Flower> flowers_in_species = this.speciesMap.get(flower.species);
                Set<Flower> flowers_for_origin = this.originMap.get(flower.origin);
                Set<Flower> flowers_with_color = this.colorMap.get(flower.color);

                flowers_in_species.add(flower);
                flowers_for_origin.add(flower);
                flowers_with_color.add(flower);
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
            Set<Flower> species_results = new HashSet<>();
            Set<Flower> color_results = new HashSet<>();
            Set<Flower> origin_results = new HashSet<>();

            for (FlowerConstants.Species s : this.query.species)
                species_results.addAll(FlowerDatabase.this.speciesMap.get(s));

            for (FlowerConstants.Color c : this.query.colors)
                color_results.addAll(FlowerDatabase.this.colorMap.get(c));

            for (FlowerConstants.Origin o : this.query.origins)
                origin_results.addAll(FlowerDatabase.this.originMap.get(o));

            species_results.retainAll(color_results);
            species_results.retainAll(origin_results);

            for (Callback<Collection<Flower>, Void> f : this.query.callbacks)
                f.apply(species_results);
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
