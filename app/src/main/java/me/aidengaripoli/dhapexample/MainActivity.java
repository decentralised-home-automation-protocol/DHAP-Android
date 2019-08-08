package me.aidengaripoli.dhapexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.List;

import me.aidengaripoli.dhap.Device;
import me.aidengaripoli.dhap.Discovery;
import me.aidengaripoli.dhap.callbacks.GetDiscoveredDevicesCallbacks;

public class MainActivity extends AppCompatActivity {

    private Discovery discovery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        discovery = new Discovery(this);

        findDevices();
    }

    private void findDevices() {
        discovery.discoverDevices(new GetDiscoveredDevicesCallbacks() {
            @Override
            public void foundDevices(List<Device> devices) {

            }

            @Override
            public void noDevicesFound() {

            }

            @Override
            public void discoveryFailure() {

            }
        });
    }
}
