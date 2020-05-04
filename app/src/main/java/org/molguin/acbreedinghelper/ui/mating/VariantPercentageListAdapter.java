package org.molguin.acbreedinghelper.ui.mating;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import org.molguin.acbreedinghelper.R;
import org.molguin.acbreedinghelper.flowers.FuzzyFlower;

public class VariantPercentageListAdapter extends ListAdapter<FuzzyFlower, VariantPercentageListAdapter.ViewHolder> {

    VariantPercentageListAdapter() {
        super(new VariantPercentageListAdapter.DiffCallback());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.offspring_prob_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FuzzyFlower flower = getItem(position);
        holder.setFlower(flower);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView colorView;
        final TextView variantIdView;
        final TextView percentView;
        final ImageView iconView;

        ViewHolder(View v) {
            super(v);
            this.colorView = v.findViewById(R.id.flower_color);
            this.variantIdView = v.findViewById(R.id.flower_variant_id);
            this.percentView = v.findViewById(R.id.variant_percent);
            this.iconView = v.findViewById(R.id.flower_icon);
        }

        void setFlower(FuzzyFlower flower) {
            // set icon
            int icon_id = iconView.getContext()
                    .getResources()
                    .getIdentifier(flower.getIconName(),
                            "drawable",
                            iconView.getContext().getPackageName());

            iconView.setImageResource(icon_id);

            colorView.setText(flower.getColor().name().toUpperCase());
//            variantIdView.setText(f.human_readable_genotype);
            percentView.setText(String.format("%.2f%%", flower.getTotalProbability() * 100)); // convert to percent
        }
    }

    private static class DiffCallback extends DiffUtil.ItemCallback<FuzzyFlower> {

        @Override
        public boolean areItemsTheSame(@NonNull FuzzyFlower oldItem,
                                       @NonNull FuzzyFlower newItem) {
            return oldItem.hashCode() == newItem.hashCode();
        }

        @Override
        public boolean areContentsTheSame(@NonNull FuzzyFlower oldItem,
                                          @NonNull FuzzyFlower newItem) {
            return areItemsTheSame(oldItem, newItem) && (oldItem.equals(newItem));
        }
    }
}
