package me.aidengaripoli.dhap;

import java.net.InetAddress;

public interface PacketListener {
    void newPacket(String packetData, InetAddress fromIP);
}
