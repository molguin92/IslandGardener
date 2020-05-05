package org.molguin.islandgardener.ui.mating;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xwray.groupie.Group;
import com.xwray.groupie.GroupAdapter;

import org.molguin.islandgardener.databinding.MatingCalcLayoutBinding;
import org.molguin.islandgardener.flowers.FlowerConstants;
import org.molguin.islandgardener.flowers.FuzzyFlower;
import org.molguin.islandgardener.model.MainActivityViewModel;
import org.molguin.islandgardener.model.MatingViewModel;
import org.molguin.islandgardener.utils.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

        final FlowerConstants.Species species =
                FlowerConstants.Species.values()[args.getInt(MatingFragment.ARG_SPECIES_ORDINAL)];

        MainActivityViewModel mViewModel = new ViewModelProvider(this.getActivity()).get(MainActivityViewModel.class);
        MatingViewModel.Factory factory = new MatingViewModel.Factory(mViewModel.getFlowerCollection(), species);
        final MatingViewModel viewModel = new ViewModelProvider(this, factory).get(MatingViewModel.class);

        final Spinner p1spinner = this.binding.parent1Spinner;
        final Spinner p2spinner = this.binding.parent2Spinner;

        // on item selected listeners
        p1spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.dispatcher.setMate1((FuzzyFlower) parent.getAdapter().getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // on item selected listeners
        p2spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.dispatcher.setMate2((FuzzyFlower) parent.getAdapter().getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final RecyclerView resultview = this.binding.resultRecyclerView;

        final ProgressBar pbar = this.binding.loadingPbar;
        final TextView loadText = this.binding.loadingText;

//        final VariantPercentageListAdapter recyclerViewAdapter = new VariantPercentageListAdapter();
        final GroupAdapter adapter = new GroupAdapter();
        resultview.setAdapter(adapter);
        resultview.setLayoutManager(new LinearLayoutManager(getContext()));

        viewModel.dispatcher.setCallback(new Callback<Set<FuzzyFlower>, Void>() {
            @Override
            public Void apply(final Set<FuzzyFlower> offspring) {
                // offspring already sorted, no need to sort again
//                final List<FuzzyFlower> offspring_list = new ArrayList<FuzzyFlower>(offspring);
//
//                // sort the entries before handing them over
//                // by probability, in ascending order!
//                Collections.sort(offspring_list, new Comparator<FuzzyFlower>() {
//                    @Override
//                    public int compare(FuzzyFlower o1, FuzzyFlower o2) {
//                        return Double.compare(o2.getTotalProbability(), o1.getTotalProbability());
//                    }
//                });

                // prepare groups in background thread
                final List<Group> groups = new ArrayList<Group>();
                for (FuzzyFlower flower : offspring)
                    groups.add(new ExpandableFlowerGroup(flower));

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
        });

        // load data at the end
        viewModel.loadData(new Callback<SortedSet<FuzzyFlower>, Void>() {
            @Override
            public Void apply(final SortedSet<FuzzyFlower> flowers) {
                final MatingSpinnerAdapter adapter = new MatingSpinnerAdapter(flowers);
                MatingFragment.this
                        .getActivity()
                        .runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                p1spinner.setAdapter(adapter);
                                p2spinner.setAdapter(adapter);

                                pbar.setVisibility(View.GONE);
                                loadText.setVisibility(View.GONE);
                            }
                        });
                return null;
            }
        });

    }
}
