package org.molguin.acbreedinghelper;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import org.molguin.flowers.FlowerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class FlowerBreedingApplication extends Application {
    private FlowerFactory flowerFactory = null;

    public FlowerFactory getFlowerFactory() throws IOException {
        if (this.flowerFactory == null) {
            // instantiate
            AssetManager am = this.getAssets();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(am.open("data/flower_data.json")));
            JsonObject json = Json.parse(reader).asObject();
            this.flowerFactory = new FlowerFactory(json);
        }
        return this.flowerFactory;
    }

    // Called when the application is starting, before any other application objects have been created.
    // Overriding this method is totally optional!
    @Override
    public void onCreate() {
        super.onCreate();
        // Required initialization logic here!
    }

    // Called by the system when the device configuration changes while your component is running.
    // Overriding this method is totally optional!
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    // This is called when the overall system is running low on memory,
    // and would like actively running processes to tighten their belts.
    // Overriding this method is totally optional!
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}
