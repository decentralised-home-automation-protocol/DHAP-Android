package me.aidengaripoli.dhap;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class UdpPacketSender {
    private static final String TAG = UdpPacketSender.class.getSimpleName();
    private static final int UDP_PORT = 8888;
    private static final String BROADCAST_ADDRESS = "255.255.255.255";
    private static final int BUFFER_SIZE = 65507;
    private static final int DELIM_CHAR_INDEX = 3;
    private DatagramSocket datagramSocket;
    private final ArrayList<PacketListener> listeners = new ArrayList<>();
    private static UdpPacketSender udpPacketSender;

    private UdpPacketSender() {
        try {
            datagramSocket = new DatagramSocket(UDP_PORT);
            datagramSocket.setBroadcast(true);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        listenForPackets();
    }

    public static UdpPacketSender getInstance() {
        if (udpPacketSender == null) {
            udpPacketSender = new UdpPacketSender();
        }

        return udpPacketSender;
    }

    public void addPacketListener(PacketListener listener) {
        synchronized(listeners) {
            if (listener != null) {
                listeners.add(listener);
            }
        }
    }

    public void removePacketListener(PacketListener listener) {
        synchronized(listeners) {
            listeners.remove(listener);
        }
    }

    public void sendUdpPacketToIP(String data, String IP) {

        try {
            InetAddress address = InetAddress.getByName(IP);
            sendPacket(address, data);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void sendUdpBroadcastPacket(String data) {
        Log.e(TAG, "sendUdpBroadcastPacket: " + data);
        try {
            InetAddress address = InetAddress.getByName(BROADCAST_ADDRESS);
            sendPacket(address, data);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private void sendPacket(InetAddress address, String data) {
        new Thread(() -> {
            try {
                byte[] buffer = data.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, UDP_PORT);
                datagramSocket.send(packet);
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void listenForPackets() {
        new Thread(() -> {
            byte[] receiveBuffer = new byte[BUFFER_SIZE];
            InetAddress address = null;
            try {
                address = InetAddress.getByName(BROADCAST_ADDRESS);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            DatagramPacket receivePacket =
                    new DatagramPacket(receiveBuffer, receiveBuffer.length, address, UDP_PORT);

            while (true) {
                try {
                    datagramSocket.receive(receivePacket);
                    String packet = new String(receiveBuffer, 0, receivePacket.getLength());
                    String packetType = packet.substring(0, DELIM_CHAR_INDEX);
                    String packetData = "";
                    if(packet.length() > DELIM_CHAR_INDEX) {
                        packetData = packet.substring(DELIM_CHAR_INDEX+1);
                    }

                    //TODO: Ensure packet is a DHAP packet
                    synchronized(listeners) {
                        for (PacketListener listener : listeners) {
                            listener.newPacket(packetType, packetData, receivePacket.getAddress());
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void accessListenersList(int action) {

    }
}
