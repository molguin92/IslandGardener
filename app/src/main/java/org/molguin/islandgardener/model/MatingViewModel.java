/*
 *     Copyright © 2020 Manuel Olguín Muñoz <manuel@olguin.se>
 *
 *     MatingViewModel.java is part of Island Gardener
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

package org.molguin.islandgardener.model;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.molguin.islandgardener.flowers.FlowerCollection;
import org.molguin.islandgardener.flowers.FlowerColorGroup;
import org.molguin.islandgardener.flowers.FlowerConstants;
import org.molguin.islandgardener.flowers.FuzzyFlower;
import org.molguin.islandgardener.flowers.SpecificFlower;

import java.util.Collection;
import java.util.SortedSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MatingViewModel extends ViewModel {
    final private FlowerCollection flowerCollection;
    final private FlowerConstants.Species species;
    final private FuzzyFlowerMatingDispatcher dispatcher;

    public MatingViewModel(final FlowerCollection flowerCollection,
                           final FlowerConstants.Species species,
                           final matesCalculatedCallback matingCallback) {
        this.flowerCollection = flowerCollection;
        this.species = species;
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

    public void loadData(final dataLoadedCallback callback) {
        // asynchronously load flowers
        final ExecutorService exec = Executors.newSingleThreadExecutor();
        exec.submit(new Runnable() {
            @Override
            public void run() {
                SortedSet<FlowerColorGroup> spinnerGroups = flowerCollection.getAllColorGroupsForSpecies(species);
                SortedSet<SpecificFlower> spinnerFlowers = flowerCollection.getAllFlowersForSpecies(species);
                callback.onDataLoaded(spinnerGroups, spinnerFlowers);
                exec.shutdown();
            }
        });
    }

    public interface dataLoadedCallback {
        void onDataLoaded(Collection<FlowerColorGroup> groups, Collection<SpecificFlower> flowers);
    }

    public interface matesCalculatedCallback {
        void onMatesCalculated(Collection<FuzzyFlower> flowers);
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final FlowerCollection db;
        private final FlowerConstants.Species species;
        private final matesCalculatedCallback matingCallback;

        public Factory(FlowerCollection db,
                       FlowerConstants.Species species,
                       matesCalculatedCallback matesCalculatedCallback) {
            this.db = db;
            this.species = species;
            this.matingCallback = matesCalculatedCallback;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(MatingViewModel.class)) {
                return (T) new MatingViewModel(this.db, this.species, this.matingCallback);
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

        FuzzyFlowerMatingDispatcher(final @NonNull matesCalculatedCallback matingCallback) {
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
                            matingCallback.onMatesCalculated(all_offspring);
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
