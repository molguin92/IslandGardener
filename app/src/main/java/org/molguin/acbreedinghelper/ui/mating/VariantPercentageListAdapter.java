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
import org.molguin.acbreedinghelper.flowers.FlowerColorGroup;

import java.util.Map;

public class VariantPercentageListAdapter extends ListAdapter<Map.Entry<FlowerColorGroup, Double>, VariantPercentageListAdapter.ViewHolder> {

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
        Map.Entry<FlowerColorGroup, Double> item = getItem(position);
        holder.setFlower(item.getKey(), item.getValue());
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

        void setFlower(FlowerColorGroup f, double probability) {
            // set icon
            int icon_id = iconView.getContext()
                    .getResources()
                    .getIdentifier(String.format("%s_%s",
                            f.species.name().toLowerCase(),
                            f.color.name().toLowerCase()),
                            "drawable",
                            iconView.getContext().getPackageName());

            iconView.setImageResource(icon_id);

            colorView.setText(f.color.name().toUpperCase());
//            variantIdView.setText(f.human_readable_genotype);
            percentView.setText(String.format("%.2f%%", probability * 100)); // convert to percent
        }
    }

    private static class DiffCallback extends DiffUtil.ItemCallback<Map.Entry<FlowerColorGroup, Double>> {

        @Override
        public boolean areItemsTheSame(@NonNull Map.Entry<FlowerColorGroup, Double> oldItem,
                                       @NonNull Map.Entry<FlowerColorGroup, Double> newItem) {
            return oldItem.getKey().hashCode() == newItem.getKey().hashCode();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Map.Entry<FlowerColorGroup, Double> oldItem,
                                          @NonNull Map.Entry<FlowerColorGroup, Double> newItem) {
            return areItemsTheSame(oldItem, newItem) && (oldItem.getValue().equals(newItem.getValue()));
        }
    }
}
