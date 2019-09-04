package me.aidengaripoli.dhap;

import android.content.Context;

import me.aidengaripoli.dhap.discovery.Discovery;
import me.aidengaripoli.dhap.discovery.callbacks.GetDiscoveredDevicesCallbacks;
import me.aidengaripoli.dhap.display.Display;
import me.aidengaripoli.dhap.display.callbacks.GetDeviceUIActivityCallbacks;
import me.aidengaripoli.dhap.joining.Joining;
import me.aidengaripoli.dhap.joining.callbacks.BaseCallback;
import me.aidengaripoli.dhap.joining.callbacks.ConnectToNetworkCallback;

public class DHAP {
    private static final String TAG = DHAP.class.getSimpleName();

    private Discovery discovery;
    private Display display;
    private Joining joining;

    public DHAP(Context context) {
        discovery = new Discovery();
        display = new Display(context);
        joining = new Joining(context);
    }

    public void fetchDeviceInterface(Device device, GetDeviceUIActivityCallbacks callbacks) {
        display.fetchDeviceInterface(device, callbacks);
    }

    public void joinDevice(String networkSSID, String networkPassword, String deviceSSID, String devicePassword, ConnectToNetworkCallback callback) {
        joining.joinDevice(networkSSID, networkPassword, deviceSSID, devicePassword, callback);
    }

    public void connectToAccessPoint(String SSID, String password, ConnectToNetworkCallback callback) {
        joining.connectToAccessPoint(SSID, password, callback);
    }

    public void sendCredentials(String SSID, String password, BaseCallback callback) {
        joining.sendCredentials(SSID, password, callback);
    }

    public void discoverDevices(GetDiscoveredDevicesCallbacks callbacks) {
        discovery.discoverDevices(callbacks);
    }
}
