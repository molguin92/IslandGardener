package org.molguin.acbreedinghelper.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.molguin.acbreedinghelper.R;
import org.molguin.flowers.Flower;
import org.molguin.flowers.FlowerConstants;

import java.util.Collection;
import java.util.List;

public class MainFragment extends Fragment {

    private MainViewModel mViewModel;
    private final FlowerAdapter flowerAdapter;

    public MainFragment() {
        this.flowerAdapter = new FlowerAdapter();
    }

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
        mViewModel = new ViewModelProvider(this.getActivity()).get(MainViewModel.class);

        RecyclerView rview = this.getView().findViewById(R.id.resultview);
        rview.setAdapter(this.flowerAdapter);
        rview.setLayoutManager(new LinearLayoutManager(this.getContext()));

        // listen to the flower live data
        mViewModel.flowerListLiveData().observe(this.getViewLifecycleOwner(),
                new Observer<Collection<Flower>>() {
                    @Override
                    public void onChanged(Collection<Flower> flowers) {
                        flowerAdapter.updateFlowers(flowers);
                    }
                });

        final Spinner species_select = this.getView().findViewById(R.id.spinner_species);
        ArrayAdapter<String> species_adapter = new ArrayAdapter<String>(this.getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                mViewModel.getFlowerSpecies()
        );
        species_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        species_select.setAdapter(species_adapter);
        species_select.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String species_str = species_select.getSelectedItem().toString().toUpperCase();
                FlowerConstants.Species species = FlowerConstants.Species.valueOf(species_str);
                mViewModel.loadFlowersForSpeciesAsync(species);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
//        mViewModel.loadFlowerListAsync();
    }
}
