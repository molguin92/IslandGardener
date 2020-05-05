package org.molguin.islandgardener.model;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.molguin.islandgardener.flowers.FlowerCollection;
import org.molguin.islandgardener.flowers.FlowerConstants;
import org.molguin.islandgardener.flowers.FuzzyFlower;
import org.molguin.islandgardener.utils.Callback;

import java.util.Set;
import java.util.SortedSet;
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
        this.dispatcher = new FuzzyFlowerMatingDispatcher(new Callback<Set<FuzzyFlower>, Void>() {
            @Override
            public Void apply(Set<FuzzyFlower> fuzzyFlowers) {
                return null;
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        dispatcher.shutdown();
    }

    public void loadData(final Callback<SortedSet<FuzzyFlower>, Void> callback) {
        // asynchronously load flowers
        final ExecutorService exec = Executors.newSingleThreadExecutor();
        exec.submit(new Runnable() {
            @Override
            public void run() {
                SortedSet<FuzzyFlower> spinnerFlowers = flowerCollection.getAllFuzzyFlowersForSpecies(species);
                spinnerFlowers.addAll(flowerCollection.getAllFlowersForSpecies(species));
                callback.apply(spinnerFlowers);
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
        private Callback<Set<FuzzyFlower>, Void> callback;

        private FuzzyFlower mate1;
        private FuzzyFlower mate2;

        FuzzyFlowerMatingDispatcher(final Callback<Set<FuzzyFlower>, Void> matingCallback) {
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
                            while (true) {
                                if (!running)
                                    return;
                                else if (!matesChanged || mate1 == null || mate2 == null)
                                    cond.await();
                                else break;
                            }

                            SortedSet<FuzzyFlower> all_offspring = flowerCollection.getAllOffspring(mate1, mate2);
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

        public void setCallback(Callback<Set<FuzzyFlower>, Void> callback) {
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
