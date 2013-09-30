package com.paulbuckley.blescanner.utilities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import com.paulbuckley.blescanner.ble_standards.GattUuids;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * Created by paulb on 9/27/13.
 */
public class
Peripheral
{
    private final static String TAG = Peripheral.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private BleCommandQueuer commands;
    private Context mContext;
    private GattCallback mGattCallback;

    private String mAddress;
    private int mConnectionState; // @TODO Implement the usage of this so we can disconnect/reconnect.

    public final static String ACTION_GATT_CONNECTED = TAG + ".ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = TAG + ".ACTION_GATT_DISCONNECTED";
    public final static String ACTION_SERVICES_DISCOVERED = TAG + ".ACTION_SERVICES_DISCOVERED";
    public final static String EXTRA_DATA = TAG + ".EXTRA_DATA";
    public final static String EXTRA_CHARACTERISTIC_UUID = TAG + ".EXTRA_CHARACTERISTIC_UUID";
    public final static String ACTION_CHARACTERISTIC_READ = TAG + ".ACTION_CHARACTERISTIC_READ";
    public final static String EXTRA_CHARACTERISTIC_READ_FAIL_REASON = TAG + ".EXTRA_CHARACTERISTIC_READ_FAIL_REASON";
    public final static String ACTION_RSSI_UPDATED = TAG + ".ACTION_RSSI_UPDATED";
    public final static String EXTRA_RSSI_VALUE = TAG + ".EXTRA_RSSI_VALUE";
    public final static String ACTION_CHARACTERISTIC_WRITE_COMPLETE = TAG + ".ACTION_CHARACTERISTIC_WRITE_COMPLETE";
    public final static String ACTION_DESCRIPTOR_WRITE_COMPLETE = TAG + ".ACTION_DESCRIPTOR_WRITE_COMPLETE";
    public final static String ACTION_RELIABLE_CHARACTERISTIC_WRITE_COMPLETE = TAG + ".ACTION_RELIABLE_CHARACTERISTIC_WRITE_COMPLETE";
    public final static String ACTION_DESCRIPTOR_READ = TAG + ".ACTION_DESCRIPTOR_READ";


    public
    Peripheral(
            Context context,
            String address
    )
    {
        this( context, address, true );
    }


    public
    Peripheral(
            Context context,
            String address,
            boolean connectNow
    )
    {
        this.mContext = context;
        this.mAddress = address;

        this.mConnectionState = BluetoothProfile.STATE_DISCONNECTED;

        Log.d( TAG, "Peripheral created with address: " + address );

        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if ( mBluetoothManager == null )
        {
            mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null)
        {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
        }

        this.connect();
    }


    private BluetoothGattCharacteristic
    getCharacteristicFromUuidStrings(
            String serviceUuidString,
            String characteristicUuidString
    )
    {

        if( characteristicUuidString == null || serviceUuidString == null ) return null;

        UUID serviceUuid = UUID.fromString( serviceUuidString );
        UUID characteristicUuid = UUID.fromString( characteristicUuidString );

        if( serviceUuid == null || characteristicUuid == null ) return null;

        BluetoothGattService service = mBluetoothGatt.getService( serviceUuid );

        if( service == null ) return null;

        return service.getCharacteristic( characteristicUuid );
    }


    /***********************************************************************************************
     *
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean
    connect( )
    {
        // @TODO Check for connection state here.

        Log.d( TAG, "Attempting to connect to peripheral with address: " + mAddress );

        if( mBluetoothAdapter == null || mAddress == null ) return false;
        Log.d( TAG, "Bluetooth adapter and address set." );

        // Get the remote device reference.
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice( mAddress );
        if( device == null ) return false;
        Log.d( TAG, "Bluetooth device found." );

        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mGattCallback = new GattCallback( mContext );
        mBluetoothGatt = device.connectGatt( this.mContext, false, mGattCallback );
        Log.d( TAG, "Connected to the GATT server." );

        // Can only create the command queuer if we've successfully connected to the GATT.
        commands = new BleCommandQueuer( mBluetoothGatt );
        mGattCallback.setQueuer( commands );

        return true;
    }


    /***********************************************************************************************
     *
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void
    disconnect()
    {
        if ( mBluetoothAdapter == null || mBluetoothGatt == null ) return;
        mBluetoothGatt.disconnect();
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }


    /***********************************************************************************************
     *
     * Discovers the services provided by this peripheral.
     *
     */
    public void
    discoverServices()
    {
        DiscoverServicesCommand discoverServicesCommand = new DiscoverServicesCommand( );
        commands.add( discoverServicesCommand );
    }


    /***********************************************************************************************
     * Reads the RSSI of the connection.
     */
    public void
    readRssi()
    {
        ReadRssiCommand readRssiCommand = new ReadRssiCommand();
        commands.add( readRssiCommand );
    }


    /***********************************************************************************************
     *
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void
    readCharacteristic(
            BluetoothGattCharacteristic characteristic
    )
    {
        if ( mBluetoothAdapter == null || mBluetoothGatt == null )
        {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        if( characteristic != null )
        {
            if( ( characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ ) != 0 )
            {
                Log.d( TAG, "readCharactertistic called" );
                ReadBleCharacteristic cmd = new ReadBleCharacteristic( characteristic );
                commands.add( cmd );
            }
        }
    }


    public void
    writeCharaceristic(
            BluetoothGattCharacteristic characteristic,
            byte[] data
    )
    {
        this.writeCharaceristic( characteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT );
    }


    public int
    writeCharaceristic(
            BluetoothGattCharacteristic characteristic,
            byte[] data,
            int writeType
    )
    {
        int result = BluetoothGatt.GATT_FAILURE;
        if ( mBluetoothAdapter == null || mBluetoothGatt == null )
        {
            Log.w(TAG, "BluetoothAdapter not initialized");
        }
        else if( characteristic != null )
        {
            int propertiesBitmask = characteristic.getProperties();

            // First check that the characteristic has write permissions
            if( ( propertiesBitmask & BluetoothGattCharacteristic.PROPERTY_WRITE ) != 0 )
            {
                if( characteristic.setValue( data ) )
                {
                    WriteBleCharacteristic cmd = new WriteBleCharacteristic( characteristic );
                    commands.add( cmd );

                    result = BluetoothGatt.GATT_SUCCESS;
                }
            }
            else
            {
                result = BluetoothGatt.GATT_WRITE_NOT_PERMITTED;
            }
        }

        return result;
    }


    /***********************************************************************************************
     *
     */
    public void
    readDescriptor(
            BluetoothGattDescriptor descriptor
    )
    {
        // if( ( descriptor.getPermissions() & BluetoothGattDescriptor.PERMISSION_READ ) != 0 )
        //{
        ReadBleDescriptor cmd = new ReadBleDescriptor( descriptor );
        commands.add( cmd );
        //}
    }


    /***********************************************************************************************
     *
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void
    setCharacteristicNotification(
            BluetoothGattCharacteristic characteristic,
            boolean enabled
    )
    {
        if (mBluetoothAdapter == null || mBluetoothGatt == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        if( ( characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY ) != 0 )
        {
            SetBleNotificationCommand cmd = new SetBleNotificationCommand( characteristic, enabled );
            commands.add( cmd );
        }
    }


    /***********************************************************************************************
     *
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void
    setCharacteristicIndication(
            BluetoothGattCharacteristic characteristic,
            boolean enabled
    )
    {
        if (mBluetoothAdapter == null || mBluetoothGatt == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        if( ( characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE ) != 0 )
        {
            SetBleIndicationCommand cmd = new SetBleIndicationCommand( characteristic, enabled );
            commands.add( cmd );
        }
    }


    /***********************************************************************************************
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService>
    getSupportedGattServices()
    {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }


    /***********************************************************************************************
     *
     */
    private class ReadBleCharacteristic
            implements BleCommand
    {
        private BluetoothGattCharacteristic mCharacteristic;

        public
        ReadBleCharacteristic(
                BluetoothGattCharacteristic characteristic
        )
        {
            mCharacteristic = characteristic;
        }


        public String
        toString()
        {
            return "ReadBleCharacteristic";
        }


        @Override
        public boolean
        run(
                BluetoothGatt gatt
        )
        {
            if( ( mCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ ) == 0) return false;
            return gatt.readCharacteristic( mCharacteristic );
        }
    }


    /***********************************************************************************************
     *
     */
    private class ReadRssiCommand
            implements BleCommand
    {
        public boolean
        run( BluetoothGatt gatt )
        {
            return gatt.readRemoteRssi();
        }


        public String
        toString()
        {
            return "ReadRssiCommand";
        }
    }


    /***********************************************************************************************
     *
     */
    private class WriteBleCharacteristic
            implements BleCommand
    {
        private BluetoothGattCharacteristic mCharacteristic;

        public
        WriteBleCharacteristic(
                BluetoothGattCharacteristic characteristic
        )
        {
            mCharacteristic = characteristic;
        }


        public String
        toString()
        {
            return "WriteBleCharacteristic";
        }


        public boolean
        run( BluetoothGatt gatt )
        {
            //boolean success =  mCharacteristic.setValue( mData );
            //if( success )
            //{
            return gatt.writeCharacteristic( mCharacteristic );
            //}
            //return success;
        }
    }


    /***********************************************************************************************
     *
     */
    private class ReadBleDescriptor
            implements BleCommand
    {

        private BluetoothGattDescriptor mDescriptor;

        public
        ReadBleDescriptor (
                BluetoothGattDescriptor descriptor
        )
        {
            mDescriptor = descriptor;
        }


        public String
        toString()
        {
            return "ReadBleDescriptor";
        }


        public boolean
        run( BluetoothGatt gatt )
        {
            return gatt.readDescriptor( mDescriptor );
        }
    }


    /***********************************************************************************************
     *
     */
    private class WriteBleDescriptor
            implements BleCommand
    {

        private BluetoothGattDescriptor mDescriptor;
        private byte[] mData;

        public
        WriteBleDescriptor (
                BluetoothGattDescriptor descriptor,
                byte[] data
        )
        {
            mDescriptor = descriptor;
            mData = data;

            mDescriptor.setValue( mData );
        }


        public String
        toString()
        {
            return "WriteBleDescriptor";
        }


        public boolean
        run( BluetoothGatt gatt )
        {
            return gatt.writeDescriptor( mDescriptor );
        }
    }


    /***********************************************************************************************
     *
     */
    private class SetBleNotificationCommand
            implements BleCommand
    {

        private BluetoothGattCharacteristic mCharacteristic;
        private boolean mEnabled;

        public SetBleNotificationCommand(
                BluetoothGattCharacteristic characteristic,
                boolean enabled
        )
        {
            mCharacteristic = characteristic;
            mEnabled = enabled;
        }


        public String
        toString()
        {
            return "SetBleNotificationCommand";
        }


        public boolean
        run( BluetoothGatt gatt )
        {
            gatt.setCharacteristicNotification( mCharacteristic, mEnabled );

            BluetoothGattDescriptor descriptor
                    = mCharacteristic.getDescriptor( UUID.fromString( GattUuids.CLIENT_CHARACTERISTIC_CONFIG ) );

            byte[] notificationSetting = ( mEnabled ) ?
                    BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
            descriptor.setValue( notificationSetting );

            return gatt.writeDescriptor( descriptor );
        }
    }


    /***********************************************************************************************
     *
     */
    private class SetBleIndicationCommand
            implements BleCommand
    {

        private BluetoothGattCharacteristic mCharacteristic;
        private boolean mEnabled;

        public SetBleIndicationCommand(
                BluetoothGattCharacteristic characteristic,
                boolean enabled
        )
        {
            mCharacteristic = characteristic;
            mEnabled = enabled;
        }


        public String
        toString()
        {
            return "SetBleIndicationCommand";
        }


        public boolean
        run( BluetoothGatt gatt )
        {
            gatt.setCharacteristicNotification( mCharacteristic, mEnabled );

            BluetoothGattDescriptor descriptor
                    = mCharacteristic.getDescriptor( UUID.fromString( GattUuids.CLIENT_CHARACTERISTIC_CONFIG ) );

            byte[] notificationSetting = ( mEnabled ) ?
                    BluetoothGattDescriptor.ENABLE_INDICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
            descriptor.setValue( notificationSetting );

            return gatt.writeDescriptor( descriptor );
        }
    }


    /***********************************************************************************************
     *
     */
    private class DiscoverServicesCommand
            implements BleCommand
    {
        public boolean
        run( BluetoothGatt gatt )
        {
            return gatt.discoverServices();
        }


        public String
        toString()
        {
            return "DiscoverServicesCommand";
        }
    }
}

