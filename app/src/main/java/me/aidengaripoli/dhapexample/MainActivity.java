package me.aidengaripoli.dhapexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.List;

import me.aidengaripoli.dhap.DHAP;
import me.aidengaripoli.dhap.Device;
import me.aidengaripoli.dhap.discovery.callbacks.DiscoverDevicesCallbacks;
import me.aidengaripoli.dhap.display.callbacks.fetchDeviceInterfaceCallbacks;
import me.aidengaripoli.dhap.joining.callbacks.BaseJoiningCallbacks;
import me.aidengaripoli.dhap.joining.callbacks.JoinDeviceCallbacks;

public class MainActivity extends AppCompatActivity implements
        ActionFragment.OnActionResultListener,
        DiscoveredDevicesFragment.OnDeviceSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private DHAP dhap;
    private ActionFragment actionFragment;

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

        dhap.discoverDevices(new DiscoverDevicesCallbacks() {
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

            actionFragment = ActionFragment
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

            actionFragment = ActionFragment
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
    public void onDeviceSelected(Device device) {
        Log.d(TAG, "received device: " + device.getMacAddress());
        dhap.fetchDeviceInterface(device, new fetchDeviceInterfaceCallbacks() {
            @Override
            public void deviceActivityIntent(Intent intent) {
                Log.d(TAG, "deviceActivityIntent");
                startActivity(intent);
            }

            @Override
            public void invalidDisplayXmlFailure() {
                Log.d(TAG, "invalidDisplayXmlFailure");
            }

            @Override
            public void displayTimeoutFailure() {
                Log.d(TAG, "displayTimeoutFailure");
            }
        });
    }

    @Override
    public void onActionResult(String action) {
        switch (action) {
            case "Refresh": {
                Log.d(TAG, "Re-discovering devices.");
                beginDeviceDiscovery();
                break;
            }
            case "Add": {
                Log.d(TAG,  "Join Device.");
                actionFragment.setActionEnabled(false);
//                startActivity(new Intent(this, WifiNetworkListActivity.class));
                dhap.joinDevice("TP-LINK_AE045A", "0358721743", "ESPsoftAP_01", "passforap", new JoinDeviceCallbacks() {
                    @Override
                    public void networkNotFound(String SSID) {
                        Log.e(TAG, "Network not found: " + SSID);
                    }

                    @Override
                    public void credentialsAcknowledged() {
                        Log.e(TAG, "Credentials Acknowledged");
                    }

                    @Override
                    public void sendCredentialsTimeout() {
                        Log.e(TAG, "Sending of credentials timed out");
                    }

                    @Override
                    public void success() {
                        actionFragment.setActionEnabled(true);
                        Log.d(TAG, "successfully joined device");
                    }

                    @Override
                    public void failure(String message) {
                        actionFragment.setActionEnabled(true);
                        Log.e(TAG, "failure: " + message);
                    }
                });
                break;
            }
        }
    }

}
