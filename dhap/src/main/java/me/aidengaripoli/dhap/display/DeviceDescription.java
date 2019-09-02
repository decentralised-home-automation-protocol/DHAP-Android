package me.aidengaripoli.dhap.display;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import me.aidengaripoli.dhap.Device;
import me.aidengaripoli.dhap.PacketCodes;
import me.aidengaripoli.dhap.PacketListener;
import me.aidengaripoli.dhap.display.elements.BaseElementFragment;

public class DeviceDescription implements Parcelable, PacketListener {

    private static final String TAG = DeviceDescription.class.getSimpleName();

    public static final Creator<DeviceDescription> CREATOR = new Creator<DeviceDescription>() {
        @Override
        public DeviceDescription createFromParcel(Parcel in) {
            return new DeviceDescription(in);
        }

        @Override
        public DeviceDescription[] newArray(int size) {
            return new DeviceDescription[size];
        }
    };

    public String name;
    public String room;

    private String xml;

    private Device device;

    private HashMap<String, BaseElementFragment> elements;

    public DeviceDescription(String xml, Device device) {
        this.xml = xml;
        this.device = device;
    }

    protected DeviceDescription(Parcel in) {
        name = in.readString();
        room = in.readString();
        xml = in.readString();
        device = in.readParcelable(getClass().getClassLoader());
    }

    public String getXml() {
        return xml;
    }

    public Device getDevice() {
        return device;
    }

    public void executeCommand(String tag, String data) {
        Log.d(TAG, "executeCommand");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(room);
        dest.writeString(xml);
        dest.writeParcelable(device, 0);
    }

    @Override
    public void newPacket(String packetData, InetAddress fromIP) {
        Log.d(TAG, "newPacket: " + packetData);
        for (Status status : getStatus(packetData)) {
            String tag = status.getGroupId() + "-" + status.getElementId();

            BaseElementFragment element = elements.get(tag);
            if(element != null) {
                element.updateFragmentData(status.getValue());
            } else {
                Log.d(TAG, "newPacket: No element with tag " + tag + " exists");
            }
        }
    }

    public void setElements(HashMap<String, BaseElementFragment> elements) {
        this.elements = elements;
    }

    private ArrayList<Status> getStatus(String packetData) {
        ArrayList<Status> statuses = new ArrayList<>();

        String temp = packetData.substring(4);

        StringTokenizer st = new StringTokenizer(temp, ",");
        st.nextToken();
        st.nextToken();

        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            String groupId = token.split("-")[0];
            String elementId = token.split("-")[1].split("=")[0];
            String value = token.split("=")[1];
            Status status = new Status(Integer.parseInt(groupId), Integer.parseInt(elementId), value);
            statuses.add(status);
        }

        return statuses;
    }
}
