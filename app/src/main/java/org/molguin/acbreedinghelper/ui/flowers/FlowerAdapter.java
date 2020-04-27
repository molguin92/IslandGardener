package org.molguin.acbreedinghelper.ui.flowers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
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
        View flower_view = inflater.inflate(R.layout.flower_card, parent, false);

        // Return a new holder instance
        FlowerCard viewHolder = new FlowerCard(flower_view, context);
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
        final Context context;

        FlowerCard(@NonNull View itemView, @NonNull Context context) {
            super(itemView);
            this.icon = itemView.findViewById(R.id.flowerIcon);
            this.color = itemView.findViewById(R.id.flower_color);
            this.genotype = itemView.findViewById(R.id.flower_genotype);
            this.origin = itemView.findViewById(R.id.flower_origin);
            this.card = itemView.findViewById(R.id.flower_details);

            this.context = context;
        }

        void setFlower(final Flower flower) {
            int icon_id = icon.getContext()
                    .getResources()
                    .getIdentifier(flower.props.icon_name, "drawable", icon.getContext().getPackageName());

            this.icon.setImageResource(icon_id);
            this.color.setText(flower.props.color);
            this.genotype.setText(flower.props.genotype);
            this.origin.setText(flower.props.origin);

            this.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogFragment details = new FlowerDetailsDialog(flower);
                    details.show(
                            ((AppCompatActivity) FlowerCard.this.context).getSupportFragmentManager(),
                            "flower_dialog"
                    );
                }
            });
        }
    }
}
