package me.aidengaripoli.dhap.display;

import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import me.aidengaripoli.dhap.Device;
import me.aidengaripoli.dhap.PacketCodes;
import me.aidengaripoli.dhap.PacketListener;
import me.aidengaripoli.dhap.UdpPacketSender;
import me.aidengaripoli.dhap.display.callbacks.fetchDeviceInterfaceCallbacks;

public class Display extends AppCompatActivity {
    private Context context;
    private UdpPacketSender udpPacketSender;

    public Display(Context context) {
        this.context = context;
        udpPacketSender = UdpPacketSender.getInstance();
    }

    public void fetchDeviceInterface(Device device, fetchDeviceInterfaceCallbacks callbacks) {
        if (device.isDebugDevice()) {
            Intent intent = new Intent(context, DeviceActivity.class);
            intent.putExtra("device", device);
            callbacks.deviceActivityIntent(intent);
        } else {
            AtomicBoolean responseReceived = new AtomicBoolean(false);

            PacketListener packetListener = (packetType, packetData, fromIP) -> {
                if (packetType.equals(PacketCodes.SEND_UI)) {
                    responseReceived.set(true);

                    if (!DeviceLayoutBuilder.isValidXml(packetData)) {
                        callbacks.invalidDisplayXmlFailure();
                    } else {
                        device.newDeviceLayout(packetData);

                        Intent intent = new Intent(context, DeviceActivity.class);
                        intent.putExtra("device", device);

                        callbacks.deviceActivityIntent(intent);
                    }
                    return true;
                }
                return false;
            };

            udpPacketSender.addPacketListener(packetListener);

            int timeOut = 20;

            while (!responseReceived.get()) {
                udpPacketSender.sendUdpPacketToIP(PacketCodes.REQUEST_UI, device.getIpAddress().getHostAddress());
                timeOut--;

                if (timeOut < 0) {
                    callbacks.displayTimeoutFailure();
                    udpPacketSender.removePacketListener(packetListener);
                    return;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
