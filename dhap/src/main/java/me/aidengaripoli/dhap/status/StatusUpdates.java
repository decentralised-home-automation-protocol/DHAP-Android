package me.aidengaripoli.dhap.status;

import android.util.Log;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;

import me.aidengaripoli.dhap.Device;
import me.aidengaripoli.dhap.PacketCodes;
import me.aidengaripoli.dhap.PacketListener;
import me.aidengaripoli.dhap.UdpPacketSender;

public class StatusUpdates implements PacketListener {
    private static final String END_OF_LEASE = "T";
    private static final int STATUS_LOCATION_START = 1;
    private UdpPacketSender udpPacketSender;
    private Device device;
    private float leaseLength;
    private float updatePeriod;
    private long lastUpdateTime;

    private boolean responseRequired;
    private AtomicBoolean isListening;
    private StatusLeaseCallbacks statusLeaseCallbacks;

    public StatusUpdates(Device device) {
        udpPacketSender = UdpPacketSender.getInstance();
        isListening = new AtomicBoolean(false);
        this.device = device;
    }

    public void requestStatusLease(float leaseLength, float updatePeriod, boolean responseRequired, StatusLeaseCallbacks statusLeaseCallbacks) {
        this.statusLeaseCallbacks = statusLeaseCallbacks;
        sendLeaseRequest(leaseLength, updatePeriod, responseRequired);

        this.leaseLength = leaseLength;
        this.updatePeriod = updatePeriod;
        this.responseRequired = responseRequired;
        lastUpdateTime = System.currentTimeMillis();

        new Thread(() -> {
            long sleepTime = (long) (updatePeriod*3);

            while(isListening.get()){
                if(System.currentTimeMillis() - lastUpdateTime > sleepTime){
                    Log.e("Status", "requestStatusLease: status updates are not longer being received. Requesting new lease...");
                    sendLeaseRequest(leaseLength, updatePeriod, responseRequired);
                }

                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void sendLeaseRequest(float leaseLength, float updatePeriod, boolean responseRequired) {
        String statusLeaseRequest = PacketCodes.STATUS_LEASE_REQUEST + PacketCodes.PACKET_CODE_DELIM
                + leaseLength + "," + updatePeriod + ",";
        statusLeaseRequest += responseRequired ? "T" : "F";

        udpPacketSender.sendUdpPacketToIP(statusLeaseRequest, device.getIpAddress());
        listenForUpdates();
    }

    private void listenForUpdates() {
        if (!isListening.get()) {
            udpPacketSender.addPacketListener(this);
            isListening.set(true);
        }
    }

    public void leaveLease() {
        udpPacketSender.sendUdpPacketToIP(PacketCodes.STATUS_END_LEASE, device.getIpAddress());
        stopListeningForUpdates();
    }

    private void stopListeningForUpdates() {
        if (isListening.get()) {
            udpPacketSender.removePacketListener(this);
            isListening.set(false);
        }
    }

    @Override
    public boolean newPacket(String packetType, String packetData, InetAddress fromIP) {
        if (packetType.equals(PacketCodes.STATUS_UPDATE)) {
            lastUpdateTime = System.currentTimeMillis();
            if (isFromCorrectDevice(packetData)) {
                ArrayList<ElementStatus> elementStatuses = getStatus(packetData);
                device.newStatusUpdate(elementStatuses);
            }
        } else if (packetType.equals(PacketCodes.STATUS_LEASE_RESPONSE)) {
            StringTokenizer st = new StringTokenizer(packetData, ",");

            float leaseLength = Float.parseFloat(st.nextToken());
            float updatePeriod = Float.parseFloat(st.nextToken());

            statusLeaseCallbacks.leaseResponse(leaseLength, updatePeriod);
        }
        return false;
    }

    private boolean isFromCorrectDevice(String packetData) {
        StringTokenizer st = new StringTokenizer(packetData, ",");
        return st.nextToken().equals(device.getMacAddress());
    }

    private ArrayList<ElementStatus> getStatus(String packetData) {
        ArrayList<ElementStatus> elementStatuses = new ArrayList<>();

        StringTokenizer st = new StringTokenizer(packetData, ",");
        st.nextToken();

        if (st.nextToken().equals(END_OF_LEASE)) {
            if (statusLeaseCallbacks.shouldRenewStatusLease()) {
                sendLeaseRequest(leaseLength, updatePeriod, responseRequired);
            }
        }

        int fragmentTag = STATUS_LOCATION_START;

        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            ElementStatus elementStatus = new ElementStatus(fragmentTag, token);
            elementStatuses.add(elementStatus);
            fragmentTag++;
        }

        return elementStatuses;
    }
}
