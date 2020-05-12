/*
 *     Copyright © 2020 Manuel Olguín Muñoz <manuel@olguin.se>
 *
 *     MainActivity.java is part of Island Gardener
 *
 *     Island Gardener is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Island Gardener is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Island Gardener.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.molguin.islandgardener.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import org.molguin.islandgardener.R;
import org.molguin.islandgardener.databinding.AboutDialogBinding;
import org.molguin.islandgardener.databinding.MainActivityBinding;
import org.molguin.islandgardener.model.MainViewModel;

public class MainActivity extends AppCompatActivity {
    private static final String ADV_MODE_PREF_KEY = "ADVANCED_MODE_ON";
    private static final String WGENE_MODE_PREF_KEY = "WGENE_MODE_INV";

    MainViewModel viewModel;

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences prefs = this.getPreferences(Context.MODE_PRIVATE);
        prefs.edit().putBoolean(ADV_MODE_PREF_KEY, viewModel.isAdvancedMode()).apply();
        prefs.edit().putBoolean(WGENE_MODE_PREF_KEY, viewModel.iswGeneInvMode()).apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // load preferences
        SharedPreferences prefs = this.getPreferences(Context.MODE_PRIVATE);
        boolean advMode = prefs.getBoolean(ADV_MODE_PREF_KEY, false);
        boolean wGeneMode = prefs.getBoolean(WGENE_MODE_PREF_KEY, false);

        // load the bindings
        final MainActivityBinding binding = MainActivityBinding.inflate(getLayoutInflater());
        final View view = binding.getRoot();
        setContentView(view);

        // loading splash screen while we do stuff in the background
        this.getSupportFragmentManager()
                .beginTransaction()
                .replace(binding.container.getId(), LoadingFragment.newInstance())
                .commitNow();

        // load the viewmodel, set a callback to start the fragments once the data has been loaded
        final MainViewModel.Factory fact =
                new MainViewModel.Factory(
                        this.getAssets(),
                        this.getApplicationContext(),
                        new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        getSupportFragmentManager()
                                                .beginTransaction()
                                                .replace(binding.container.getId(),
                                                        SpeciesTabFragment.newInstance())
                                                .commitNow();
                                    }
                                });
                            }
                        }
                );
        this.viewModel = new ViewModelProvider(this, fact).get(MainViewModel.class);
        this.viewModel.setAdvancedMode(advMode);
        this.viewModel.setwGeneInvMode(wGeneMode);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        if (menu instanceof MenuBuilder) {
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }

        menu.findItem(R.id.advanced_mode_switch).setChecked(this.viewModel.isAdvancedMode());
        menu.findItem(R.id.wgene_mode_switch).setChecked(this.viewModel.iswGeneInvMode());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                this.showAboutDialog();
                return true;
            case R.id.advanced_mode_switch:
                this.toggleAdvancedMode(item);
                return true;
            case R.id.wgene_mode_switch:
                this.toggleWGeneMode(item);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showAboutDialog() {
        DialogFragment aboutDialog = new AboutDialog();
        aboutDialog.show(getSupportFragmentManager(), "about");
    }

    private void toggleAdvancedMode(MenuItem advancedModeToggle) {
        advancedModeToggle.setChecked(this.viewModel.toggleAdvancedMode());
    }

    private void toggleWGeneMode(MenuItem wGeneToggle) {
        wGeneToggle.setChecked(this.viewModel.toggleWGeneInvMode());
    }

    public static class AboutDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            LayoutInflater inflater = requireActivity().getLayoutInflater();

            String app_name = this.requireActivity().getResources().getString(R.string.app_name);
            try {
                PackageInfo pInfo = this.requireActivity()
                        .getPackageManager()
                        .getPackageInfo(this.requireActivity().getPackageName(), 0);

                String version = pInfo.versionName;
                String title = String.format("%s v.%s", app_name, version);

                AboutDialogBinding binding = AboutDialogBinding.inflate(inflater, null, false);
                builder.setView(binding.getRoot())
                        .setTitle(title)
                        .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(false);

            } catch (PackageManager.NameNotFoundException ignored) {
//                Log.e("MyApp", "PackageManager Catch : ", e);
            }
            return builder.create();
        }

    }
}
