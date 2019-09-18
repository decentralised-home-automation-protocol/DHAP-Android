package me.aidengaripoli.dhap.display;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import me.aidengaripoli.dhap.DHAP;
import me.aidengaripoli.dhap.Device;
import me.aidengaripoli.dhap.display.elements.OnElementCommandListener;
import me.aidengaripoli.dhap.status.StatusLeaseCallbacks;

public class DeviceActivity extends AppCompatActivity implements OnElementCommandListener {

    private static final String DEVICE_INTENT_EXTRA = "device";

    private Device device;
    private DHAP dhap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        device = getIntent().getParcelableExtra(DEVICE_INTENT_EXTRA);
        dhap = new DHAP(this);

        ScrollView scrollView = new ScrollView(this);
        ViewGroup viewGroup = device.getDeviceViewGroup(getSupportFragmentManager(), this);
        if(viewGroup != null){
            scrollView.addView(viewGroup);
        }

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
            device.requestStatusLease(10000, 1000, false, new StatusLeaseCallbacks() {
                @Override
                public void leaseResponse(float leaseLength, float updatePeriod) {
                    Log.d("DeviceActivity", "leaseResponse: " + leaseLength + " UpdatePeriod: " + updatePeriod);
                }

                @Override
                public boolean shouldRenewStatusLease() {
                    return true;
                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!device.isDebugDevice()) {
            device.leaveLease();
        }
    }
}