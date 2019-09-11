package me.aidengaripoli.dhap;

import android.content.Context;

import me.aidengaripoli.dhap.discovery.Discovery;
import me.aidengaripoli.dhap.discovery.callbacks.DiscoveredDevicesCallbacks;
import me.aidengaripoli.dhap.display.Display;
import me.aidengaripoli.dhap.display.callbacks.DeviceUIActivityCallbacks;
import me.aidengaripoli.dhap.joining.Joining;
import me.aidengaripoli.dhap.joining.callbacks.BaseJoiningCallbacks;
import me.aidengaripoli.dhap.joining.callbacks.ConnectToNetworkCallbacks;

public class DHAP {
    private static final String TAG = DHAP.class.getSimpleName();

    private Discovery discovery;
    private Display display;
    private Joining joining;

    public DHAP(Context context) {
        discovery = new Discovery(context);
        display = new Display(context);
        joining = new Joining(context);
    }

    public void fetchDeviceInterface(Device device, DeviceUIActivityCallbacks callbacks) {
        display.fetchDeviceInterface(device, callbacks);
    }

    public void joinDevice(String networkSSID, String networkPassword, String deviceSSID, String devicePassword, ConnectToNetworkCallbacks callback) {
        joining.joinDevice(networkSSID, networkPassword, deviceSSID, devicePassword, callback);
    }

    public void connectToAccessPoint(String SSID, String password, ConnectToNetworkCallbacks callback) {
        joining.connectToAccessPoint(SSID, password, callback);
    }

    public void sendCredentials(String SSID, String password, BaseJoiningCallbacks callback) {
        joining.sendCredentials(SSID, password, callback);
    }

    public void discoverDevices(DiscoveredDevicesCallbacks callbacks) {
        discovery.discoverDevices(callbacks);
    }

    public void discoverDebugDevices(DiscoveredDevicesCallbacks callbacks) {
        discovery.discoverDebugDevices(callbacks);
    }
}
