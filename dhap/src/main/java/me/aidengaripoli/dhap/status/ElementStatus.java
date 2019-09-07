package me.aidengaripoli.dhap.status;

public class ElementStatus {

    private int fragmentTag;
    private String value;

    public ElementStatus(int fragmentTag, String value) {
        this.fragmentTag = fragmentTag;
        this.value = value;
    }

    public int getFragmentTag() {
        return fragmentTag;
    }

    public String getValue() {
        return value;
    }
}
