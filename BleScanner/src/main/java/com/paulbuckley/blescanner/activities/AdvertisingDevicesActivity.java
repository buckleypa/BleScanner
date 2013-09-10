package com.paulbuckley.blescanner.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.paulbuckley.blescanner.R;
import com.paulbuckley.blescanner.adapters.AdvertisingDevicesAdapter;
import com.paulbuckley.blescanner.types.AdvertisingBleDevice;

import java.util.ArrayList;

public class AdvertisingDevicesActivity
        extends Activity
{
    private final static String TAG = AdvertisingDevicesActivity.class.getSimpleName();

    private AdvertisingDevicesAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private Context mContext;

    private ArrayList< AdvertisingBleDevice > mAdvertisers;

    private static final int REQUEST_ENABLE_BT = 1;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;


    /***********************************************************************************************
     *
     */
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if( BluetoothDevice.ACTION_UUID.equals( action ) )
            {

            }
            else if ( BluetoothDevice.ACTION_FOUND.equals( action ) )
            {
                /*
                BluetoothDevice device = (BluetoothDevice) mLeDeviceListAdapter.getItem( 0 );
                if( device != null )
                {
                    device.fetchUuidsWithSdp();
                }
                */
            }
        }
    };


    /***********************************************************************************************
     *
     */
    private static IntentFilter
    makeIntentFilter()
    {
        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction( BluetoothDevice.ACTION_UUID );
        intentFilter.addAction( BluetoothDevice.ACTION_FOUND );

        return intentFilter;
    }


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
        setContentView( R.layout.advertising_devices );

        this.setTitle( R.string.available_devices_string );

        mContext = this;
        mScanning = false;
        mHandler = new Handler();

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // If Bluetooth isn't currently turned on, get the user to turn it on.
        if( mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled() )
        {
            Intent enableBtIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
            startActivityForResult( enableBtIntent, REQUEST_ENABLE_BT );
        }

        mAdvertisers = new ArrayList<AdvertisingBleDevice>();

        ExpandableListView advertisingDevicesListView = (ExpandableListView) findViewById( R.id.advertisingDevicesListView );
        mLeDeviceListAdapter = new AdvertisingDevicesAdapter( this, this.mAdvertisers );
        advertisingDevicesListView.setAdapter( mLeDeviceListAdapter );

        advertisingDevicesListView.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener()
                {
                    @Override
                    public boolean onItemLongClick (
                            AdapterView<?>  parent,
                            final View      view,
                            int             position,
                            long            id
                    )
                    {
                        scanLeDevice( false );

                        int groupPosition = ExpandableListView.getPackedPositionGroup( id );

                        try
                        {
                            final AdvertisingBleDevice advertiser = (AdvertisingBleDevice) mLeDeviceListAdapter.getGroup( groupPosition );

                            if( advertiser == null ) return false;

                            Intent intent = new Intent( mContext, ConnectedDeviceActivity.class );
                            intent.putExtra( ConnectedDeviceActivity.EXTRAS_DEVICE_NAME, advertiser.device.getName() );
                            intent.putExtra( ConnectedDeviceActivity.EXTRAS_DEVICE_ADDRESS, advertiser.device.getAddress() );

                            startActivity( intent );
                        }
                        catch( ArrayIndexOutOfBoundsException e )
                        {
                            Log.d( TAG, e.getMessage() );
                        }

                        return true;
                    }
                }
        );

        scanLeDevice( true );
    }


    /***********************************************************************************************
     *
     */
    @Override
    public void
    onResume()
    {
        super.onResume();

        registerReceiver( mBroadcastReceiver, makeIntentFilter() );

        scanLeDevice( true );
    }


    /***********************************************************************************************
     *
     */
    @Override
    public void
    onPause()
    {
        super.onPause();

        scanLeDevice( false );

        // Reset what is displayed.
        mLeDeviceListAdapter.clear();
        mLeDeviceListAdapter.notifyDataSetChanged();

        unregisterReceiver( mBroadcastReceiver );
    }


    /***********************************************************************************************
     *
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    /***********************************************************************************************
     *
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch ( item.getItemId() )
        {
            case R.id.action_rescan:
                scanLeDevice(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /***********************************************************************************************
     *
     */
    public void
    startScan(
            View view
    )
    {
        scanLeDevice( true );
    }


    /***********************************************************************************************
     *
     */
    private void
    scanLeDevice(
            final boolean enable
    )
    {
        ProgressBar progressBar = (ProgressBar) findViewById( R.id.scanDurationProgress );
        TextView textComplete = (TextView) findViewById( R.id.scanCompleteTextView );

        if( enable )
        {
            progressBar.setVisibility( View.VISIBLE );
            textComplete.setVisibility( View.GONE );

            // Reset what is displayed.
            mLeDeviceListAdapter.clear();
            mLeDeviceListAdapter.notifyDataSetChanged();

            // If already scanning, just restart the callback for turning scanning off.
            if( mScanning )
            {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                mHandler.removeCallbacks( delayedDisableScanning );
            }

            mBluetoothAdapter.startLeScan(mLeScanCallback);

            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed( delayedDisableScanning, SCAN_PERIOD );
            mScanning = true;
        }
        else
        {
            if( mScanning )
            {
                mScanning = false;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);

                progressBar.setVisibility( View.GONE );
            }

            textComplete.setVisibility( View.VISIBLE );
            mHandler.removeCallbacks( delayedDisableScanning );
        }

    }


    /***********************************************************************************************
     * Device scan callback
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void
                onLeScan(
                        final BluetoothDevice device,
                        int rssi,
                        byte[] scanRecord
                )
                {
                    mAdvertisers.add( new AdvertisingBleDevice ( device, rssi, scanRecord ) );

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };



    /***********************************************************************************************
     *Create a runnable that cancels scanning that we can run later.
     */
    private Runnable delayedDisableScanning = new Runnable() {
        @Override
        public void run() {
            scanLeDevice( false );
        }
    };
}
