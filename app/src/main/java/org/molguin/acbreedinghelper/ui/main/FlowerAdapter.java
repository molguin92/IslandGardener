package org.molguin.acbreedinghelper.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.molguin.acbreedinghelper.R;
import org.molguin.flowers.Flower;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FlowerAdapter extends RecyclerView.Adapter<FlowerAdapter.ViewHolder> {
    final private List<Flower> flowers;

    public FlowerAdapter() {
        this.flowers = new ArrayList<Flower>();

    }

    public void updateFlowers(Collection<Flower> flowers) {
        this.flowers.clear();
        this.flowers.addAll(flowers);
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.flowercard, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data model based on position
        holder.setFlower(this.flowers.get(position));
    }

    @Override
    public int getItemCount() {
        return this.flowers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView icon;

        public final TextView color;
        public final TextView genotype;
        public final TextView origin;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.icon = itemView.findViewById(R.id.flowerIcon);
            this.color = itemView.findViewById(R.id.flower_color);
            this.genotype = itemView.findViewById(R.id.flower_genotype);
            this.origin = itemView.findViewById(R.id.flower_origin);
        }

        void setFlower(Flower flower) {
            String color = flower.color.name().toLowerCase();
            String species = flower.species.name().toLowerCase();
            String icon_name = String.format("%s_%s", species, color);
            String origin = flower.origin.name().toLowerCase();

            int icon_id = icon.getContext()
                    .getResources()
                    .getIdentifier(icon_name, "drawable", icon.getContext().getPackageName());

            this.icon.setImageResource(icon_id);
            this.color.setText(color);
            this.genotype.setText(flower.getHumanReadableGenotype());
            this.origin.setText(origin);
        }
    }
}
