package me.aidengaripoli.dhapexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.List;

import me.aidengaripoli.dhap.DHAP;
import me.aidengaripoli.dhap.Device;
import me.aidengaripoli.dhap.discovery.callbacks.DiscoverDevicesCallbacks;
import me.aidengaripoli.dhap.discovery.callbacks.RefreshCensuslistCallbacks;
import me.aidengaripoli.dhap.display.callbacks.GetDeviceInterfaceCallbacks;

public class MainActivity extends AppCompatActivity implements
        ActionFragment.OnActionResultListener,
        DiscoveredDevicesFragment.OnDeviceSelectedListener,
        ChangeHeaderFragment.OnChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private DHAP dhap;
    private ActionFragment actionFragment;
    private FragmentManager fragmentManager;
    private DiscoveredDevicesFragment devicesFragment;
    private ArrayList<Device> censusList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dhap = new DHAP(this);

        fragmentManager = getSupportFragmentManager();

        censusList = new ArrayList<>();
        censusList.addAll(dhap.getSavedDevices());

        refreshCensusList();
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
                censusList.clear();
                censusList.addAll(devices);
                displayDiscoveredDevices();
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

    private void displayDiscoveredDevices() {
        runOnUiThread(() -> {
            displayActionBar();

            devicesFragment = DiscoveredDevicesFragment.newInstance(censusList);

            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_discovery_state_container, devicesFragment)
                    .commit();
        });
    }

    private void displayNoDevicesFound() {
        runOnUiThread(() -> {
            displayActionBar();

            NoDevicesFoundFragment fragment = NoDevicesFoundFragment.newInstance();

            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_discovery_state_container, fragment)
                    .commit();
        });
    }

    private void displayActionBar(){
        ActionFragment actionFragment = (ActionFragment) fragmentManager
                .findFragmentById(R.id.fragment_action_container);

        if (actionFragment == null) {
            actionFragment = ActionFragment
                    .newInstance("Add", true, "Refresh");

            fragmentManager.beginTransaction()
                    .add(R.id.fragment_action_container, actionFragment)
                    .commit();
        }
    }

    @Override
    public void onDeviceSelected(Device device) {
        Log.d(TAG, "received device: " + device.getMacAddress());
        dhap.fetchDeviceInterface(device, new GetDeviceInterfaceCallbacks() {
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
            case "Discovery": {
                Log.d(TAG, "Re-discovering devices.");
                beginDeviceDiscovery();
                break;
            }

            case "Refresh": {
                Log.d(TAG, "Refreshing censuslist.");
                refreshCensusList();
                break;
            }

            case "Clear": {
                censusList.clear();
                dhap.clearSavedDevices();
                displayNoDevicesFound();
                break;
            }

            case "Add": {
                startActivity(new Intent(this, JoiningActivity.class));
                break;
            }
        }
    }

    public void removeDevice(View view) {
        Device device = (Device) view.getTag();
        Log.e(TAG, "removeDevice: " + device.getName());
        dhap.removeDevice(device);
        devicesFragment.removeDevice(device);
    }

    public void editDeviceHeader(View view) {
        Device device = (Device) view.getTag();

        ChangeHeaderFragment dialog = ChangeHeaderFragment.newInstance(device, this);
        dialog.show(getSupportFragmentManager(), "ChangeHeaderFragment");
    }

    private void refreshCensusList(){
        dhap.refreshCensusList(censusList, new RefreshCensuslistCallbacks() {
            @Override
            public void censusListRefreshed() {
                if(censusList.isEmpty()){
                    displayNoDevicesFound();
                }else{
                    displayDiscoveredDevices();
                }
            }
        });
    }

    @Override
    public void headerChanged() {
        refreshCensusList();
    }
}
