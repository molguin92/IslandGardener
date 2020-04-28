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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MainViewModel extends ViewModel {
    private final MutableLiveData<Boolean> dataLoaded = new MutableLiveData<Boolean>(false);
    private final ExecutorService exec;
    private final Map<String, MutableLiveData<Collection<Flower>>> flowerListLiveDatas;
    private FlowerDatabase flowerFactory = null;
    private Lock factoryLock;

    public MainViewModel() {
        super();
        this.exec = Executors.newCachedThreadPool();
        this.factoryLock = new ReentrantLock();
        this.flowerListLiveDatas = new ConcurrentHashMap<String, MutableLiveData<Collection<Flower>>>();
    }

    public LiveData<Boolean> dataLoadedLiveData() {
        return this.dataLoaded;
    }

    public LiveData<Collection<Flower>> getLiveData(String id) {
        MutableLiveData<Collection<Flower>> liveData = this.flowerListLiveDatas.get(id);
        if (liveData == null) {
            liveData = new MutableLiveData<Collection<Flower>>();
            this.flowerListLiveDatas.put(id, liveData);
        }
        return liveData;
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

    public void loadFlowersForSpeciesAsync(final FlowerConstants.Species species,
                                           final String resultLiveDataID) {
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
                    List<Flower> flowers = new ArrayList<Flower>(flowerFactory.getAllFlowersForSpecies(species));
                    Collections.sort(flowers);

                    MutableLiveData<Collection<Flower>> liveData = flowerListLiveDatas.get(resultLiveDataID);
                    if (liveData != null)
                        liveData.postValue(flowers);
                } catch (Exception e) {
                    Log.e("Exception", e.toString());
                }
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
