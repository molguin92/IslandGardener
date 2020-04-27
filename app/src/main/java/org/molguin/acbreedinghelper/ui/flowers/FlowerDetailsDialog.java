package org.molguin.acbreedinghelper.ui.flowers;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.molguin.acbreedinghelper.R;
import org.molguin.flowers.Flower;

public class FlowerDetailsDialog extends DialogFragment {

    final Flower flower;

    FlowerDetailsDialog(Flower flower) {
        super();
        this.flower = flower;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View v = inflater.inflate(R.layout.flower_dialog, null);
        builder.setView(v)
                // Add action buttons
                .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FlowerDetailsDialog.this.getDialog().cancel();
                    }
                });
        AlertDialog d = builder.create();
        this.onViewCreated(v, savedInstanceState);
        return d;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TextView colorView = view.findViewById(R.id.flower_color);
        TextView originView = view.findViewById(R.id.flower_origin);
        TextView genotypeView = view.findViewById(R.id.flower_genotype);
        ImageView iconView = view.findViewById(R.id.flowerIcon);

        int icon_id = view.getContext()
                .getResources()
                .getIdentifier(flower.props.icon_name, "drawable", view.getContext().getPackageName());

        colorView.setText(this.flower.props.color);
        originView.setText(this.flower.props.origin);
        genotypeView.setText(this.flower.props.genotype);
        iconView.setImageResource(icon_id);
    }
}
