package me.aidengaripoli.dhap.display;

public class Status {

    private int groupId;

    private int elementId;

    private String value;

    Status(int groupId, int elementId, String value) {
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
