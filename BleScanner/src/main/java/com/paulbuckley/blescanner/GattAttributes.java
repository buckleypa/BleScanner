package com.paulbuckley.blescanner;


import java.util.HashMap;

public class GattAttributes {
    private static HashMap<String, String> attributes = new HashMap();

    // Service UUIDs
    public static String GENERIC_ACCESS                 = "00001800-0000-1000-8000-00805f9b34fb";
    public static String GENERIC_ATTRIBUTE              = "00001801-0000-1000-8000-00805f9b34fb";
    public static String BATTERY_SERVICE                = "0000180f-0000-1000-8000-00805f9b34fb";
    public static String DEVICE_INFORMATION             = "0000180a-0000-1000-8000-00805f9b34fb";
    public static String HEART_RATE_SERVICE             = "0000180d-0000-1000-8000-00805f9b34fb";

    // Characteristic UUIDs
    public static String HEART_RATE_MEASUREMENT         = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG   = "00002902-0000-1000-8000-00805f9b34fb";

    // Default string identifier for UUIDs we don't recognize.
    private static String DEFAULT_NAME = "Unknown";

    static {
        // Sample Services.
        attributes.put( HEART_RATE_SERVICE, "Heart Rate Service");
        attributes.put( DEVICE_INFORMATION, "Device Information Service" );
        attributes.put( GENERIC_ACCESS, "Generic Access" );
        attributes.put( GENERIC_ATTRIBUTE, "Generic Attribute" );
        attributes.put( BATTERY_SERVICE, "Battery Service" );

        // Sample Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
    }

    public static String
    lookup(
            String uuid
    )
    {
        String name = attributes.get(uuid);
        return name == null ? DEFAULT_NAME : name;
    }
}