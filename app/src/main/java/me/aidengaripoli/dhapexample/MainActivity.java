package me.aidengaripoli.dhapexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import java.util.List;

import me.aidengaripoli.dhap.DHAP;
import me.aidengaripoli.dhap.Device;
import me.aidengaripoli.dhap.Discovery;
import me.aidengaripoli.dhap.callbacks.GetDeviceUIActivityCallbacks;
import me.aidengaripoli.dhap.callbacks.discovery.GetDiscoveredDevicesCallbacks;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Discovery discovery;
    private DHAP dhap;

    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        discovery = new Discovery(this);
        dhap = new DHAP(this);

        button = findViewById(R.id.button);
        button.setOnClickListener(v -> test());
    }

    private void test() {
        dhap.fetchDeviceInterface("", true, new GetDeviceUIActivityCallbacks() {
            @Override
            public void assetsFileFailure() {
                Log.d(TAG, "assetsFileFailure");
            }

            @Override
            public void deviceActivityIntent(Intent intent) {
                Log.d(TAG, "deviceActivityIntent");
                startActivity(intent);
            }

            @Override
            public void displayFailure() {
                Log.d(TAG, "displayFailure");
            }
        });
    }

    private void findDevices() {
        discovery.discoverDevices(new GetDiscoveredDevicesCallbacks() {
            @Override
            public void foundDevices(List<Device> devices) {
                Log.d(TAG, "Found devices: ");
                for (Device device : devices) {
                    Log.d(TAG,  "\tMAC: " + device.getMacAddress());
                }
            }

            @Override
            public void noDevicesFound() {
                Log.d(TAG, "No devices found.");
            }

            @Override
            public void discoveryFailure() {
                Log.d(TAG, "Discovery failed.");
            }
        });
    }
}
