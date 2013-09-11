package com.paulbuckley.blescanner.adapters;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.graphics.Paint;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.TableLayout;
import android.widget.TextView;

import com.paulbuckley.blescanner.utilities.BluetoothLeService;
import com.paulbuckley.blescanner.types.ExtendedBtGattCharacteristic;
import com.paulbuckley.blescanner.ble_standards.GattUuids;
import com.paulbuckley.blescanner.R;

public class ConnectedDeviceAdapter
        extends BaseExpandableListAdapter
{

    private Activity context;
    private Map< BluetoothGattService, List<ExtendedBtGattCharacteristic> > mServiceCharacteristics;
    private List< BluetoothGattService > mServices;

    private BluetoothLeService mBluetoothLeService;

    private Pair< Integer, Integer> selectedChildItem = null;

    public ConnectedDeviceAdapter(
            Activity context,
            List<BluetoothGattService> services,
            Map<BluetoothGattService, List<ExtendedBtGattCharacteristic>> serviceCharacteristics,
            BluetoothLeService bluetoothLeService
    )
    {
        this.context = context;
        this.mServiceCharacteristics = serviceCharacteristics;
        this.mServices = services;
        this.mBluetoothLeService = bluetoothLeService;
    }


    public Object
    getChild(
            int groupPosition,
            int childPosition
    )
    {
        return mServiceCharacteristics.get( mServices.get( groupPosition ) ).get( childPosition );
    }


    public long
    getChildId(
            int groupPosition,
            int childPosition
    )
    {
        return childPosition;
    }


    public View
    getChildView(
            final int groupPosition,
            final int childPosition,
            boolean isLastChild,
            View convertView,
            ViewGroup parent
    )
    {
        ExtendedBtGattCharacteristic extendedBtGattCharacteristic
                = (ExtendedBtGattCharacteristic) getChild(groupPosition, childPosition);

        BluetoothGattCharacteristic characteristic = extendedBtGattCharacteristic.get();

        LayoutInflater inflater = context.getLayoutInflater();
        
        if (convertView == null)
        {
            convertView = inflater.inflate( R.layout.connected_device_characteristic_list_item, null );
        }
        extendedBtGattCharacteristic.setListItemView( convertView );

        populateCharacteristicMetadata( extendedBtGattCharacteristic );

        updateCharacteristicReadDate( extendedBtGattCharacteristic );

        // Populate the values in the characteristic
        populateCharacteristicValue(convertView, characteristic);

        populateCharacteristicControls(extendedBtGattCharacteristic);

        if( selectedChildItem != null && selectedChildItem.first == groupPosition && selectedChildItem.second == childPosition )
        {
            convertView.setBackgroundColor(this.context.getResources().getColor(android.R.color.holo_blue_light));
        }
        else
        {
            convertView.setBackgroundColor( this.context.getResources().getColor( R.color.characteristic_background ) );
        }

        return convertView;
    }


    public int
    getChildrenCount(
            int groupPosition
    )
    {
        return mServiceCharacteristics.get( mServices.get( groupPosition ) ).size();
    }


    public Object
    getGroup(
            int groupPosition
    )
    {
        return mServices.get(groupPosition);
    }


    public int
    getGroupCount()
    {
        return mServices.size();
    }


    public long
    getGroupId(
            int groupPosition
    )
    {
        return groupPosition;
    }


    public View
    getGroupView(
            int groupPosition,
            boolean isExpanded,
            View convertView,
            ViewGroup parent
    )
    {
        BluetoothGattService service = (BluetoothGattService) getGroup( groupPosition );

        if ( convertView == null )
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate( R.layout.connected_device_service_list_item, null );
        }

        TextView serviceNameTextView = (TextView) convertView.findViewById( R.id.serviceNameTextView );

        String uuid =  service.getUuid().toString();

        if( GattUuids.isKnownUuid(uuid) )
        {
            uuid = GattUuids.lookup(uuid);
        }

        serviceNameTextView.setText( uuid );

        return convertView;
    }


    public boolean
    hasStableIds()
    {
        return true;
    }


    public boolean
    isChildSelectable(
            int groupPosition,
            int childPosition
    )
    {
        return true;
    }


    public void
    updateCharacteristicReadDate (
            ExtendedBtGattCharacteristic characteristic
    )
    {
        TextView dateValueView = (TextView) characteristic.getListItemView().findViewById( R.id.valueDateTextView );

        if( dateValueView != null )
        {
            dateValueView.setText( characteristic.getReadTime().format( "%m-%d-%Y (%H:%M:%S)" ) );
        }
    }


    private void
    populateCharacteristicMetadata(
            ExtendedBtGattCharacteristic extendedBtGattCharacteristic
    )
    {
        View convertView = extendedBtGattCharacteristic.getListItemView();
        BluetoothGattCharacteristic characteristic = extendedBtGattCharacteristic.get();

        // The name of the characteristic is either held in the description "Characteristic User
        // Description" or in GattUuids were we have a temporarily maintained mapping of
        // UUIDs to names.
        TextView charNameTv = (TextView) convertView.findViewById( R.id.characteristicNameTextView );
        TextView charUuidTv = (TextView) convertView.findViewById( R.id.characteristicUuidTextView );
        BluetoothGattDescriptor userDescription
                = characteristic.getDescriptor(UUID.fromString( GattUuids.CHARACTERISTIC_USER_DESCRIPTION ) );
        String characteristicName;
        if( userDescription != null )
        {
            try
            {
                characteristicName = new String ( userDescription.getValue() );
            }
            catch ( NullPointerException e )
            {
                characteristicName = new String( "Error!" );
            }
        }
        else
        {
            characteristicName = GattUuids.lookup(characteristic.getUuid().toString());
        }
        charNameTv.setText( characteristicName );

        // Set the characteristic UUID string
        String shortUuidString = "0x" + characteristic.getUuid().toString().substring( 4, 8 );
        charUuidTv.setText( shortUuidString );
    }


    private void
    populateCharacteristicValue (
            View convertView,
            BluetoothGattCharacteristic characteristic
    )
    {
        TableLayout valueTable = (TableLayout) convertView.findViewById( R.id.valueTableLayout );
        int whenToShowValuesMask = 0
                | BluetoothGattCharacteristic.PROPERTY_READ
                | BluetoothGattCharacteristic.PROPERTY_NOTIFY
                | BluetoothGattCharacteristic.PROPERTY_INDICATE;

        // Get the value, and format it in a default way, or as is described in the "Characteristic
        // Presentation Format" descriptor.
        if( ( characteristic.getProperties() & whenToShowValuesMask ) != 0 )
        {
            TextView asciiValueView = (TextView) convertView.findViewById( R.id.asciiValueTextView );
            TextView hexValueView = (TextView) convertView.findViewById( R.id.hexValueTextView );
            TextView intValueView = (TextView) convertView.findViewById( R.id.intValueTextView );

            final byte[] data = characteristic.getValue();

            // Present the data is there is data read.
            if( data != null )
            {
                // Get value as a string.
                asciiValueView.setText( new String( data ) );

                // Get the value as a hex array
                StringBuilder hexString = new StringBuilder();
                hexString.append( "0x" );
                for( byte hex: data )
                {
                    //hexString.append( String.format( "%2X-", Integer.toHexString( 0xFF & hex ) + "-" ) );
                    hexString.append( String.format( "%02X-", hex ) );
                }
                hexString.deleteCharAt( hexString.length() - 1 );
                hexValueView.setText( hexString.toString() );

                // Get the value as an integer, if possible.
                if( ( 0 < data.length ) && ( data.length <= 4 ) )
                {
                    int result = 0;
                    for( int i = 0; i < data.length; i++ )
                    {
                        result = result | ( ( data[ i ] & 0xFF ) << ( 8 * i ) );
                    }
                    intValueView.setText( Integer.toString( result ) );
                }
                else
                {
                    intValueView.setText( "" );
                }

                // Turn on the table
                valueTable.setVisibility( View.VISIBLE );
            }
            else
            {
                asciiValueView.setText( "..." );
            }
        }
        else
        {
            valueTable.setVisibility( View.GONE );
        }
    }

    /*
    private void
    clientConfigurationSetup(
            ExtendedBtGattCharacteristic extendedCharacteristic
    )
    {
        ToggleButton toggleNotificationsButton
                = (ToggleButton) extendedCharacteristic.getListItemView().findViewById( R.id.toggleNotificationsButton );
        ToggleButton toggleIndicationsButton
                = (ToggleButton) extendedCharacteristic.getListItemView().findViewById( R.id.toggleIndicationsButton );

        // If notifications are possible for this characteristic, show the toggle
        if( ( extendedCharacteristic.get().getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY ) != 0 )
        {
            toggleNotificationsButton.setVisibility( View.VISIBLE );

            toggleNotificationsButton.setTag(
                    R.string.VIEW_CHARACTERISTIC_TAG,
                    extendedCharacteristic.get() );

            toggleNotificationsButton.setOnCheckedChangeListener( onToggleNotificationsClicked );
        }
        else
        {
            toggleNotificationsButton.setVisibility( View.GONE );
        }

        // If indications are possible for this characteristic, show the toggle
        if( (extendedCharacteristic.get().getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE ) != 0 )
        {
            toggleIndicationsButton.setVisibility( View.VISIBLE );

            toggleIndicationsButton.setTag(
                    R.string.VIEW_CHARACTERISTIC_TAG,
                    extendedCharacteristic.get() );

            toggleIndicationsButton.setOnCheckedChangeListener( onToggleIndicationsClicked );
        }
        else
        {
            toggleIndicationsButton.setVisibility( View.GONE );
        }
    }

    */


    private void
    populateCharacteristicControls(
            ExtendedBtGattCharacteristic extendedBtGattCharacteristic
    )
    {
        BluetoothGattCharacteristic characteristic = extendedBtGattCharacteristic.get();

        // Get all the text views
        View baseView = extendedBtGattCharacteristic.getListItemView();
        TextView readTv = (TextView) baseView.findViewById( R.id.doReadTextView );
        TextView writeTv = (TextView) baseView.findViewById( R.id.doWriteTextView );
        TextView notifyTv = (TextView) baseView.findViewById( R.id.doNotifyTextView );
        TextView indicateTv = (TextView) baseView.findViewById( R.id.doIndicateTextView );

        // Get colors to use.
        int inactiveColor = this.context.getResources().getColor( R.color.inactive_option );
        int activeColor = this.context.getResources().getColor( android.R.color.black );

        // Set up handling reads
        if( ( characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ ) != 0 )
        {
            readTv.setTextColor( activeColor );
            readTv.setTag( R.string.VIEW_CHARACTERISTIC_TAG, characteristic );
            readTv.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBluetoothLeService.readCharacteristic((BluetoothGattCharacteristic) v.getTag(R.string.VIEW_CHARACTERISTIC_TAG));
                }
            });
        }
        else
        {
            readTv.setTextColor( inactiveColor );
        }

        // Set up handling writes
        if( ( characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE ) != 0 )
        {
            writeTv.setTextColor( activeColor );
        }
        else
        {
            writeTv.setTextColor( inactiveColor );
        }

        // Set up handling writes
        if( ( characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY ) != 0 )
        {
            notifyTv.setTextColor( activeColor );
            notifyTv.setPaintFlags( notifyTv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG );
        }
        else
        {
            notifyTv.setTextColor( inactiveColor );
        }

        // Set up handling writes
        if( ( characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE ) != 0 )
        {
            indicateTv.setTextColor( activeColor );
        }
        else
        {
            indicateTv.setTextColor( inactiveColor );
        }
    }


    public CompoundButton.OnCheckedChangeListener onToggleNotificationsClicked
            = new CompoundButton.OnCheckedChangeListener() {

        public void
        onCheckedChanged(
                CompoundButton buttonView,
                boolean isChecked
        )
        {
            BluetoothGattCharacteristic characteristic
                    = (BluetoothGattCharacteristic) buttonView.getTag( R.string.VIEW_CHARACTERISTIC_TAG );

            mBluetoothLeService.setCharacteristicNotification( characteristic, isChecked );
        }
    };


    public CompoundButton.OnCheckedChangeListener onToggleIndicationsClicked
            = new CompoundButton.OnCheckedChangeListener() {

        public void
        onCheckedChanged(
                CompoundButton buttonView,
                boolean isChecked
        )
        {
            BluetoothGattCharacteristic characteristic
                    = (BluetoothGattCharacteristic) buttonView.getTag( R.string.VIEW_CHARACTERISTIC_TAG );

            mBluetoothLeService.setCharacteristicIndication( characteristic, isChecked );
        }
    };


    public void
    setSelectedChildItem(
            int groupPosition,
            int childPosition
    )
    {
        selectedChildItem = new Pair< Integer, Integer >( groupPosition, childPosition );
    }


    public void
    resetSelectedCharacteristic()
    {
        selectedChildItem = null;
    }


    public ExtendedBtGattCharacteristic
    getSelectedCharacteristic()
    {
        if( selectedChildItem == null ) return null;

        return (ExtendedBtGattCharacteristic) getChild( selectedChildItem.first, selectedChildItem.second );
    }
}