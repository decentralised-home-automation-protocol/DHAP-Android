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
import java.util.HashSet;
import java.util.List;
import java.util.zip.DeflaterInputStream;

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
    private ArrayList<Device> devicesReplies;
    private HashSet<String> respondingDevices;

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
                devicesReplies = new ArrayList<>();
                respondingDevices = new HashSet<>();

                findDevices();

                if (censusList.size() > 0) {
                    ArrayList<Device> devicesWithoutHeader = new ArrayList<>(censusList);
                    ArrayList<Device> savedDevices = getSavedDevices();

                    if(savedDevices != null){
                        //Find devices that are saved to file and in the list of devices that just responded.
                        for(Device savedDevice : savedDevices){
                            for (Device censusDevice : censusList){
                                if(censusDevice.toString().equals(savedDevice.toString())){
                                    //Device is in both lists.
                                    devicesWithoutHeader.remove(censusDevice);
                                }
                            }
                        }
                        getDeviceHeaders(devicesWithoutHeader);
                        savedDevices.addAll(devicesWithoutHeader);
                        saveToFile(savedDevices);

                        //Mark the devices that did not respond as inactive.
                        for(Device device : savedDevices){
                            if(!respondingDevices.contains(device.toString())){
                                device.isActive = 0;
                            }
                        }

                        callback.foundDevices(savedDevices);
                    } else{
                        getDeviceHeaders(censusList);
                        saveToFile(censusList);
                        callback.foundDevices(censusList);
                    }
                } else {
                    callback.noDevicesFound();
                }
            } catch (Exception e) {
                Log.e(TAG, String.valueOf(e));
                callback.discoveryFailure();
            } finally {
                censusList.clear();
                previousCensusList.clear();
            }
        }).start();
    }

    private ArrayList<Device> getSavedDevices() {
        try {
            InputStream inputStream = context.openFileInput(FILENAME);
            String censusListString = inputStreamToString(inputStream);
            Log.e(TAG, "getSavedDevices: " + censusListString);

            ArrayList<Device> censusListFromFile = new ArrayList<>();

            String[] devices = censusListString.split("-");

            for (String deviceString : devices) {
                Device device = parseDeviceFromFile(deviceString);
                censusListFromFile.add(device);
            }

            return censusListFromFile;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "getSavedDevices: No census List found");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Device parseDeviceFromFile(String deviceString) {
        String[] deviceData = deviceString.split(",");

        Device device = new Device(
                deviceData[0],
                deviceData[1],
                Integer.parseInt(deviceData[2]),
                Integer.parseInt(deviceData[3])
        );

        device.setName(deviceData[4]);
        device.setLocation(deviceData[5]);
        return device;
    }

    private void saveToFile(ArrayList<Device> censusListToSave) {
        FileOutputStream outputStream;
        StringBuilder censusListString = new StringBuilder();

        for (int i = 0; i < censusListToSave.size(); i++) {
            Device device = censusListToSave.get(i);
            censusListString.append(device.toString());
            censusListString.append(",").append(device.getName());
            censusListString.append(",").append(device.getLocation());
            if (i < censusListToSave.size() - 1) {
                censusListString.append("-");
            }
        }

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
                    Device device = new Device(null, null, 0, 0);
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

            Log.d(TAG, "Received (" + devicesReplies.size() + ") replies.");
            // received replies?
            if (devicesReplies.size() > 0) { // yes
                updateCensusList(devicesReplies);
            } else { // no
                emptyRepliesCount++;
                Log.d(TAG, "Empty replies: (" + emptyRepliesCount + ").");
                // is this the 3rd empty reply?
                if (emptyRepliesCount == 3) { // yes
                    Log.d(TAG, "Third empty reply. Finishing discovery.");
                    return;
                }
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

    private void getDeviceHeaders(ArrayList<Device> devices) {
        udpPacketSender.addPacketListener(this);
        int timeOut = 10;

        ArrayList<Device> devicesWithoutHeader = new ArrayList<>(devices);


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

        devicesReplies.clear();
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

            if (respondingDevices.contains(device.toString())) {
                return false;
            }

            respondingDevices.add(device.toString());
            devicesReplies.add(device);
        } else if (packetType.equals(PacketCodes.DISCOVERY_HEADER_RESPONSE)) {
            addHeaderToDevice(packetData, fromIP);
        }
        return false;
    }

    private void addHeaderToDevice(String header, InetAddress fromIP) {
        Log.d(TAG, "addHeaderToDevice: Header received " + header);
        String[] headerData = header.split(",");

        for (Device device : censusList) {
            if (device.getIpAddress().equals(fromIP.getHostAddress())) {
                device.setName(headerData[1]);
                device.setLocation(headerData[2]);
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
                Integer.parseInt(deviceString[1]),
                Integer.parseInt(deviceString[2])
        );
    }

    private void updateCensusList(List<Device> repliedDevices) {
        Log.d(TAG, "Updating list...");
        censusList.addAll(repliedDevices);
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
        ArrayList<Device> censusListFromFile = getSavedDevices();
        if (censusListFromFile == null) {
            return;
        }

        Device deviceToRemove = null;
        Log.e(TAG, "removeDevice: " + device.getMacAddress());

        for (Device dev : censusListFromFile) {
            Log.e(TAG, "removeDevice: " + dev.getMacAddress());
            if (device.getMacAddress().equals(dev.getMacAddress())) {
                deviceToRemove = dev;
                break;
            }
        }
        if (deviceToRemove == null) {
            Log.e(TAG, "removeDevice: Device not found");
        } else {
            Log.e(TAG, "removeDevice: Device removed " + deviceToRemove.getName());
            censusListFromFile.remove(deviceToRemove);
            saveToFile(censusListFromFile);
        }
    }
}
