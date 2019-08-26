package me.aidengaripoli.dhap.elements;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import me.aidengaripoli.dhap.R;

public class StatusFragment extends BaseElementFragment {

    public static final String STATUS = "status";
    private TextView statusLabel;
    private String statusValue;

    public StatusFragment() {}

    public static StatusFragment newInstance(ArrayList<String> displaySettings) {
        StatusFragment fragment = new StatusFragment();

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_status_element, container, false);

        labelView = view.findViewById(R.id.status_labels);
        addLabel();

        TextView statusValueView = view.findViewById(R.id.status_value);
        statusValueView.setText(statusValue);
        return view;
    }


    @Override
    void updateFragmentData() {

    }
}
