package org.molguin.acbreedinghelper.ui.flowers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;
import androidx.recyclerview.widget.SortedListAdapterCallback;

import org.molguin.acbreedinghelper.R;
import org.molguin.acbreedinghelper.flowers.FlowerConstants;
import org.molguin.acbreedinghelper.flowers.FlowerDatabase;
import org.molguin.acbreedinghelper.ui.species.FlowerSpeciesFragment;
import org.molguin.acbreedinghelper.utils.Callback;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FlowerColorAdapter extends RecyclerView.Adapter<FlowerColorAdapter.FlowerColorCard> {
    final SortedList<FlowerColorCollection> colorCollections;
    final String variants_fmt_string;

    public FlowerColorAdapter(final FlowerConstants.Species species,
                              FlowerDatabase db,
                              final FlowerSpeciesFragment parentFragment) {
        super();
        this.variants_fmt_string = parentFragment.getResources().getString(R.string.variants_fmt_string);
        SortedList.Callback<FlowerColorCollection> callback =
                new SortedListAdapterCallback<FlowerColorCollection>(this) {
                    @Override
                    public int compare(FlowerColorCollection o1, FlowerColorCollection o2) {
                        return o1.color.compareTo(o2.color);
                    }

                    @Override
                    public boolean areContentsTheSame(FlowerColorCollection oldItem, FlowerColorCollection newItem) {
                        return this.areItemsTheSame(oldItem, newItem) &&
                                (oldItem.flowers.equals(newItem.flowers));
                    }

                    @Override
                    public boolean areItemsTheSame(FlowerColorCollection item1, FlowerColorCollection item2) {
                        return item1.color == item2.color;
                    }
                };

        this.colorCollections =
                new SortedList<FlowerColorCollection>(FlowerColorCollection.class, callback);
        // asynchronously load flowers
        db.makeQuery()
                .add(species)
                .addCallback(new Callback<Collection<FlowerDatabase.Flower>, Void>() {
                    @Override
                    public Void apply(Collection<FlowerDatabase.Flower> flowers) {
                        final Map<FlowerConstants.Color, FlowerColorCollection> colors =
                                new HashMap<FlowerConstants.Color, FlowerColorCollection>();

                        for (FlowerDatabase.Flower flower : flowers) {
                            FlowerColorCollection colorCollection = colors.get(flower.color);
                            if (colorCollection == null) {
                                colorCollection = new FlowerColorCollection(species, flower.color);
                                colors.put(flower.color, colorCollection);
                            }
                            colorCollection.flowers.add(flower);
                        }

                        // notify flowers loaded

                        parentFragment.getActivity().runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        FlowerColorAdapter.this.colorCollections.addAll(colors.values());
                                        parentFragment.notifyFinishedLoading();
                                    }
                                }
                        );

                        return null;
                    }
                })
                .submit();
    }

    @NonNull
    @Override
    public FlowerColorCard onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View flower_view = inflater.inflate(R.layout.color_card, parent, false);

        // Return a new holder instance
        FlowerColorCard viewHolder = new FlowerColorCard(flower_view, this.variants_fmt_string);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FlowerColorCard holder, int position) {
        holder.setColorCollection(this.colorCollections.get(position));
    }

    @Override
    public int getItemCount() {
        return this.colorCollections.size();
    }

    static class FlowerColorCard extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView color;
        final TextView genotypes_counts_view;
        final String variants_fmt_string;

        FlowerColorCard(@NonNull View itemView, String variants_fmt_string) {
            super(itemView);
            this.icon = itemView.findViewById(R.id.flower_icon);
            this.color = itemView.findViewById(R.id.flower_color);
            this.genotypes_counts_view = itemView.findViewById(R.id.variants_heading);
            this.variants_fmt_string = variants_fmt_string;

        }

        void setColorCollection(final FlowerColorCollection colorCollection) {
            // set up the display
            int icon_id = icon.getContext()
                    .getResources()
                    .getIdentifier(colorCollection.icon_name,
                            "drawable",
                            icon.getContext().getPackageName());

            this.icon.setImageResource(icon_id);
            this.color.setText(colorCollection.color.name().toUpperCase());
            this.genotypes_counts_view.setText(
                    String.format(this.variants_fmt_string, colorCollection.flowers.size())
            );

            // add a listener to switch to a fragment with details
            this.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
        }
    }

    private static class FlowerColorCollection {
        final FlowerConstants.Species species;
        final FlowerConstants.Color color;
        final Set<FlowerDatabase.Flower> flowers;
        final String icon_name;

        FlowerColorCollection(FlowerConstants.Species species, FlowerConstants.Color color) {
            this.species = species;
            this.color = color;
            this.flowers = new HashSet<FlowerDatabase.Flower>();
            this.icon_name = String.format("%s_%s",
                    species.name().toLowerCase(),
                    color.name().toLowerCase());
        }
    }
}
