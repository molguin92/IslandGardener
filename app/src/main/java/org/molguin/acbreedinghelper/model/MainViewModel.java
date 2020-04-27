package org.molguin.acbreedinghelper.model;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.eclipsesource.json.Json;

import org.molguin.acbreedinghelper.R;
import org.molguin.flowers.Flower;
import org.molguin.flowers.FlowerConstants;
import org.molguin.flowers.FlowerDatabase;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MainViewModel extends ViewModel {
    private final MutableLiveData<Boolean> dataLoaded = new MutableLiveData<Boolean>(false);
    private final MutableLiveData<Collection<Flower>> flowerList =
            new MutableLiveData<Collection<Flower>>(new ArrayList<Flower>());
    private final String[] flower_species;
    private final ExecutorService exec;
    private FlowerDatabase flowerFactory = null;
    private Lock factoryLock;

    public MainViewModel() {
        super();
        this.exec = Executors.newCachedThreadPool();
        this.factoryLock = new ReentrantLock();

        FlowerConstants.Species[] species = FlowerConstants.Species.values();
        this.flower_species = new String[species.length];
        for (int i = 0; i < species.length; i++) {
            String name = species[i].name().toLowerCase();
            this.flower_species[i] = name.substring(0, 1).toUpperCase() + name.substring(1);
        }
    }

    public String[] getFlowerSpecies() {
        return this.flower_species;
    }

    public LiveData<Boolean> dataLoadedLiveData() {
        return this.dataLoaded;
    }

    public LiveData<Collection<Flower>> flowerListLiveData() {
        return this.flowerList;
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

    public void loadFlowersForSpeciesAsync(final FlowerConstants.Species species) {
        try {
            this.factoryLock.lock();
            if (this.flowerFactory == null) return;
        } finally {
            this.factoryLock.unlock();
        }

        this.exec.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    factoryLock.lock();
                    List<Flower> flowers =
                            new ArrayList<Flower>(flowerFactory.getAllFlowersForSpecies(species));
                    Collections.sort(flowers);
                    flowerList.postValue(flowers);
                    Log.w("Adapter", "Added " + flowers.size() + " flowers.");
                } finally {
                    factoryLock.unlock();
                }
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
