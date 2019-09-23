package me.aidengaripoli.dhap.discovery;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import me.aidengaripoli.dhap.Device;
import me.aidengaripoli.dhap.PacketCodes;
import me.aidengaripoli.dhap.PacketListener;
import me.aidengaripoli.dhap.UdpPacketSender;
import me.aidengaripoli.dhap.discovery.callbacks.DiscoverDevicesCallbacks;

public final class Discovery implements PacketListener {

    private static final String TAG = Discovery.class.getSimpleName();
    private static final int LISTEN_TIMEOUT_IN_MILLIS = 1000;
    private static final int HEADER_TIMEOUT_IN_MILLIS = 300;
    private static final String FILENAME = "census_list";

    private ArrayList<Device> censusList;
    private ArrayList<Device> previousCensusList;
    private int devicesReplies;
    private HashSet<String> respondingDevices;
    private HashMap<String, Device> devicesWithoutHeader;
    private HashMap<String, Device> savedDevices;

    private UdpPacketSender udpPacketSender;
    private Context context;

    public Discovery(Context context) {
        this.context = context;
        udpPacketSender = UdpPacketSender.getInstance();
    }

    public void discoverDevices(DiscoverDevicesCallbacks callback) {
        new Thread(() -> {
            try {
                censusList = new ArrayList<>();
                previousCensusList = new ArrayList<>();
                devicesReplies = 0;
                respondingDevices = new HashSet<>();
                savedDevices = getSavedDevices();
                devicesWithoutHeader = new HashMap<>();

                for (Map.Entry<String, Device> entry : savedDevices.entrySet()) {
                    censusList.add(entry.getValue());
                }

                findDevices();

                if(censusList.size() == 0){
                    callback.noDevicesFound();
                    return;
                }

                getDeviceHeaders(devicesWithoutHeader);
                saveToFile(savedDevices);

                censusList.clear();
                for (Map.Entry<String, Device> entry : savedDevices.entrySet()) {
                    censusList.add(entry.getValue());
                }
                callback.foundDevices(censusList);
            } catch (Exception e) {
                Log.e(TAG, String.valueOf(e));
                callback.discoveryFailure();
            } finally {
                censusList.clear();
                previousCensusList.clear();
            }
        }).start();
    }

    private HashMap<String, Device> getSavedDevices() {
        HashMap<String, Device> censusListFromFile = new HashMap<>();

        try {
            InputStream inputStream = context.openFileInput(FILENAME);
            String censusListString = inputStreamToString(inputStream);

            String[] devices = censusListString.split("-");

            for (String deviceString : devices) {
                Device device = parseDeviceFromFile(deviceString);
                censusListFromFile.put(device.getMacAddress(), device);
            }

            return censusListFromFile;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "getSavedDevices: No census List found");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return censusListFromFile;
    }

    private Device parseDeviceFromFile(String deviceString) {
        String[] deviceData = deviceString.split(",");

        Device device = new Device(
                deviceData[0],
                deviceData[4],
                0,
                Integer.parseInt(deviceData[2]),
                Integer.parseInt(deviceData[3])
        );

        device.setName(deviceData[5]);
        device.setLocation(deviceData[6]);
        return device;
    }

    private void saveToFile(HashMap<String, Device> censusListToSave) {
        FileOutputStream outputStream;
        StringBuilder censusListString = new StringBuilder();


        for (Map.Entry<String, Device> entry : censusListToSave.entrySet()) {
            Device value = entry.getValue();

            censusListString.append(value.toString());
            censusListString.append(",").append(value.getIpAddress());
            censusListString.append(",").append(value.getName());
            censusListString.append(",").append(value.getLocation());
            censusListString.append("-");
        }

        Log.e(TAG, "saveToFile: " + censusListString);

        try {
            outputStream = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            outputStream.write(censusListString.toString().getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void discoverDebugDevices(DiscoverDevicesCallbacks callback) {
        String deviceXML;
        AssetManager assetManager = context.getAssets();

        try {
            String[] list = assetManager.list("");
            if (list == null) {
                callback.noDevicesFound();
                return;
            }
            for (String fileName : list) {
                if (fileName.endsWith(".xml")) {
                    InputStream inputStream = assetManager.open(fileName);
                    deviceXML = inputStreamToString(inputStream);
                    Device device = new Device(null, null, 1, 0, 0);
                    device.setXml(deviceXML);
                    device.setLocation("Debug Location");
                    device.setName("Debug Name");
                    censusList.add(device);
                }
            }
        } catch (IOException e) {
            callback.noDevicesFound();
        }

        if (censusList.isEmpty()) {
            callback.noDevicesFound();
        } else {
            callback.foundDevices(censusList);
        }
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

    private void findDevices() {
        int emptyRepliesCount = 0;
        int listRepeatedCount = 0;

        while (true) {
            broadcastList();

            listenForReplies();

            Log.d(TAG, "Received (" + devicesReplies + ") replies.");
            // received replies?
            if (devicesReplies == 0) { // yes
                emptyRepliesCount++;
                Log.d(TAG, "Empty replies: (" + emptyRepliesCount + ").");
                // is this the 3rd empty reply?
                if (emptyRepliesCount == 3) { // yes
                    Log.d(TAG, "Third empty reply. Finishing discovery.");
                    return;
                }
            } else { // no
                emptyRepliesCount = 0;
            }

            Log.d(TAG, "List: " + censusList.toString());

            if (previousCensusList.equals(censusList)) {
                listRepeatedCount++;
                Log.d(TAG, "List repeated (" + listRepeatedCount + ") times.");
            } else {
                Log.d(TAG, "Setting previous list to current list.");
                listRepeatedCount = 0;
                previousCensusList.clear();
                previousCensusList.addAll(censusList);
            }

            // list repeated > 5 times?
            if (listRepeatedCount > 5) { // yes
                Log.d(TAG, "List repeated 5 times. Finishing discovery.");
                return;
            }

            // repeat
            Log.d(TAG, "Repeating...");
        }
    }

    private void getDeviceHeaders(HashMap<String, Device> devices) {
        udpPacketSender.addPacketListener(this);
        int timeOut = 10;

        ArrayList<Device> devicesWithoutHeader = new ArrayList<>();

        for (Map.Entry<String, Device> entry : devices.entrySet()) {
            devicesWithoutHeader.add(entry.getValue());
        }

        while (devicesWithoutHeader.size() > 0 && timeOut > 0) {
            previousCensusList.clear();

            for (Device device : devicesWithoutHeader) {
                udpPacketSender.sendUdpPacketToIP(PacketCodes.DISCOVERY_HEADER_REQUEST, device.getIpAddress());

                try {
                    Thread.sleep(HEADER_TIMEOUT_IN_MILLIS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            devicesWithoutHeader.removeAll(previousCensusList);
            previousCensusList.clear();
            timeOut--;
        }

        udpPacketSender.removePacketListener(this);
    }

    private void broadcastList() {
        Log.d(TAG, "Broadcasting list...");
        String censusListString = PacketCodes.DISCOVERY_REQUEST;

        if (censusList.size() > 0) {
            censusListString += "|" + TextUtils.join("-", censusList);
        }

        udpPacketSender.sendUdpBroadcastPacket(censusListString);
    }

    private void listenForReplies() {
        Log.d(TAG, "Listening to replies...");

        devicesReplies = 0;
        udpPacketSender.addPacketListener(this);

        // listen to replies for ~1 second
        try {
            Thread.sleep(LISTEN_TIMEOUT_IN_MILLIS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        udpPacketSender.removePacketListener(this);
    }

    @Override
    public boolean newPacket(String packetType, String packetData, InetAddress fromIP) {
        if (packetType.equals(PacketCodes.DISCOVERY_RESPONSE)) {
            Device device = parseReply(packetData, fromIP);

            if (respondingDevices.contains(device.getMacAddress())) {
                return false;
            }

            //Check if this device has been saved in the local storage list.
            if (savedDevices.containsKey(device.getMacAddress())) {
                //Check if the IP and header version are out of date
                Device savedDevice = savedDevices.get(device.getMacAddress());
                savedDevice.setStatus(1);
                if (savedDevice.getHeaderVersion() != device.getHeaderVersion()
                        || !savedDevice.getIpAddress().equals(device.getIpAddress())) {
                    savedDevices.remove(device.getMacAddress());
                    devicesWithoutHeader.put(device.getMacAddress(), device);
                    censusList.add(device);
                }
            } else {
                savedDevices.put(device.getMacAddress(), device);
                devicesWithoutHeader.put(device.getMacAddress(), device);
                censusList.add(device);
            }

            respondingDevices.add(device.getMacAddress());
            devicesReplies++;
        } else if (packetType.equals(PacketCodes.DISCOVERY_HEADER_RESPONSE)) {
            addHeaderToDevice(packetData, fromIP);
        }
        return false;
    }

    private void addHeaderToDevice(String header, InetAddress fromIP) {
        Log.d(TAG, "addHeaderToDevice: Header received " + header);
        String[] headerData = header.split(",");

        Log.e(TAG, "addHeaderToDevice: " + header);

        for (Device device : censusList) {
            if (device.getIpAddress().equals(fromIP.getHostAddress())) {
                if (headerData.length < 4) {
                    return;
                }
                device.setName(headerData[2]);
                device.setLocation(headerData[3]);
                previousCensusList.add(device);
                return;
            }
        }
    }

    private Device parseReply(String packetData, InetAddress fromIP) {
        String[] deviceString = packetData.split(",");

        return new Device(
                deviceString[0],
                fromIP.getHostAddress(),
                1,
                Integer.parseInt(deviceString[2]),
                Integer.parseInt(deviceString[3])

        );
    }

    public void clearSavedDevices() {
        File dir = context.getFilesDir();
        File file = new File(dir, FILENAME);
        boolean deleted = file.delete();

        if (deleted) {
            Log.e(TAG, "clearSavedDevices: cleared");
        } else {
            Log.e(TAG, "clearSavedDevices: not cleared");
        }

    }

    public void removeDevice(Device device) {
        HashMap<String, Device> censusListFromFile = getSavedDevices();
        if (censusListFromFile == null) {
            return;
        }

        if (censusListFromFile.containsKey(device.getMacAddress())) {
            Log.e(TAG, "removeDevice: Device removed " + device.getName());
            censusListFromFile.remove(device.getMacAddress());
            saveToFile(censusListFromFile);
        } else {
            Log.e(TAG, "removeDevice: Device not found " + device.getName());
        }
    }
}
