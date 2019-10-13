package me.aidengaripoli.dhap.discovery;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import me.aidengaripoli.dhap.Device;
import me.aidengaripoli.dhap.PacketCodes;
import me.aidengaripoli.dhap.PacketListener;
import me.aidengaripoli.dhap.SavedCensusListManager;
import me.aidengaripoli.dhap.UdpPacketSender;
import me.aidengaripoli.dhap.discovery.callbacks.DiscoverDevicesCallbacks;
import me.aidengaripoli.dhap.discovery.callbacks.RefreshCensuslistCallbacks;

public final class Discovery implements PacketListener {

    private static final String TAG = Discovery.class.getSimpleName();
    private static final int LISTEN_TIMEOUT_IN_MILLIS = 1000;
    private static final int HEADER_TIMEOUT_IN_MILLIS = 300;

    private int devicesReplies;
    private ArrayList<Device> censusList;
    private ArrayList<Device> tempList;
    private HashSet<String> respondingDevices;
    private HashMap<String, Device> devicesWithoutHeader;
    private HashMap<String, Device> knownDevices;

    private UdpPacketSender udpPacketSender;
    private SavedCensusListManager savedCensusListManager;
    private Context context;

    public Discovery(Context context) {
        this.context = context;
        udpPacketSender = UdpPacketSender.getInstance();
        savedCensusListManager = new SavedCensusListManager(context);
    }

    public void discoverDevices(DiscoverDevicesCallbacks callback) {
        new Thread(() -> {
            try {
                censusList = new ArrayList<>();
                devicesReplies = 0;
                respondingDevices = new HashSet<>();
                knownDevices = savedCensusListManager.getKnownDevices();
                devicesWithoutHeader = new HashMap<>();

                for (Map.Entry<String, Device> entry : knownDevices.entrySet()) {
                    censusList.add(entry.getValue());
                }

                findDevices();

                if (censusList.size() == 0) {
                    callback.noDevicesFound();
                    return;
                }

                getDeviceHeaders(devicesWithoutHeader);
                savedCensusListManager.saveToFile(knownDevices);

                callback.foundDevices(censusList);
            } catch (Exception e) {
                Log.e(TAG, String.valueOf(e));
                callback.discoveryFailure();
            } finally {
                censusList.clear();
            }
        }).start();
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
                    deviceXML = savedCensusListManager.inputStreamToString(inputStream);
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

    private void findDevices() {
        int emptyRepliesCount = 0;
        int listRepeatedCount = 0;

        String previousCensusList = "";

        while (true) {
            broadcastList();

            listenForReplies();

            // received replies?
            if (devicesReplies == 0) { // yes
                emptyRepliesCount++;
                // is this the 3rd empty reply?
                if (emptyRepliesCount == 3) { // yes
                    return;
                }
            } else { // no
                emptyRepliesCount = 0;
            }

            if (previousCensusList.equals(censusList.toString())) {
                listRepeatedCount++;
            } else {
                listRepeatedCount = 0;
                previousCensusList = censusList.toString();
            }

            // list repeated > 5 times?
            if (listRepeatedCount > 5) { // yes
                return;
            }
        }
    }

    private void getDeviceHeaders(HashMap<String, Device> devices) {
        udpPacketSender.addPacketListener(this);
        int timeOut = 10;

        ArrayList<Device> devicesWithoutHeader = new ArrayList<>();
        tempList = new ArrayList<>();
        for (Map.Entry<String, Device> entry : devices.entrySet()) {
            devicesWithoutHeader.add(entry.getValue());
        }

        while (devicesWithoutHeader.size() > 0 && timeOut > 0) {
            tempList.clear();

            for (Device device : devicesWithoutHeader) {
                udpPacketSender.sendUdpPacketToIP(PacketCodes.DISCOVERY_HEADER_REQUEST, device.getIpAddress());

                try {
                    Thread.sleep(HEADER_TIMEOUT_IN_MILLIS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            devicesWithoutHeader.removeAll(tempList);
            tempList.clear();
            timeOut--;
        }

        udpPacketSender.removePacketListener(this);
    }

    private void broadcastList() {
        String censusListString = PacketCodes.DISCOVERY_REQUEST;

        if (censusList.size() > 0) {
            censusListString += "|" + TextUtils.join("-", censusList);
        }

        udpPacketSender.sendUdpBroadcastPacket(censusListString);
    }

    private void listenForReplies() {
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

            if(device == null){
                return false;
            }

            if (respondingDevices.contains(device.getMacAddress())) {
                return false;
            }

            //Check if this device has been saved in the local storage list.
            if (knownDevices.containsKey(device.getMacAddress())) {
                //Check if the IP and header version are out of date
                Device knownDevice = knownDevices.get(device.getMacAddress());
                knownDevice.setStatus(1);
                if (knownDevice.getHeaderVersion() != device.getHeaderVersion()) {
                    knownDevice.setHeaderVersion(device.getHeaderVersion());
                    devicesWithoutHeader.put(knownDevice.getMacAddress(), knownDevice);
                }

                if (!knownDevice.getIpAddress().equals(device.getIpAddress())) {
                    knownDevice.setIpAddress(device.getIpAddress());
                }
            } else {
                knownDevices.put(device.getMacAddress(), device);
                devicesWithoutHeader.put(device.getMacAddress(), device);
                censusList.add(device);
            }

            respondingDevices.add(device.getMacAddress());
            devicesReplies++;
        } else if (packetType.equals(PacketCodes.DISCOVERY_HEADER_RESPONSE)) {
            addHeaderToDevice(packetData);
        }
        return false;
    }

    private void addHeaderToDevice(String header) {
        String[] headerData = header.split(",");

        if (devicesWithoutHeader.containsKey(headerData[0])) {
            Device device = devicesWithoutHeader.get(headerData[0]);
            if (device == null) {
                return;
            }
            device.setHeaderVersion(Integer.parseInt(headerData[1]));
            device.setName(headerData[2]);
            device.setLocation(headerData[3]);
            tempList.add(device);
        }
    }

    private Device parseReply(String packetData, InetAddress fromIP) {
        String[] deviceString = packetData.split(",");

        if(deviceString.length < 4){
            return null;
        }

        return new Device(
                deviceString[0],
                fromIP.getHostAddress(),
                1,
                Integer.parseInt(deviceString[2]),
                Integer.parseInt(deviceString[3])
        );
    }

    public void clearSavedDevices() {
        savedCensusListManager.clearSavedDevices();
    }

    public void removeDevice(Device device) {
        HashMap<String, Device> censusListFromFile = savedCensusListManager.getKnownDevices();
        if (censusListFromFile == null) {
            return;
        }

        if (censusListFromFile.containsKey(device.getMacAddress())) {
            censusListFromFile.remove(device.getMacAddress());
            savedCensusListManager.saveToFile(censusListFromFile);
        } else {
            Log.e(TAG, "removeDevice: Device not found " + device.getName());
        }
    }

    public void refreshCensusList(List<Device> devices, RefreshCensuslistCallbacks callbacks) {
        new Thread(() -> {
            HashMap<String, Device> devicesMap = new HashMap<>();
            for (Device device : devices) {
                devicesMap.put(device.getMacAddress(), device);
                device.setStatus(0);
            }
            ArrayList<Device> responseList = new ArrayList<>();

            PacketListener packetListener = new PacketListener() {
                @Override
                public boolean newPacket(String packetType, String packetData, InetAddress fromIP) {
                    if (packetType.equals(PacketCodes.DISCOVERY_RESPONSE)) {
                        Device deviceResponse = parseReply(packetData, fromIP);

                        Device device = devicesMap.get(deviceResponse.getMacAddress());

                        if (device == null) {
                            return false;
                        }
                        device.setStatus(1);
                        if(responseList.contains(device)){
                            return false;
                        }
                        responseList.add(device);

                        if (device.getHeaderVersion() != deviceResponse.getHeaderVersion()) {
                            udpPacketSender.sendUdpPacketToIP(PacketCodes.DISCOVERY_HEADER_REQUEST, deviceResponse.getIpAddress());
                        }
                    } else if (packetType.equals(PacketCodes.DISCOVERY_HEADER_RESPONSE)) {
                        String[] headerData = packetData.split(",");

                        if (devicesMap.containsKey(headerData[0])) {
                            Device device = devicesMap.get(headerData[0]);
                            if (device == null) {
                                return false;
                            }
                            device.setHeaderVersion(Integer.parseInt(headerData[1]));
                            device.setName(headerData[2]);
                            device.setLocation(headerData[3]);
                        }
                    }
                    return false;
                }
            };

            udpPacketSender.addPacketListener(packetListener);

            int timeOut = 5;
            ArrayList<Device> devicesToRefresh = new ArrayList<>(devices);

            while (devicesToRefresh.size() > 0 && timeOut > 0) {
                for (Device device : devicesToRefresh) {
                    udpPacketSender.sendUdpPacketToIP(PacketCodes.DISCOVERY_REQUEST, device.getIpAddress());

                    try {
                        Thread.sleep(HEADER_TIMEOUT_IN_MILLIS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                devicesToRefresh.removeAll(responseList);
                responseList.clear();
                timeOut--;
            }

            udpPacketSender.removePacketListener(packetListener);
            savedCensusListManager.saveToFile(devicesMap);
            callbacks.censusListRefreshed();
        }).start();
    }

    public ArrayList<Device> getSavedDevices() {
        HashMap<String, Device> savedDevices = savedCensusListManager.getKnownDevices();
        ArrayList<Device> savedDevicesList = new ArrayList<>();

        for (Map.Entry<String, Device> entry : savedDevices.entrySet()) {
            entry.getValue().setStatus(0);
            savedDevicesList.add(entry.getValue());
        }

        return savedDevicesList;
    }
}
