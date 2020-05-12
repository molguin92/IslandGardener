/*
 *     Copyright © 2020 Manuel Olguín Muñoz <manuel@olguin.se>
 *
 *     ExpandableFlowerGroup.java is part of Island Gardener
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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.xwray.groupie.ExpandableGroup;
import com.xwray.groupie.ExpandableItem;
import com.xwray.groupie.viewbinding.BindableItem;

import org.molguin.islandgardener.R;
import org.molguin.islandgardener.databinding.OffspringProbCardBinding;
import org.molguin.islandgardener.databinding.VariantProbHolderBinding;
import org.molguin.islandgardener.flowers.FuzzyFlower;
import org.molguin.islandgardener.flowers.SpecificFlower;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ExpandableFlowerGroup extends ExpandableGroup {
    private final FuzzyFlower flower;
    private final HeaderItem header;
    private final List<VariantItem> items;

    public ExpandableFlowerGroup(FuzzyFlower flower, boolean advancedMode, boolean invWGene) {
        super(new HeaderItem(flower, advancedMode), false);

        this.header = (HeaderItem) this.getGroup(0);
        this.items = new ArrayList<VariantItem>(flower.getVariantProbs().size());

        this.flower = flower;
        List<Map.Entry<SpecificFlower, Double>> entries = new ArrayList<Map.Entry<SpecificFlower, Double>>(this.flower.getVariantProbs().entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<SpecificFlower, Double>>() {
            @Override
            public int compare(Map.Entry<SpecificFlower, Double> o1, Map.Entry<SpecificFlower, Double> o2) {
                return Double.compare(o2.getValue(), o1.getValue()); // reverse order on probabilities.
            }
        });

        for (Map.Entry<SpecificFlower, Double> e : entries) {
            VariantItem item = new VariantItem(e, invWGene);
            this.items.add(item);
            this.add(item);
        }
    }

    public void setAdvancedMode(boolean on) {
        this.setExpanded(false);
        this.header.setAdvancedMode(on);
        this.notifyItemChanged(0);
    }

    public void setInvWGeneMode(boolean on) {
        for (VariantItem item : this.items)
            item.setInvWGene(on);
        this.notifyItemRangeChanged(1, this.items.size());
    }

    private static class HeaderItem extends BindableItem<OffspringProbCardBinding> implements ExpandableItem {

        private final FuzzyFlower flower;
        private ExpandableGroup eGroup;
        private boolean advancedMode;

        HeaderItem(FuzzyFlower headerFlower, boolean advancedMode) {
            this.advancedMode = advancedMode;
            this.flower = headerFlower;
        }

        void setAdvancedMode(boolean on) {
            if (this.advancedMode == on) return;
            this.advancedMode = on;
            this.notifyChanged();
        }

        @NonNull
        @Override
        protected OffspringProbCardBinding initializeViewBinding(@NonNull View view) {
            return OffspringProbCardBinding.inflate(LayoutInflater.from(view.getContext()), (ViewGroup) view, false);
        }

        @Override
        public void bind(@NonNull final OffspringProbCardBinding binding, int position) {
            // bind stuff here
            Context context = binding.viewholder.flowerIcon.getContext();
            binding.viewholder.flowerColor.setText(this.flower.getColor().name());
            int icon_id = context.getResources().getIdentifier(flower.getIconName(),
                    "drawable", context.getPackageName());

            binding.viewholder.flowerIcon.setImageResource(icon_id);
            binding.viewholder.variantPercent
                    .setText(String.format("%.2f%%", flower.getTotalProbability() * 100));

            String fmt_string = context.getResources().getString(R.string.variants_fmt_string);
            binding.viewholder.flowerVariantId.setText(String.format(fmt_string, flower.getVariantProbs().size()));
            binding.viewholder.arrow.setImageResource(R.drawable.ic_arrow_drop_down_black_24dp);

            if (this.advancedMode) {
                binding.getRoot().setClickable(true);
                binding.viewholder.arrow.setVisibility(View.VISIBLE);
                binding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (eGroup != null) {
                            eGroup.onToggleExpanded();
                            boolean expanded = eGroup.isExpanded();
                            binding.getRoot().setSelected(expanded);

                            if (expanded)
                                binding.viewholder.arrow.setImageResource(R.drawable.ic_arrow_drop_up_black_24dp);
                            else
                                binding.viewholder.arrow.setImageResource(R.drawable.ic_arrow_drop_down_black_24dp);
                        }
                    }
                });
            } else {
                binding.getRoot().setClickable(false);
                binding.viewholder.arrow.setVisibility(View.INVISIBLE);
                binding.getRoot().setOnClickListener(null);
            }

        }

        @Override
        public int getLayout() {
            return R.layout.offspring_prob_card;
        }

        @Override
        public void setExpandableGroup(@NonNull ExpandableGroup onToggleListener) {
            this.eGroup = onToggleListener;
        }
    }


    private static class VariantItem extends BindableItem<org.molguin.islandgardener.databinding.VariantProbHolderBinding> {
        // for subitems
        private final SpecificFlower flower;
        private final double probability;
        private boolean invWGene;

        VariantItem(Map.Entry<SpecificFlower, Double> entry, boolean invWGene) {
            this.flower = entry.getKey();
            this.probability = entry.getValue();
            this.invWGene = invWGene;
        }

        void setInvWGene(boolean on) {
            if (on == this.invWGene) return;
            this.invWGene = on;
            this.notifyChanged();
        }

        @NonNull
        @Override
        protected VariantProbHolderBinding initializeViewBinding(@NonNull View view) {
            return VariantProbHolderBinding.inflate(LayoutInflater.from(view.getContext()), (ViewGroup) view, false);
        }

        @Override
        public void bind(@NonNull VariantProbHolderBinding binding, int position) {
            // bind stuff here
            Context context = binding.flowerIcon.getContext();
            int icon_id = context.getResources().getIdentifier(flower.getIconName(),
                    "drawable", context.getPackageName());

            binding.flowerIcon.setImageResource(icon_id);
            binding.variantPercent.setText(String.format("%.2f%%", this.probability * 100));

            SpecificFlower f = this.flower
                    .getVariantProbs()
                    .keySet()
                    .toArray(new SpecificFlower[1])[0];
            binding.variantsHeading.setText(f.genotypeIDToSymbolic(this.invWGene));

        }

        @Override
        public int getLayout() {
            return R.layout.variant_prob_holder;
        }
    }
}
