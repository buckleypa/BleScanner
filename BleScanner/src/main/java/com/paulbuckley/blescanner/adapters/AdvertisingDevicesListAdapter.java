package com.paulbuckley.blescanner.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.paulbuckley.blescanner.AdvertisingBleDevice;
import com.paulbuckley.blescanner.R;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * Created by paulb on 8/12/13.
 */
public class AdvertisingDevicesListAdapter
        extends ArrayAdapter
{

    private final Context context;
    //private ArrayList< AdvertisingBleDevice > advertisers;

    public AdvertisingDevicesListAdapter(
            Context context,
            ArrayList<AdvertisingBleDevice> advertisers
    )
    {
        super( context, R.layout.available_device_row_item_layout, advertisers );

        this.context = context;
        //this.advertisers = advertisers;
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
        TextView deviceRssiTV = (TextView) rowView.findViewById( R.id.deviceRssiTV );
        TextView deviceScanRecordTV = (TextView) rowView.findViewById( R.id.deviceScanRecordTV );

        AdvertisingBleDevice advertiser = (AdvertisingBleDevice) this.getItem( position );

        // Populate all the fields.
        deviceNameTextView.setText( advertiser.device.getName() );
        deviceAddrTextView.setText( advertiser.device.getAddress() );
        deviceRssiTV.setText( Integer.toString( advertiser.rssi ) + "db" );

/*
        // Get the value as a hex array
        StringBuilder hexString = new StringBuilder();
        hexString.append( "0x" );
        for( byte hex: advertiser.scanRecord )
        {
            hexString.append( String.format( "%02X-", hex ) );
        }
        hexString.deleteCharAt( hexString.length() - 1 );
        deviceScanRecordTV.setText( hexString.toString() );
  */

        deviceScanRecordTV.setText( advertiser.getLocalName() );

        return rowView;
    }
}
