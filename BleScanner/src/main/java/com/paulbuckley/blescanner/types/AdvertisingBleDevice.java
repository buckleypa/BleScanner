package com.paulbuckley.blescanner.types;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.paulbuckley.blescanner.ble_standards.BleAdTypes;
import com.paulbuckley.blescanner.ble_standards.BleAdvertisingFlags;
import com.paulbuckley.blescanner.exceptions.IllegalAdvertisementDataException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by paulb on 9/6/13.
 */
public class
AdvertisingBleDevice
{
    private final static String TAG = AdvertisingBleDevice.class.getSimpleName();

    public BluetoothDevice device = null;
    public int rssi = 0;
    public byte[] scanRecord;

    private HashMap< Integer, byte[] > packets;
    private ArrayList< AdvertisementData > advertisementData;

    private String localName;
    private BleAdvertisingFlags flags;
    private ArrayList< String > uuids;
    private int txPowerLevel;
    private int deviceClass; // @TODO Make type
    private HashMap< UUID, byte[] > serviceData;
    private int appearance; // @TODO Make type
    private byte[] manufacturerSpecificData;

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
        advertisementData = new ArrayList<AdvertisementData>();

        int i = 0;
        while( scanRecord[ i ] != 0 )
        {
            i += parseNextPacket( scanRecord, i ) + 1;
        }
    }


    public ArrayList< AdvertisementData >
    getAdvertisementData()
    {
        return advertisementData;
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
            byte[] packetData = new byte[ length - 1 ];
            System.arraycopy( data, index + 2, packetData, 0, length - 1 );

            try
            {
                AdvertisementData adData = new AdvertisementData( adType, packetData );
                advertisementData.add( adData );
            }
            catch( IllegalAdvertisementDataException e )
            {
                Log.d( TAG, e.getMessage() );
            }
        }

        return length;
    }

}
