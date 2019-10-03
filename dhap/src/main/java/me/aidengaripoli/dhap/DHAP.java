package me.aidengaripoli.dhap;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import me.aidengaripoli.dhap.discovery.Discovery;
import me.aidengaripoli.dhap.discovery.callbacks.DiscoverDevicesCallbacks;
import me.aidengaripoli.dhap.discovery.callbacks.RefreshCensuslistCallbacks;
import me.aidengaripoli.dhap.display.Display;
import me.aidengaripoli.dhap.display.callbacks.GetDeviceInterfaceCallbacks;
import me.aidengaripoli.dhap.joining.Joining;
import me.aidengaripoli.dhap.joining.callbacks.ConnectToApCallbacks;
import me.aidengaripoli.dhap.joining.callbacks.JoinDeviceCallbacks;
import me.aidengaripoli.dhap.joining.callbacks.SendCredentialsCallbacks;

public class DHAP {
    private Discovery discovery;
    private Display display;
    private Joining joining;

    public DHAP(Context context) {
        discovery = new Discovery(context);
        display = new Display(context);
        joining = new Joining(context);
    }

    public void fetchDeviceInterface(Device device, GetDeviceInterfaceCallbacks callbacks) {
        if (device.getStatus() == 1) {
            display.fetchDeviceInterface(device, callbacks);
        } else {
            callbacks.displayTimeoutFailure();
        }
    }

    public void joinDevice(String networkSSID, String networkPassword, String deviceSSID, String devicePassword, String name, String location, JoinDeviceCallbacks callback) {
        joining.joinDevice(networkSSID, networkPassword, deviceSSID, devicePassword, name, location, callback);
    }

    public void connectToAccessPoint(String SSID, String password, ConnectToApCallbacks callback) {
        joining.connectToAccessPoint(SSID, password, callback);
    }

    public void sendCredentials(String SSID, String password, String name, String location, SendCredentialsCallbacks callback) {
        joining.sendCredentials(SSID, password, name, location, callback);
    }

    public void discoverDevices(DiscoverDevicesCallbacks callbacks) {
        discovery.discoverDevices(callbacks);
    }

    public void clearSavedDevices() {
        discovery.clearSavedDevices();
    }

    public ArrayList<Device> getSavedDevices() {
        return discovery.getSavedDevices();
    }

    public void refreshCensusList(List<Device> devices, RefreshCensuslistCallbacks callbacks){
        discovery.refreshCensusList(devices, callbacks);
    }

    public void discoverDebugDevices(DiscoverDevicesCallbacks callbacks) {
        discovery.discoverDebugDevices(callbacks);
    }

    public void removeDevice(Device device) {
        discovery.removeDevice(device);
    }
}
