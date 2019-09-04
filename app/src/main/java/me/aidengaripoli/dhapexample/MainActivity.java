package me.aidengaripoli.dhapexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import me.aidengaripoli.dhap.DHAP;
import me.aidengaripoli.dhap.Device;
import me.aidengaripoli.dhap.discovery.callbacks.GetDiscoveredDevicesCallbacks;
import me.aidengaripoli.dhap.display.callbacks.GetDeviceUIActivityCallbacks;

public class MainActivity extends AppCompatActivity implements
        ActionFragment.OnActionResultListener,
        DiscoveredDevicesFragment.OnDeviceSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private DHAP dhap;

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dhap = new DHAP(this);

        fragmentManager = getSupportFragmentManager();

        beginDeviceDiscovery();
    }

    private void beginDeviceDiscovery() {
        // discover devices
        DiscoveringDevicesFragment fragment = DiscoveringDevicesFragment.newInstance();

        ActionFragment actionFragment = (ActionFragment) fragmentManager
                .findFragmentById(R.id.fragment_action_container);

        if (actionFragment != null) {
            fragmentManager.beginTransaction().remove(actionFragment).commit();
        }

        fragmentManager.beginTransaction()
                .replace(R.id.fragment_discovery_state_container, fragment)
                .commit();

        dhap.discoverDevices(new GetDiscoveredDevicesCallbacks() {
            @Override
            public void foundDevices(List<Device> devices) {
                Log.e(TAG, "Devices found.");
                displayDiscoveredDevices(new ArrayList<>(devices));
            }

            @Override
            public void noDevicesFound() {
                Log.e(TAG, "No devices found.");
                displayNoDevicesFound();
            }

            @Override
            public void discoveryFailure() {
                Log.e(TAG, "Discovery failed.");
            }
        });
    }

    private void displayDiscoveredDevices(final ArrayList<Device> devices) {
        runOnUiThread(() -> {
            DiscoveredDevicesFragment devicesFragment = DiscoveredDevicesFragment
                    .newInstance(devices);

            ActionFragment actionFragment = ActionFragment
                    .newInstance("Add", true, "Refresh");

            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_discovery_state_container, devicesFragment)
                    .commit();

            fragmentManager.beginTransaction().
                    add(R.id.fragment_action_container, actionFragment)
                    .commit();
        });
    }

    private void displayNoDevicesFound() {
        runOnUiThread(() -> {
            NoDevicesFoundFragment fragment =  NoDevicesFoundFragment.newInstance();

            ActionFragment actionFragment = ActionFragment
                    .newInstance("Add", true, "Refresh");

            fragmentManager.beginTransaction()
                    .add(R.id.fragment_action_container, actionFragment)
                    .commit();

            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_discovery_state_container, fragment)
                    .commit();
        });
    }

    @Override
    public void onActionResult(String action) {
        Log.d(TAG, "onActionResult");
    }

    @Override
    public void onDeviceSelected(Device device) {
        Log.d(TAG, "received device: " + device.getMacAddress());
        dhap.fetchDeviceInterface(device, false, new GetDeviceUIActivityCallbacks() {
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
}
