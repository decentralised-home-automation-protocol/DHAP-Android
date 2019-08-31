package me.aidengaripoli.dhap.display.elements;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import me.aidengaripoli.dhap.R;

public class SwitchToggleFragment extends BaseElementFragment implements
        CompoundButton.OnCheckedChangeListener {

    public static final String SWITCH_TOGGLE = "switch_toggle";

    private Switch toggleSwitch;

    private boolean isChecked;

    public SwitchToggleFragment() {}

    public static SwitchToggleFragment newInstance(ArrayList<String> displaySettings) {
        SwitchToggleFragment fragment = new SwitchToggleFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_switch_toggle_element, container, false);

        toggleSwitch = rootView.findViewById(R.id.toggle_switch);
        toggleSwitch.setOnCheckedChangeListener(this);
        toggleSwitch.setChecked(isChecked);

        return rootView;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        this.isChecked = isChecked;
    }

    @Override
    void updateFragmentData() {

    }
}
