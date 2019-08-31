package me.aidengaripoli.dhapexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import me.aidengaripoli.dhap.DHAP;
import me.aidengaripoli.dhap.Device;
import me.aidengaripoli.dhap.discovery.callbacks.GetDiscoveredDevicesCallbacks;

public class MainActivity extends AppCompatActivity implements
        ActionFragment.OnActionResultListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private DHAP dhap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dhap = new DHAP(this);

        beginDeviceDiscovery();
    }

    private void beginDeviceDiscovery() {
        // discover devices
        FragmentManager fragmentManager = getSupportFragmentManager();

        DiscoveringDevicesFragment fragment = DiscoveringDevicesFragment.newInstance();

        ActionFragment actionFragment = (ActionFragment) fragmentManager
                .findFragmentById(R.id.fragment_action_container);

        if (actionFragment != null) {
            fragmentManager.beginTransaction().remove(actionFragment).commit();
        }

        fragmentManager.beginTransaction()
                .replace(R.id.fragment_discovery_state_container, fragment)
                .commit();

        dhap.startDiscovery(new GetDiscoveredDevicesCallbacks() {
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

            FragmentManager fragmentManager = getSupportFragmentManager();

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

            FragmentManager fragmentManager = getSupportFragmentManager();

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
}
