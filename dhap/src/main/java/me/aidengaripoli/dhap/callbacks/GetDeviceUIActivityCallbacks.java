package me.aidengaripoli.dhap.callbacks;

import android.content.Intent;

public interface GetDeviceUIActivityCallbacks extends BaseCallback {

    void assetsFileFailure();

    void deviceActivityIntent(Intent intent);

}
