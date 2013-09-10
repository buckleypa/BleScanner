package com.paulbuckley.blescanner.types;

import com.paulbuckley.blescanner.ble_standards.BleAdTypes;
import com.paulbuckley.blescanner.ble_standards.BleAdvertisingFlags;
import com.paulbuckley.blescanner.ble_standards.GattUuids;
import com.paulbuckley.blescanner.exceptions.IllegalAdvertisementDataException;

/**
 * Created by paulb on 9/9/13.
 */
public class AdvertisementData {

    private int mAdType = 0;
    private byte[] mData = null;
    private String mStringRepresentation;

    public
    AdvertisementData(
            int type,
            byte[] data
    )
            throws IllegalAdvertisementDataException
    {
        if( !BleAdTypes.validAdType( type ) ) throw new IllegalAdvertisementDataException( "Advertisement data type unknown: " + Integer.toString( type ) );
        if( data.length < 1 ) throw new IllegalAdvertisementDataException( "Advertisement data too short." );

        mAdType = type;
        mData = data;

        // @TODO Parse to string depending on type.
        mStringRepresentation = parseToString( type, data );
    }

    public int
    getType()
    {
        return mAdType;
    }

    public byte[]
    getData()
    {
        return mData;
    }

    public String
    getName()
    {
        return BleAdTypes.getName( this.mAdType );
    }

    @Override
    public String
    toString()
    {
        return mStringRepresentation;
    }

    private static String
    parseToString(
            int type,
            byte[] data
    )
    {
        String str = null;

        switch( type )
        {
            case BleAdTypes.COMPLETE_LOCAL_NAME:
            case BleAdTypes.SHORTENED_LOCAL_NAME:
                str = new String( data );
                break;

            case BleAdTypes.TX_POWER_LEVEL:
                str = Integer.toString( data[ 0 ] );
                break;

            case BleAdTypes.FLAGS:
                str = BleAdvertisingFlags.toString( data[ 0 ] );
                break;

            case BleAdTypes.COMPLETE_16_BIT_UUID_LIST:

                StringBuilder uuidsString = new StringBuilder();
                int rawUuidsLength = data.length;
                int i = 0;
                while( i < rawUuidsLength )
                {
                    String uuid = String.format("%02X%02X", data[i + 1], data[i]);
                    uuidsString.append( "0x" + uuid );
                    uuidsString.append( " (" + GattUuids.getNameFrom16bitUuid( uuid ) + ")\n" );
                    i += 2;
                }
                str = uuidsString.toString().trim();
                break;

            default:
                // Get the value as a hex array
                StringBuilder hexString = new StringBuilder();
                hexString.append( "0x" );
                for( byte hex: data )
                {
                    hexString.append( String.format( "%02X-", hex ) );
                }
                hexString.deleteCharAt( hexString.length() - 1 );
                str = hexString.toString();
                break;
        }

        return str;
    }
}
