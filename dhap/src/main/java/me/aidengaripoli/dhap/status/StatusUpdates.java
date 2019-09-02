package me.aidengaripoli.dhap.status;

import me.aidengaripoli.dhap.Device;
import me.aidengaripoli.dhap.PacketCodes;
import me.aidengaripoli.dhap.UdpPacketSender;

public class StatusUpdates {
    private UdpPacketSender udpPacketSender;
    public StatusUpdates() {
        udpPacketSender = UdpPacketSender.getInstance();
    }

    public void requestStatusLease(Device device, float leaseLength, float updatePeriod, boolean responseRequired){
        String statusLeaseRequest = PacketCodes.STATUS_LEASE_REQUEST + PacketCodes.PACKET_CODE_DELIM
                + leaseLength + "," + updatePeriod + ",";
        statusLeaseRequest += responseRequired ? "T" : "F";

        udpPacketSender.sendUdpPacketToIP(statusLeaseRequest, device.getIpAddress().getHostAddress());

        if(responseRequired) {
            listenForUpdates(device);
        }
    }

    public void leaveLease(Device device) {
        udpPacketSender.sendUdpPacketToIP(PacketCodes.STATUS_END_LEASE, device.getIpAddress().getHostAddress());
    }

    public void listenForUpdates(Device device){
        udpPacketSender.addPacketListener(device.getDeviceDescription());
    }

    public void stopListeningForUpdates(Device device) {
        udpPacketSender.removePacketListener(device.getDeviceDescription());
    }
}
