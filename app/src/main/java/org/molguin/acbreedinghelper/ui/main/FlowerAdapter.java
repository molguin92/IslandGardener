package org.molguin.acbreedinghelper.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.molguin.acbreedinghelper.R;
import org.molguin.flowers.Flower;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FlowerAdapter extends RecyclerView.Adapter<FlowerAdapter.FlowerCard> {
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
    public FlowerCard onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.flowercard, parent, false);

        // Return a new holder instance
        FlowerCard viewHolder = new FlowerCard(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FlowerCard holder, int position) {
        // Get the data model based on position
        holder.setFlower(this.flowers.get(position));
    }

    @Override
    public int getItemCount() {
        return this.flowers.size();
    }

    static class FlowerCard extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView color;
        final TextView genotype;
        final TextView origin;
        final LinearLayout card;

        FlowerCard(@NonNull View itemView) {
            super(itemView);
            this.icon = itemView.findViewById(R.id.flowerIcon);
            this.color = itemView.findViewById(R.id.flower_color);
            this.genotype = itemView.findViewById(R.id.flower_genotype);
            this.origin = itemView.findViewById(R.id.flower_origin);
            this.card = itemView.findViewById(R.id.flower_card);
        }

        void setFlower(Flower flower) {
            final String color = flower.color.name().toLowerCase();
            final String species = flower.species.name().toLowerCase();
            final String icon_name = String.format("%s_%s", species, color);
            final String origin = flower.origin.name().toLowerCase();

            int icon_id = icon.getContext()
                    .getResources()
                    .getIdentifier(icon_name, "drawable", icon.getContext().getPackageName());

            this.icon.setImageResource(icon_id);
            this.color.setText(color);
            this.genotype.setText(flower.getHumanReadableGenotype());
            this.origin.setText(origin);

            this.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast toast = Toast.makeText(v.getContext(), color, Toast.LENGTH_LONG);
                    toast.show();

                }
            });
        }
    }
}
