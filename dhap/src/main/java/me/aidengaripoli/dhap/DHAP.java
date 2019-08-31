package me.aidengaripoli.dhap;

import android.content.Context;
import android.util.Log;

import me.aidengaripoli.dhap.discovery.Discovery;
import me.aidengaripoli.dhap.discovery.callbacks.GetDiscoveredDevicesCallbacks;
import me.aidengaripoli.dhap.display.Display;
import me.aidengaripoli.dhap.display.callbacks.GetDeviceUIActivityCallbacks;
import me.aidengaripoli.dhap.joining.Joining;
import me.aidengaripoli.dhap.joining.callbacks.ConnectToNetworkCallback;

public class DHAP {

    private static final String TAG = DHAP.class.getSimpleName();

    private Context context;
    private Discovery discovery;
    private Display display;
    private Joining joining;

    public DHAP(Context context) {
        this.context = context;
        discovery = new Discovery(context);
        display = new Display(context);
        joining = new Joining(context);
    }

    public void fetchDeviceInterface(String deviceName, boolean useAssetsFolder, GetDeviceUIActivityCallbacks callbacks) {
        display.fetchDeviceInterface(deviceName, useAssetsFolder, callbacks);
    }

    public void joinDevice() {
        joining.joinDevice("TP-LINK_AE045A", "0358721743", "ESPsoftAP_01", "passforap", new ConnectToNetworkCallback() {
            @Override
            public void networkNotFound() {
                Log.e(TAG, "networkNotFound");
            }

            @Override
            public void success() {
                Log.e(TAG, "success" );
            }

            @Override
            public void failure() {
                Log.e(TAG, "failure");
            }
        });
    }

    public void startDiscovery(GetDiscoveredDevicesCallbacks callbacks) {
        discovery.discoverDevices(callbacks);
    }
}
