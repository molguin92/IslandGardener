package org.molguin.islandgardener.model;

import android.content.Context;
import android.content.res.AssetManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.molguin.islandgardener.flowers.FlowerCollection;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MainViewModel extends ViewModel {
    private final Lock lock;
    private final Condition loaded_cond;
    private final MutableLiveData<Boolean> advancedMode;
    private FlowerCollection flowerCollection;

    private MainViewModel(final AssetManager am,
                          final Context appContext,
                          final Runnable onLoadCallback) {
        super();
        this.advancedMode = new MutableLiveData<Boolean>(false);
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
                } catch (IOException ignored) {
                    //Log.e("Loading", "Failed to load flower data.");
                }
            }
        });
    }


    public boolean isAdvancedMode() {
        return this.advancedMode.getValue();
    }

    public void setAdvancedMode(boolean on) {
        this.advancedMode.setValue(on);
    }

    public boolean toggleAdvancedMode() {
        boolean newmode = !this.advancedMode.getValue();
        this.advancedMode.setValue(newmode); // setValue sets value immediately, as opposed to postValue
        return newmode;
    }

    public void observeMode(LifecycleOwner lifecycleOwner, Observer<Boolean> observer) {
        this.advancedMode.observe(lifecycleOwner, observer);
    }


    public FlowerCollection getFlowerCollection() {
        this.lock.lock();
        try {
            while (this.flowerCollection == null) {
                try {
                    this.loaded_cond.await();
                } catch (InterruptedException ignored) {
                    //Log.w("MainViewModel", "Interrupted while trying to get FlowerCollection", e);
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
