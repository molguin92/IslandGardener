package org.molguin.acbreedinghelper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.util.Log;

import org.molguin.acbreedinghelper.ui.main.LoadingFragment;
import org.molguin.acbreedinghelper.ui.main.MainFragment;
import org.molguin.acbreedinghelper.ui.main.MainViewModel;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private MainViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        if (savedInstanceState == null) {
            this.model = new ViewModelProvider(this).get(MainViewModel.class);
            try {
                this.model.loadDataAsync(this.getApplicationContext());
            } catch (IOException e) {
                Log.e(this.toString(), e.toString());
            }

            boolean data_avail = this.model.dataAvailable().getValue();
            Log.e(this.toString(), String.valueOf(data_avail));
            if (!data_avail) {
                this.model.dataAvailable().observe(this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        if (aBoolean) {
                            Log.w(MainActivity.this.toString(), "Loaded");
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.container, MainFragment.newInstance())
                                    .commitNow();
                        }
                    }
                });

                Log.e(this.toString(), "Good, I guess?");
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, LoadingFragment.newInstance())
                        .commitNow();
            } else {
                Log.e(this.toString(), "Lolwhat");
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, MainFragment.newInstance())
                        .commitNow();
            }
        }
    }
}
