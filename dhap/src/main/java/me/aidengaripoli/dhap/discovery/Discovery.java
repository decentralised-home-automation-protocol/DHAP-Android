package me.aidengaripoli.dhap.discovery;

import android.text.TextUtils;
import android.util.Log;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import me.aidengaripoli.dhap.Device;
import me.aidengaripoli.dhap.PacketCodes;
import me.aidengaripoli.dhap.PacketListener;
import me.aidengaripoli.dhap.UdpPacketSender;
import me.aidengaripoli.dhap.discovery.callbacks.GetDiscoveredDevicesCallbacks;

/**
 *
 */
public final class Discovery implements PacketListener {

    private static final String TAG = Discovery.class.getSimpleName();
    private static final int LISTEN_TIMEOUT_IN_MILLIS = 1000;

    private List<Device> censusList;
    private List<Device> previousCensusList;
    private UdpPacketSender udpPacketSender;
    private List<Device> devices = new ArrayList<>();

    public Discovery() {
        censusList = new ArrayList<>();
        previousCensusList = new ArrayList<>();

        udpPacketSender = UdpPacketSender.getInstance();
    }

    /**
     * @param callback
     */
    public void discoverDevices(GetDiscoveredDevicesCallbacks callback) {
        new Thread(() -> {
            try {

                findDevices();

                if (censusList.size() > 0) {
                    callback.foundDevices(censusList);
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

    /**
     *
     */
    private void findDevices() {
        int emptyRepliesCount = 0;
        int listRepeatedCount = 0;

        while (true) {
            broadcastList();

            List<Device> repliedDevices = listenForReplies();

            Log.d(TAG, "Received (" + repliedDevices.size() + ") replies.");
            // received replies?
            if (repliedDevices.size() > 0) { // yes
                updateCensusList(repliedDevices);
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

    /**
     *
     */
    private void broadcastList() {
        Log.d(TAG, "Broadcasting list...");
        String censusListString = PacketCodes.DISCOVERY_REQUEST;

        if (censusList.size() > 0) {
            censusListString += "|" + TextUtils.join("-", censusList);
        }

        udpPacketSender.sendUdpBroadcastPacket(censusListString);
    }

    /**
     * @return
     */
    private List<Device> listenForReplies() {
        Log.d(TAG, "Listening to replies...");

        devices.clear();
        udpPacketSender.addPacketListener(this);

        // listen to replies for ~1 second
        try {
            Thread.sleep(LISTEN_TIMEOUT_IN_MILLIS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        udpPacketSender.removePacketListener(this);

        return devices;
    }

    @Override
    public void newPacket(String packetType, String packetData, InetAddress fromIP) {
        if(packetType.equals(PacketCodes.DISCOVERY_RESPONSE)) {
            Device device = parseReply(packetData, fromIP);
            devices.add(device);
        }
    }

    /**
     * @param packetData
     * @param fromIP
     * @return
     */
    private Device parseReply(String packetData, InetAddress fromIP) {
        String[] deviceString = packetData.split(",");

        return new Device(
                deviceString[0],
                fromIP,
                Integer.parseInt(deviceString[1]),
                Integer.parseInt(deviceString[2])
        );
    }

    /**
     * @param repliedDevices
     */
    private void updateCensusList(List<Device> repliedDevices) {
        // update list
        Log.d(TAG, "Updating list...");
        censusList.addAll(repliedDevices);
    }
}
