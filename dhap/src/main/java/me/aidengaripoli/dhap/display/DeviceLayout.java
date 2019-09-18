package me.aidengaripoli.dhap.display;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import me.aidengaripoli.dhap.display.elements.BaseElementFragment;
import me.aidengaripoli.dhap.status.ElementStatus;

public class DeviceLayout implements Parcelable {

    public static final Creator<DeviceLayout> CREATOR = new Creator<DeviceLayout>() {
        @Override
        public DeviceLayout createFromParcel(Parcel in) {
            return new DeviceLayout(in);
        }

        @Override
        public DeviceLayout[] newArray(int size) {
            return new DeviceLayout[size];
        }
    };

    private String xml;
    private HashMap<String, BaseElementFragment> elements;

    public DeviceLayout(String xml) {
        this.xml = xml;
    }

    private DeviceLayout(Parcel in) {
        xml = in.readString();
    }

    public String getXml() {
        return xml;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(xml);
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
}
