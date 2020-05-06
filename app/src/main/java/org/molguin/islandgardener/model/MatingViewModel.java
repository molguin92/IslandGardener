package org.molguin.islandgardener.model;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.molguin.islandgardener.flowers.FlowerCollection;
import org.molguin.islandgardener.flowers.FlowerConstants;
import org.molguin.islandgardener.flowers.FuzzyFlower;
import org.molguin.islandgardener.utils.Callback;

import java.util.SortedSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MatingViewModel extends ViewModel {
    final Callback<SortedSet<FuzzyFlower>, Void> onLoadCallback;
    final private FlowerCollection flowerCollection;
    final private FlowerConstants.Species species;
    final private FuzzyFlowerMatingDispatcher dispatcher;

    public MatingViewModel(final FlowerCollection flowerCollection,
                           final FlowerConstants.Species species,
                           final Callback<SortedSet<FuzzyFlower>, Void> onLoadCallback,
                           final Callback<SortedSet<FuzzyFlower>, Void> matingCallback) {
        this.flowerCollection = flowerCollection;
        this.species = species;
        this.onLoadCallback = onLoadCallback;
        this.dispatcher = new FuzzyFlowerMatingDispatcher(matingCallback);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        dispatcher.shutdown();
    }

    public void setMate1(FuzzyFlower mate) {
        this.dispatcher.setMate1(mate);
    }

    public void setMate2(FuzzyFlower mate) {
        this.dispatcher.setMate2(mate);
    }

    public void loadData(final boolean advancedMode) {
        // asynchronously load flowers
        final ExecutorService exec = Executors.newSingleThreadExecutor();
        exec.submit(new Runnable() {
            @Override
            public void run() {
                SortedSet<FuzzyFlower> spinnerFlowers = flowerCollection.getAllFuzzyFlowersForSpecies(species);
                if (advancedMode)
                    spinnerFlowers.addAll(flowerCollection.getAllFlowersForSpecies(species));
                onLoadCallback.apply(spinnerFlowers);
                exec.shutdown();
            }
        });
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final FlowerCollection db;
        private final FlowerConstants.Species species;
        private final Callback<SortedSet<FuzzyFlower>, Void> onLoadCallback;
        private final Callback<SortedSet<FuzzyFlower>, Void> matingCallback;

        public Factory(FlowerCollection db,
                       FlowerConstants.Species species,
                       Callback<SortedSet<FuzzyFlower>, Void> onLoadCallback,
                       Callback<SortedSet<FuzzyFlower>, Void> matingCallback) {
            this.db = db;
            this.species = species;
            this.onLoadCallback = onLoadCallback;
            this.matingCallback = matingCallback;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(MatingViewModel.class)) {
                return (T) new MatingViewModel(this.db, this.species, this.onLoadCallback, this.matingCallback);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }

    private class FuzzyFlowerMatingDispatcher {
        private final ExecutorService exec;
        private final Lock lock;
        private final Condition cond;
        private boolean matesChanged;
        private boolean running;

        private FuzzyFlower mate1;
        private FuzzyFlower mate2;

        FuzzyFlowerMatingDispatcher(final @NonNull Callback<SortedSet<FuzzyFlower>, Void> matingCallback) {
            this.lock = new ReentrantLock();
            this.cond = this.lock.newCondition();
            this.matesChanged = false;
            this.running = true;

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
                            matingCallback.apply(all_offspring);
                            matesChanged = false;

                        } catch (Exception ignored) {
                            //Log.e("Error", "Error mating flowers.", e);
                            return;
                        } finally {
                            lock.unlock();
                        }
                    }
                }
            });
        }

        void setMate1(FuzzyFlower mate) {
            lock.lock();
            try {
                mate1 = mate;
                matesChanged = true;
                cond.signalAll();
            } finally {
                lock.unlock();
            }
        }

        void setMate2(FuzzyFlower mate) {
            lock.lock();
            try {
                mate2 = mate;
                matesChanged = true;
                cond.signalAll();
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
