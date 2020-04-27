package org.molguin.acbreedinghelper.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import org.molguin.acbreedinghelper.R;
import org.molguin.flowers.FlowerConstants;

public class TabFragment extends Fragment {
    FlowerFragmentAdapter flowerFragmentAdapter;
    ViewPager2 viewPager;

    public static TabFragment newInstance() {
        return new TabFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        flowerFragmentAdapter = new FlowerFragmentAdapter(this);
        viewPager = view.findViewById(R.id.flower_pager);
        viewPager.setAdapter(flowerFragmentAdapter);
    }

    public static class FlowerFragmentAdapter extends FragmentStateAdapter {
        public FlowerFragmentAdapter(Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Fragment fragment = FlowerSpeciesFragment.newInstance();
            Bundle args = new Bundle();
            args.putInt(FlowerSpeciesFragment.ARG_SPECIES_ORDINAL,
                    position % FlowerConstants.Species.values().length);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return FlowerConstants.Species.values().length;
        }
    }
}
