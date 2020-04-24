package org.molguin.acbreedinghelper.ui.main;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.molguin.acbreedinghelper.R;
import org.molguin.flowers.FlowerConstants;

import java.io.IOException;

public class MainFragment extends Fragment {

    private MainViewModel mViewModel;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        try {
            String[] flowers = mViewModel.getFlowers(FlowerConstants.Species.ROSE,
                    this.getContext().getApplicationContext());
            for (String flower : flowers)
                Log.e("Flower", flower);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
