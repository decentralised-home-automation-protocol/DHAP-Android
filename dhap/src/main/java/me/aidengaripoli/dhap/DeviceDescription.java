package me.aidengaripoli.dhap;

import android.os.Parcel;
import android.os.Parcelable;

public class DeviceDescription implements Parcelable {

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

    public String group;
    public String room;

    private String xml;

    public DeviceDescription(String xml) {
        this.xml = xml;
    }

    protected DeviceDescription(Parcel in) {
        group = in.readString();
        room = in.readString();
        xml = in.readString();
    }

    public String getXml() {
        return xml;
    }

    public void registerStatusUpdatesThread() {

    }

    public void executeCommand(String tag, String data) {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(group);
        dest.writeString(room);
        dest.writeString(xml);
    }
}
