package me.aidengaripoli.dhapexample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.isupatches.wisefy.WiseFy;
import com.isupatches.wisefy.callbacks.GetNearbyAccessPointsCallbacks;

import java.util.ArrayList;
import java.util.List;

import me.aidengaripoli.dhap.DHAP;
import me.aidengaripoli.dhap.joining.callbacks.JoinDeviceCallbacks;

public class JoiningActivity extends AppCompatActivity {
    private DHAP dhap;
    private WiseFy wiseFy;

    private RecyclerView.Adapter adapterHome;
    private RecyclerView.Adapter adapterDevice;
    private EditText homePassword;
    private EditText devicePassword;
    private EditText deviceName;
    private EditText deviceLocation;
    private TextView joiningState;
    private List<ScanResult> nearbyAccessPoints;
    private ScanResult selectedHomeNetwork;
    private ScanResult selectedDeviceNetwork;
    private Button joinButton;

    private OnWifiNetworkClickListener homeWifiListener = new OnWifiNetworkClickListener() {
        @Override
        public void onWifiNetworkSelected(ScanResult scanResult) {
            selectedHomeNetwork = scanResult;
        }
    };
    private OnWifiNetworkClickListener deviceWifiListener = new OnWifiNetworkClickListener() {
        @Override
        public void onWifiNetworkSelected(ScanResult scanResult) {
            selectedDeviceNetwork = scanResult;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joining);

        dhap = new DHAP(this);
        wiseFy = new WiseFy.Brains(this).logging(true).getSmarts();
        nearbyAccessPoints = new ArrayList<>();

        adapterHome = new WifiNetworkAdapter(nearbyAccessPoints, homeWifiListener);
        adapterDevice = new WifiNetworkAdapter(nearbyAccessPoints, deviceWifiListener);

        setUpRecyclerView(findViewById(R.id.recyclerViewHome), adapterHome);
        setUpRecyclerView(findViewById(R.id.recyclerViewDevice), adapterDevice);

        homePassword = findViewById(R.id.edit_text_home_pasword);
        devicePassword = findViewById(R.id.edit_text_device_pasword);

        deviceName = findViewById(R.id.edit_text_device_name);
        deviceLocation = findViewById(R.id.edit_text_device_location);

        joiningState = findViewById(R.id.joiningState);

        joinButton = findViewById(R.id.join_button);
        getNearbyAccessPoints();
        scanWiFI(null);
    }


    private void setUpRecyclerView(RecyclerView recyclerView, RecyclerView.Adapter adapter) {
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManagerHome = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManagerHome);
        recyclerView.setAdapter(adapter);
    }

    public void scanWiFI(View view){
        WifiManager wifiManager = (WifiManager)
                getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.startScan();

        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(),"Wifi scanned",Toast.LENGTH_SHORT).show());
                    getNearbyAccessPoints();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);
    }

    private void getNearbyAccessPoints() {
        wiseFy.getNearbyAccessPoints(true, new GetNearbyAccessPointsCallbacks() {
            @Override
            public void noAccessPointsFound() {
                // TODO: handle failure
            }

            @Override
            public void retrievedNearbyAccessPoints(@NonNull List<ScanResult> accessPoints) {
                runOnUiThread(() -> {
                    // Update WifiNetworkFragment's access points
                    nearbyAccessPoints.clear();
                    nearbyAccessPoints.addAll(accessPoints);
                    adapterHome.notifyDataSetChanged();
                    adapterDevice.notifyDataSetChanged();
                });
            }

            @Override
            public void wisefyFailure(int wisefyFailureCode) {
                // TODO: handle failure
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        scanWiFI(null);
    }

    public void joinDevice(View view) {
        if (selectedHomeNetwork == null || selectedDeviceNetwork == null) {
            Toast.makeText(getApplicationContext(), "Select a home and IoT device Network", Toast.LENGTH_SHORT).show();
            return;
        }
        joinButton.setEnabled(false);

        String text = "Verifying credentials...";
        joiningState.setText(text);

        dhap.joinDevice(selectedHomeNetwork.SSID, homePassword.getText().toString(), selectedDeviceNetwork.SSID,
                devicePassword.getText().toString(), deviceName.getText().toString(), deviceLocation.getText().toString(),
                new JoinDeviceCallbacks() {
                    @Override
                    public void networkNotFound(String SSID) {
                        String text = "Network with SSID: " + SSID + " not found";
                        runOnUiThread(() -> {
                            joiningState.setText(text);
                            joinButton.setEnabled(true);
                        });
                    }

                    @Override
                    public void credentialsAcknowledged() {
                        String text = "Credentials Acknowledged";
                        runOnUiThread(() -> joiningState.setText(text));
                    }

                    @Override
                    public void sendCredentialsTimeout() {
                        String text = "Joining Failed. Sending credentials timed out";
                        runOnUiThread(() -> {
                            joiningState.setText(text);
                            joinButton.setEnabled(true);
                        });
                    }

                    @Override
                    public void success() {
                        String text = "Device Successfully Joined";
                        runOnUiThread(() -> {
                            joiningState.setText(text);
                            joinButton.setEnabled(true);
                        });
                    }

                    @Override
                    public void failure(String message) {
                        String text = "Joining Failed. " + message;
                        runOnUiThread(() -> {
                            joiningState.setText(text);
                            joinButton.setEnabled(true);
                        });
                    }
                });
    }
}
