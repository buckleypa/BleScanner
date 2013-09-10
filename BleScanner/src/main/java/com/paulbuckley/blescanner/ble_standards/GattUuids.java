package com.paulbuckley.blescanner.ble_standards;


import java.util.HashMap;
import java.util.UUID;

public class GattUuids {

    private static HashMap<String, String> attributes = new HashMap();

    private static String BASE_UUID                     = "00000000-0000-1000-8000-00805f9b34fb";

    // Service UUIDs
    public static String GENERIC_ACCESS                 = "00001800-0000-1000-8000-00805f9b34fb";
    public static String GENERIC_ATTRIBUTE              = "00001801-0000-1000-8000-00805f9b34fb";
    public static String BATTERY_SERVICE                = "0000180f-0000-1000-8000-00805f9b34fb";
    public static String DEVICE_INFORMATION             = "0000180a-0000-1000-8000-00805f9b34fb";
    public static String HEART_RATE_SERVICE             = "0000180d-0000-1000-8000-00805f9b34fb";

    // Characteristic UUIDs
    public static String HEART_RATE_MEASUREMENT         = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String BATTERY_LEVEL_MEASUREMENT      = "00002a19-0000-1000-8000-00805f9b34fb";
    public static String DEVICE_NAME                    = "00002a00-0000-1000-8000-00805f9b34fb";
    public static String SERIAL_NUMBER_STRING           = "00002a25-0000-1000-8000-00805f9b34fb";
    public static String MANUFACTURER_NAME_STRING       = "00002a29-0000-1000-8000-00805f9b34fb";
    public static String DEVICE_APPEARANCE              = "00002a01-0000-1000-8000-00805f9b34fb";
    public static String RECONNECTION_ADDRESS           = "00002a03-0000-1000-8000-00805f9b34fb";
    public static String PREFERRED_CONNECTION_PARAMS    = "00002a04-0000-1000-8000-00805f9b34fb";
    public static String PERIPHERY_PRIVACY_FLAG         = "00002a02-0000-1000-8000-00805f9b34fb";
    public static String SERVICE_CHANGED                = "00002a05-0000-1000-8000-00805f9b34fb";

    // Descriptor UUIDs
    public static String CHARACTERISTIC_EXTENDED_PROPERTIES = "00002900-0000-1000-8000-00805f9b34fb";
    public static String CHARACTERISTIC_USER_DESCRIPTION    = "00002901-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG       = "00002902-0000-1000-8000-00805f9b34fb";
    public static String CHARACTERISTIC_PRESENTATION_FORMAT = "00002904-0000-1000-8000-00805f9b34fb";
    public static String CHARACTERISTIC_AGGREGATE_FORMAT    = "00002905-0000-1000-8000-00805f9b34fb";
    public static String VALID_RANGE                        = "00002906-0000-1000-8000-00805f9b34fb";
    public static String EXTERNAL_REPORT_REFERENCE          = "00002907-0000-1000-8000-00805f9b34fb";
    public static String REPORT_REFERENCE                   = "00002908-0000-1000-8000-00805f9b34fb";

    // Default string identifier for UUIDs we don't recognize.
    public static String UNKNOWN_UUID = "Unknown";

    static {
        // Sample Services.
        attributes.put( HEART_RATE_SERVICE, "Heart Rate Service");
        attributes.put( DEVICE_INFORMATION, "Device Information Service" );
        attributes.put( GENERIC_ACCESS, "Generic Access" );
        attributes.put( GENERIC_ATTRIBUTE, "Generic Attribute" );
        attributes.put( BATTERY_SERVICE, "Battery Service" );

        // Sample Characteristics.
        attributes.put( HEART_RATE_MEASUREMENT, "Heart Rate Measurement" );
        attributes.put( MANUFACTURER_NAME_STRING, "Manufacturer Name String" );
        attributes.put( BATTERY_LEVEL_MEASUREMENT, "Battery Level Measurement" );
        attributes.put( DEVICE_NAME, "Device Name" );
        attributes.put( SERIAL_NUMBER_STRING, "Serial Number String" );
        attributes.put( CLIENT_CHARACTERISTIC_CONFIG, "Client Characteristic Configuration" );
        attributes.put( DEVICE_APPEARANCE, "Device Appearance" );
        attributes.put( RECONNECTION_ADDRESS, "Reconnection Address" );
        attributes.put( PREFERRED_CONNECTION_PARAMS, "Peripheral Preferred Connection Params" );
        attributes.put( PERIPHERY_PRIVACY_FLAG, "Peripheral Privacy Flag" );
        attributes.put( SERVICE_CHANGED, "Service Changed" );

        // Descriptors
        attributes.put( CHARACTERISTIC_EXTENDED_PROPERTIES, "Characteristic Extended Properties" );
        attributes.put( CHARACTERISTIC_USER_DESCRIPTION, "Characteristic User Description" );
        attributes.put( CLIENT_CHARACTERISTIC_CONFIG, "Client Characteristics Configuration" );
        attributes.put( CHARACTERISTIC_PRESENTATION_FORMAT, "Characteristic Presentation Format" );
        attributes.put( CHARACTERISTIC_AGGREGATE_FORMAT, "Characteristic Aggregate Format" );
        attributes.put( VALID_RANGE, "Valid Range" );
        attributes.put( EXTERNAL_REPORT_REFERENCE, "External Report Reference" );
        attributes.put( REPORT_REFERENCE, "Report Reference" );
    }

    public static String
    lookup(
            String uuid
    )
    {
        String name = attributes.get(uuid);
        return name == null ? UNKNOWN_UUID : name;
    }

    public static String
    getNameFrom16bitUuid(
        String shortUuid
    )
    {
        String longUuid = BASE_UUID.substring(0,4) + shortUuid.toLowerCase() + BASE_UUID.substring(8,36);
        return lookup( longUuid );
    }

    public static boolean
    isKnownUuid(
            String uuid
    )
    {
        return attributes.containsKey( uuid );
    }


    public static void
    addUuid(
            String uuid,
            String name
    )
    {
        attributes.put( uuid, name );
    }


}