package org.molguin.acbreedinghelper.ui.species;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.molguin.acbreedinghelper.R;
import org.molguin.acbreedinghelper.model.MainViewModel;
import org.molguin.acbreedinghelper.ui.flowers.FlowerColorAdapter;
import org.molguin.flowers.FlowerConstants;

public class FlowerSpeciesFragment extends Fragment {
    public static final String ARG_SPECIES_ORDINAL = "species";
    private MainViewModel mViewModel;


    public FlowerSpeciesFragment() {
        super();
    }

    public static FlowerSpeciesFragment newInstance() {
        return new FlowerSpeciesFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_fragment, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.mViewModel = new ViewModelProvider(this.getActivity()).get(MainViewModel.class);

        Bundle args = getArguments();
        if (args != null) {
            FlowerConstants.Species species =
                    FlowerConstants.Species.values()[args.getInt(FlowerSpeciesFragment.ARG_SPECIES_ORDINAL)];

            RecyclerView rview = this.getView().findViewById(R.id.resultview);
            rview.setAdapter(new FlowerColorAdapter(species,
                    this.mViewModel.getFlowerDB(),
                    this));
            rview.setLayoutManager(new LinearLayoutManager(this.getContext()));
        }
    }
}
