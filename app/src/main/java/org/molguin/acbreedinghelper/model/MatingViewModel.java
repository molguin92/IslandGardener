package org.molguin.acbreedinghelper.model;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedListAdapterCallback;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import org.molguin.acbreedinghelper.flowers.Flower;
import org.molguin.acbreedinghelper.flowers.FlowerCollection;
import org.molguin.acbreedinghelper.flowers.FlowerConstants;
import org.molguin.acbreedinghelper.utils.Callback;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MatingViewModel extends ViewModel {
    final FlowerCollection flowerCollection;
    final FlowerConstants.Species species;

    public MatingViewModel(final FlowerCollection flowerCollection,
                           final FlowerConstants.Species species) {
        this.flowerCollection = flowerCollection;
        this.species = species;
    }

    public void loadData(final Callback<Set<FuzzyFlower>, Void> callback) {
        // asynchronously load flowers
        this.flowerCollection.applyToSpecies(this.species, new Callback<Set<Flower>, Void>() {
            @Override
            public Void apply(Set<Flower> flowers) {
                Multimap<FlowerConstants.Color, Flower> colors = HashMultimap.create();

                for (Flower f : flowers)
                    colors.put(f.color, f);

                Set<FuzzyFlower> fuzzyFlowers = new HashSet<FuzzyFlower>(colors.keySet().size());
                for (Map.Entry<FlowerConstants.Color, Collection<Flower>> e : colors.asMap().entrySet()) {
                    Set<Flower> variants = new HashSet<Flower>(e.getValue());
                    fuzzyFlowers.add(new FuzzyFlower(MatingViewModel.this.species,
                            e.getKey(), variants));
                }

                return callback.apply(fuzzyFlowers);
            }
        });
    }

    private static class FuzzyFlowerMatingDispatcher {
        private final ExecutorService exec;
        private final Lock lock;
        private final Condition cond;
        private boolean matesChanged;

        private FuzzyFlower mate1;
        private FuzzyFlower mate2;

        FuzzyFlowerMatingDispatcher() {
            this.lock = new ReentrantLock();
            this.cond = this.lock.newCondition();
            this.matesChanged = false;

            this.exec = Executors.newSingleThreadExecutor();
            this.mate1 = null;
            this.mate2 = null;

            this.exec.submit(new Runnable() {
                @Override
                public void run() {
                    for(;;){
                        lock.lock();
                        try {
                            while (!matesChanged && !(mate1 != null && mate2 != null) )
                                cond.await();



                        } catch (InterruptedException e) {
                            return;
                        } finally {
                            lock.unlock();
                        }
                    }
                }
            });
        }


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

    public static class SortedListColorCollectionCallback extends SortedListAdapterCallback<FuzzyFlower> {
        public SortedListColorCollectionCallback(RecyclerView.Adapter adapter) {
            super(adapter);
        }

        @Override
        public int compare(FuzzyFlower o1, FuzzyFlower o2) {
            return o1.compareTo(o2);
        }

        @Override
        public boolean areContentsTheSame(FuzzyFlower oldItem, FuzzyFlower newItem) {
            return (this.areItemsTheSame(oldItem, newItem)) &&
                    (oldItem.variants == newItem.variants);
        }

        @Override
        public boolean areItemsTheSame(FuzzyFlower item1, FuzzyFlower item2) {
            return item1.hashCode() == item2.hashCode();
        }
    }
}
