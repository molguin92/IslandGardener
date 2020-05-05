package org.molguin.islandgardener.ui.mating;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.molguin.islandgardener.R;
import org.molguin.islandgardener.databinding.ColorHolderBinding;
import org.molguin.islandgardener.flowers.FuzzyFlower;

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
        ViewHolder vh;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            ColorHolderBinding binding = ColorHolderBinding.inflate(inflater, parent, false);
            vh = new ViewHolder(binding);
        } else
            vh = (ViewHolder) convertView.getTag();


        FuzzyFlower f = this.flowers.get(position);
        vh.bind(f);
        return vh.rootView;
    }

    private static class ViewHolder {
        private final TextView colorview;
        private final ImageView iconView;
        private final TextView variantsView;
        private final String variants_fmt_string;
        private final View rootView;

        ViewHolder(ColorHolderBinding binding) {
            this.rootView = binding.getRoot();
            this.rootView.setTag(this);

            this.colorview = binding.flowerColor;
            this.iconView = binding.flowerIcon;
            this.variantsView = binding.variantsHeading;
            this.variants_fmt_string = this.rootView
                    .getContext()
                    .getResources()
                    .getString(R.string.variants_fmt_string);
        }

        void bind(FuzzyFlower f) {
            this.colorview.setText(f.getColor().name());
            if (!f.isGroup()) {
                this.colorview.setTypeface(null, Typeface.NORMAL);
                this.variantsView.setText(f.humanReadableVariants());
            } else {
                this.variantsView.setText(
                        String.format(this.variants_fmt_string, f.getVariantProbs().size()));
            }

            int icon_id = this.rootView.getContext()
                    .getResources()
                    .getIdentifier(f.getIconName(),
                            "drawable",
                            this.rootView.getContext().getPackageName());

            iconView.setImageResource(icon_id);
        }

    }
}
