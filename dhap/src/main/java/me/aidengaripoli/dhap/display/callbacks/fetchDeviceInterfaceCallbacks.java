package me.aidengaripoli.dhap.display.callbacks;

import android.content.Intent;

public interface fetchDeviceInterfaceCallbacks {
    void invalidDisplayXmlFailure();

    void deviceActivityIntent(Intent intent);
}
