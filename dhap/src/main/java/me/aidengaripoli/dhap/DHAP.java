package me.aidengaripoli.dhap;

import android.content.Context;

import me.aidengaripoli.dhap.discovery.Discovery;
import me.aidengaripoli.dhap.discovery.callbacks.GetDiscoveredDevicesCallbacks;
import me.aidengaripoli.dhap.display.Display;
import me.aidengaripoli.dhap.display.callbacks.GetDeviceUIActivityCallbacks;

public class DHAP {

    private static final String TAG = DHAP.class.getSimpleName();

    private Context context;
    private Discovery discovery;
    private Display display;

    public DHAP(Context context) {
        this.context = context;
        discovery = new Discovery(context);
        display = new Display(context);
    }

    public void fetchDeviceInterface(String deviceName, boolean useAssetsFolder, GetDeviceUIActivityCallbacks callbacks) {
        display.fetchDeviceInterface(deviceName, useAssetsFolder, callbacks);
    }

    public void startDiscovery(GetDiscoveredDevicesCallbacks callbacks) {
        discovery.discoverDevices(callbacks);
    }
}
