package com.paulbuckley.blescanner.utilities;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.concurrent.Callable;

/**
 * Created by paulb on 9/28/13.
 */
public class
GattCallback
        extends BluetoothGattCallback
{
    private static final String TAG = GattCallback.class.getSimpleName();

    private Context mContext;

    // @todo Remove this reference and instead use a broadcast receiver on the Peripheral
    private BleCommandQueuer mCommandQueuer;


    public
    GattCallback(
            Context context
    )
    {
        this.mContext = context;
    }


    public void
    setQueuer(
        BleCommandQueuer queuer
    )
    {
        this.mCommandQueuer = queuer;
    }


    @Override
    public void
    onConnectionStateChange(
            BluetoothGatt gatt,
            int status,
            int newState
    )
    {
        String intentAction;

        if ( newState == BluetoothProfile.STATE_CONNECTED )
        {
            intentAction = Peripheral.ACTION_GATT_CONNECTED;
            broadcastUpdate(intentAction);

            Log.i(TAG, "Connected to GATT server.");
        }
        else if ( newState == BluetoothProfile.STATE_DISCONNECTED )
        {
            intentAction = Peripheral.ACTION_GATT_DISCONNECTED;

            Log.i(TAG, "Disconnected from GATT server.");
            broadcastUpdate(intentAction);
        }
    }


    @Override
    public void
    onServicesDiscovered(
            BluetoothGatt gatt,
            int status
    )
    {
        if ( status == BluetoothGatt.GATT_SUCCESS )
        {
            Log.d( TAG, "Services discovered." );
            broadcastUpdate( Peripheral.ACTION_SERVICES_DISCOVERED );
        }
        else
        {
            Log.w( TAG, "onServicesDiscovered received: " + status );
        }
    }


    @Override
    public void
    onCharacteristicRead(
            BluetoothGatt gatt,
            BluetoothGattCharacteristic characteristic,
            int status
    )
    {
        if ( status == BluetoothGatt.GATT_SUCCESS )
        {
            Log.d( TAG, "Characteristic read with UUID: " + characteristic.getUuid().toString() );
            broadcastUpdate( Peripheral.ACTION_CHARACTERISTIC_READ, characteristic );
        }
        else
        {
            String statusString;
            switch( status )
            {
                case BluetoothGatt.GATT_FAILURE:
                    statusString = "GATT_FAILURE";
                    break;

                case BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION:
                    statusString = "GATT_INSUFFICIENT_AUTHENTICATION";
                    break;

                case BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION:
                    statusString = "GATT_INSUFFICIENT_ENCRYPTION";
                    break;

                case BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH:
                    statusString = "GATT_INVALID_ATTRIBUTE_LENGTH";
                    break;

                case BluetoothGatt.GATT_READ_NOT_PERMITTED:
                    statusString = "GATT_READ_NOT_PERMITTED";
                    break;

                default:
                    statusString = Integer.toString( status );
                    break;
            }

            Log.d( TAG, "Characteristic read FAILED! Status = " + statusString );
            broadcastUpdate( Peripheral.ACTION_CHARACTERISTIC_READ, status );
        }
    }


    @Override
    public void
    onDescriptorRead(
            BluetoothGatt gatt,
            BluetoothGattDescriptor descriptor,
            int status
    )
    {
        if( status == BluetoothGatt.GATT_SUCCESS )
        {
            Log.d( TAG, "Descriptor read with UUID: " + descriptor.getUuid().toString() );
            broadcastUpdate( Peripheral.ACTION_DESCRIPTOR_READ );
        }
    }


    @Override
    public void
    onCharacteristicChanged(
            BluetoothGatt gatt,
            BluetoothGattCharacteristic characteristic
    )
    {
        // @todo This shouldn't be "characteristic read", instead "characteristic changed"
        broadcastUpdate( Peripheral.ACTION_CHARACTERISTIC_READ, characteristic );
    }


    @Override
    public void
    onCharacteristicWrite(
            BluetoothGatt gatt,
            BluetoothGattCharacteristic characteristic,
            int status
    )
    {

        broadcastUpdate( Peripheral.ACTION_CHARACTERISTIC_WRITE_COMPLETE );
    }


    @Override
    public void
    onDescriptorWrite(
            BluetoothGatt gatt,
            BluetoothGattDescriptor descriptor,
            int status
    )
    {
        broadcastUpdate( Peripheral.ACTION_DESCRIPTOR_WRITE_COMPLETE );
    }


    @Override
    public void
    onReadRemoteRssi(
            BluetoothGatt gatt,
            int rssi,
            int status
    )
    {
        if( status == BluetoothGatt.GATT_SUCCESS )
        {
            final Intent intent = new Intent( Peripheral.ACTION_RSSI_UPDATED );
            intent.putExtra( Peripheral.EXTRA_RSSI_VALUE, rssi );

            mContext.sendBroadcast(intent);
        }

        mCommandQueuer.callComplete();
    }


    @Override
    public void
    onReliableWriteCompleted(
            BluetoothGatt gatt,
            int status
    )
    {
        broadcastUpdate( Peripheral.ACTION_RELIABLE_CHARACTERISTIC_WRITE_COMPLETE );
    }


    /***********************************************************************************************
     * TODO Remove this function
     */
    private void
    broadcastUpdate(
            final String action
    )
    {
        final Intent intent = new Intent(action);
        mContext.sendBroadcast(intent);

        mCommandQueuer.callComplete();
    }


    /***********************************************************************************************
     * TODO Remove this function
     */
    private void
    broadcastUpdate(
            final String action,
            int status
    )
    {
        final Intent intent = new Intent(action);
        intent.putExtra( Peripheral.EXTRA_CHARACTERISTIC_READ_FAIL_REASON, status );

        mContext.sendBroadcast(intent);

        mCommandQueuer.callComplete();
    }


    /***********************************************************************************************
     * TODO Remove this function.
     */
    private void
    broadcastUpdate(
            final String action,
            final BluetoothGattCharacteristic characteristic
    )
    {
        final Intent intent = new Intent(action);

        // For all other profiles, writes the data formatted in HEX.
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for(byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));
            intent.putExtra( Peripheral.EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString() );
            intent.putExtra( Peripheral.EXTRA_CHARACTERISTIC_UUID, characteristic.getUuid().toString() );
        }

        mContext.sendBroadcast(intent);

        mCommandQueuer.callComplete();
    }
}
