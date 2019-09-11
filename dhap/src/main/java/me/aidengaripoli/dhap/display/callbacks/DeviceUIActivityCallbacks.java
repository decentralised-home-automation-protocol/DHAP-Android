package me.aidengaripoli.dhap.display.callbacks;

import android.content.Intent;

public interface DeviceUIActivityCallbacks extends BaseDisplayCallbacks {

    void assetsFileFailure();

    void deviceActivityIntent(Intent intent);

}
