package me.aidengaripoli.dhap;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.ViewGroup;

import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.HashMap;

import me.aidengaripoli.dhap.display.DeviceLayoutBuilder;
import me.aidengaripoli.dhap.display.elements.BaseElementFragment;
import me.aidengaripoli.dhap.status.ElementStatus;
import me.aidengaripoli.dhap.status.StatusLeaseCallbacks;
import me.aidengaripoli.dhap.status.StatusUpdates;

public class Device implements Parcelable {

    public static final Creator<Device> CREATOR = new Creator<Device>() {
        @Override
        public Device createFromParcel(Parcel in) {
            return new Device(in);
        }

        @Override
        public Device[] newArray(int size) {
            return new Device[size];
        }
    };

    private String name;
    private String location;
    private String macAddress;
    private String ipAddress;
    private int status;
    private int visibility;
    private int headerVersion;
    private StatusUpdates statusUpdates;
    private String xml;
    private HashMap<String, BaseElementFragment> elements;

    public Device(String macAddress, String ipAddress, int status, int visibility, int headerVersion) {
        this.macAddress = macAddress;
        this.ipAddress = ipAddress;
        this.status = status;
        this.visibility = visibility;
        this.headerVersion = headerVersion;
        this.statusUpdates = new StatusUpdates(this);
    }

    protected Device(Parcel in) {
        macAddress = in.readString();
        ipAddress = in.readString();
        status = in.readInt();
        visibility = in.readInt();
        headerVersion = in.readInt();
        name = in.readString();
        location = in.readString();
        xml = in.readString();
        statusUpdates = new StatusUpdates(this);
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getName() {
        return name;
    }

    public int getStatus(){
        return status;
    }

    public void setStatus(int status){
        this.status = status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setHeaderVersion(int version) {
        this.headerVersion = version;
    }

    public int getHeaderVersion() {
        return headerVersion;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public void setElements(HashMap<String, BaseElementFragment> elements) {
        this.elements = elements;
    }

    public void newStatusUpdate(ArrayList<ElementStatus> elementStatuses) {
        for (ElementStatus elementStatus : elementStatuses) {
            String key = String.valueOf(elementStatus.getFragmentTag());
            if (elements == null) {
                return;
            }
            if (elements.containsKey(key)) {
                BaseElementFragment element = elements.get(key);
                if (element != null) {
                    element.updateFragmentData(elementStatus.getValue());
                }
            }
        }
    }

    @Override
    public String toString() {
        return macAddress  + "," + status + "," + visibility + "," + headerVersion;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(macAddress);
        dest.writeString(ipAddress);
        dest.writeInt(status);
        dest.writeInt(visibility);
        dest.writeInt(headerVersion);
        dest.writeString(name);
        dest.writeString(location);
        dest.writeString(xml);
    }

    public boolean isDebugDevice() {
        return macAddress == null && ipAddress == null;
    }

    public ViewGroup getDeviceViewGroup(FragmentManager supportFragmentManager, Context context) {
        if (xml == null || xml.isEmpty()) {
            Log.e("Device", "getDeviceViewGroup: No XML");
            return null;
        }
        DeviceLayoutBuilder layout = new DeviceLayoutBuilder(supportFragmentManager, context);
        return layout.create(this);
    }

    public void requestStatusLease(float leaseLength, float updatePeriod, boolean responseRequired, StatusLeaseCallbacks statusLeaseCallbacks) {
        statusUpdates.requestStatusLease(leaseLength, updatePeriod, responseRequired, statusLeaseCallbacks);
    }

    public void leaveLease() {
        statusUpdates.leaveLease();
    }

    public void sendIoTCommand(String tag, String data) {
        UdpPacketSender.getInstance().sendUdpPacketToIP(PacketCodes.IOT_COMMAND + "|" + tag + "=" + data, ipAddress);
    }

    public void changeDeviceName(String name) {
        UdpPacketSender.getInstance().sendUdpPacketToIP(PacketCodes.CHANGE_NAME + "|" + name, ipAddress);
    }

    public void changeDeviceLocation(String location) {
        UdpPacketSender.getInstance().sendUdpPacketToIP(PacketCodes.CHANGE_LOCATION + "|" + location, ipAddress);
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}

