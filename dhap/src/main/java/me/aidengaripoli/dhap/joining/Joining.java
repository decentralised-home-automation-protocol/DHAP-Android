package me.aidengaripoli.dhap.joining;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import com.isupatches.wisefy.WiseFy;
import com.isupatches.wisefy.callbacks.ConnectToNetworkCallbacks;

import me.aidengaripoli.dhap.PacketCodes;
import me.aidengaripoli.dhap.PacketListener;
import me.aidengaripoli.dhap.UdpPacketSender;
import me.aidengaripoli.dhap.joining.callbacks.BaseCallback;
import me.aidengaripoli.dhap.joining.callbacks.ConnectToNetworkCallback;

import static android.content.Context.WIFI_SERVICE;

public class Joining {
    private static final String TAG = Joining.class.getSimpleName();
    private static final int TIMEOUT_IN_MILLIS = 10000;

    private WiseFy wiseFy;
    private WifiManager wifiManager;
    private Context context;
    private UdpPacketSender udpPacketSender;

    public Joining(Context context) {
        this.context = context;
        wifiManager = (WifiManager) context
                .getApplicationContext()
                .getSystemService(WIFI_SERVICE);

        wiseFy = new WiseFy.Brains(context).logging(true).getSmarts();

        udpPacketSender = UdpPacketSender.getInstance();
    }

    public void verifyWifiNetwork(String SSID, String password, ConnectToNetworkCallback callback) {
        boolean isWifiEnabled = wiseFy.isWifiEnabled();

        if (isWifiEnabled) {
            Log.d(TAG, "wifi enabled");
        } else {
            wiseFy.enableWifi();
        }

        if (wiseFy.isNetworkSaved(SSID)) {
            wiseFy.removeNetwork(SSID);
        }

        wiseFy.addWPA2Network(SSID, password);

        connectToAP(SSID, password, callback);
    }

    public void connectToAP(String SSID, String password, ConnectToNetworkCallback callback) {
        if (!wiseFy.isNetworkSaved(SSID)) {
            wiseFy.addWPA2Network(SSID, password);
        }

        wiseFy.connectToNetwork(SSID, TIMEOUT_IN_MILLIS, new ConnectToNetworkCallbacks() {
            @Override
            public void connectedToNetwork() {
                Log.d(TAG, "connectToNetwork - connectedToNetwork");
                callback.success();
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

        udpPacketSender.addPacketListener(new PacketListener() {
            @Override
            public void newPacket(String packetData) {
                Log.e(TAG, "newPacket: " + packetData);
                if(packetData.startsWith(PacketCodes.JOINING_SUCCESS)) {
                    udpPacketSender.removePacketListener(this);
                    callback.success();
                }
            }
        });
    }

    public void joinDevice(String networkSSID, String networkPassword, String deviceSSID, String devicePassword, ConnectToNetworkCallback callback) {
        verifyWifiNetwork(networkSSID, networkPassword, new ConnectToNetworkCallback() {
            @Override
            public void networkNotFound() {
                callback.networkNotFound();
            }

            @Override
            public void success() {
                Log.e(TAG, "Verified credentials");
                connectToAP(deviceSSID, devicePassword, new ConnectToNetworkCallback() {
                    @Override
                    public void networkNotFound() {
                        callback.networkNotFound();
                    }

                    @Override
                    public void success() {
                        Log.e(TAG, "Connected to ESP");
                        Handler handler = new Handler();
                        handler.postDelayed(() -> sendCredentials(networkSSID, networkPassword, callback), 10000);
                    }

                    @Override
                    public void failure() {
                        callback.failure();
                    }
                });
            }

            @Override
            public void failure() {
                callback.failure();
            }
        });
    }
}
