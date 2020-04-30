package org.molguin.acbreedinghelper.ui.species;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.molguin.acbreedinghelper.R;
import org.molguin.acbreedinghelper.model.MainViewModel;
import org.molguin.acbreedinghelper.flowers.FlowerConstants;

public class SpeciesColorsListFragment extends Fragment {
    public static final String ARG_SPECIES_ORDINAL = "species";
    private MainViewModel mViewModel;


    public SpeciesColorsListFragment() {
        super();
    }

    public static SpeciesColorsListFragment newInstance() {
        return new SpeciesColorsListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_fragment, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.mViewModel = new ViewModelProvider(this.getActivity()).get(MainViewModel.class);
        ProgressBar pbar = view.findViewById(R.id.progressbar);
        pbar.setIndeterminate(true);
        pbar.setVisibility(View.VISIBLE);

        Bundle args = getArguments();
        if (args != null) {
            FlowerConstants.Species species =
                    FlowerConstants.Species.values()[args.getInt(SpeciesColorsListFragment.ARG_SPECIES_ORDINAL)];

            RecyclerView rview = this.getView().findViewById(R.id.resultview);
            rview.setVisibility(View.INVISIBLE);
            rview.setAdapter(new ColorListAdapter(species,
                    this.mViewModel.getFlowerDB(),
                    this));
            rview.setLayoutManager(new LinearLayoutManager(this.getContext()));
        }
    }

    public void notifyFinishedLoading() {
        ProgressBar pbar = this.getView().findViewById(R.id.progressbar);
        pbar.setIndeterminate(true);
        pbar.setVisibility(View.INVISIBLE);
        ((ViewGroup) this.getView()).removeView(pbar);
        RecyclerView rview = this.getView().findViewById(R.id.resultview);
        rview.setVisibility(View.VISIBLE);
    }
}
