package com.paulbuckley.blescanner;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.ParcelUuid;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by paulb on 8/12/13.
 */
public class AdvertisingDevicesListAdapter
        extends ArrayAdapter< BluetoothDevice >
{

    private final Context context;
    private ArrayList< BluetoothDevice > devices;

    public AdvertisingDevicesListAdapter(
            Context context,
            ArrayList<BluetoothDevice> devices
    )
    {
        super( context, R.layout.available_device_row_item_layout, devices );
        this.context = context;
        this.devices = devices;
    }


    @Override
    public View
    getView (
            int         position,
            View        convertView,
            ViewGroup   parent
    )
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View rowView = inflater.inflate( R.layout.available_device_row_item_layout, parent, false );

        TextView deviceNameTextView = (TextView) rowView.findViewById( R.id.deviceNameTextView );
        TextView deviceAddrTextView = (TextView) rowView.findViewById( R.id.deviceAddrTextView );

        // Populate all the fields.
        deviceNameTextView.setText(devices.get(position).getName());
        deviceAddrTextView.setText( devices.get( position ).getAddress() );

        return rowView;
    }
}
