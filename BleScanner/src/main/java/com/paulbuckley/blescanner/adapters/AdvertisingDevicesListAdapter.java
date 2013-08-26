package com.paulbuckley.blescanner.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.paulbuckley.blescanner.R;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * Created by paulb on 8/12/13.
 */
public class AdvertisingDevicesListAdapter
        extends BaseAdapter
{

    private final Context context;
    private Map< BluetoothDevice, ArrayList<UUID> > mDevices;
    private ArrayList< BluetoothDevice > mDeviceKeys;

    public AdvertisingDevicesListAdapter(
            Context context,
            Map< BluetoothDevice, ArrayList<UUID> > devices
    )
    {
        this.context = context;
        this.mDevices = devices;

        // Set up an array so we can reference a position in the adapter.
        this.mDeviceKeys = new ArrayList<BluetoothDevice>();
        BluetoothDevice[] deviceArray = this.mDevices.keySet().toArray( new BluetoothDevice[ devices.size() ] );
        if( deviceArray != null )
        {
            for( BluetoothDevice device : deviceArray )
            {
                mDeviceKeys.add( device );
            }
        }
    }


    @Override
    public int
    getCount()
    {
        return this.mDevices.size();
    }


    @Override
    public Object
    getItem(
            int position
    )
    {
        return mDeviceKeys.get( position );
    }

    @Override
    public long
    getItemId(
            int position
    )
    {
        return position;
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
        TableLayout deviceUuidsTable = (TableLayout) rowView.findViewById( R.id.deviceUuidsTableLayout );

        BluetoothDevice device = (BluetoothDevice) this.getItem( position );

        // Populate all the fields.
        deviceNameTextView.setText( device.getName() );
        deviceAddrTextView.setText( device.getAddress() );

        ArrayList< UUID > uuids = mDevices.get( device );
        if( uuids != null )
        {
            for( UUID uuid : uuids )
            {
                LayoutInflater rowInflater  = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View v = rowInflater.inflate( R.layout.device_uuid_table_row, deviceUuidsTable, true );

                TableRow row = (TableRow) v.findViewById( R.id.deviceUuidTableRow );
                TextView text = (TextView) v.findViewById( R.id.deviceUuidTextView );
                text.setText( uuid.toString() );

                deviceUuidsTable.addView( row );
            }
        }

        return rowView;
    }


    public void
    addDevice(
            BluetoothDevice device
    )
    {
        mDeviceKeys.add( device );
        mDevices.put(device, new ArrayList<UUID>());
    }

    public void
    clear()
    {
        mDeviceKeys.clear();
        mDevices.clear();
    }
}
