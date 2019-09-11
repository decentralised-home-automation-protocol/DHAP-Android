package me.aidengaripoli.dhap;

import android.content.Context;

import me.aidengaripoli.dhap.discovery.Discovery;
import me.aidengaripoli.dhap.discovery.callbacks.DiscoveryCallbacks;
import me.aidengaripoli.dhap.display.Display;
import me.aidengaripoli.dhap.display.callbacks.DisplayCallbacks;
import me.aidengaripoli.dhap.joining.Joining;
import me.aidengaripoli.dhap.joining.callbacks.JoiningCallbacks;

public class DHAP {
    private Discovery discovery;
    private Display display;
    private Joining joining;

    public DHAP(Context context) {
        discovery = new Discovery(context);
        display = new Display(context);
        joining = new Joining(context);
    }

    public void fetchDeviceInterface(Device device, DisplayCallbacks callbacks) {
        display.fetchDeviceInterface(device, callbacks);
    }

    public void joinDevice(String networkSSID, String networkPassword, String deviceSSID, String devicePassword, JoiningCallbacks callback) {
        joining.joinDevice(networkSSID, networkPassword, deviceSSID, devicePassword, callback);
    }

    public void connectToAccessPoint(String SSID, String password, JoiningCallbacks callback) {
        joining.connectToAccessPoint(SSID, password, callback);
    }

    public void sendCredentials(String SSID, String password, JoiningCallbacks callback) {
        joining.sendCredentials(SSID, password, callback);
    }

    public void discoverDevices(DiscoveryCallbacks callbacks) {
        discovery.discoverDevices(callbacks);
    }

    public void discoverDebugDevices(DiscoveryCallbacks callbacks) {
        discovery.discoverDebugDevices(callbacks);
    }
}
