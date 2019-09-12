package me.aidengaripoli.dhap.display;

import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import java.net.InetAddress;

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
            udpPacketSender.addPacketListener(new PacketListener() {
                @Override
                public boolean newPacket(String packetType, String packetData, InetAddress fromIP) {
                    if (packetType.equals(PacketCodes.SEND_UI)) {
                        UdpPacketSender.getInstance().removePacketListener(this);

                        if (!DeviceLayoutBuilder.isValidXml(packetData)) {
                            callbacks.invalidDisplayXmlFailure();
                        } else {
                            device.newDeviceLayout(packetData);

                            Intent intent = new Intent(context, DeviceActivity.class);
                            intent.putExtra("device", device);

                            callbacks.deviceActivityIntent(intent);
                        }
                    }
                    return false;
                }
            });

            udpPacketSender.sendUdpPacketToIP(PacketCodes.REQUEST_UI, device.getIpAddress().getHostAddress());
        }
    }
}
