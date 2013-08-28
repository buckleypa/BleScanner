package com.paulbuckley.blescanner;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.paulbuckley.blescanner.adapters.ServicesListViewAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.paulbuckley.blescanner.BluetoothLeService.*;

public class ConnectedDeviceActivity
        extends Activity
{
    private final static String TAG = ConnectedDeviceActivity.class.getSimpleName();

    // Identifiers for establishing the initial connection.
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private String mDeviceName;
    private String mDeviceAddress;

    Context mContext;

    private List< BluetoothGattService > mServices;
    private Map< BluetoothGattService, List< ExtendedBtGattCharacteristic >> mServiceCharacteristics;
    private Map< UUID, ExtendedBtGattCharacteristic > mCharacteristicMapping;

    private ServicesListViewAdapter mServicesListViewAdapter;

    private BluetoothLeService mBluetoothLeService;


    /***********************************************************************************************
     *
     */
    private final TextView.OnEditorActionListener saveServiceName = new TextView.OnEditorActionListener()
    {
        @Override
        public boolean
        onEditorAction(
                TextView v,
                int actionId,
                KeyEvent event
        )
        {
            if( actionId == EditorInfo.IME_NULL
                    && event.getAction() == KeyEvent.ACTION_DOWN )
            {

                ViewSwitcher switcher = (ViewSwitcher) v.findViewById(R.id.serviceNameViewSwitcher);
                EditText nameToSaveView = (EditText) switcher.findViewById( R.id.serviceNameEditView );
                String nameToSave = nameToSaveView.getText().toString();

                TextView textView = (TextView) switcher.findViewById( R.id.serviceNameTextView );
                textView.setText( nameToSave );

                switcher.showNext();
            }
            return true;
        }
    };


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

            if (ACTION_GATT_CONNECTED.equals(action))
            {
                updateConnectionState(R.string.connected);
            }
            else if (ACTION_GATT_DISCONNECTED.equals(action))
            {
                updateConnectionState(R.string.disconnected);

                Intent exitIntent = new Intent( mContext, AdvertisingDevicesActivity.class );
                startActivity( exitIntent );
                finish();
            }
            else if (ACTION_GATT_SERVICES_DISCOVERED.equals(action))
            {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices( mBluetoothLeService.getSupportedGattServices() );
            }
            else if (ACTION_DATA_AVAILABLE.equals(action))
            {
                mServicesListViewAdapter.notifyDataSetChanged();
            }
            else if( ACTION_CHARACTERISTIC_READ.equals( action ) )
            {
                String uuidString = intent.getStringExtra( BluetoothLeService.EXTRA_CHARACTERISTIC_UUID );
                UUID uuid = UUID.fromString( uuidString );
                ExtendedBtGattCharacteristic extendedCharacteristic = mCharacteristicMapping.get( uuid );

                extendedCharacteristic.wasRead();

                mServicesListViewAdapter.notifyDataSetChanged();
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

        Intent gattServiceIntent = new Intent( this, BluetoothLeService.class );
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
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


    private void
    setupDataView()
    {
        // Set the adapter for the services list view.
        mServices = new ArrayList<BluetoothGattService>();
        mServiceCharacteristics = new HashMap<BluetoothGattService, List<ExtendedBtGattCharacteristic>>();
        mCharacteristicMapping = new HashMap< UUID, ExtendedBtGattCharacteristic>();

        ExpandableListView servicesListView = (ExpandableListView) findViewById( R.id.deviceServicesListView );
        mServicesListViewAdapter = new ServicesListViewAdapter(
                this,
                mServices,
                mServiceCharacteristics,
                mBluetoothLeService );

        servicesListView.setAdapter( mServicesListViewAdapter );

        /*
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
                BluetoothGattCharacteristic characteristic
                        = mServiceCharacteristics.get( mServices.get( groupPosition ) ).get( childPosition );

                if( ( characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ ) != 0 )
                {
                    v.setTag( R.string.CHARACTERISTIC_READ_TAG, new Boolean( true ) );
                    mBluetoothLeService.readCharacteristic( characteristic );
                }

                return true;
            }
        });
        */

        servicesListView.setOnItemLongClickListener( new ExpandableListView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(
                    AdapterView<?> parent,
                    View view,
                    int position,
                    long id
            )
            {
                ViewSwitcher switcher = (ViewSwitcher) view.findViewById(R.id.serviceNameViewSwitcher);

                EditText editText = (EditText) switcher.findViewById( R.id.serviceNameEditView );
                String currentUuid = ((BluetoothGattService)mServicesListViewAdapter.getGroup( position ) ).getUuid().toString();

                editText.setText( GattAttributes.lookup( currentUuid ) );
                editText.setOnEditorActionListener( saveServiceName );

                switcher.showNext();

                return true;
            }
        });
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
        // Set up the data presentation table.
        setupDataView();

        mServices.clear();
        mServiceCharacteristics.clear();

        for( BluetoothGattService service : services )
        {
            mServices.add(service);

            List< ExtendedBtGattCharacteristic > extendedBtGattCharacteristics
                    = new ArrayList<ExtendedBtGattCharacteristic>();
            for( BluetoothGattCharacteristic characteristic : service.getCharacteristics() )
            {
                extendedBtGattCharacteristics.add( new ExtendedBtGattCharacteristic( characteristic ) );
            }

            mServiceCharacteristics.put(service, extendedBtGattCharacteristics );

            for( ExtendedBtGattCharacteristic characteristic : extendedBtGattCharacteristics )
            {
                mBluetoothLeService.readCharacteristic( characteristic.get() );
                mCharacteristicMapping.put( characteristic.get().getUuid(), characteristic );

                if( characteristic.get().getDescriptors() != null )
                {
                    List< BluetoothGattDescriptor > descriptors = characteristic.get().getDescriptors();
                    for( BluetoothGattDescriptor descriptor : descriptors )
                    {
                        mBluetoothLeService.readDescriptor( descriptor );
                    }

                }
            }
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
        intentFilter.addAction(ACTION_GATT_CONNECTED);
        intentFilter.addAction(ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(ACTION_DATA_AVAILABLE);
        intentFilter.addAction( ACTION_CHARACTERISTIC_READ );
        return intentFilter;
    }
}


