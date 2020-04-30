package org.molguin.acbreedinghelper.model;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.molguin.acbreedinghelper.flowers.FlowerDatabase;

public class MainActivityViewModel extends ViewModel {
    private final FlowerDatabase flowerDB;

    public MainActivityViewModel(final AssetManager am, final Context appContext) {
        super();
        this.flowerDB = new FlowerDatabase(am, appContext);
    }

    public FlowerDatabase getFlowerDB() {
        return this.flowerDB;
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final Context appContext;
        private final AssetManager am;

        public Factory(Application app) {
            this.appContext = app.getApplicationContext();
            this.am = app.getResources().getAssets();
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(MainActivityViewModel.class)) {
                return (T) new MainActivityViewModel(this.am, this.appContext);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
