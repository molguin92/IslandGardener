package org.molguin.islandgardener.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import org.molguin.islandgardener.R;
import org.molguin.islandgardener.databinding.MainActivityBinding;
import org.molguin.islandgardener.flowers.FlowerCollection;
import org.molguin.islandgardener.model.MainActivityViewModel;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.about) {
            Toast t = Toast.makeText(getApplicationContext(), "Testing info", Toast.LENGTH_LONG);
            t.show();
            return (true);
        }
        return (super.onOptionsItemSelected(item));
    }
}
