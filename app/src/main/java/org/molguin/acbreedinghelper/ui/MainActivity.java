package org.molguin.acbreedinghelper.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import org.molguin.acbreedinghelper.R;
import org.molguin.acbreedinghelper.model.MainViewModel;
import org.molguin.acbreedinghelper.ui.species.SpeciesTabFragment;

public class MainActivity extends AppCompatActivity {

    private MainViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        if (savedInstanceState == null) {
            MainViewModel.Factory factory = new MainViewModel.Factory(this.getApplication());
            this.model = new ViewModelProvider(this, factory).get(MainViewModel.class);
            this.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, SpeciesTabFragment.newInstance())
                    .commitNow();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.model = null;
    }
}
