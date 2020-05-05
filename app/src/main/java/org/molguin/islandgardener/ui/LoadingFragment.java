package org.molguin.islandgardener.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.molguin.islandgardener.databinding.LoadingFragmentBinding;

public class LoadingFragment extends Fragment {
    public LoadingFragment() {
    }

    public static LoadingFragment newInstance() {
        return new LoadingFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return LoadingFragmentBinding.inflate(inflater).getRoot();

    }
}
