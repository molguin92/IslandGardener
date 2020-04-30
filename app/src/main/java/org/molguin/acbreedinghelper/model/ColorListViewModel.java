package org.molguin.acbreedinghelper.model;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedListAdapterCallback;

import org.molguin.acbreedinghelper.flowers.FlowerConstants;
import org.molguin.acbreedinghelper.flowers.FlowerDatabase;
import org.molguin.acbreedinghelper.utils.Callback;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ColorListViewModel extends ViewModel {
    final FlowerDatabase flowerDB;
    final FlowerConstants.Species species;

    public ColorListViewModel(final FlowerDatabase db,
                              final FlowerConstants.Species species) {
        this.flowerDB = db;
        this.species = species;
    }

    public void loadData(final Callback<Collection<FlowerColorCollection>, Void> callback) {
        // execute the actual query here
        // asynchronously load flowers
        this.flowerDB.makeQuery()
                .add(species)
                .addCallback(new Callback<Collection<FlowerDatabase.Flower>, Void>() {
                    @Override
                    public Void apply(Collection<FlowerDatabase.Flower> flowers) {
                        final Map<FlowerConstants.Color, FlowerColorCollection> colors =
                                new HashMap<FlowerConstants.Color, FlowerColorCollection>();

                        for (FlowerDatabase.Flower flower : flowers) {
                            FlowerColorCollection colorCollection = colors.get(flower.color);
                            if (colorCollection == null) {
                                colorCollection = new FlowerColorCollection(species, flower.color);
                                colors.put(flower.color, colorCollection);
                            }
                            colorCollection.flowers.add(flower);
                        }
                        callback.apply(colors.values());
                        return null;
                    }
                })
                .submit();

    }

    public static class FlowerColorCollection {
        public final FlowerConstants.Species species;
        public final FlowerConstants.Color color;
        public final Set<FlowerDatabase.Flower> flowers;
        public final String icon_name;

        FlowerColorCollection(FlowerConstants.Species species, FlowerConstants.Color color) {
            this.species = species;
            this.color = color;
            this.flowers = new HashSet<FlowerDatabase.Flower>();
            this.icon_name = String.format("%s_%s",
                    species.name().toLowerCase(),
                    color.name().toLowerCase());
        }
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final FlowerDatabase db;
        private final FlowerConstants.Species species;

        public Factory(FlowerDatabase db,
                       FlowerConstants.Species species) {
            this.db = db;
            this.species = species;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ColorListViewModel.class)) {
                return (T) new ColorListViewModel(this.db, this.species);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }

    public static class SortedListColorCollectionCallback extends SortedListAdapterCallback<FlowerColorCollection> {
        public SortedListColorCollectionCallback(RecyclerView.Adapter adapter) {
            super(adapter);
        }

        @Override
        public int compare(FlowerColorCollection o1,
                           FlowerColorCollection o2) {
            return o1.color.compareTo(o2.color);
        }

        @Override
        public boolean areContentsTheSame(FlowerColorCollection oldItem,
                                          FlowerColorCollection newItem) {
            return this.areItemsTheSame(oldItem, newItem) &&
                    (oldItem.flowers.equals(newItem.flowers));
        }

        @Override
        public boolean areItemsTheSame(FlowerColorCollection item1,
                                       FlowerColorCollection item2) {
            return item1.color == item2.color;
        }
    }
}
