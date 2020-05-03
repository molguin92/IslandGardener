package org.molguin.acbreedinghelper.model;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.molguin.acbreedinghelper.flowers.FlowerCollection;

import java.io.IOException;

public class MainActivityViewModel extends ViewModel {
    private final FlowerCollection flowerCollection;

    public MainActivityViewModel(FlowerCollection collection) {
        super();
        this.flowerCollection = collection;
    }

    public FlowerCollection getFlowerCollection() {
        return this.flowerCollection;
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final FlowerCollection collection;

        public Factory(FlowerCollection collection) {
            this.collection = collection;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(MainActivityViewModel.class)) {
                return (T) new MainActivityViewModel(collection);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
