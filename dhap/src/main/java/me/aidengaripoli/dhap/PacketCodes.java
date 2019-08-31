package me.aidengaripoli.dhap;

public class PacketCodes {
    public static final String PACKET_CODE_DELIM = "|";

    public static final String SEND_CREDENTIALS = "100";
    public static final String ACKNOWLEDGE_CREDENTIALS = "110";
    public static final String JOINING_SUCCESS = "120";
    public static final String JOINING_FAILURE = "130";

    public static final String REQUEST_UI = "200";
    public static final String SEND_UI = "210";

    public static final String DISCOVERY_REQUEST = "300";
    public static final String DISCOVERY_RESPONSE = "310";
    public static final String DISCOVERY_HEADER_REQUEST = "320";
    public static final String DISCOVERY_HEADER_RESPONSE = "330";

    public static final String IOT_COMMAND = "400";

    public static final String STATUS_LEASE_REQUEST = "500";
    public static final String STATUS_LEASE_RESPONSE = "510";
    public static final String STATUS_END_LEASE = "520";
    public static final String STATUS_UPDATE = "530";

}
