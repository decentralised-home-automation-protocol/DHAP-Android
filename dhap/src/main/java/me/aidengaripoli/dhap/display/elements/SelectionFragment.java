package me.aidengaripoli.dhap.display.elements;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import me.aidengaripoli.dhap.R;

public class SelectionFragment extends BaseElementFragment implements
        AdapterView.OnItemSelectedListener {

    public static final String SELECTION = "selection";

    private static final String ARG_ITEMS = "items";

    private String[] items;
    private Spinner selection;
    private int position;

    public SelectionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param displaySettings Parameter 1.
     * @return A new instance of fragment SelectionFragment.
     */
    public static SelectionFragment newInstance(ArrayList<String> displaySettings) {
        SelectionFragment fragment = new SelectionFragment();

        Bundle args = new Bundle();
        args.putString(ARG_LABEL, displaySettings.get(ARG_LABEL_INDEX));
        displaySettings.remove(ARG_LABEL_INDEX);

        String[] values = displaySettings.toArray(new String[0]);
        args.putStringArray(ARG_ITEMS, values);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            label = getArguments().getString(ARG_LABEL);
            items = getArguments().getStringArray(ARG_ITEMS);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_selection_element, container, false);

        labelView = view.findViewById(R.id.selection_label);
        addLabel();

        selection = view.findViewById(R.id.selection_value);
        selection.setOnItemSelectedListener(this);
        selection.setAdapter(new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_dropdown_item_1line,
                items
        ));

        selection.setSelection(position);

        return view;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String value = (String) parent.getItemAtPosition(position);
        sendMessage(value);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void updateFragmentData(String value) {

    }
}
