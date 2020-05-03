package org.molguin.acbreedinghelper.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import org.molguin.acbreedinghelper.R;
import org.molguin.acbreedinghelper.model.MainActivityViewModel;

public class MainActivity extends AppCompatActivity {

    private MainActivityViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        if (savedInstanceState == null) {
            MainActivityViewModel.Factory factory = new MainActivityViewModel.Factory(this.getApplication());
            this.model = new ViewModelProvider(this, factory).get(MainActivityViewModel.class);
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
