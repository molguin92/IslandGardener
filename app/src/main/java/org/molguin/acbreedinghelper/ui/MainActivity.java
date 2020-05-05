package org.molguin.acbreedinghelper.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import org.molguin.acbreedinghelper.databinding.MainActivityBinding;
import org.molguin.acbreedinghelper.flowers.FlowerCollection;
import org.molguin.acbreedinghelper.model.MainActivityViewModel;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final MainActivityBinding binding = MainActivityBinding.inflate(getLayoutInflater());
        final View view = binding.getRoot();
        setContentView(view);
        // loading splash screen
        this.getSupportFragmentManager().beginTransaction()
                .replace(binding.container.getId(), LoadingFragment.newInstance())
                .commitNow();

        // load data in background
        final ExecutorService execServ = Executors.newSingleThreadExecutor();
        execServ.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    FlowerCollection collection = new FlowerCollection(getAssets(), getApplicationContext());
                    final MainActivityViewModel.Factory fact = new MainActivityViewModel.Factory(collection);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // store in viewmodel and switch to fragment
                            new ViewModelProvider(MainActivity.this, fact).get(MainActivityViewModel.class);
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(binding.container.getId(), SpeciesTabFragment.newInstance())
                                    .commitNow();
                            execServ.shutdownNow();
                        }
                    });
                } catch (IOException e) {
                    Log.e("MainActivity", "Could not load flower data!!", e);
                }
            }
        });
    }
}
