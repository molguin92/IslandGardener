package org.molguin.acbreedinghelper.model;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.molguin.acbreedinghelper.flowers.FlowerCollection;
import org.molguin.acbreedinghelper.utils.Callback;

public class MainActivityViewModel extends ViewModel {
    private final FlowerCollection flowerCollection;

    public MainActivityViewModel(final AssetManager am,
                                 final Context appContext,
                                 final Callback<Void, Void> finishedLoadingCallback) {
        super();
        this.flowerCollection = new FlowerCollection(am, appContext, finishedLoadingCallback);
    }

    public FlowerCollection getFlowerCollection() {
        return this.flowerCollection;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        try {
            this.flowerCollection.shutdown();
        } catch (InterruptedException e) {
            Log.w("MainActivityViewModel", "Error while shutting down flower collection");
            Log.e("MainActivityViewModel", e.toString());
        }
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final Context appContext;
        private final AssetManager am;
        private final Callback<Void, Void> finishedLoadingCallback;

        public Factory(Application app, Callback<Void, Void> finishedLoadingCallback) {
            this.appContext = app.getApplicationContext();
            this.am = app.getResources().getAssets();
            this.finishedLoadingCallback = finishedLoadingCallback;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(MainActivityViewModel.class)) {
                return (T) new MainActivityViewModel(this.am, this.appContext, this.finishedLoadingCallback);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
