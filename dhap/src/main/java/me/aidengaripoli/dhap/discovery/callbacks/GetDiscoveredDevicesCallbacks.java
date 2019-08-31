package me.aidengaripoli.dhap.callbacks.discovery;

import java.util.List;

import me.aidengaripoli.dhap.Device;

public interface GetDiscoveredDevicesCallbacks extends BaseCallback {

    /**
     *
     * @param devices
     */
    void foundDevices(List<Device> devices);

    /**
     *
     */
    void noDevicesFound();

}
