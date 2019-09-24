package me.aidengaripoli.dhap;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class SavedCensusListManager {
    private static final String TAG = SavedCensusListManager.class.getSimpleName();
    private static final String FILENAME = "census_list";

    private static final String DEVICE_DELIM = "═";     //ASCII code 205
    private static final String DEVICE_DATA_DELIM = "║"; //ASCII code 186
    private Context context;

    public SavedCensusListManager(Context context) {
        this.context = context;
    }

    public HashMap<String, Device> getKnownDevices() {
        HashMap<String, Device> censusListFromFile = new HashMap<>();

        try {
            InputStream inputStream = context.openFileInput(FILENAME);
            String censusListString = inputStreamToString(inputStream);

            String[] devices = censusListString.split(DEVICE_DELIM);

            for (String deviceString : devices) {
                Device device = parseDeviceFromFile(deviceString);
                censusListFromFile.put(device.getMacAddress(), device);
            }

            return censusListFromFile;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "getKnownDevices: No census List found");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return censusListFromFile;
    }

    private Device parseDeviceFromFile(String deviceString) {
        String[] deviceData = deviceString.split(DEVICE_DATA_DELIM);

        Device device = new Device(
                deviceData[0],
                deviceData[2],
                0,
                1,
                Integer.parseInt(deviceData[1])
        );

        device.setName(deviceData[3]);
        device.setLocation(deviceData[4]);
        device.setXml(deviceData[5]);
        return device;
    }

    public void saveToFile(HashMap<String, Device> censusListToSave) {
        FileOutputStream outputStream;
        StringBuilder censusListString = new StringBuilder();

        for (Map.Entry<String, Device> entry : censusListToSave.entrySet()) {
            Device value = entry.getValue();

            censusListString.append(value.getMacAddress());
            censusListString.append(DEVICE_DATA_DELIM).append(value.getHeaderVersion());
            censusListString.append(DEVICE_DATA_DELIM).append(value.getIpAddress());
            censusListString.append(DEVICE_DATA_DELIM).append(value.getName());
            censusListString.append(DEVICE_DATA_DELIM).append(value.getLocation());
            censusListString.append(DEVICE_DATA_DELIM).append(value.getXml());
            censusListString.append(DEVICE_DELIM);
        }

        try {
            outputStream = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            outputStream.write(censusListString.toString().getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public void clearSavedDevices() {
        File dir = context.getFilesDir();
        File file = new File(dir, FILENAME);
        file.delete();
    }

    public void updateSavedDevice(Device device) {
        HashMap<String, Device> knownDevices = getKnownDevices();

        if (knownDevices.containsKey(device.getMacAddress())) {
            knownDevices.get(device.getMacAddress()).setXml(device.getXml());
            saveToFile(knownDevices);
        }
    }
}
