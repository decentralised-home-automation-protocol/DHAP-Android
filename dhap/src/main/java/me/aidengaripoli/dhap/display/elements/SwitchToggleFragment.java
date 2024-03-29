package me.aidengaripoli.dhap.display.elements;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import me.aidengaripoli.dhap.R;

public class SwitchToggleFragment extends BaseElementFragment implements
        View.OnClickListener {

    public static final String SWITCH_TOGGLE = "switchtoggle";

    private Switch toggleSwitch;

    private boolean isChecked;

    public SwitchToggleFragment() {
    }

    public static SwitchToggleFragment newInstance(ArrayList<String> displaySettings) {

        SwitchToggleFragment fragment = new SwitchToggleFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LABEL, displaySettings.get(ARG_LABEL_INDEX));
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            label = getArguments().getString(ARG_LABEL);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_switch_toggle_element, container, false);

        toggleSwitch = rootView.findViewById(R.id.toggle_switch);
        toggleSwitch.setOnClickListener(this);
        toggleSwitch.setChecked(isChecked);
        toggleSwitch.setText(label);

        return rootView;
    }

    @Override
    public void updateFragmentData(String value) {
        isChecked = value.equals("true");
        getActivity().runOnUiThread(() -> toggleSwitch.setChecked(isChecked));
    }

    @Override
    public void onClick(View v) {
        isChecked = !isChecked;
        sendMessage(String.valueOf(isChecked));
    }
}
