package me.aidengaripoli.dhap.display.elements;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;

import me.aidengaripoli.dhap.R;

public class ButtonGroupFragment extends BaseElementFragment {

    public static final String BUTTON_GROUP = "buttongroup";
    private static final String ARG_BUTTON_LABELS = "buttonlabels";

    private String[] buttonLabels;

    public ButtonGroupFragment() {}

    public static ButtonGroupFragment newInstance(ArrayList<String> displaySettings) {
        ButtonGroupFragment fragment = new ButtonGroupFragment();

        Bundle args = new Bundle();

        String label = displaySettings.get(ARG_LABEL_INDEX);
        args.putString(ARG_LABEL, label);
        displaySettings.remove(ARG_LABEL_INDEX);

        String[] buttonLabels = displaySettings.toArray(new String[0]);
        args.putStringArray(ARG_BUTTON_LABELS, buttonLabels);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            buttonLabels = getArguments().getStringArray(ARG_BUTTON_LABELS);
            label = getArguments().getString(ARG_LABEL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_button_group_element, container, false);

        labelView = view.findViewById(R.id.buttonGroupLabel);

        addLabel();

        LinearLayout buttonLayout = view.findViewById(R.id.buttonGroup_layout);

        for (String buttonLabel : buttonLabels) {
            Button button = new Button(view.getContext());
            button.setText(buttonLabel);
            button.setOnClickListener(v -> sendMessage(buttonLabel));
            buttonLayout.addView(button);
        }

        return view;
    }

    @Override
    public void updateFragmentData(String value) {
        //No updates for this element.
    }
}
