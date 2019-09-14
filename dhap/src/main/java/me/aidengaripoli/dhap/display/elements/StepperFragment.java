package me.aidengaripoli.dhap.display.elements;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import me.aidengaripoli.dhap.R;

public class StepperFragment extends BaseElementFragment {

    public static final String STEPPER = "stepper";

    private static final String ARG_MIN = "min";
    private static final String ARG_MAX = "max";

    private static final int ARG_MIN_INDEX = 1;
    private static final int ARG_MAX_INDEX = 2;

    private TextView value;

    private int currentValue;
    private int max;
    private int min;

    public StepperFragment() {
    }

    public static StepperFragment newInstance(ArrayList<String> displaySettings) {
        StepperFragment fragment = new StepperFragment();

        Bundle args = new Bundle();
        args.putString(ARG_LABEL, displaySettings.get(ARG_LABEL_INDEX));
        args.putInt(ARG_MIN, Integer.parseInt(displaySettings.get(ARG_MIN_INDEX)));
        args.putInt(ARG_MAX, Integer.parseInt(displaySettings.get(ARG_MAX_INDEX)));

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            label = getArguments().getString(ARG_LABEL);
            min = getArguments().getInt(ARG_MIN);
            max = getArguments().getInt(ARG_MAX);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stepper_element, container, false);

        value = view.findViewById(R.id.plusMinusValue);
        Button plusButton = view.findViewById(R.id.plus);
        Button minusButton = view.findViewById(R.id.minus);
        labelView = view.findViewById(R.id.plusMinusLabel);
        addLabel();

        value.setText(String.valueOf(currentValue));

        plusButton.setOnClickListener(v -> changeValue(true));
        minusButton.setOnClickListener(v -> changeValue(false));

        return view;
    }


    private void changeValue(boolean plusButton) {
        if (plusButton) {
            if (currentValue < max)
                currentValue++;
        } else {
            if (currentValue > min)
                currentValue--;
        }
        value.setText(String.valueOf(currentValue));

        sendMessage(String.valueOf(currentValue));
    }

    @Override
    public void updateFragmentData(String value) {
        this.currentValue = Integer.parseInt(value);
        getActivity().runOnUiThread(() -> this.value.setText(String.valueOf(currentValue)));
    }
}
