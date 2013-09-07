package com.paulbuckley.blescanner;

import android.bluetooth.BluetoothDevice;

import com.paulbuckley.blescanner.ble_standards.BleFlags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by paulb on 9/6/13.
 */
public class
AdvertisingBleDevice
{

    public BluetoothDevice device = null;
    public int rssi = 0;
    public byte[] scanRecord;

    private HashMap< Integer, byte[] > packets;

    public
    AdvertisingBleDevice(
            BluetoothDevice device,
            int rssi,
            byte[] scanRecord
    )
    {
        this.device = device;
        this.rssi = rssi;
        this.scanRecord = scanRecord;

        packets = new HashMap<Integer, byte[]>();
        int i = 0;
        while( scanRecord[ i ] != 0 )
        {
            i += parseNextPacket( scanRecord, i ) + 1;
        }

        if( packets.containsKey( Integer.valueOf( AdType.FLAGS ) ) )
        {

        }

    }


    public String
    getLocalName()
    {
        String name = null;
        if( packets.containsKey( AdType.COMPLETE_LOCAL_NAME ) )
        {
            name = new String( packets.get( AdType.COMPLETE_LOCAL_NAME ) );
        }
        return name;
    }

    public ArrayList<UUID>
    getUuids()
    {
        return null;
    }

    public int
    getAppearance()
    {
        int appearance = 0;
        if( packets.containsKey( AdType.APPEARANCE ) )
        {

        }
        return appearance;
    }


    public byte[]
    getManufacturerData()
    {
        return null;
    }

    public BleFlags
    getFlags()
    {
        return null;
    }

    private int
    parseNextPacket(
            byte[] data,
            int index
    )
    {
        // A packet is composed as the following:
        // Byte 0 = length of packet data + packet type
        // Byte 1 = packet type (see https://www.bluetooth.org/en-us/specification/assigned-numbers/generic-access-profile)
        // Byte 2-(2 + length) = packet data

        // Get the packet length.
        int length = data[ index ];

        // Only add the packet if the length is greater than one.
        if ( ( length > 1 ) && ( data.length >= ( index + length ) ) )
        {
            Integer adType = Integer.valueOf( data[ index + 1 ] );
            byte[] packetData = new byte[ length ];
            System.arraycopy( data, index + 2, packetData, 0, length );

            packets.put( adType, packetData );
        }

        return length;
    }


    private static class AdType
    {
        public static final int FLAGS = 1;
        public static final int INCOMPLETE_16_BIT_UUID_LIST = 2;
        public static final int COMPLETE_16_BIT_UUID_LIST = 3;
        public static final int INCOMPLETE_32_BIT_UUID_LIST = 4;
        public static final int COMPLETE_32_BIT_UUID_LIST = 5;
        public static final int INCOMPLETE_128_BIT_UUID_LIST = 6;
        public static final int COMPLETE_128_BIT_UUID_LIST = 7;
        public static final int SHORTENED_LOCAL_NAME = 8;
        public static final int COMPLETE_LOCAL_NAME = 9;
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
    }

}
