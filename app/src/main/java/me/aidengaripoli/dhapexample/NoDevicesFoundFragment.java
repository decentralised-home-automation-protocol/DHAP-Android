package me.aidengaripoli.dhapexample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class NoDevicesFoundFragment extends Fragment {

    public NoDevicesFoundFragment() {
    }

    static NoDevicesFoundFragment newInstance() {
        return new NoDevicesFoundFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_no_devices_found, container, false);
    }
}
