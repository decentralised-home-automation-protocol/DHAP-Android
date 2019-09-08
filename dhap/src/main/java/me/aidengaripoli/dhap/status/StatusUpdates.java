package me.aidengaripoli.dhap.status;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.StringTokenizer;

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
    private boolean responseRequired;

    private boolean isListening = false;
    private String mac;

    public StatusUpdates(Device device) {
        udpPacketSender = UdpPacketSender.getInstance();
        this.device = device;
    }

    public void requestStatusLease(float leaseLength, float updatePeriod, boolean responseRequired) {
        sendLeaseRequest(leaseLength, updatePeriod, responseRequired);

        this.leaseLength = leaseLength;
        this.updatePeriod = updatePeriod;
        this.responseRequired = responseRequired;
    }

    private void sendLeaseRequest(float leaseLength, float updatePeriod, boolean responseRequired) {
        String statusLeaseRequest = PacketCodes.STATUS_LEASE_REQUEST + PacketCodes.PACKET_CODE_DELIM
                + leaseLength + "," + updatePeriod + ",";
        statusLeaseRequest += responseRequired ? "T" : "F";

        udpPacketSender.sendUdpPacketToIP(statusLeaseRequest, device.getIpAddress().getHostAddress());
        listenForUpdates();
    }

    public void listenForUpdates() {
        if (!isListening) {
            udpPacketSender.addPacketListener(this);
            isListening = true;
        }
    }

    public void leaveLease() {
        udpPacketSender.sendUdpPacketToIP(PacketCodes.STATUS_END_LEASE, device.getIpAddress().getHostAddress());
        stopListeningForUpdates();
    }

    public void stopListeningForUpdates() {
        if (isListening) {
            udpPacketSender.removePacketListener(this);
            isListening = false;
        }
    }

    @Override
    public void newPacket(String packetType, String packetData, InetAddress fromIP) {
        if (packetType.equals(PacketCodes.STATUS_UPDATE)) {
            if(isFromCorrectDevice(packetData)) {
                ArrayList<ElementStatus> elementStatuses = getStatus(packetData);
                device.getDeviceLayout().newStatusUpdate(elementStatuses);
            }
        } else if (packetType.equals(PacketCodes.STATUS_LEASE_RESPONSE)) {
            StringTokenizer st = new StringTokenizer(packetData, ",");
            this.mac = st.nextToken();

            float leaseLength = Float.parseFloat(st.nextToken());
            float updatePeriod = Float.parseFloat(st.nextToken());

            device.getDeviceLayout().statusRequestResponse(leaseLength, updatePeriod);
        }
    }

    private boolean isFromCorrectDevice(String packetData) {
        StringTokenizer st = new StringTokenizer(packetData, ",");
        return st.nextToken().equals(mac);
    }

    private ArrayList<ElementStatus> getStatus(String packetData) {
        ArrayList<ElementStatus> elementStatuses = new ArrayList<>();

        StringTokenizer st = new StringTokenizer(packetData, ",");
        st.nextToken();

        if (st.nextToken().equals(END_OF_LEASE)) {
            if (device.getDeviceLayout().shouldRenewStatusLease()) {
                sendLeaseRequest(leaseLength, updatePeriod, responseRequired);
            }
        }

        int fragmentTag = STATUS_LOCATION_START;

        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            ElementStatus elementStatus = new ElementStatus(fragmentTag,  token);
            elementStatuses.add(elementStatus);
            fragmentTag++;
        }

        return elementStatuses;
    }
}
