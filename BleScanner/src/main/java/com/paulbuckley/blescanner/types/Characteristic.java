package com.paulbuckley.blescanner.types;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.text.format.Time;

import com.paulbuckley.blescanner.activities.ConnectedDeviceActivity;

import java.util.UUID;

/**
 * Created by paulb on 8/28/13.
 */
public class
        Characteristic
{
    private static final String TAG = Characteristic.class.getSimpleName();

    public static final String READ_REQUEST = TAG + ".READ_REQUEST";
    public static final String WRITE_REQUEST = TAG + ".WRITE_REQUEST";
    public static final String NOTIFY_START_REQUEST = TAG + ".NOTIFY_START_REQUEST";
    public static final String NOTIFY_STOP_REQUEST = TAG + ".NOTIFY_STOP_REQUEST";
    public static final String INDICATE_START_REQUEST = TAG + ".INDICATE_START_REQUEST";
    public static final String INDICATE_STOP_REQUEST = TAG + ".INDICATE_STOP_REQUEST";

    public static final String CHARACTERISTIC_UUID = TAG + ".CHARACTERISTIC_UUID";
    public static final String SERVICE_UUID = TAG + ".SERVICE_UUID";

    private BluetoothGattCharacteristic mCharacteristic;
    private Time mReadTime;
    private Context mContext;

    public boolean readable;
    public boolean writable;
    public boolean notifiable;
    public boolean indicatable;
    public boolean noResponseWritable;
    public boolean signedWritable;

    public Characteristic(
            Context context,
            BluetoothGattCharacteristic characteristic
    )
    {
        this.mContext = context;
        this.mCharacteristic = characteristic;
        this.mReadTime = new Time();
        this.mReadTime.setToNow();

        this.readable = ( ( characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ ) != 0 );
        this.writable = ( ( characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE ) != 0 );
        this.notifiable = ( ( characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY ) != 0 );
        this.indicatable = ( ( characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE ) != 0 );
        this.noResponseWritable = ( ( characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE ) != 0 );
        this.signedWritable = ( ( characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE ) != 0 );
    }


    public BluetoothGattCharacteristic
    get()
    {
        return mCharacteristic;
    }


    public Time
    getReadTime()
    {
        return mReadTime;
    }


    public boolean
    read()
    {
        if( this.readable != true ) return false;

        mReadTime.setToNow();

        Intent readIntent = new Intent( READ_REQUEST );
        readIntent.putExtra( CHARACTERISTIC_UUID, this.mCharacteristic.getUuid().toString() );
        readIntent.putExtra( SERVICE_UUID, this.mCharacteristic.getService().getUuid().toString() );
        mContext.sendBroadcast( readIntent );

        return true;
    }
}
