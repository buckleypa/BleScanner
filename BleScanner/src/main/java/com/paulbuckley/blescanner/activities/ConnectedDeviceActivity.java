package com.paulbuckley.blescanner.activities;

import android.app.AlertDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.paulbuckley.blescanner.adapters.ConnectedDeviceAdapter;
import com.paulbuckley.blescanner.types.Characteristic;
import com.paulbuckley.blescanner.R;
import com.paulbuckley.blescanner.utilities.Peripheral;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.paulbuckley.blescanner.utilities.BluetoothLeService.*;

public class ConnectedDeviceActivity
        extends Activity
{
    private final static String TAG = ConnectedDeviceActivity.class.getSimpleName();

    // Identifiers for establishing the initial connection.
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private final long RSSI_READ_PERIOD_MS = 4000; // ms

    private String mDeviceName;
    private String mDeviceAddress;

    private Context mContext;
    private Handler mHandler;

    private List< BluetoothGattService > mServices;
    private Map< BluetoothGattService, List<Characteristic>> mServiceCharacteristics;
    private Map< UUID, Characteristic> mCharacteristicMapping;

    private ConnectedDeviceAdapter mServicesAdapter;

    protected ActionMode mActionMode;

    private Peripheral mPeripheral;


    /***********************************************************************************************
     *
     *
    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
     */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void
        onReceive(
                Context context,
                Intent intent
        )
        {
            final String action = intent.getAction();

            if ( Peripheral.ACTION_GATT_CONNECTED.equals(action))
            {
                mPeripheral.discoverServices();
            }

            else if ( Peripheral.ACTION_GATT_DISCONNECTED.equals(action))
            {
                // @TODO Do we want to return to the previous screen or explain WHY the disconnect
                // happened?

                Intent exitIntent = new Intent( mContext, AdvertisingDevicesActivity.class );
                startActivity( exitIntent );
                finish();
            }

            else if ( Peripheral.ACTION_SERVICES_DISCOVERED.equals(action))
            {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices( mPeripheral.getSupportedGattServices() );
            }

            else if ( Peripheral.ACTION_DESCRIPTOR_READ.equals(action))
            {
                mServicesAdapter.notifyDataSetChanged();
            }

            else if( Peripheral.ACTION_CHARACTERISTIC_READ.equals( action ) )
            {
                String uuidString = intent.getStringExtra( Peripheral.EXTRA_CHARACTERISTIC_UUID );
                if ( uuidString != null )
                {
                    UUID uuid = UUID.fromString( uuidString );
                    Characteristic extendedCharacteristic = mCharacteristicMapping.get( uuid );

                    mServicesAdapter.notifyDataSetChanged();
                }
            }

            else if( Peripheral.ACTION_RSSI_UPDATED.equals( action ) )
            {
                int rssiValue = intent.getIntExtra( Peripheral.EXTRA_RSSI_VALUE, 0 );

                TextView rssiValueTV = (TextView) findViewById( R.id.rssiValue );
                rssiValueTV.setText( "RSSI: " + Integer.toString( rssiValue ) );
            }
        }
    };


    /***********************************************************************************************
     *
     */
    @Override
    protected void
    onCreate(
            Bundle savedInstanceState
    )
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connected_device);

        this.mContext = this;

        // Start reading RSSI values
        this.mHandler = new Handler();

        // Show the Up button in the action bar.
        setupActionBar();

        Intent intent = getIntent();
        mDeviceName = intent.getStringExtra( EXTRAS_DEVICE_NAME );
        mDeviceAddress = intent.getStringExtra( EXTRAS_DEVICE_ADDRESS );

        // Set the activity title to be the name of the device.
        this.setTitle( mDeviceName );

        registerReceiver( mGattUpdateReceiver, makeGattUpdateIntentFilter() );

        // If we're creating this for the first time, connect to the Bluetooth peripheral.
        if( savedInstanceState == null )
        {
            mPeripheral = new Peripheral( this, mDeviceAddress, true );
        }
    }


    /***********************************************************************************************
     *
     */
    @Override
    protected void
    onResume()
    {
        super.onResume();

        mHandler.postDelayed( readRssiRunnable, RSSI_READ_PERIOD_MS );
    }


    /***********************************************************************************************
     *
     */
    @Override
    protected void
    onPause()
    {
        super.onPause();

        mHandler.removeCallbacks( readRssiRunnable );
    }


    /***********************************************************************************************
     *
     */
    @Override
    protected void
    onDestroy()
    {
        super.onDestroy();

        if( isFinishing() )
        {
            mPeripheral.disconnect();
        }

        unregisterReceiver( mGattUpdateReceiver );
    }



    /***********************************************************************************************
     *
     */
    private void
    setupActionBar()
    {
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }


    /***********************************************************************************************
     *
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.connected_device, menu);
        return true;
    }


    /***********************************************************************************************
     *
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void
    setupDataView()
    {
        // Set the adapter for the services list view.
        mServices = new ArrayList<BluetoothGattService>();
        mServiceCharacteristics = new HashMap<BluetoothGattService, List<Characteristic>>();
        mCharacteristicMapping = new HashMap< UUID, Characteristic>();

        ExpandableListView servicesListView = (ExpandableListView) findViewById( R.id.deviceServicesListView );
        mServicesAdapter = new ConnectedDeviceAdapter(
                this,
                mServices,
                mServiceCharacteristics,
                mPeripheral );

        servicesListView.setAdapter(mServicesAdapter);

        // When a child is clicked, show operations that can be done on that item.
        servicesListView.setOnChildClickListener( new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean
            onChildClick(
                    ExpandableListView parent,
                    View v,
                    int groupPosition,
                    int childPosition,
                    long id
            )
            {
                if( mActionMode != null )
                {
                    mActionMode.finish();
                }

                mServicesAdapter.setSelectedChildItem(groupPosition, childPosition);
                mActionMode = ConnectedDeviceActivity.this.startActionMode( mActionModeCallback );

                v.setSelected( true );
                mServicesAdapter.notifyDataSetChanged();

                return true;
            }
        });
    }


    /***********************************************************************************************
     * Handle action mode UI actions.
     */
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean
        onCreateActionMode(
                ActionMode mode,
                Menu menu
        )
        {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate( R.menu.characteristic_operations, menu );

            MenuItem readItem = menu.findItem( R.id.readCharacteristicMI );
            MenuItem writeItem = menu.findItem( R.id.writeCharacteristicMI );
            MenuItem writeNotResponseItem = menu.findItem( R.id.writeNoResponseItem );
            MenuItem notifyStartItem = menu.findItem( R.id.notifyStartItem );
            MenuItem notifyStopItem = menu.findItem( R.id.notifyStopItem );
            MenuItem indicateStartItem = menu.findItem( R.id.indicateStartItem );
            MenuItem indicateStopItem = menu.findItem( R.id.indicateStopItem );

            Characteristic characteristic = mServicesAdapter.getSelectedCharacteristic();

            if( characteristic != null )
            {
                readItem.setVisible( characteristic.readable );
                writeItem.setVisible( characteristic.writable );
                writeNotResponseItem.setVisible( characteristic.noResponseWritable );
                notifyStartItem.setVisible( characteristic.notifiable && !characteristic.notifying );
                notifyStopItem.setVisible( characteristic.notifiable && characteristic.notifying );
                indicateStartItem.setVisible( characteristic.indicatable && !characteristic.indicating );
                indicateStopItem.setVisible( characteristic.indicatable && characteristic.indicating );
            }

            return true;
        }

        @Override
        public boolean
        onPrepareActionMode(
                ActionMode mode,
                Menu menu
        )
        {
            Characteristic characteristic = mServicesAdapter.getSelectedCharacteristic();

            if( characteristic != null )
            {
                MenuItem notifyStartItem = menu.findItem( R.id.notifyStartItem );
                MenuItem notifyStopItem = menu.findItem( R.id.notifyStopItem );
                MenuItem indicateStartItem = menu.findItem( R.id.indicateStartItem );
                MenuItem indicateStopItem = menu.findItem( R.id.indicateStopItem );

                notifyStartItem.setVisible( characteristic.notifiable && !characteristic.notifying );
                notifyStopItem.setVisible( characteristic.notifiable && characteristic.notifying );
                indicateStartItem.setVisible( characteristic.indicatable && !characteristic.indicating );
                indicateStopItem.setVisible( characteristic.indicatable && characteristic.indicating );
            }

            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            Characteristic characteristic = mServicesAdapter.getSelectedCharacteristic();

            switch ( item.getItemId() )
            {
                case R.id.readCharacteristicMI:
                    characteristic.read();
                    return true;

                case R.id.writeCharacteristicMI:
                    showWriteDialog();
                    return true;

                case R.id.writeNoResponseItem:
                    Toast.makeText(ConnectedDeviceActivity.this, "Write no response coming soon.", Toast.LENGTH_LONG).show();
                    //mode.finish(); // Action picked, so close the CAB
                    return true;

                case R.id.notifyStartItem:
                    characteristic.notify( true );
                    mode.invalidate();
                    return true;

                case R.id.notifyStopItem:
                    characteristic.notify( false );
                    mode.invalidate();
                    return true;

                case R.id.indicateStartItem:
                    characteristic.indicate( true );
                    mode.invalidate();
                    return true;

                case R.id.indicateStopItem:
                    characteristic.indicate( false );
                    mode.invalidate();
                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mServicesAdapter.resetSelectedCharacteristic();
            mServicesAdapter.notifyDataSetChanged();
            mActionMode = null;
        }
    };


    /***********************************************************************************************
     *
     */
    private void
    displayGattServices(
            List< BluetoothGattService > services
    )
    {
        // Set up the data presentation table.
        setupDataView();

        mServices.clear();
        mServiceCharacteristics.clear();

        for( BluetoothGattService service : services )
        {
            mServices.add(service);
            Log.d( TAG, "Adding characteristic: " + service.getUuid().toString() );

            List<Characteristic> extendedBtGattCharacteristics
                    = new ArrayList<Characteristic>();
            for( BluetoothGattCharacteristic characteristic : service.getCharacteristics() )
            {
                extendedBtGattCharacteristics.add( new Characteristic( mPeripheral, characteristic ) );
            }

            mServiceCharacteristics.put(service, extendedBtGattCharacteristics );

            for( Characteristic characteristic : extendedBtGattCharacteristics )
            {
                //mBluetoothLeService.readCharacteristic( characteristic.get() );
                characteristic.read();
                mCharacteristicMapping.put( characteristic.get().getUuid(), characteristic );
                Log.d( TAG, "Adding characteristic: " + characteristic.get().getUuid().toString() );

                if( characteristic.get().getDescriptors() != null )
                {
                    List< BluetoothGattDescriptor > descriptors = characteristic.get().getDescriptors();
                    for( BluetoothGattDescriptor descriptor : descriptors )
                    {
                        mPeripheral.readDescriptor( descriptor );
                    }

                }
            }
        }

        mServicesAdapter.notifyDataSetChanged();
    }


    private void
    showWriteDialog()
    {
        LayoutInflater inflater = this.getLayoutInflater();
        final AlertDialog.Builder builder = new AlertDialog.Builder( this );

        builder.setView( inflater.inflate( R.layout.connected_device_write_dialog, null ) );
        builder.setTitle( "Write Characteristic Value" );
        builder.setNegativeButton( "Cancel", null );

        //final EditText valueText = (EditText) findViewById( R.id.writeValue );

        builder.setPositiveButton("Write",
            new DialogInterface.OnClickListener() {
                public void onClick( DialogInterface dialog, int which )
                {
                    dialog.dismiss();
                    processWriteData( dialog );
                }
            } );

        builder.create().show();
    }


    private void
    processWriteData(
            DialogInterface dialog
    )
    {
        EditText editText = (EditText) ((AlertDialog)dialog).findViewById( R.id.writeValue );
        String writeValue = editText.getText().toString();
        if( writeValue == null ) return;

        RadioGroup valueTypeSelection = (RadioGroup) ((AlertDialog)dialog).findViewById( R.id.dataFormatSelection );
        Characteristic characteristic = mServicesAdapter.getSelectedCharacteristic();
        byte[] data = null;

        switch( valueTypeSelection.getCheckedRadioButtonId() )
        {
            case R.id.writeAsHex:
            {
                // @TODO Have this format it correctly.
                int length = writeValue.length();
                if( length % 2 != 0 || length == 0 ) break;

                data = new byte[ length / 2 ];
                for( int i = 0; i < (length / 2); i++ )
                {
                    // Decode as an integer to account for the inability to have unsigned bytes
                    // with Java.
                    String temp = writeValue.substring( i * 2, (i * 2 ) + 2 );
                    int t = Integer.decode( "0x" + temp );
                    data[ i ] = (byte)t;
                }
            } break;


            case R.id.writeAsInteger:
            {
                try
                {
                    Integer value = Integer.valueOf( writeValue );
                    data = new byte[1];
                    data[0] = value.byteValue();
                }
                catch( NumberFormatException e )
                {
                    Log.d( TAG, e.getMessage() );
                }
            } break;


            case R.id.writeAsString:
                data = writeValue.getBytes();
                break;
        }

        if( !characteristic.write( data ) )
        {
            Toast valueNotIntegerToast = Toast.makeText(
                    ((AlertDialog) dialog).getContext(),
                    "Value formatted incorrectly.",
                    Toast.LENGTH_SHORT );
            valueNotIntegerToast.show();
        }
    }


    /***********************************************************************************************
     *
     */
    private static IntentFilter
    makeGattUpdateIntentFilter()
    {
        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction( Peripheral.ACTION_GATT_CONNECTED);
        intentFilter.addAction( Peripheral.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction( Peripheral.ACTION_SERVICES_DISCOVERED);
        intentFilter.addAction( Peripheral.ACTION_CHARACTERISTIC_WRITE_COMPLETE );
        intentFilter.addAction( Peripheral.ACTION_DESCRIPTOR_READ );
        intentFilter.addAction( Peripheral.ACTION_CHARACTERISTIC_READ );
        intentFilter.addAction( Peripheral.ACTION_RELIABLE_CHARACTERISTIC_WRITE_COMPLETE );
        intentFilter.addAction( Peripheral.ACTION_RSSI_UPDATED );

        return intentFilter;
    }


    /***********************************************************************************************
     * Runnable to read RSSI periodically.
     */
    private Runnable readRssiRunnable = new Runnable() {
        @Override
        public void run() {
            if( mPeripheral != null )
            {
                mPeripheral.readRssi();

                // Repost the activity to run again.
                mHandler.postDelayed( readRssiRunnable, RSSI_READ_PERIOD_MS );
            }
        }
    };
}


