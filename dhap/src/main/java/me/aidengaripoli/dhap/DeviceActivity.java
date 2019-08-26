package me.aidengaripoli.dhap;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import me.aidengaripoli.dhap.elements.OnElementCommandListener;

public class DeviceActivity extends AppCompatActivity implements OnElementCommandListener {

    private static final String TAG = DeviceActivity.class.getSimpleName();

    private static final String DEVICE_INTENT_EXTRA = "deviceDescription";

    private DeviceDescription device;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        device = getIntent().getParcelableExtra(DEVICE_INTENT_EXTRA);

        FragmentManager fragmentManager = getSupportFragmentManager();
        DeviceDescriptionLayout layout = new DeviceDescriptionLayout(fragmentManager, this);
        ViewGroup deviceLayout = layout.create(device);

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(deviceLayout);

        setContentView(scrollView);
    }

    @Override
    public void onElementCommand(String tag, String data) {
        Log.d(TAG, "Received " + data + " from " + tag);
        device.executeCommand(tag, data);
    }
}
