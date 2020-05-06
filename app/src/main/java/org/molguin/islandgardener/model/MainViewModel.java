package org.molguin.islandgardener.model;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.molguin.islandgardener.flowers.FlowerCollection;
import org.molguin.islandgardener.utils.Callback;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MainViewModel extends ViewModel {
    private final Lock lock;
    private final Condition loaded_cond;
    private FlowerCollection flowerCollection;
    private boolean advancedMode;

    private MainViewModel(final AssetManager am,
                          final Context appContext,
                          final Runnable onLoadCallback) {
        super();
        this.advancedMode = false;
        this.lock = new ReentrantLock();
        this.loaded_cond = this.lock.newCondition();
        this.flowerCollection = null;

        final ExecutorService exec = Executors.newSingleThreadExecutor();
        exec.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    MainViewModel.this.setFlowerCollection(new FlowerCollection(am, appContext));
                    onLoadCallback.run();
                } catch (IOException e) {
                    Log.e("Loading", "Failed to load flower data.");
                }
            }
        });
    }


    public boolean isAdvancedMode() {
        return this.advancedMode;
    }

    public void setAdvancedMode(boolean on) {
        this.advancedMode = on;
    }

    public boolean toggleAdvancedMode() {
        this.advancedMode = !this.advancedMode;
        return advancedMode;
    }

    public FlowerCollection getFlowerCollection() {
        this.lock.lock();
        try {
            while (this.flowerCollection == null) {
                try {
                    this.loaded_cond.await();
                } catch (InterruptedException e) {
                    Log.w("MainViewModel", "Interrupted while trying to get FlowerCollection", e);
                }
            }
            return this.flowerCollection;
        } finally {
            this.lock.unlock();
        }
    }

    private void setFlowerCollection(FlowerCollection collection) {
        this.lock.lock();
        try {
            this.flowerCollection = collection;
            this.loaded_cond.signalAll();
        } finally {
            this.lock.unlock();
        }
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final AssetManager am;
        private final Context appContext;
        private final Runnable onLoadCallback;

        public Factory(final AssetManager am, final Context appContext, final Runnable onLoadCallback) {
            this.am = am;
            this.appContext = appContext;
            this.onLoadCallback = onLoadCallback;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(MainViewModel.class)) {
                return (T) new MainViewModel(this.am, this.appContext, this.onLoadCallback);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }

}
