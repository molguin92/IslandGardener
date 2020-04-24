package org.molguin.acbreedinghelper.ui.main;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.ViewModel;

import org.molguin.acbreedinghelper.FlowerBreedingApplication;
import org.molguin.flowers.Flower;
import org.molguin.flowers.FlowerConstants;
import org.molguin.flowers.FlowerFactory;

import java.io.IOException;
import java.util.List;

public class MainViewModel extends ViewModel {
    public String[] getFlowers(FlowerConstants.Species species, Context appContext) throws IOException {
        FlowerFactory flowerFactory =
                ((FlowerBreedingApplication) appContext.getApplicationContext()).getFlowerFactory();

        List<Flower> flowers = flowerFactory.getAllFlowersForSpecies(species);
        String[] results = new String[flowers.size()];

        int idx = 0;
        for (Flower f : flowers)
            results[idx++] = f.toString();

        return results;
    }
}
