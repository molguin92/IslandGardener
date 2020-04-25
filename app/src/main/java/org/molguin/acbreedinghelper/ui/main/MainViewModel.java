package org.molguin.acbreedinghelper.ui.main;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import org.molguin.acbreedinghelper.R;
import org.molguin.flowers.Flower;
import org.molguin.flowers.FlowerConstants;
import org.molguin.flowers.FlowerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MainViewModel extends ViewModel {
    private FlowerFactory flowerFactory = null;
    private Lock factoryLock;
    private final MutableLiveData<Boolean> dataAvailable = new MutableLiveData<Boolean>(false);
    private final FlowerAdapter flowerAdapter;

    public MainViewModel() {
        super();
        this.factoryLock = new ReentrantLock();
        this.flowerAdapter = new FlowerAdapter();
    }

    public LiveData<Boolean> dataAvailable() {
        return this.dataAvailable;
    }
    public FlowerAdapter getFlowerAdapter() { return this.flowerAdapter; }

    public void loadDataAsync(final Context appContext) throws IOException {
        try {
            this.factoryLock.lock();
            if (this.flowerFactory != null) return;
        } finally {
            this.factoryLock.unlock();
        }

        final AssetManager am = appContext.getAssets();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Reader reader = new InputStreamReader(am.open(appContext.getString(R.string.flower_json)));
                    FlowerFactory fact = new FlowerFactory(Json.parse(reader).asObject());
                    try {
                        factoryLock.lock();
                        flowerFactory = fact;
                        Log.i("loadDataAsync", "Data loaded in background.");
                        dataAvailable.postValue(true);
                    } finally {
                        factoryLock.unlock();
                    }
                } catch (IOException e) {
                    Log.e("loadDataAsync", e.toString());
                }
            }
        }).start();
    }

    public void loadFlowerListAsync() {
        try {
            this.factoryLock.lock();
            if (this.flowerFactory == null) return;
        } finally {
            this.factoryLock.unlock();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    factoryLock.lock();
                    List<Flower> flowers = flowerFactory.getAllFlowersForSpecies(FlowerConstants.Species.LILY);
                    flowerAdapter.addFlowers(flowers);
                    Log.w("Adapter", "Added " + flowers.size() + " flowers.");
                } finally {
                    factoryLock.unlock();
                }
            }
        }).start();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
