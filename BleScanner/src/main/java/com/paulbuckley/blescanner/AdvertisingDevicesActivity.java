package com.paulbuckley.blescanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class AdvertisingDevicesActivity
        extends Activity
{
    private AdvertisingDevicesListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private Context mContext;

    private ArrayList< BluetoothDevice > mAdvertisingDevices;

    private static final int REQUEST_ENABLE_BT = 1;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;



    @Override
    protected void
    onCreate(
            Bundle savedInstanceState
    )
    {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.available_devices_layout );

        mContext = this;
        mScanning = false;
        mHandler = new Handler();
        mAdvertisingDevices = new ArrayList< BluetoothDevice >();


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

        ListView advertisingDevicesListView = (ListView) findViewById( R.id.availableDevicesListView );
        mLeDeviceListAdapter = new AdvertisingDevicesListAdapter( this, this.mAdvertisingDevices );
        advertisingDevicesListView.setAdapter( mLeDeviceListAdapter );

        advertisingDevicesListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick (
                            AdapterView<?>  parent,
                            final View      view,
                            int             position,
                            long            id
                    )
                    {
                        scanLeDevice( false );

                        final BluetoothDevice device = mLeDeviceListAdapter.getItem( position );

                        if( device == null ) return;

                        Intent intent = new Intent( mContext, ConnectedDeviceActivity.class );
                        intent.putExtra( ConnectedDeviceActivity.EXTRAS_DEVICE_NAME, device.getName() );
                        intent.putExtra( ConnectedDeviceActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress() );

                        startActivity( intent );
                    }
                }
        );

        scanLeDevice( true );
    }


    @Override
    public void
    onResume()
    {
        super.onResume();

        scanLeDevice( true );
    }


    @Override
    public void
    onPause()
    {
        super.onPause();

        scanLeDevice( false );

        // Reset what is displayed.
        mAdvertisingDevices.clear();
        mLeDeviceListAdapter.notifyDataSetChanged();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


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


    public void
    startScan(
            View view
    )
    {
        scanLeDevice( false );

        scanLeDevice( true );
    }


    private void
    scanLeDevice(
            final boolean enable
    )
    {
        if( enable )
        {
            // If already scanning, just restart the callback for turning scanning off.
            if( mScanning )
            {
                mHandler.removeCallbacks( delayedDisableScanning );
            }
            // Else if not already scanning, set the device to be scanning.
            else
            {
                mBluetoothAdapter.startLeScan(mLeScanCallback);

                // Reset what is displayed.
                mAdvertisingDevices.clear();
                mLeDeviceListAdapter.notifyDataSetChanged();
            }
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
            }
        }
    }


    // Device scan callback.
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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLeDeviceListAdapter.add(device);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };

    // Create a runnable that cancels scanning that we can run later.
    private Runnable delayedDisableScanning = new Runnable() {
        @Override
        public void run() {
            mScanning = false;
            mBluetoothAdapter.stopLeScan( mLeScanCallback );
        }
    };


}
