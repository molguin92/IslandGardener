package org.molguin.acbreedinghelper.model;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import org.molguin.acbreedinghelper.flowers.Flower;
import org.molguin.acbreedinghelper.flowers.FlowerCollection;
import org.molguin.acbreedinghelper.flowers.FlowerConstants;
import org.molguin.acbreedinghelper.utils.Callback;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MatingViewModel extends ViewModel {
    final public FlowerCollection flowerCollection;
    final public FlowerConstants.Species species;
    final public FuzzyFlowerMatingDispatcher dispatcher;

    public MatingViewModel(final FlowerCollection flowerCollection,
                           final FlowerConstants.Species species) {
        this.flowerCollection = flowerCollection;
        this.species = species;
        this.dispatcher = new FuzzyFlowerMatingDispatcher(new Callback<Map<Flower, Double>, Void>() {
            @Override
            public Void apply(Map<Flower, Double> flowerDoubleMap) {
                return null;
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        dispatcher.shutdown();
    }

    public void loadData(final Callback<Set<FuzzyFlower>, Void> callback) {
        // asynchronously load flowers
        final ExecutorService exec = Executors.newSingleThreadExecutor();
        exec.submit(new Runnable() {
            @Override
            public void run() {
                Set<Flower> flowers = flowerCollection.getAllFlowersForSpecies(species);
                Multimap<FlowerConstants.Color, Flower> colors = HashMultimap.create();

                for (Flower f : flowers)
                    colors.put(f.color, f);

                Set<FuzzyFlower> fuzzyFlowers = new HashSet<FuzzyFlower>(colors.keySet().size());
                for (Map.Entry<FlowerConstants.Color, Collection<Flower>> e : colors.asMap().entrySet()) {
                    Set<Flower> variants = new HashSet<Flower>(e.getValue());
                    fuzzyFlowers.add(new FuzzyFlower(MatingViewModel.this.species,
                            e.getKey(), variants));
                }

                callback.apply(fuzzyFlowers);
                exec.shutdownNow();
            }
        });
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final FlowerCollection db;
        private final FlowerConstants.Species species;

        public Factory(FlowerCollection db,
                       FlowerConstants.Species species) {
            this.db = db;
            this.species = species;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(MatingViewModel.class)) {
                return (T) new MatingViewModel(this.db, this.species);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }

    public class FuzzyFlowerMatingDispatcher {
        private final ExecutorService exec;
        private final Lock lock;
        private final Condition cond;
        private boolean matesChanged;
        private boolean running;
        @NonNull
        private Callback<Map<Flower, Double>, Void> callback;

        private FuzzyFlower mate1;
        private FuzzyFlower mate2;

        FuzzyFlowerMatingDispatcher(final Callback<Map<Flower, Double>, Void> matingCallback) {
            this.lock = new ReentrantLock();
            this.cond = this.lock.newCondition();
            this.matesChanged = false;
            this.running = true;
            this.callback = matingCallback;

            this.exec = Executors.newSingleThreadExecutor();
            this.mate1 = null;
            this.mate2 = null;

            this.exec.submit(new Runnable() {
                @Override
                public void run() {
                    for (; ; ) {
                        lock.lock();
                        try {
                            while (true)
                                if (!running)
                                    return;
                                else if (!matesChanged || mate1 == null || mate2 == null)
                                    cond.await();
                                else break;

                            Map<Flower, Double> all_offspring = new HashMap<Flower, Double>();
                            double mating_count = mate1.variants.size() * mate2.variants.size();

                            for (Flower parent1 : mate1.variants) {
                                for (Flower parent2 : mate2.variants) {
                                    Map<Flower, Double> offspring = flowerCollection.getAllOffspring(parent1, parent2);
                                    for (Map.Entry<Flower, Double> kid : offspring.entrySet()) {
                                        Double old_value = all_offspring.get(kid.getKey());
                                        if (old_value == null)
                                            old_value = 0.0;

                                        all_offspring.put(kid.getKey(), (kid.getValue() / mating_count) + old_value);
                                    }
                                }
                            }

                            callback.apply(all_offspring);
                            matesChanged = false;

                        } catch (Exception e) {
                            Log.e("Error", "Error mating flowers.", e);
                            return;
                        } finally {
                            lock.unlock();
                        }
                    }
                }
            });
        }

        public void setMate1(FuzzyFlower mate) {
            lock.lock();
            try {
                mate1 = mate;
                matesChanged = true;
                cond.signalAll();
            } finally {
                lock.unlock();
            }
        }

        public void setMate2(FuzzyFlower mate) {
            lock.lock();
            try {
                mate2 = mate;
                matesChanged = true;
                cond.signalAll();
            } finally {
                lock.unlock();
            }
        }

        public void setCallback(Callback<Map<Flower, Double>, Void> callback) {
            lock.lock();
            try {
                this.callback = callback;
            } finally {
                lock.unlock();
            }
        }

        void shutdown() {
            lock.lock();
            try {
                this.running = false;
                this.cond.signalAll();
            } finally {
                lock.unlock();
            }
            this.exec.shutdown();
        }

    }
}
