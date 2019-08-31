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
    private DatagramSocket datagramSocket;
    private static final int BUFFER_SIZE = 2048;

    private ArrayList<PacketListener> listeners;

    private static UdpPacketSender udpPacketSender;

    private UdpPacketSender() {
        try {
            datagramSocket = new DatagramSocket(UDP_PORT);
            datagramSocket.setBroadcast(true);

            listeners = new ArrayList<>();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        listenForPackets();
    }

    public static UdpPacketSender getInstance() {
        if(udpPacketSender == null) {
            udpPacketSender = new UdpPacketSender();
        }

        return udpPacketSender;
    }

    public void addPacketListener(PacketListener listener){
        if(listener != null) {
            listeners.add(listener);
        }
    }

    public void removePacketListener(PacketListener listener) {
        listeners.remove(listener);
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
                    for (PacketListener listener: listeners) {
                        listener.newPacket(new String(receiveBuffer, 0, receivePacket.getLength()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
