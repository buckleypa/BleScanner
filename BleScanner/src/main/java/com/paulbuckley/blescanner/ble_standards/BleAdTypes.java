package com.paulbuckley.blescanner.ble_standards;

import java.util.HashMap;

/**
 * Created by paulb on 9/8/13.
 */
public class BleAdTypes
{
    public static final int FLAGS = 0x01;
    public static final int INCOMPLETE_16_BIT_UUID_LIST = 0x02;
    public static final int COMPLETE_16_BIT_UUID_LIST = 0x03;
    public static final int INCOMPLETE_32_BIT_UUID_LIST = 0x04;
    public static final int COMPLETE_32_BIT_UUID_LIST = 0x05;
    public static final int INCOMPLETE_128_BIT_UUID_LIST = 0x06;
    public static final int COMPLETE_128_BIT_UUID_LIST = 0x07;
    public static final int SHORTENED_LOCAL_NAME = 0x08;
    public static final int COMPLETE_LOCAL_NAME = 0x09;
    public static final int TX_POWER_LEVEL = 0x0A;
    public static final int CLASS_OF_DEVICE = 0x0D;
    public static final int SIMPLE_PAIRING_HASH_C = 0x0E;
    public static final int SIMPLE_PAIRING_RANDOMIZER_R = 0x0F;
    public static final int DEVICE_ID = 0x10;
    public static final int SECURITY_MANAGER_TK_VALUE = 0x11;
    public static final int SECURITY_MANAGER_OUT_OF_BAND_FLAGS = 0x12;
    public static final int SLAVE_CONNECTION_INTERVAL_RANGE = 0x13;
    public static final int SERVICE_SOLICITATION_16_BIT_UUID_LIST = 0x14;
    public static final int SERVICE_SOLICITATION_128_BIT_UUID_LIST = 0x15;
    public static final int SERVICE_DATA = 0x16;
    public static final int PUBLIC_TARGET_ADDRESS = 0x17;
    public static final int RANDOM_TARGET_ADDRESS = 0x18;
    public static final int APPEARANCE = 0x19;
    public static final int ADVERTISING_INTERVAL = 0x1A;
    public static final int THREE_D_INFORMATION_DATA = 0x3D;
    public static final int MANUFACTURER_SPECIFIC_DATA = 0xFF;

    private static HashMap< Integer, String > adTypeNames = new HashMap();

    static
    {
        adTypeNames.put( FLAGS, "Flags" );
        adTypeNames.put( INCOMPLETE_16_BIT_UUID_LIST, "Incomplete 16-bit UUID list" );
        adTypeNames.put( COMPLETE_16_BIT_UUID_LIST, "Complete 16-bit UUID list" );
        adTypeNames.put( INCOMPLETE_32_BIT_UUID_LIST, "Incomplete 32-bit UUID list" );
        adTypeNames.put( COMPLETE_32_BIT_UUID_LIST, "Complete 32-bit UUID list" );
        adTypeNames.put( INCOMPLETE_128_BIT_UUID_LIST, "Incomplete 128-bit UUID list" );
        adTypeNames.put( COMPLETE_128_BIT_UUID_LIST, "Complete 128-bit UUID list" );
        adTypeNames.put( SHORTENED_LOCAL_NAME, "Local name (short)" );
        adTypeNames.put( COMPLETE_LOCAL_NAME, "Local name" );
        adTypeNames.put( TX_POWER_LEVEL, "TX power level" );
        adTypeNames.put( CLASS_OF_DEVICE, "Device class" );
        adTypeNames.put( SIMPLE_PAIRING_HASH_C, "Simple pairing hash C" );
        adTypeNames.put( SIMPLE_PAIRING_RANDOMIZER_R, "Simple pairing randomizer R" );
        adTypeNames.put( DEVICE_ID, "Device ID" );
        adTypeNames.put( SECURITY_MANAGER_TK_VALUE, "Security manager TK value" );
        adTypeNames.put( SECURITY_MANAGER_OUT_OF_BAND_FLAGS, "Security manager out-of-band flags" );
        adTypeNames.put( SLAVE_CONNECTION_INTERVAL_RANGE, "Slave connection interval range" );
        adTypeNames.put( SERVICE_SOLICITATION_16_BIT_UUID_LIST, "Service solicitation 16-bit UUID list" );
        adTypeNames.put( SERVICE_SOLICITATION_128_BIT_UUID_LIST, "Service solicitation 128-bit UUID list" );
        adTypeNames.put( SERVICE_DATA, "Service data" );
        adTypeNames.put( PUBLIC_TARGET_ADDRESS, "Public target address" );
        adTypeNames.put( RANDOM_TARGET_ADDRESS, "Random target address" );
        adTypeNames.put( APPEARANCE, "Appearance" );
        adTypeNames.put( ADVERTISING_INTERVAL, "Advertising interval" );
        adTypeNames.put( THREE_D_INFORMATION_DATA, "3D information data" );
        adTypeNames.put( MANUFACTURER_SPECIFIC_DATA, "Manufacturer specific data" );
    }

    public static String
    getName(
            int adType
    )
    {
        String name = null;

        if( adTypeNames.containsKey( adType ) )
        {
            name = adTypeNames.get( adType );
        }

        return name;
    }

    public static String
    toString(
            int adType
    )
    {
        return getName( adType );
    }

    public static boolean
    validAdType(
            int adType
    )
    {
        return adTypeNames.containsKey( adType );
    }
}
