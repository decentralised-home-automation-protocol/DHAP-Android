package me.aidengaripoli.dhapexample;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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

    DiscoveredDeviceAdapter(List<Device> devices, OnDeviceClicked listener) {
        mDevices = devices;
        mListener = listener;
    }

    public interface OnDeviceClicked {
        void onDeviceClicked(Device device);
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
                mListener.onDeviceClicked(device);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }

    static class DiscoveredDeviceViewHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private TextView location;
        private Button remove;
        private ImageView noResponse;

        DiscoveredDeviceViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.text_view_device_name);
            location = itemView.findViewById(R.id.text_view_device_room);
            remove = itemView.findViewById(R.id.remove_button);
            noResponse = itemView.findViewById(R.id.warning_image);
        }

        void bind(Device device) {
            name.setText(device.getName());
            location.setText(device.getLocation());
            remove.setTag(device);
            noResponse.setVisibility(device.getStatus() == 1 ? View.INVISIBLE : View.VISIBLE);
        }
    }
}
