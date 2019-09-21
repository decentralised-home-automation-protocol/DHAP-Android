package me.aidengaripoli.dhap;

import android.content.Context;

import me.aidengaripoli.dhap.discovery.Discovery;
import me.aidengaripoli.dhap.discovery.callbacks.DiscoverDevicesCallbacks;
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
        if(device.isActive == 1){
            display.fetchDeviceInterface(device, callbacks);
        } else {
            callbacks.displayTimeoutFailure();
        }
    }

    public void joinDevice(String networkSSID, String networkPassword, String deviceSSID, String devicePassword, JoinDeviceCallbacks callback) {
        joining.joinDevice(networkSSID, networkPassword, deviceSSID, devicePassword, callback);
    }

    public void connectToAccessPoint(String SSID, String password, ConnectToApCallbacks callback) {
        joining.connectToAccessPoint(SSID, password, callback);
    }

    public void sendCredentials(String SSID, String password, SendCredentialsCallbacks callback) {
        joining.sendCredentials(SSID, password, callback);
    }

    public void discoverDevices(DiscoverDevicesCallbacks callbacks) {
        discovery.discoverDevices(callbacks);
    }

    public void clearSavedDevices(){
        discovery.clearSavedDevices();
    }

    public void discoverDebugDevices(DiscoverDevicesCallbacks callbacks) {
        discovery.discoverDebugDevices(callbacks);
    }

    public void removeDevice(Device device) {
        discovery.removeDevice(device);
    }
}
