package org.molguin.acbreedinghelper.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import org.molguin.acbreedinghelper.R;
import org.molguin.acbreedinghelper.model.MainActivityViewModel;
import org.molguin.acbreedinghelper.utils.Callback;

public class MainActivity extends AppCompatActivity {

    private MainActivityViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        if (savedInstanceState == null) {
            MainActivityViewModel.Factory factory =
                    new MainActivityViewModel.Factory(
                            this.getApplication(),
                            new Callback<Void, Void>() {
                                // callback to switch to the relevant fragment once data is loaded
                                @Override
                                public Void apply(Void aVoid) {
                                    MainActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            MainActivity.this.getSupportFragmentManager()
                                                    .beginTransaction()
                                                    .replace(R.id.container, SpeciesTabFragment.newInstance())
                                                    .commitNow();
                                        }
                                    });
                                    return null;
                                }
                            });


            this.model = new ViewModelProvider(this, factory).get(MainActivityViewModel.class);
            this.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, LoadingFragment.newInstance())
                    .commitNow();
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.model = null;
    }
}
