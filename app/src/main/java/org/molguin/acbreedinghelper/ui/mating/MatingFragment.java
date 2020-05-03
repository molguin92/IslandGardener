package org.molguin.acbreedinghelper.ui.mating;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.molguin.acbreedinghelper.R;
import org.molguin.acbreedinghelper.flowers.FlowerConstants;
import org.molguin.acbreedinghelper.model.ColorListViewModel;
import org.molguin.acbreedinghelper.model.FuzzyFlower;
import org.molguin.acbreedinghelper.model.MainActivityViewModel;
import org.molguin.acbreedinghelper.utils.Callback;

import java.util.Set;

public class MatingFragment extends Fragment {
    public static final String ARG_SPECIES_ORDINAL = "species";


    public MatingFragment() {
        super();
    }

    public static MatingFragment newInstance() {
        return new MatingFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.mating_calc_layout, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args == null) return;

        final FlowerConstants.Species species =
                FlowerConstants.Species.values()[args.getInt(MatingFragment.ARG_SPECIES_ORDINAL)];

        MainActivityViewModel mViewModel = new ViewModelProvider(this.getActivity()).get(MainActivityViewModel.class);
        ColorListViewModel.Factory factory = new ColorListViewModel.Factory(mViewModel.getFlowerCollection(), species);
        final ColorListViewModel cViewModel = new ViewModelProvider(this, factory).get(ColorListViewModel.class);

        final Spinner p1spinner = view.findViewById(R.id.parent1_spinner);
        final Spinner p2spinner = view.findViewById(R.id.parent2_spinner);

        // on item selected listeners

        p1spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        // load data at the end
        cViewModel.loadData(new Callback<Set<FuzzyFlower>, Void>() {
            @Override
            public Void apply(final Set<FuzzyFlower> fuzzyFlowers) {
                MatingFragment.this
                        .getActivity()
                        .runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                p1spinner.setAdapter(new MatingSpinnerAdapter(fuzzyFlowers));
                                p2spinner.setAdapter(new MatingSpinnerAdapter(fuzzyFlowers));
                            }
                        });
                return null;
            }
        });

    }
}
