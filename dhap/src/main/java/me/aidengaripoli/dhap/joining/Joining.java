package me.aidengaripoli.dhap.joining;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.isupatches.wisefy.WiseFy;
import com.isupatches.wisefy.callbacks.ConnectToNetworkCallbacks;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import me.aidengaripoli.dhap.PacketCodes;
import me.aidengaripoli.dhap.PacketListener;
import me.aidengaripoli.dhap.UdpPacketSender;
import me.aidengaripoli.dhap.joining.callbacks.BaseCallback;
import me.aidengaripoli.dhap.joining.callbacks.ConnectToNetworkCallback;

public class Joining {
    private static final String TAG = Joining.class.getSimpleName();
    private static final int TIMEOUT_IN_MILLIS = 10000;

    private WiseFy wiseFy;
    private UdpPacketSender udpPacketSender;
    private Context context;

    public Joining(Context context) {
        wiseFy = new WiseFy.Brains(context).logging(true).getSmarts();
        this.context = context;
        udpPacketSender = UdpPacketSender.getInstance();
    }

    public void connectToAccessPoint(String SSID, String password, ConnectToNetworkCallback callback) {
        addWiFi(SSID, password);

        wiseFy.disconnectFromCurrentNetwork();

        wiseFy.connectToNetwork(SSID, TIMEOUT_IN_MILLIS, new ConnectToNetworkCallbacks() {
            @Override
            public void connectedToNetwork() {
                Log.d(TAG, "connectToNetwork - connectedToNetwork");

                if (waitForWifiConnection()) {
                    callback.success();
                } else {
                    callback.failure();
                }
            }

            @Override
            public void failureConnectingToNetwork() {
                Log.d(TAG, "connectToNetwork - failureConnectingToNetwork");
                callback.failure();
            }

            @Override
            public void networkNotFoundToConnectTo() {
                Log.d(TAG, "connectToNetwork - networkNotFoundToConnectTo");
                callback.networkNotFound();
            }

            @Override
            public void wisefyFailure(int i) {
                Log.d(TAG, "connectToNetwork - wisefyFailure");
                callback.failure();
            }
        });

    }

    public void sendCredentials(String SSID, String password, BaseCallback callback) {
        String credentials = PacketCodes.SEND_CREDENTIALS + PacketCodes.PACKET_CODE_DELIM + SSID + "," + password;
        udpPacketSender.sendUdpBroadcastPacket(credentials);

        AtomicBoolean credentialsAcknowledged = new AtomicBoolean(false);
        PacketListener packetListener = (packetType, packetData, fromIP) -> {
            if (packetType.equals(PacketCodes.JOINING_SUCCESS)) {
                callback.success();
                return true;
            }

            if (packetType.equals(PacketCodes.ACKNOWLEDGE_CREDENTIALS)) {
                credentialsAcknowledged.set(true);
            }
            return false;
        };

        udpPacketSender.addPacketListener(packetListener);

        int timeOut = 20;

        while (!credentialsAcknowledged.get()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            timeOut--;
            udpPacketSender.sendUdpBroadcastPacket(credentials);

            if(timeOut < 0) {
                callback.failure();
                udpPacketSender.removePacketListener(packetListener);
                return;
            }
        }
    }

    public void joinDevice(String networkSSID, String networkPassword, String deviceSSID, String devicePassword, ConnectToNetworkCallback callback) {
        Log.e(TAG, "joinDevice: Starting joining");
        connectToAccessPoint(networkSSID, networkPassword, new ConnectToNetworkCallback() {
            @Override
            public void networkNotFound() {
                callback.networkNotFound();
            }

            @Override
            public void success() {
                Log.e(TAG, "Verified credentials");
                connectToAccessPoint(deviceSSID, devicePassword, new ConnectToNetworkCallback() {
                    @Override
                    public void networkNotFound() {
                        callback.networkNotFound();
                    }

                    @Override
                    public void success() {
                        Log.e(TAG, "Connected to ESP");
                        sendCredentials(networkSSID, networkPassword, callback);
                    }

                    @Override
                    public void failure() {
                        Log.e(TAG, "Failed to connect to ESP");
                        callback.failure();
                    }
                });
            }

            @Override
            public void failure() {
                Log.e(TAG, "Failed to verify credentials");
                callback.failure();
            }
        });
    }

    private boolean waitForWifiConnection() {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        int timeOut = 20;

        while (!mWifi.isConnected()) {
            try {
                mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                Thread.sleep(1000);
                timeOut--;
                Log.d(TAG, "waitForWifiConnection: timeout... " + timeOut);
                if (timeOut < 0) {
                    return false;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    private void addWiFi(String SSID, String password) {
        if (wiseFy.isWifiEnabled()) {
            Log.d(TAG, "wifi enabled");
        } else {
            wiseFy.enableWifi();
        }

        if (wiseFy.isNetworkSaved(SSID)) {
            wiseFy.removeNetwork(SSID);
        }

        wiseFy.addWPA2Network(SSID, password);
    }
}
