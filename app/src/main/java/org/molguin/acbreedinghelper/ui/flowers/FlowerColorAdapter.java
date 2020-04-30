package org.molguin.acbreedinghelper.ui.flowers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import org.molguin.acbreedinghelper.R;
import org.molguin.flowers.FlowerConstants;
import org.molguin.flowers.FlowerDatabase;
import org.molguin.utils.Callback;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FlowerColorAdapter extends RecyclerView.Adapter<FlowerColorAdapter.FlowerColorCard> {
    final SortedList<FlowerColorCollection> colorCollections;

    public FlowerColorAdapter(final FlowerConstants.Species species,
                              FlowerDatabase db,
                              Fragment parentFragment) {
        super();
        this.colorCollections =
                new SortedList<FlowerColorCollection>(
                        FlowerColorCollection.class,
                        new SortedList.Callback<FlowerColorCollection>() {
                            @Override
                            public void onInserted(int position, int count) {
                                for (int i = position; i < position + count; i++)
                                    FlowerColorAdapter.this.notifyItemInserted(i);
                            }

                            @Override
                            public void onRemoved(int position, int count) {
                                for (int i = 0; i < count; i++)
                                    FlowerColorAdapter.this.notifyItemRemoved(position);
                            }

                            @Override
                            public void onMoved(int fromPosition, int toPosition) {
                                FlowerColorAdapter.this.notifyItemMoved(fromPosition, toPosition);
                            }

                            @Override
                            public int compare(FlowerColorCollection o1, FlowerColorCollection o2) {
                                return o1.color.compareTo(o2.color);
                            }

                            @Override
                            public void onChanged(int position, int count) {
                                for (int i = position; i < position + count; i++)
                                    FlowerColorAdapter.this.notifyItemChanged(i);
                            }

                            @Override
                            public boolean areContentsTheSame(FlowerColorCollection oldItem,
                                                              FlowerColorCollection newItem) {
                                return (oldItem.color == newItem.color) &&
                                        (oldItem.flowers.equals(newItem.flowers));
                            }

                            @Override
                            public boolean areItemsTheSame(FlowerColorCollection item1,
                                                           FlowerColorCollection item2) {
                                return item1.color == item2.color;
                            }
                        });

        // asynchronously load flowers
        final MutableLiveData<Collection<FlowerColorCollection>> collectionLiveData =
                new MutableLiveData<Collection<FlowerColorCollection>>();

        collectionLiveData.observe(parentFragment.getViewLifecycleOwner(),
                new Observer<Collection<FlowerColorCollection>>() {
                    @Override
                    public void onChanged(Collection<FlowerColorCollection> flowerColorCollections) {
                        FlowerColorAdapter.this.colorCollections.addAll(flowerColorCollections);
                        collectionLiveData.removeObserver(this);
                    }
                });

        db.makeQuery()
                .add(species)
                .addCallback(new Callback<Collection<FlowerDatabase.Flower>, Void>() {
                    @Override
                    public Void apply(Collection<FlowerDatabase.Flower> flowers) {
                        Map<FlowerConstants.Color, FlowerColorCollection> colors =
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
                        collectionLiveData.postValue(colors.values());
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
        FlowerColorCard viewHolder = new FlowerColorCard(flower_view);
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
        final TextView variant_counts;

        FlowerColorCard(@NonNull View itemView) {
            super(itemView);
            this.icon = itemView.findViewById(R.id.flower_icon);
            this.color = itemView.findViewById(R.id.flower_color);
            this.variant_counts = itemView.findViewById(R.id.flower_genotypes_count);

        }

        void setColorCollection(final FlowerColorCollection colorCollection) {
            int icon_id = icon.getContext()
                    .getResources()
                    .getIdentifier(colorCollection.icon_name,
                            "drawable",
                            icon.getContext().getPackageName());

            this.icon.setImageResource(icon_id);
            this.color.setText(colorCollection.color.name().toUpperCase());
            this.variant_counts.setText(String.valueOf(colorCollection.flowers.size()));
        }
    }

    private static class FlowerColorCollection {
        final FlowerConstants.Color color;
        final Set<FlowerDatabase.Flower> flowers;
        final String icon_name;

        FlowerColorCollection(FlowerConstants.Species species, FlowerConstants.Color color) {
            this.color = color;
            this.flowers = new HashSet<FlowerDatabase.Flower>();
            this.icon_name = String.format("%s_%s",
                    species.name().toLowerCase(),
                    color.name().toLowerCase());
        }
    }
}
