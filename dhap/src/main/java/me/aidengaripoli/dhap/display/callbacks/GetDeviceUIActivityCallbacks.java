package me.aidengaripoli.dhap.display.callbacks;

import android.content.Intent;

public interface GetDeviceUIActivityCallbacks extends BaseCallback {

    void assetsFileFailure();

    void deviceActivityIntent(Intent intent);

}
