package com.paulbuckley.blescanner;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.app.Activity;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ConnectedDeviceActivity
        extends Activity
{
    private final static String TAG = ConnectedDeviceActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private String mDeviceName;
    private String mDeviceAddress;

    private boolean mConnected = false;

    Context mContext;
    BluetoothGatt mBluetoothGatt;
    private ArrayList< BluetoothGattService > mServices;
    private DeviceServicesListViewAdapter mServicesListViewAdapter;

    private BluetoothLeService mBluetoothLeService;


    /***********************************************************************************************
     *
     */
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void
        onServiceConnected(
                ComponentName componentName,
                IBinder service
        )
        {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();

            if ( !mBluetoothLeService.initialize() )
            {
                Log.e( TAG, "Unable to initialize Bluetooth" );
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect( mDeviceAddress );
        }


        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };


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

            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action))
            {
                mConnected = true;
                updateConnectionState(R.string.connected);
            }
            else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action))
            {
                mConnected = false;
                updateConnectionState(R.string.disconnected);

                Intent exitIntent = new Intent( mContext, AdvertisingDevicesActivity.class );
                startActivity( exitIntent );
            }
            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action))
            {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices( mBluetoothLeService.getSupportedGattServices() );
            }
            else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action))
            {
                //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
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
        setContentView(R.layout.connected_device_layout);

        this.mContext = this;

        // Show the Up button in the action bar.
        setupActionBar();

        Intent intent = getIntent();
        mDeviceName = intent.getStringExtra( EXTRAS_DEVICE_NAME );
        mDeviceAddress = intent.getStringExtra( EXTRAS_DEVICE_ADDRESS );

        // Set the activity title to be the name of the device.
        this.setTitle( mDeviceName );

        // Set the adapter for the services list view.
        mServices = new ArrayList<BluetoothGattService>();
        ListView servicesListView = (ListView) findViewById( R.id.deviceServicesListView );
        mServicesListViewAdapter = new DeviceServicesListViewAdapter( this, mServices );
        servicesListView.setAdapter( mServicesListViewAdapter );

        Intent gattServiceIntent = new Intent( this, BluetoothLeService.class );
        bindService( gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE );
    }


    /***********************************************************************************************
     *
     */
    @Override
    protected void
    onResume()
    {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }


    /***********************************************************************************************
     *
     */
    @Override
    protected void
    onPause()
    {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }


    /***********************************************************************************************
     *
     */
    @Override
    protected void
    onDestroy()
    {
        super.onDestroy();

        mBluetoothLeService.disconnect();

        unbindService(mServiceConnection);
        mBluetoothLeService = null;
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


    /***********************************************************************************************
     *
     */
    private void
    updateConnectionState (
            int stringId
    )
    {
        //TextView connectionStatusTextView = (TextView) findViewById( R.id.connectionStatusTextView );
        //connectionStatusTextView.setText( getString( stringId ) );
    }


    /***********************************************************************************************
     *
     */
    private void
    displayGattServices(
            List< BluetoothGattService > services
    )
    {
        mServices.clear();

        for( BluetoothGattService service : services )
        {
            mServices.add( service );
        }

        mServicesListViewAdapter.notifyDataSetChanged();
    }


    /***********************************************************************************************
     *
     */
    private static IntentFilter
    makeGattUpdateIntentFilter()
    {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
