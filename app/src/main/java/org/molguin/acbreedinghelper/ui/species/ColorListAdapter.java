package org.molguin.acbreedinghelper.ui.species;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import org.molguin.acbreedinghelper.R;
import org.molguin.acbreedinghelper.model.ColorListViewModel;
import org.molguin.acbreedinghelper.model.FuzzyFlower;

public class ColorListAdapter extends RecyclerView.Adapter<ColorListAdapter.FlowerColorCard> {
    public final SortedList<FuzzyFlower> fuzzyFlowerList;

    public ColorListAdapter() {
        super();
        SortedList.Callback<FuzzyFlower> callback =
                new ColorListViewModel.SortedListColorCollectionCallback(this);

        this.fuzzyFlowerList =
                new SortedList<FuzzyFlower>(FuzzyFlower.class, callback);
    }

    @NonNull
    @Override
    public FlowerColorCard onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View flower_view = inflater.inflate(R.layout.color_card, parent, false);

        // Return a new holder instance
        FlowerColorCard viewHolder = new FlowerColorCard(flower_view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FlowerColorCard holder, int position) {
        holder.setFuzzyFlower(this.fuzzyFlowerList.get(position));
    }

    @Override
    public int getItemCount() {
        return this.fuzzyFlowerList.size();
    }

    static class FlowerColorCard extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView color;
        final TextView genotypes_counts_view;
        final String variants_fmt_string;

        FlowerColorCard(@NonNull View itemView) {
            super(itemView);
            this.icon = itemView.findViewById(R.id.flower_icon);
            this.color = itemView.findViewById(R.id.flower_color);
            this.genotypes_counts_view = itemView.findViewById(R.id.variants_heading);
            this.variants_fmt_string = itemView.getContext().getResources().getString(R.string.variants_fmt_string);

        }

        void setFuzzyFlower(final FuzzyFlower fuzzyFlower) {
            // set up the display
            int icon_id = icon.getContext()
                    .getResources()
                    .getIdentifier(fuzzyFlower.icon_name,
                            "drawable",
                            icon.getContext().getPackageName());

            this.icon.setImageResource(icon_id);
            this.color.setText(fuzzyFlower.color.name().toUpperCase());
            this.genotypes_counts_view.setText(
                    String.format(this.variants_fmt_string, fuzzyFlower.variants.size())
            );

            // add a listener to switch to a fragment with details
            this.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
        }
    }
}
