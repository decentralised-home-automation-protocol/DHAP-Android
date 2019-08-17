package me.aidengaripoli.dhap;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import me.aidengaripoli.dhap.callbacks.GetDeviceUIActivityCallbacks;

public class DHAP {

    private static final String TAG = DHAP.class.getSimpleName();

    private Context context;

    public DHAP(Context context) {
        this.context = context;
    }

    public void fetchDevicesInterface(String deviceName, boolean useAssetsFolder, GetDeviceUIActivityCallbacks callbacks) {
        // --temp-- get device from assets folder
        // make it an option for users of the lib to specify to retrieve xml from assets folder
        // instead of requiring a compliant device for testing.

        if (useAssetsFolder) {
            AssetManager assetManager = context.getAssets();

            try {
                String[] list = assetManager.list("");
                for (String fileName : list) {
                    if (fileName.contains(deviceName) && fileName.endsWith(".xml")) {
                        InputStream inputStream = assetManager.open(fileName);
                        Log.d(TAG, inputStreamToString(inputStream));
                    }
                }
            } catch (IOException e) {
                callbacks.assetsFileFailure();
            }

//            assetManager.close();
        } else {
            // attempt to retrieve cached device file/data from storage or db

            // if not found, ask the device for its xml file over network
                // should be a background thread with retry (3) and timeouts (1s)
                // if successful, cache the file (or save data to db)
        }

//        View view = ElementLayout.create();
//
//        // parse file for device ui
        DeviceDescription description = ;

//        // create element instances
//        // create view with elements
//        ScrollView scrollView = new ScrollView(context);
//        scrollView.addView(view);
//
//        Intent intent = new Intent(context, DeviceActivity.class);
//        intent.putExtra("view", (Serializable) scrollView);
//
//        callbacks.deviceActivityIntent(intent);
    }

    public String inputStreamToString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }

}
