package com.paulbuckley.blescanner;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public class DeviceServicesListViewAdapter
        extends ArrayAdapter< BluetoothGattService >
{

    private final Context mContext;

    private ArrayList< BluetoothGattService > mServices;

    public DeviceServicesListViewAdapter (
            Context context,
            ArrayList< BluetoothGattService > services
    )
    {
        super( context, R.layout.available_device_row_item_layout, services );
        this.mContext = context;
        this.mServices = services;
    }


    @Override
    public View
    getView (
            int         position,
            View        convertView,
            ViewGroup   parent
    )
    {
        BluetoothGattService service = mServices.get(position);

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View rowView = inflater.inflate( R.layout.service_list_item_layout, parent, false );

        // Get the views
        TextView serviceNameTextView = (TextView) rowView.findViewById( R.id.serviceNameTextView );
        TextView serviceUuidTextView = (TextView) rowView.findViewById( R.id.serviceUuidTextView );

        // Populate all the fields.
        serviceNameTextView.setText( GattAttributes.lookup(service.getUuid().toString()) );
        serviceUuidTextView.setText( service.getUuid().toString() );

        ArrayList< BluetoothGattCharacteristic > characteristics = new ArrayList<BluetoothGattCharacteristic>();
        CharacteristicListAdapter characteristicListAdapter = new CharacteristicListAdapter( mContext, characteristics );
        ListView characteristicsListView = (ListView) rowView.findViewById( R.id.serviceCharacteristicsListView );
        characteristicsListView.setAdapter( characteristicListAdapter );

        for( BluetoothGattCharacteristic characteristic : service.getCharacteristics() )
        {
            characteristics.add( characteristic );
            characteristicListAdapter.notifyDataSetChanged();
        }

        return rowView;
    }
}
