package me.aidengaripoli.dhap;

import android.os.Parcel;
import android.os.Parcelable;

import java.net.InetAddress;

import me.aidengaripoli.dhap.display.DeviceDescription;

/**
 *
 */
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
    private String room;
    private String macAddress;
    private InetAddress ipAddress;
    private DeviceDescription deviceDescription;
    private int status;
    private int visibility;

    public Device(String macAddress, InetAddress ipAddress, int status, int visibility) {
        this.macAddress = macAddress;
        this.ipAddress = ipAddress;
        this.status = status;
        this.visibility = visibility;
    }

    protected Device(Parcel in) {
        macAddress = in.readString();
        ipAddress = (InetAddress) in.readSerializable();
        deviceDescription = in.readParcelable(getClass().getClassLoader());
        status = in.readInt();
        visibility = in.readInt();
        name = in.readString();
        room = in.readString();
    }

    public void setDeviceDescription(DeviceDescription deviceDescription) {
        this.deviceDescription = deviceDescription;
    }

    public DeviceDescription getDeviceDescription() {
        return deviceDescription;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public int getStatus() {
        return status;
    }

    public int getVisibility() {
        return visibility;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    @Override
    public String toString() {
        return macAddress + "," + ipAddress + "," + status + "," + visibility;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(macAddress);
        dest.writeSerializable(ipAddress);
        dest.writeParcelable(deviceDescription, 0);
        dest.writeInt(status);
        dest.writeInt(visibility);
        dest.writeString(name);
        dest.writeString(room);
    }
}

