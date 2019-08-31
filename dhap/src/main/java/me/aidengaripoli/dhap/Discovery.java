package me.aidengaripoli.dhap;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import me.aidengaripoli.dhap.callbacks.discovery.GetDiscoveredDevicesCallbacks;

/**
 *
 */
public final class Discovery {

    private static final String TAG = Discovery.class.getSimpleName();
    private static final String BROADCAST_ADDRESS = "255.255.255.255";
    private static final int UDP_PORT = 8888;
    private static final int REPLY_BUFFER_SIZE = 25;
    private static final int SOCKET_TIMEOUT_IN_MILLIS = 1000;
    private static final int LISTEN_TIMEOUT_IN_MILLIS = 1000;
    private static final String DISCOVERY_REQUEST_CODE = "300";
    private static final String DISCOVERY_RESPONSE_CODE = "310";

    private DatagramSocket socket;
    private WifiManager wifiManager;

    private List<Device> censusList;
    private List<Device> previousCensusList;

    public Discovery(Context context) {
        wifiManager = (WifiManager) context
                .getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);

        censusList = new ArrayList<>();
        previousCensusList = new ArrayList<>();
    }

    /**
     *
     * @param callback
     */
    public void discoverDevices(GetDiscoveredDevicesCallbacks callback) {
        new Thread(() -> {
            try {
                socket = new DatagramSocket(UDP_PORT);
                socket.setBroadcast(true);
                socket.setSoTimeout(SOCKET_TIMEOUT_IN_MILLIS);

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
                if (socket != null) {
                    socket.close();
                }
            }
        }).start();
    }

    /**
     *
     * @throws IOException
     */
    private void findDevices() throws IOException {
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
     * @throws IOException
     */
    private void broadcastList() throws IOException {
        Log.d(TAG, "Broadcasting list...");
        byte[] buffer;

        String censusListString = DISCOVERY_REQUEST_CODE;

        if (censusList.size() > 0) {
            censusListString += "|" + TextUtils.join("-", censusList);
        }

        buffer = censusListString.getBytes();

        DatagramPacket packet = new DatagramPacket(
                buffer,
                buffer.length,
                InetAddress.getByName(BROADCAST_ADDRESS),
                UDP_PORT
        );

        socket.send(packet);
    }

    /**
     *
     * @return
     * @throws IOException
     */
    private List<Device> listenForReplies() throws IOException {
        Log.d(TAG, "Listening to replies...");

        List<Device> devices = new ArrayList<>();

        // listen to replies for ~1 second
        long finish = System.currentTimeMillis() + LISTEN_TIMEOUT_IN_MILLIS; // end time
        while (System.currentTimeMillis() < finish) {
            Log.d(TAG, "Waiting for reply...");

            byte[] receiveBuffer = new byte[REPLY_BUFFER_SIZE];
            DatagramPacket replyPacket = new DatagramPacket(
                    receiveBuffer,
                    receiveBuffer.length,
                    InetAddress.getByName(BROADCAST_ADDRESS),
                    UDP_PORT
            );

            try {
                socket.receive(replyPacket);

                // ignore packet from self.
                if (isReplyFromSelf(replyPacket)) {
                    continue;
                }
            } catch (SocketTimeoutException e) {
                Log.d(TAG, "Socket Timeout.");
                break;
            }

            Log.d(TAG, "Received reply from: " + replyPacket.getAddress().getHostAddress());

            Device device = parseReply(receiveBuffer, replyPacket);

            if (device != null) {
                devices.add(device);
            }
        }

        return devices;
    }

    /**
     *
     * @param receiveBuffer
     * @param replyPacket
     * @return
     */
    private Device parseReply(byte[] receiveBuffer, DatagramPacket replyPacket) {
        try {
            String[] contents = new String(receiveBuffer).split("\\|");
            if (!contents[0].equals(DISCOVERY_RESPONSE_CODE)) {
                throw new Exception();
            }
            String[] deviceString = contents[1].split(",");

            return new Device(
                    deviceString[0],
                    replyPacket.getAddress(),
                    Integer.parseInt(deviceString[1]),
                    Integer.parseInt(deviceString[2])
            );
        } catch (Exception e) {
            Log.d(TAG, "Device is not compliant, ignoring.");
        }

        return null;
    }

    /**
     *
     * @param repliedDevices
     */
    private void updateCensusList(List<Device> repliedDevices) {
        // update list
        Log.d(TAG, "Updating list...");

        for (Device device : repliedDevices) {
            censusList.add(device);
        }
    }

    /**
     *
     * @param replyPacket
     * @return
     * @throws UnknownHostException
     */
    private boolean isReplyFromSelf(DatagramPacket replyPacket) throws UnknownHostException {
        return replyPacket.getAddress().getHostAddress().equals(getIpAddress());
    }

    /**
     *
     * @return
     * @throws UnknownHostException
     */
    private String getIpAddress() throws UnknownHostException {
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        ipAddress = (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN))
                ? Integer.reverseBytes(ipAddress) : ipAddress;

        byte[] bytes = BigInteger.valueOf(ipAddress).toByteArray();

        return InetAddress.getByAddress(bytes).getHostAddress();
    }
}
