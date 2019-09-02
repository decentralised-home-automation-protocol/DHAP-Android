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

public class DeviceActivity extends AppCompatActivity implements OnElementCommandListener {

    private static final String TAG = DeviceActivity.class.getSimpleName();

    private static final String DEVICE_INTENT_EXTRA = "device";

    private Device device;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        device = getIntent().getParcelableExtra(DEVICE_INTENT_EXTRA);

        FragmentManager fragmentManager = getSupportFragmentManager();
        DeviceDescriptionLayout layout = new DeviceDescriptionLayout(fragmentManager, this);
        ViewGroup deviceLayout = layout.create(device.getDeviceDescription());

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(deviceLayout);

        UdpPacketSender.getInstance().sendUdpPacketToIP("500|10000,2000,F", device.getIpAddress().getHostAddress());

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
        UdpPacketSender.getInstance().addPacketListener(device.getDeviceDescription());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        UdpPacketSender.getInstance().removePacketListener(device.getDeviceDescription());
    }
}
