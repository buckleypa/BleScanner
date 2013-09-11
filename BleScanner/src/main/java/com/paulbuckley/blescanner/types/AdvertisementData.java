package com.paulbuckley.blescanner.types;

import com.paulbuckley.blescanner.ble_standards.BleAdTypes;
import com.paulbuckley.blescanner.ble_standards.BleAdvertisingFlags;
import com.paulbuckley.blescanner.ble_standards.BleAppearance;
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
        String stringRep = null;

        switch( type )
        {
            case BleAdTypes.COMPLETE_LOCAL_NAME:
            case BleAdTypes.SHORTENED_LOCAL_NAME:
                stringRep = new String( data );
                break;


            case BleAdTypes.TX_POWER_LEVEL:
                stringRep = Integer.toString( data[ 0 ] );
                break;


            case BleAdTypes.FLAGS:
                stringRep = BleAdvertisingFlags.toString( data[ 0 ] );
                break;


            case BleAdTypes.COMPLETE_16_BIT_UUID_LIST:
            case BleAdTypes.INCOMPLETE_16_BIT_UUID_LIST:
                stringRep = getUuidStringRepresentation( 2, data );
                break;


            case BleAdTypes.COMPLETE_128_BIT_UUID_LIST:
            case BleAdTypes.INCOMPLETE_128_BIT_UUID_LIST:
                stringRep = getUuidStringRepresentation( 16, data );
                break;


            case BleAdTypes.COMPLETE_32_BIT_UUID_LIST:
            case BleAdTypes.INCOMPLETE_32_BIT_UUID_LIST:
                stringRep = getUuidStringRepresentation( 4, data );
                break;


            case BleAdTypes.APPEARANCE:
            {
                int appearance = 0;
                if( data.length == 2)
                {
                    appearance = (( 0xFF & data[1] ) << 8 ) + ( 0xFF & data[0] );
                }
                stringRep = BleAppearance.toString( appearance );
            }
                break;


            default:
            {
                // Get the value as a hex array
                StringBuilder hexString = new StringBuilder();
                hexString.append( "0x" );
                for( byte hex: data )
                {
                    hexString.append( String.format( "%02X-", hex ) );
                }
                hexString.deleteCharAt( hexString.length() - 1 );
                stringRep = hexString.toString();
            }
                break;
        }

        return stringRep;
    }


    private static String
    getUuidStringRepresentation(
            int bytesPerUuid,
            byte[] data
    )
    {
        if( bytesPerUuid % 2 != 0 ) return "Invalid UUID";

        StringBuilder uuidsString = new StringBuilder();
        int i = 0;
        while( i < data.length )
        {
            StringBuilder uuidString = new StringBuilder();
            int j = bytesPerUuid - 1;
            while( j > 0 )
            {
                String uuidFragment = String.format("%02X%02X", data[i + j], data[i + j - 1]);
                uuidString.append( uuidFragment );
                j -= 2;
            }
            i += bytesPerUuid;

            String uuid = uuidString.toString();
            uuidsString.append( "0x" + uuid + " (" + GattUuids.getNameFrom16bitUuid( uuid ) + ")\n" );
        }

        return uuidsString.toString().trim();
    }
}
