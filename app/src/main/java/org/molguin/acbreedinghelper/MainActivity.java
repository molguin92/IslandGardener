package org.molguin.acbreedinghelper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import org.molguin.acbreedinghelper.ui.main.LoadingFragment;
import org.molguin.acbreedinghelper.ui.main.MainFragment;
import org.molguin.acbreedinghelper.ui.main.MainViewModel;

public class MainActivity extends AppCompatActivity {

    private MainViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        if (savedInstanceState == null) {
            this.model = new ViewModelProvider(this).get(MainViewModel.class);
            this.model.loadDataAsync(this.getApplicationContext());

            boolean data_avail = this.model.dataLoadedLiveData().getValue();
            if (!data_avail) {
                this.model.dataLoadedLiveData().observe(this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean data_loaded) {
                        if (data_loaded) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.container, MainFragment.newInstance())
                                    .commitNow();
                        }
                    }
                });
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, LoadingFragment.newInstance())
                        .commitNow();
            } else {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, MainFragment.newInstance())
                        .commitNow();
            }
        }
    }
}
