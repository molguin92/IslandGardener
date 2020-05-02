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
import org.molguin.acbreedinghelper.flowers.FlowerConstants;
import org.molguin.acbreedinghelper.model.ColorListViewModel;
import org.molguin.acbreedinghelper.model.FuzzyFlower;
import org.molguin.acbreedinghelper.model.MainActivityViewModel;
import org.molguin.acbreedinghelper.utils.Callback;

import java.util.Set;

public class SpeciesColorsListFragment extends Fragment {
    public static final String ARG_SPECIES_ORDINAL = "species";


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
        MainActivityViewModel mViewModel = new ViewModelProvider(this.getActivity()).get(MainActivityViewModel.class);

        ProgressBar pbar = view.findViewById(R.id.progressbar);
        pbar.setIndeterminate(true);
        pbar.setVisibility(View.VISIBLE);

        Bundle args = getArguments();
        if (args != null) {
            FlowerConstants.Species species =
                    FlowerConstants.Species.values()[args.getInt(SpeciesColorsListFragment.ARG_SPECIES_ORDINAL)];

            RecyclerView rview = this.getView().findViewById(R.id.resultview);
            final ColorListAdapter adapter = new ColorListAdapter();
            rview.setVisibility(View.INVISIBLE);
            rview.setAdapter(adapter);
            rview.setLayoutManager(new LinearLayoutManager(this.getContext()));

            ColorListViewModel.Factory factory = new ColorListViewModel.Factory(mViewModel.getFlowerCollection(), species);
            ColorListViewModel cViewModel = new ViewModelProvider(this, factory).get(ColorListViewModel.class);

            cViewModel.loadData(new Callback<Set<FuzzyFlower>, Void>() {
                @Override
                public Void apply(final Set<FuzzyFlower> fuzzyFlowers) {
                    SpeciesColorsListFragment.this
                            .getActivity()
                            .runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.fuzzyFlowerList.addAll(fuzzyFlowers);
                                    SpeciesColorsListFragment.this.notifyFinishedLoading();
                                }
                            });
                    return null;
                }
            });
        }
    }

    private void notifyFinishedLoading() {
        ProgressBar pbar = this.getView().findViewById(R.id.progressbar);
        pbar.setIndeterminate(true);
        pbar.setVisibility(View.INVISIBLE);
        ((ViewGroup) this.getView()).removeView(pbar);
        RecyclerView rview = this.getView().findViewById(R.id.resultview);
        rview.setVisibility(View.VISIBLE);
    }
}
