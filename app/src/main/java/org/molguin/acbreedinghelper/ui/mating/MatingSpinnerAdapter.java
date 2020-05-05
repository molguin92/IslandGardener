package org.molguin.acbreedinghelper.ui.mating;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.molguin.acbreedinghelper.R;
import org.molguin.acbreedinghelper.databinding.ColorHolderBinding;
import org.molguin.acbreedinghelper.flowers.FuzzyFlower;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MatingSpinnerAdapter extends BaseAdapter {
    final List<FuzzyFlower> flowers;

    MatingSpinnerAdapter(Collection<FuzzyFlower> flowers) {
        this.flowers = new ArrayList<FuzzyFlower>(flowers);
    }


    @Override
    public int getCount() {
        return this.flowers.size();
    }

    @Override
    public FuzzyFlower getItem(int position) {
        return this.flowers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return this.flowers.hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO: viewholder pattern

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ColorHolderBinding binding = ColorHolderBinding.inflate(inflater);
        View view = binding.getRoot();

        FuzzyFlower f = this.flowers.get(position);

        TextView colorview = binding.flowerColor;
        ImageView iconView = binding.flowerIcon;
        TextView variantsView = binding.variantsHeading;
        String variants_fmt_string = parent.getContext()
                .getResources()
                .getString(R.string.variants_fmt_string);

        colorview.setText(f.getColor().name());

        if (f.getVariantProbs().size() == 1) {
            variantsView.setText(f.humanReadableVariants());
        } else {
            variantsView.setText(
                    String.format(variants_fmt_string, f.getVariantProbs().size())
            );
        }

        int icon_id = parent.getContext()
                .getResources()
                .getIdentifier(f.getIconName(),
                        "drawable",
                        parent.getContext().getPackageName());

        iconView.setImageResource(icon_id);

        return view;
    }
}
