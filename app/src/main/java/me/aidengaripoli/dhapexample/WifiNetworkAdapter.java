package me.aidengaripoli.dhapexample;

import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WifiNetworkAdapter extends
        RecyclerView.Adapter<WifiNetworkAdapter.WifiNetworkViewHolder> {

    private List<ScanResult> scanResults;
    private OnWifiNetworkClickListener listener;
    private String selectedNetwork;

    WifiNetworkAdapter(List<ScanResult> scanResults, OnWifiNetworkClickListener listener) {
        this.scanResults = scanResults;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WifiNetworkViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.access_point_view, viewGroup, false);

        return new WifiNetworkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WifiNetworkViewHolder wifiNetworkViewHolder, int position) {
        ScanResult wifiNetwork = scanResults.get(position);
        wifiNetworkViewHolder.bind(wifiNetwork);

        wifiNetworkViewHolder.itemView.setOnClickListener(v -> {
            selectedNetwork = wifiNetwork.SSID;
            notifyDataSetChanged();

            if (listener != null) {
                listener.onWifiNetworkSelected(wifiNetwork);
            }
        });

        wifiNetworkViewHolder.wifiIcon.setImageResource(
                wifiNetwork.SSID.equals(selectedNetwork)
                        ? R.drawable.ic_signal_wifi_4_bar_lock_blue_800_24dp
                        : R.drawable.ic_signal_wifi_4_bar_lock_grey_900_24dp
        );

    }

    @Override
    public int getItemCount() {
        return scanResults.size();
    }

    static class WifiNetworkViewHolder extends RecyclerView.ViewHolder {

        TextView wifiNetworkSSID;
        ImageView wifiIcon;

        WifiNetworkViewHolder(View itemView) {
            super(itemView);

            wifiNetworkSSID = itemView.findViewById(R.id.text_view_wifi_network_name);
            wifiIcon = itemView.findViewById(R.id.image_view_wifi_icon);
        }

        void bind(ScanResult scanResult) {
            wifiNetworkSSID.setText(scanResult.SSID);
        }
    }
}
