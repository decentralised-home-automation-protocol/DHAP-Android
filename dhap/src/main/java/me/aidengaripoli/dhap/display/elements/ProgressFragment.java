package me.aidengaripoli.dhap.display.elements;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import me.aidengaripoli.dhap.R;

public class ProgressFragment extends BaseElementFragment {

    public static final String PROGRESS = "progress";

    private ProgressBar progressValue;

    public ProgressFragment() {
    }

    public static ProgressFragment newInstance(ArrayList<String> displaySettings) {
        ProgressFragment fragment = new ProgressFragment();

        Bundle args = new Bundle();
        args.putString(ARG_LABEL, displaySettings.get(ARG_LABEL_INDEX));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            label = getArguments().getString(ARG_LABEL);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_progress_element, container, false);

        labelView = view.findViewById(R.id.progress_label);
        addLabel();

        progressValue = view.findViewById(R.id.progress_value);

        return view;
    }

    @Override
    public void updateFragmentData(String value) {
        int progress = Integer.parseInt(value);
        getActivity().runOnUiThread(() -> this.progressValue.setProgress(progress));
    }
}
