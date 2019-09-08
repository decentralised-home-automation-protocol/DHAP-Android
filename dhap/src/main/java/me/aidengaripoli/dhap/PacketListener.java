package me.aidengaripoli.dhap;

import java.net.InetAddress;

public interface PacketListener {
    boolean newPacket(String packetType, String packetData, InetAddress fromIP);
}
