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

import com.xwray.groupie.GroupAdapter;

import org.molguin.acbreedinghelper.databinding.MatingCalcLayoutBinding;
import org.molguin.acbreedinghelper.flowers.FlowerConstants;
import org.molguin.acbreedinghelper.flowers.FuzzyFlower;
import org.molguin.acbreedinghelper.model.MainActivityViewModel;
import org.molguin.acbreedinghelper.model.MatingViewModel;
import org.molguin.acbreedinghelper.utils.Callback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class MatingFragment extends Fragment {
    public static final String ARG_SPECIES_ORDINAL = "species";
    private MatingCalcLayoutBinding binding;


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
        this.binding = MatingCalcLayoutBinding.inflate(inflater);
        return this.binding.getRoot();

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

        final Spinner p1spinner = this.binding.parent1Spinner;
        final Spinner p2spinner = this.binding.parent2Spinner;
        final RecyclerView resultview = this.binding.resultRecyclerView;

//        final VariantPercentageListAdapter recyclerViewAdapter = new VariantPercentageListAdapter();
        final GroupAdapter adapter = new GroupAdapter();
        resultview.setAdapter(adapter);
        resultview.setLayoutManager(new LinearLayoutManager(getContext()));

        cViewModel.dispatcher.setCallback(new Callback<Set<FuzzyFlower>, Void>() {
            @Override
            public Void apply(final Set<FuzzyFlower> offspring) {
                final List<FuzzyFlower> offspring_list = new ArrayList<FuzzyFlower>(offspring);

                // sort the entries before handing them over
                // by probability, in ascending order!
                Collections.sort(offspring_list, new Comparator<FuzzyFlower>() {
                    @Override
                    public int compare(FuzzyFlower o1, FuzzyFlower o2) {
                        return Double.compare(o2.getTotalProbability(), o1.getTotalProbability());
                    }
                });

                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.clear();
                        for (FuzzyFlower offspring : offspring_list)
                            adapter.add(new ExpandableFlowerGroup(offspring));
                        resultview.smoothScrollToPosition(0);
                    }
                });
                return null;
            }
        });

        // load data at the end
        cViewModel.loadData(new Callback<Set<FuzzyFlower>, Void>() {
            @Override
            public Void apply(final Set<FuzzyFlower> flowerColorGroups) {
                MatingFragment.this
                        .getActivity()
                        .runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                final MatingSpinnerAdapter adapter1 = new MatingSpinnerAdapter(flowerColorGroups);
                                final MatingSpinnerAdapter adapter2 = new MatingSpinnerAdapter(flowerColorGroups);
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
