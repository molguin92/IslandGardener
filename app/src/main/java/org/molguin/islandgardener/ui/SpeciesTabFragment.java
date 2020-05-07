/*
 *     Copyright © 2020 Manuel Olguín Muñoz <manuel@olguin.se>
 *
 *     SpeciesTabFragment.java is part of Island Gardener
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

package org.molguin.islandgardener.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.molguin.islandgardener.databinding.FragmentTabBinding;
import org.molguin.islandgardener.flowers.FlowerConstants;
import org.molguin.islandgardener.ui.mating.MatingFragment;

public class SpeciesTabFragment extends Fragment {
    FlowerFragmentAdapter flowerFragmentAdapter;
    ViewPager2 viewPager;
    FragmentTabBinding binding;

    public static SpeciesTabFragment newInstance() {
        return new SpeciesTabFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        this.binding = FragmentTabBinding.inflate(inflater);
        return this.binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        flowerFragmentAdapter = new FlowerFragmentAdapter(this);
        viewPager = this.binding.flowerPager;
        viewPager.setAdapter(flowerFragmentAdapter);
        viewPager.setOffscreenPageLimit(FlowerConstants.Species.values().length);

        TabLayout tabLayout = this.binding.tabLayout;
        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                int species_pos = position % FlowerConstants.Species.values().length;
                tab.setText(FlowerConstants.Species.values()[species_pos].namePlural());
            }
        }).attach();
    }

    public static class FlowerFragmentAdapter extends FragmentStateAdapter {
        public FlowerFragmentAdapter(Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
//            Fragment fragment = SpeciesColorsListFragment.newInstance();
            Fragment fragment = MatingFragment.newInstance();
            Bundle args = new Bundle();
            args.putInt(MatingFragment.ARG_SPECIES_ORDINAL,
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
