package me.aidengaripoli.dhapexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import me.aidengaripoli.dhap.DHAP;
import me.aidengaripoli.dhap.Device;
import me.aidengaripoli.dhap.Discovery;
import me.aidengaripoli.dhap.callbacks.discovery.GetDiscoveredDevicesCallbacks;

public class MainActivity extends AppCompatActivity implements
        ActionFragment.OnActionResultListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Discovery discovery;
    private DHAP dhap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        discovery = new Discovery(this);
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

        discovery.discoverDevices(new GetDiscoveredDevicesCallbacks() {
            @Override
            public void foundDevices(List<Device> devices) {
                displayDiscoveredDevices(new ArrayList<>(devices));
            }

            @Override
            public void noDevicesFound() {
                displayNoDevicesFound();
            }

            @Override
            public void discoveryFailure() {
                Log.d(TAG, "Discovery failed.");
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

//    private void test() {
//        dhap.fetchDeviceInterface("", true, new GetDeviceUIActivityCallbacks() {
//            @Override
//            public void assetsFileFailure() {
//                Log.d(TAG, "assetsFileFailure");
//            }
//
//            @Override
//            public void deviceActivityIntent(Intent intent) {
//                Log.d(TAG, "deviceActivityIntent");
//                startActivity(intent);
//            }
//
//            @Override
//            public void displayFailure() {
//                Log.d(TAG, "displayFailure");
//            }
//        });
//    }

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

    @Override
    public void onActionResult(String action) {
        Log.d(TAG, "onActionResult");
    }
}
