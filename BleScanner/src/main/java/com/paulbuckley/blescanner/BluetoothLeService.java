package com.paulbuckley.blescanner;


import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class
BluetoothLeService
        extends Service
{

    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private ArrayList< String > bleCommandsSeen = new ArrayList< String >();


    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private BleCommandQueuer commands;


    public final static String ACTION_GATT_CONNECTED =
            "com.paulbuckley.blescanner.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.paulbuckley.blescanner.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.paulbuckley.blescanner.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.paulbuckley.blescanner.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.paulbuckley.blescanner.EXTRA_DATA";
    public final static String EXTRA_CHARACTERISTIC =
            "com.paulbuckley.blescanner.EXTRA_CHARACTERISTIC";
    public final static String EXTRA_CHARACTERISTIC_UUID =
            "com.paulbuckley.blescanner.EXTRA_CHARACTERISTIC_UUID";
    public final static String ACTION_CHARACTERISTIC_READ =
            "com.paulbuckley.blescanner.ACTION_CHARACTERISTIC_READ";



    /***********************************************************************************************
     *
     */
    public enum BleOperationType
    {
        UNKNOWN_BLE_OPERATION,
        READ_CHARACTERISTIC,
        WRITE_CHARACTERISTIC,
        READ_DESCRIPTOR,
        WRITE_DESCRIPTOR,
        DISCOVER_SERVICES,
        SET_NOTIFICATION,
        SET_INDICATION
    };


    /***********************************************************************************************
     *
     */
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

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
                    intentAction = ACTION_GATT_CONNECTED;
                    mConnectionState = STATE_CONNECTED;

                    bleCommandsSeen.add( "Resp: Connection" );

                    broadcastUpdate(intentAction);

                    Log.i(TAG, "Connected to GATT server.");

                    // Attempts to discover services after successful connection.
                    Log.i(TAG, "Attempting to start service discovery" );

                    DiscoverBLeServices discoverBLeServices = new DiscoverBLeServices( );
                    commands.add( (BleCommand) discoverBLeServices );
                }
                else if ( newState == BluetoothProfile.STATE_DISCONNECTED )
                {
                    intentAction = ACTION_GATT_DISCONNECTED;
                    mConnectionState = STATE_DISCONNECTED;

                    Log.i(TAG, "Disconnected from GATT server.");

                    bleCommandsSeen.add( "Resp: Disconnection" );

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
                    bleCommandsSeen.add( "Resp: Services discovered" );
                    broadcastUpdate( ACTION_GATT_SERVICES_DISCOVERED );
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
                    bleCommandsSeen.add( "Resp: Characteristic read " + characteristic.getUuid().toString() );
                    broadcastUpdate(ACTION_CHARACTERISTIC_READ, characteristic);
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
                    bleCommandsSeen.add( "Descriptor read" );
                    broadcastUpdate( ACTION_DATA_AVAILABLE );
                }
            }


            @Override
            public void
            onCharacteristicChanged(
                    BluetoothGatt gatt,
                    BluetoothGattCharacteristic characteristic
            )
            {
                bleCommandsSeen.add( "Resp: Characteristic changed" );
                broadcastUpdate( ACTION_CHARACTERISTIC_READ, characteristic );
            }

            @Override
            public void
            onCharacteristicWrite(
                    BluetoothGatt gatt,
                    BluetoothGattCharacteristic characteristic,
                    int status
            )
            {
                bleCommandsSeen.add( "Resp: Characteristic written" );
                broadcastUpdate( ACTION_DATA_AVAILABLE );
            }

            @Override
            public void
            onDescriptorWrite(
                    BluetoothGatt gatt,
                    BluetoothGattDescriptor descriptor,
                    int status
            )
            {
                bleCommandsSeen.add( "Resp: Descriptor written" );
                broadcastUpdate( ACTION_DATA_AVAILABLE );
            }

            @Override
            public void
            onReadRemoteRssi(
                    BluetoothGatt gatt,
                    int rssi,
                    int status
            )
            {
                bleCommandsSeen.add( "Resp: RSSI read: " + rssi );
                broadcastUpdate( ACTION_DATA_AVAILABLE );
            }

            @Override
            public void
            onReliableWriteCompleted(
                    BluetoothGatt gatt,
                    int status
            )
            {
                bleCommandsSeen.add( "Resp: Reliable write complete" );
                broadcastUpdate( ACTION_DATA_AVAILABLE );
            }
    };


    /***********************************************************************************************
     *
     */
    private void
    broadcastUpdate(
            final String action
    )
    {
        final Intent intent = new Intent(action);
        sendBroadcast( intent );

        commands.callComplete();
    }


    /***********************************************************************************************
     *
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
            intent.putExtra( EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString() );
            intent.putExtra( EXTRA_CHARACTERISTIC_UUID, characteristic.getUuid().toString() );
        }

        sendBroadcast( intent );

        commands.callComplete();
    }


    /***********************************************************************************************
     *
     */
    public class LocalBinder
            extends Binder
    {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }



    /***********************************************************************************************
     *
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }



    /***********************************************************************************************
     *
     */
    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }



    /***********************************************************************************************
     *
     */
    private final IBinder mBinder = new LocalBinder();


    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean
    initialize()
    {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if ( mBluetoothManager == null )
        {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }


        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null)
        {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }


    /***********************************************************************************************
     *
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean
    connect(
            final String address
    )
    {
        if (mBluetoothAdapter == null || address == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if ( mBluetoothDeviceAddress != null
                && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null
        )
        {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");

            if ( mBluetoothGatt.connect() )
            {
                mConnectionState = STATE_CONNECTING;
                commands = new BleCommandQueuer( mBluetoothGatt );

                return true;
            }
            else
            {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null)
        {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }

        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt( this, false, mGattCallback );

        commands = new BleCommandQueuer( mBluetoothGatt );

        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
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
        if ( mBluetoothAdapter == null || mBluetoothGatt == null )
        {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }


    /***********************************************************************************************
     *
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void
    close()
    {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
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
            SetBleNotification cmd = new SetBleNotification( characteristic, enabled );
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
            SetBleIndication cmd = new SetBleIndication( characteristic, enabled );
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
    private class
    BleCommand
    {
        protected BleOperationType mType;

        public
        BleCommand( )
        {
            mType = BleOperationType.UNKNOWN_BLE_OPERATION;
        }

        public BleOperationType
        getType()
        {
            return mType;
        }

        public boolean
        run( BluetoothGatt gatt )
        {
            return false;
        }
    }


    /***********************************************************************************************
     *
     */
    private class ReadBleCharacteristic
            extends BleCommand
    {
        private BluetoothGattCharacteristic mCharacteristic;

        public
        ReadBleCharacteristic( BluetoothGattCharacteristic characteristic )
        {
            this.mType = BleOperationType.READ_CHARACTERISTIC;

            mCharacteristic = characteristic;
        }

        @Override
        public boolean
        run( BluetoothGatt gatt )
        {
            boolean success = false;

            if( ( mCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ ) != 0 )
            {
                success = gatt.readCharacteristic( mCharacteristic );
                bleCommandsSeen.add( "Cmd: Characteristic Read: " + mCharacteristic.getUuid().toString() );
            }

            return success;
        }
    }


    /***********************************************************************************************
     *
     */
    private class WriteBleCharacteristic
            extends BleCommand
    {
        private BluetoothGattCharacteristic mCharacteristic;

        public
        WriteBleCharacteristic(
                BluetoothGattCharacteristic characteristic
        )
        {
            mCharacteristic = characteristic;
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
            extends BleCommand
    {

        private BluetoothGattDescriptor mDescriptor;

        public
        ReadBleDescriptor (
                BluetoothGattDescriptor descriptor
        )
        {
            mDescriptor = descriptor;
            mType = BleOperationType.READ_DESCRIPTOR;
        }


        public boolean
        run( BluetoothGatt gatt )
        {
            bleCommandsSeen.add( "Cmd: Descriptor Read: " + mDescriptor.getUuid().toString() );
            return gatt.readDescriptor( mDescriptor );
        }
    }


    /***********************************************************************************************
     *
     */
    private class WriteBleDescriptor
            extends BleCommand
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

            mType = BleOperationType.READ_DESCRIPTOR;
        }


        public boolean
        run( BluetoothGatt gatt )
        {
            bleCommandsSeen.add( "Cmd: Descriptor Write: " + mDescriptor.getUuid().toString() );
            return gatt.writeDescriptor( mDescriptor );
        }
    }


    /***********************************************************************************************
     *
     */
    private class SetBleNotification
            extends BleCommand
    {

        private BluetoothGattCharacteristic mCharacteristic;
        private boolean mEnabled;

        public
        SetBleNotification (
                BluetoothGattCharacteristic characteristic,
                boolean enabled
        )
        {
            mCharacteristic = characteristic;
            mEnabled = enabled;

            mType = BleOperationType.SET_NOTIFICATION;
        }


        public boolean
        run( BluetoothGatt gatt )
        {
            bleCommandsSeen.add( "Cmd: Notification Set: " + mCharacteristic.getUuid().toString() );

            gatt.setCharacteristicNotification( mCharacteristic, mEnabled );

            BluetoothGattDescriptor descriptor
                    = mCharacteristic.getDescriptor( UUID.fromString( GattAttributes.CLIENT_CHARACTERISTIC_CONFIG ) );

            byte[] notificationSetting = ( mEnabled ) ?
                    BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
            descriptor.setValue( notificationSetting );

            return gatt.writeDescriptor( descriptor );
        }
    }


    /***********************************************************************************************
     *
     */
    private class SetBleIndication
            extends BleCommand
    {

        private BluetoothGattCharacteristic mCharacteristic;
        private boolean mEnabled;

        public
        SetBleIndication (
                BluetoothGattCharacteristic characteristic,
                boolean enabled
        )
        {
            mCharacteristic = characteristic;
            mEnabled = enabled;

            mType = BleOperationType.SET_INDICATION;
        }


        public boolean
        run( BluetoothGatt gatt )
        {
            bleCommandsSeen.add( "Cmd: Indication Set: " + mCharacteristic.getUuid().toString() );

            gatt.setCharacteristicNotification( mCharacteristic, mEnabled );

            BluetoothGattDescriptor descriptor
                    = mCharacteristic.getDescriptor( UUID.fromString( GattAttributes.CLIENT_CHARACTERISTIC_CONFIG ) );

            byte[] notificationSetting = ( mEnabled ) ?
                    BluetoothGattDescriptor.ENABLE_INDICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
            descriptor.setValue( notificationSetting );

            return gatt.writeDescriptor( descriptor );
        }
    }


    /***********************************************************************************************
     *
     */
    private class DiscoverBLeServices
            extends BleCommand
    {
        public
        DiscoverBLeServices ()
        {
            mType = BleOperationType.DISCOVER_SERVICES;
        }


        public boolean
        run( BluetoothGatt gatt )
        {
            bleCommandsSeen.add( "Cmd: Discover Services" );
            return gatt.discoverServices();
        }
    }


    /***********************************************************************************************
     *
     */
    private class
    BleCommandQueuer
    {
        private LinkedList< BleCommand > mCommandQueue;
        private BluetoothGatt mGatt;
        private boolean mRunning;


        public
        BleCommandQueuer(
                BluetoothGatt gatt
        )
        {
            mGatt = gatt;
            mCommandQueue = new LinkedList< BleCommand >();
            mRunning = false;
        }


        public boolean
        run()
        {
            boolean didRun = false;
            if( !mRunning )
            {
                if( mCommandQueue.peek() != null )
                {
                    BleCommand cmd = mCommandQueue.remove();
                    mRunning = true;
                    didRun = cmd.run( mGatt );
                    if( !didRun )
                    {
                        run();
                    }
                }
            }
            return didRun;
        }


        /*
         * Hook this into the BluetoothGattCallback so whenever there is a GATT callback this
         * function is called.
         */
        public void
        callComplete()
        {
            mRunning = false;
            this.run();
        }


        public void
        add(
                BleCommand command
        )
        {
            mCommandQueue.add( command );
            this.run();
        }
    }
}
