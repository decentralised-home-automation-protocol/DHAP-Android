package me.aidengaripoli.dhap.display;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import me.aidengaripoli.dhap.Device;
import me.aidengaripoli.dhap.UdpPacketSender;
import me.aidengaripoli.dhap.display.elements.OnElementCommandListener;
import me.aidengaripoli.dhap.status.StatusUpdates;

public class DeviceActivity extends AppCompatActivity implements OnElementCommandListener {

    private static final String TAG = DeviceActivity.class.getSimpleName();

    private static final String DEVICE_INTENT_EXTRA = "device";

    private Device device;
    private StatusUpdates statusUpdates;
    private UdpPacketSender udpPacketSender;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        device = getIntent().getParcelableExtra(DEVICE_INTENT_EXTRA);

        FragmentManager fragmentManager = getSupportFragmentManager();
        DeviceDescriptionLayout layout = new DeviceDescriptionLayout(fragmentManager, this);
        ViewGroup deviceLayout = layout.create(device.getDeviceDescription());

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(deviceLayout);

        statusUpdates = new StatusUpdates(device);
        udpPacketSender = UdpPacketSender.getInstance();

        setContentView(scrollView);
    }

    @Override
    public void onElementCommand(String tag, String data) {
        Log.d(TAG, "Received " + data + " from " + tag);
        device.getDeviceDescription().executeCommand(tag, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        statusUpdates.requestStatusLease(10000, 1000, false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        statusUpdates.leaveLease();
    }
}
