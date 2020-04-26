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
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView textView;
        public final ImageView iconView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.textView = itemView.findViewById(R.id.flower_text);
            this.iconView = itemView.findViewById(R.id.flowerIcon);
        }
    }

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
        View contactView = inflater.inflate(R.layout.flowerinfo, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data model based on position
        Flower flower = (Flower) this.flowers.get(position);

        // Set item views based on your views and data model
        String color = flower.color.name().toLowerCase();
        String species = flower.species.name().toLowerCase();
        String icon_name = String.format("%s_%s", species, color);


        TextView textView = holder.textView;
        ImageView iconView = holder.iconView;

        int icon_id = iconView.getContext()
                .getResources()
                .getIdentifier(icon_name, "drawable", iconView.getContext().getPackageName());

        textView.setText(flower.getEncodedGenotype());
        iconView.setImageResource(icon_id);
        iconView.setMaxHeight(24);
    }

    @Override
    public int getItemCount() {
        return this.flowers.size();
    }
}
