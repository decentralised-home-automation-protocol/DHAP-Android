package me.aidengaripoli.dhap.discovery.callbacks;

import java.util.List;

import me.aidengaripoli.dhap.Device;

public interface DiscoverDevicesCallbacks {
    void discoveryFailure();

    void foundDevices(List<Device> devices);

    void noDevicesFound();
}
