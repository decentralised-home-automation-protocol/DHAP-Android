package me.aidengaripoli.dhap;

import java.net.InetAddress;

public interface PacketListener {
    void newPacket(String packetType, String packetData, InetAddress fromIP);
}
