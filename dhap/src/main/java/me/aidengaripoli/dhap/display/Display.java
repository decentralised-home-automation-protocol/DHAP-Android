package me.aidengaripoli.dhap.display;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import me.aidengaripoli.dhap.Device;
import me.aidengaripoli.dhap.PacketCodes;
import me.aidengaripoli.dhap.UdpPacketSender;
import me.aidengaripoli.dhap.display.callbacks.GetDeviceUIActivityCallbacks;

public class Display {
    private static final String TAG = Display.class.getSimpleName();

    private Context context;

    public Display (Context context) {
        this.context = context;
    }

    public void fetchDeviceInterface(Device device, boolean useAssetsFolder, GetDeviceUIActivityCallbacks callbacks) {
        // --temp-- get device from assets folder
        // make it an option for users of the lib to specify to retrieve xml from assets folder
        // instead of requiring a compliant device for testing.

//        String deviceXML = null;
//
//        if (useAssetsFolder) {
////            AssetManager assetManager = context.getAssets();
////
////            try {
////                String[] list = assetManager.list("");
////                for (String fileName : list) {
////                    if (fileName.contains(deviceName) && fileName.endsWith(".xml")) {
////                        InputStream inputStream = assetManager.open(fileName);
////                        deviceXML = inputStreamToString(inputStream);
////                        Log.d(TAG, deviceXML);
////                    }
////                }
////            } catch (IOException e) {
////                callbacks.assetsFileFailure();
////            }
//
////            assetManager.close();
//        } else {
//            // attempt to retrieve cached device file/data from storage or db
//            // if not found, ask the device for its xml file over network
//            // should be a background thread with retry (3) and timeouts (1s)
//            // if successful, cache the file (or save data to db)
//        }
//
//        if (deviceXML == null) {
//            callbacks.displayFailure();
//            return;
//        }

        UdpPacketSender.getInstance().addPacketListener(packetData -> {
            String xml = packetData.substring(4);

            Log.d(TAG, xml);

//        // parse file for device ui
            DeviceDescription description = new DeviceDescription(xml, device);
            UdpPacketSender.getInstance().addPacketListener(description);

            Intent intent = new Intent(context, DeviceActivity.class);
            intent.putExtra("deviceDescription", description);

            // create new status updates listener thread
            // assign thread to device description

            callbacks.deviceActivityIntent(intent);
        });

        UdpPacketSender.getInstance().sendUdpPacketToIP("200", device.getIpAddress().getHostAddress());
    }

    private String inputStreamToString(InputStream is) throws IOException {
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
