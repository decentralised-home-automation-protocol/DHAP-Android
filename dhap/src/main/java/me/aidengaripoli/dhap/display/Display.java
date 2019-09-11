package me.aidengaripoli.dhap.display;

import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import java.net.InetAddress;

import me.aidengaripoli.dhap.Device;
import me.aidengaripoli.dhap.PacketCodes;
import me.aidengaripoli.dhap.PacketListener;
import me.aidengaripoli.dhap.UdpPacketSender;
import me.aidengaripoli.dhap.display.callbacks.DisplayCallbacks;

public class Display extends AppCompatActivity {
    private static final String TAG = Display.class.getSimpleName();

    private Context context;
    private UdpPacketSender udpPacketSender;

    public Display(Context context) {
        this.context = context;
        udpPacketSender = UdpPacketSender.getInstance();
    }

    public void fetchDeviceInterface(Device device, DisplayCallbacks callbacks) {
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

                        device.newDeviceLayout(packetData);

                        Intent intent = new Intent(context, DeviceActivity.class);
                        intent.putExtra("device", device);

                        callbacks.deviceActivityIntent(intent);
                    }
                    return false;
                }
            });

            udpPacketSender.sendUdpPacketToIP("200", device.getIpAddress().getHostAddress());
        }
    }
}
