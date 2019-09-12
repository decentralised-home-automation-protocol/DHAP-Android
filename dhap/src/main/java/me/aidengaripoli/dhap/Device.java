package me.aidengaripoli.dhap;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.ViewGroup;

import androidx.fragment.app.FragmentManager;

import java.net.InetAddress;

import me.aidengaripoli.dhap.display.DeviceActivity;
import me.aidengaripoli.dhap.display.DeviceLayout;
import me.aidengaripoli.dhap.display.DeviceLayoutBuilder;

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
    private DeviceLayout deviceLayout;
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
        deviceLayout = in.readParcelable(getClass().getClassLoader());
        status = in.readInt();
        visibility = in.readInt();
        name = in.readString();
        room = in.readString();
    }

    public DeviceLayout getDeviceLayout() {
        return deviceLayout;
    }

    public void newDeviceLayout(String xml) {
        deviceLayout = new DeviceLayout(xml);
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
        dest.writeParcelable(deviceLayout, 0);
        dest.writeInt(status);
        dest.writeInt(visibility);
        dest.writeString(name);
        dest.writeString(room);
    }

    public boolean isDebugDevice() {
        return macAddress == null && ipAddress == null;
    }

    public ViewGroup getDeviceViewGroup(FragmentManager supportFragmentManager, Context context) {
        DeviceLayoutBuilder layout = new DeviceLayoutBuilder(supportFragmentManager, context);
        return layout.create(getDeviceLayout(), name);
    }
}

