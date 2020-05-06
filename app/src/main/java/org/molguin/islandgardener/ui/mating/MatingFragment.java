package org.molguin.islandgardener.ui.mating;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xwray.groupie.Group;
import com.xwray.groupie.GroupAdapter;

import org.molguin.islandgardener.databinding.MatingCalcLayoutBinding;
import org.molguin.islandgardener.flowers.FlowerConstants;
import org.molguin.islandgardener.flowers.FuzzyFlower;
import org.molguin.islandgardener.model.MainViewModel;
import org.molguin.islandgardener.model.MatingViewModel;
import org.molguin.islandgardener.utils.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

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

        final Spinner p1spinner = this.binding.parent1Spinner;
        final Spinner p2spinner = this.binding.parent2Spinner;
        final RecyclerView resultview = this.binding.resultRecyclerView;

        final GroupAdapter adapter = new GroupAdapter();
        resultview.setAdapter(adapter);
        resultview.setLayoutManager(new LinearLayoutManager(getContext()));

        final FlowerConstants.Species species =
                FlowerConstants.Species.values()[args.getInt(MatingFragment.ARG_SPECIES_ORDINAL)];
        final MainViewModel mViewModel = new ViewModelProvider(this.requireActivity()).get(MainViewModel.class);

        // prepare callback to pass to the viewmodel
        // this callback is called when the data for the spinners is loaded
        Callback<SortedSet<FuzzyFlower>, Void> dataLoadedCallback =
                new Callback<SortedSet<FuzzyFlower>, Void>() {
                    @Override
                    public Void apply(SortedSet<FuzzyFlower> flowers) {
                        final MatingSpinnerAdapter adapter = new MatingSpinnerAdapter(flowers);
                        MatingFragment.this
                                .requireActivity()
                                .runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        p1spinner.setAdapter(adapter);
                                        p2spinner.setAdapter(adapter);
                                    }
                                });
                        return null;
                    }
                };

        // this callback is called whenever new offspring are calculated
        Callback<SortedSet<FuzzyFlower>, Void> offspringCallback =
                new Callback<SortedSet<FuzzyFlower>, Void>() {
                    @Override
                    public Void apply(SortedSet<FuzzyFlower> offspring) {
                        // prepare groups in background thread
                        final List<Group> groups = new ArrayList<Group>();
                        if (mViewModel.isAdvancedMode()) {
                            for (FuzzyFlower flower : offspring)
                                groups.add(new ExpandableFlowerGroup(flower));
                        } else {
                            for (FuzzyFlower flower : offspring)
                                groups.add(new SimpleFlowerItem(flower));
                        }

                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.clear();
                                adapter.addAll(groups);
                                resultview.smoothScrollToPosition(0);
                            }
                        });
                        return null;
                    }
                };

        MatingViewModel.Factory factory =
                new MatingViewModel.Factory(
                        mViewModel.getFlowerCollection(),
                        species,
                        dataLoadedCallback,
                        offspringCallback);
        final MatingViewModel viewModel = new ViewModelProvider(this, factory).get(MatingViewModel.class);

        // on item selected listeners for the spinners
        p1spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.setMate1((FuzzyFlower) parent.getAdapter().getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        p2spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.setMate2((FuzzyFlower) parent.getAdapter().getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        // load data at the end
        viewModel.loadData(mViewModel.isAdvancedMode());
        mViewModel.observeMode(this.getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean advancedMode) {
                viewModel.loadData(advancedMode);
            }
        });
    }
}
