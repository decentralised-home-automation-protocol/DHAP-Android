package me.aidengaripoli.dhap.display;

public class ElementStatus {

    private int groupId;

    private int elementId;

    private String value;

    ElementStatus(int groupId, int elementId, String value) {
        this.groupId = groupId;
        this.elementId = elementId;
        this.value = value;
    }

    public int getGroupId() {
        return groupId;
    }

    public int getElementId() {
        return elementId;
    }

    public String getValue() {
        return value;
    }
}
