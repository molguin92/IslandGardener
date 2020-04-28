package org.molguin.acbreedinghelper.model;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.eclipsesource.json.Json;

import org.molguin.acbreedinghelper.R;
import org.molguin.flowers.FlowerDatabase;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MainViewModel extends ViewModel {
    private final MutableLiveData<Boolean> dataLoaded = new MutableLiveData<Boolean>(false);
    private final ExecutorService exec;
    private FlowerDatabase flowerFactory = null;
    private Lock factoryLock;

    public MainViewModel() {
        super();
        this.exec = Executors.newCachedThreadPool();
        this.factoryLock = new ReentrantLock();
    }

    public LiveData<Boolean> dataLoadedLiveData() {
        return this.dataLoaded;
    }


    public void loadDataAsync(final Context appContext) {
        try {
            this.factoryLock.lock();
            if (this.flowerFactory != null) return;
        } finally {
            this.factoryLock.unlock();
        }

        final AssetManager am = appContext.getAssets();
        this.exec.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Reader reader = new InputStreamReader(am.open(appContext.getString(R.string.flower_json)));
                    FlowerDatabase fact = new FlowerDatabase(Json.parse(reader).asObject());
                    try {
                        factoryLock.lock();
                        flowerFactory = fact;
                        Log.i("loadDataAsync", "Data loaded in background.");
                        dataLoaded.postValue(true);
                    } finally {
                        factoryLock.unlock();
                    }
                } catch (Exception e) {
                    Log.e("loadDataAsync", e.toString());
                }
            }
        });
    }

    public FlowerDatabase getFlowerDB() {
        try {
            this.factoryLock.lock();
            if (this.flowerFactory != null) return flowerFactory;
            else throw new RuntimeException(); // todo: descriptive.
        } finally {
            this.factoryLock.unlock();
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        try {
            this.factoryLock.lock();
            if (this.flowerFactory != null) flowerFactory.shutdown();
        } catch (InterruptedException ignored) {
        } finally {
            this.factoryLock.unlock();
        }
    }
}
