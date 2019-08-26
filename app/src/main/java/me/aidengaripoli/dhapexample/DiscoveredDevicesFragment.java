package me.aidengaripoli.dhapexample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import me.aidengaripoli.dhap.Device;
import me.aidengaripoli.dhap.DeviceActivity;

public class DiscoveredDevicesFragment extends Fragment implements
        DiscoveredDeviceAdapter.OnDeviceClicked {

    private static final String TAG = DiscoveredDevicesFragment.class.getSimpleName();

    private static final String ARG_DEVICES = "devices";

    private RecyclerView discoveredDevicesRecyclerView;
    private RecyclerView.Adapter adapter;

    private ArrayList<Device> mDevices = new ArrayList<>();

    public DiscoveredDevicesFragment() {}

    public static DiscoveredDevicesFragment newInstance(ArrayList<Device> devices) {
        DiscoveredDevicesFragment fragment = new DiscoveredDevicesFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_DEVICES, devices);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDevices = getArguments().getParcelableArrayList(ARG_DEVICES);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discovered_devices_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        discoveredDevicesRecyclerView = view.findViewById(R.id.recycler_view_discovered_devices);
        discoveredDevicesRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        discoveredDevicesRecyclerView.setLayoutManager(layoutManager);

        adapter = new DiscoveredDeviceAdapter(mDevices, this);
        discoveredDevicesRecyclerView.setAdapter(adapter);

        DividerItemDecoration itemDecoration = new DividerItemDecoration(
                discoveredDevicesRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL
        );
        discoveredDevicesRecyclerView.addItemDecoration(itemDecoration);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void displayDeviceUi(String xml) {
        Intent intent = new Intent(getContext(), DeviceActivity.class);
        intent.putExtra("xml", xml);
        startActivity(intent);
    }

    @Override
    public void onDeviceClicked(String ip) {
        Log.d(TAG, "OnDeviceClicked");
    }
}
