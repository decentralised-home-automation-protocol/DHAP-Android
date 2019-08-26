package me.aidengaripoli.dhapexample;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import me.aidengaripoli.dhap.Device;

public class DiscoveredDeviceAdapter extends
        RecyclerView.Adapter<DiscoveredDeviceAdapter.DiscoveredDeviceViewHolder> {

    private static final String TAG = DiscoveredDeviceAdapter.class.getSimpleName();

    private List<Device> mDevices;

    private OnDeviceClicked mListener;

    public DiscoveredDeviceAdapter(List<Device> devices, OnDeviceClicked listener) {
        mDevices = devices;
        mListener = listener;
    }

    public interface OnDeviceClicked {
        void onDeviceClicked(String ip);
    }

    @NonNull
    @Override
    public DiscoveredDeviceViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.discovered_device_view, viewGroup, false);

        return new DiscoveredDeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiscoveredDeviceViewHolder discoveredDeviceViewHolder, int position) {
        Device device = mDevices.get(position);
        discoveredDeviceViewHolder.bind(device);

        discoveredDeviceViewHolder.itemView.setOnClickListener(v -> {
            Log.d(TAG, "Device clicked.");
            if (mListener != null) {
                mListener.onDeviceClicked(device.getIpAddress().getHostAddress());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }

    public static class DiscoveredDeviceViewHolder extends RecyclerView.ViewHolder {

        private TextView MACAddress;
        private TextView ipAddress;

        public DiscoveredDeviceViewHolder(@NonNull View itemView) {
            super(itemView);

            MACAddress = itemView.findViewById(R.id.text_view_device_mac);
            ipAddress = itemView.findViewById(R.id.text_view_device_ip);
        }

        public void bind(Device device) {
            MACAddress.setText(device.getMacAddress());
            ipAddress.setText(device.getIpAddress().toString());
        }
    }

}