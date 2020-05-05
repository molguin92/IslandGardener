package org.molguin.acbreedinghelper.ui.mating;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.xwray.groupie.Section;
import com.xwray.groupie.viewbinding.BindableItem;

import org.molguin.acbreedinghelper.R;
import org.molguin.acbreedinghelper.databinding.OffspringProbCardBinding;
import org.molguin.acbreedinghelper.databinding.VariantProbHolderBinding;
import org.molguin.acbreedinghelper.flowers.FuzzyFlower;
import org.molguin.acbreedinghelper.flowers.SpecificFlower;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class FuzzyFlowerSection extends Section {
    private final FuzzyFlower flower;

    public FuzzyFlowerSection(FuzzyFlower flower) {
        super(new Section(new HeaderItem(flower)));
        this.flower = flower;
        List<Map.Entry<SpecificFlower, Double>> entries = new ArrayList<Map.Entry<SpecificFlower, Double>>(this.flower.getVariantProbs().entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<SpecificFlower, Double>>() {
            @Override
            public int compare(Map.Entry<SpecificFlower, Double> o1, Map.Entry<SpecificFlower, Double> o2) {
                return Double.compare(o2.getValue(), o1.getValue()); // reverse order on probabilities.
            }
        });

        for (Map.Entry<SpecificFlower, Double> e : entries)
            this.add(new VariantItem(e));
    }

    private static class HeaderItem extends BindableItem<OffspringProbCardBinding> {

        private final FuzzyFlower flower;

        HeaderItem(FuzzyFlower headerFlower) {
            this.flower = headerFlower;
        }

        @NonNull
        @Override
        protected OffspringProbCardBinding initializeViewBinding(@NonNull View view) {
            return OffspringProbCardBinding.inflate(LayoutInflater.from(view.getContext()), (ViewGroup) view, false);
        }

        @Override
        public void bind(@NonNull OffspringProbCardBinding binding, int position) {
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

        }

        @Override
        public int getLayout() {
            return R.layout.offspring_prob_card;
        }
    }

    private static class VariantItem extends BindableItem<org.molguin.acbreedinghelper.databinding.VariantProbHolderBinding> {
        // for subitems
        private final SpecificFlower flower;
        private final double probability;

        VariantItem(Map.Entry<SpecificFlower, Double> entry) {
            this.flower = entry.getKey();
            this.probability = entry.getValue();
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
            binding.variantsHeading.setText(f.human_readable_genotype);

        }

        @Override
        public int getLayout() {
            return R.layout.variant_prob_holder;
        }
    }
}
