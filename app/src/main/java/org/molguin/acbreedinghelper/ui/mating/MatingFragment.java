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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.molguin.acbreedinghelper.R;
import org.molguin.acbreedinghelper.flowers.Flower;
import org.molguin.acbreedinghelper.flowers.FlowerConstants;
import org.molguin.acbreedinghelper.model.FuzzyFlower;
import org.molguin.acbreedinghelper.model.MainActivityViewModel;
import org.molguin.acbreedinghelper.model.MatingViewModel;
import org.molguin.acbreedinghelper.utils.Callback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
        MatingViewModel.Factory factory = new MatingViewModel.Factory(mViewModel.getFlowerCollection(), species);
        final MatingViewModel cViewModel = new ViewModelProvider(this, factory).get(MatingViewModel.class);

        final Spinner p1spinner = view.findViewById(R.id.parent1_spinner);
        final Spinner p2spinner = view.findViewById(R.id.parent2_spinner);
        final RecyclerView resultview = view.findViewById(R.id.result_recycler_view);

        final VariantPercentageListAdapter recyclerViewAdapter = new VariantPercentageListAdapter();
        resultview.setAdapter(recyclerViewAdapter);
        resultview.setLayoutManager(new LinearLayoutManager(getContext()));

        cViewModel.dispatcher.setCallback(new Callback<Map<Flower, Double>, Void>() {
            @Override
            public Void apply(final Map<Flower, Double> flowerDoubleMap) {
                final List<Map.Entry<Flower, Double>> entries = new ArrayList<Map.Entry<Flower, Double>>(flowerDoubleMap.entrySet());
                // sort the entries before handing them over
                Collections.sort(entries, new Comparator<Map.Entry<Flower, Double>>() {
                    @Override
                    public int compare(Map.Entry<Flower, Double> o1, Map.Entry<Flower, Double> o2) {
                        return Double.compare(o2.getValue(), o1.getValue()); // NOTE: REVERSE ORDER!
                    }
                });

                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recyclerViewAdapter.submitList(entries);
                        resultview.smoothScrollToPosition(0);
                    }
                });
                return null;
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
                                final MatingSpinnerAdapter adapter1 = new MatingSpinnerAdapter(fuzzyFlowers);
                                final MatingSpinnerAdapter adapter2 = new MatingSpinnerAdapter(fuzzyFlowers);
                                p1spinner.setAdapter(adapter1);
                                p2spinner.setAdapter(adapter2);

                                // on item selected listeners
                                p1spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                        cViewModel.dispatcher.setMate1(adapter1.getItem(position));
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {

                                    }
                                });

                                // on item selected listeners
                                p2spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                        cViewModel.dispatcher.setMate2(adapter2.getItem(position));
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {

                                    }
                                });
                            }
                        });
                return null;
            }
        });

    }
}
