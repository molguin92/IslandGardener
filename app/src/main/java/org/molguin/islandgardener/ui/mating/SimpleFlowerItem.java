/*
 *     Copyright © 2020 Manuel Olguín Muñoz <manuel@olguin.se>
 *
 *     SimpleFlowerItem.java is part of Island Gardener
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

import com.xwray.groupie.viewbinding.BindableItem;

import org.molguin.islandgardener.R;
import org.molguin.islandgardener.databinding.SimpleOffspringProbCardBinding;
import org.molguin.islandgardener.flowers.FuzzyFlower;

public class SimpleFlowerItem extends BindableItem<SimpleOffspringProbCardBinding> {
    private final FuzzyFlower flower;

    SimpleFlowerItem(FuzzyFlower headerFlower) {
        this.flower = headerFlower;
    }

    @NonNull
    @Override
    protected SimpleOffspringProbCardBinding initializeViewBinding(@NonNull View view) {
        return SimpleOffspringProbCardBinding.inflate(LayoutInflater.from(view.getContext()), (ViewGroup) view, false);
    }

    @Override
    public void bind(@NonNull final SimpleOffspringProbCardBinding binding, int position) {
        // bind stuff here
        Context context = binding.flowerIcon.getContext();
        binding.flowerColor.setText(this.flower.getColor().name());
        int icon_id = context.getResources().getIdentifier(flower.getIconName(),
                "drawable", context.getPackageName());

        binding.flowerIcon.setImageResource(icon_id);
        binding.variantPercent
                .setText(String.format("%.2f%%", flower.getTotalProbability() * 100));
    }

    @Override
    public int getLayout() {
        return R.layout.simple_offspring_prob_card;
    }
}
