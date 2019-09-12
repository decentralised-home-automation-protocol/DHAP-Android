package me.aidengaripoli.dhap.display.callbacks;

import android.content.Intent;

public interface FetchDeviceInterfaceCallbacks {
    void invalidDisplayXmlFailure();

    void displayTimeoutFailure();

    void deviceActivityIntent(Intent intent);
}
