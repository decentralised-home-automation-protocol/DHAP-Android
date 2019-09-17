package me.aidengaripoli.dhap.display;

import android.os.Bundle;
import android.widget.ScrollView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import me.aidengaripoli.dhap.DHAP;
import me.aidengaripoli.dhap.Device;
import me.aidengaripoli.dhap.display.elements.OnElementCommandListener;
import me.aidengaripoli.dhap.status.StatusUpdates;

public class DeviceActivity extends AppCompatActivity implements OnElementCommandListener {

    private static final String DEVICE_INTENT_EXTRA = "device";

    private Device device;
    private StatusUpdates statusUpdates;
    private DHAP dhap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        device = getIntent().getParcelableExtra(DEVICE_INTENT_EXTRA);

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(device.getDeviceViewGroup(getSupportFragmentManager(), this));

        statusUpdates = new StatusUpdates(device);
        dhap = new DHAP(this);

        setContentView(scrollView);
    }

    @Override
    public void onElementCommand(String tag, String data) {
        if (!device.isDebugDevice()) {
            dhap.sendIoTCommand(tag, data, device);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!device.isDebugDevice()) {
            statusUpdates.requestStatusLease(10000, 1000, false);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!device.isDebugDevice()) {
            statusUpdates.leaveLease();
        }
    }
}
