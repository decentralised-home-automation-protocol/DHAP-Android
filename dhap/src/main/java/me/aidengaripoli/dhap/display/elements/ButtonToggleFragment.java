package me.aidengaripoli.dhap.display.elements;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import me.aidengaripoli.dhap.R;

public class ButtonToggleFragment extends BaseElementFragment implements
        View.OnClickListener {

    public static final String BUTTON_TOGGLE = "buttontoggle";

    private static final String ARG_POS_LABEL = "pos_label";
    private static final String ARG_NEG_LABEL = "neg_label";

    private static final int ARG_POS_LABEL_INDEX = 1;
    private static final int ARG_NEG_LABEL_INDEX = 2;

    private String buttonPosLabel;
    private String buttonNegLabel;
    private boolean state;

    private Button toggleButton;

    public ButtonToggleFragment() {}

    public static ButtonToggleFragment newInstance(ArrayList<String> displaySettings) {
        ButtonToggleFragment fragment = new ButtonToggleFragment();

        Bundle args = new Bundle();
        args.putString(ARG_LABEL,  displaySettings.get(ARG_LABEL_INDEX));
        args.putString(ARG_POS_LABEL, displaySettings.get(ARG_POS_LABEL_INDEX));
        args.putString(ARG_NEG_LABEL, displaySettings.get(ARG_NEG_LABEL_INDEX));

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            label = getArguments().getString(ARG_LABEL);
            buttonPosLabel = getArguments().getString(ARG_POS_LABEL);
            buttonNegLabel = getArguments().getString(ARG_NEG_LABEL);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_button_toggle_element, container, false);

        labelView = view.findViewById(R.id.toggle_label);
        addLabel();

        toggleButton = view.findViewById(R.id.toggle_button);
        toggleButton.setText(state ? buttonPosLabel : buttonNegLabel);
        toggleButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        state = !state;
        toggleButton.setText(state ? buttonPosLabel : buttonNegLabel);
        sendMessage(state ? buttonPosLabel : buttonNegLabel);
    }

    @Override
    public void updateFragmentData(String value) {
        this.state = value.equals("true");
        getActivity().runOnUiThread(() -> toggleButton.setText(state ? buttonPosLabel : buttonNegLabel));
    }
}
