package me.aidengaripoli.dhapexample;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import me.aidengaripoli.dhap.Device;

public class ChangeHeaderFragment extends DialogFragment {
    private Device device;
    private EditText name;
    private EditText location;
    private OnChangeListener listener;

    private ChangeHeaderFragment(OnChangeListener listener){
        this.listener = listener;
    }

    static ChangeHeaderFragment newInstance(Device device, OnChangeListener listener) {
        ChangeHeaderFragment changeHeaderFragment = new ChangeHeaderFragment(listener);

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putParcelable("device", device);
        changeHeaderFragment.setArguments(args);

        return changeHeaderFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        device = getArguments().getParcelable("device");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.change_header, null)).setMessage(R.string.change_header)
                .setPositiveButton(R.string.submit, (dialogs, id) -> {
                    device.changeDeviceName(name.getText().toString());
                    device.changeDeviceLocation(location.getText().toString());
                    listener.headerChanged();
                })
                .setNegativeButton(R.string.cancel, (dialogs, id) -> {
                    // User cancelled the dialog
                });

        Dialog dialog = builder.create();
        dialog.show();
        name = dialog.findViewById(R.id.change_header_name);
        location = dialog.findViewById(R.id.change_header_location);

        name.setText(device.getName());
        location.setText(device.getLocation());
        return dialog;
    }

    public interface OnChangeListener {
        void headerChanged();
    }
}
