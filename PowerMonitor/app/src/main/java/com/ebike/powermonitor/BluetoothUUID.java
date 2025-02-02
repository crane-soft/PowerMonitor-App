package com.ebike.powermonitor;
import java.util.UUID;


// https://novelbits.io/uuid-for-custom-services-and-characteristics/
public final class BluetoothUUID   {
    public static final String bleBase = "00000000-0000-1000-8000-00805f9b34fb";
    public static UUID fromString(String name) {
        int nameLen = name.length();
        String bleName;
        if (nameLen < 8) {
            bleName = bleBase.substring(0,8-nameLen) + name + bleBase.substring(8);
        } else {
            bleName = name;
        }
        return UUID.fromString(bleName);
    }
}
