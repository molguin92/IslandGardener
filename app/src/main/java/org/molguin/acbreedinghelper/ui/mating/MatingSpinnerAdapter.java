package org.molguin.acbreedinghelper.ui.mating;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.molguin.acbreedinghelper.R;
import org.molguin.acbreedinghelper.flowers.FuzzyFlower;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MatingSpinnerAdapter extends BaseAdapter {
    final List<FuzzyFlower> flowers;

    MatingSpinnerAdapter(Collection<FuzzyFlower> flowers) {
        this.flowers = new ArrayList<FuzzyFlower>(flowers);
        Collections.sort(this.flowers);
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
        // TODO: viewholder pattern

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.color_holder, null);

        FuzzyFlower f = this.flowers.get(position);

        TextView colorview = view.findViewById(R.id.flower_color);
        ImageView iconView = view.findViewById(R.id.flower_icon);
        TextView variantsView = view.findViewById(R.id.variants_heading);
        String variants_fmt_string = parent.getContext()
                .getResources()
                .getString(R.string.variants_fmt_string);

        colorview.setText(f.getColor().name());
        variantsView.setText(
                String.format(variants_fmt_string, f.getVariantProbs().size())
        );

        int icon_id = parent.getContext()
                .getResources()
                .getIdentifier(f.getIconName(),
                        "drawable",
                        parent.getContext().getPackageName());

        iconView.setImageResource(icon_id);

        return view;
    }
}
