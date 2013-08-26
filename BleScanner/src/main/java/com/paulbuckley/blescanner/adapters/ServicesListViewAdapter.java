package com.paulbuckley.blescanner.adapters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.text.format.Time;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.paulbuckley.blescanner.BluetoothLeService;
import com.paulbuckley.blescanner.GattAttributes;
import com.paulbuckley.blescanner.R;

public class ServicesListViewAdapter
        extends BaseExpandableListAdapter
{

    private Activity context;
    private Map< BluetoothGattService, List< BluetoothGattCharacteristic > > mServiceCharacteristics;
    private List< BluetoothGattService > mServices;

    private BluetoothLeService mBluetoothLeService;


    public
    ServicesListViewAdapter(
            Activity context,
            List< BluetoothGattService > services,
            Map< BluetoothGattService, List< BluetoothGattCharacteristic > > serviceCharacteristics,
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
        BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) getChild(groupPosition, childPosition);
        LayoutInflater inflater = context.getLayoutInflater();
        
        if (convertView == null)
        {
            convertView = inflater.inflate( R.layout.characteristic_list_item, null );
        }

        // The name of the characteristic is either held in the description "Characteristic User
        // Description" or in GattAttributes were we have a temporarily maintained mapping of
        // UUIDs to names.
        TextView item = (TextView) convertView.findViewById( R.id.characteristicNameTextView );
        BluetoothGattDescriptor userDescription
                = characteristic.getDescriptor(UUID.fromString( GattAttributes.CHARACTERISTIC_USER_DESCRIPTION ) );
        if( userDescription != null )
        {
            byte[] characteristicName = userDescription.getValue();
            if( characteristicName != null )
            {
                item.setText( new String( characteristicName ) );
            }
        }
        else
        {
            item.setText( GattAttributes.lookup( characteristic.getUuid().toString() ) );
        }

        // Populate the values in the characteristic
        populateCharacteristicValue(convertView, characteristic);

        // Populate the write values
        populateCharacteristicWrite( convertView, characteristic );

        // Set up the notify and indication enabling buttons.
        clientConfigurationSetup(convertView, characteristic);

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
            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate( R.layout.service_list_item_layout, null );
        }

        TextView serviceNameTextView = (TextView) convertView.findViewById( R.id.serviceNameTextView );

        String uuid =  service.getUuid().toString();

        if( GattAttributes.isKnownUuid( uuid ) )
        {
            uuid = GattAttributes.lookup(uuid);
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


    private void
    populateCharacteristicValue (
            View convertView,
            BluetoothGattCharacteristic characteristic
    )
    {
        TableLayout valueTable = (TableLayout) convertView.findViewById( R.id.valueTableLayout );

        // Get the value, and format it in a default way, or as is described in the "Characteristic
        // Presentation Format" descriptor.
        if( ( characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ ) != 0 )
        {
            TextView asciiValueView = (TextView) convertView.findViewById( R.id.asciiValueTextView );
            TextView hexValueView = (TextView) convertView.findViewById( R.id.hexValueTextView );
            TextView intValueView = (TextView) convertView.findViewById( R.id.intValueTextView );
            TextView dateValueView = (TextView) convertView.findViewById( R.id.valueDateTextView );

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
                    hexString.append( Integer.toHexString( 0xFF & hex ) + "-" );
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

                // Set the time of the reading.
                Boolean read = (Boolean) convertView.getTag( R.string.CHARACTERISTIC_READ_TAG );
                if( read != null )
                {
                    if( read.booleanValue() )
                    {
                        Time now = new Time();
                        now.setToNow();
                        dateValueView.setText( now.format( "%m-%d-%Y (%H:%M:%S)" ) );

                        convertView.setTag( R.string.CHARACTERISTIC_READ_TAG, new Boolean( false ) );
                    }
                }

                // Turn on the table
                valueTable.setVisibility( View.VISIBLE );
            }
            else
            {
                asciiValueView.setText( "..." );
                dateValueView.setText( "Unread" );
            }
        }
        else
        {
            valueTable.setVisibility( View.GONE );
        }
    }


    private void
    populateCharacteristicWrite(
            View convertView,
            BluetoothGattCharacteristic characteristic
    )
    {
        EditText editText = (EditText) convertView.findViewById( R.id.writeCharacteristicEditText );

        if( ( characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE ) != 0 )
        {
            editText.setTag( R.string.VIEW_CHARACTERISTIC_TAG, characteristic );
            editText.setOnEditorActionListener( new TextView.OnEditorActionListener() {
                @Override
                public boolean
                onEditorAction(
                        TextView v,
                        int actionId,
                        KeyEvent event
                )
                {
                    boolean handled = false;
                    if( actionId == EditorInfo.IME_ACTION_SEND )
                    {
                        BluetoothGattCharacteristic editedCharacteristic
                                = (BluetoothGattCharacteristic) v.getTag( R.string.VIEW_CHARACTERISTIC_TAG );
                        if( editedCharacteristic != null )
                        {
                            byte[] data = v.getText().toString().getBytes();
                            mBluetoothLeService.writeCharaceristic( editedCharacteristic, data );
                            handled = true;
                        }
                    }
                    return handled;
                }
            });
            editText.setVisibility( View.VISIBLE );
        }
        else
        {
            editText.setVisibility( View.GONE );
        }
    }


    private void
    clientConfigurationSetup(
            View convertView,
            BluetoothGattCharacteristic characteristic
    )
    {
        ToggleButton toggleNotificationsButton
                = (ToggleButton) convertView.findViewById( R.id.toggleNotificationsButton );
        ToggleButton toggleIndicationsButton
                = (ToggleButton) convertView.findViewById( R.id.toggleIndicationsButton );

        // If notifications are possible for this characteristic, show the toggle
        if( (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY ) != 0 )
        {
            toggleNotificationsButton.setVisibility( View.VISIBLE );

            toggleNotificationsButton.setTag(
                    R.string.VIEW_CHARACTERISTIC_TAG,
                    characteristic );

            toggleNotificationsButton.setOnCheckedChangeListener( onToggleNotificationsClicked );
        }
        else
        {
            toggleNotificationsButton.setVisibility( View.GONE );
        }

        // If indications are possible for this characteristic, show the toggle
        if( (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE ) != 0 )
        {
            toggleIndicationsButton.setVisibility( View.VISIBLE );

            toggleIndicationsButton.setTag(
                    R.string.VIEW_CHARACTERISTIC_TAG,
                    characteristic );

            toggleIndicationsButton.setOnCheckedChangeListener( onToggleIndicationsClicked );
        }
        else
        {
            toggleIndicationsButton.setVisibility( View.GONE );
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
}