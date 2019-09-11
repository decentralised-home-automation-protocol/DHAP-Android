package me.aidengaripoli.dhap.display.callbacks;

import android.content.Intent;

public interface DisplayCallbacks {
    void displayFailure();

    void deviceActivityIntent(Intent intent);
}
