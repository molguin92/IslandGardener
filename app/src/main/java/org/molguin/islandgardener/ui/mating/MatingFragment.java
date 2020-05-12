/*
 *     Copyright © 2020 Manuel Olguín Muñoz <manuel@olguin.se>
 *
 *     MatingFragment.java is part of Island Gardener
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

import org.molguin.islandgardener.databinding.MatingCalcLayoutBinding;
import org.molguin.islandgardener.flowers.FlowerColorGroup;
import org.molguin.islandgardener.flowers.FlowerConstants;
import org.molguin.islandgardener.flowers.FuzzyFlower;
import org.molguin.islandgardener.flowers.SpecificFlower;
import org.molguin.islandgardener.model.MainViewModel;
import org.molguin.islandgardener.model.MatingViewModel;
import org.molguin.islandgardener.utils.Callback;

import java.util.Collection;
import java.util.SortedSet;

public class MatingFragment extends Fragment implements MatingViewModel.dataLoadedCallback, MatingViewModel.matesCalculatedCallback {
    public static final String ARG_SPECIES_ORDINAL = "species";
    private MatingCalcLayoutBinding binding;
    private MatingSpinnerAdapter adapter;
    private MatingGroupWrapper wrapper;


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
        final MainViewModel mViewModel = new ViewModelProvider(this.requireActivity()).get(MainViewModel.class);

        final Spinner p1spinner = this.binding.parent1Spinner;
        final Spinner p2spinner = this.binding.parent2Spinner;
        final RecyclerView resultview = this.binding.resultRecyclerView;
        this.adapter = new MatingSpinnerAdapter();
        p1spinner.setAdapter(this.adapter);
        p2spinner.setAdapter(this.adapter);

        this.wrapper =
                new MatingGroupWrapper(mViewModel.isAdvancedMode(),
                        mViewModel.iswGeneInvMode());
        resultview.setAdapter(wrapper.getAdapter());
        resultview.setLayoutManager(new LinearLayoutManager(getContext()));


        // this callback is called whenever new offspring are calculated
        Callback<SortedSet<FuzzyFlower>, Void> offspringCallback =
                new Callback<SortedSet<FuzzyFlower>, Void>() {
                    @Override
                    public Void apply(SortedSet<FuzzyFlower> offspring) {
                        // prepare groups in background thread
//                        final List<Group> groups = new ArrayList<Group>();
//                        if (mViewModel.isAdvancedMode()) {
//                            for (FuzzyFlower flower : offspring)
//                                groups.add(new ExpandableFlowerGroup(flower, mViewModel.iswGeneInvMode()));
//                        } else {
//                            for (FuzzyFlower flower : offspring)
//                                groups.add(new SimpleFlowerItem(flower));
//                        }
//                        requireActivity().runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                adapter.clear();
//                                adapter.addAll(groups);
//                                resultview.smoothScrollToPosition(0);
//                            }
//                        });

                        return null;
                    }
                };

        MatingViewModel.Factory factory =
                new MatingViewModel.Factory(
                        mViewModel.getFlowerCollection(), species, this);
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
        viewModel.loadData(this);
        mViewModel.observeAdvancedMode(this.getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean advancedMode) {
                wrapper.setAdvancedMode(advancedMode);
                // spinner adapter
                adapter.setAdvancedMode(advancedMode);
            }
        });

        mViewModel.observeWGeneInvMode(this.getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean invWGeneMode) {
                wrapper.setInvWGeneMode(invWGeneMode);
                //spinner adapter
                adapter.setInvWGeneMode(invWGeneMode);
            }
        });
    }

    @Override
    public void onDataLoaded(final Collection<FlowerColorGroup> groups, final Collection<SpecificFlower> flowers) {
        this.requireActivity()
                .runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                adapter.setGroupsAndFlowers(groups, flowers);
                            }
                        }
                );
    }

    @Override
    public void onMatesCalculated(final Collection<FuzzyFlower> flowers) {
        this.requireActivity()
                .runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        wrapper.setFlowers(flowers);
                    }
                });
    }
}
