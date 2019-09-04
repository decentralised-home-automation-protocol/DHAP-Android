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
    private UdpPacketSender udpPacketSender;
    private Device device;
    private float leaseLength;
    private float updatePeriod;
    private boolean responseRequired;

    private boolean isListening = false;

    public StatusUpdates(Device device) {
        udpPacketSender = UdpPacketSender.getInstance();
        this.device = device;
    }

    public void requestStatusLease(float leaseLength, float updatePeriod, boolean responseRequired) {
        sendLeaseRequest(leaseLength, updatePeriod, responseRequired);

        if (!isListening && responseRequired) {
            udpPacketSender.addPacketListener(this);
            isListening = true;
        }

        this.leaseLength = leaseLength;
        this.updatePeriod = updatePeriod;
        this.responseRequired = responseRequired;
    }

    private void sendLeaseRequest(float leaseLength, float updatePeriod, boolean responseRequired) {
        String statusLeaseRequest = PacketCodes.STATUS_LEASE_REQUEST + PacketCodes.PACKET_CODE_DELIM
                + leaseLength + "," + updatePeriod + ",";
        statusLeaseRequest += responseRequired ? "T" : "F";

        udpPacketSender.sendUdpPacketToIP(statusLeaseRequest, device.getIpAddress().getHostAddress());
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
            ArrayList<ElementStatus> elementStatuses = getStatus(packetData);
            device.getDeviceDescription().newStatusUpdate(elementStatuses);
        } else if (packetType.equals(PacketCodes.STATUS_LEASE_RESPONSE)) {
            StringTokenizer st = new StringTokenizer(packetData, ",");
            st.nextToken();

            float leaseLength = Float.parseFloat(st.nextToken());
            float updatePeriod = Float.parseFloat(st.nextToken());

            device.getDeviceDescription().statusRequestResponse(leaseLength, updatePeriod);
        }
    }

    private ArrayList<ElementStatus> getStatus(String packetData) {
        ArrayList<ElementStatus> elementStatuses = new ArrayList<>();

        StringTokenizer st = new StringTokenizer(packetData, ",");
        st.nextToken();

        if (st.nextToken().equals(END_OF_LEASE)) {
            if (device.getDeviceDescription().shouldRenewStatusLease()) {
                sendLeaseRequest(leaseLength, updatePeriod, responseRequired);
            }
        }

        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            String groupId = token.split("-")[0];
            String elementId = token.split("-")[1].split("=")[0];
            String value = token.split("=")[1];
            ElementStatus elementStatus = new ElementStatus(Integer.parseInt(groupId), Integer.parseInt(elementId), value);
            elementStatuses.add(elementStatus);
        }

        return elementStatuses;
    }
}
